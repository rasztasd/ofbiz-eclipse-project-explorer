package org.ofbiz.plugin.parser;

import org.ofbiz.plugin.model.OfbizModelSingleton;
import org.ofbiz.plugin.model.ServiceHelper;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.Seca;
import org.ofbiz.plugin.ofbiz.Service;
import org.xmlpull.v1.XmlPullParser;

public class SecaParser extends Parser {
	private int i = 0;
	@Override protected void processStartElement(XmlPullParser xpp) {
		if (xpp.getName().equals("eca")) {
			String serviceName = xpp.getAttributeValue(null, "service");
			for (Service service : ServiceHelper.findServiceByName(serviceName, OfbizModelSingleton.get().findProjectByEclipseProjectName(file.getProject().getName()))) {
				Seca createSeca = OfbizFactory.eINSTANCE.createSeca();
				createSeca.setName(xpp.getAttributeValue(null, "event"));
				String markerKey = "Seca" + service.getName() + i++;
				createSeca.setMarkerKey(markerKey);
				createSeca.setFile(file);
				createMarker(xpp.getLineNumber(), markerKey);
				service.getSecas().add(createSeca);
			}
		}
	}
}
