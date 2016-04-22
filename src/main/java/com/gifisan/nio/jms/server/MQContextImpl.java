package com.gifisan.nio.jms.server;

import java.util.HashMap;
import java.util.HashSet;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.jms.ByteMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.TextMessage;
import com.gifisan.nio.server.session.NIOSession;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.server.session.Session;

public class MQContextImpl extends AbstractLifeCycle implements MQContext {

	private interface MessageParseFromRequest {

		Message parse(NIOSession session);
	}

	private long					dueTime				= 0;
	private final int				LOGINED				= 1;
	private HashMap<String, Message>	messageIDs			= new HashMap<String, Message>();
	private P2PProductLine			p2pProductLine			= new P2PProductLine(this);
	private SubscribeProductLine		subProductLine			= new SubscribeProductLine(this);
	private HashSet<String>			receivers				= new HashSet<String>();
	private String					username				= null;
	private String					password				= null;

	private MessageParseFromRequest[]	messageParsesFromRequest	= new MessageParseFromRequest[] {
													// ERROR
													// Message
			null,
			// NULL Message
			null,
			// Text Message
			new MessageParseFromRequest() {
				public Message parse(NIOSession session) {
					Parameters param = session.getParameters();
					String messageID = param.getParameter("msgID");
					String queueName = param.getParameter("queueName");
					String text = param.getParameter("text");
					TextMessage message = new TextMessage(messageID, queueName, text);

					return message;
				}
			}, new MessageParseFromRequest() {
				public Message parse(NIOSession session) {
					Parameters param = session.getParameters();
					String messageID = param.getParameter("msgID");
					String queueName = param.getParameter("queueName");
					String text = param.getParameter("text");

					BufferedOutputStream outputStream = (BufferedOutputStream) session
							.getServerOutputStream();
					byte[] array = outputStream.toByteArray();
					return new ByteMessage(messageID, queueName, text, array);
				}
			}										};

	MQContextImpl() {
	}

	public Message browser(String messageID) {
		return messageIDs.get(messageID);
	}

	protected void doStart() throws Exception {

		p2pProductLine.start();

		subProductLine.start();

		Thread p2pThread = new Thread(p2pProductLine, "JMS-P2P-ProductLine");

		Thread subThread = new Thread(subProductLine, "JMS-SUB-ProductLine");

		p2pThread.start();

		subThread.start();

	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(p2pProductLine);
		LifeCycleUtil.stop(subProductLine);

	}

	public long getMessageDueTime() {
		return this.dueTime;
	}

	public boolean isLogined(Session session) {
		return LOGINED == session.getEndpointMark();
	}

	public int messageSize() {
		return this.messageIDs.size();
	}

	public void offerMessage(Message message) {

		synchronized (messageIDs) {
			messageIDs.put(message.getMsgID(), message);
		}

		p2pProductLine.offerMessage(message);
	}

	public void publishMessage(Message message) {

		subProductLine.offerMessage(message);
	}

	public void consumerMessage(Message message) {
		synchronized (messageIDs) {
			messageIDs.remove(message.getMsgID());
		}
	}

	public Message parse(NIOSession session) {
		Parameters param = session.getParameters();
		int msgType = param.getIntegerParameter("msgType");
		Message message = messageParsesFromRequest[msgType].parse(session);
		return message;
	}

	public void pollMessage(Session session, JMSSessionAttachment attachment) {

		p2pProductLine.pollMessage(session, attachment);
	}

	public void subscribeMessage(Session session, JMSSessionAttachment attachment) {

		subProductLine.pollMessage(session, attachment);
	}

	public void setLogined(boolean logined, Session session) {
		session.setEndpointMark(LOGINED);
	}

	public void setMessageDueTime(long dueTime) {
		this.dueTime = dueTime;
		this.p2pProductLine.setDueTime(dueTime);
		this.subProductLine.setDueTime(dueTime);
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean login(NIOSession session, JMSSessionAttachment attachment) {

		Parameters param = session.getParameters();

		String _username = param.getParameter("username");

		String _password = param.getParameter("password");

		if (username.equals(_username) && password.equals(_password)) {

			if (attachment == null) {

				attachment = new JMSSessionAttachment(this);

				session.attach(attachment);
			}

			boolean isConsumer = param.getBooleanParameter("consumer");

			if (isConsumer) {
				session.addEventListener(new TransactionProtectListener());

				String queueName = param.getParameter("queueName");

				attachment.addQueueName(queueName);

				synchronized (receivers) {

					receivers.add(queueName);
				}
			}

			setLogined(true, session);

			DebugUtil.debug("user [" + username + "] login successful!");

			return true;
		}

		return false;
	}

	public boolean isOnLine(String queueName) {
		return receivers.contains(queueName);
	}

	public void removeReceiver(String queueName) {

		synchronized (receivers) {
			receivers.remove(queueName);
		}
	}

}
