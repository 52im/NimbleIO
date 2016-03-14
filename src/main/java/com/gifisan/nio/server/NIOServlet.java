package com.gifisan.nio.server;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.servlet.GenericServlet;

public abstract class NIOServlet extends GenericServlet {

	public void initialize(ServerContext context, Configuration config) throws Exception {

	}

	public void destroy(ServerContext context, Configuration config) throws Exception {

	}
	
	public void prepare(ServerContext context, Configuration config) throws Exception {
		this.initialize(context, config);
	}

	public void unload(ServerContext context, Configuration config) throws Exception {
		this.destroy(context, config);
	}

	public String toString() {
		
		
		Configuration configuration = this.getConfig();
		
		String serviceName = configuration.getProperty("serviceName");
		
		if (StringUtil.isNullOrBlank(serviceName)) {
			serviceName = this.getClass().getSimpleName();
		}
		
		return "(service-name:"+serviceName+"@class:"+this.getClass().getName()+")";
	}
	
}
