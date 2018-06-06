package eu.domibus.connector.controller.process;


import eu.domibus.connector.controller.exception.handling.StoreMessageExceptionIntoDatabase;
import eu.domibus.connector.persistence.service.DomibusConnectorEvidencePersistenceService;
import eu.domibus.connector.persistence.service.DomibusConnectorMessagePersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.domibus.connector.controller.exception.DomibusConnectorMessageExceptionBuilder;
import eu.domibus.connector.controller.service.DomibusConnectorBackendDeliveryService;
import eu.domibus.connector.domain.enums.DomibusConnectorEvidenceType;
import eu.domibus.connector.domain.model.DomibusConnectorMessage;
import eu.domibus.connector.domain.model.DomibusConnectorMessageConfirmation;

@Component("GatewayToBackendConfirmationProcessor")
public class GatewayToBackendConfirmationProcessor implements DomibusConnectorMessageProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(GatewayToBackendConfirmationProcessor.class);

    private DomibusConnectorMessagePersistenceService messagePersistenceService;

    private DomibusConnectorEvidencePersistenceService evidencePersistenceService;

	private DomibusConnectorBackendDeliveryService backendDeliveryService;

	@Autowired
    public void setMessagePersistenceService(DomibusConnectorMessagePersistenceService messagePersistenceService) {
        this.messagePersistenceService = messagePersistenceService;
    }

    @Autowired
    public void setEvidencePersistenceService(DomibusConnectorEvidencePersistenceService evidencePersistenceService) {
        this.evidencePersistenceService = evidencePersistenceService;
    }

    @Autowired
    public void setBackendDeliveryService(DomibusConnectorBackendDeliveryService backendDeliveryService) {
        this.backendDeliveryService = backendDeliveryService;
    }

    @Override
    @StoreMessageExceptionIntoDatabase
	public void processMessage(DomibusConnectorMessage confirmationMessage) {
		String refToMessageID = confirmationMessage.getMessageDetails().getRefToMessageId();

        DomibusConnectorMessage originalMessage = messagePersistenceService.findMessageByEbmsId(refToMessageID);
        DomibusConnectorMessageConfirmation confirmation = confirmationMessage.getMessageConfirmations().get(0);

        if (isMessageAlreadyRejected(originalMessage)) {
            messagePersistenceService.rejectMessage(originalMessage);
            throw DomibusConnectorMessageExceptionBuilder.createBuilder()
                    .setMessage(originalMessage)
                    .setText("Received evidence of type " + confirmation.getEvidenceType().toString() + 
                            " for an already rejected Message with ebms ID " + refToMessageID)
                    .setSource(this.getClass())
                    .build();	
            
        }

        originalMessage.addConfirmation(confirmation);

        evidencePersistenceService.persistEvidenceForMessageIntoDatabase(originalMessage, confirmation);


        if (originalMessage.getMessageDetails().getBackendMessageId() != null) {
            confirmationMessage.getMessageDetails().setBackendMessageId(originalMessage.getMessageDetails().getBackendMessageId());
        }
        backendDeliveryService.deliverMessageToBackend(confirmationMessage);


        boolean confirmedOrRejected = messagePersistenceService.checkMessageConfirmedOrRejected(originalMessage);
        if (!confirmedOrRejected) {
        	if (confirmation.getEvidenceType().equals(DomibusConnectorEvidenceType.RELAY_REMMD_ACCEPTANCE)
                    || confirmation.getEvidenceType().equals(DomibusConnectorEvidenceType.DELIVERY)) {
                messagePersistenceService.confirmMessage(originalMessage);
        	}
        }

        logger.info("Successfully processed evidence of type {} to originalMessage {}", confirmation.getEvidenceType(),
                originalMessage.getConnectorMessageId());

	}
	
	private boolean isMessageAlreadyRejected(DomibusConnectorMessage message) {
      if (messagePersistenceService.checkMessageRejected(message)) {
          return true;
      }
      if (message.getMessageConfirmations() != null) {
          for (DomibusConnectorMessageConfirmation confirmation : message.getMessageConfirmations()) {
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