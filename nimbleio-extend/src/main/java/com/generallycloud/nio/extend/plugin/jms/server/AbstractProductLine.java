package com.generallycloud.nio.extend.plugin.jms.server;

import java.util.HashMap;
import java.util.Map;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.extend.ApplicationContextUtil;
import com.generallycloud.nio.extend.plugin.jms.Message;
import com.generallycloud.nio.extend.security.Authority;

public abstract class AbstractProductLine extends AbstractLifeCycle implements MessageQueue, Runnable {

	protected MQContext					context		;
	protected MessageStorage				storage		;
	protected long						dueTime		;
	protected boolean						running		= false;
	protected Map<String, ConsumerQueue>		consumerMap	;
	private Logger							logger		= LoggerFactory.getLogger(AbstractProductLine.class);

	public AbstractProductLine(MQContext context) {
		this.context = context;
	}

	protected void doStart() throws Exception {

		this.running = true;

		this.storage = new MessageStorage();

		this.consumerMap = new HashMap<String, ConsumerQueue>();

		this.dueTime = context.getMessageDueTime();

	}

	// TODO 处理剩下的message 和 receiver
	protected void doStop() throws Exception {
		this.running = false;
	}

	public MQContext getContext() {
		return context;
	}

	public void pollMessage(Session session, NIOReadFuture future, MQSessionAttachment attachment) {

		if (attachment.getConsumer() != null) {
			return;
		}
		
		Authority authority = ApplicationContextUtil.getAuthority(session);

		String queueName = authority.getUuid();

		// 来自终端类型
		context.addReceiver(queueName);
		
		ConsumerQueue consumerQueue = getConsumerQueue(queueName);

		Consumer consumer = new Consumer(consumerQueue, attachment, session, future, queueName);
		
		attachment.setConsumer(consumer);

		//FIXME 按位置设置
		consumerQueue.offer(consumer);
	}

	protected ConsumerQueue getConsumerQueue(String queueName) {

		ConsumerQueue consumerQueue = consumerMap.get(queueName);

		if (consumerQueue == null) {

			synchronized (consumerMap) {

				consumerQueue = consumerMap.get(queueName);

				if (consumerQueue == null) {
					consumerQueue = createConsumerQueue();
					consumerMap.put(queueName, consumerQueue);
				}
			}
		}
		return consumerQueue;
	}

	protected abstract ConsumerQueue createConsumerQueue();

	public void offerMessage(Message message) {

		storage.offer(message);
	}

	protected void filterUseless(Message message) {
		long now = System.currentTimeMillis();
		long dueTime = this.dueTime;

		if (now - message.getTimestamp() > dueTime) {
			// 消息过期了
			logger.debug(">>>> message invalidate : {}", message);
			return;
		}
		this.offerMessage(message);
	}

	public void setDueTime(long dueTime) {
		this.dueTime = dueTime;
	}
}
