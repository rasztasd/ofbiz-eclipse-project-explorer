/**
 * Copyright 2008 Anders Hessellund 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: Analysis.java,v 1.1 2008/01/17 18:48:19 hessellund Exp $
 */
package org.ofbiz.plugin.analysis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.ofbiz.Attribute;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.parser.AttributeFinder;
import org.ofbiz.plugin.parser.FinderException;

public class Analysis {
	private static boolean invocationRunAlready = false;
	private int invocationMapKeys = 0;;
	private static Map<String, IMarker> serviceInvocationMarkers = new HashMap<String, IMarker>();
	private final AnalysisContext[] contexts;
	private final IJavaProject javaProject; 
	
	private static List<Service> asList(Service service) {
		assert service != null;
		List<Service> list = new ArrayList<Service>();
		list.add(service);
		return list;
	}
	
	public Analysis(IJavaProject javaProject, Service service, Project p) {
		this(javaProject,Analysis.asList(service), p);
	}
	
	public Analysis(IJavaProject javaProject, Service service) {
		this(javaProject, service, null);
	}
	
	public Analysis(IJavaProject javaProject, List<Service> services, Project p) {
		assert javaProject != null;
		assert javaProject.exists();
		assert services != null;
		this.javaProject = javaProject;
		this.contexts = new AnalysisContext[ services.size() ];
		for(int i = 0; i < services.size(); i++) {
			assert services.get(i) != null;
			this.contexts[i] = new AnalysisContext();
			this.contexts[i].javaProject = javaProject;
			this.contexts[i].service = services.get(i);
		}
	}
	
	private void analyzeOutMap(final AnalysisContext ctx,ControlFlowGraph cfg) throws FinderException {
		
		//TODO: if the in-map also serves as out-map then the initial keys should be available
		
		// get mandatory keys for out-map
		final List<String> mandatoryKeys = new ArrayList<String>();
		List<Attribute> attributes = new AttributeFinder(ctx.service).getAttributes();
		for(Attribute attr : attributes) {
			if (!attr.isOptional() &&
				(attr.getMode().getLiteral().equals("OUT") || attr.getMode().getLiteral().equals("INOUT"))) {
				mandatoryKeys.add(attr.getName());
			}
		}
		
		if (mandatoryKeys.isEmpty()) return; // skip analysis
		
		AvailableMapKeys amk = new AvailableMapKeys(cfg);
		amk.computeFixPoint();
				
		// find all relevant return statements in methodbody
		final List<ReturnStatement> returns = new ArrayList<ReturnStatement>();
		ctx.method.accept(new ASTVisitor() {
			@Override public boolean visit(ReturnStatement returnStmt) {
				Expression expr = returnStmt.getExpression();
				// basic case, e.g., return out;
				if (expr instanceof SimpleName) {
					returns.add(returnStmt);
					return false;
				} 
				// special cases
				if (expr instanceof MethodInvocation) {
					MethodInvocation mi = (MethodInvocation) expr;
					IMethodBinding binding = mi.resolveMethodBinding();
					// ignore ServiceUtil.java methods
					if(binding.getDeclaringClass().getQualifiedName().equals("org.ofbiz.service.ServiceUtil")) {
						return false;
					}
					// handle the toMap methods
					if(binding.getDeclaringClass().getQualifiedName().equals("org.ofbiz.base.util.UtilMisc") &&
					   binding.getName().equals("toMap")) {
						Set<String> keys = new HashSet<String>();
						//TODO: handle toMap(String[])
						for(int i = 0; i<mi.arguments().size();i+=2) {
							// collect keys from toMap(..)-call
							if (mi.arguments().get(i) instanceof StringLiteral) {
								keys.add(((StringLiteral)mi.arguments().get(i)).getLiteralValue());
							} 
							// handle special case of service execution errors (ModelService-class)
							if (mi.arguments().get(i) instanceof QualifiedName
								&& mi.arguments().get(i+1) instanceof QualifiedName) {
								QualifiedName keyQN = (QualifiedName)mi.arguments().get(i);
								QualifiedName valueQN = (QualifiedName)mi.arguments().get(i+1);
								String key = keyQN.resolveConstantExpressionValue().toString();
								String value = valueQN.resolveConstantExpressionValue().toString();
								// service execution errors are ignored
								if (key.equals("responseMessage") &&
									(value.equals("error") || value.equals("fail"))) {
									return false;	
								} else {
									keys.add(key);
								}
							}
						}
						
						for(String key : mandatoryKeys) {
							if (!keys.contains(key)) {
								ctx.error(returnStmt,"Missing mandatory output-parameter: "+key);
							} 
						}
						return false;
					}
				}
				ctx.warn(returnStmt, "Unable to analyze complex returns: "+Util.getFirstLine(returnStmt));
				return false;
			}
		});
		
		// check key usage for each return statement
		for(ReturnStatement stmt : returns) {
			String variable = ((SimpleName) stmt.getExpression()).getIdentifier();
			
			// is there a statement on this path in the control flow which
			// uses the out-map as a parameter? e.g., doSomething( out );
			
			if (amk.isNameUsedInInterproceduralCall( variable )) {
				for(Statement s : amk.getInterproceduralCalls( variable )) {
					ctx.warn( s , "Unable to analyze interprocedural calls");
				}
				
			} else {
				
			// regular analysis
				
				Set<Pair<String,String>> outSet = amk.getOutSet(stmt);
				for(String key : mandatoryKeys) {
					if (!outSet.contains(new Pair<String,String>(variable,key))) {
						if(outSet.contains(new Pair<String,String>(variable,"responseMessage")) ||
						   outSet.contains(new Pair<String,String>(variable,"errorMessage"))) {
							// might be the special case of service execution error (ModelService-class)
							ctx.warn(stmt, "Might be missing mandatory output parameter: "+key);
						} else {
							ctx.error(stmt, "Missing mandatory output-parameter: "+key);
						}
					} 
				}				
			}

		}
	}
	
