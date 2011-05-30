package org.ofbiz.plugin.handlers;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.ofbiz.plugin.model.ServiceHelper;
import org.ofbiz.plugin.ofbiz.Attribute;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.parser.AttributeFinder;
import org.ofbiz.plugin.parser.FinderException;
import org.eclipse.jface.viewers.ComboViewer;

public class InsertServiceInvocationDialog {
	private IEditorPart activeEditor;
	private IWorkbenchWindow window;
	/**
	 * Launch the application.
	 * @param args
	 */
	public InsertServiceInvocationDialog() {
		
	}
	
	public InsertServiceInvocationDialog(IEditorPart activeEditor, IWorkbenchWindow window) {
		this.activeEditor = activeEditor;
		this.window = window;
		
	}
	public static void main(String[] args) {
		try {
			InsertServiceInvocationDialog window = new InsertServiceInvocationDialog();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		final Display display = Display.getDefault();
		final Shell shell = new Shell();
		shell.setSize(573, 629);
		shell.setText("SWT Application");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		final ArrayList<Service> sortedServices = new ArrayList<Service>(ServiceHelper.getSortedServices());
		
		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayout(new FormLayout());
		
		ScrolledComposite panel2 = new ScrolledComposite(composite_1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		FormData fd_scrolledComposite = new FormData();
		fd_scrolledComposite.right = new FormAttachment(0, 217);
		fd_scrolledComposite.bottom = new FormAttachment(0, 527);
		fd_scrolledComposite.top = new FormAttachment(0);
		fd_scrolledComposite.left = new FormAttachment(0);
		panel2.setLayoutData(fd_scrolledComposite);
		panel2.setExpandHorizontal(true);
		panel2.setExpandVertical(true);
		final ScrolledComposite scrolledComposite_1 = new ScrolledComposite(composite_1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		FormData fd_scrolledComposite_1 = new FormData();
		fd_scrolledComposite_1.bottom = new FormAttachment(panel2, 0, SWT.BOTTOM);
		
		ComboViewer comboViewer = new ComboViewer(panel2, SWT.NONE);
		final Combo combo = comboViewer.getCombo();
		final Composite composite = new Composite(scrolledComposite_1, SWT.NONE);
		combo.addSelectionListener(new SelectionListener() {
			Composite toDraw = composite;
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Service currentService = sortedServices.get(combo.getSelectionIndex());
				for (Control children : toDraw.getChildren()) {
					children.dispose();
				}
				//							parametersComposite.dispose();
				try {
					for (Attribute attribute : new AttributeFinder(currentService).getAttributes()) {
						Label label2 = new Label(toDraw, 0);
						label2.setText(attribute.getName() + " mode: " + attribute.getMode().getName());
						Button button = new Button(toDraw, SWT.CHECK);
						button.setData(attribute);
						if (!attribute.isOptional()) {
							button.setEnabled(false);
							button.setSelection(true);
						}
					}
					toDraw.layout(true);
				} catch (FinderException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		panel2.setContent(combo);
		panel2.setMinSize(combo.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		fd_scrolledComposite_1.right = new FormAttachment(0, 557);
		fd_scrolledComposite_1.top = new FormAttachment(0);
		fd_scrolledComposite_1.left = new FormAttachment(0, 217);
		scrolledComposite_1.setLayoutData(fd_scrolledComposite_1);
		scrolledComposite_1.setExpandHorizontal(true);
		scrolledComposite_1.setExpandVertical(true);
		for (Service service : sortedServices) {
			combo.add(service.getName());
		}
		
		
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.horizontalSpacing = 2;
		composite.setLayout(gl_composite);
		scrolledComposite_1.setContent(composite);
		scrolledComposite_1.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		Button btnNewButton = new Button(composite_1, SWT.NONE);
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.bottom = new FormAttachment(100, -10);
		fd_btnNewButton.left = new FormAttachment(0, 24);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("Insert service");
		btnNewButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Service currentService = sortedServices.get(combo.getSelectionIndex());
				Control[] attributesChildren = composite.getChildren();
				if (!(activeEditor instanceof AbstractTextEditor)) {
					MessageDialog.openInformation(
							window.getShell(),
							"Insert service invocation",
							"Couldn't insert Service invocation to this editor.");		
					return;
				}
				ITextEditor editor = (ITextEditor) activeEditor;
				IDocumentProvider dp = editor.getDocumentProvider();
				IDocument doc = dp.getDocument(editor.getEditorInput());

				int offset = ((ITextSelection)editor.getSelectionProvider().getSelection()).getOffset();;
				StringBuilder sb = new StringBuilder("Map<String, Object> inputMap = new FastMap();\n");
				StringBuilder outMap = new StringBuilder();
				for (Control attributeChildren : attributesChildren) {
					if (attributeChildren instanceof Button) {
						Button attributeCheckButton = (Button) attributeChildren;
						if (attributeCheckButton.getSelection()) {
							Attribute data = (Attribute) attributeCheckButton.getData();
							if (data.getMode().getName().equals("IN") || data.getMode().getName().equals("INOUT")) {
								sb.append("inputMap.put(\"").append(data.getName()).append("\", \"\");\t //type:").append(data.getType()).append("\n");
							}
							if (data.getMode().getName().equals("OUT") || data.getMode().getName().equals("INOUT")) {
								outMap.append("outMap.get(\"").append(data.getName()).append("\"); // type:").append(data.getType()).append("\n");;
							}
						}
					}
				}
				sb.append("Map<String, Object> outMap = dispatcher.runSync(\"").append(currentService.getName()).append("\", inputMap);\n");
				sb.append(outMap);
				try {
					doc.replace(offset, 0, sb.toString()+"\n");

				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}
