package org.ofbiz.plugin.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.ofbiz.plugin.model.ComponentHelper;
import org.ofbiz.plugin.model.ControllerHelper;
import org.ofbiz.plugin.model.OfbizModelSingleton;
import org.ofbiz.plugin.nature.OfbizNature;
import org.ofbiz.plugin.ofbiz.AbstractEvent;
import org.ofbiz.plugin.ofbiz.AbstractResponse;
import org.ofbiz.plugin.ofbiz.AbstractViewMap;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Controller;
import org.ofbiz.plugin.ofbiz.DummyEvent;
import org.ofbiz.plugin.ofbiz.FtlViewMap;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.RequestMap;
import org.ofbiz.plugin.ofbiz.ScreenViewMap;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.ofbiz.ServiceEvent;
import org.ofbiz.plugin.ofbiz.ViewResponse;
import org.ofbiz.plugin.ofbiz.WebApp;
import org.ofbiz.plugin.parser.model.WebappModel;
import org.xmlpull.v1.XmlPullParser;

/**
 * Parses the controller.xml
 * @author rasztasd
 *
 */
public class WebappParser extends Parser {
	private final Component component;
	private RequestMap curRequestMap;
	private WebApp webApp;
	private String uri;
	private Controller controller;
	private Set<String> screenLocations = new HashSet<String>();
	private Set<WebappModel> includeLocations = new HashSet<WebappModel>();
	private Controller referencingController;
	/**
	 * returns the view-response by the value attribute
	 * its used for matching the responses to the view-maps
	 */
	private Map<String, List<ViewResponse>> viewResponsesByValue = new HashMap<String, List<ViewResponse>>();

	public WebappParser(Component component, String uri, IFile file, WebApp webApp, Controller referencingController) throws CoreException {
		this.file = file;
		IProject project = file.getProject();
		project.getNature(OfbizNature.ID);
		Project findProjectByEclipseProjectName = OfbizModelSingleton.get().findProjectByEclipseProjectName(project.getName());
		Controller controller2 = ControllerHelper.getController(file);
		if (controller2 != null) {
			controller2.setWebapp(null);
			controller2.setReferencedWebapp(null);
		}
		
		this.referencingController = referencingController;

		controller = OfbizFactory.eINSTANCE.createController();
		controller.setFile(file);
		controller.setComponent(component);
		controller.setUri(uri);
		controller.setName(file.getName());
		if (referencingController == null) {
			webApp.setController(controller);
		} else {
			webApp.getReferencedControllers().add(controller);
			
		}
		controller.setWebapp(webApp);
		String markerKey = webApp.getName();
		createMarker(1, markerKey);
		controller.setMarkerKey(markerKey);
		this.component = component;
		this.uri = uri;
	}

	private void addViewResponse(String value, ViewResponse viewResponse) {
		if (viewResponsesByValue.get(value) == null) {
			viewResponsesByValue.put(value, new ArrayList<ViewResponse>());
		}
		viewResponsesByValue.get(value).add(viewResponse);
	}
	
	public Set<WebappModel> getincludeLocations() {
		return this.includeLocations;
	}