	private void analyzeInMap(final AnalysisContext ctx,DefUseChain duc) throws FinderException {
		// second param in serviceimpls is the in-map
		ASTNode inMap = (ASTNode) ctx.method.parameters().get(1);
		
		// get references to this map
		Set<Statement> refs = duc.findRefsToDef(inMap);
		
		// get valid keys for in-map
		final List<String> validInKeys = new ArrayList<String>();
		validInKeys.add("userLogin");
		validInKeys.add("locale");
		List<Attribute> attributes = new AttributeFinder(ctx.service).getAttributes();
		for(Attribute attr : attributes) {
			if (attr.getMode().getLiteral().equals("IN") || attr.getMode().getLiteral().equals("INOUT")) {
				validInKeys.add(attr.getName());
			} 	
		}
		
		/* RULE: For any execution of the method it should hold that 
		 * if in.get(X) is executed then key X must be available in the model.
		 */
		for (Statement ref : refs) {
			
			ref.accept(new ASTVisitor() {
				@Override public boolean visit(MethodInvocation invocation) {
					
					// resolve binding
					IMethodBinding binding = invocation.resolveMethodBinding();
					if (binding==null) 
						throw new RuntimeException("Unable to resolve binding for "+Util.getFirstLine(invocation));
					
					//TODO: what about clear- and put-calls for in-map?
					
					// filter out anything but get-calls
					String methodName = binding.getName();
					if (methodName==null || !methodName.equals("get"))
						return super.visit(invocation);
					
					// only allow get-calls from java.util.Map
					String declaringClass = binding.getDeclaringClass().getQualifiedName();
					if (!declaringClass.equals("java.util.Map"))
						return super.visit(invocation);
					
					// check argument, non-StringLiteral arguments are flagged as errors
					Object expression = invocation.arguments().get(0);
					if (!(expression instanceof StringLiteral)) {
						ctx.warn(invocation,"Cannot analyze expression: "+Util.getFirstLine(expression));
						return super.visit(invocation);
					}
					
					// check argument, arguments must be valid keys
					String argument = ((StringLiteral) expression).getEscapedValue().replaceAll("\"", "");
					if (!validInKeys.contains(argument)) {
						ctx.error(invocation,"Undeclared input-parameter: "+argument);
						return super.visit(invocation);
					}
					
					// ok :)
					return super.visit(invocation);
				}
			});
		}
		invocationRunAlready = true;
	}

