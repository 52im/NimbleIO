package com.generallycloud.nio.component.protocol.http11;

import java.io.IOException;

import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.WaiterOnReadFuture;
import com.generallycloud.nio.component.protocol.http11.future.HttpReadFuture;
import com.generallycloud.nio.component.protocol.http11.future.HttpRequestFuture;
import com.generallycloud.nio.connector.TCPConnector;

public class HttpClient {
	
	protected HttpClient(TCPConnector connector) {
		this.connector = connector;
	}
	
	private TCPConnector connector;

	private WaiterOnReadFuture listener = null;
	
	public HttpReadFuture request(Session session, HttpRequestFuture	 future, long timeout) throws IOException {

		this.listener = new WaiterOnReadFuture();

		session.flush(future);

		// FIXME 连接丢失时叫醒我
		if (!listener.await(timeout)) {

			return (HttpReadFuture) listener.getReadFuture();
		}

		CloseUtil.close(connector);
		
		throw new TimeoutException("timeout");

	}

	public WaiterOnReadFuture getListener(){
		return listener;
	}

}
