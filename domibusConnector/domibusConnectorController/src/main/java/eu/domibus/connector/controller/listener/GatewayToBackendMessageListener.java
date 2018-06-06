package eu.domibus.connector.controller.listener;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.domibus.connector.controller.process.DomibusConnectorMessageProcessor;

import static eu.domibus.connector.controller.spring.ControllerContext.NON_TRANSACTED_JMS_LISTENER_CONTAINER_FACTORY_BEAN_NAME;

@Component("GatewayToBackendMessageListener")
public class GatewayToBackendMessageListener extends AbstractControllerMessageListener implements MessageListener {

	private static final String QUEUE_NAME = "domibus.connector.internal.gateway.to.controller.queue";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BackendToGatewayMessageListener.class);
	
	@Resource(name="GatewayToBackendMessageProcessor")
	private DomibusConnectorMessageProcessor messageProcessor;
	
	@Resource(name="GatewayToBackendConfirmationProcessor")
	private DomibusConnectorMessageProcessor confirmationProcessor;
	
	@Override
	@JmsListener(destination = "${"+ QUEUE_NAME +"}", containerFactory = NON_TRANSACTED_JMS_LISTENER_CONTAINER_FACTORY_BEAN_NAME)
	public void onMessage(Message message) {
		handleMessage(message);
	}

	@Override
	Logger getLogger() {
		return LOGGER;
	}

	@Override
	String getQueueName() {
		return QUEUE_NAME;
	}

	@Override
	DomibusConnectorMessageProcessor getMessageProcessor() {
		return messageProcessor;
	}

	@Override
	DomibusConnectorMessageProcessor getConfirmationProcessor() {
		return confirmationProcessor;
	}

}