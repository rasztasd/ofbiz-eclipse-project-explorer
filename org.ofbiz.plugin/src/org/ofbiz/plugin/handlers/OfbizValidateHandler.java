package org.ofbiz.plugin.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.model.ControllerHelper;
import org.ofbiz.plugin.model.ScreenHelper;
import org.ofbiz.plugin.model.ServiceHelper;
import org.ofbiz.plugin.ofbiz.Controller;
import org.ofbiz.plugin.ofbiz.Screen;
import org.ofbiz.plugin.parser.Parser;
import org.xmlpull.v1.XmlPullParser;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OfbizValidateHandler extends AbstractHandler {
	private static final String MARKER_TYPE = "org.ofbiz.plugin.xmlProblem";
	/**
	 * The constructor.
	 */
	public OfbizValidateHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IEditorInput editorInput = Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getSite().getWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) editorInput).getFile();
			if (file.getFullPath().toString().endsWith("controller.xml")) {
				Controller controller = ControllerHelper.getController(file);
				if (controller == null) { //Maybe a parse error for the controller
					MessageDialog.openInformation(
							window.getShell(),
							"Validate current file",
					"Not a registered controller.xml file. Maybe the file is never used.");
					return null;
				}
				deleteMarkers(file);
				try {
					XmlPullParser xpp = Plugin.getDefault().getXmlPullParserPool().getPullParserFromPool();
					ControllerXmlParser controllerXmlParser = new ControllerXmlParser(controller);
					controllerXmlParser.processDocument(xpp, file);
					controllerXmlParser.generateUnusedViews();
					
				} catch (Exception e1) {
				}
				MessageDialog.openInformation(
						window.getShell(),
						"Validate current file",
				"Validate controller.xml Ok.");
			} else {
				MessageDialog.openInformation(
						window.getShell(),
						"Validate current file",
				"Current file validation not implemented yet.");
			}
		}
		return null;
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}
	private class ControllerXmlParser extends Parser {
		private Controller controller;
		private Set<String> viewReferences = new HashSet<String>();
		private Map<String, Integer> viewDeclarations = new HashMap<String, Integer>();
		private Map<String, Integer> requestDeclarations = new HashMap<String, Integer>();

		public ControllerXmlParser(Controller controller) {
			this.controller = controller;
		}

		@Override
		protected void processStartElement(XmlPullParser xpp) {
			String name = xpp.getName();
			String attributeTypeValue = xpp.getAttributeValue(null, "type");
			if (name.equals("view-map")) {
				String pageValue = xpp.getAttributeValue(null, "page");
				Integer previousDeclaration = viewDeclarations.put(xpp.getAttributeValue(null, "name"), xpp.getLineNumber());
				if (previousDeclaration != null) {
					addMarker(file, "View map is masked/overriden by another View map", previousDeclaration, IMarker.SEVERITY_WARNING);
					addMarker(file, "This View map masks/overrides another View map", xpp.getLineNumber(), IMarker.SEVERITY_WARNING);
				}
				if (pageValue.equals("component://emerald_admin/widget/voucherstock/Vouche rStockScreens.xml#orionexportresult")) {
					System.out.println();
				}
				if ("screen".equals(attributeTypeValue)) {
					Screen screensByComponentName = ScreenHelper.getScreensByComponentName(controller.getComponent().getDirectory().getProject(), pageValue);
					if (screensByComponentName == null) {
						addMarker(file, "Couldn't find Screen", xpp.getLineNumber(), IMarker.SEVERITY_ERROR);
					}
				} else if ("ftl".equals(attributeTypeValue)) {
					String page = pageValue;
					Controller controller = ControllerHelper.getController(file);
					IFile findMember;
					if (page.contains("component://")) {
						findMember = null;
					} else if (page.indexOf("/") == 0) {
						findMember = null;
					} else {
						findMember = (IFile) controller.getFile().getParent().getParent().findMember(page);
					}
					if (findMember == null) {
						addMarker(file, "Couldn't find ftl file", xpp.getLineNumber(), IMarker.SEVERITY_ERROR);
					}
				}
			} else if (name.equals("response")) {
				String attributeValueValue = xpp.getAttributeValue(null, "value");
				if ("view".equals(attributeTypeValue)) {					
					viewReferences.add(attributeValueValue);
					if (ControllerHelper.getHyperlinkForRequestValueView(attributeValueValue, file).size() == 0) {
						addMarker(file, "Couldn't find view", xpp.getLineNumber(), IMarker.SEVERITY_ERROR);
					}
				} else if ("request".equals(attributeTypeValue)) {
					if (ControllerHelper.getHyperlinkForRequestValueRequest(attributeValueValue, file).size() == 0) {
						addMarker(file, "Couldn't find request", xpp.getLineNumber(), IMarker.SEVERITY_ERROR);
					}
				}
			} else if (name.equals("event")) {
				if (attributeTypeValue.equals("service") || attributeTypeValue.equals("service-multi")) {
					if (ServiceHelper.findServiceByName(xpp.getAttributeValue(null, "invoke"), controller.getComponent().getDirectory().getProject()).size() == 0) {
						addMarker(file, "Couldn't find service", xpp.getLineNumber(), IMarker.SEVERITY_ERROR);
					}
				}
			} else if (name.equals("request-map")) {
				Integer previousDeclaration = requestDeclarations.put(xpp.getAttributeValue(null, "uri"), xpp.getLineNumber());
				if (previousDeclaration != null) {
					addMarker(file, "Request map is masked/overriden by another request", previousDeclaration, IMarker.SEVERITY_WARNING);
					addMarker(file, "This request map masks/overrides another request", xpp.getLineNumber(), IMarker.SEVERITY_WARNING);
				}
			}
		}
		public void generateUnusedViews() {
			for (String viewDeclaration : viewDeclarations.keySet()) {
				if (!viewReferences.contains(viewDeclaration)) {
					Integer lineNumber = viewDeclarations.get(viewDeclaration);
					addMarker(file, "Unused view", lineNumber, IMarker.SEVERITY_WARNING);
				}
			}
		}

		@Override
		protected String getMarkerType() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	private void addMarker(IFile file, String message, int lineNumber,
			int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}


}
