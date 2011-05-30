package org.ofbiz.plugin.model.hyperlink;

import org.eclipse.core.resources.IMarker;

public class ServiceDefHyperlink extends HyperlinkMarker {
	private String serviceName;
	public ServiceDefHyperlink(String serviceName, IMarker marker) {
		super(marker);
		this.serviceName = serviceName;
	}

	@Override
	public String getTypeLabel() {
		return "Service def: " + serviceName;
	}

	@Override
	public String getHyperlinkText() {
		return "Service def: " + serviceName;
	}

}
