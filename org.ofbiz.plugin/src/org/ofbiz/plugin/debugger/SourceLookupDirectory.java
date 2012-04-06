package org.ofbiz.plugin.debugger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

public class SourceLookupDirectory extends AbstractSourceLookupDirector {

	@Override
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] {
			new AbstractSourceLookupParticipant() {
				
				@Override
				public String getSourceName(Object object) throws CoreException {
					StackFrame stackFrame = (StackFrame) object;
					String fileName = stackFrame.getFileName();
//					fileName = "/hot-deploy/emerald_admin/webapp/emerald_admin/WEB-INF/actions/client/programmehome.bsh";
					return fileName;
				}
			}	
		});
	}		
}
