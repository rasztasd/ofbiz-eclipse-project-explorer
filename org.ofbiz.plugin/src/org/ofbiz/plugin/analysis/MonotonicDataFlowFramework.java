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
 * $Id: MonotonicDataFlowFramework.java,v 1.1 2008/01/17 18:48:19 hessellund Exp $
 */
package org.ofbiz.plugin.analysis;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.Statement;
//inspired by source code from a compiler course by Robby
abstract class MonotonicDataFlowFramework<E> {
	protected ControlFlowGraph cfg;

	protected Map<Statement, Set<E>> outMap = new HashMap<Statement, Set<E>>();

	protected Set<E> init;

	protected Set<E> bottom;

	protected boolean isForward;

	/** aka least upper bound/union (true) or greatest upper bound/intersection (false) */
	protected boolean meetOperator;

	protected boolean isDone;

	/**
	 * Construct a customized data flow analysis
	 * @param cfg 
	 * 		The control flow graph of a method
	 * @param isForward 
	 * 		The direction of analysis, e.g., forward for Reaching Defs and backward for Liveness
	 * @param isLeastUpperBound
	 * 		The <i>meet</i> operator is either union (true) or intersection (false)
	 */
	MonotonicDataFlowFramework(ControlFlowGraph cfg, boolean isForward,
			boolean meetOperator) {
		assert cfg != null;
		this.cfg = cfg;
		this.isForward = isForward;
		this.meetOperator = meetOperator;
	}

	/** Computes the fix point solution. */
	abstract void computeFixPoint();

	abstract String toString(E e);

	abstract String getAnalysisName();

	String getResultString() {
		StringBuilder sb = new StringBuilder("*** " + getAnalysisName()
				+ " for ");
		sb.append(Util.getFirstLine(cfg.method));
		sb.append(" ***\n*** InSet Map ***\n");
		sb.append(getResultString(true));
		sb.append("\n*** OutSet Map ***\n");
		sb.append(getResultString(false));
		return sb.toString();
	}

	/**
	 * Returns the in-set of a {@link Statement}.
	 * @param stmt The {@link Statement}.
	 * @return The in-set of the given {@link Statement}.
	 */
	Set<E> getInSet(Statement stmt) {
		assert stmt != null;
		Set<E> inSet;
		boolean first = true;
		if (isForward ? stmt == cfg.start : stmt == cfg.end) {
			inSet = new HashSet<E>(init);
			first = false;
		} else {
			inSet = new HashSet<E>();
		}
		Set<Statement> set = isForward ? cfg.predecessors.get(stmt)
				: cfg.successors.get(stmt);
		if (set == null) {
			return inSet;
		}
		for (Statement predS : set) {
			if (first) {
				inSet.addAll(getOutSet(predS));
				first = false;
			} else {
				if (meetOperator) {
					inSet.addAll(getOutSet(predS));
				} else {
					inSet.retainAll(getOutSet(predS));
				}
			}
		}
		return inSet;
	}

	/**
	 * Returns the out-set of a {@link Statement}.
	 * @param stmt The {@link Statement}.
	 * @return The out-set of the given {@link Statement}.
	 */
	Set<E> getOutSet(Statement stmt) {
		assert stmt != null;

		Set<E> result = outMap.get(stmt);
		if (result == null) {
			result = new HashSet<E>(bottom);
			outMap.put(stmt, result);
		}
		return result;
	}

	protected String getResultString(boolean isInSet) {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> list = new ArrayList<String>();
		for (Statement s : outMap.keySet()) {
			sb.append("("+s.getStartPosition()+") ");
			if (s == cfg.end) {
				sb.append("(VIRTUAL LAST)");
			} 
			sb.append(Util.getFirstLine(s));
			sb.append("\t==>\t");
			TreeSet<String> ts = new TreeSet<String>();
			for (E e : (isInSet ? getInSet(s) : getOutSet(s))) {
				ts.add(toString(e));
			}
			for (String str : ts) {
				sb.append(str);
				sb.append("  #  ");
			}
			String str = sb.toString();
			sb.setLength(0);
			list.add(str.substring(0, str.length() - 5) + "\n");
		}
		Collections.sort(list);
		for (String s : list) {
			sb.append(s);
		}
		return sb.toString();
	}

	protected void computeFixPoint(Set<E> init) {
		computeFixPoint(init, new HashSet<E>());
	}

	protected void computeFixPoint(Set<E> init, Set<E> bottom) {
		this.bottom = bottom;
		if (isDone) {
			return;
		}
		isDone = true;
		this.init = init;
		Set<Statement> seen = new HashSet<Statement>();
		while (iterate(seen, isForward ? cfg.start : cfg.end)) {
			seen.clear();
		}
	}

	protected boolean iterate(Set<Statement> seen, Statement stmt) {
		if (seen.contains(stmt)) {
			return false;
		}
		boolean hasChanged = compute(stmt);
		seen.add(stmt);
		Set<Statement> succs = isForward ? cfg.successors.get(stmt)
				: cfg.predecessors.get(stmt);
		if (succs != null) {
			for (Statement succS : succs) {
				hasChanged = iterate(seen, succS) || hasChanged;
			}
		}
		return hasChanged;
	}

	protected boolean compute(Statement stmt) {
		Set<E> inSet = getInSet(stmt);
		inSet.removeAll(kill(inSet, stmt));
		inSet.addAll(gen(inSet, stmt));
		Set<E> outSet = getOutSet(stmt);
		if (outSet.size() != inSet.size() || !outSet.containsAll(inSet)
				|| !inSet.containsAll(outSet)) {
			outSet.clear();
			outSet.addAll(inSet);
			inSet.clear();
			return true;
		}
		inSet.clear();
		return false;
	}

	/** definitions generated by this statement */
	protected abstract Set<E> gen(Set<E> set, Statement stmt);
	/** definitions killed by this statement */
	protected abstract Set<E> kill(Set<E> set, Statement stmt);
}
