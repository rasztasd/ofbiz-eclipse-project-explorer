package org.ofbiz.plugin.model;

import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Directory;
import org.ofbiz.plugin.ofbiz.Project;

public class ComponentHelper {
	public static Component getComponentByUrl(String url) {
		Project project = OfbizModelSingleton.get().findActiveEclipseProject();
		String componentName = url.substring("component://".length());
		componentName = componentName.substring(0, componentName.indexOf("/"));
		for (Directory directory : project.getDirectories()) {
			for (Component component : directory.getComponents()) {
				if (component.getName().equals(componentName)) {
					return component;
				}
			}
		}
		return null;
	}
	public static Component getComponentByUrl(Project project, String url) {
		String componentName = url.substring("component://".length());
		if (componentName.indexOf("/") == -1) {
			return null;
		}
		componentName = componentName.substring(0, componentName.indexOf("/"));
		for (Directory directory : project.getDirectories()) {
			for (Component component : directory.getComponents()) {
				if (component.getName().equals(componentName)) {
					return component;
				}
			}
		}
		return null;
	}
}
