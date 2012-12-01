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
 * $Id: ExplorerView.java,v 1.2 2008/01/18 12:31:23 hessellund Exp $
 */
package org.ofbiz.plugin;

import java.util.Comparator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.ofbiz.plugin.dnd.DragSource;
import org.ofbiz.plugin.dnd.OfbizTransfer;
import org.ofbiz.plugin.model.CurrentFileHelper;
import org.ofbiz.plugin.ofbiz.AbstractEvent;
import org.ofbiz.plugin.ofbiz.AbstractViewMap;
import org.ofbiz.plugin.ofbiz.ClasspathEntry;
import org.ofbiz.plugin.ofbiz.EntityEngine;
import org.ofbiz.plugin.ofbiz.EntityFile;
import org.ofbiz.plugin.ofbiz.HasUrl;
import org.ofbiz.plugin.ofbiz.HasXmlDefinition;
import org.ofbiz.plugin.ofbiz.IEntity;
import org.ofbiz.plugin.ofbiz.JavaFiles;
import org.ofbiz.plugin.ofbiz.NamedElement;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.Root;
import org.ofbiz.plugin.ofbiz.ScreenFile;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.ofbiz.ServiceEvent;
import org.ofbiz.plugin.ofbiz.ServiceFile;
import org.ofbiz.plugin.ofbiz.WebApp;
import org.ofbiz.plugin.ofbiz.provider.OfbizItemProviderAdapterFactory;
import org.ofbiz.plugin.parser.GoToFile;


public class ExplorerView extends ViewPart {
	private FilteredTree filteredTree;
	private ComposedAdapterFactory adapterFactory;
	private PropertySheetPage propertySheetPage;
	private IAction analyzeAllAction;
	private IAction filterAction;
	private ViewerFilter filter;
	private boolean filterOn;
	private IAction refreshAction;
	private Action doubleClickAction;
	private Action analyzeSourceAction;
	private Root root;


	class Myfilter extends PatternFilter {
		private String pattern;

		@Override
		public boolean isElementVisible(Viewer viewer, Object element) {
			//i want to show every children object of a visible element
			//try to figure out that the element's parent is visible and show up if it's visible
			boolean isElementVisible = isParentObjectMatch(viewer, element);

			//fall back to default behavior
			return isElementVisible || super.isElementVisible(viewer, element);			
		}

		private boolean isParentObjectMatch(Viewer viewer, Object element) {
			EObject eObject = (EObject) element;
			do {
				eObject = eObject.eContainer();
				if (isLeafMatch(viewer, eObject)) {
					return true;
				}
			} while (eObject != null);
			return false;
		}

		@Override
		protected boolean isLeafMatch(Viewer viewer, Object element){
			if (element instanceof HasUrl) {
				HasUrl hasUrl = (HasUrl) element;
				if (hasUrl.getUrl() != null && hasUrl.getUrl().startsWith(pattern)) {
					return true;
				}
			}
			return super.isLeafMatch(viewer, element);
		}

		@Override
		public void setPattern(String patternString) {
			this.pattern = patternString;
			super.setPattern(patternString);
		}


	}

