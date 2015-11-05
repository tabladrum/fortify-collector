package com.capitalone.dashboard.collector;

import com.fortify.ws.core.ClientCore;

public class FortifyWSTemplate extends ClientCore {

	private static final String F360_WS_PATH = "/fm-ws/services";
	private String urls;


	public FortifyWSTemplate(String serverURL) {
		this.urls = serverURL.trim();
	}

	public String getUri() {
		return this.urls + F360_WS_PATH;
	}

}
