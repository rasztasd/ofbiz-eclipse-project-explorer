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
 * $Id: Plugin.java,v 1.2 2008/01/18 12:31:23 hessellund Exp $
 */
package org.ofbiz.plugin;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.ofbiz.plugin.model.OfbizModelSingleton;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.impl.OfbizPackageImpl;
import org.osgi.framework.BundleContext;
import org.xmlpull.v1.parser_pool.XmlPullParserPool;
public class Plugin extends AbstractUIPlugin {

	// --- configuration (START)

	public static boolean USE_MARKERS = true;
	public static final boolean DEBUG = true;

	// --- configuration (END)	

	// String constants
	public static final String 
	BASECONFIG = "/framework/base/config/component-load.xml",
	TEXT_MARKER = "org.ofbiz.plugin.model.text",
	PROBLEM_MARKER = "org.ofbiz.plugin.model.problem";

	// The plug-in ID
	public static final String PLUGIN_ID = "org.ofbiz.plugin";

	// The shared instance
	private static Plugin plugin;

	/**
	 * The constructor
	 */
	public Plugin() {
		System.out.println("");
	}

	private XmlPullParserPool pool;

	public XmlPullParserPool getXmlPullParserPool() {
		return this.pool;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		pool = new XmlPullParserPool();
		// As of here we preparing to save the model content
		OfbizPackageImpl.init();
		// Register the XMI resource factory for the .website extension
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("ofbiz", new XMIResourceFactoryImpl());
		// Obtain a new resource set
		ResourceSet resSet = new ResourceSetImpl();
		// Get the resource
		try {
			Resource resource = resSet.getResource(URI
					.createURI("ofbizContent/ofbizContent.ofbiz"), true);
			for (EObject object : resource.getContents()) {
				if (object instanceof Project) {
					Project project = (Project) object;
					OfbizModelSingleton.get().addProject(project.getProject().getName(), project);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		//		resource.load(Collections.EMPTY_MAP);
		// Get the first model element and cast it to the right type, in my
		// example everything is hierarchical included in this first node
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		pool = null;
		ResourceSet resSet = new ResourceSetImpl();
		// Create a resource
		Resource resource = resSet.createResource(URI
				.createURI("ofbizContent/ofbizContent.ofbiz"));
		resource.getContents().clear();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			Project ofbizProject = OfbizModelSingleton.get().findProjectByEclipseProjectName(project.getName());
			resource.getContents().add(ofbizProject);
		}
		resource.save(Collections.EMPTY_MAP);
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Plugin getDefault() {
		return plugin;
	}

	public static void logError(String msg) {
		logError(msg,null);
	}

	public static void logError(String msg, Exception exception) {
		msg = "[ERROR] "+msg;
		plugin.getLog().log(
				new Status(
						IStatus.ERROR, 
						PLUGIN_ID, 
						IStatus.ERROR, 
						msg,
						exception));
	}

	public static void logInfo(String msg) {
		logInfo(msg,null);
	}

	public static void logInfo(String msg, Exception exception) {
		if (!msg.startsWith("[DEBUG]"))
			msg = "[INFO] "+msg;
		plugin.getLog().log(
				new Status(
						IStatus.INFO, 
						PLUGIN_ID, 
						IStatus.INFO, 
						msg,
						exception));
	}

	public static void debug(String msg) {
		if (DEBUG) logInfo("[DEBUG] "+msg, null);
	}



	public static ImageDescriptor create(String name) {
		return imageDescriptorFromPlugin(PLUGIN_ID, name);
	}
}
