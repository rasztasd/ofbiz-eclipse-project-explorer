package org.ofbiz.plugin.builder;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.model.ControllerHelper;
import org.ofbiz.plugin.model.OfbizModelSingleton;
import org.ofbiz.plugin.model.ScreenHelper;
import org.ofbiz.plugin.model.ServiceHelper;
import org.ofbiz.plugin.nature.OfbizNature;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Controller;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.Screen;
import org.ofbiz.plugin.parser.Parser;
import org.ofbiz.plugin.parser.WebappParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OfbizBuilder extends IncrementalProjectBuilder {

	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource instanceof IFile) {
				//				Project project = OfbizModelSingleton.get().findProjectByEclipseProjectName(resource.getProject().getName());
				//				IFile file = (IFile) delta.getResource();
				//				Controller controller = ControllerHelper.getController(file);
				//				String uri = controller.getUri();
				//				Component component = controller.getComponent();
				//				controller.getWebapp().setComponent(null);
				//				controller.setWebapp(null);
				//				WebappParser webAppParser = new WebappParser(component, uri, file);
				//				try {
				//					XmlPullParser xpp = Plugin.getDefault().getXmlPullParserPool().getPullParserFromPool();
				//					webAppParser.processDocument(xpp, file);
				//				} catch (XmlPullParserException e) {
				//				} catch (IOException e) {
				//				}
				switch (delta.getKind()) {
				//				case IResourceDelta.SYNC:
				//				case IResourceDelta.ADDED:
				//					// handle added resource
				//					checkControllerXml(resource);
				//					break;
				//				case IResourceDelta.REMOVED:
				//					// handle removed resource
				//					checkControllerXml(resource);
				//					break;
				case IResourceDelta.CHANGED:
					// handle changed resource
//					checkControllerXml(resource);
					break;
				}
			}
			//return true to continue visiting children.
			return true;
		}
	}

	class ControllerXmlVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			checkControllerXml(resource);
			//return true to continue visiting children.
			return true;
		}
	}

	public static final String BUILDER_ID = "org.ofbiz.plugin.builder.OfbizBuilder";

	private static final String MARKER_TYPE = "org.ofbiz.plugin.xmlProblem";

	private SAXParserFactory parserFactory;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	void checkControllerXml(IResource resource) {
		if (resource instanceof IFile) {
			//			OfbizModelSingleton.get().
			try {
				IProjectNature nature = resource.getProject().getNature(OfbizNature.ID);
				Controller controller = ControllerHelper.getController((IFile) resource);
				if (controller == null) { //Maybe a parse error for the controller
					return;
				}
				IFile file = (IFile) resource;
				deleteMarkers(file);
				try {
					XmlPullParser xpp = Plugin.getDefault().getXmlPullParserPool().getPullParserFromPool();
					new ControllerXmlParser(controller).processDocument(xpp, file);
				} catch (Exception e1) {
				}
			} catch (CoreException e) {
				return;
			}
		}
	}
	private class ControllerXmlParser extends Parser {
		private Controller controller;

		public ControllerXmlParser(Controller controller) {
			this.controller = controller;
		}

		@Override
		protected void processStartElement(XmlPullParser xpp) {
			String name = xpp.getName();
			if (name.equals("view-map")) {
				String pageValue = xpp.getAttributeValue(null, "page");
				if (xpp.getAttributeValue(null, "type").equals("screen")) {
					Screen screensByComponentName = ScreenHelper.getScreensByComponentName(controller.getComponent().getDirectory().getProject(), pageValue);
					if (screensByComponentName == null) {
						addMarker(file, "Couldn't find Screen", xpp.getLineNumber(), IMarker.SEVERITY_ERROR);
					}
				} else if (xpp.getAttributeValue(null, "type").equals("ftl")) {
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
				if (xpp.getAttributeValue(null, "type").equals("view")) {
					if (ControllerHelper.getHyperlinkForRequestValueView(xpp.getAttributeValue(null, "value"), file).size() == 0) {
						addMarker(file, "Couldn't find view", xpp.getLineNumber(), IMarker.SEVERITY_ERROR);
					}
				} else if (xpp.getAttributeValue(null, "type").equals("request")) {
					if (ControllerHelper.getHyperlinkForRequestValueRequest(xpp.getAttributeValue(null, "value"), file).size() == 0) {
						addMarker(file, "Couldn't find request", xpp.getLineNumber(), IMarker.SEVERITY_ERROR);
					}
				}
			} else if (name.equals("event")) {
				if (xpp.getAttributeValue(null, "type").equals("service")) {
					if (ServiceHelper.findServiceByName(xpp.getAttributeValue(null, "invoke"), controller.getComponent().getDirectory().getProject()).size() == 0) {
						addMarker(file, "Couldn't find service", xpp.getLineNumber(), IMarker.SEVERITY_ERROR);
					}
				}
			}
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new ControllerXmlVisitor());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new SampleDeltaVisitor());
	}
}
