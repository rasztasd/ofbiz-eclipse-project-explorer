package bsh;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.ofbiz.plugin.Plugin;

public class BshBuilder {
	private IFile file;

	public BshBuilder(IFile file) throws CoreException {
		file.deleteMarkers("org.ofbiz.plugin.xmlProblem", true, IResource.DEPTH_INFINITE);
		this.file = file;
		int errorLineNumber = 0;
		int beginChar = 0;
		int endChar = 0;
		TextFileDocumentProvider documentProvider = new TextFileDocumentProvider();
		documentProvider.connect(file);
		IDocument document = documentProvider.getDocument(file);
		Parser parser = new Parser(new StringReader(document.get().replaceAll("\t", " ")));
		SimpleNode currNode = null;
		try {
			Stack<SimpleNode> parseNextNodeStack = new Stack<SimpleNode>();
			while (!parser.Line()) {
				
				SimpleNode simpleNode = parser.popNode();
				currNode = simpleNode;
				errorLineNumber = simpleNode.firstToken.beginLine;
				beginChar = simpleNode.firstToken.beginColumn;
				endChar = simpleNode.lastToken.endColumn;
				parseNextNodeStack.add(simpleNode);
				while (parseNextNodeStack.size() > 0) {
					SimpleNode parseNextNode = parseNextNodeStack.pop();
					currNode = parseNextNode;
					errorLineNumber = parseNextNode.firstToken.beginLine;
					beginChar = parseNextNode.firstToken.beginColumn;
					endChar = parseNextNode.lastToken.endColumn;
					if (parseNextNode.children != null && parseNextNode.children.length > 0) {
						parseNextNodeStack.addAll((List<? extends SimpleNode>) Arrays.asList(parseNextNode.children));
					}	
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
			try {
				int lineOffset = document.getLineOffset(errorLineNumber);
				IMarker marker = file.createMarker("org.ofbiz.plugin.xmlProblem");			
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				marker.setAttribute(IMarker.LINE_NUMBER, errorLineNumber);
				marker.setAttribute(IMarker.CHAR_START, lineOffset + beginChar - 1);
				marker.setAttribute(IMarker.CHAR_END, lineOffset + endChar - 1);
				marker.setAttribute(IMarker.MESSAGE, "Parse error");
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	
	
}
