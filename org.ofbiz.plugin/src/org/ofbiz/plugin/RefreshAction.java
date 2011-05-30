package org.ofbiz.plugin;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Directory;
import org.ofbiz.plugin.ofbiz.Entity;
import org.ofbiz.plugin.ofbiz.IEntity;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.Service;


public class RefreshAction extends Action {
	private final ExplorerView view;
	public RefreshAction(ExplorerView view) {
		this.view = view;
	}
	public void run() {
		assert view != null;
		boolean restore = storeSelection();
		// perform load
		try {
			LoadOperation refresh = new LoadOperation(view);
			new ProgressMonitorDialog(view.getSite().getShell()).run(true, true, refresh);
		} catch (InvocationTargetException e) {
			Plugin.logError("Error occurred during refresh", e);
		} catch (InterruptedException e) {
			// ignore cancel
		}
		// attempt to restore viewer selection
		if (restore) restoreSelection();
	}
	private String path;
	private boolean storeSelection() {
		// store viewer selection
		if (view.getViewer().getSelection() instanceof TreeSelection) {
			TreeSelection treeSelObj =
				(TreeSelection) view.getViewer().getSelection();
			Object obj = treeSelObj.getFirstElement();
			if ( obj instanceof Project) {
				path = "/"+((Project)obj).getName();
				return true;
			}
			if ( obj instanceof Directory) {
				Directory directory = (Directory) obj;
				path = directory.getFolder().getFullPath()+"";
				return true;
			}
			if ( obj instanceof Component) {
				Component component = (Component) obj;
				path = component.getFolder().getFullPath()+"";
				return true;
			}
			if ( obj instanceof Service) {
				Service service = (Service) obj;
				Component component = service.getComponent();
				path = component.getFolder().getFullPath()+"/"+service.getName();
				return true;
			}
			if ( obj instanceof Entity) {
				Entity entity = (Entity) obj;
				Component component = entity.getComponent();
				path = component.getFolder().getFullPath()+"/"+entity.getName();
				return true;
			}
			// TODO: handle implements, attributes and fields
		}
		return false;
	}
	private void restoreSelection() {
		assert view != null;
		assert path != null && path.length()>0;
		String[] segments = path.split("/");
		Object selection = null;
		// find project
		if (segments.length<2) return;
		Project project = null;
		for(Project p : view.getRoot().getProjects()) {
			if (p.getName().equals(segments[1])) {
				project = p;
				selection = p;
			}
		}
		// find directory
		Directory directory = null;
		if (segments.length>2 && project!=null) {
			for(Directory d : project.getDirectories()) {
				if (d.getName().equals(segments[2])) {
					directory = d;
					selection = d;
				}
			}
		}
		// find component
		Component component = null;
		if(segments.length>3 && directory!=null) {
			for(Component c : directory.getComponents()) {
				if (c.getName().equals(segments[3])) {
					component = c;
					selection = c;
				}
			}
		}
		// find entity or service
		if(segments.length>4 && component!=null) {
			for(IEntity e : component.getEntities()) {
				if (e.getName().equals(segments[4])) {
					selection = e;
				}
			}
			for(Service s : component.getServices()) {
				if (s.getName().equals(segments[4])) {
					selection = s;
				}
			}
		}
		// show in view
		if (selection != null) {
			view.getViewer().reveal(selection);
			view.getViewer().setSelection(new StructuredSelection(selection));
			Plugin.debug("restored selection "+selection);
		}
	}
}
