package org.ofbiz.plugin.debugger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

public class BeanshellDebuggerDelegate extends LaunchConfigurationDelegate implements IDebugEventSetListener {

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		// TODO Auto-generated method stub
		System.out.println();
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
//			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 5);
//			subMonitor.beginTask("Launching beanshell debugger", 1);
//			PythonRunnerConfig config = new PythonRunnerConfig();
//			RemoteDebugger debugger = new RemoteDebugger(config);
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = wsRoot.getProject("asd");
//			try {
//				debugger.startConnect(subMonitor);
//				debugger.waitForConnect(subMonitor);
//				IDebugTarget target = new PyDebugTarget(launch, null, null, debugger, project);
				IDebugTarget target = new BeanshellDebugTarget(launch, project);
				launch.addDebugTarget(target);
		        IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		        breakpointManager.addBreakpointListener(target);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			//			launch.setSourceLocator(new PDASourceLookupDirector());
//			subMonitor.subTask("Done");
		}
	}
}

