package eu.domibus.connector.controller.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.domibus.connector.common.db.model.DomibusConnectorAction;
import eu.domibus.connector.common.enums.MessageDirection;
import eu.domibus.connector.common.exception.DomibusConnectorMessageException;
import eu.domibus.connector.common.exception.ImplementationMissingException;
import eu.domibus.connector.common.exception.PersistenceException;
import eu.domibus.connector.common.message.Message;
import eu.domibus.connector.common.message.MessageConfirmation;
import eu.domibus.connector.common.message.MessageDetails;
import eu.domibus.connector.controller.exception.DomibusConnectorControllerException;
import eu.domibus.connector.evidences.exception.DomibusConnectorEvidencesToolkitException;
import eu.domibus.connector.evidences.type.RejectionReason;
import eu.domibus.connector.gwc.exception.DomibusConnectorGatewayWebserviceClientException;
import eu.domibus.connector.mapping.exception.DomibusConnectorContentMapperException;
import eu.domibus.connector.nbc.exception.DomibusConnectorNationalBackendClientException;
import eu.domibus.connector.security.exception.DomibusConnectorSecurityException;

public class IncomingMessageService extends AbstractMessageService implements MessageService {

    static Logger LOGGER = LoggerFactory.getLogger(IncomingMessageService.class);

    @Override
    public void handleMessage(Message message) throws DomibusConnectorControllerException,
            DomibusConnectorMessageException {

        try {
            persistenceService.persistMessageIntoDatabase(message, MessageDirection.GW_TO_NAT);
        } catch (PersistenceException e1) {
            createRelayREMMDEvidenceAndSendIt(message, false);
            LOGGER.error("Message could not be persisted!", e1);
            return;
        }

        if (connectorProperties.isUseEvidencesToolkit()) {
            createRelayREMMDEvidenceAndSendIt(message, true);
        }

        if (connectorProperties.isUseSecurityToolkit()) {
            try {
                securityToolkit.validateContainer(message);
            } catch (DomibusConnectorSecurityException e) {
                createNonDeliveryEvidenceAndSendIt(message);
                throw e;
            }
        }

        if (connectorProperties.isUseContentMapper()) {
            try {
                contentMapper.mapInternationalToNational(message);
            } catch (DomibusConnectorContentMapperException e) {
                createNonDeliveryEvidenceAndSendIt(message);
                throw new DomibusConnectorMessageException(message,
                        "Error mapping content of message into national format!", e, this.getClass());
            } catch (ImplementationMissingException e) {
                createNonDeliveryEvidenceAndSendIt(message);
                throw new DomibusConnectorMessageException(message, e.getMessage(), e, this.getClass());
            }
            persistenceService.mergeMessageWithDatabase(message);
        }

        try {
            nationalBackendClient.deliverMessage(message);
        } catch (DomibusConnectorNationalBackendClientException e) {
            createNonDeliveryEvidenceAndSendIt(message);
            throw new DomibusConnectorMessageException(message, "Error delivering message to national backend client!",
                    e, this.getClass());
        } catch (ImplementationMissingException e) {
            createNonDeliveryEvidenceAndSendIt(message);
            throw new DomibusConnectorMessageException(message, e.getMessage(), e, this.getClass());
        }

        persistenceService.setMessageDeliveredToNationalSystem(message);

        LOGGER.info("Successfully processed message with id {} from GW to NAT.", message.getDbMessage().getId());

    }

    private void createNonDeliveryEvidenceAndSendIt(Message originalMessage)
            throws DomibusConnectorControllerException, DomibusConnectorMessageException {

        MessageConfirmation nonDelivery = null;
        try {

            nonDelivery = evidencesToolkit.createNonDeliveryEvidence(RejectionReason.OTHER, originalMessage);
        } catch (DomibusConnectorEvidencesToolkitException e) {
            throw new DomibusConnectorMessageException(originalMessage,
                    "Error creating NonDelivery evidence for message!", e, this.getClass());
        }

        DomibusConnectorAction action = persistenceService.getDeliveryNonDeliveryToRecipientAction();

        sendEvidenceToBackToGateway(originalMessage, action, nonDelivery);

        persistenceService.rejectMessage(originalMessage);
    }

    private void createRelayREMMDEvidenceAndSendIt(Message originalMessage, boolean isAcceptance)
            throws DomibusConnectorControllerException, DomibusConnectorMessageException {
        MessageConfirmation messageConfirmation = null;
        try {
            messageConfirmation = isAcceptance ? evidencesToolkit.createRelayREMMDAcceptance(originalMessage)
                    : evidencesToolkit.createRelayREMMDRejection(RejectionReason.OTHER, originalMessage);
        } catch (DomibusConnectorEvidencesToolkitException e) {
            throw new DomibusConnectorMessageException(originalMessage,
                    "Error creating RelayREMMD evidence for message!", e, this.getClass());
        }

        DomibusConnectorAction action = persistenceService.getRelayREMMDAcceptanceRejectionAction();

        sendEvidenceToBackToGateway(originalMessage, action, messageConfirmation);

        if (!isAcceptance) {
            persistenceService.rejectMessage(originalMessage);
        }
    }

    private void sendEvidenceToBackToGateway(Message originalMessage, DomibusConnectorAction action,
            MessageConfirmation messageConfirmation) throws DomibusConnectorControllerException,
            DomibusConnectorMessageException {

        originalMessage.addConfirmation(messageConfirmation);
        persistenceService.persistEvidenceForMessageIntoDatabase(originalMessage, messageConfirmation.getEvidence(),
                messageConfirmation.getEvidenceType());

        MessageDetails details = new MessageDetails();
        details.setRefToMessageId(originalMessage.getMessageDetails().getEbmsMessageId());
        details.setConversationId(originalMessage.getMessageDetails().getConversationId());
        details.setService(originalMessage.getMessageDetails().getService());
        details.setAction(action);
        details.setFromParty(originalMessage.getMessageDetails().getToParty());
        details.setToParty(originalMessage.getMessageDetails().getFromParty());

        Message evidenceMessage = new Message(details, messageConfirmation);

        try {
            gatewayWebserviceClient.sendMessage(evidenceMessage);
        } catch (DomibusConnectorGatewayWebserviceClientException e) {
            throw new DomibusConnectorMessageException(originalMessage,
                    "Exception sending evidence back to sender gateway of message "
                            + originalMessage.getMessageDetails().getEbmsMessageId(), e, this.getClass());
        }

        persistenceService.setEvidenceDeliveredToGateway(originalMessage, messageConfirmation.getEvidenceType());
    }
}