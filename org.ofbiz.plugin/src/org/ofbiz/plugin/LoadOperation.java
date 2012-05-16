package org.ofbiz.plugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.FeatureMapUtil.BasicValidator;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.ofbiz.plugin.model.ComponentHelper;
import org.ofbiz.plugin.model.OfbizModelSingleton;
import org.ofbiz.plugin.ofbiz.AbstractViewMap;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Controller;
import org.ofbiz.plugin.ofbiz.Directory;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.Root;
import org.ofbiz.plugin.ofbiz.ScreenViewMap;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.ofbiz.ServiceInvocation;
import org.ofbiz.plugin.ofbiz.WebApp;
import org.ofbiz.plugin.parser.ComponentParser;
import org.ofbiz.plugin.parser.DirectoryParser;
import org.ofbiz.plugin.parser.EntityParser;
import org.ofbiz.plugin.parser.FormParser;
import org.ofbiz.plugin.parser.ScreenParser;
import org.ofbiz.plugin.parser.SecaParser;
import org.ofbiz.plugin.parser.ServiceParser;
import org.ofbiz.plugin.parser.WebappParser;
import org.ofbiz.plugin.parser.model.WebappModel;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


public class LoadOperation extends WorkspaceModifyOperation {

	private Map<String, Service> serviceByName;
	private Set<String> alreadyParsedJavaFiles = new HashSet<String>();
	private List<String> screensToParse = new ArrayList<String>();
	private List<IFile> secasToParse = new ArrayList<IFile>();
	private IProject project;

	public LoadOperation(IProject project) {
		this.project = project;
	}

	@Override
	protected void execute(IProgressMonitor monitor)
			throws CoreException, InvocationTargetException, InterruptedException {

		monitor.beginTask("load OFBiz projects:", IProgressMonitor.UNKNOWN);

		monitor.worked(1);

		// load open OFBiz projects
		load(project, monitor);

		monitor.done();

	}		

