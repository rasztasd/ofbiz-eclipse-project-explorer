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
 * $Id: AttributeFinder.java,v 1.1 2008/01/17 18:48:15 hessellund Exp $
 */
package org.ofbiz.plugin.parser;

import java.util.ArrayList;
import java.util.List;

import org.ofbiz.plugin.ofbiz.Attribute;
import org.ofbiz.plugin.ofbiz.EntityRef;
import org.ofbiz.plugin.ofbiz.Field;
import org.ofbiz.plugin.ofbiz.Include;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.Service;


public class AttributeFinder {
	
	private final Service service;
	
	public AttributeFinder(Service service) {
		assert service != null;
		this.service = service;
	}
	
	public List<Attribute> getAttributes() throws FinderException {
		List<Attribute> attributes = new ArrayList<Attribute>();
		
		// inherit attributes from super classes
		if (!service.getExtends().isEmpty()) {
			for(String superServiceName : service.getExtends()) {
				ServiceFinder serviceFinder = 
					new ServiceFinder(service.getServiceFile().getComponent(),superServiceName);
				if(serviceFinder.getService() == null) {
					throw new FinderException(
					"Unable to locate service "+superServiceName);
				}
				AttributeFinder attrFinder = 
					new AttributeFinder(serviceFinder.getService());
				attributes.addAll(attrFinder.getAttributes());
			}
		}
		
		// collect fields from entity references
		if (!service.getAutoAttributes().isEmpty()) {
			for(EntityRef ref : service.getAutoAttributes()) {
				// determine entity reference
				String entityName = ref.getEntity() != null
					? ref.getEntity() : service.getEntity();
				assert entityName != null;
				EntityFinder finder = 
					new EntityFinder(service.getServiceFile().getComponent(),entityName);
				if(finder.getEntity() == null)
					throw new FinderException(
					"Unable to locate entity "+entityName+" from auto-attributes on service "+
					ref.getService().getName());
				for(Field field : finder.getEntity().getFields()) {
					// filter based on includes and excludes
					if (ref.getExlude().contains(field.getName())) continue;
					if (ref.getInclude().equals(Include.PK) && !field.isPk()) continue;
					if (ref.getInclude().equals(Include.NONPK) && field.isPk()) continue;
					// add attribute
					Attribute attr = OfbizFactory.eINSTANCE.createAttribute();
					attr.setName(field.getName());
					attr.setType(field.getType());
					attr.setMode(ref.getMode());
					attr.setOptional(ref.isOptional());
					attributes.add(attr);
				}
			}
		}
		
		// handle override
			// TODO: implement override in AttributeFinder
		
		// add the service's own attribute ot the mix
		attributes.addAll(service.getAttributes());
		
		return attributes;
	}
}
