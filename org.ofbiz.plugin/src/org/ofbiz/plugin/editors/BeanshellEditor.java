package org.ofbiz.plugin.editors;

import org.eclipse.ui.editors.text.TextEditor;

public class BeanshellEditor extends TextEditor {

	private ColorManager colorManager;

	public BeanshellEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new XMLConfiguration(colorManager));
		setDocumentProvider(new XMLDocumentProvider());
	}
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

}
