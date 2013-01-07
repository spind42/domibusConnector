package eu.ecodex.connector.controller.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ecodex.connector.common.enums.ECodexEvidenceType;
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

public class OutgoingMessageService extends AbstractMessageService implements MessageService {

    static Logger LOGGER = LoggerFactory.getLogger(OutgoingMessageService.class);

    @Override
    public void handleMessage(Message message) throws ECodexConnectorControllerException {

        try {
            persistenceService.persistMessageIntoDatabase(message, ECodexMessageDirection.NAT_TO_GW);
        } catch (PersistenceException e1) {
            // createSubmissionRejectionAndReturnIt(message, hashValue);
            throw new ECodexConnectorControllerException(e1);
        }

        String hashValue = buildAndPersistHashValue(message);

        if (connectorProperties.isUseContentMapper()) {
            try {
                contentMapper.mapNationalToInternational(message.getMessageContent());
            } catch (ECodexConnectorContentMapperException cme) {
                createSubmissionRejectionAndReturnIt(message, hashValue);
                cme.printStackTrace();
            } catch (ImplementationMissingException ime) {
                createSubmissionRejectionAndReturnIt(message, hashValue);
                ime.printStackTrace();
            }
        }

        if (connectorProperties.isUseSecurityToolkit()) {
            // TODO: Integration of SecurityToolkit to build ASIC-S container
            // and TrustOKToken
            LOGGER.warn("SecurityToolkit not available yet! Must send message unsecure!");
        }

        MessageConfirmation confirmation = null;
        if (connectorProperties.isUseEvidencesToolkit()) {
            try {
                byte[] submissionAcceptance = evidencesToolkit.createSubmissionAcceptance(message, hashValue);
                // immediately persist new evidence into database
                persistenceService.persistEvidenceForMessageIntoDatabase(message, submissionAcceptance,
                        ECodexEvidenceType.SUBMISSION_ACCEPTANCE);

                confirmation = new MessageConfirmation(ECodexEvidenceType.SUBMISSION_ACCEPTANCE, submissionAcceptance);
            } catch (ECodexConnectorEvidencesToolkitException ete) {
                createSubmissionRejectionAndReturnIt(message, hashValue);
                throw new ECodexConnectorControllerException("Could not generate evidence for submission acceptance! ",
                        ete);
            }

        }

        try {
            gatewayWebserviceClient.sendMessage(message);
        } catch (ECodexConnectorGatewayWebserviceClientException gwse) {
            createSubmissionRejectionAndReturnIt(message, hashValue);
            throw new ECodexConnectorControllerException("Could not send ECodex Message to Gateway! ", gwse);
        }

        try {
            Message returnMessage = buildEvidenceMessage(confirmation, message);
            nationalBackendClient.deliverLastEvidenceForMessage(returnMessage);
        } catch (ECodexConnectorNationalBackendClientException e) {
            LOGGER.error("Could not send submission acceptance back to national connector! ", e);
            e.printStackTrace();
        } catch (ImplementationMissingException e) {
            LOGGER.error("Could not send submission acceptance back to national connector! ", e);
            e.printStackTrace();
        }

    }

    private String buildAndPersistHashValue(Message message) {
        // whatever the source for the hash will be - by now it is the pdf
        // document
        String hash = hashValueBuilder.buildHashValueAsString(message.getMessageContent().getPdfDocument());

        // now persist the hash value into the database entry for the
        // message
        message.getDbMessage().setHashValue(hash);
        persistenceService.mergeMessageWithDatabase(message);

        return hash;
    }

    private void createSubmissionRejectionAndReturnIt(Message message, String hashValue)
            throws ECodexConnectorControllerException {
        try {
            byte[] submissionRejection = evidencesToolkit.createSubmissionRejection(RejectionReason.OTHER, message,
                    hashValue);

            // immediately persist new evidence into database
            persistenceService.persistEvidenceForMessageIntoDatabase(message, submissionRejection,
                    ECodexEvidenceType.SUBMISSION_REJECTION);

            MessageConfirmation confirmation = new MessageConfirmation(ECodexEvidenceType.SUBMISSION_REJECTION,
                    submissionRejection);

            Message returnMessage = buildEvidenceMessage(confirmation, message);
            nationalBackendClient.deliverLastEvidenceForMessage(returnMessage);
        } catch (ECodexConnectorEvidencesToolkitException e) {
            LOGGER.error("Could not even generate submission rejection! ", e);
            return;
        } catch (ECodexConnectorNationalBackendClientException e) {
            throw new ECodexConnectorControllerException("Exception while trying to send submission rejection. ", e);
        } catch (ImplementationMissingException ime) {
            throw new ECodexConnectorControllerException("Exception while trying to send submission rejection. ", ime);
        }

    }

    private Message buildEvidenceMessage(MessageConfirmation confirmation, Message originalMessage) {
        MessageDetails details = new MessageDetails();
        details.setRefToMessageId(originalMessage.getMessageDetails().getNationalMessageId());

        Message returnMessage = new Message(details, confirmation);

        return returnMessage;
    }

}
