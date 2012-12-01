package org.ofbiz.plugin.handlers;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.parser.AntParser;

public class CompileComponent implements IObjectActionDelegate {

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
		Component component = (Component) treeSelect;
		IResource findMember = component.getFolder().findMember("build.xml");
		IFile iFile = (IFile) findMember;
////        ProjectHelper.configureProject(project, iFile.getLocation().toFile());
////        project.setProperty("JAVA_HOME", System.getenv("JAVA_HOME"));
//		AntRunner antRunner = new AntRunner();
//		antRunner.setBuildFileLocation(iFile.getLocation().toFile().toString());
////		antRunner.setExecutionTargets(new String [] {"jar"});
//		try {
//			ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(new Shell());
//			antRunner.setArguments("-Dmessage=Building -verbose");
//			antRunner.run(progressMonitorDialog.getProgressMonitor());
////			antRunner.addBuildLogger("org.ofbiz.plugin.console.AntBuildLogger");
////			antRunner.run();
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		try {
			new AntParser(iFile.getLocation().toFile().toString());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