	public void createPartControl(Composite parent) {

		Myfilter pFilter = new Myfilter();

		filteredTree = new FilteredTree(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL, pFilter, true);

		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new OfbizItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
		TreeViewer viewer = filteredTree.getViewer();
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent arg0) {
				ISelection selection = arg0.getSelection();
				if (selection instanceof TreeSelection) {
					TreeSelection treeSelection =(TreeSelection) selection;					
					Object source = treeSelection.getFirstElement();;
					if (source instanceof Service) {
						GoToFile.gotoFile((Service) source);
					} else if (source instanceof HasXmlDefinition) {
						GoToFile.gotoFile((HasXmlDefinition) source);
					} else if (source instanceof IEntity) {
						GoToFile.gotoFile((IEntity) source);
					}
				}
			}
		});
		viewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
		viewer.setLabelProvider(new ILabelProvider() {

			@Override
			public void removeListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void dispose() {
				// TODO Auto-generated method stub

			}

			@Override
			public void addListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub

			}

			@Override
			public String getText(Object element) {
				if (element instanceof NamedElement) {
					return ((NamedElement) element).getName();
				}
				return "TODO";
			}

			@Override
			public Image getImage(Object element) {
				if (element == null) {
					return null;
				}
				String className = element.getClass().getSimpleName();
				return Plugin.create("icons/full/obj16/" + (className.substring(0, className.length()-"Impl".length())) + ".gif").createImage();
			}
		});

		root = OfbizFactory.eINSTANCE.createRoot();

		viewer.setInput( root );

		viewer.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				getPropertySheetPage().selectionChanged(ExplorerView.this, event.getSelection());
			}
		});

		
		viewer.setComparator(new ViewerComparator(new Comparator<Object>() {
			
			@Override
			public int compare(Object o1, Object o2) {
				if (o1 instanceof NamedElement) {
					if (o2 instanceof NamedElement) {
						NamedElement files = (NamedElement) o1;
						return files.getName().compareTo(((NamedElement)o2).getName());
					} else {
						return 1;
					}
				} else {
					return o1.toString().compareTo(o2.toString());
				}
			}
		}) {

			@Override
			public int category(Object o1) {
				if (o1 instanceof JavaFiles) {
					return 1;
				} else if (o1 instanceof WebApp) {
					return 2;
				} else if (o1 instanceof EntityEngine) {
					return 2;
				} else if (o1 instanceof ClasspathEntry) {
					return 3;
				} else if (o1 instanceof ServiceFile) {
					return 4;
				} else if (o1 instanceof EntityFile) {
					return 5;
				} else if (o1 instanceof ScreenFile) {
					return 6;
				} else if (o1 instanceof AbstractViewMap) {
					return 2;
				} else if (o1 instanceof AbstractEvent) {
					return 1;
				} else if (o1 instanceof NamedElement) {
					return 7;
				} else {
					return 8;
				}
			}}
		);

		int ops = DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { OfbizTransfer.getInstance()};
		viewer.addDragSupport(ops, transfers, new DragSource(viewer));

		filter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement,Object element) {
				if (element instanceof Service) {
					return ((Service) element).getEngine().equals("java");
				}
				if (element instanceof IEntity) {
					return false;
				}
				return true;
			}
		};

		hookContextMenu();
		makeActions();
		hookDoubleClickAction();
		contributeToActionBars();

	}

	public Root getRoot() {
		return this.root;
	}

	public TreeViewer getViewer() {
		TreeViewer viewer = filteredTree.getViewer();
		return viewer;
	}

	public IPropertySheetPage getPropertySheetPage() {
		if (propertySheetPage == null) {
			propertySheetPage = new PropertySheetPage(){
				@Override public void selectionChanged(IWorkbenchPart part, ISelection selection) {
					super.selectionChanged(part, selection);
				}
			};
			propertySheetPage.setPropertySourceProvider(new AdapterFactoryContentProvider(adapterFactory));
		}
		return propertySheetPage;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class key) {
		if (key.equals(IPropertySheetPage.class)) {
			return getPropertySheetPage();
		} else {
			return super.getAdapter(key);
		}
	}

	public void setFocus() {
		TreeViewer viewer = filteredTree.getViewer();
		viewer.getControl().setFocus();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ExplorerView.this.fillContextMenu(manager);
			}
		});
		TreeViewer viewer = filteredTree.getViewer();
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshAction);
		//		manager.add(filterAction);
		//		manager.add(analyzeAllAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		//		manager.add(filterAction);
		//		manager.add(analyzeAllAction);
	}

	private void makeActions() {
		//
		refreshAction = new RefreshAction(this);
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refresh");
		refreshAction.setImageDescriptor(Plugin.create("icons/refresh.gif"));

		//
		filterAction = new Action() {
			public void run() {
				TreeViewer viewer = filteredTree.getViewer();
				if(filterOn) {
					viewer.removeFilter(filter);
					viewer.refresh();
				} else {
					viewer.addFilter(filter);
					viewer.refresh();
				}
				filterOn = !filterOn;
				root.getProjects().clear();
				//				ResourceSet resSet = new ResourceSetImpl();
				// Create a resource
				//				Resource resource = resSet.createResource(URI
				//						.createURI("ofbizContent/ofbizContent.ofbiz"));
				//				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				//					Project ofbizProject = OfbizModelSingleton.get().findProjectByEclipseProjectName(project.getName());
				//					if (ofbizProject != null) {
				//						resource.getContents().add(ofbizProject);
				//					}
				//				}
				//				try {
				//					resource.save(Collections.EMPTY_MAP);
				//				} catch (IOException e) {
				//					// TODO Auto-generated catch block
				//					e.printStackTrace();
				//				}
			}
		};
		filterAction.setText("Filter");
		filterAction.setToolTipText("Toggle filter for java-based services");
		filterAction.setImageDescriptor(Plugin.create("icons/filter.gif"));

		//
		//		analyzeAllAction = new Action() {
		//			public void run() {
		////				ResourceSet resSet = new ResourceSetImpl();
		////				// Get the resource
		////				try {
		////					Resource resource = resSet.getResource(URI
		////							.createURI("ofbizContent/ofbizContent.ofbiz"), true);
		////					for (EObject object : resource.getContents()) {
		////						if (object instanceof Project) {
		////							Project project = (Project) object;
		////							OfbizModelSingleton.get().addProject(project.getProject().getName(), project);
		////							
		////						}
		////					}
		////				} catch (Exception ex) {
		////					ex.printStackTrace();
		////				}}
		//		};
		//		analyzeAllAction.setText("Analyze all");
		//		analyzeAllAction.setToolTipText("Analyze all java-based serviceimplementations");
		//		analyzeAllAction.setImageDescriptor(Plugin.create("icons/analyzeall.gif"));

		// 
		doubleClickAction = new Action() {
			public void run() {
				TreeViewer viewer = filteredTree.getViewer();
				Object selObj = viewer.getSelection();
				if (selObj instanceof TreeSelection) {
					TreeSelection treeSelObj = (TreeSelection) selObj;
					Object selection = treeSelObj.getFirstElement();
				}
			}
		};
		IWorkbenchWindow activeWorkbenchWindow = Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		ISelectionListener listener = new ISelectionListener() {

			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if (selection instanceof TextSelection) {
					TextSelection textSelection = (TextSelection) selection;
					textSelection.getText();
					ISelection selection2 = ExplorerView.this.filteredTree.getViewer().getSelection();
					if (selection2 instanceof TreeSelection) {
						TreeSelection treeSelection = (TreeSelection) selection2;
						//						TreeSelection selection3 = new TreeSelection(new TreePath(new Object[] {}));
						//						filteredTree.getViewer().setSelection(selection3);
						CurrentFileHelper.getCurrentElement();
					}
					System.out.println();
				}
			}
		};
		activeWorkbenchWindow.getSelectionService().addSelectionListener(listener);
		activeWorkbenchWindow.getSelectionService().addPostSelectionListener(listener);
	}

	private void hookDoubleClickAction() {
		TreeViewer viewer = filteredTree.getViewer();
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

}