package org.ofbiz.plugin.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.HasXmlDefinition;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.util.OfbizSwitch;

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
	public Set<HasXmlDefinition> getAllFilesWithXmlDefinitions() {
		Set<HasXmlDefinition> xmlDefinitions = new HashSet<HasXmlDefinition>();
		OfbizSwitch<HasXmlDefinition> ofbizSwitch = new OfbizSwitch<HasXmlDefinition>() {

			@Override
			public HasXmlDefinition caseHasXmlDefinition(
					HasXmlDefinition object) {
				return object;
			}
			
		};
		for (Project project : getAllProjects()) {
			TreeIterator<EObject> eAllContents = project.eAllContents();
			while (eAllContents.hasNext()) {
				EObject eObject = eAllContents.next();
				HasXmlDefinition doSwitch = ofbizSwitch.doSwitch(eObject);
				if (doSwitch != null) {
					xmlDefinitions.add(doSwitch);
				}
			}
		}
		return xmlDefinitions;
	}
}
