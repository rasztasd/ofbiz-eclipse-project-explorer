package org.ofbiz.plugin.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.ofbiz.plugin.ofbiz.AbstractResponse;
import org.ofbiz.plugin.ofbiz.AbstractViewMap;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Controller;
import org.ofbiz.plugin.ofbiz.DummyEvent;
import org.ofbiz.plugin.ofbiz.FtlViewMap;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.RequestMap;
import org.ofbiz.plugin.ofbiz.ScreenViewMap;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.ofbiz.ServiceEvent;
import org.ofbiz.plugin.ofbiz.ViewResponse;
import org.ofbiz.plugin.ofbiz.WebApp;
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
	private static Set<String> screenLocations = new HashSet<String>();
	/**
	 * returns the view-response by the value attribute
	 * its used for matching the responses to the view-maps
	 */
	private Map<String, List<ViewResponse>> viewResponsesByValue = new HashMap<String, List<ViewResponse>>();

	public WebappParser(Component component, String uri, IFile file) {
		this.file = file;
		webApp = OfbizFactory.eINSTANCE.createWebApp();
		webApp.setUri(uri);
		webApp.setName(uri);
		controller = OfbizFactory.eINSTANCE.createController();
		String markerKey = webApp.getName();
		controller.setMarkerKey(markerKey);
		controller.setFile(file);
		controller.setComponent(component);
		controller.setUri(uri);
		createMarker(1, markerKey);
		webApp.setController(controller);
		component.getWebapps().add(webApp);
		this.component = component;
		this.uri = uri;
	}

	private void addViewResponse(String value, ViewResponse viewResponse) {
		if (viewResponsesByValue.get(value) == null) {
			viewResponsesByValue.put(value, new ArrayList<ViewResponse>());
		}
		viewResponsesByValue.get(value).add(viewResponse);
	}

	@Override
	protected void processStartElement(XmlPullParser xpp) {
		String name = xpp.getName();
		if (name.equals("request-map")) {
			String uri = xpp.getAttributeValue(null, "uri");
			String requestUriString = uri + "/control/" + uri;
			//			requestUri.setName(requestUri);
			curRequestMap = OfbizFactory.eINSTANCE.createRequestMap();
			curRequestMap.setSecurityAuth(false);
			curRequestMap.setSecurityHttps(false);
			curRequestMap.setName(requestUriString);
			curRequestMap.setUrl(requestUriString);
			createMarker(xpp.getLineNumber(), requestUriString);
			curRequestMap.setMarkerKey(requestUriString);
			curRequestMap.setHyperlinkKey(uri);
			curRequestMap.setHyperlinkText("Request map: " + requestUriString);
			curRequestMap.setFile(file);
		} else if (name.equals("event") && curRequestMap != null) {
			if (xpp.getAttributeValue(null, "type").equals("service")) {
				ServiceEvent serviceEvent = OfbizFactory.eINSTANCE.createServiceEvent();
				String markerKey = "event" + curRequestMap.getMarkerKey();
				serviceEvent.setMarkerKey(markerKey);
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
						break;
					}
				}
			} else { //TODO implement other type of events like: java
				DummyEvent dummyEvent = OfbizFactory.eINSTANCE.createDummyEvent();
				String eventType = xpp.getAttributeValue(null, "type");
				String eventPath = xpp.getAttributeValue(null, "path");
				String eventInvoke = xpp.getAttributeValue(null, "invoke");
				dummyEvent.setName("Type: " + eventType + ", Path: " + eventPath + "," + ", Invoke: " + eventInvoke);
				String markerKey = "dummy_event" + curRequestMap.getMarkerKey();
				dummyEvent.setMarkerKey(markerKey);
				dummyEvent.setFile(file);
				createMarker(xpp.getLineNumber(), markerKey);
				curRequestMap.setEvent(dummyEvent);
			}
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
			abstractViewMap.setSearchScropeKeyword(component.getName()+webApp.getName());
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
