package com.generallycloud.nio.extend.plugin.rtp.client;

import java.io.IOException;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.DatagramPacketAcceptor;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.UDPEndPoint;
import com.generallycloud.nio.component.protocol.DatagramPacket;
import com.generallycloud.nio.component.protocol.DatagramPacketGroup;

public class RTPClientDPAcceptor implements DatagramPacketAcceptor {

	private int				markInterval		;
	private long				lastMark			;
	private long				currentMark		;
	private DatagramPacketGroup	packetGroup		;
	private RTPHandle			udpReceiveHandle	;
	private RTPClient			rtpClient			;
	private NIOContext			context			;
	private int				groupSize			;
	private Logger				logger			= LoggerFactory.getLogger(RTPClientDPAcceptor.class);

	public RTPClientDPAcceptor(int markInterval, long currentMark, int groupSize, RTPHandle udpReceiveHandle,
			RTPClient rtpClient) {
		this.markInterval = markInterval;
		this.udpReceiveHandle = udpReceiveHandle;
		this.rtpClient = rtpClient;
		this.context = rtpClient.getContext();
		this.markInterval = markInterval;
		this.currentMark = currentMark;
		this.lastMark = currentMark - markInterval;
		this.groupSize = groupSize;
		this.packetGroup = new DatagramPacketGroup(groupSize);

		logger.debug("________________lastMark______create:{}", lastMark);

	}

	public void accept(UDPEndPoint endPoint, DatagramPacket packet) throws IOException {

		long timestamp = packet.getTimestamp();

		// logger.debug("timestamp:{},lastMark:{}", timestamp, lastMark);

		if (timestamp < lastMark) {
			logger.info("______________________ignore packet:{},___lastMark:{},", packet, lastMark);
			return;
		}

		if (timestamp < currentMark) {
			packetGroup.addDatagramPacket(packet);
			return;
		}

		if (timestamp >= currentMark) {

			lastMark = currentMark;

			currentMark = lastMark + markInterval;

			final DatagramPacketGroup _packetGroup = this.packetGroup;

			// logger.debug("__________________packetGroup size :{}",_packetGroup.size());

			this.packetGroup = new DatagramPacketGroup(groupSize);

			this.packetGroup.addDatagramPacket(packet);

			context.getThreadPool().dispatch(new Runnable() {

				public void run() {
					try {
						udpReceiveHandle.onReceiveUDPPacket(rtpClient, _packetGroup);
					} catch (Throwable e) {
						logger.debug(e);
					}
				}
			});

		}
	}

}
