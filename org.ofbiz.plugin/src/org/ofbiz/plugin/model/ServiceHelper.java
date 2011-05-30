package org.ofbiz.plugin.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Directory;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.ofbiz.util.OfbizSwitch;

public class ServiceHelper {
	public static List<Service> findServiceByName(final String name, Project project) {
		List<Service> retValue = new ArrayList<Service>();
		if (project != null) {
			TreeIterator<EObject> eAllContents = project.eAllContents();
			while (eAllContents.hasNext()) {
				EObject eObject = eAllContents.next();
				OfbizSwitch<Service> ofbizSwitch = new OfbizSwitch<Service>() {
					
					@Override
					public Service caseService(Service object) {
						if (name.equals(object.getName())) {
							return object;
						}
						return null;
					}
					
				};
				
				Service doSwitch = ofbizSwitch.doSwitch(eObject);
				if (doSwitch != null) {
					retValue.add(doSwitch);
				}
			}
		}
		return retValue;
	}
	public static List<Service> findServiceByName(final String name)  {
		Project project = OfbizModelSingleton.get().findActiveEclipseProject();
		return findServiceByName(name, project);
	}
	public static SortedSet<Service> getSortedServices() {
		Comparator<Service> comparator = new Comparator<Service>() {

			@Override
			public int compare(Service o1, Service o2) {
				return o1.getName().compareTo(o2.getName());
			}
			
		};
		SortedSet<Service> retValue = new TreeSet<Service>(comparator);
		Project project = OfbizModelSingleton.get().findActiveEclipseProject();
		for (Directory directory : project.getDirectories()) {
			for (Component component : directory.getComponents()) {
				for (Service service : component.getServices()) {
					retValue.add(service);
				}
			}
		}
		return retValue;
	}
	
	public static Service isServiceFile(final IFile file) {
		Project project = OfbizModelSingleton.get().findActiveEclipseProject();
		if (project != null) {
			TreeIterator<EObject> eAllContents = project.eAllContents();
			while (eAllContents.hasNext()) {
				EObject eObject = eAllContents.next();
				OfbizSwitch<Service> ofbizSwitch = new OfbizSwitch<Service>() {
					
					@Override
					public Service caseService(Service object) {
						if (file.equals(object.getFile())) {
							return object;
						}
						return null;
					}
					
				};
				
				Service doSwitch = ofbizSwitch.doSwitch(eObject);
				if (doSwitch != null) {
					return doSwitch;
				}
			}
		}
		return null;
	}
}
