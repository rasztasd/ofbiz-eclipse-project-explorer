package org.ofbiz.plugin.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.ofbiz.plugin.model.EntityHelper;
import org.ofbiz.plugin.model.OfbizModelSingleton;
import org.ofbiz.plugin.model.ServiceHelper;
import org.ofbiz.plugin.ofbiz.NamedElement;
import org.ofbiz.plugin.ofbiz.Service;

public class JavaCompletion implements IJavaCompletionProposalComputer {

	@Override
	public void sessionStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		List<ICompletionProposal> retValue = new ArrayList<ICompletionProposal>();
//		try {
//			int offset = context.getInvocationOffset();
//			int aposOffset = offset;
//			char previousChar = context.getDocument().getChar(offset - 1);
//			StringBuilder sb = new StringBuilder();
//			for (int i = 0; i < 100; i++) {
//				previousChar = context.getDocument().getChar(offset - i - 1);
//				if (previousChar == '"') {
//					aposOffset = offset - i;
//					break;
//				} else {
//					sb.append(previousChar);
//				}
//			}
//			sb.reverse();
//			SortedSet<Service> services = ServiceHelper.getSortedServices();
//			List<NamedElement> elements = new ArrayList<NamedElement>();
//			elements.addAll(EntityHelper.getIEntities());
//			elements.addAll(services);
//			for (NamedElement service : elements) {
//				if (service == null) {
//					continue;
//				}
//				String name = service.getName();
//				if (name.startsWith(sb.toString())) {
//					retValue.add(new CompletionProposal(name, aposOffset, sb.length(), name.length()));
//					monitor.worked(1);
//				}
//			}
//		} catch (BadLocationException e) {
//
//		}
		return retValue;
	}

	@Override
	public List<IContextInformation> computeContextInformation(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sessionEnded() {
		// TODO Auto-generated method stub

	}

}
