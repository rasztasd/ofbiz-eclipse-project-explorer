package org.ofbiz.plugin.hyperlink;

import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.w3c.dom.Node;

public class ClassHandleXml {
	public static Node getCurrentNode(IDocument document, int offset) {
		// get the current node at the offset (returns either: element,
		// doctype, text)
		IndexedRegion inode = null;
		IStructuredModel sModel = null;
		try {
			sModel = StructuredModelManager.getModelManager().getExistingModelForRead(document);
			if (sModel != null) {
				inode = sModel.getIndexedRegion(offset);
				if (inode == null) {
					inode = sModel.getIndexedRegion(offset - 1);
				}
			}
		}
		finally {
			if (sModel != null) {
				sModel.releaseFromRead();
			}
		}

		if (inode instanceof Node) {
			return (Node) inode;
		}
		return null;
	}
	public ClassHandleXml(IDocument document, int offset) {
	}	
}
