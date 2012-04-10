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
 * $Id: DirectoryParser.java,v 1.1 2008/01/17 18:48:15 hessellund Exp $
 */
package org.ofbiz.plugin.parser;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Directory;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.Project;
import org.xmlpull.v1.XmlPullParser;



public class DirectoryParser extends Parser {

	private final Project project;
	private static final String
		LOADCOMPONENTS = "load-components",
		PARENTDIRECTORY = "parent-directory",
		LOADCOMPONENT = "load-component",
		COMPONENTNAME = "component-name",
		COMPONENTLOCATION = "component-location";
	
	public DirectoryParser(Project project) {
		assert project != null;
		assert project.getProject() != null;
		this.project = project;
	}

	@Override
	protected void processStartElement(XmlPullParser xpp) {
		if (xpp.getName().equals(LOADCOMPONENTS)) {
			Plugin.debug("process directory from "+file.getFullPath());
			processDirectories(xpp);
		} else if (xpp.getName().equals(LOADCOMPONENT)) {
			Plugin.debug("process component from "+file.getFullPath());
			processComponents(xpp);
		} 
	}
	
	private void processDirectories(XmlPullParser xpp) {
		Directory dir = OfbizFactory.eINSTANCE.createDirectory();
		String location = xpp.getAttributeValue(null, PARENTDIRECTORY); 
		if (location==null) {
			Plugin.logError("Skipped loading of directory because no location is given");
			return;
		}
		dir.setName(location.substring(location.lastIndexOf('/')+1));
		if (location.startsWith("${ofbiz.home}/")) {
			location = location.substring(14);
			Plugin.debug("trimmed location of ${ofbiz.home} variable: "+location);
		} 
		IResource res = project.getProject().findMember(location);
		if (res == null || !res.exists() || !(res instanceof IFolder)) {
			Plugin.logError("Skipped loading of directory because location is invalid: "+location);
			return;
		}
		dir.setFolder( (IFolder) res );
		project.getDirectories().add(dir);
		Plugin.debug("added directory: "+dir);
	}

	private Directory dir;
	public void setDirectory(Directory dir) {
		this.dir = dir;
	}
	
	private void processComponents(XmlPullParser xpp) {
		// configure domain objects
		assert dir != null;
		Component component = OfbizFactory.eINSTANCE.createComponent();
		String name = xpp.getAttributeValue(null, COMPONENTNAME);
		String location = xpp.getAttributeValue(null, COMPONENTLOCATION);
		if (location==null) {
			Plugin.logError("Skipped loading of components because no location is given: "+file.getFullPath());
			return;
		}
		IResource res = null;
		if (location.startsWith("${ofbiz.home}/")) {
			location = location.substring(14);
			Plugin.debug("trimmed location of ${ofbiz.home} variable: "+location);
			// old OFBiz versions use project relative paths
			res = project.getProject().findMember(location);
		} else {
			// new OFBiz versions use directory relative paths
			res = dir.getFolder().findMember(location);
		}
		if (res == null || !res.exists() || !(res instanceof IFolder)) {
			Plugin.logError("Skipped loading of directory because location is invalid: "+location);
			return;
		}
		component.setName(name != null ? name : location);
		component.setFolder((IFolder) res);
		component.setDirectory(dir);
		Plugin.debug("added component: "+component);
	}

	@Override
	protected String getMarkerType() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
