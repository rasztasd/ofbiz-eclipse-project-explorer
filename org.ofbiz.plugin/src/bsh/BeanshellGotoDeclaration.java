package bsh;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.model.JavaHelper;
import org.ofbiz.plugin.model.JavaHelper.GotoJavaMethodAST;
import org.ofbiz.plugin.model.OfbizModelSingleton;
import org.ofbiz.plugin.model.hyperlink.HyperlinkMarker;
import org.ofbiz.plugin.parser.GoToFile;

public class BeanshellGotoDeclaration {
	public static List<HyperlinkMarker> gotoDeclaration(String beanshellCode, final String selectedWord, int currentOffsetInLine, int selectedLineNumber, final IDocument doc) {
		List<HyperlinkMarker> retValue = new ArrayList<HyperlinkMarker>();
		Parser parser = new Parser(new StringReader(beanshellCode.replaceAll("\t", " ")));
		List<String> imports = new ArrayList<String>();
		imports.add("java.lang");
		Map<String, Integer> variableDeclarations = new HashMap<String, Integer>();

		try {
			String variable = null;
			int currentLineNumber = 0;
			/**
			 * We store all the candidates here that are in the selection
			 */
			SimpleNode gotBshElement = null;
			int prevBeginColumn = 0;
			int prevEndColumn = Integer.MAX_VALUE;
			Stack<SimpleNode> parseNextNodeStack = new Stack<SimpleNode>();
			while (variable == null && !parser.Line()) {
				SimpleNode popNode = parser.popNode();
				parseNextNodeStack.add(popNode);
				while (parseNextNodeStack.size() > 0) {
					SimpleNode parseNextNode = parseNextNodeStack.pop();
					currentLineNumber = parseNextNode.getLineNumber() - 1;
					int beginColumn = parseNextNode.firstToken.beginColumn -1 ;
					int endColumn = parseNextNode.lastToken.endColumn + 1;
					if (currentLineNumber == selectedLineNumber) {
						if (beginColumn <= currentOffsetInLine && endColumn >= currentOffsetInLine) {
							if (prevBeginColumn <= beginColumn && prevEndColumn >= endColumn) {
								prevBeginColumn = beginColumn;
								prevEndColumn = endColumn;
								gotBshElement = parseNextNode;
							}
						}
					}
					if (parseNextNode.children != null && parseNextNode.children.length > 0) {
						parseNextNodeStack.addAll((List<? extends SimpleNode>) Arrays.asList(parseNextNode.children));
					}
					if (parseNextNode instanceof BSHVariableDeclarator) {
						String text = ((BSHVariableDeclarator) parseNextNode).firstToken.image;
						Integer integer = variableDeclarations.get(text);
						if (integer == null || integer > parseNextNode.getLineNumber()) {
							variableDeclarations.put(text, parseNextNode.getLineNumber());
						}
					}
					if (parseNextNode instanceof BSHAssignment) {
						String text = ((BSHAssignment) parseNextNode).firstToken.image;
						Integer integer = variableDeclarations.get(text);
						if (integer == null || integer > parseNextNode.getLineNumber()) {
							variableDeclarations.put(text, parseNextNode.getLineNumber());
						}
					}
					if (parseNextNode instanceof BSHImportDeclaration) {
						BSHAmbiguousName node = (BSHAmbiguousName) ((BSHImportDeclaration) parseNextNode).children[0];
						imports.add(node.text);
					}
				}
			}

			if (gotBshElement != null) {
				currentLineNumber = gotBshElement.getLineNumber();
				SimpleNode bshElement = gotBshElement;
				//try to parse every bsh elements in the selection
				if (bshElement instanceof BSHAmbiguousName) {
					BSHAmbiguousName bshAmbiguousName = (BSHAmbiguousName) bshElement;
					String image = bshAmbiguousName.firstToken.image;
					final Integer lineNumber = variableDeclarations.get(image);
					if (lineNumber != null && bshAmbiguousName.firstToken.beginColumn <= currentOffsetInLine && bshAmbiguousName.firstToken.endColumn >= currentOffsetInLine) { //if it's a variable
						int length = bshAmbiguousName.firstToken.endColumn-bshAmbiguousName.firstToken.beginColumn;
						addHyperLink(selectedWord, retValue, selectedLineNumber, bshAmbiguousName.firstToken.beginColumn, length, doc, lineNumber);
					} else if (bshAmbiguousName.parent instanceof BSHAllocationExpression || bshAmbiguousName.parent instanceof BSHMethodInvocation || bshAmbiguousName.parent instanceof BSHPrimaryExpression) { //try to find the java element
						String text;
						String method = null;
						if (bshAmbiguousName.parent instanceof BSHAllocationExpression ) {
							text = bshAmbiguousName.text;
						} else if(bshAmbiguousName.parent instanceof BSHMethodInvocation) {
							text = bshAmbiguousName.text;
							text = text.substring(0, text.indexOf(bshAmbiguousName.lastToken.image) - 1);
							method = bshAmbiguousName.lastToken.image;
						} else {
							text = bshAmbiguousName.firstToken.image;
						}
						final int offset = bshAmbiguousName.lastToken.beginColumn;
						final int length = bshAmbiguousName.lastToken.endColumn - offset;
						if (bshAmbiguousName.lastToken.beginColumn <= currentOffsetInLine && bshAmbiguousName.lastToken.endColumn >= currentOffsetInLine) {
							IJavaProject javaproject = OfbizModelSingleton.get().findActiveEclipseProject().getJavaproject();
							try {
								List<String> typesToLookUp = new ArrayList<String>();
								if (text.equals("delegator")) {
									typesToLookUp.add("org.ofbiz.entity.GenericDelegator");
								} else if (text.equals("dispatcher")) {
									typesToLookUp.add("org.ofbiz.service.GenericDispatcher");
								} else {
									typesToLookUp.add(text);
									for (String importWord : imports) {
										if (importWord.endsWith(text)) {
											typesToLookUp.add(importWord);
										} else if (importWord.toLowerCase().equals(importWord)) { //is it a import vasdf.*
											typesToLookUp.add(importWord + "." + text);
										}
									}
								}
								for (final String typeToLookUp : typesToLookUp) {
									if (typeToLookUp != null) {
										final IType findType = javaproject.findType(typeToLookUp);
										if (findType != null) {
											if (method != null) {
												CompilationUnit parse = JavaHelper.parse(findType.getCompilationUnit());
												if (parse != null) {
													ASTVisitor astVisitor = new GotoJavaMethodAST(findType, retValue, parse, method, doc, selectedLineNumber, offset, length, bshAmbiguousName.parent.jjtGetChild(1).jjtGetNumChildren());
													parse.accept(astVisitor);
												}
											} else {
												retValue.add(new HyperlinkMarker() {

													@Override
													public String getTypeLabel() {
														return null;
													}

													@Override
													public String getHyperlinkText() {
														return "Java type: " + typeToLookUp;
													}

													@Override
													public void open() {
														try {
															IDE.openEditor(GoToFile.getActiveWorkbenchPage(), (IFile) findType.getCompilationUnit().getResource());
														} catch (PartInitException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}
													}
												});
											}
										}
									}
								}
							} catch (JavaModelException e) {
							}
						}
					}
				} else if (bshElement instanceof BSHTypedVariableDeclaration) {
					BSHTypedVariableDeclaration bshTypedVariableDeclaration = (BSHTypedVariableDeclaration) bshElement;
					String firstToken = ((BSHVariableDeclarator)bshTypedVariableDeclaration.children[1]).firstToken.toString();
					variable = firstToken;
					int lineNumber = bshElement.getLineNumber();
					int length = ((BSHVariableDeclarator)bshTypedVariableDeclaration.children[1]).lastToken.endColumn-((BSHVariableDeclarator)bshTypedVariableDeclaration.children[1]).firstToken.beginColumn;
					addHyperLink(firstToken, retValue, selectedLineNumber, ((BSHVariableDeclarator)bshTypedVariableDeclaration.children[1]).firstToken.beginColumn, length, doc, lineNumber);
				} else if (bshElement instanceof BSHVariableDeclarator) {
					BSHVariableDeclarator bshTypedVariableDeclaration = (BSHVariableDeclarator) bshElement;
					bshElement.getLineNumber();
					String firstToken = bshTypedVariableDeclaration.firstToken.toString();
					int lineNumber = bshElement.getLineNumber();
					variable = firstToken;
					addHyperLink(firstToken, retValue, selectedLineNumber, bshTypedVariableDeclaration.firstToken.beginColumn, bshTypedVariableDeclaration.firstToken.endColumn-bshTypedVariableDeclaration.firstToken.beginColumn, doc, lineNumber);
				} else if (bshElement instanceof BSHAssignment) {
					BSHAssignment bshTypedVariableDeclaration = (BSHAssignment) bshElement;
					bshElement.getLineNumber();
					String firstToken = bshTypedVariableDeclaration.firstToken.toString();
					if (firstToken.equals(selectedWord)) {
						variable = firstToken;
					}
					bshTypedVariableDeclaration.getText();
				}
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retValue;
	}

	private static void addHyperLink(final String selectedWord,
			List<HyperlinkMarker> retValue, final Integer lineNumber, final int offset, final int length, final IDocument doc, final int linkLineNumber) {
		retValue.add(new HyperlinkMarker() {

			@Override
			public String getTypeLabel() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getHyperlinkText() {
				return "Bsh variable first assignment: " + selectedWord;
			}
			@Override
			public void open() {
				IFile activeFile = OfbizModelSingleton.get().getActiveFile();
				IMarker marker;
				try {
					marker = activeFile.createMarker(Plugin.TEXT_MARKER);
					marker.setAttribute(IMarker.LINE_NUMBER, linkLineNumber);
					IDE.openEditor(GoToFile.getActiveWorkbenchPage(), marker);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public IRegion getHyperlinkRegion() {
				try {
					return new Region(doc.getLineOffset(lineNumber) + offset - 1, length + 1);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		});
	}
}
