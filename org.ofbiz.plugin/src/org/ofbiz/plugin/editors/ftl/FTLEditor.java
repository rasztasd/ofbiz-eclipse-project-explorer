package org.ofbiz.plugin.editors.ftl;

import java.lang.reflect.Field;

import org.jboss.ide.eclipse.freemarker.editor.ColorManager;
import org.jboss.ide.eclipse.freemarker.editor.DocumentProvider;
import org.jboss.ide.eclipse.freemarker.editor.Editor;

public class FTLEditor extends Editor {
	public FTLEditor() {
		try {
			Field declaredField = Editor.class.getDeclaredField("configuration");
			declaredField.setAccessible(true);
			FtlSourceViewerConfiguration configuration = new FtlSourceViewerConfiguration(getPreferenceStore(), new ColorManager(), this);
			setSourceViewerConfiguration(configuration);
			declaredField.set(this, configuration);
			setDocumentProvider(new DocumentProvider());
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
