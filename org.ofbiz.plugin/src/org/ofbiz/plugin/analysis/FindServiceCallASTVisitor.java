package org.ofbiz.plugin.analysis;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.ofbiz.plugin.ofbiz.Service;


public class FindServiceCallASTVisitor extends ASTVisitor {
	private List<Service> services;
	
	public FindServiceCallASTVisitor(List<Service> services) {
		this.services = services;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding binding = node.resolveMethodBinding();
		if (binding.getDeclaringClass().getQualifiedName().equals("org.ofbiz.service.DispatchContext")) {
			Object expression = node.arguments().get(0);
			if (expression instanceof StringLiteral) {
				StringLiteral stringLiteral = (StringLiteral) expression;
				stringLiteral.getEscapedValue().replaceAll("\"", "");
			}
		}
		return super.visit(node);
	}


}
