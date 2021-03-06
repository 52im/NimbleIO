package com.generallycloud.nio.balancing;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.generallycloud.nio.acceptor.TCPAcceptor;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.DefaultNIOContext;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.ServerConfiguration;

public class FrontFacadeAcceptor {

	private AtomicBoolean		started	= new AtomicBoolean(false);
	private TCPAcceptor			acceptor	= new TCPAcceptor();
	private FrontContext		frontContext;
	private FrontReverseAcceptor	frontReverseAcceptor;

	public void start(FrontConfiguration configuration) throws IOException {

		if (configuration == null) {
			throw new IllegalArgumentException("null configuration");
		}

		if (!started.compareAndSet(false, true)) {
			return;
		}

		this.frontContext = new FrontContext(configuration);

		this.frontContext.setFrontFacadeAcceptor(this);

		this.frontReverseAcceptor = new FrontReverseAcceptor();

		NIOContext context = new DefaultNIOContext();

		context.setIOEventHandleAdaptor(frontContext.getFrontFacadeAcceptorHandler());

		context.addSessionEventListener(frontContext.getFrontFacadeAcceptorSEListener());

		ServerConfiguration serverConfiguration = new ServerConfiguration();

		serverConfiguration.setSERVER_TCP_PORT(configuration.getFRONT_FACADE_PORT());

		context.setServerConfiguration(serverConfiguration);

		this.frontReverseAcceptor.start(frontContext);

		acceptor.setContext(context);

		acceptor.bind();

		LoggerUtil.prettyNIOServerLog(LoggerFactory.getLogger(FrontFacadeAcceptor.class), "Front Facade Acceptor 启动成功 ...");
	}

	public void stop() {
		if (!started.get()) {
			return;
		}
		this.acceptor.unbind();

		this.frontReverseAcceptor.stop();
	}

	public FrontContext getFrontContext() {
		return frontContext;
	}

	public TCPAcceptor getAcceptor() {
		return acceptor;
	}

}
