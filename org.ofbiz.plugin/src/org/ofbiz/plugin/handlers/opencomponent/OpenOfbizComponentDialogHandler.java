package org.ofbiz.plugin.handlers.opencomponent;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.ofbiz.plugin.Plugin;

public class OpenOfbizComponentDialogHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent executionEvent) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(executionEvent);
		OpenComponentDialog componentDialog = new OpenComponentDialog(window.getShell(), true, null, 0);
		componentDialog.open();
		return null;
	}
}