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
 * $Id: EntityParser.java,v 1.1 2008/01/17 18:48:15 hessellund Exp $
 */
package org.ofbiz.plugin.parser;

import org.eclipse.core.resources.IMarker;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Entity;
import org.ofbiz.plugin.ofbiz.Field;
import org.ofbiz.plugin.ofbiz.IEntity;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.xmlpull.v1.XmlPullParser;


public class EntityParser extends Parser {

	public static final String 
		ENTITYNAME = "entity-name",
		ENTITY = "entity",
		VIEW = "view-entity",
		EXTEND = "extend-entity";
	private Component component;
	private IEntity current;
	
	public EntityParser(Component component) {
		this.component = component;
	}
	
	@Override
	protected void processStartElement(XmlPullParser xpp) {
		IMarker createMarker = null;
		String markerKey = null;
		String viewType = null;
		if (xpp.getName().equals(ENTITY)) {
			
			assert current == null;
			this.current = OfbizFactory.eINSTANCE.createEntity();
			this.current.setName(xpp.getAttributeValue(null, ENTITYNAME));
			createMarker = createMarker(xpp.getLineNumber(), current.getName());
			markerKey = current.getName();
			viewType = "Entity: ";
			
		} else if (xpp.getName().equals(VIEW)) {
			
			assert current == null;
			this.current = OfbizFactory.eINSTANCE.createViewEntity();
			this.current.setName(xpp.getAttributeValue(null, ENTITYNAME));
			createMarker = createMarker(xpp.getLineNumber(), current.getName());
			markerKey = current.getName();
			viewType = "View: ";
			
		} else if (xpp.getName().equals(EXTEND)) {
			
			assert current == null;
			this.current = OfbizFactory.eINSTANCE.createExtendEntity();
			this.current.setName(xpp.getAttributeValue(null, ENTITYNAME));
			createMarker = createMarker(xpp.getLineNumber(), current.getName());
			markerKey = current.getName();
			viewType = "Extend: ";
			
		} else if (xpp.getName().equals("field")) {

			assert current != null;
			Field field = OfbizFactory.eINSTANCE.createField();
			field.setName(xpp.getAttributeValue(null, "name"));
			field.setType(xpp.getAttributeValue(null, "type"));
			field.setPk(false);
			field.setEntity(current);
			
		} else if (xpp.getName().equals("prim-key")) { 
			
			assert current != null;
			assert current instanceof Entity;
			Entity currentAsEntity = (Entity) current;
			String fieldName = xpp.getAttributeValue(null, "field");
			for(Field field : currentAsEntity.getFields()) {
				if(fieldName.equals(field.getName())) {
					field.setPk(true);
				}
			}
			
		}
		if (createMarker != null) {
			current.setMarkerKey(markerKey);
			current.setHyperlinkKey(current.getName());
			current.setHyperlinkText("Entity ");
			current.setFile(file);
		}
	}
	
	@Override
	protected void processEndElement(XmlPullParser xpp) {
		
		if (xpp.getName().equals(ENTITY)) {
			
			assert this.current != null;
			synchronized (component) {
				current.setComponent(component);
			}
			this.current = null;
			
		} else if (xpp.getName().equals(VIEW)) {
			
			assert this.current != null;
			current.setComponent(component);
			this.current = null;
			
		} else if (xpp.getName().equals(EXTEND)) {
			
			assert this.current != null;
			current.setComponent(component);
			this.current = null;
			
		}
	}
}