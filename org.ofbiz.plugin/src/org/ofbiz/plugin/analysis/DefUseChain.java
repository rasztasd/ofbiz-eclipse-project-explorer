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
 * $Id: DefUseChain.java,v 1.1 2008/01/17 18:48:18 hessellund Exp $
 */
package org.ofbiz.plugin.analysis;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
class DefUseChain {

	private ReachingDefinitionAnalysis rda;
	private MethodDeclaration method;
	
	DefUseChain(ReachingDefinitionAnalysis rda, MethodDeclaration method) {
		assert rda != null;
		assert rda.isDone : "analysis must have been performed initially";
		assert rda.cfg.method == method : "wrong method";
		this.rda = rda;
		this.method = method;
	}

	// inspired by StaticAnalysisFor JavaInEclipse p.87
	Set<Statement> findRefsToDef(ASTNode def) {
		final Set<Statement> result = new HashSet<Statement>();
		final SimpleName name;
		if(def instanceof SingleVariableDeclaration) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) def;
			name = svd.getName();
		} else if (def instanceof Assignment) { //TODO: impl ref2def for assigment
			throw new RuntimeException("not implemented yet");
		} else { //TODO: impl andre typer ref2def
			throw new RuntimeException("not implemented yet");
		}
		final IVariableBinding defBinding = (IVariableBinding) name.resolveBinding();
		method.accept(new ASTVisitor() {
			@Override public boolean visit(SimpleName node) {
				// why is this necessary?
				if(node.resolveBinding()!=defBinding) return false;
				Statement stmt = getContainingStatement(node);
				if (stmt==null) return false;
				for (Pair<String, ASTNode> pair : rda.getInSet(stmt)) {
					if(pair.first.equals(name.getIdentifier())) {
						result.add(stmt); // stmt contains node, ok?
					}
				}
				return false;
			}
		});
		return result;
	}

	// inspired by StaticAnalysisFor JavaInEclipse p.88
	Set<ASTNode> findDefsForRef(SimpleName ref) {
		final Set<ASTNode> result = new HashSet<ASTNode>();
		Statement stmt = getContainingStatement(ref);
		if (stmt==null) 
			throw new RuntimeException("invalid reference "+Util.getFirstLine(ref));
		// if the variable is in the entry-set of the statement
		// then add the corresponding ASTNode.
		for (Pair<String, ASTNode> pair : rda.getInSet(stmt)) {
			if(pair.first.equals(ref.getIdentifier())) {
				result.add(pair.second); // stmt contains node, ok?
			}
		}
		return result;
	}
	
	/** returns the statement that contains the given node or null if no such statement can be found */
	private Statement getContainingStatement(ASTNode node) {
		assert node != null;
		while(!(node instanceof Statement)) {
			node = node.getParent();
			if (node==null) return null;
		}
		return (Statement) node;
	}
	
}
