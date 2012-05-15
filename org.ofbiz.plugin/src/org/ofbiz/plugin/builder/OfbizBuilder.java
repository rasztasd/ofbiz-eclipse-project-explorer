package org.ofbiz.plugin.builder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.ofbiz.plugin.LoadOperation;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.model.ControllerHelper;
import org.ofbiz.plugin.model.OfbizModelSingleton;
import org.ofbiz.plugin.model.ScreenHelper;
import org.ofbiz.plugin.model.ServiceHelper;
import org.ofbiz.plugin.nature.OfbizNature;
import org.ofbiz.plugin.ofbiz.Controller;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.Screen;
import org.ofbiz.plugin.ofbiz.WebApp;
import org.ofbiz.plugin.parser.Parser;
import org.ofbiz.plugin.parser.WebappParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import bsh.BshBuilder;

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
				IFile file = (IFile) resource;

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
				//				case IResourceDelta.ADDED:
				//					// handle added resource
				//					checkControllerXml(resource);
				//					break;
				//				case IResourceDelta.REMOVED:
				//					// handle removed resource
				//					checkControllerXml(resource);
				//					break;
				case IResourceDelta.SYNC:
				case IResourceDelta.CHANGED:
//					for (IMarker marker : file.findMarkers("org.ofbiz.plugin.text", true, IResource.DEPTH_INFINITE)) {
//						if (marker.getType().equals("org.ofbiz.plugin.controllerMarker")) {
//							WebappParser parser = new WebappParser(component, uri, file, webApp, referencingController)
//							xpp = Plugin.getDefault().getXmlPullParserPool().getPullParserFromPool();
//							Controller referencingController = null;
//							if (!controller.equals(webapp.getController())) {
//								referencingController = webapp.getController();
//							}
//							WebappParser webAppParser = new WebappParser(webapp.getComponent(), webapp.getUri(), file, webapp, referencingController);
//							webAppParser.processDocument(xpp, file);
//						}
//					}
//					if (resource.getName().endsWith(".bsh")) {
//						new BshBuilder(file);
//					} else {
//						EObject eObject = OfbizModelSingleton.get().getEObject(file);
//						if (eObject instanceof Controller) {
//							Controller controller = (Controller) eObject;
//							WebApp webapp = controller.getWebapp();
//							controller.setWebapp(null);
//							XmlPullParser xpp;
//							try {
//								xpp = Plugin.getDefault().getXmlPullParserPool().getPullParserFromPool();
//								Controller referencingController = null;
//								if (!controller.equals(webapp.getController())) {
//									referencingController = webapp.getController();
//								}
//								WebappParser webAppParser = new WebappParser(webapp.getComponent(), webapp.getUri(), file, webapp, referencingController);
//								webAppParser.processDocument(xpp, file);
//							} catch (XmlPullParserException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							} catch (IOException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//					}
//					
//					// handle changed resource
//					//					checkControllerXml(resource);
//					break;
				}
				//				case IResourceDelta.:
			}
			//return true to continue visiting children.
			return true;
		}
	}

	class ControllerXmlVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
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
//			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
//				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
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

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		//		try {
//		IProject project = getProject();
//		Project ofbizProject = OfbizModelSingleton.get().findProjectByEclipseProjectName(project.getName());
//		if (ofbizProject == null) {
//			ofbizProject = OfbizFactory.eINSTANCE.createProject();
//			OfbizModelSingleton.get().addProject(project.getName(), ofbizProject);
//		}
//
//		try {
//			LoadOperation loadOperation = new LoadOperation(project);
//			loadOperation.run(monitor);
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//			project.accept(new ControllerXmlVisitor());
		//		} catch (CoreException e) {
		//		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new SampleDeltaVisitor());
	}
}
