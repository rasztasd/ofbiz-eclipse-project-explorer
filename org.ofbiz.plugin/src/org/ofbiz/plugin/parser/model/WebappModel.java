package org.ofbiz.plugin.parser.model;

import org.eclipse.core.resources.IFile;
import org.ofbiz.plugin.ofbiz.Controller;
import org.ofbiz.plugin.ofbiz.WebApp;

public class WebappModel {
	private IFile iFile;
	private String uri;
	private Controller referencingController;
	
	/**
	 * 
	 * @param iFile
	 * @param uri
	 * @param referencingController null if it's referenced from a ofbiz-component.xml
	 */
	public WebappModel(IFile iFile, String uri, Controller referencingController) {
		this.iFile = iFile;
		this.uri = uri;
		this.referencingController = referencingController;
	}
	public IFile getiFile() {
		return iFile;
	}
	public String getUri() {
		return uri;
	}
	public Controller getReferencingController() {
		return referencingController;
	}
	
}
