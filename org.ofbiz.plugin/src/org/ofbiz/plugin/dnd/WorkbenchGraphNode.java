package org.ofbiz.plugin.dnd;

import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.IContainer;
import org.ofbiz.plugin.ofbiz.HasXmlDefinition;

public class WorkbenchGraphNode extends GraphNode {
	private HasXmlDefinition xmlDef;
	public WorkbenchGraphNode(HasXmlDefinition xmlDef, IContainer graphModel, int style, String text) {
		super(graphModel, style, text);
		this.xmlDef = xmlDef;
	}
	public HasXmlDefinition getXmlDef() {
		return xmlDef;
	}
	
}
