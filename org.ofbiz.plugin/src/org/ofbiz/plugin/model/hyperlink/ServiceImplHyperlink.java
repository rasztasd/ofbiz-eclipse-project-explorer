package org.ofbiz.plugin.model.hyperlink;

import org.eclipse.core.resources.IMarker;

public class ServiceImplHyperlink extends HyperlinkMarker {

	private String serviceName;
	public ServiceImplHyperlink(String serviceName, IMarker marker) {
		super(marker);
		this.serviceName = serviceName;
	}


	@Override
	public String getTypeLabel() {
		return "Service impl: " + serviceName;
	}

	@Override
	public String getHyperlinkText() {
		return "Service impl: " + serviceName;
	}

}
