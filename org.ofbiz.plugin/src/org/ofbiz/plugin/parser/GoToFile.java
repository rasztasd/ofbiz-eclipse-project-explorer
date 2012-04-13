package org.ofbiz.plugin.parser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.HasXmlDefinition;
import org.ofbiz.plugin.ofbiz.IEntity;
import org.ofbiz.plugin.ofbiz.Service;

public class GoToFile {
	public static boolean gotoFile(HasXmlDefinition xmlDefinition) {
		String markerKey = xmlDefinition.getMarkerKey();
		IFile file = xmlDefinition.getFile();
		return gotoLine(markerKey, file);
	}
	public static boolean gotoFile(IEntity entity) {
		gotoLine(entity.getComponent().getFolder(), entity.getName(), "xml");
		return true;
	}
	public static IMarker getMarker(HasXmlDefinition xmlDefinition) {
		String markerKey = xmlDefinition.getMarkerKey();
		IFile file = xmlDefinition.getFile();
		return resolveMarker(file, markerKey);
	}
	
	public static IMarker getMarker(IFile file, String markerKey) {
		return resolveMarker(file, markerKey);
	}
	
	public static IWorkbenchPage getActiveWorkbenchPage() {
		IWorkbenchPage page = 
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		return page;
	}
	
	private static boolean gotoLine(String markerKey, IFile file) {
		IWorkbenchPage page = 
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IMarker marker = resolveMarker(file, markerKey);
		try {
			IDE.openEditor(page, marker);
			return true;
		} catch (PartInitException e) {
			Plugin.logInfo("Unable to open editor for "+marker, e);
			return false;
		}
	}
	private static void gotoLine(IFolder folder, String entityName, String extension) {
		IMarker marker = resolveMarker(folder, entityName, extension);
		openFile(marker);
	}
	public static IMarker resolveMarker(IFile file, String name) {
		try {
			IMarker[] markers = 
				file.findMarkers("org.ofbiz.plugin.text", true, IResource.DEPTH_INFINITE);
			for(IMarker m : markers) {
				if(m.getAttribute("name").equals(name)) {
					return m;
				}
			}
		} catch (CoreException e) {
			Plugin.logError("Unable to resolve marker "+name,e);
		}
		return null;
	}
	private static boolean openFile(IMarker marker) {
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
	private static IMarker resolveMarker(IFolder folder, String name, String fileExtension) {
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
}
