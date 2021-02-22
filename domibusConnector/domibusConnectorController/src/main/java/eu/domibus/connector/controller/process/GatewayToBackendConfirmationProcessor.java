package eu.domibus.connector.controller.process;


import eu.domibus.connector.controller.exception.handling.StoreMessageExceptionIntoDatabase;
import eu.domibus.connector.controller.processor.DomibusConnectorMessageProcessor;
import eu.domibus.connector.controller.processor.steps.MessageConfirmationStep;
import eu.domibus.connector.domain.enums.DomibusConnectorMessageDirection;
import eu.domibus.connector.lib.logging.MDC;
import eu.domibus.connector.persistence.service.DCMessagePersistenceService;
import eu.domibus.connector.tools.LoggingMDCPropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.domibus.connector.controller.exception.DomibusConnectorMessageExceptionBuilder;
import eu.domibus.connector.domain.enums.DomibusConnectorEvidenceType;
import eu.domibus.connector.domain.model.DomibusConnectorMessage;
import eu.domibus.connector.domain.model.DomibusConnectorMessageConfirmation;

@Component(GatewayToBackendConfirmationProcessor.GW_TO_BACKEND_CONFIRMATION_PROCESSOR)
public class GatewayToBackendConfirmationProcessor implements DomibusConnectorMessageProcessor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GatewayToBackendConfirmationProcessor.class);

	public static final String GW_TO_BACKEND_CONFIRMATION_PROCESSOR = "GatewayToBackendConfirmationProcessor";

    private DCMessagePersistenceService messagePersistenceService;
    private MessageConfirmationStep messageConfirmationStep;
//	private DomibusConnectorBackendDeliveryService backendDeliveryService;

	@Autowired
    public void setMessagePersistenceService(DCMessagePersistenceService messagePersistenceService) {
        this.messagePersistenceService = messagePersistenceService;
    }

    @Autowired
    public void setMessageConfirmationProcessor(MessageConfirmationStep messageConfirmationStep) {
        this.messageConfirmationStep = messageConfirmationStep;
    }

//    @Autowired
//    public void setBackendDeliveryService(DomibusConnectorBackendDeliveryService backendDeliveryService) {
//        this.backendDeliveryService = backendDeliveryService;
//    }

    @Override
    @StoreMessageExceptionIntoDatabase
    @MDC(name = LoggingMDCPropertyNames.MDC_DC_MESSAGE_PROCESSOR_PROPERTY_NAME, value = GW_TO_BACKEND_CONFIRMATION_PROCESSOR)
	public void processMessage(DomibusConnectorMessage confirmationMessage) {
		String refToMessageID = confirmationMessage.getMessageDetails().getRefToMessageId();

        DomibusConnectorMessage originalMessage = messagePersistenceService
                .findMessageByEbmsIdAndDirection(refToMessageID, DomibusConnectorMessageDirection.BACKEND_TO_GATEWAY)
                .get();

        DomibusConnectorMessageConfirmation confirmation = confirmationMessage.getTransportedMessageConfirmations().get(0);

        if (isMessageAlreadyRejected(originalMessage)) {
            throw DomibusConnectorMessageExceptionBuilder.createBuilder()
                    .setMessage(originalMessage)
                    .setText("Received evidence of type " + confirmation.getEvidenceType() +
                            " for an already in database marked as rejected Message with ebms ID " + refToMessageID)
                    .setSource(this.getClass())
                    .build();	
            
        }
//        if (containsRejectionConfirmation(originalMessage)) {
//            LOGGER.info(LoggingMarker.BUSINESS_LOG, "Confirmation message received of type [{}] - putting message into rejected state", confirmation.getEvidenceType());
//            messagePersistenceService.rejectMessage(originalMessage);
//        }

        originalMessage.addTransportedMessageConfirmation(confirmation);

//        evidencePersistenceService.persistEvidenceForMessageIntoDatabase(originalMessage,
//                confirmation,
//                new DomibusConnectorMessageId(confirmationMessage.getConnectorMessageIdAsString()));


        DomibusConnectorEvidenceType evidenceType = confirmation.getEvidenceType();
//        CommonConfirmationProcessor commonConfirmationProcessor = new CommonConfirmationProcessor(messagePersistenceService);
        messageConfirmationStep.confirmRejectMessage(evidenceType, originalMessage);

        if (originalMessage.getMessageDetails().getBackendMessageId() != null) {
            confirmationMessage.getMessageDetails().setRefToBackendMessageId(originalMessage.getMessageDetails().getBackendMessageId());
        }
//        backendDeliveryService.deliverMessageToBackend(confirmationMessage);


        LOGGER.info("Successfully processed evidence of type {} to originalMessage {}", confirmation.getEvidenceType(),
                originalMessage.getConnectorMessageIdAsString());

	}
	
	private boolean isMessageAlreadyRejected(DomibusConnectorMessage message) {
      if (messagePersistenceService.checkMessageRejected(message)) {
          return true;
      }
      return false;
    }

    /**
     *
     *
     * @return returns false if the message contains an evidence/confirmation which
     * requires to put the message in rejected state. These evidences are:
     * <ul>
     *     <li>RELAY_REMMD_REJECTION</li>
     *     <li>NON_DELIVERY</li>
     *     <li>NON_RETRIEVAL</li>
     * </ul>
     */
    private boolean containsRejectionConfirmation(DomibusConnectorMessage message) {
        if (message.getTransportedMessageConfirmations() != null) {
            for (DomibusConnectorMessageConfirmation confirmation : message.getTransportedMessageConfirmations()) {
                if (confirmation.getEvidenceType().equals(DomibusConnectorEvidenceType.RELAY_REMMD_REJECTION)
                        || confirmation.getEvidenceType().equals(DomibusConnectorEvidenceType.NON_DELIVERY)
                        || confirmation.getEvidenceType().equals(DomibusConnectorEvidenceType.NON_RETRIEVAL)) {
                    messagePersistenceService.rejectMessage(message);
                    return true;
                }
            }
        }
        return false;
    }

}
