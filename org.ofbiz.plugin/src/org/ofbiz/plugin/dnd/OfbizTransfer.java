package org.ofbiz.plugin.dnd;

import org.eclipse.emf.edit.ui.dnd.LocalTransfer;

public class OfbizTransfer extends LocalTransfer {
	private static OfbizTransfer instance = new OfbizTransfer();

	/**
	 * Returns the singleton gadget transfer instance.
	 */
	public static OfbizTransfer getInstance() {
		return instance;
	}
}
