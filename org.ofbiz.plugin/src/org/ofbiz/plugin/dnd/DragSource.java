package org.ofbiz.plugin.dnd;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.ofbiz.plugin.ofbiz.HasXmlDefinition;

public class DragSource extends DragSourceAdapter {
	TreeViewer viewer;
	public DragSource(TreeViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		event.doit = !viewer.getSelection().isEmpty();
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
		if (!event.doit) {
			return;
		}
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		Object firstElement = selection.getFirstElement();
		if (firstElement != null && firstElement instanceof HasXmlDefinition) {
			event.data = firstElement;
		}
	}

}
