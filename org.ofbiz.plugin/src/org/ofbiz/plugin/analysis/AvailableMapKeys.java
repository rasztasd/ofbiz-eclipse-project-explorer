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
 * $Id: AvailableMapKeys.java,v 1.1 2008/01/17 18:48:19 hessellund Exp $
 */
package org.ofbiz.plugin.analysis;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.ofbiz.plugin.Plugin;

class AvailableMapKeys extends
		MonotonicDataFlowFramework<Pair<String, String>> {

	/**
	 * computes available map keys for each statement in a single method
	 * Lattice element: (map x key)
	 * @param cfg The control flow graph of the method
	 */
	AvailableMapKeys(ControlFlowGraph cfg) {
		super(cfg, true, false);
	}

	private Set<Pair<String, String>> universe = new HashSet<Pair<String, String>>();
	
	@Override void computeFixPoint() {
		// compute universe of possible ( map x key ) combinations
		computeUniverse(new HashSet<Statement>(), cfg.start);
		// compute fixpoint with all EXIT(B) set to universe
		computeFixPoint(new HashSet<Pair<String, String>>(),universe);
	}

	@Override String getAnalysisName() {
		return "Avaliable Map Keys";
	}

	@Override String toString(Pair<String, String> e) {
		return "<"+e.first+","+e.second+">";
	}

	/** returns null if the statement is not a ExpressionStatement 
	 * with an embedded method invocation */
	private MethodInvocation getMethodInvocation(Statement stmt) {
		if (stmt instanceof ExpressionStatement) {
			ExpressionStatement es = (ExpressionStatement) stmt;
			Expression expr = es.getExpression();
			if (expr instanceof MethodInvocation) {
				return (MethodInvocation) expr;
			}
		}
		// only handles variable declaration statements with a single fragment. TODO: handle more fragments
		if (stmt instanceof VariableDeclarationStatement) {
			VariableDeclarationStatement vds = (VariableDeclarationStatement) stmt;
			if (vds.fragments().size()==1) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) vds.fragments().get(0);
				Expression expr = vdf.getInitializer();
				if (expr instanceof MethodInvocation) {
					return (MethodInvocation) expr;
				}
			}
		}
		return null;
				
	}
	
	/** returns a pair (map x key) from a java.util.Map.put()-operation */
	private Pair<String,String> getPutOperation(MethodInvocation mi) {
		Expression expr = mi.getExpression();
		if (expr==null) return null;
		ITypeBinding binding = expr.resolveTypeBinding();
		if ("java.util.Map".equals(binding.getQualifiedName()) &&
			"put".equals(mi.getName().getIdentifier())) {
			
			if(!(expr instanceof SimpleName)) {
				// special case, screensAtt.getContext().put("formStringRenderer", foFormRenderer);
				return null;
			}
			
			SimpleName variable = (SimpleName) expr;
			Object key = mi.arguments().get(0);
			
			// ordinary strings
			if (key instanceof StringLiteral) {
				String keyContent = key.toString().replace("\"", "");
				return new Pair<String,String>(variable.getIdentifier(),keyContent);
			// constants
			} else if (key instanceof QualifiedName) {
				QualifiedName name = (QualifiedName) key;
				Object obj = name.resolveConstantExpressionValue();
				if (obj!=null && obj instanceof String) {
					return new Pair<String,String>(variable.getIdentifier(),obj.toString());
				}
			}
		}
		return null;
	}
	
	private Map<String, Set<Statement>> namesUsedInInterproceduralCalls = new HashMap<String, Set<Statement>>();
	
	boolean isNameUsedInInterproceduralCall(String name) {
		return namesUsedInInterproceduralCalls.containsKey( name );
	}
	
	Set<Statement> getInterproceduralCalls( String name ) {
		assert namesUsedInInterproceduralCalls.containsKey( name );
		return namesUsedInInterproceduralCalls.get( name );
	}
	
	private void addNameToInterproceduralCallSet(String name, Statement location) {
		Set<Statement> set;
		if (namesUsedInInterproceduralCalls.containsKey( name )) {
			set = namesUsedInInterproceduralCalls.get( name );
		} else {
			set = new HashSet<Statement>();
			namesUsedInInterproceduralCalls.put( name , set );
		}
		set.add( location );
	}
	
	@Override protected Set<Pair<String, String>> gen(Set<Pair<String, String>> set, Statement stmt) {
		Set<Pair<String, String>> result = new HashSet<Pair<String, String>>();
		
		MethodInvocation mi = getMethodInvocation(stmt);
		// is it a method invocation?
		if (mi!=null) {
			// is it a java.util.Map.put()-operation?
			Pair<String,String> pair = getPutOperation(mi);
			if (pair!= null) {
				result.add(pair);
				return result;
			}
			
			
			// check for interprocedural calls
			String clazz = mi.resolveMethodBinding().getDeclaringClass().getQualifiedName();
			if (!"org.ofbiz.base.util.UtilMisc".equals(clazz)  
				&& !"org.ofbiz.service.ServiceUtil".equals(clazz)
				&& !"javolution.util.FastMap".equals(clazz)){
				// check if any of the arguments is a java.util.Map
				for(Object obj : mi.arguments()) {
					if (obj instanceof SimpleName) {
						SimpleName arg = (SimpleName) obj;
						ITypeBinding argType = arg.resolveTypeBinding();
						IBinding argBinding = arg.resolveBinding();
						if ("java.util.Map".equals(argType.getQualifiedName())) {
							addNameToInterproceduralCallSet(argBinding.getName(), stmt);
						}
					}
				}
				// handles Map out = doSomething(..)
				if (stmt instanceof VariableDeclarationStatement) {
					VariableDeclarationStatement vds = (VariableDeclarationStatement) stmt;
					//TODO: handle VariableDeclarationStatement with more than one fragment
					if (vds.fragments().size()==1) {
						VariableDeclarationFragment vdf = 
							(VariableDeclarationFragment) vds.fragments().get(0);
						IVariableBinding var = vdf.resolveBinding();
						if ("java.util.Map".equals(var.getType().getQualifiedName())) {
							addNameToInterproceduralCallSet(var.getName(), stmt);
						}
					}
				}
				// handles out.putAll(..)
				if ("putAll".equals(mi.getName().getIdentifier())) {
					Expression expr = mi.getExpression();
					if (expr instanceof SimpleName) {
						if ("java.util.Map".equals(expr.resolveTypeBinding().getQualifiedName())) {
							String name = ((SimpleName) expr).getIdentifier();
							addNameToInterproceduralCallSet(name, stmt);
						}
					}
				}
			}
		}
		
		// handles assignment: "out = dispatcher.runSynch(...)
		if (stmt instanceof ExpressionStatement) {
			ExpressionStatement exprStmt = (ExpressionStatement) stmt;
			if (exprStmt.getExpression() instanceof Assignment) {
				Assignment assignment = (Assignment) exprStmt.getExpression();
				String assignmentType = assignment.resolveTypeBinding().getQualifiedName();
				if ("java.util.Map".equals(assignmentType)) {
					//TODO: this is to simple. Could be something else than a simplename
					if (assignment.getLeftHandSide() instanceof SimpleName) {
						SimpleName sn = (SimpleName) assignment.getLeftHandSide();
						addNameToInterproceduralCallSet(sn.getIdentifier(), stmt);
					}
				}
			}
		}
		
		return result;
	}

	@Override protected Set<Pair<String, String>> kill(Set<Pair<String, String>> set, Statement stmt) {
		Set<Pair<String, String>> result = new HashSet<Pair<String, String>>();
		
		// is it a method invocation?
		MethodInvocation mi = getMethodInvocation(stmt);
		if (mi!=null) {
			Expression expr = mi.getExpression();
			if (expr==null) return result;
			ITypeBinding binding = expr.resolveTypeBinding();
			
			// clear-calls removes all keys
			if ("java.util.Map".equals(binding.getQualifiedName()) &&
				"clear".equals(mi.getName().getIdentifier())) {
				assert universe!=null : "universe must be initialized!";
				
				if (expr instanceof SimpleName) {
					String var = ((SimpleName) expr).getIdentifier();
					for(Pair<String,String> pair : universe) {
						if (pair.first.equals(var)) {
							result.add(pair);
						}
					}
				}
			}
			
			// remove-calls removes a single key
			if ("java.util.Map".equals(binding.getQualifiedName()) &&
				"remove".equals(mi.getName().getIdentifier())) {
				SimpleName variable = (SimpleName) expr;
				Object key = mi.arguments().get(0);
				if (key instanceof StringLiteral) {
					String keyContent = key.toString().replace("\"", "");
					result.add(new Pair<String,String>(variable.getIdentifier(),keyContent));
				}
			}
		}
		
		// "out = null;" or "out = new HashMap();
		if (stmt instanceof ExpressionStatement) {
			ExpressionStatement exprStmt = (ExpressionStatement) stmt;
			Expression expr = exprStmt.getExpression();
			if (expr instanceof Assignment) {
				Assignment assignment = (Assignment) expr;
				Expression lhs = assignment.getLeftHandSide();
				ITypeBinding lhsBinding = lhs.resolveTypeBinding();
				if ("java.util.Map".equals(lhsBinding.getQualifiedName())) {
					String var = ((SimpleName) lhs).getIdentifier();
					Expression rhs = assignment.getRightHandSide();
					if (rhs instanceof SimpleName &&
					    ((SimpleName) rhs).getIdentifier().equals(var)) {
						// out = out => ignore
					} else {
						// out = ...
						for(Pair<String,String> pair : universe) {
							if (pair.first.equals(var)) {
								result.add(pair);
							}
						}
					}
					
				}
			}
		}
		
		return result;
	}
	
	private void computeUniverse(Set<Statement> seen, Statement stmt) {
		MethodInvocation mi = getMethodInvocation(stmt);
		if (mi!=null) {
			Pair<String,String> pair = getPutOperation(mi);
			if (pair!=null) {
				universe.add(pair);
			}
		}
		seen.add(stmt);
		// TODO: ignore blocks from cfg?
		if (stmt instanceof Block) return;
		// recursion
		try {
			for(Statement successor : cfg.successors.get(stmt)) {
				if (!seen.contains(successor))
					computeUniverse(seen, successor);
			}
		} catch (RuntimeException e) {
			Plugin.logError("ERROR in "+Util.getFirstLine(stmt), e);
			throw new RuntimeException(e);
		}
	}
}