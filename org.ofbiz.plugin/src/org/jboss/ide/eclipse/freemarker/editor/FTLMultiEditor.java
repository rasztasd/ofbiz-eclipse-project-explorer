package org.jboss.ide.eclipse.freemarker.editor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PartInitException;
import org.jboss.ide.eclipse.freemarker.editor.FreemarkerMultiPageEditor;
import org.ofbiz.plugin.editors.ftl.FTLEditor;


public class FTLMultiEditor extends FreemarkerMultiPageEditor {
	private Editor vEditor2;

	public FTLMultiEditor() throws SecurityException, NoSuchMethodException {
		super();		
	}


	@Override
	protected void createPages() {
		createPage0();
		if (!(this.vEditor2.isEditorInputReadOnly())) {
			try {
				Method declaredMethod = FreemarkerMultiPageEditor.class.getDeclaredMethod("createContextPage");
				declaredMethod.setAccessible(true);
				declaredMethod.invoke(this);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	}



	@Override
	void createPage0() {
		// TODO Auto-generated method stub
		try {
			this.vEditor2 = new FTLEditor();
			this.vEditor2.init(getEditorSite(), getEditorInput());
			Field declaredField = FreemarkerMultiPageEditor.class.getDeclaredField("vEditor");
			declaredField.setAccessible(true);
			declaredField.set(this, vEditor2);			
			int index = addPage(this.vEditor2, getEditorInput());
			setPageText(index, "Source");
			setPartName(this.vEditor2.getTitle());
		}
		catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text vEditor", null, e.getStatus());

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
