package org.ofbiz.plugin.parser;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.ofbiz.plugin.model.JavaHelper;
import org.ofbiz.plugin.ofbiz.JavaFile;
import org.ofbiz.plugin.ofbiz.JavaFiles;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.ServiceJavaImpl;

public class JavaServiceParser {

	public JavaServiceParser(JavaFiles javaFiles, IJavaProject javaProject, final Set<String> invokes, String type) {
		try {
			if (invokes.size() == 0) {
				return;
			}
			final JavaFile javaFile = OfbizFactory.eINSTANCE.createJavaFile();
			javaFile.setName(type);
			javaFile.setJavaFiles(javaFiles);
			IType findType = javaProject.findType(type);
			if (findType == null) {
				return;
			}
			final ICompilationUnit cu = findType.getCompilationUnit();
			final CompilationUnit parse = JavaHelper.parse(cu);
			if (parse != null) {
				ASTVisitor astVisitor = new ASTVisitor(){
					@Override
					public boolean visit(final MethodDeclaration node) {
						final IMethodBinding resolveMethodBinding = node.resolveBinding();
						if (resolveMethodBinding == null) {
							return true;
						}
						String methodName = resolveMethodBinding.getName();
						if (invokes.contains(methodName)) {
							ServiceJavaImpl javaImpl = OfbizFactory.eINSTANCE.createServiceJavaImpl();
							javaImpl.setJavaFile(javaFile);
							final String markerKey = "javaImpl" + methodName;
							javaImpl.setMarkerKey(markerKey);
							javaImpl.setFile((IFile) cu.getResource());
							javaImpl.setName(methodName);
							javaImpl.setNameToShow(methodName);
							
							try {
								IMarker marker = cu.getResource().createMarker("org.ofbiz.plugin.javaServiceImplMarker");
								marker.setAttribute(IMarker.LINE_NUMBER, parse.getLineNumber(node.getStartPosition()));
								marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
								marker.setAttribute("name", markerKey);
							} catch (CoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						return true;
					}
				};
				parse.accept(astVisitor);
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}