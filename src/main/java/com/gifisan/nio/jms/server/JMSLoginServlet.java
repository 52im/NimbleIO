package com.gifisan.nio.jms.server;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.session.NIOSession;

public class JMSLoginServlet extends JMSServlet {

	public void accept(NIOSession session,JMSSessionAttachment attachment) throws Exception {

		MQContext context = getMQContext();
		
		if (context.login(session, attachment)) {
			session.write(ByteUtil.TRUE);
		}else{
			session.disconnect();
			DebugUtil.debug("user [" + session.getParameters().getParameter("username") + "] login failed!");
			session.write(ByteUtil.FALSE);
		}
		
		session.flush();

	}
	
	public void prepare(ServerContext context, Configuration config) throws Exception {

		MQContext mqContext = getMQContext();
		
		mqContext.setUsername(config.getProperty("username"));
		mqContext.setPassword(config.getProperty("password"));

		long dueTime = config.getLongProperty("due-time");

		mqContext.setMessageDueTime(dueTime == 0 ? 1000 * 60 * 60 * 24 * 7 : dueTime);
	}

	public void unload(ServerContext context, Configuration config) throws Exception {
		
	}

	public void destroy(ServerContext context, Configuration config) throws Exception {

		MQContextFactory.setNullMQContext();
		
		super.destroy(context, config);
	}

	public void initialize(ServerContext context, Configuration config) throws Exception {

		MQContext mqContext = getMQContext();
		
		mqContext.setUsername(config.getProperty("username"));
		mqContext.setPassword(config.getProperty("password"));

		long dueTime = config.getLongProperty("due-time");

		mqContext.setMessageDueTime(dueTime == 0 ? 1000 * 60 * 60 * 24 * 7 : dueTime);
		
		MQContextFactory.initializeContext();
		
		super.initialize(context, config);
	}

}
