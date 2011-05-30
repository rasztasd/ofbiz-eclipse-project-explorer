package org.ofbiz.plugin.analysis;
/**
 * Type-parametric 2-tuple class.
 * @author <a href="mailto:matt@cis.ksu.edu">Matt Hoosier</a>
 */
class Pair<E1, E2> {
	final E1 first;

	final E2 second;

	Pair(E1 first, E2 second) {
		this.first = first;
		this.second = second;
	}

	@Override public int hashCode() {
		return first.hashCode() + second.hashCode();
	}

	@Override public boolean equals(Object o) {
		if (o instanceof Pair) {
			Pair other = (Pair) o;
			return first.equals(other.first) && second.equals(other.second);
		}
		return false;
	}

	@Override public String toString() {
		return "<" + first + ", " + second + ">";
	}
}
