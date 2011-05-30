package org.ofbiz.plugin.model.hyperlink;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.ofbiz.plugin.Plugin;

public abstract class HyperlinkMarker {
	private IMarker marker;
	public HyperlinkMarker(IMarker marker) {
		this.marker = marker;
	}
	public HyperlinkMarker() {
	}
	public void open() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(page, marker);
		} catch (PartInitException e) {
			Plugin.logInfo("Unable to open editor for "+marker, e);
		}
	}
	public abstract String getTypeLabel();
	public abstract String getHyperlinkText();
	public IRegion getHyperlinkRegion() {
		return null;
	}

}
