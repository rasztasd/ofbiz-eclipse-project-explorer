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
 * $Id: ServiceFinder.java,v 1.1 2008/01/17 18:48:15 hessellund Exp $
 */
package org.ofbiz.plugin.parser;

import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Directory;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.Service;

public class ServiceFinder {
	private Project project;
	private Directory dir;
	private Component component;
	private String serviceName;
	public ServiceFinder(Project project, String serviceName) {
		assert project != null;
		assert serviceName != null;
		assert serviceName.length()>0;
		this.project = project;
		this.serviceName = serviceName;
	}
	public ServiceFinder(Directory dir, String serviceName) {
		this(dir.getProject(),serviceName);
		this.dir = dir;
	}
	public ServiceFinder(Component component, String serviceName) {
		this(component.getDirectory(),serviceName);
		this.component = component;
	}
	public Service getService() throws FinderException {
		// 1) search component first
		if (component!=null) {
			for(Service s : component.getServices()) {
				if(serviceName.equals(s.getName()))
					return s;
			}
		}
		// 2) expand to directory
		if (dir!=null) {
			for(Component c : dir.getComponents()) {
				if (c==component) continue; // skip
				for(Service s : c.getServices()) {
					if(serviceName.equals(s.getName()))
						return s;
				}
			}
		}
		// 3) search entire project
		for(Directory d : project.getDirectories()) {
			if (d==dir) continue;
			for(Component c : d.getComponents()) {
				for(Service s : c.getServices()) {
					if(serviceName.equals(s.getName()))
						return s;
				}
			}
		}
		throw new FinderException("Unable to locate service: "+serviceName);
	}
}
