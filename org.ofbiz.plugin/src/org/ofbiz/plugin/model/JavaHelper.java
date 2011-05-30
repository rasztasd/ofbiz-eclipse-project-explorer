package org.ofbiz.plugin.model;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.ide.IDE;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.model.hyperlink.HyperlinkMarker;
import org.ofbiz.plugin.parser.GoToFile;

public class JavaHelper {
	//TODO put this code to appropriate place
	public static CompilationUnit parse(ICompilationUnit lwUnit) {
		if (lwUnit != null) {
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(lwUnit); // set source
			parser.setResolveBindings(true); // we need bindings later on
			return (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
		} else {
			return null;
		}
	}
	public static class GotoJavaMethodAST extends ASTVisitor {
		private IType findType;
		private List<HyperlinkMarker> hyperlinkMarkers;
		private CompilationUnit parse;
		private String invoke;
		private IDocument doc;
		private int lineNumber = -1;
		private int offset;
		private int length;
		private int parameterNumber = -1;

		public GotoJavaMethodAST(IType findType,
				List<HyperlinkMarker> hyperlinkMarkers, CompilationUnit parse,
				String invoke, IDocument doc, int lineNumber, int offset, int length, int parameterNumber) {
			this.findType = findType;
			this.hyperlinkMarkers = hyperlinkMarkers;
			this.parse = parse;
			this.invoke = invoke;
			this.doc = doc;
			this.offset = offset;
			this.lineNumber = lineNumber;
			this.length = length;
			this.parameterNumber = parameterNumber;
		}

		public GotoJavaMethodAST(IType findType,
				List<HyperlinkMarker> hyperlinkMarkers, CompilationUnit parse,
				String invoke) {
			this.findType = findType;
			this.hyperlinkMarkers = hyperlinkMarkers;
			this.parse = parse;
			this.invoke = invoke;
		}

		@Override
		public boolean visit(final MethodDeclaration node) {
			final IMethodBinding resolveMethodBinding = node.resolveBinding();
			if (resolveMethodBinding == null) {
				return true;
			}
			String methodName = resolveMethodBinding.getName();
			if (methodName.equals(invoke)) {
				if (parameterNumber == -1 || parameterNumber == resolveMethodBinding.getParameterTypes().length) {
					final IFile file = (IFile) findType.getResource();
					hyperlinkMarkers.add(new HyperlinkMarker() {

						@Override
						public String getTypeLabel() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public String getHyperlinkText() {
							StringBuilder types = new StringBuilder("");
							int i = 0;
							for (ITypeBinding type : resolveMethodBinding.getParameterTypes()) {
								if (i != 0) {
									types.append(", ");
								}
								types.append(type.getName());
								i++;
							}
							return "Goto java: " + invoke + "parameters: (" + types.toString() + ")";
						}

						@Override
						public void open() {
							try {
								IMarker marker = file.createMarker(Plugin.TEXT_MARKER);
								marker.setAttribute(IMarker.LINE_NUMBER, parse.getLineNumber(node.getStartPosition()));
								IDE.openEditor(GoToFile.getActiveWorkbenchPage(), marker);
							} catch (CoreException e) {
							}
						}
						@Override
						public IRegion getHyperlinkRegion() {
							try {
								if (lineNumber == -1) {
									return null;
								}
								return new Region(doc.getLineOffset(lineNumber) + offset - 1, length + 1);
							} catch (BadLocationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return null;
						}
					});
				}
			}
			return true;
		}
	}

}
