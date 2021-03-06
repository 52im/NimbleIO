package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.acceptor.UDPEndPointFactory;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.protocol.DatagramPacket;

public class UDPSelectionReader implements SelectionAcceptor {

	private NIOContext	context		;
	private ByteBuffer	cacheBuffer	= ByteBuffer.allocate(DatagramPacket.PACKET_MAX);
	private Logger		logger		= LoggerFactory.getLogger(UDPSelectionReader.class);

	public UDPSelectionReader(NIOContext context) {
		this.context = context;
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		NIOContext context = this.context;

		ByteBuffer cacheBuffer = this.cacheBuffer;

		cacheBuffer.clear();

		DatagramChannel channel = (DatagramChannel) selectionKey.channel();

		InetSocketAddress remoteSocketAddress = (InetSocketAddress) channel.receive(cacheBuffer);

		UDPEndPointFactory factory = context.getUDPEndPointFactory();

		DatagramPacket packet = new DatagramPacket(cacheBuffer, remoteSocketAddress);

		DatagramPacketAcceptor acceptor = context.getDatagramPacketAcceptor();
		
		if (acceptor == null) {
			logger.debug("______________ none acceptor for context");
			return;
		}

		UDPEndPoint endPoint = factory.getUDPEndPoint(context, selectionKey, remoteSocketAddress);

		acceptor.accept(endPoint, packet);

	}
}
