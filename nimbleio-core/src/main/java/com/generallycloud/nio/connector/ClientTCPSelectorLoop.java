package com.generallycloud.nio.connector;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.ChannelWriterImpl;
import com.generallycloud.nio.component.ChannelWriter;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.TCPSelectorLoop;
import com.generallycloud.nio.component.concurrent.UniqueThread;

public class ClientTCPSelectorLoop extends TCPSelectorLoop {
	
	private ChannelWriter	channelWriter			= null;

	private UniqueThread	channelWriterThread	= null;

	public ClientTCPSelectorLoop(NIOContext context, TCPConnector connector) {

		super(context);

		this._alpha_acceptor = new TCPSelectionConnector(context, connector);
	}

	public void register(NIOContext context, SelectableChannel channel) throws IOException {

		this.selector = Selector.open();
		
		this.channelWriter = new ChannelWriterImpl(context);

		this.channelWriterThread = new UniqueThread(channelWriter, channelWriter.toString());
		
		this._alpha_acceptor.setChannelWriter(channelWriter);

		this.channelWriterThread.start();

		TCPSelectionConnector selectionConnector = (TCPSelectionConnector) this._alpha_acceptor;

		selectionConnector.setSelector(selector);

		channel.register(selector, SelectionKey.OP_CONNECT);
	}

	public void stop() {

		super.stop();

		LifeCycleUtil.stop(channelWriterThread);
	}
}
