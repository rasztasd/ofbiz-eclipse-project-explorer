package org.ofbiz.plugin.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Project;

public class OfbizModelSingleton {
	private static OfbizModelSingleton instance = new OfbizModelSingleton();
	private Map<String, Project> projectsByName = new HashMap<String, Project>();
	public static OfbizModelSingleton get() {
		return instance;
	}
	public void addProject(String eclipseProjectName, Project ofbizProject) {
		projectsByName.put(eclipseProjectName, ofbizProject);
	}
	public Project findProjectByEclipseProjectName(String name) {
		return projectsByName.get(name);
	}
	public Collection<Project> getAllProjects() {
		return projectsByName.values();
	}
	public Project findActiveEclipseProject() {
		IProject activeProject = getActiveProject();
		if (activeProject == null) {
			return null;
		} else {
			String name = activeProject.getName();
			return projectsByName.get(name);
		}
	}
	public IProject getActiveProject() {
		IEditorInput editorInput = Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getSite().getWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			return ((IFileEditorInput) editorInput).getFile().getProject();
		} else {
			return null;
		}
	}
	public Component findActiveComponent() {
		IEditorInput editorInput = Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getSite().getWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) editorInput).getFile();
			file.getName();			
			file.getProject();
		}
		return null;
	}
	public IFile getActiveFile() {
		IEditorInput editorInput = Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getSite().getWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) editorInput).getFile();
			return file;
		}
		return null;
	}
}
