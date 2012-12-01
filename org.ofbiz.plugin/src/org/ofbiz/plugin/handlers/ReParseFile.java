package org.ofbiz.plugin.handlers;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.ofbiz.plugin.Plugin;
import org.ofbiz.plugin.ofbiz.ParseableFile;
import org.ofbiz.plugin.parser.Parser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ReParseFile implements IObjectActionDelegate {
	private IWorkbenchPart targetPart;
	private ISelection selection;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	@Override
	public void run(IAction arg0) {
		Object treeSelect = ((TreeSelection)selection).getFirstElement();
		if (treeSelect instanceof ParseableFile) {
			ParseableFile parseAble = (ParseableFile) treeSelect;
//			EcoreUtil.remove(parseAble);
			Parser parser = parseAble.getParser();
			parseAble.eCrossReferences();
			try {
				XmlPullParser xpp =
						Plugin.getDefault().getXmlPullParserPool().getPullParserFromPool();
				parser.processDocument(xpp, parser.getFile());
			} catch (XmlPullParserException e) {
			} catch (CoreException e) {
			} catch (IOException e) {
			}
		}
	}

}
