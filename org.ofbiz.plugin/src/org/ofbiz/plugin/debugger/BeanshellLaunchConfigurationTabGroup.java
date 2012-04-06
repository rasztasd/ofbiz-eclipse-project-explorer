package org.ofbiz.plugin.debugger;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;

public class BeanshellLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		CommonTab commonTab = new CommonTab();
		ILaunchConfigurationTab tabs[] = new ILaunchConfigurationTab[] {commonTab, new SourceLookupTab()}; 
		setTabs(tabs);
	}

}
