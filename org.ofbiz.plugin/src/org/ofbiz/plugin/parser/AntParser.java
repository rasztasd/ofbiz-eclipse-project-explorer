package org.ofbiz.plugin.parser;

import org.eclipse.ant.internal.launching.launchConfigurations.AntProcess;
import org.eclipse.ant.launching.IAntLaunchConstants;
import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;


public class AntParser {

	public AntParser(String buildXmlFilepath) throws CoreException {

		// show the console
		final IWorkbenchPage activePage = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow()
				.getActivePage();
		activePage.showView(IConsoleConstants.ID_CONSOLE_VIEW);

		// let launch manager handle ant script so output is directed to Console view
		final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IAntLaunchConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE);
		final ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, "Compile");
		workingCopy.setAttribute(ILaunchManager.ATTR_PRIVATE, true);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION, buildXmlFilepath);
		final ILaunch launch = workingCopy.launch(ILaunchManager.RUN_MODE, null);
		// make sure the build doesnt fail
		final boolean[] buildSucceeded = new boolean[] { true };
		((AntProcess) launch.getProcesses()[0]).getStreamsProxy()
		.getErrorStreamMonitor()
		.addListener(new IStreamListener() {
			@Override
			public void streamAppended(String text, IStreamMonitor monitor) {
				if (text.indexOf("BUILD FAILED") > -1) {
					buildSucceeded[0] = false;
				}
			}
		});
		// wait for the launch (ant build) to complete
		manager.addLaunchListener(new ILaunchesListener2() {
			public void launchesTerminated(ILaunch[] launches) {
				boolean patchSuccess = false;
				try {
					if (!buildSucceeded[0]) {
						throw new Exception("Build FAILED!");
					}
					for (int i = 0; i < launches.length; i++) {
						if (launches[i].equals(launch)
								&& buildSucceeded[0]
										&& !((IProgressMonitor) launches[i].getProcesses()[0]).isCanceled()) {
									break;
						}
					}
				} catch (Exception e) {
				} finally {
					// get rid of this listener
					manager.removeLaunchListener(this);
				}
			}

			public void launchesAdded(ILaunch[] launches) {
			}

			public void launchesChanged(ILaunch[] launches) {
			}

			public void launchesRemoved(ILaunch[] launches) {
			}
		});
	}
}
