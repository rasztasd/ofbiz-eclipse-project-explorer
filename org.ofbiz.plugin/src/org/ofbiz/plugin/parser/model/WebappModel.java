package org.ofbiz.plugin.parser.model;

import org.eclipse.core.resources.IFile;

public class WebappModel {
	private IFile iFile;
	private String uri;
	
	public WebappModel(IFile iFile, String uri) {
		super();
		this.iFile = iFile;
		this.uri = uri;
	}
	public IFile getiFile() {
		return iFile;
	}
	public String getUri() {
		return uri;
	}
	
}
