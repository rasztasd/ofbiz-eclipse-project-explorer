package org.ofbiz.plugin.model.hyperlink;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.ofbiz.plugin.parser.GoToFile;

public class FtlHyperlink extends HyperlinkMarker {
	private String page;
	private IFile file;
	public FtlHyperlink(String page, IFile file) {
		this.page = page;
		this.file = file;
	}

	@Override
	public String getTypeLabel() {
		return "";
	}

	@Override
	public String getHyperlinkText() {
		return "Ftl file: " + page;
	}

	@Override
	public void open() {
		try {
			IDE.openEditor(GoToFile.getActiveWorkbenchPage(), (IFile) file);
		} catch (PartInitException e) {
		}
	}

}
