package eu.domibus.connector.controller.processor.confirmation;

import eu.domibus.connector.controller.exception.DomibusConnectorControllerException;
import eu.domibus.connector.controller.exception.DomibusConnectorMessageException;
import eu.ecodex.dc5.flow.steps.MessageConfirmationStep;
import eu.domibus.connector.controller.processor.steps.SubmitConfirmationAsEvidenceMessageStep;
import eu.ecodex.dc5.message.ConfirmationCreatorService;
import eu.domibus.connector.domain.enums.DomibusConnectorRejectionReason;
import eu.ecodex.dc5.message.model.DC5Message;
import eu.ecodex.dc5.message.model.DC5Confirmation;
import eu.domibus.connector.lib.logging.MDC;
import eu.domibus.connector.tools.LoggingMDCPropertyNames;
import eu.domibus.connector.tools.logging.LoggingMarker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateEvidenceTimeoutConfirmationStep {

    private static final Logger LOGGER = LogManager.getLogger(CreateEvidenceTimeoutConfirmationStep.class);

    private final ConfirmationCreatorService confirmationCreatorService;
    private final SubmitConfirmationAsEvidenceMessageStep submitConfirmationAsEvidenceMessageStep;
    private final MessageConfirmationStep messageConfirmationStep;

    public CreateEvidenceTimeoutConfirmationStep(ConfirmationCreatorService confirmationCreatorService,
                                                 SubmitConfirmationAsEvidenceMessageStep submitConfirmationAsEvidenceMessageStep,
                                                 MessageConfirmationStep messageConfirmationStep) {
        this.confirmationCreatorService = confirmationCreatorService;
        this.submitConfirmationAsEvidenceMessageStep = submitConfirmationAsEvidenceMessageStep;
        this.messageConfirmationStep = messageConfirmationStep;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @MDC(name = LoggingMDCPropertyNames.MDC_DC_MESSAGE_PROCESSOR_PROPERTY_NAME, value = "CreateRelayRemmdFailureAndSendIt")
    public void createRelayRemmdFailureAndSendIt(DC5Message originalMessage) throws DomibusConnectorControllerException,
            DomibusConnectorMessageException {

        LOGGER.error(LoggingMarker.Log4jMarker.BUSINESS_LOG, "The RelayREMMDAcceptance/Rejection evidence timeout for originalMessage {} timed out. Sending RELAY_REMMD_FAILURE to backend!", originalMessage
                .getEbmsData().getEbmsMessageId());

        DC5Confirmation nonDelivery = confirmationCreatorService.createRelayRemmdFailure(originalMessage, DomibusConnectorRejectionReason.RELAY_REMMD_TIMEOUT);
        processConfirmationForMessage(originalMessage, nonDelivery);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @MDC(name = LoggingMDCPropertyNames.MDC_DC_MESSAGE_PROCESSOR_PROPERTY_NAME, value = "CreateNonDeliveryAndSendIt")
    public void createNonDeliveryAndSendIt(DC5Message originalMessage) throws DomibusConnectorControllerException,
            DomibusConnectorMessageException {

        LOGGER.error(LoggingMarker.Log4jMarker.BUSINESS_LOG, "The Delivery/NonDelivery evidence timeout for originalMessage {} timed out. Sending NonDelivery to backend!", originalMessage
                .getEbmsData().getEbmsMessageId());

        DC5Confirmation nonDelivery = confirmationCreatorService.createNonDelivery(originalMessage, DomibusConnectorRejectionReason.DELIVERY_EVIDENCE_TIMEOUT);
        processConfirmationForMessage(originalMessage, nonDelivery);
    }

    private void processConfirmationForMessage(DC5Message originalMessage, DC5Confirmation confirmation) {
        //process confirmation for business message (store confirmation, reject business message)
        messageConfirmationStep.processConfirmationForMessage(originalMessage, confirmation);
        //send confirmation to backend
        submitConfirmationAsEvidenceMessageStep.submitOppositeDirection(null, originalMessage, confirmation);
    }
}
