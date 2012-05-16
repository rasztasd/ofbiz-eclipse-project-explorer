package org.ofbiz.plugin.model.hyperlink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.analysis.Analysis;
import org.ofbiz.plugin.model.ServiceHelper;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Service;

/**
 * Get hyperlinks for Ofbiz components
 * @author McAllisterD
 *
 */
public class HyperlinkMarkersHelper {
	private static Map<String, List<HyperlinkMarker>> hyperlinkMarkers = new HashMap<String, List<HyperlinkMarker>>();
	public static void addHyperlinkMarker(String hyperlinkKey, HyperlinkMarker marker) {
		if (hyperlinkMarkers.get(hyperlinkKey) == null) {
			hyperlinkMarkers.put(hyperlinkKey, new ArrayList<HyperlinkMarker>());
		}
		hyperlinkMarkers.get(hyperlinkKey).add(marker);
	}
	//TODO remove duplicate code
	private static IMarker resolveMarker(IFolder folder, String name, String fileExtension) {
		try {
			IMarker[] markers =
					folder.findMarkers(
							Plugin.TEXT_MARKER, true, IResource.DEPTH_INFINITE);
			for(IMarker m : markers) {
				if(m.getAttribute("name").equals(name) &&
						m.getResource().getName().endsWith(fileExtension)) {
					return m;
				}
			}
		} catch (CoreException e) {
			Plugin.logError("Unable to resolve marker "+name,e);
		}
		return null;
	}
	public static List<HyperlinkMarker> searchForServices(String name) {
		List<HyperlinkMarker> retValue = new ArrayList<HyperlinkMarker>();
		List<Service> services = ServiceHelper.findServiceByName(name);
		for (Service service : services) {
			Component component = service.getServiceFile().getComponent();
			if (component != null) {
				IFolder folder = component.getFolder();
				String serviceName = service.getName();

				if (service.getEngine().equals("java")) {
					IMarker markerJavaImpl = resolveMarker(folder, serviceName, "java");
					if(markerJavaImpl==null) {
						new Analysis(
								component.getDirectory().getProject().getJavaproject(),
								service, component.getDirectory().getProject()).run(true);
						markerJavaImpl = resolveMarker(folder, serviceName, "java");
					}
					if (markerJavaImpl!=null) {
						retValue.add(new ServiceImplHyperlink(serviceName, markerJavaImpl));
					}
				}
				// show XML
				IMarker markerXml = resolveMarker(folder, serviceName, "xml");
				if (markerXml!=null) {
					retValue.add(new ServiceDefHyperlink(serviceName, markerXml));
				}
			}
		}
		return retValue;
	}
}
