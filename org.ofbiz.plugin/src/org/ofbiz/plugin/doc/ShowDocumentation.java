package org.ofbiz.plugin.doc;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.ofbiz.plugin.ofbiz.HasDocumentation;


public class ShowDocumentation implements IObjectActionDelegate {
	private IWorkbenchPart targetPart;
	private ISelection selection;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	@Override
	public void run(IAction arg0) {
		// TODO Auto-generated method stub
		Object treeSelect = ((TreeSelection)selection).getFirstElement();
		if (treeSelect instanceof HasDocumentation) {
			HasDocumentation hasDoc = (HasDocumentation) treeSelect;
//			ToolTip toolTip = new ToolTip(targetPart.getSite().getShell(), SWT.ICON_INFORMATION);
//			toolTip.setVisible(true);
//			toolTip.setAutoHide(true);
//			toolTip.setMessage(hasDoc.getDocumentation());
		}
	}

}
