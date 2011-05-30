package org.ofbiz.plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.ofbiz.plugin.model.hyperlink.HyperlinkMarker;
import org.ofbiz.plugin.ofbiz.Controller;
import org.ofbiz.plugin.ofbiz.HasXmlDefinition;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.util.OfbizSwitch;
import org.ofbiz.plugin.parser.GoToFile;

public class HasXmlHasXmlDefinitionHelper {
	public static Controller findControllerByFile(Project project, final IFile file) {
		TreeIterator<EObject> eAllContents = project.eAllContents();
		while (eAllContents.hasNext()) {
			EObject eObject = eAllContents.next();
			OfbizSwitch<Controller> ofbizSwitch = new OfbizSwitch<Controller>() {

				@Override
				public Controller caseController(Controller object) {
					if (object.getFile().equals(file)) {
						return object;
					}
					return null;
				}
				
			};
			
			Controller doSwitch = ofbizSwitch.doSwitch(eObject);
			if (doSwitch != null) {
				return doSwitch;
			}
		}
		return null;
	}
	
	/*
	public static List<HyperlinkMarker> findHyperlinkMarkers(final String uri, String componentName) {
		List<HyperlinkMarker> retValue = new ArrayList<HyperlinkMarker>();
		Project project = OfbizModelSingleton.get().findActiveEclipseProject();
		TreeIterator<EObject> eAllContents = project.eAllContents();
		while (eAllContents.hasNext()) {
			EObject eObject = eAllContents.next();
			OfbizSwitch<HasXmlDefinition> ofbizSwitch = new OfbizSwitch<HasXmlDefinition>() {

				@Override
				public HasXmlDefinition caseHasXmlDefinition(HasXmlDefinition object) {
					String objectUrl = object.getHyperlinkKey();
					if (uri.equals(objectUrl)) {
						return object;
					}
					return null;
				}

			};
			final HasXmlDefinition doSwitch = ofbizSwitch.doSwitch(eObject);
			if (doSwitch != null) {
				retValue.add(new HyperlinkMarker(GoToFile.getMarker(doSwitch)) {
					
					@Override
					public String getTypeLabel() {
						return "";
					}
					
					@Override
					public String getHyperlinkText() {
						return doSwitch.getHyperlinkText();
					}
				});
			}
		}
		return retValue;
	}
	public static List<HyperlinkMarker> findHyperlinkMarkers(final String uri) {
		return findHyperlinkMarkers(uri, null);
	}
	*/
}
