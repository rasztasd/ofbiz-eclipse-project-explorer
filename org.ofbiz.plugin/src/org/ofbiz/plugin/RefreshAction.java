package org.ofbiz.plugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.ofbiz.plugin.model.OfbizModelSingleton;
import org.ofbiz.plugin.nature.OfbizNature;
import org.ofbiz.plugin.ofbiz.Project;


public class RefreshAction extends Action {
	private final ExplorerView view;
	public RefreshAction(ExplorerView view) {
		this.view = view;
	}
	public void run() {
		view.getRoot().getProjects().clear();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : workspaceRoot.getProjects()) {
			try {
				if (project.getNature(OfbizNature.ID) != null) {
					LoadOperation loadOperation = new LoadOperation(project);
					loadOperation.schedule();
				}
				Project ofbizProject = OfbizModelSingleton.get().findProjectByEclipseProjectName(project.getName());
				if (ofbizProject != null) {
					ofbizProject.setRoot(view.getRoot());
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	private String path;
}
