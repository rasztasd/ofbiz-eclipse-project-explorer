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

import org.eclipse.core.resources.IFile;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Entity;
import org.ofbiz.plugin.ofbiz.EntityFile;
import org.ofbiz.plugin.ofbiz.Field;
import org.ofbiz.plugin.ofbiz.IEntity;
import org.ofbiz.plugin.ofbiz.MemberEntity;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.ViewEntity;
import org.ofbiz.plugin.ofbiz.ViewLink;
import org.xmlpull.v1.XmlPullParser;


public class EntityParser extends Parser {

	public static final String 
		ENTITYNAME = "entity-name",
		ENTITY = "entity",
		VIEW = "view-entity",
		EXTEND = "extend-entity";
	private EntityFile entityFile;
	private IEntity current;
	
	public EntityParser(Component component, IFile file) {
		this.file = file;
		entityFile = OfbizFactory.eINSTANCE.createEntityFile();
		entityFile.setFile(file);
		String entityFileMarkerKey = "Entity" + file.getName() + component.getName();
		createMarker(1, entityFileMarkerKey);
		entityFile.setMarkerKey(entityFileMarkerKey);
		entityFile.setName(file.getName());
		entityFile.setComponent(component);
	}
	
	@Override
	protected void processStartElement(XmlPullParser xpp) {
		String markerKey = null;
		boolean entity = false;
		if (xpp.getName().equals(ENTITY)) {
			entity = true;
			assert current == null;
			this.current = OfbizFactory.eINSTANCE.createEntity();
			this.current.setName(xpp.getAttributeValue(null, ENTITYNAME));			
			markerKey = current.getName();
			
		} else if (xpp.getName().equals(VIEW)) {
			entity = true;
			assert current == null;
			this.current = OfbizFactory.eINSTANCE.createViewEntity();
			this.current.setName(xpp.getAttributeValue(null, ENTITYNAME));
			markerKey = current.getName();
			
		} else if (xpp.getName().equals(EXTEND)) {
			entity = true;
			assert current == null;
			this.current = OfbizFactory.eINSTANCE.createExtendEntity();
			this.current.setName(xpp.getAttributeValue(null, ENTITYNAME));
			markerKey = current.getName();
			
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
			entity = false;
			Entity currentAsEntity = (Entity) current;
			String fieldName = xpp.getAttributeValue(null, "field");
			for(Field field : currentAsEntity.getFields()) {
				if(fieldName.equals(field.getName())) {
					field.setPk(true);
				}
			}
			
		} else if (xpp.getName().equals("member-entity")) {
			ViewEntity currentView = (ViewEntity) current;
			MemberEntity memberEntity = OfbizFactory.eINSTANCE.createMemberEntity();
			memberEntity.setEntityAlias(xpp.getAttributeValue(null, "entity-alias"));
			memberEntity.setEntityName(xpp.getAttributeValue(null, "entity-name"));
			memberEntity.setViewEntity(currentView);
		} else if (xpp.getName().equals("view-link")) {
			ViewEntity currentView = (ViewEntity) current;
			ViewLink viewLink = OfbizFactory.eINSTANCE.createViewLink();
			viewLink.setViewEntity(currentView);
			viewLink.setEntityAlias(xpp.getAttributeValue(null, "entity-alias"));
			viewLink.setRelEntityAlias(xpp.getAttributeValue(null, "rel-entity-alias"));
			viewLink.setOptional(Boolean.valueOf(xpp.getAttributeValue(null, "entity-alias")));
		}
		if (entity && current != null) {
			current.setFile(file);
			markerKey += entityFile.getName();
			current.setMarkerKey(markerKey);
			createMarker(xpp.getLineNumber(), markerKey);
			current.setNameToShow(current.getName());
			current.setHyperlinkKey(current.getName());
			current.setHyperlinkText("Entity ");
		}
	}
	
	@Override
	protected void processEndElement(XmlPullParser xpp) {
		
		if (xpp.getName().equals(ENTITY)) {
			
			assert this.current != null;
			current.setEntityFile(entityFile);
			this.current = null;
			
		} else if (xpp.getName().equals(VIEW)) {
			
			assert this.current != null;
			current.setEntityFile(entityFile);
			this.current = null;
			
		} else if (xpp.getName().equals(EXTEND)) {
			
			assert this.current != null;
			current.setEntityFile(entityFile);
			this.current = null;
			
		}
	}

	@Override
	protected String getMarkerType() {
		return "org.ofbiz.plugin.iEntityMarker";
	}
}