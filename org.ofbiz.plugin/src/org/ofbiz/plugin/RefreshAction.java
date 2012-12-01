package org.ofbiz.plugin;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.ofbiz.plugin.nature.OfbizNature;


public class RefreshAction extends Action {
	private final ExplorerView view;
	public RefreshAction(ExplorerView view) {
		this.view = view;
	}
	public void run() {
		view.getRoot().getProjects().clear();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (final IProject project : workspaceRoot.getProjects()) {
			try {
				if (project.getNature(OfbizNature.ID) != null) {
					project.deleteMarkers("org.ofbiz.plugin.text", true, IResource.DEPTH_INFINITE);
					LoadOperation loadOperation = new LoadOperation(view.getRoot(), project);
					new ProgressMonitorDialog(view.getSite().getShell()).run(true, true, loadOperation);
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		view.getViewer().refresh();
	}
}