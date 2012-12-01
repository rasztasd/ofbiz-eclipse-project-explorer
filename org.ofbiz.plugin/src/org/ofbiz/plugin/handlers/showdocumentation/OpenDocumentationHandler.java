package org.ofbiz.plugin.handlers.showdocumentation;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenDocumentationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		OpenOfbizDocumentationDialog componentDialog = new OpenOfbizDocumentationDialog(window.getShell(), true, null, 0);
		componentDialog.open();
		return null;
	}

}
