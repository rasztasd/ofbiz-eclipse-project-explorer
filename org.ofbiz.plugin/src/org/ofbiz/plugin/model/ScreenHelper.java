package org.ofbiz.plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.ofbiz.plugin.model.hyperlink.HyperlinkMarker;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Controller;
import org.ofbiz.plugin.ofbiz.Directory;
import org.ofbiz.plugin.ofbiz.Form;
import org.ofbiz.plugin.ofbiz.FormFile;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.Screen;
import org.ofbiz.plugin.ofbiz.ScreenFile;
import org.ofbiz.plugin.parser.GoToFile;

public class ScreenHelper {
	public static List<HyperlinkMarker> getHyperlinkMarkerForController(String requestValue, Controller controller, String screenFileUrl) {
		String lookUpValue = requestValue.substring(requestValue.indexOf("#") + 1);
		List<HyperlinkMarker> retValue = new ArrayList<HyperlinkMarker>();
		for (Directory directory : controller.getComponent().getDirectory().getProject().getDirectories()) {
			for (Component component : directory.getComponents()) {
				for (ScreenFile screenFile : component.getScreenFiles()) {
					if (screenFileUrl.equals(screenFile.getName())) {
						for (final Screen screen : screenFile.getScreens()) {
							if (screen.getName().equals(lookUpValue)) {
								retValue.add(new HyperlinkMarker(GoToFile.getMarker(screen)) {

									@Override
									public String getTypeLabel() {
										return "";
									}

									@Override
									public String getHyperlinkText() {
										return "Screen: " + screen.getName();
									}
								});
							}
						}
					}
				}
			}
		}
		return retValue;
	}


	public static Screen getScreensByComponentName(Project project, String componentFullName) {
		String key = project.getName() + componentFullName;
		String lookUpValue = componentFullName.substring(componentFullName.indexOf("#") + 1);
		String componentUri = componentFullName.substring(0, componentFullName.indexOf("#"));
		for (Directory directory : project.getDirectories()) {
			for (Component component : directory.getComponents()) {
				for (ScreenFile screenFile : component.getScreenFiles()) {
					if (componentUri.equals(screenFile.getName())) {
						for (final Screen screen : screenFile.getScreens()) {
							if (screen.getName().equals(lookUpValue)) {
								return screen;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public static List<HyperlinkMarker> getHyperlinkMarker(String screenName, String screenFileUrl) {
		List<HyperlinkMarker> retValue = new ArrayList<HyperlinkMarker>();
		TreeIterator<EObject> eAllContents = OfbizModelSingleton.get().findActiveEclipseProject().eAllContents();
		if (eAllContents != null) {
			while (eAllContents.hasNext()) {
				EObject next = eAllContents.next();
				if (next instanceof ScreenFile) {
					final ScreenFile screenFile = (ScreenFile) next;
					if (screenFileUrl.equals(screenFile.getName())) {
						for (final Screen screen : screenFile.getScreens()) {
							if (screen.getName().equals(screenName))
								retValue.add(new HyperlinkMarker(GoToFile.getMarker(screen)) {

									@Override
									public String getTypeLabel() {
										return null;
									}

									@Override
									public String getHyperlinkText() {
										return "Screen: " + screen.getName();
									}
								});
						}
					}
				}
			}
		}
		return retValue;
	}

	public static List<HyperlinkMarker> getFormHyperlinkMarker(String screenName, String screenFileUrl) {
		List<HyperlinkMarker> retValue = new ArrayList<HyperlinkMarker>();
		TreeIterator<EObject> eAllContents = OfbizModelSingleton.get().findActiveEclipseProject().eAllContents();
		if (eAllContents != null) {
			while (eAllContents.hasNext()) {
				EObject next = eAllContents.next();
				if (next instanceof FormFile) {
					final FormFile screenFile = (FormFile) next;
					if (screenFileUrl.equals(screenFile.getName())) {
						for (final Form form : screenFile.getForms()) {
							if (form.getName().equals(screenName))
								retValue.add(new HyperlinkMarker(GoToFile.getMarker(form)) {

									@Override
									public String getTypeLabel() {
										return null;
									}

									@Override
									public String getHyperlinkText() {
										return "Screen: " + form.getName();
									}
								});
						}
					}
				}
			}
		}
		return retValue;
	}

	public static Screen getScreenByFile(IFile screenFile) {
		Project findActiveEclipseProject = OfbizModelSingleton.get().findActiveEclipseProject();
		if (findActiveEclipseProject != null) {
			TreeIterator<EObject> eAllContents = findActiveEclipseProject.eAllContents();
			if (eAllContents != null) {
				while (eAllContents.hasNext()) {
					EObject next = eAllContents.next();
					if (next instanceof Screen) {
						Screen screen = (Screen) next;
						if (screenFile.equals(screen.getFile())) {
							return screen;
						}
					}
				}
			}
		}
		return null;
	}
}
