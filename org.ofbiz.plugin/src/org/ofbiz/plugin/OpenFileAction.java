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
 * $Id: OpenFileAction.java,v 1.2 2008/01/18 12:31:23 hessellund Exp $
 */
package org.ofbiz.plugin;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.ofbiz.plugin.analysis.Analysis;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.IEntity;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.ofbiz.util.OfbizSwitch;


public class OpenFileAction implements IObjectActionDelegate {

	private IWorkbenchPart targetPart;
	private ISelection selection;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	public void run(final IAction action) {
		if(!Plugin.USE_MARKERS) {
			MessageDialog.openError(
				targetPart.getSite().getShell(), 
				"Operation Unavailable",
				"Markers are turned off");
			return;
		}
		assert targetPart instanceof ExplorerView;
		if (!(selection instanceof TreeSelection)) return;
		Object treeSelect = ((TreeSelection)selection).getFirstElement();
		assert treeSelect instanceof EObject;
		EObject eObj = (EObject) treeSelect;
		new OfbizSwitch<Object>(){
			@Override
			public Object caseIEntity(IEntity object) {
				Component component = object.getComponent();
				IFolder folder = component.getFolder();
				String entityName = object.getName();
				IMarker marker = resolveMarker(folder, entityName, "xml");
				if (marker==null || !openFile(marker)) {
					MessageDialog.openError(
						targetPart.getSite().getShell(),
						"OFBiz Explorer",
						"Unable to locate definition for "+object.getName());
				}
				return this;
			}
			@Override
			public Object caseService(Service object) {
				Component component = object.getComponent();
				IFolder folder = component.getFolder();
				String serviceName = object.getName();
				if ("org.ofbiz.plugin.action.showserviceimpl".equals(action.getId())) {
					if(!object.getEngine().equals("java")) {
						MessageDialog.openError(
							targetPart.getSite().getShell(),
							"OFBiz Explorer",
							object.getName()+" is not a java-based service"+ 
							"and can therefore not be located");
						return this;
					}
					// show code
					IMarker marker = resolveMarker(folder, serviceName, "java");
					if(marker==null) {
						new Analysis(
							component.getDirectory().getProject().getJavaproject(),
							object, component.getDirectory().getProject()).run(true);
						marker = resolveMarker(folder, serviceName, "java");
					}
					if (marker==null || !openFile(marker)) {
						MessageDialog.openError(
							targetPart.getSite().getShell(),
							"OFBiz Explorer",
							"Unable to locate implementation for "+object.getName());
					}
				} else if ("org.ofbiz.plugin.action.showservicedef".equals(action.getId())) {
					// show XML
					IMarker marker = resolveMarker(folder, serviceName, "xml");
					if (marker==null || !openFile(marker)) {
						MessageDialog.openError(
							targetPart.getSite().getShell(),
							"OFBiz Explorer",
							"Unable to locate definition for "+object.getName());
					}
				} 
				return this;
			}
		}.doSwitch(eObj);
	}
	
	
	private IMarker resolveMarker(IFolder folder, String name, String fileExtension) {
		try {
			IMarker[] markers =
				folder.findMarkers(
				Plugin.TEXT_MARKER, true, IResource.DEPTH_INFINITE);
			for(IMarker m : markers) {
				if(m.getAttribute("name").equals(name) &&
				   m.getResource().getName().endsWith(fileExtension)) {
					return m;
				}
			}
		} catch (CoreException e) {
			Plugin.logError("Unable to resolve marker "+name,e);
		}
		return null;
	}
	
	private boolean openFile(IMarker marker) {
		IWorkbenchPage page = 
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(page, marker);
			return true;
		} catch (PartInitException e) {
			Plugin.logInfo("Unable to open editor for "+marker, e);
			return false;
		}
	}
}