package bsh.syntaxtree.visitor;

public interface BeanshellElement {
	public boolean accept(BeanshellVisitor visitor);
}
