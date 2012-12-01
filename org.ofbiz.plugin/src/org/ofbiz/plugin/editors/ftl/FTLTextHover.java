package org.ofbiz.plugin.editors.ftl;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.ofbiz.plugin.model.OfbizModelSingleton;
import org.ofbiz.plugin.ofbiz.FreemarkerMacroDeclaration;

public class FTLTextHover implements ITextHover {

	public IRegion getHoverRegion(ITextViewer tv, int off) {
		return new Region(off, 0);
	}

	public String getHoverInfo(ITextViewer tv, IRegion r) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (true) {
			try {
				String string = tv.getDocument().get(r.getOffset() + i++, 1);
				if (" ".equals(string) || "\"".equals(string) || ">".equals(string) || "}".equals(string)) {
					break;
				}
				sb.append(string);
			} catch (BadLocationException e) {
				break;
			}

		}
		i=0;
		while (true) {
			try {
				String string = tv.getDocument().get(r.getOffset() -1 + i--, 1);
				if (" ".equals(string) || "\"".equals(string) || "<".equals(string) || "{".equals(string)) {
					break;
				}
				sb.insert(0, string);
			} catch (BadLocationException e) {
				break;
			}
			
		}
		StringBuilder retValue = new StringBuilder();
		if (sb.toString().indexOf("@") != -1) {
			String searchString = sb.toString().substring(sb.toString().indexOf("@")+1, sb.toString().length());
			for (FreemarkerMacroDeclaration freemarkerMacroDeclaration : OfbizModelSingleton.get().findMacro(searchString)) {
				retValue.append(freemarkerMacroDeclaration.getDocumentation()).append("\n");
			}
			return retValue.toString();
			
		} else {
			return null;
		}
	}
}