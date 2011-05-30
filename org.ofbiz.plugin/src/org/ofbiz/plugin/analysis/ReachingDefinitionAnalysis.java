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
 * $Id: ReachingDefinitionAnalysis.java,v 1.1 2008/01/17 18:48:18 hessellund Exp $
 */
package org.ofbiz.plugin.analysis;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
//inspired by source code from a compiler course by Robby
class ReachingDefinitionAnalysis extends
		MonotonicDataFlowFramework<Pair<String, ASTNode>> {

	/**
	 * computes reaching definitions for each statement in a single method
	 * Lattice element: (variable x defining node)
	 * @param cfg The control flow graph of the method
	 */
	ReachingDefinitionAnalysis(ControlFlowGraph cfg) {
		super(cfg, true, true);
	}

	@Override void computeFixPoint() {
		Set<Pair<String, ASTNode>> init = new HashSet<Pair<String, ASTNode>>();
		for (Object o : cfg.method.parameters()) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
			init.add(new Pair<String, ASTNode>(svd.getName().getIdentifier(),
					svd));
		}
		computeFixPoint(init);
	}

	@Override String getAnalysisName() {
		return "Reaching Definition";
	}

	@Override String toString(Pair<String, ASTNode> e) {
		return e.first + " <"+Util.getFirstLine(e.second)+">";
	}

	@SuppressWarnings("unchecked")
	@Override protected Set<Pair<String, ASTNode>> gen(Set<Pair<String, ASTNode>> set, Statement stmt) {
		Set<Pair<String, ASTNode>> result = new HashSet<Pair<String, ASTNode>>();
		
		// assignments generate a def
		if (stmt instanceof ExpressionStatement) {
			ExpressionStatement es = (ExpressionStatement) stmt;
			Expression e = es.getExpression();
			if (e instanceof Assignment) {
				Assignment a = (Assignment) e;
				Expression lhs = a.getLeftHandSide();
				
				// special case, lhs is an ArrayAccess node
				if (!(lhs instanceof SimpleName)) return result;
				
				SimpleName sn = (SimpleName) lhs;
				String lhsLocalName = sn.getIdentifier();
				if (lhsLocalName != null) {
					result.add(new Pair<String, ASTNode>(lhsLocalName, stmt));
				}
			}
			
		// variable declarations generate a def
		} else if (stmt instanceof VariableDeclarationStatement) {
			VariableDeclarationStatement vds = (VariableDeclarationStatement) stmt;
			List<VariableDeclarationFragment> fragments = vds.fragments();
			for (VariableDeclarationFragment vdf : fragments) {
				if (vdf.getName().getIdentifier() != null) {
					result.add(new Pair<String, ASTNode>(vdf.getName().getIdentifier(), stmt));
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override protected Set<Pair<String, ASTNode>> kill(Set<Pair<String, ASTNode>> set, Statement stmt) {
		Set<Pair<String, ASTNode>> result = new HashSet<Pair<String, ASTNode>>();
		
		// expressions kill a def
		if (stmt instanceof ExpressionStatement) {
			ExpressionStatement es = (ExpressionStatement) stmt;
			Expression e = es.getExpression();
			if (e instanceof Assignment) {
				Assignment a = (Assignment) e;
				Expression lhs = a.getLeftHandSide();
				
				// special case, lhs is an ArrayAccess node
				if (!(lhs instanceof SimpleName)) return result;
				
				SimpleName sn = (SimpleName) lhs;
				String lhsLocalName = sn.getIdentifier();
				if (lhsLocalName != null) {
					for (Pair<String, ASTNode> pair : set) {
						if (pair.first.equals(lhsLocalName)) {
							result.add(pair);
						}
					}
				}
			}
			
		// variable declarations kill a def
		} else if (stmt instanceof VariableDeclarationStatement) {
			VariableDeclarationStatement vds = (VariableDeclarationStatement) stmt;
			List<VariableDeclarationFragment> fragments = vds.fragments();
			for (VariableDeclarationFragment vdf : fragments) {
				if (vdf.getName().getIdentifier() != null) {
					for (Pair<String, ASTNode> pair : set) {
						if (pair.first.equals(vdf.getName().getIdentifier())) {
							result.add(pair);
						}
					}
				}
			}
		}
		return result;
	}
}