package org.ofbiz.plugin.debugger;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.LineBreakpoint;

public class PDALineBreakpoint extends LineBreakpoint {

	public PDALineBreakpoint(final IResource resource, final int lineNumber)
			throws CoreException {
//		IMarker marker = resource.createMarker(
//				"org.ofbiz.plugin.breakpointMarker");
//		setMarker(marker);
//		setEnabled(true);
	}

	@Override
	public String getModelIdentifier() {
		return "org.ofbiz.plugin";
//		return null;
	}

}
