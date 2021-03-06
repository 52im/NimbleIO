package test.http11;

import java.io.IOException;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.common.test.ITestThread;
import com.generallycloud.nio.common.test.ITestThreadHandle;
import com.generallycloud.nio.component.ReadFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.http11.ClientHTTPProtocolFactory;
import com.generallycloud.nio.component.protocol.http11.HttpClient;
import com.generallycloud.nio.component.protocol.http11.HttpIOEventHandle;
import com.generallycloud.nio.component.protocol.http11.future.HttpRequestFuture;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;

public class TestHttpLoadThread extends ITestThread {

	HttpIOEventHandle	eventHandleAdaptor	= new HttpIOEventHandle();

	TCPConnector		connector			= IOConnectorUtil.getTCPConnector(eventHandleAdaptor);

	Session			session;

	HttpClient		client;

	public void run() {
		
		int time = getTime();
		
		for (int i = 0; i < time; i++) {
			
			HttpRequestFuture future = ReadFutureFactory.createHttpReadFuture(session, "/test");

			try {
				client.request(session, future, 10000);
				
				getLatch().countDown();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void prepare() throws IOException {
		
		connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());

		eventHandleAdaptor.setTCPConnector(connector);
		
		client = eventHandleAdaptor.getHttpClient();

		connector.connect();
		
		session = connector.getSession();
	}

	public void stop() {
		CloseUtil.close(connector);
	}
	
	public static void main(String[] args) {
		
		PropertiesLoader.setBasepath("nio");
		
		int	time		= 10240000;
		
		int core_size = 256;
		
		ITestThreadHandle.doTest(TestHttpLoadThread.class, core_size, time / core_size);
	}

}