	private void load(IProject project, IProgressMonitor monitor) {

		monitor.subTask("load project: "+project.getName());
		
		try {
			for (IMarker marker : project.findMarkers("org.ofbiz.plugin.text", true, IResource.DEPTH_INFINITE)) {
				marker.delete();
			}
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// parse project configuration
		
		IResource res = project.findMember(Plugin.BASECONFIG);
		if (res==null || !res.exists() || !(res instanceof IFile)) {
			Plugin.logError("Project configuration does not exist: "+res.getName(), null);
			return;
		}

		// initialize domain object but don't add project to root until the end

		// clear		
		Project ofbizProject = OfbizModelSingleton.get().findProjectByEclipseProjectName(project.getName());
		if (ofbizProject == null) {
			ofbizProject = OfbizFactory.eINSTANCE.createProject();
			OfbizModelSingleton.get().addProject(project.getName(), ofbizProject);
		}
		ofbizProject.getDirectories().clear();
		ofbizProject.setName(project.getName());
		ofbizProject.setJavaproject(JavaCore.create(project));
		ofbizProject.setProject(project);


		DirectoryParser parser = new DirectoryParser(ofbizProject);
		try {
			XmlPullParser xpp =
					Plugin.getDefault().getXmlPullParserPool().getPullParserFromPool();
			parser.processDocument(xpp, (IFile)res);
			Plugin.getDefault().getXmlPullParserPool().returnPullParserToPool(xpp);
		} catch (Exception e) {
			Plugin.logError("Unable to parse configuration for project: "+project.getName(),e);
			return;
		}
		monitor.worked(1);

		// load each component directory

		for(Directory directory : ofbizProject.getDirectories()) {
			load(directory,monitor);
		}

		//load each screen
		Set<String> parsedScreens = new HashSet<String>();
		Set<String> nextFormsToParse = new HashSet<String>();
		for(Directory directory : ofbizProject.getDirectories()) {
			for (Component component : directory.getComponents()) {
				for (WebApp webapp : component.getWebapps()) {
					Controller controller = webapp.getController();
					if (controller == null) {
						continue;
					}
					EList<AbstractViewMap> viewMaps = controller.getViewMaps();
					for (AbstractViewMap viewMap : viewMaps) {
						if (viewMap instanceof ScreenViewMap) {
							ScreenViewMap screenViewMap = (ScreenViewMap) viewMap;
							String screenLocation = screenViewMap.getViewName();
							String screenUrl;
							int indexOf = screenLocation.indexOf("#");
							if (indexOf > 0) {
								screenUrl = screenLocation.substring(0, indexOf);
							} else {
								screenUrl = screenLocation;
							}
							screensToParse.add(screenUrl);
						}
					}
					while (screensToParse.size() > 0) {
						String nextScreenToParse = screensToParse.remove(0);
						if (!parsedScreens.contains(nextScreenToParse)) {
							parsedScreens.add(nextScreenToParse);
							IFile screenFile = null;
							if (nextScreenToParse == null) {
								continue;
							}
							Component componentByUrl = ComponentHelper.getComponentByUrl(ofbizProject, nextScreenToParse);
							if (componentByUrl == null) {
								continue;
							}
							try {
								screenFile = (IFile) componentByUrl.getDirectory().getFolder().findMember(nextScreenToParse.substring("component://".length()));
							} catch (NullPointerException e) {

							}
							if (screenFile != null) {
								ScreenParser screenParser = new ScreenParser(screenFile, nextScreenToParse, ofbizProject);
								monitor.subTask("load screen: " + screenFile.getName());
								XmlPullParser xpp;
								try {
									xpp = Plugin.getDefault().getXmlPullParserPool().getPullParserFromPool();
									screenParser.processDocument(xpp, screenFile);
									nextFormsToParse.addAll(screenParser.getForms());
									screensToParse.addAll(screenParser.getScreens());
								} catch (XmlPullParserException e) {
								} catch (CoreException e) {
								} catch (IOException e) {
								}
								monitor.worked(1);
							}
						}

					}
				}
			}
		}
		for (String nextFormToParse :nextFormsToParse) {
			IFile formFile = null;
			try {
				Component component = ComponentHelper.getComponentByUrl(ofbizProject, nextFormToParse);
				formFile = (IFile) component.getDirectory().getFolder().findMember(nextFormToParse.substring("component://".length()));
			} catch (NullPointerException e) {

			}
			if (formFile != null) {
				monitor.subTask("load form: " + nextFormToParse);
				XmlPullParser xpp;
				try {
					xpp = Plugin.getDefault().getXmlPullParserPool().getPullParserFromPool();
					new FormParser(formFile, nextFormToParse, ofbizProject).processDocument(xpp, formFile);
				} catch (XmlPullParserException e) {
				} catch (CoreException e) {
				} catch (IOException e) {
				}
				monitor.worked(1);
			}

		}

		//Parse SECAs

		for (IFile secaFile : secasToParse) {
			monitor.subTask("load seca: " + secaFile.getName());
			SecaParser secaParser = new SecaParser();
			XmlPullParser xpp;
			try {
				xpp = Plugin.getDefault().getXmlPullParserPool().getPullParserFromPool();
				secaParser.processDocument(xpp, secaFile);
			} catch (XmlPullParserException e) {
			} catch (CoreException e) {
			} catch (IOException e) {
			}
			monitor.worked(1);
		}

		// everything is ok so we can add the loaded project to the root

		//find all service usage
		//		findServiceUsage(ofbizProject);

	}

	private void findServiceUsage(Project ofbizProject) {
		IJavaProject javaProject = ofbizProject.getJavaproject();
		try {
			IJavaSearchScope createWorkspaceScope = SearchEngine.createJavaSearchScope(new IResource[] {javaProject.getResource()});
			SearchEngine searchEngine = new SearchEngine();
			IType findType = javaProject.findType("org.ofbiz.service.LocalDispatcher");
			IMethod[] methods = findType.getMethods();
			for (IMethod method : methods) {
				if (method.getElementName().startsWith("run")) {
					SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
					SearchRequestor requestor = new SearchRequestor() {

						@Override
						public void acceptSearchMatch(final SearchMatch searchMatch) throws CoreException {
							ICompilationUnit createCompilationUnitFrom = JavaCore.createCompilationUnitFrom((IFile) searchMatch.getResource());
							ASTParser parser = ASTParser.newParser(AST.JLS3);
							parser.setKind(ASTParser.K_COMPILATION_UNIT);
							parser.setSource(createCompilationUnitFrom);
							parser.setResolveBindings(true);
							CompilationUnit unit = (CompilationUnit) parser.createAST(null); // parse
							unit.accept(new ASTVisitor() {
								String javaDoc = null;

								@Override
								public boolean visit(Javadoc node) {
									javaDoc = node.tags().toString();
									return super.visit(node);
								}

								@Override
								public boolean visit(MethodInvocation node) {
									IMethodBinding resolveMethodBinding = node.resolveMethodBinding();
									if (resolveMethodBinding == null) {
										return false;
									}
									String methodName = resolveMethodBinding.getName();
									if (methodName.startsWith("run") && methodName.length() > 3) {
										if (node.arguments().size() == 2) {
											Object firstArgObject = node.arguments().get(0);
											if (firstArgObject instanceof StringLiteral) {
												StringLiteral firstArg = (StringLiteral) firstArgObject;
												String serviceName = firstArg.getLiteralValue().replaceAll("\"", "");
												Service serviceByName = getServiceByName(serviceName);
												if (serviceByName != null) {
													ResolvedSourceMethod element = (ResolvedSourceMethod) searchMatch.getElement();
													String alreadyParsedKey = element.getParent().getPath()+serviceName;
													if (!alreadyParsedJavaFiles.contains(alreadyParsedKey)) {
														ServiceInvocation createServiceInvocation = OfbizFactory.eINSTANCE.createServiceInvocation();
														createServiceInvocation.setService(serviceByName);
														createServiceInvocation.setName(element.getParent().getPath()+ "." + element.getElementName());
														alreadyParsedJavaFiles.add(alreadyParsedKey);
													}
												}
											}
										}
									}
									return super.visit(node);
								}

							});
						}
					};
					SearchParticipant searchParticipiant[] = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
					searchEngine.search(pattern, searchParticipiant, createWorkspaceScope, requestor, null);
				}
			}
		} catch (Exception e) {
			Plugin.logError("Ex", e);
		}
	}

	private Service getServiceByName(String name) {
//		if (serviceByName == null) {
//			serviceByName = new HashMap<String, Service>();
//			for (Project project : root.getProjects()) {
//				for (Directory directory : project.getDirectories()) {
//					for (Component component : directory.getComponents()) {
//						for (Service service : component.getServices()) {
//							serviceByName.put(service.getName(), service);
//						}
//					}
//				}
//			}
//		}
//		return serviceByName.get(name);
		return null;
	}

	private void load(Directory directory,IProgressMonitor monitor) {

		monitor.subTask("load directory: "+directory.getName());

		// load static directory or hot-deploy'ed directory

		if (directory.getName().equals("hot-deploy")) {
			loadDynamicDirectory(directory);
		} else {
			loadStaticDirectory(directory);
		}
		monitor.worked(1);

		// load each component

		for(Component component : directory.getComponents()) {
			load(component,monitor);
		}


	}

	private void loadDynamicDirectory(final Directory directory) {

		// add a component for each folder

		final IFolder folder = directory.getFolder();
		try {
			folder.accept(new IResourceVisitor(){
				@Override
				public boolean visit(IResource resource) throws CoreException {
					if (resource instanceof IFolder && resource!=folder) {
						IFolder componentFolder = (IFolder) resource;
						Component component = OfbizFactory.eINSTANCE.createComponent();
						component.setName(componentFolder.getName());
						component.setFolder(componentFolder);
						component.setDirectory(directory);
					}
					return true;
				}
			}, IResource.DEPTH_ONE, false);
		} catch (CoreException e) {
			Plugin.logError("Unable to load hot-deploy directory", e);
		}
	}

	private void loadStaticDirectory(Directory directory) {

		// parse directory configuration

		DirectoryParser parser = new DirectoryParser(directory.getProject());
		parser.setDirectory(directory);
		IResource cfg = directory.getFolder().findMember("component-load.xml");
		if(cfg==null || !cfg.exists() || !(cfg instanceof IFile)) {
			Plugin.logError(
					"Unable to locate directory configuration file: "
							+directory.getName(), null);
			return;
		}

		try {
			XmlPullParser xpp =
					Plugin.getDefault().getXmlPullParserPool().getPullParserFromPool();
			parser.processDocument(xpp, (IFile)cfg);
			Plugin.getDefault().getXmlPullParserPool().returnPullParserToPool(xpp);
		} catch (Exception e) {
			Plugin.logError("Unable to parse directory configuration: "+directory.getName(), e);
		}
	}

	private void load(Component component,IProgressMonitor monitor) {

		monitor.subTask("load component: "+component.getName());

		// parse component configuration

		ComponentParser parser = new ComponentParser(component);
		BasicValidator basicValidator = new BasicValidator(component.eClass(), component.eContainingFeature());
		IResource cfg = component.getFolder().findMember("ofbiz-component.xml");
		component.setFile((IFile) cfg);
		if(cfg==null || !cfg.exists() || !(cfg instanceof IFile)) {
			Plugin.logError(
					"Unable to locate component configuration file: "
							+component.getName(), null);
			return;
		}

		try {
			XmlPullParser xpp =
					Plugin.getDefault().getXmlPullParserPool().getPullParserFromPool();
			parser.processDocument(xpp, (IFile) cfg);
			Plugin.getDefault().getXmlPullParserPool().returnPullParserToPool(xpp);
			parser.addMarker();
		} catch (Exception e) {
			Plugin.logError("Unable to parse component configuration: "+ component.getName(), e);
			return;
		}
		monitor.worked(1);

		// load entity models

		for (final IFile file : parser.getEntityModels()) {
			monitor.subTask("load entity model: "+file.getName());
			loadEntityModel(component, file);
			monitor.worked(1);
		}

		// load service models

		for (final IFile file : parser.getServiceModels()) {
			monitor.subTask("load service model: "+file.getName());
			loadServiceModel(component, file);
			monitor.worked(1);
		}
		secasToParse.addAll(parser.getSecaModels());

		List<WebappModel> listOfWebapps = new ArrayList<WebappModel>();
		// load webapp models
		listOfWebapps.addAll(parser.getWebappModels());
		while (listOfWebapps.size() > 0) {
			WebappModel webappModel = listOfWebapps.remove(0);
			IFile file = webappModel.getiFile();
			monitor.subTask("load webapp model: "+file.getName());
			loadWebappModel(component, file, webappModel.getUri(), listOfWebapps, webappModel.getReferencingController());
			monitor.worked(1);
		}
	}

	private void loadEntityModel(Component component, IFile file) {
		try {
			EntityParser parser = new EntityParser(component, file);
			XmlPullParser xpp = Plugin.getDefault()
					.getXmlPullParserPool().getPullParserFromPool();
			parser.processDocument(xpp, file);
			Plugin.getDefault().getXmlPullParserPool().returnPullParserToPool(xpp);
		} catch (Exception e) {
			Plugin.logError("Unable to parse entitymodel: "+ file.getName(), e);
		}
	}

	private void loadServiceModel(Component component, IFile file) {
		try {
			ServiceParser parser = new ServiceParser(component, file);
			XmlPullParser xpp = Plugin.getDefault()
					.getXmlPullParserPool().getPullParserFromPool();
			parser.processDocument(xpp, file);
			Plugin.getDefault().getXmlPullParserPool().returnPullParserToPool(xpp);
		} catch (Exception e) {
			Plugin.logError("Unable to parse servicemodel: "+ file.getName(), e);
		}
	}


	private void loadWebappModel(Component component, IFile file, String uri, List<WebappModel> listOfWebapps, Controller referencingController) {
		try {
			WebApp webApp;
			if (referencingController == null) {
				webApp = OfbizFactory.eINSTANCE.createWebApp();
				webApp.setUri(uri);
				webApp.setName(uri);
				webApp.setComponent(component);
			} else {
				webApp = referencingController.getWebapp();
			}
			WebappParser parser = new WebappParser(component, uri, file, webApp, referencingController);
			XmlPullParser xpp = Plugin.getDefault()
					.getXmlPullParserPool().getPullParserFromPool();
			parser.processDocument(xpp, file);
			screensToParse.addAll(parser.getScreenLocations());
			listOfWebapps.addAll(parser.getincludeLocations());
			Plugin.getDefault().getXmlPullParserPool().returnPullParserToPool(xpp);
		} catch (Exception e) {
			Plugin.logError("Unable to parse webapp model: "+ file.getName(), e);
		}
	}


	/** Checks whether a project contains an OFBiz app. Closed projects always return false. */
	private boolean isOfbizProject(IProject project) {
		if (!project.isOpen()) return false;
		IResource config =
				project.findMember(Plugin.BASECONFIG);
		return config != null && config.exists();
	}

}