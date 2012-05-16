/**
 * Copyright 2008 Anders Hessellund 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: ServiceParser.java,v 1.1 2008/01/17 18:48:15 hessellund Exp $
 */
package org.ofbiz.plugin.parser;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.ofbiz.plugin.ofbiz.Attribute;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.EntityRef;
import org.ofbiz.plugin.ofbiz.Include;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.ofbiz.ServiceFile;
import org.ofbiz.plugin.ofbiz.ServiceMode;
import org.xmlpull.v1.XmlPullParser;


public class ServiceParser extends Parser {
	
	private Service curService;
	private ServiceFile serviceFile;
	private EntityRef curAutoAttr;
	private Set<String> servicesToPars = new HashSet<String>();
	
	public ServiceParser(Component component, IFile file) {
		this.file = file;
		serviceFile = OfbizFactory.eINSTANCE.createServiceFile();
		String markerKey = component.getName() + file.getName();
		serviceFile.setMarkerKey(markerKey);
		createMarker(1, markerKey);
		serviceFile.setComponent(component);
		serviceFile.setName(file.getName());
		serviceFile.setFile(file);
	}

	@Override protected void processStartElement(XmlPullParser xpp) {
		
		if (xpp.getName().equals("service")) {
			
			assert curService == null;
			handleService(xpp);
			
		} else if (xpp.getName().equals("attribute")) {

			assert curService != null;
			handleAttribute(xpp);
			
		} else if (xpp.getName().equals("auto-attributes")) {

			assert curService != null;
			assert curAutoAttr == null;
			handleAutoAttributes(xpp);
			
		} else if (xpp.getName().equals("exclude")) {	
			
			assert curService != null;
			assert curAutoAttr != null;
			curAutoAttr.getExlude().add(xpp.getAttributeValue(null, "field-name"));
			
		} else if (xpp.getName().equals("implements")) {

			assert curService != null;
			handleImplements(xpp);
			
		} else if (xpp.getName().equals("override")) {

			assert curService != null;
			handleOverride(xpp);
			
		}
	}

	@Override protected void processEndElement(XmlPullParser xpp) {
		
		if (xpp.getName().equals("service")) {
			
			assert curService != null;
			curService = null;
			
		} else if (xpp.getName().equals("auto-attributes")) {

			assert curService != null;
			assert curAutoAttr != null;
			curAutoAttr = null;
			
		}
	}
	
	// -- utility methods
	
	private void handleService(XmlPullParser xpp) {
		curService = OfbizFactory.eINSTANCE.createService();
		curService.setReference(OfbizFactory.eINSTANCE.createReferenceIn());
		curService.setServiceFile(serviceFile);
		curService.setName(xpp.getAttributeValue(null, "name"));
		curService.setEngine(xpp.getAttributeValue(null, "engine"));
		String markerKey = serviceFile.getName() + curService.getName();
		createMarker(xpp.getLineNumber(), markerKey);
		curService.setMarkerKey(markerKey);
		curService.setNameToShow(curService.getName());
		curService.setFile(file);
		String location = xpp.getAttributeValue(null, "location");
		if(location!=null) 
			curService.setLocation(location);
		String invoke = xpp.getAttributeValue(null, "invoke"); 
		if(invoke!=null) 
			curService.setInvoke(invoke);
		curService.setEntity(xpp.getAttributeValue(null, "default-entity-name"));
		createMarker(xpp.getLineNumber(), curService.getName());
	}

	private void handleAttribute(XmlPullParser xpp) {
		Attribute attr = OfbizFactory.eINSTANCE.createAttribute();
		attr.setName(xpp.getAttributeValue(null, "name"));
		attr.setType(xpp.getAttributeValue(null, "type"));
		ServiceMode mode = ServiceMode.get(xpp.getAttributeValue(null, "mode"));
		attr.setMode(mode);
		String optional = xpp.getAttributeValue(null, "optional");
		if (optional==null || optional.equals("false")) {
			attr.setOptional(false);
		} else {
			attr.setOptional(true);
		}
		curService.getAttributes().add(attr);
		attr.setService(curService);
	}

	private void handleAutoAttributes(XmlPullParser xpp) {
		curAutoAttr = OfbizFactory.eINSTANCE.createEntityRef();
		curAutoAttr.setService(curService);
		curAutoAttr.setInclude(Include.get(xpp.getAttributeValue(null, "include")));
		curAutoAttr.setMode(ServiceMode.get(xpp.getAttributeValue(null, "mode")));
		String optional = xpp.getAttributeValue(null, "optional");
		if (optional!=null && optional.equals("true")) {
			curAutoAttr.setOptional(true);
		}
		curAutoAttr.setEntity(xpp.getAttributeValue(null, "entity-name"));
	}

	private void handleImplements(XmlPullParser xpp) {
		curService.getExtends().add(xpp.getAttributeValue(null, "service"));
	}
	
	private void handleOverride(XmlPullParser xpp) {
		//TODO: parse override tag
	}

	public Set<String> getServicesToPars() {
		return servicesToPars;
	}

	@Override
	protected String getMarkerType() {
		// TODO Auto-generated method stub
		return "org.ofbiz.plugin.controllerMarker";
	}
	
}
