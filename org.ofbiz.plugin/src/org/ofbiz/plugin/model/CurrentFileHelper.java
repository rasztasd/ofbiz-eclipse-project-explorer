package org.ofbiz.plugin.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.ofbiz.HasXmlDefinition;

public class CurrentFileHelper {
	public static HasXmlDefinition getCurrentElement() {
		IWorkbenchWindow activeWorkbenchWindow = Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		IEditorPart activeEditor2 = activeWorkbenchWindow.getActivePage().getActiveEditor();
		if (activeEditor2 == null) {
			return null;
		}
		IEditorInput editorInput = activeEditor2.getEditorInput();
		if (editorInput instanceof FileEditorInput) {
			FileEditorInput fileEditorInput = (FileEditorInput) editorInput;
			try {
				IMarker[] findMarkers = fileEditorInput.getFile().findMarkers("org.ofbiz.plugin.text", true, IResource.DEPTH_INFINITE);
				for (IMarker marker : findMarkers) {
					String type = marker.getType();
					if (type.equals("org.ofbiz.plugin.iEntityMarker")) {
						System.out.println();
//						EntityHelper.getHyperlinksForEntity(searchString)
						break;
					}
					System.out.println();
				}
			} catch (CoreException e) {
			}
		}
		return null;
	}
}
