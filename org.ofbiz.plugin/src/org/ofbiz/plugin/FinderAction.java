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
 * $Id: FinderAction.java,v 1.1 2008/01/17 18:48:20 hessellund Exp $
 */
package org.ofbiz.plugin;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

public class FinderAction implements IEditorActionDelegate {

	private IEditorPart targetEditor;
	private ISelection selection;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	public void run(IAction action) {
		if(!Plugin.USE_MARKERS) {
			MessageDialog.openError(
				targetEditor.getSite().getShell(), 
				"Operation Unavailable",
				"Markers are turned off");
			return;
		}
		//commented by PP - this is better to leave it out
		//if (!(targetEditor instanceof TextEditor)) return;
		if (!(selection instanceof TextSelection)) return;
		String name = ((TextSelection) selection).getText();
		try {
			FileEditorInput input = (FileEditorInput) targetEditor.getEditorInput();
			IMarker[] markers = 
				input.getFile().getProject().findMarkers(
				Plugin.TEXT_MARKER, true, IResource.DEPTH_INFINITE);
			for(IMarker marker : markers) {
				if(name.equals(marker.getAttribute("name"))) {
					IWorkbenchPage page = 
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IDE.openEditor(page, marker);
					return;
				}
			}
		} catch (Exception e) {
			Plugin.logError("Error occurred during find operation", e);
		} 
		MessageDialog.openError(
			targetEditor.getSite().getShell(), 
			"No Search Results",
			"Unable to locate: "+name);
	}

}
