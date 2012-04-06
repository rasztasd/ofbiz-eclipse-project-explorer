package org.ofbiz.plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.ofbiz.plugin.model.hyperlink.HyperlinkMarker;
import org.ofbiz.plugin.ofbiz.AbstractViewMap;
import org.ofbiz.plugin.ofbiz.Controller;
import org.ofbiz.plugin.ofbiz.HasXmlDefinition;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.RequestMap;
import org.ofbiz.plugin.ofbiz.util.OfbizSwitch;
import org.ofbiz.plugin.parser.GoToFile;

public class ControllerHelper {
	public static Controller getController(final IFile file) {
		Project project = OfbizModelSingleton.get().findProjectByEclipseProjectName(file.getProject().getName());
		return getController(file, project);
	}
	public static Controller getController(final IFile file, Project project) {
		List<HyperlinkMarker> retValue = new ArrayList<HyperlinkMarker>();
		TreeIterator<EObject> eAllContents = project.eAllContents();
		while (eAllContents.hasNext()) {
			EObject eObject = eAllContents.next();
			OfbizSwitch<Controller> ofbizSwitch = new OfbizSwitch<Controller>() {

				@Override
				public Controller caseController(Controller object) {
					String objectUrl = object.getHyperlinkKey();
					if (file.equals(object.getFile())) {
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
	public static List<HyperlinkMarker> getHyperlinkForRequestValueView(String requestValue, IFile file) {
		List<HyperlinkMarker> retValue = new ArrayList<HyperlinkMarker>();
		List<Controller> controllers = new ArrayList<Controller>();
		final Controller controller = getController(file);
		controllers.addAll(controller.getWebapp().getReferencedControllers());
		if (controller.equals(controller.getWebapp().getController())) {
			controllers.add(controller);
		}
		for (Controller controller2 : controllers) {
			EList<AbstractViewMap> viewMaps = controller2.getViewMaps();
			for (final AbstractViewMap viewMap : viewMaps) {
				if (requestValue.equals(viewMap.getHyperlinkKey())) {
					retValue.add(new HyperlinkMarker(GoToFile.getMarker(controller2.getFile(), viewMap.getMarkerKey())){

						@Override
						public String getTypeLabel() {
							return "";
						}

						@Override
						public String getHyperlinkText() {
							return "View: " + viewMap.getName();
						}

					});
				}
			}
		}
		return retValue;
	}
	public static List<HyperlinkMarker> getHyperlinkForRequestValueRequest(String requestValue, IFile file) {
		List<HyperlinkMarker> retValue = new ArrayList<HyperlinkMarker>();
		List<Controller> controllers = new ArrayList<Controller>();
		final Controller controller = getController(file);
		controllers.addAll(controller.getWebapp().getReferencedControllers());
		if (controller.equals(controller.getWebapp().getController())) {
			controllers.add(controller);
		}
		for (Controller controller2 : controllers) {
			EList<RequestMap> requestMaps = controller2.getRequestMaps();
			for (final RequestMap requestMap : requestMaps) {
				if (requestValue.equals(requestMap.getHyperlinkKey())) {
					retValue.add(new HyperlinkMarker(GoToFile.getMarker(controller2.getFile(), requestMap.getMarkerKey())){

						@Override
						public String getTypeLabel() {
							return "";
						}

						@Override
						public String getHyperlinkText() {
							return "Request map: " + requestMap.getName();
						}

					});
				}
			}
		}
		return retValue;
	}
}