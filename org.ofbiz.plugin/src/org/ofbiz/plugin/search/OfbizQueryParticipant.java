package org.ofbiz.plugin.search;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.core.internal.resources.Marker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.ui.search.ElementQuerySpecification;
import org.eclipse.jdt.ui.search.IMatchPresentation;
import org.eclipse.jdt.ui.search.IQueryParticipant;
import org.eclipse.jdt.ui.search.ISearchRequestor;
import org.eclipse.jdt.ui.search.QuerySpecification;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.ofbiz.plugin.model.ServiceHelper;
import org.ofbiz.plugin.ofbiz.Referencable;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.ofbiz.ServiceEvent;
import org.ofbiz.plugin.parser.GoToFile;

public class OfbizQueryParticipant implements IQueryParticipant {

	@Override
	public void search(ISearchRequestor requestor,
			QuerySpecification querySpecification, IProgressMonitor monitor)
			throws CoreException {
		if (querySpecification instanceof ElementQuerySpecification) {
			ElementQuerySpecification elementQuerySpecification = (ElementQuerySpecification) querySpecification;
			String projectName = elementQuerySpecification.getElement().getJavaProject().getProject().getName();
			IJavaElement element = elementQuerySpecification.getElement();
			if (element instanceof SourceMethod) {
				SourceMethod sourceMethod = (SourceMethod) element;
				ILocalVariable[] parameters = sourceMethod.getParameters();
				if (parameters.length == 2) {
					//TODO change the parameter and return type to proper lookup
					//TODO extract the service lookup to allow reuse
					if ((parameters[0].getSource().startsWith("DispatchContext") &&
					parameters[1].getSource().startsWith("Map") || parameters[0].getSource().startsWith("HttpServletRequest") &&
					parameters[1].getSource().startsWith("HttpServletResponse")) &&
					(sourceMethod.getReturnType().equals("QMap;") || sourceMethod.getReturnType().equals("QString;"))) {
						Service serviceByJavaMethodName = ServiceHelper.getServiceByJavaMethodName(sourceMethod.getElementName(), projectName);
						if (serviceByJavaMethodName != null) {
							IMarker marker = GoToFile.getMarker(serviceByJavaMethodName);
							IFile file = serviceByJavaMethodName.getFile();
							InputStream contents = file.getContents();
							TextFileDocumentProvider textFileDocumentProvider = new TextFileDocumentProvider(null);
							textFileDocumentProvider.connect(file);
							IDocument document = textFileDocumentProvider.getDocument(file);
							Integer lineNumber = (Integer)marker.getAttribute(IMarker.LINE_NUMBER);
							try {
								int offset = document.getLineOffset(lineNumber -1);
								requestor.reportMatch(new Match(file, offset, -1));
								for (Referencable references : serviceByJavaMethodName.getReference().getReferences()) {
									IFile file2 = references.getFile();
									textFileDocumentProvider.connect(file2);
									IDocument document2 = textFileDocumentProvider.getDocument(file2);
									IMarker marker2 = GoToFile.getMarker(references);
									Integer lineNumber2 = (Integer)marker2.getAttribute(IMarker.LINE_NUMBER) -1;
									int offset2 = document2.getLineOffset(lineNumber2);
									Match match = new Match(file2, offset2, -1);
									requestor.reportMatch(match);
								}
							} catch (Exception e) {
								return;
							}
						}
					}
				}
			}
		}
//		requestor.reportMatch(new FileMatch());
		
	}

	@Override
	public int estimateTicks(QuerySpecification specification) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IMatchPresentation getUIParticipant() {
		// TODO Auto-generated method stub
		return null;
	}

}
