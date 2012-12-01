package org.ofbiz.plugin.editors.ftl;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.jboss.ide.eclipse.freemarker.editor.ColorManager;
import org.jboss.ide.eclipse.freemarker.editor.Configuration;
import org.jboss.ide.eclipse.freemarker.editor.Editor;

public class FtlSourceViewerConfiguration extends Configuration {
	public FtlSourceViewerConfiguration(IPreferenceStore preferenceStore, ColorManager colorManager, Editor editor) {
		super(preferenceStore, colorManager, editor);
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer,
			String contentType) {
		return new FTLTextHover();
	}
}
