package com.generallycloud.nio.component.protocol.http11;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.TCPEndPoint;
import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.IOWriteFuture;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.TextWriteFuture;
import com.generallycloud.nio.component.protocol.http11.future.Cookie;
import com.generallycloud.nio.component.protocol.http11.future.ServerHttpReadFuture;

public class ServerHTTPProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(TCPEndPoint endPoint, IOReadFuture readFuture) throws IOException {
		
		ServerHttpReadFuture future = (ServerHttpReadFuture) readFuture;

		BufferedOutputStream o = readFuture.getWriteBuffer();

		StringBuilder h = new StringBuilder();

		h.append("HTTP/1.1 ");
		h.append(future.getStatus().getHeaderText());
		h.append(" \r\n");
		h.append("Server: nimbleio/0.0.1\r\n");
		h.append("Connection:keep-alive\r\n");
		h.append("Content-Length:");
		h.append(o.size());
		h.append("\r\n");
		
		Map<String,String> headers = future.getHeaders();
		
		if (headers != null) {
			Set<Entry<String, String>> hs = headers.entrySet();
			for(Entry<String,String> header : hs){
				h.append(header.getKey());
				h.append(":");
				h.append(header.getValue());
				h.append("\r\n");
			}
		}else{
			h.append("Content-Type:text/html;charset=UTF-8\r\n");
		}

		List<Cookie> cookieList = future.getCookieList();
		
		if (cookieList != null) {
			for(Cookie c : cookieList){
				h.append("Set-Cookie:");
				h.append(c.toString());
				h.append("\r\n");
			}
		}
		
		
		h.append("\r\n");
		
		int size = o.size();
		
		ByteBuffer buffer = ByteBuffer.allocate(h.length() + size);
		
		buffer.put(h.toString().getBytes(endPoint.getContext().getEncoding()));
		
		if (size != 0) {
			buffer.put(o.toByteArray(), 0, o.size());
		}
		
		buffer.flip();

		TextWriteFuture textWriteFuture = new TextWriteFuture(endPoint, readFuture, buffer);

		return textWriteFuture;
	}

}
