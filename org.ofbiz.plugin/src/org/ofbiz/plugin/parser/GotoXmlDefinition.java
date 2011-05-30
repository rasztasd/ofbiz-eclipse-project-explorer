package org.ofbiz.plugin.parser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.ofbiz.HasXmlDefinition;


public class GotoXmlDefinition implements IObjectActionDelegate {
	private IWorkbenchPart targetPart;
	private ISelection selection;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	@Override
	public void run(IAction arg0) {
		// TODO Auto-generated method stub
		Object treeSelect = ((TreeSelection)selection).getFirstElement();
		if (treeSelect instanceof HasXmlDefinition) {
			HasXmlDefinition hasXmlDefinition = (HasXmlDefinition) treeSelect;
			IMarker marker = resolveMarker(hasXmlDefinition.getFile(), hasXmlDefinition.getMarkerKey(), "xml");
			openFile(marker);
		}
	}
	public static IMarker resolveMarker(IFile file, String name, String fileExtension) {
		try {
			IMarker[] markers = 
				file.findMarkers(
				Plugin.TEXT_MARKER, true, IResource.DEPTH_INFINITE);
			for(IMarker m : markers) {
				if(m.getAttribute("name").equals(name) &&
				   m.getResource().getName().endsWith(fileExtension)) {
					return m;
				}
			}
		} catch (CoreException e) {
			Plugin.logError("Unable to resolve marker "+name,e);
		}
		return null;
	}
	public static boolean openFile(IMarker marker) {
		IWorkbenchPage page = 
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(page, marker);
			return true;
		} catch (PartInitException e) {
			Plugin.logInfo("Unable to open editor for "+marker, e);
			return false;
		}
	}
}
