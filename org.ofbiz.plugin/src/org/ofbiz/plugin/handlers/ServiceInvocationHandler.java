package org.ofbiz.plugin.handlers;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.model.ServiceHelper;
import org.ofbiz.plugin.ofbiz.Attribute;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.parser.AttributeFinder;
import org.ofbiz.plugin.parser.FinderException;

public class ServiceInvocationHandler extends AbstractHandler {
	private Dialog dialog;
	IEditorPart activeEditor;
	IEditorInput editorInput;
	
	public Object execdute(ExecutionEvent executionEvent) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(executionEvent);
		activeEditor = Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		editorInput = activeEditor.getSite().getWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			InsertServiceInvocationDialog dialog = new InsertServiceInvocationDialog(activeEditor, window);
			dialog.open();
		}
		return null;
	}
	@Override
	public Object execute(ExecutionEvent executionEvent) throws ExecutionException {
		//		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(executionEvent);
		//		final IEditorPart activeEditor = window.getActivePage().getActiveEditor();
		//		IEditorInput editorInput = activeEditor.getSite().getWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(executionEvent);
		activeEditor = Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		editorInput = activeEditor.getSite().getWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
		if (editorInput instanceof IFileEditorInput) {			
			final IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
			if (dialog == null) {
				dialog = new Dialog(window.getShell()) {

					@Override
					protected Control createContents(Composite parent) {
						return createDialog(window, parent);
					}

					private Control createDialog(final IWorkbenchWindow window,
							final Composite parent) {
						Layout l;
						parent.setLayout(new FillLayout());
//						parent.setLayout(new ColumnLayout());
						final Composite viewForm = new Composite(parent, 0);
//						viewForm.setSize(600, 600);
//						viewForm.setLayoutData(new ColumnLayoutData());
						GridLayout formGridLayout = new GridLayout(2, false);
						viewForm.setLayout(formGridLayout);
						Composite panel1 = new Composite(viewForm, 0);
						GridData panel1FormData = new GridData(GridData.HORIZONTAL_ALIGN_FILL, GridData.VERTICAL_ALIGN_FILL, true, true);
						panel1FormData.heightHint = 800;
						panel1FormData.widthHint = 600;
						panel1.setLayoutData(panel1FormData);
						panel1.setLayout(new GridLayout(1, false));
						GridData panel2FormData = new GridData(GridData.HORIZONTAL_ALIGN_FILL, GridData.VERTICAL_ALIGN_FILL, true, true);
						final ScrolledComposite sc = new ScrolledComposite(viewForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
						sc.setLayout(new FillLayout());
						final Composite panel2 = new Composite(sc, 0);
						sc.setLayoutData(panel2FormData);
						sc.setContent(panel2);
						panel2FormData.heightHint = 800;
						panel2FormData.widthHint = 300;
						panel2.setLayoutData(sc);
						sc.setExpandHorizontal(true);
						sc.setExpandVertical(true);
						panel2.setLayout(new FillLayout());
						Button button = new Button(viewForm, 0);

						final ArrayList<Service> sortedServices = new ArrayList<Service>(ServiceHelper.getSortedServices());
						int i = 0;
//						Label label = new Label(panel1, 0); 	//0 0
//						label.setText("Service: ");
//						serviceListComposite.setLayout(new GridLayout(1, false));
//						serviceListComposite.setLayoutData(new GridData(300, 300));
						ILabelProvider labelProvider = new ILabelProvider() {

							@Override
							public void removeListener(ILabelProviderListener arg0) {
								// TODO Auto-generated method stub

							}

							@Override
							public boolean isLabelProperty(Object arg0, String arg1) {
								return false;
							}

							@Override
							public void dispose() {

							}

							@Override
							public void addListener(ILabelProviderListener arg0) {

							}

							@Override
							public String getText(Object arg0) {
								return ((Service)arg0).getName();
							}

							@Override
							public Image getImage(Object arg0) {
								// TODO Auto-generated method stub
								return null;
							}
						};
						//					final List serviceList = new List(serviceListComposite, 0);
						final ComboViewer serviceList = new ComboViewer(panel1);
						serviceList.setUseHashlookup(true);
//						Label parameters = new Label(panel2, SWT.NONE); // 1 0
//						parameters.setText("parameters");
						for (Service service : sortedServices) {
							serviceList.getCombo().add(service.getName());
						}
						final Composite parametersComposite = new Composite(panel2, 0); // 1 1
						parametersComposite.setLayout(new GridLayout(2, false));
						serviceList.getCombo().addSelectionListener(new SelectionListener() {
							Composite toDraw = parametersComposite;
							@Override
							public void widgetSelected(SelectionEvent arg0) {
								Service currentService = sortedServices.get(serviceList.getCombo().getSelectionIndex());
								for (Control children : parametersComposite.getChildren()) {
									children.dispose();
								}
								//							parametersComposite.dispose();
								try {
									for (Attribute attribute : new AttributeFinder(currentService).getAttributes()) {
										Label label2 = new Label(parametersComposite, 0);
										label2.setText(attribute.getName() + " mode: " + attribute.getMode().getName());
										Button button = new Button(parametersComposite, SWT.CHECK);
										button.setData(attribute);
										if (!attribute.isOptional()) {
											button.setEnabled(false);
											button.setSelection(true);
										}
									}
									parametersComposite.layout(true);
									parametersComposite.pack(true);
									sc.setMinSize(panel2.computeSize(SWT.DEFAULT, SWT.DEFAULT));
									sc.layout(true);
									sc.pack(true);
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
						button.setText("Insert text");
						button.addSelectionListener(new SelectionListener() {

							@Override
							public void widgetSelected(SelectionEvent arg0) {
								Service currentService = sortedServices.get(serviceList.getCombo().getSelectionIndex());
								Control[] attributesChildren = parametersComposite.getChildren();
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
						return viewForm;
					}

//					@Override
//					protected Point getInitialSize() {
//						return new Point(800, 800);
//					}


				};
			}
			dialog.open();
			return null;
		}
		MessageDialog.openInformation(
				window.getShell(),
				"Insert service invocation",
				"Couldn't insert Service invocation to this file.");
		return null;
	}

}
