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
 * $Id: EntityFinder.java,v 1.1 2008/01/17 18:48:15 hessellund Exp $
 */
package org.ofbiz.plugin.parser;

import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Directory;
import org.ofbiz.plugin.ofbiz.IEntity;
import org.ofbiz.plugin.ofbiz.Project;

public class EntityFinder {
	private Project project;
	private Directory dir;
	private Component component;
	private String entityName;
	public EntityFinder(Project project, String entityName) {
		assert project != null;
		assert entityName != null;
		assert entityName.length()>0;
		this.project = project;
		this.entityName = entityName;
	}
	public EntityFinder(Directory dir, String entityName) {
		this(dir.getProject(),entityName);
		this.dir = dir;
	}
	public EntityFinder(Component component, String entityName) {
		this(component.getDirectory(),entityName);
		this.component = component;
	}
	public IEntity getEntity() {
		// 1) search component first
		if (component!=null) {
			for(IEntity e : component.getEntities()) {
				if(entityName.equals(e.getName()))
					return e;
			}
		}
		// 2) expand to directory
		if (dir!=null) {
			for(Component c : dir.getComponents()) {
				if (c==component) continue; // skip
				for(IEntity e : c.getEntities()) {
					if(entityName.equals(e.getName()))
						return e;
				}
			}
		}
		// 3) search entire project
		for(Directory d : project.getDirectories()) {
			if (d==dir) continue;
			for(Component c : d.getComponents()) {
				for(IEntity e : c.getEntities()) {
					if(entityName.equals(e.getName()))
						return e;
				}
			}
		}
		return null;
	}
}