	@Override
	protected void processStartElement(XmlPullParser xpp) {
		String name = xpp.getName();
		if (name.equals("include")) {
			String location = xpp.getAttributeValue(null, "location");
			Component componentByUrl = component;
			if (location.startsWith("component://")) {
				componentByUrl = ComponentHelper.getComponentByUrl(component.getDirectory().getProject(), location);
				location = location.substring("component://".length());
				location = location.substring(location.indexOf("/"));
			}
			includeLocations.add(new WebappModel(getResourceFile(location, componentByUrl), location, referencingController==null?controller:referencingController));
		} else if (name.equals("request-map")) {
			String uri = xpp.getAttributeValue(null, "uri");
			String requestUriString = this.uri + "/control/" + uri;
			//			requestUri.setName(requestUri);
			curRequestMap = OfbizFactory.eINSTANCE.createRequestMap();
			curRequestMap.setSecurityAuth(false);
			curRequestMap.setSecurityHttps(false);
			curRequestMap.setName(requestUriString);
			curRequestMap.setUrl(requestUriString);
			createMarker(xpp.getLineNumber(), requestUriString);
			curRequestMap.setMarkerKey(requestUriString);
			curRequestMap.setNameToShow(requestUriString);
			curRequestMap.setHyperlinkKey(uri);
			curRequestMap.setHyperlinkText("Request map: " + requestUriString);
			curRequestMap.setFile(file);
		} else if (name.equals("event") && curRequestMap != null) {
			AbstractEvent event = null;
			String markerKey;
			if (xpp.getAttributeValue(null, "type").equals("service")) {
				ServiceEvent serviceEvent = OfbizFactory.eINSTANCE.createServiceEvent();
				event = serviceEvent;
				markerKey = "event" + curRequestMap.getMarkerKey();
				serviceEvent.setFile(file);
				curRequestMap.setEvent(serviceEvent);
				String serviceName = xpp.getAttributeValue(null, "invoke"); 
				for (Service service : component.getServices()) {
					if (serviceName.equals(service.getName())) {
						serviceEvent.setComponent(service.getComponent());
						serviceEvent.setEngine(service.getEngine());
						serviceEvent.setEntity(service.getEntity());
						serviceEvent.setInvoke(service.getInvoke());
						serviceEvent.setLocation(service.getLocation());
						serviceEvent.setName(service.getName());
						serviceEvent.setRequestMap(curRequestMap);
						service.getReference().getReferences().add(serviceEvent);
						break;
					}
				}
			} else { //TODO implement other type of events like: java
				DummyEvent dummyEvent = OfbizFactory.eINSTANCE.createDummyEvent();
				event = dummyEvent;
				String eventType = xpp.getAttributeValue(null, "type");
				String eventPath = xpp.getAttributeValue(null, "path");
				String eventInvoke = xpp.getAttributeValue(null, "invoke");
				dummyEvent.setName("Type: " + eventType + ", Path: " + eventPath + "," + ", Invoke: " + eventInvoke);
				markerKey = "dummy_event" + curRequestMap.getMarkerKey();
				curRequestMap.setEvent(dummyEvent);
			}
			event.setMarkerKey(markerKey);
			event.setFile(file);
			createMarker(xpp.getLineNumber(), markerKey);
		} else if (name.equals("security")) {
			if (curRequestMap == null) {
				return;
			}
			curRequestMap.setSecurityAuth("true".equals(xpp.getAttributeValue(null, "auth")));
			curRequestMap.setSecurityHttps("true".equals(xpp.getAttributeValue(null, "https")));
		} else if (name.equals("response")) {
			AbstractResponse response = null;
			String responseName = xpp.getAttributeValue(null, "name");
			String responseType = xpp.getAttributeValue(null, "type");
			if (responseType.equals("view")) {
				response = OfbizFactory.eINSTANCE.createViewResponse();
				addViewResponse(xpp.getAttributeValue(null, "value"), (ViewResponse) response);
				//			} else if (responseType.equals("request")) {
				//				response = OfbizFactory.eINSTANCE.creater
			} else if (responseType.equals("request")) {
				response = OfbizFactory.eINSTANCE.createRequestResponse();
			} else {
				return;
			}
			String responseValue = xpp.getAttributeValue(null, "value");
			response.setName("Name: " + responseName + ", Type: " + responseType + ", Value: " + responseValue);
			response.setType(responseType);
			response.setValue(responseValue);
			curRequestMap.getResponses().add(response);
			String markerKey = curRequestMap.getMarkerKey() + responseValue;
			response.setFile(file);
			response.setMarkerKey(markerKey);
			response.setHyperlinkKey(responseValue);
			response.setHyperlinkText("Request view value: " + responseValue);
			createMarker(xpp.getLineNumber(), markerKey);
			response.setFile(file);
		} else if (name.equals("view-map")) {
			String viewMapName = xpp.getAttributeValue(null, "name");
			String viewMapType = xpp.getAttributeValue(null, "type");
			AbstractViewMap abstractViewMap = null;
			if ("screen".equals(viewMapType)) {
				ScreenViewMap screenViewMap = OfbizFactory.eINSTANCE.createScreenViewMap();
				abstractViewMap = screenViewMap;
				screenViewMap.setController(controller);
				abstractViewMap.setHyperlinkText("Screen: " + viewMapName);
				String page = xpp.getAttributeValue(null, "page");
				screenViewMap.setViewName(page);
				if (component.getName().equals("webtools")) {
					System.out.println();
				}
				screenViewMap.setName(page);
				try {
					if (page.equals("component://emerald_admin/widget/catalog/StoreScreens.xml#FindProductStore")) {
						System.out.println("");
					}
					int indexOf = page.indexOf("#");
					String screenXml = page.substring(0, indexOf);
					screenLocations.add(screenXml);
				} catch(Error e) {
					e.getCause();
				} catch(RuntimeException e) {
					e.getCause();
				}
			} else if ("ftl".equals(viewMapType)) {
				FtlViewMap ftlViewMap = OfbizFactory.eINSTANCE.createFtlViewMap();
				ftlViewMap.setController(controller);
				String page = xpp.getAttributeValue(null, "page");
				ftlViewMap.setName(page);
				abstractViewMap = ftlViewMap;
				abstractViewMap.setHyperlinkText("Ftl: " + viewMapName);
			} else {
				return;
			}
			String markerKey = controller.getName() + abstractViewMap.getName();
			createMarker(xpp.getLineNumber(), markerKey);
			abstractViewMap.setMarkerKey(markerKey);
			abstractViewMap.setHyperlinkKey(viewMapName);
			abstractViewMap.setFile(file);
			controller.getViewMaps().add(abstractViewMap);
			List<ViewResponse> viewResponses = viewResponsesByValue.get(viewMapName);		
			if (viewResponses != null) {
				for (ViewResponse viewResponse : viewResponses) {
					AbstractViewMap copiedView = EcoreUtil.copy(abstractViewMap);
					viewResponse.setAbstractViewMap(copiedView);
				}
			} else {
				//TODO handle ftl view map declared but never invoked 
			}
		}
	}

	@Override
	protected void processEndElement(XmlPullParser xpp) {
		if (xpp.getName().equals("request-map")) {
			if (curRequestMap != null) {
				curRequestMap.setController(controller);
				controller.getRequestMaps().add(curRequestMap);
				curRequestMap = null;
			}
		}
	}
	public Set<String> getScreenLocations() {
		return screenLocations;
	}

}
