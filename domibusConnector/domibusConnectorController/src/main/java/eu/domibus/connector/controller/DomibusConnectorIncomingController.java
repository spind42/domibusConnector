package eu.domibus.connector.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.domibus.connector.common.exception.DomibusConnectorMessageException;
import eu.domibus.connector.common.message.Message;
import eu.domibus.connector.controller.exception.DomibusConnectorControllerException;
import eu.domibus.connector.controller.service.EvidenceService;
import eu.domibus.connector.controller.service.MessageService;
import eu.domibus.connector.gwc.DomibusConnectorGatewayWebserviceClient;
import eu.domibus.connector.gwc.exception.DomibusConnectorGatewayWebserviceClientException;

public class DomibusConnectorIncomingController implements DomibusConnectorController {

    static Logger LOGGER = LoggerFactory.getLogger(DomibusConnectorIncomingController.class);

    private DomibusConnectorGatewayWebserviceClient gatewayWebserviceClient;
    private MessageService incomingMessageService;
    private EvidenceService incomingEvidenceService;

    public void setGatewayWebserviceClient(DomibusConnectorGatewayWebserviceClient gatewayWebserviceClient) {
        this.gatewayWebserviceClient = gatewayWebserviceClient;
    }

    public void setIncomingMessageService(MessageService incomingMessageService) {
        this.incomingMessageService = incomingMessageService;
    }

    public void setIncomingEvidenceService(EvidenceService incomingEvidenceService) {
        this.incomingEvidenceService = incomingEvidenceService;
    }

    @Override
    public void execute() throws DomibusConnectorControllerException {
        LOGGER.debug("Job for handling incoming messages triggered.");
        Date start = new Date();

        String[] messageIDs = null;
        try {
            messageIDs = gatewayWebserviceClient.listPendingMessages();
        } catch (DomibusConnectorGatewayWebserviceClientException e) {
            throw new DomibusConnectorControllerException(e);
        }

        if (messageIDs != null && messageIDs.length > 0) {
            LOGGER.info("Found {} incoming messages on gateway to handle...", messageIDs.length);
            for (String messageId : messageIDs) {
                try {
                    handleMessage(messageId);
                } catch (DomibusConnectorControllerException e) {
                    LOGGER.error("Error handling message with id " + messageId, e);
                }
            }
        } else {
            LOGGER.debug("No pending messages on gateway!");
        }

        LOGGER.debug("Job for handling incoming messages finished in {} ms.",
                (System.currentTimeMillis() - start.getTime()));
    }

    private void handleMessage(String messageId) throws DomibusConnectorControllerException {
        Message message = null;
        try {
            message = gatewayWebserviceClient.downloadMessage(messageId);
        } catch (DomibusConnectorGatewayWebserviceClientException e) {
            throw new DomibusConnectorControllerException("Error downloading message with id " + messageId
                    + " from the gateway!", e);
        }

        if (isMessageEvidence(message)) {
            try {
                incomingEvidenceService.handleEvidence(message);
            } catch (DomibusConnectorMessageException | DomibusConnectorControllerException e) {
                LOGGER.error("Error handling message with id " + messageId, e);
            }
        } else {
            try {
                incomingMessageService.handleMessage(message);
            } catch (DomibusConnectorControllerException | DomibusConnectorMessageException e) {
                LOGGER.error("Error handling message with id " + messageId, e);
            }
        }
    }

    private boolean isMessageEvidence(Message message) {
        return message.getMessageDetails().getAction().getAction().equals("RelayREMMDAcceptanceRejection")
                || message.getMessageDetails().getAction().getAction().equals("DeliveryNonDeliveryToRecipient")
                || message.getMessageDetails().getAction().getAction().equals("RetrievalNonRetrievalToRecipient");
    }

}