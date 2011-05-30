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
 * $Id: ComponentParser.java,v 1.1 2008/01/17 18:48:15 hessellund Exp $
 */
package org.ofbiz.plugin.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.ofbiz.plugin.ofbiz.ClasspathEntry;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.parser.model.WebappModel;
import org.xmlpull.v1.XmlPullParser;


public class ComponentParser extends Parser {

	private static final String ENTITYRESOURCE = "entity-resource",
			SERVICERESOURCE = "service-resource", TYPE = "type",
			LOADER = "loader", LOCATION = "location", MODEL = "model",
			MAIN = "main",
			WEBAPP = "webapp"	;

	private final Component component;

	private List<IFile> entityModels = new ArrayList<IFile>();
	private List<IFile> serviceModels = new ArrayList<IFile>();
	private List<WebappModel> webappModels = new ArrayList<WebappModel>();
	private List<IFile> secaModels = new ArrayList<IFile>();

	public List<IFile> getEntityModels() {
		return this.entityModels;
	}

	public List<IFile> getServiceModels() {
		return this.serviceModels;
	}

	public List<IFile> getSecaModels() {
		return this.secaModels;
	}
	
	public List<WebappModel> getWebappModels() {
		return this.webappModels;
	}

	public ComponentParser(Component component) {
		assert component != null;
		this.component = component;		
	}

	public void addMarker() {
		String markerKey = "component_" + component.getName();
		component.setMarkerKey(markerKey);
		createMarker(1, markerKey);
	}

	@Override
	protected void processStartElement(XmlPullParser xpp) {

		if (xpp.getName().equals("ofbiz-component")) {
			String name = xpp.getAttributeValue(null, "name");
			component.setName(name);

		} else if (xpp.getName().equals(ENTITYRESOURCE)) {

			//TODO: lacks proper error handling if file does not exist
			IFile file = getResourceFile(xpp);
			if (file != null) {
				entityModels.add(file);
			}

		} else if (xpp.getName().equals(SERVICERESOURCE)) {

			//TODO: lacks proper error handling if file does not exist
			IFile file = getResourceFile(xpp);
			if (file != null) {
				if (xpp.getName().equals(SERVICERESOURCE) && xpp.getAttributeValue(null, "type").equals("eca")) {
					secaModels.add(file);
				} else {
					serviceModels.add(file);
				}
			}

		} else if (xpp.getName().equals(WEBAPP)) {
			//TODO: lacks proper error handling if file does not exist
			IFile file = getResourceFile(xpp);
			if (file != null) {
				webappModels.add(new WebappModel(file, xpp.getAttributeValue(null, "mount-point")));
			}
		} else if (xpp.getName().equals("classpath")) {
			if ("dir".equals(xpp.getAttributeValue(null, "type"))) {
				String location = xpp.getAttributeValue("", "location");
				ClasspathEntry classpathEntry = OfbizFactory.eINSTANCE.createClasspathEntry();
				classpathEntry.setName(location);
				classpathEntry.setClasspathLocation(location);
				classpathEntry.setComponent(component);
				String markerKey = location;
				classpathEntry.setMarkerKey(markerKey);
				classpathEntry.setFile(file);
				createMarker(xpp.getLineNumber(), markerKey);
			}
		}
	}

	/** retrieves model file for either an entity-resource or a service-resource */
	private IFile getResourceFile(XmlPullParser xpp) {
		String type = xpp.getAttributeValue(null, TYPE), loader = xpp
				.getAttributeValue(null, LOADER);
		if ((!xpp.getName().equals(WEBAPP)) && type.equals(MODEL) && loader.equals(MAIN)) {
			String location = xpp.getAttributeValue(null, LOCATION);
			IResource res = component.getFolder().findMember(location);
			assert res instanceof IFile;
			assert res.exists();
			return (IFile) res;
		} else if (xpp.getName().equals(WEBAPP)) {
			String location = xpp.getAttributeValue(null, LOCATION) + "/WEB-INF/controller.xml";
			IResource res = component.getFolder().findMember(location);
			if (res != null) {
				assert res instanceof IFile;
				assert res.exists();
			}
			return (IFile) res;
		} else if (type.equals("eca")) {
			String location = xpp.getAttributeValue(null, LOCATION);
			IResource res = component.getFolder().findMember(location);
			return (IFile) res;
		} else {
			return null;
		}

	}
}
