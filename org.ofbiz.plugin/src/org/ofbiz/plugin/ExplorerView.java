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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.ofbiz.plugin.analysis.Analysis;
import org.ofbiz.plugin.model.OfbizModelSingleton;
import org.ofbiz.plugin.ofbiz.Attribute;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Directory;
import org.ofbiz.plugin.ofbiz.Entity;
import org.ofbiz.plugin.ofbiz.ExtendEntity;
import org.ofbiz.plugin.ofbiz.HasUrl;
import org.ofbiz.plugin.ofbiz.HasXmlDefinition;
import org.ofbiz.plugin.ofbiz.IEntity;
import org.ofbiz.plugin.ofbiz.OfbizFactory;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.Root;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.ofbiz.ServiceMode;
import org.ofbiz.plugin.ofbiz.ViewEntity;
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
		viewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
		
		root = OfbizFactory.eINSTANCE.createRoot();

		viewer.setInput( root );
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				getPropertySheetPage().selectionChanged(ExplorerView.this, event.getSelection());
			}
		});
		
		viewer.setSorter(new ViewerSorter(){
			@Override
			public int category(Object element) {
				if (element instanceof Service)
					return 1;
				else if (element instanceof Entity)
					return 2;
				else if (element instanceof ExtendEntity)
					return 3;
				else if (element instanceof ViewEntity)
					return 4;
				else if (element instanceof Attribute) {
					Attribute attr = (Attribute) element;
					switch(attr.getMode().getValue()){
						case ServiceMode.IN_VALUE: return 5;
						case ServiceMode.INOUT_VALUE: return 6;
						case ServiceMode.OUT_VALUE: return 7;
						default: return super.category(element);
					}
				} else 
					return super.category(element);
			}
		});
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
		manager.add(filterAction);
		manager.add(analyzeAllAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		manager.add(filterAction);
		manager.add(analyzeAllAction);
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
//				TreeViewer viewer = filteredTree.getViewer();
//				if(filterOn) {
//					viewer.removeFilter(filter);
//					viewer.refresh();
//				} else {
//					viewer.addFilter(filter);
//					viewer.refresh();
//				}
//				filterOn = !filterOn;
				root.getProjects().clear();
				ResourceSet resSet = new ResourceSetImpl();
				// Create a resource
				Resource resource = resSet.createResource(URI
						.createURI("ofbizContent/ofbizContent.ofbiz"));
				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					Project ofbizProject = OfbizModelSingleton.get().findProjectByEclipseProjectName(project.getName());
					if (ofbizProject != null) {
						resource.getContents().add(ofbizProject);
					}
				}
				try {
					resource.save(Collections.EMPTY_MAP);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		filterAction.setText("Filter");
		filterAction.setToolTipText("Toggle filter for java-based services");
		filterAction.setImageDescriptor(Plugin.create("icons/filter.gif"));
		
		//
		analyzeAllAction = new Action() {
			public void run() {
				ResourceSet resSet = new ResourceSetImpl();
				// Get the resource
				try {
					Resource resource = resSet.getResource(URI
							.createURI("ofbizContent/ofbizContent.ofbiz"), true);
					for (EObject object : resource.getContents()) {
						if (object instanceof Project) {
							Project project = (Project) object;
							OfbizModelSingleton.get().addProject(project.getProject().getName(), project);
							
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}}
		};
		analyzeAllAction.setText("Analyze all");
		analyzeAllAction.setToolTipText("Analyze all java-based serviceimplementations");
		analyzeAllAction.setImageDescriptor(Plugin.create("icons/analyzeall.gif"));
		
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