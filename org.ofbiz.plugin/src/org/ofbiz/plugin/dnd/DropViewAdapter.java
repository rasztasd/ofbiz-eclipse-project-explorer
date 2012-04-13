package org.ofbiz.plugin.dnd;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphNode;
import org.ofbiz.plugin.ofbiz.HasXmlDefinition;
import org.ofbiz.plugin.parser.GoToFile;

public class DropViewAdapter extends ViewerDropAdapter {
	Graph graphControl;

	public DropViewAdapter(Viewer viewer, Graph graphControl) {
		super(viewer);
		this.graphControl = graphControl;
	}

	@Override
	public boolean performDrop(Object data) {
		if (data instanceof HasXmlDefinition) {
			final HasXmlDefinition dummyImpl = (HasXmlDefinition) data;
			WorkbenchGraphNode graphNode = new WorkbenchGraphNode(dummyImpl, graphControl, SWT.NONE, dummyImpl.getNameToShow());
		}
		graphControl.applyLayout();
		return true;
	}

	@Override
	public boolean validateDrop(Object target, int operation,
			TransferData transferType) {
		// TODO Auto-generated method stub
		return OfbizTransfer.getInstance().isSupportedType(transferType);
	}

}
