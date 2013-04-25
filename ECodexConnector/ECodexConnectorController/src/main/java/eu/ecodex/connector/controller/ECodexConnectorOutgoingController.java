package eu.ecodex.connector.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ecodex.connector.common.exception.ImplementationMissingException;
import eu.ecodex.connector.common.message.Message;
import eu.ecodex.connector.common.message.MessageContent;
import eu.ecodex.connector.common.message.MessageDetails;
import eu.ecodex.connector.controller.exception.ECodexConnectorControllerException;
import eu.ecodex.connector.controller.message.EvidenceService;
import eu.ecodex.connector.controller.message.MessageService;
import eu.ecodex.connector.nbc.ECodexConnectorNationalBackendClient;
import eu.ecodex.connector.nbc.exception.ECodexConnectorNationalBackendClientException;

public class ECodexConnectorOutgoingController implements ECodexConnectorController {

    static Logger LOGGER = LoggerFactory.getLogger(ECodexConnectorOutgoingController.class);

    private ECodexConnectorNationalBackendClient nationalBackendClient;
    private MessageService outgoingMessageService;
    private EvidenceService outgoingEvidenceService;

    public void setNationalBackendClient(ECodexConnectorNationalBackendClient nationalBackendClient) {
        this.nationalBackendClient = nationalBackendClient;
    }

    public void setOutgoingMessageService(MessageService outgoingMessageService) {
        this.outgoingMessageService = outgoingMessageService;
    }

    public void setOutgoingEvidenceService(EvidenceService outgoingEvidenceService) {
        this.outgoingEvidenceService = outgoingEvidenceService;
    }

    @Override
    public void handleMessages() throws ECodexConnectorControllerException {
        String[] messages = null;
        try {
            messages = nationalBackendClient.requestMessagesUnsent();
        } catch (ECodexConnectorNationalBackendClientException nbce) {
            throw new ECodexConnectorControllerException(
                    "Exception while trying to get messages list from national system. ", nbce);
        } catch (ImplementationMissingException ime) {
            throw new ECodexConnectorControllerException(
                    "Exception while trying to get messages list from national system. ", ime);
        }

        if (messages != null && messages.length > 0) {

            for (String messageId : messages) {

                try {
                    handleMessage(messageId);
                } catch (ECodexConnectorControllerException ce) {
                    LOGGER.error(ce.getMessage());
                    throw ce;
                }
            }
        } else {
            LOGGER.info("There are no unsent outgoing messages on national system!");
        }
    }

    private void handleMessage(String messageId) throws ECodexConnectorControllerException {
        MessageDetails details = new MessageDetails();
        details.setNationalMessageId(messageId);

        MessageContent content = new MessageContent();

        Message message = new Message(details, content);

        try {
            nationalBackendClient.requestMessage(message);
        } catch (ECodexConnectorNationalBackendClientException e1) {
            throw new ECodexConnectorControllerException(
                    "Exception while trying to receive message from national system. ", e1);
        } catch (ImplementationMissingException ime) {
            throw new ECodexConnectorControllerException(
                    "Exception while trying to receive message from national system. ", ime);
        }

        try {
            outgoingMessageService.handleMessage(message);
        } catch (ECodexConnectorControllerException ce) {
            LOGGER.error(ce.getMessage());
            throw ce;
        }
    }

    @Override
    public void handleEvidences() throws ECodexConnectorControllerException {
        LOGGER.info("Started to check national implementation for pending confirmations!");

        Message[] confirmationMessages = null;
        try {
            confirmationMessages = nationalBackendClient.requestConfirmations();
        } catch (ECodexConnectorNationalBackendClientException e) {
            throw new ECodexConnectorControllerException(
                    "Exception while trying to get confirmations from national system. ", e);
        } catch (ImplementationMissingException e) {
            throw new ECodexConnectorControllerException(
                    "Exception while trying to get confirmations from national system. ", e);
        }

        if (confirmationMessages != null && confirmationMessages.length > 0) {

            for (Message confirmationMessage : confirmationMessages) {

                try {
                    outgoingEvidenceService.handleEvidence(confirmationMessage);
                } catch (ECodexConnectorControllerException ce) {
                    LOGGER.error(ce.getMessage());
                    throw ce;
                }
            }
        }
    }

}