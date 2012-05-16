package org.ofbiz.plugin.hyperlink;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.model.ComponentHelper;
import org.ofbiz.plugin.model.ControllerHelper;
import org.ofbiz.plugin.model.JavaHelper;
import org.ofbiz.plugin.model.ServiceHelper;
import org.ofbiz.plugin.model.JavaHelper.GotoJavaMethodAST;
import org.ofbiz.plugin.model.OfbizModelSingleton;
import org.ofbiz.plugin.model.ScreenHelper;
import org.ofbiz.plugin.model.hyperlink.FtlHyperlink;
import org.ofbiz.plugin.model.hyperlink.HyperlinkMarker;
import org.ofbiz.plugin.model.hyperlink.HyperlinkMarkersHelper;
import org.ofbiz.plugin.ofbiz.ClasspathEntry;
import org.ofbiz.plugin.ofbiz.Component;
import org.ofbiz.plugin.ofbiz.Controller;
import org.ofbiz.plugin.ofbiz.Service;
import org.ofbiz.plugin.parser.GoToFile;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import bsh.BeanshellGotoDeclaration;

public class OfbizHyperlinkDetector extends AbstractHyperlinkDetector {



	private static String RESPONSE = "response";

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer editor, IRegion wordRegion,
			boolean arg2) {

		try {
			IEditorInput editorInput = Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getSite().getWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
			List<IHyperlink> hyperlinkMarkersToReturn = new ArrayList<IHyperlink>();
			if (editorInput instanceof IFileEditorInput) {
				IDocument doc = editor.getDocument();
				int offset = wordRegion.getOffset();
				final IRegion reg = findJavaString(doc, offset);
				List<HyperlinkMarker> hyperlinkMarkers = null;
				String selectedWord = doc.get(reg.getOffset(), reg.getLength());
				IFile file = ((IFileEditorInput) editorInput).getFile();
				String fileName = file.getName();
				if (ScreenHelper.getScreenByFile(file) != null) { //if it's a screen
					Node currentNode = ClassHandleXml.getCurrentNode(doc, offset);
					if (currentNode != null) {
						if (currentNode.getNodeName().equals("html-template")) {
							String location = currentNode.getAttributes().getNamedItem("location").getNodeValue();
							if (location.equals(selectedWord)) {
								hyperlinkMarkers = new ArrayList<HyperlinkMarker>();
								String ftlFileName = location.substring("component://".length());
								Component componentByUrl = ComponentHelper.getComponentByUrl(location);
								IFile fileToOpen =  (IFile) ( componentByUrl.getDirectory().getFolder()).findMember(ftlFileName);
								if (fileToOpen != null) {
									hyperlinkMarkers.add(new FtlHyperlink(ftlFileName, fileToOpen));
								}
							}
						} else if (currentNode.getNodeName().equals("service")) {
							String location = currentNode.getAttributes().getNamedItem("service-name").getNodeValue();
							if (selectedWord.equals(location)) {
								hyperlinkMarkers = HyperlinkMarkersHelper.searchForServices(selectedWord);
							}
						} else if (currentNode.getNodeName().equals("include-form")) {
							String nodeValue = currentNode.getAttributes().getNamedItem("name").getNodeValue();
							String location = currentNode.getAttributes().getNamedItem("location").getNodeValue();
							if (selectedWord.equals(location)) {
								hyperlinkMarkers = ScreenHelper.getFormHyperlinkMarker(nodeValue, selectedWord);
							}
						} else if (currentNode.getNodeName().equals("include-screen")) {
							String nodeValue = currentNode.getAttributes().getNamedItem("name").getNodeValue();
							String location = currentNode.getAttributes().getNamedItem("location").getNodeValue();
							if (selectedWord.equals(location)) {
								String screenXml = selectedWord.substring("component://".length());
								String formName = nodeValue;
								hyperlinkMarkers = ScreenHelper.getHyperlinkMarker(formName, selectedWord);
							}
						} else if (currentNode.getNodeName().equals("script")) {
							hyperlinkMarkers = new ArrayList<HyperlinkMarker>();
							final String location = currentNode.getAttributes().getNamedItem("location").getNodeValue();
							if (selectedWord.equals(location)) {
								Component componentByUrl = ComponentHelper.getComponentByUrl(location);
								final IFile fileToOpen =  (IFile) ( componentByUrl.getDirectory().getFolder()).findMember(location.substring("component://".length()));
								if (fileToOpen != null) {
									hyperlinkMarkers.add(new HyperlinkMarker() {

										@Override
										public String getTypeLabel() {
											// TODO Auto-generated method stub
											return null;
										}

										@Override
										public String getHyperlinkText() {
											return "Script: " + location;
										}

										@Override
										public void open() {
											try {
												IDE.openEditor(GoToFile.getActiveWorkbenchPage(), fileToOpen);
											} catch (PartInitException e) {
											}
										}
									});
								}
							}
						}
						else {
							return null;
						}
					}
				} else if (fileName.endsWith("xml") && (hyperlinkMarkers = handleControllerXml(selectedWord, file,
						fileName, doc, offset)) != null && hyperlinkMarkers.size() != 0) {
				} else if(fileName.endsWith(".bsh")) {
					int lineOfOffset = doc.getLineOfOffset(wordRegion.getOffset());
					IRegion lineInfo = doc.getLineInformationOfOffset(wordRegion.getOffset());
					int currentOffsetInLine = wordRegion.getOffset() - lineInfo.getOffset();
					//					currentOffsetInLine = doc.getLineOfOffset(lineOfOffset) + 1;
					doc.get(lineInfo.getOffset(), lineInfo.getLength());

					//expand tabs to spaces
					//					doc.get(arg0, arg1);
					hyperlinkMarkers = BeanshellGotoDeclaration.gotoDeclaration(doc.get(0, doc.getLength()), selectedWord, currentOffsetInLine, lineOfOffset, doc);

				} else {
					Service serviceFile = ServiceHelper.isServiceFile(file);
					if (serviceFile != null) {
//						hyperlinkMarkers = HyperlinkMarkersHelper.searchForServices(selectedWord);
						hyperlinkMarkers = new ArrayList<HyperlinkMarker>();
						Node currentNode = ClassHandleXml.getCurrentNode(doc, offset);
						if ("simple".equals(currentNode.getAttributes().getNamedItem("engine").getNodeValue())) {
							if (currentNode.getAttributes().getNamedItem("invoke").getNodeValue().equals(selectedWord)) {
								Component component = serviceFile.getServiceFile().getComponent();
								String location = currentNode.getAttributes().getNamedItem("location").getNodeValue();
								for (ClasspathEntry classpathEntry : component.getClasspathEntries()) {
									IResource findMember = component.getFolder().findMember(classpathEntry.getClasspathLocation() + "/" + location);
									if (findMember != null) {
										final IFile fileToOpen = (IFile) findMember;
										hyperlinkMarkers.add(new HyperlinkMarker() {

											@Override
											public String getTypeLabel() {
												// TODO Auto-generated method stub
												return null;
											}

											@Override
											public String getHyperlinkText() {
												return "Minilang";
											}

											@Override
											public void open() {
												try {
													IDE.openEditor(GoToFile.getActiveWorkbenchPage(), fileToOpen);
												} catch (PartInitException e) {
												}
											}
										});
										break;
									}
								}
							}
						} else if ("java".equals(currentNode.getAttributes().getNamedItem("engine").getNodeValue())) {
							hyperlinkMarkers = HyperlinkMarkersHelper.searchForServices(selectedWord);
							try {
								String location = currentNode.getAttributes().getNamedItem("location").getNodeValue();
								String invoke = currentNode.getAttributes().getNamedItem("invoke").getNodeValue();
								final IType findType = OfbizModelSingleton.get().findActiveEclipseProject().getJavaproject().findType(location);
								ICompilationUnit cu = findType.getCompilationUnit();
								final CompilationUnit parse = JavaHelper.parse(cu);
								if (parse != null) {
									ASTVisitor astVisitor = new GotoJavaMethodAST(findType, hyperlinkMarkers,
											parse, invoke);
									parse.accept(astVisitor);
								}
							} catch (JavaModelException e) {
								return null;
							}
						}
						//					selectedWord;
					} else {
						hyperlinkMarkers = HyperlinkMarkersHelper.searchForServices(selectedWord);
					}
				}

				if (hyperlinkMarkers != null && hyperlinkMarkers.size() > 0) {
					for (final HyperlinkMarker hyperlinkMarker : hyperlinkMarkers) {
						hyperlinkMarkersToReturn.add(new IHyperlink() {

							@Override
							public void open() {
								hyperlinkMarker.open();
							}

							@Override
							public String getTypeLabel() {
								return hyperlinkMarker.getTypeLabel();
							}

							@Override
							public String getHyperlinkText() {
								return hyperlinkMarker.getHyperlinkText();
							}

							@Override
							public IRegion getHyperlinkRegion() {
								IRegion hyperlinkRegion = hyperlinkMarker.getHyperlinkRegion();
								if (hyperlinkRegion != null) {
									return hyperlinkRegion;
								}
								return reg;
							}
						});
					}
					return hyperlinkMarkersToReturn.toArray(new IHyperlink[hyperlinkMarkersToReturn.size()]);
				} else {
					return null;
				}
			}

		} catch (BadLocationException e) {
		}
		return null;
	}

	private List<HyperlinkMarker> handleControllerXml(final String selectedWord, final IFile file, String fileName, IDocument doc, int offset) {
		final List<HyperlinkMarker> hyperlinkMarkers = new ArrayList<HyperlinkMarker>();
		Node currentNode = ClassHandleXml.getCurrentNode(doc, offset);
		if (currentNode != null) {
			NamedNodeMap attributes = currentNode.getAttributes();
			if (currentNode.getNodeName().equals(RESPONSE)) {
				Node responseValue = attributes.getNamedItem("value");
				if (attributes.getNamedItem("type").getNodeValue().equals("view")) {
					if (responseValue.getNodeValue().equals(selectedWord)) {
						hyperlinkMarkers.addAll(ControllerHelper.getHyperlinkForRequestValueView(selectedWord, file));
					}
				} else if (attributes.getNamedItem("type").getNodeValue().equals("request")) {
					if (responseValue.getNodeValue().equals(selectedWord)) {
						hyperlinkMarkers.addAll(ControllerHelper.getHyperlinkForRequestValueRequest(selectedWord, file));
					}
				}
			} else if (currentNode.getNodeName().equals("event")) {
				if (attributes.getNamedItem("type").getNodeValue().equals("service") || attributes.getNamedItem("type").getNodeValue().equals("service-multi")) {
					Node responseValue = attributes.getNamedItem("invoke");
					if (responseValue.getNodeValue().equals(selectedWord)) {
						hyperlinkMarkers.addAll(HyperlinkMarkersHelper.searchForServices(selectedWord));
					}
				} else if (attributes.getNamedItem("type").getNodeValue().equals("bsf")) {
					Node responseValue = attributes.getNamedItem("invoke");
					if (responseValue.getNodeValue().equals(selectedWord)) {
						hyperlinkMarkers.addAll(HyperlinkMarkersHelper.searchForServices(selectedWord));
					}
				} else if (attributes.getNamedItem("type").getNodeValue().equals("java")) {
					final String invoke = attributes.getNamedItem("invoke").getNodeValue();
					final String path = attributes.getNamedItem("path").getNodeValue();
					if (invoke.equals(selectedWord)) {
						try {
							final IType findType = OfbizModelSingleton.get().findActiveEclipseProject().getJavaproject().findType(path);
							ICompilationUnit cu = findType.getCompilationUnit();
							final CompilationUnit parse = JavaHelper.parse(cu);
							if (parse != null) {
								ASTVisitor astVisitor = new GotoJavaMethodAST(findType, hyperlinkMarkers,
										parse, invoke);
								parse.accept(astVisitor);
							}
						} catch (JavaModelException e) {
							return null;
						}
					}
				}
			} else if (currentNode.getNodeName().equals("view-map")) {
				final String page = attributes.getNamedItem("page").getNodeValue();
				if (page.equals(selectedWord)) {
					if (attributes.getNamedItem("type").getNodeValue().equals("ftl")) {
						Controller controller = ControllerHelper.getController(file);
						IFile findMember;
						if (page.contains("component://")) {
							findMember = null;
						} else if (page.indexOf("/") == 0) {
							findMember = null;
						} else {
							findMember = (IFile) controller.getFile().getParent().getParent().findMember(page);
						}
						if (findMember != null) {
							hyperlinkMarkers.add(new FtlHyperlink(page, findMember));
						}
					} else if (attributes.getNamedItem("type").getNodeValue().equals("screen")) {
						String screenXml = page.substring(0, page.indexOf("#"));
						Controller controller = ControllerHelper.getController(file);
						hyperlinkMarkers.addAll(ScreenHelper.getHyperlinkMarkerForController(page, controller, screenXml));
					}
				}
			}
		}
		return hyperlinkMarkers;
	}



	private IRegion findJavaString(IDocument document, int offset) {
		int beginingPos = offset;
		int endPos;
		int length = 0;
		try {
			loop:
				while (true) {
					char char1 = document.getChar(beginingPos);
					switch (char1) {
					case '"':
					case '>':
					case '(':
					case ',':
					case '\n':
					case ' ': break loop;
					}
					if (char1 == '"') {
						break;
					}
					beginingPos--;
					if (beginingPos == 0) {
						break;
					}
				}
		endPos = beginingPos + 1;
		loop:
			while (true) {
				char char1 = document.getChar(endPos);
				switch (char1) {
				case '"':
				case '<':
				case ')':
				case ',':
				case '\n':
				case ' ': break loop;
				}
				endPos++;
				length++;
				if (endPos == document.getLength()) {
					break;
				}
			}
		return new Region(beginingPos+1, length);
		} catch (BadLocationException e) {
		}
		return null;
	}



}
