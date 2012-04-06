/**
 * Copyright 2008 Anders Hessellund 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: OpenFileAction.java,v 1.2 2008/01/18 12:31:23 hessellund Exp $
 */
package org.ofbiz.plugin.action;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.ofbiz.plugin.model.ViewEntitySqlHelper;
import org.ofbiz.plugin.ofbiz.ViewEntity;


public class ShowViewEntitySqlAction implements IObjectActionDelegate {

	private IWorkbenchPart targetPart;
	private ISelection selection;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	public void run(final IAction action) {
		Object treeSelect = ((TreeSelection)selection).getFirstElement();
		assert treeSelect instanceof EObject;
		ViewEntity viewEntity = (ViewEntity) treeSelect;
		final Display display = Display.getDefault();
		final Shell shell = new Shell(display);
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		Composite composite = new Composite(shell, 0);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		Text text = new Text(composite, SWT.SINGLE);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		text.setText(ViewEntitySqlHelper.makeViewTable(viewEntity));
		shell.open();
		shell.layout();
	}
	
}