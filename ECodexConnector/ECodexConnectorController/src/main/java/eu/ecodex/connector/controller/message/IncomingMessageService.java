package eu.ecodex.connector.controller.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ecodex.connector.common.enums.ActionEnum;
import eu.ecodex.connector.common.enums.ECodexMessageDirection;
import eu.ecodex.connector.common.exception.ImplementationMissingException;
import eu.ecodex.connector.common.exception.PersistenceException;
import eu.ecodex.connector.common.message.Message;
import eu.ecodex.connector.common.message.MessageConfirmation;
import eu.ecodex.connector.common.message.MessageDetails;
import eu.ecodex.connector.controller.exception.ECodexConnectorControllerException;
import eu.ecodex.connector.evidences.exception.ECodexConnectorEvidencesToolkitException;
import eu.ecodex.connector.evidences.type.RejectionReason;
import eu.ecodex.connector.gwc.exception.ECodexConnectorGatewayWebserviceClientException;
import eu.ecodex.connector.mapping.exception.ECodexConnectorContentMapperException;
import eu.ecodex.connector.nbc.exception.ECodexConnectorNationalBackendClientException;
import eu.ecodex.connector.security.exception.ECodexConnectorSecurityException;

public class IncomingMessageService extends AbstractMessageService implements MessageService {

    static Logger LOGGER = LoggerFactory.getLogger(IncomingMessageService.class);

    @Override
    public void handleMessage(Message message) throws ECodexConnectorControllerException {

        try {
            persistenceService.persistMessageIntoDatabase(message, ECodexMessageDirection.GW_TO_NAT);
        } catch (PersistenceException e1) {
            createRelayREMMDEvidenceAndSendIt(message, false);
            throw new ECodexConnectorControllerException(e1);
        }

        if (connectorProperties.isUseEvidencesToolkit()) {
            createRelayREMMDEvidenceAndSendIt(message, true);
        }

        if (connectorProperties.isUseSecurityToolkit()) {
            try {
                securityToolkit.validateContainer(message);
            } catch (ECodexConnectorSecurityException e) {
                createNonDeliveryEvidenceAndSendIt(message);
                throw e;
            }
        }

        if (connectorProperties.isUseContentMapper()) {
            try {
                contentMapper.mapInternationalToNational(message);
            } catch (ECodexConnectorContentMapperException e) {
                createNonDeliveryEvidenceAndSendIt(message);
                throw new ECodexConnectorControllerException("Error mapping content of message into national format!",
                        e);
            } catch (ImplementationMissingException e) {
                createNonDeliveryEvidenceAndSendIt(message);
                throw new ECodexConnectorControllerException(e);
            }
        }

        try {
            nationalBackendClient.deliverMessage(message);
        } catch (ECodexConnectorNationalBackendClientException e) {
            createNonDeliveryEvidenceAndSendIt(message);
            throw new ECodexConnectorControllerException("Error delivering message to national backend client!", e);
        } catch (ImplementationMissingException e) {
            createNonDeliveryEvidenceAndSendIt(message);
            throw new ECodexConnectorControllerException(e);
        }

        persistenceService.setMessageDeliveredToNationalSystem(message);

    }

    private void createNonDeliveryEvidenceAndSendIt(Message originalMessage) throws ECodexConnectorControllerException {
        MessageConfirmation nonDelivery = null;
        try {

            nonDelivery = evidencesToolkit.createNonDeliveryEvidence(RejectionReason.OTHER, originalMessage);
        } catch (ECodexConnectorEvidencesToolkitException e) {
            throw new ECodexConnectorControllerException("Error creating NonDelivery evidence for message!", e);
        }

        sendEvidenceToBackToGateway(originalMessage, ActionEnum.DeliveryNonDeliveryToRecipient, nonDelivery);
    }

    private void createRelayREMMDEvidenceAndSendIt(Message originalMessage, boolean isAcceptance)
            throws ECodexConnectorControllerException {
        MessageConfirmation messageConfirmation = null;
        try {
            messageConfirmation = isAcceptance ? evidencesToolkit.createRelayREMMDAcceptance(originalMessage)
                    : evidencesToolkit.createRelayREMMDRejection(RejectionReason.OTHER, originalMessage);
        } catch (ECodexConnectorEvidencesToolkitException e) {
            throw new ECodexConnectorControllerException("Error creating RelayREMMD evidence for message!", e);
        }

        sendEvidenceToBackToGateway(originalMessage, ActionEnum.RelayREMMDAcceptanceRejection, messageConfirmation);
    }

    private void sendEvidenceToBackToGateway(Message originalMessage, ActionEnum action,
            MessageConfirmation messageConfirmation) {

        originalMessage.addConfirmation(messageConfirmation);
        persistenceService.persistEvidenceForMessageIntoDatabase(originalMessage, messageConfirmation.getEvidence(),
                messageConfirmation.getEvidenceType());

        MessageDetails details = new MessageDetails();
        details.setRefToMessageId(originalMessage.getMessageDetails().getEbmsMessageId());
        details.setConversationId(originalMessage.getMessageDetails().getConversationId());
        details.setService(originalMessage.getMessageDetails().getService());
        details.setAction(action);
        details.setFromPartner(originalMessage.getMessageDetails().getToPartner());
        details.setToPartner(originalMessage.getMessageDetails().getFromPartner());

        Message evidenceMessage = new Message(details, messageConfirmation);

        try {
            gatewayWebserviceClient.sendMessage(evidenceMessage);
        } catch (ECodexConnectorGatewayWebserviceClientException e) {
            LOGGER.error("Exception sending evidence back to sender gateway of message "
                    + originalMessage.getMessageDetails().getEbmsMessageId(), e);
        }

        persistenceService.setEvidenceDeliveredToGateway(originalMessage, messageConfirmation.getEvidenceType());
    }

}