package com.generallycloud.nio.extend.configuration;

import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.component.Configuration;

public class ServicesConfiguration {
	
	private List<Configuration> servlets = new ArrayList<Configuration>();

	public List<Configuration> getServlets() {
		return servlets;
	}

	protected void addServlets(Configuration servlet) {
		this.servlets.add(servlet);
	}

}
