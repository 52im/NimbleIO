package com.generallycloud.nio.component;

import com.generallycloud.nio.Looper;
import com.generallycloud.nio.component.ChannelWriterImpl.ChannelWriteEvent;
import com.generallycloud.nio.component.protocol.IOWriteFuture;

public interface ChannelWriter extends Looper{
	
	public abstract void fire(ChannelWriteEvent event);

	public abstract void offer(IOWriteFuture future);
	
	public abstract void wekeupEndPoint(TCPEndPoint endPoint);

}