	/** runs the analysis
	 * @return no of successful analysis
	 */
	public int run(boolean resetMarkers) {
		int noOfSuccesfulAnalysis = 0;
		Map<String,Set<AnalysisContext>> location2context = mapLocationsToContexts();
		for(Entry<String,Set<AnalysisContext>> entry : location2context.entrySet()) {
			String location = entry.getKey();
			Set<AnalysisContext> contexts = entry.getValue();
			Plugin.logInfo("checking location "+location, null);
			try {
				IType type = this.javaProject.findType( location );
				if ( type == null ) { 
					Plugin.logError("  Unable to locate type on build path", null);
					continue;
				}
				IFile file = (IFile) type.getResource();
				if ( file == null ) {
					Plugin.logError("  Unable to retrieve file", null);
					continue;
				}
				ICompilationUnit icu = type.getCompilationUnit();
				if ( icu == null ) {
					Plugin.logError("  Unable to locate ICompilationUnit", null);
					continue;
				}
				CompilationUnit cu = parse( icu );
				if ( cu == null ) {
					Plugin.logError("  Unable to parse", null);
					continue;
				}
				for(Iterator<AnalysisContext> iter = contexts.iterator(); iter.hasNext();) {
					AnalysisContext ctx = iter.next();
					ctx.file = file;
					ctx.cu = cu;
					ctx.method = getMethod(ctx.service.getInvoke(), ctx.cu);
					if (ctx.method==null) {
						Plugin.logError("  Unable to locate method "+ctx.service.getName(), null);
						continue;
					}
					try {
						IMarker marker = file.createMarker(Plugin.TEXT_MARKER);
						marker.setAttribute(IMarker.CHAR_START, ctx.method.getStartPosition());
						marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
						marker.setAttribute("name", ctx.service.getName());
						Plugin.logInfo("Marker created for "+ctx.service.getName()+
							" in file "+file.getFullPath(),null);
					} catch (CoreException e) {
						Plugin.logError("Unable to create marker for "
							+ctx.method.getName().getFullyQualifiedName(), e);
					}
					// this is an analyzable service, so run analysis
					boolean ok = false;
					try {
						if(resetMarkers) {
							IMarker [] markers = ctx.file.findMarkers(
								Plugin.PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
							for(IMarker m : markers) {
								if (m.getAttribute("name", "").equals(ctx.service.getName())){
									m.delete();
								}
							}
						}
						ControlFlowGraph cfg = new ControlFlowGraph(ctx.method);
						ReachingDefinitionAnalysis rda = new ReachingDefinitionAnalysis(cfg);
						rda.computeFixPoint();	
						DefUseChain duc = new DefUseChain(rda,ctx.method);
						analyzeInMap(ctx,duc);
						String returnType = ctx.method.getReturnType2().resolveBinding().getQualifiedName(); 
						if (returnType.equals("java.util.Map")) {							
							analyzeOutMap(ctx,cfg);
						}
						ok = true; noOfSuccesfulAnalysis++;
					} catch (AnalysisException ae) {
						Plugin.logError("  Unable to analyze "+ctx, ae);
					} finally {
						Plugin.logInfo("  Analysis of "+ctx.service.getName()
							+" "+(ok?"succeeded":"failed"), null);
						ctx.dispose();
					}
				}
				// clean up 
				cu = null;
				icu = null;
				file = null;
				type = null;
			} catch (Exception e) {
				Plugin.logError("Caught an exception during analysis of location: "+location, e);
			} 
		}
		return noOfSuccesfulAnalysis;
	}
	
	/** create a map from location to a set of (co-located) contexts */
	private Map<String,Set<AnalysisContext>> mapLocationsToContexts() {
		Map<String,Set<AnalysisContext>> location2context = 
			new HashMap<String, Set<AnalysisContext>>();
		int countNonJavaServices = 0;
		for(AnalysisContext ctx : contexts) {
			if (!ctx.service.getEngine().equals("java")) {
				countNonJavaServices++;
				continue;
			}
			assert ctx.service.getLocation() != null;
			assert ctx.service.getInvoke() != null;
			if (location2context.containsKey(ctx.service.getLocation())) {
				Set<AnalysisContext> values = 
					location2context.get(ctx.service.getLocation());
				values.add(ctx);
			} else {
				Set<AnalysisContext> values = new HashSet<AnalysisContext>();
				values.add(ctx);
				location2context.put(ctx.service.getLocation(), values);
			}
		}
		return location2context;
	}
	
	/** parse using {@link AST.JSL3} */
	private CompilationUnit parse(ICompilationUnit lwUnit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(lwUnit); // set source
		parser.setResolveBindings(true); // we need bindings later on
		return (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
	}
	
	// TODO: getMethod does not handle overload
	private MethodDeclaration getMethod(String name,CompilationUnit cu) {
		return getMethods(cu).get(name);
	}
	
	/** returns all methods (taking two params) in a given {@link CompilationUnit}*/
	private Map<String,MethodDeclaration> getMethods(final CompilationUnit cu) {
		final Map<String,MethodDeclaration> methods = new HashMap<String,MethodDeclaration>();
		cu.accept(new ASTVisitor(){
			@Override public boolean visit(MethodDeclaration node) {
				// naive filtering
				if (node.parameters().size()==2)
					methods.put(node.getName().toString(), node);
				return false; // skip children
			}
		});
		return methods;
	}
	
	/** stores all relevant information for the analysis of a single service */
	static class AnalysisContext {
		private void dispose() {
			javaProject = null;
			service = null;
			method = null;
			file = null;
			cu = null;
		}
		private IJavaProject javaProject;
		private Service service;
		private MethodDeclaration method;
		private IFile file;
		private CompilationUnit cu;
		void warn(ASTNode node, String message) {
			mark(node, message, IMarker.SEVERITY_WARNING);
		}
		void error(ASTNode node, String message) {
			mark(node, message, IMarker.SEVERITY_ERROR);
		}

		private void mark(ASTNode node, String message, int type) {
			assert javaProject != null && javaProject.exists();
			assert service != null;
			assert method != null;
			assert file != null && file.exists();
			assert cu != null;
			assert node != null;
			assert message != null;
			try {
				int linenumber = cu.getLineNumber(node.getStartPosition());
				IMarker [] markers = file.findMarkers(Plugin.PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
				for(IMarker m : markers) {
					String msg = "["+service.getName()+"] "+message;
					if (m.getAttribute("name", "").equals(service.getName()) &&
						m.getAttribute(IMarker.MESSAGE, "").equals(msg) &&
						m.getAttribute(IMarker.LINE_NUMBER, -1)==linenumber) {
						// skip marker creation
						return;
					}
				}
				IMarker marker = file.createMarker(Plugin.PROBLEM_MARKER);
				marker.setAttribute(IMarker.MESSAGE, "["+service.getName()+"] "+message);
				marker.setAttribute(IMarker.CHAR_START, node.getStartPosition());
				marker.setAttribute(IMarker.CHAR_END, node.getStartPosition() + node.getLength());
				marker.setAttribute(IMarker.LINE_NUMBER, linenumber);
				marker.setAttribute(IMarker.SEVERITY, type);
				marker.setAttribute("method", method.getName().getFullyQualifiedName());
				marker.setAttribute("name", service.getName());
				assert marker.exists();
			} catch (CoreException ce) {
				throw new AnalysisException("Unable to create markers for file: "+file.getName(),ce);
			}
		}
	}
	public static IMarker getMarkerLookupKey(String key) {
		return serviceInvocationMarkers.get(key);
	}
}