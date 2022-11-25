package eu.domibus.connector.controller.processor;

import eu.ecodex.dc5.flow.steps.MessageConfirmationStep;
import eu.ecodex.dc5.flow.steps.SubmitConfirmationAsEvidenceMessageStep;
import eu.ecodex.dc5.flow.steps.ValidateMessageConfirmationStep;
import eu.ecodex.dc5.message.FindBusinessMessageByMsgId;
import eu.domibus.connector.controller.queues.producer.ToCleanupQueue;
//import eu.domibus.connector.domain.enums.MessageTargetSource;
//import eu.domibus.connector.domain.model.DomibusConnectorBusinessDomain;
//import eu.domibus.connector.domain.model.DomibusConnectorMessage;
//import eu.domibus.connector.domain.model.DomibusConnectorMessageConfirmation;
import eu.ecodex.dc5.flow.steps.EvidenceTriggerStep;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvidenceMessageProcessor {

    private static final Logger LOGGER = LogManager.getLogger(EvidenceMessageProcessor.class);

    private final EvidenceTriggerStep evidenceTriggerStep;
    private final FindBusinessMessageByMsgId findBusinessMessageByMsgId;
    private final MessageConfirmationStep messageConfirmationStep;
    private final ValidateMessageConfirmationStep validateMessageConfirmationStep;
    private final SubmitConfirmationAsEvidenceMessageStep submitConfirmationAsEvidenceMessageStep;
    private final ToCleanupQueue cleanupQueue;

//    @MDC(name = LoggingMDCPropertyNames.MDC_DC_MESSAGE_PROCESSOR_PROPERTY_NAME, value = "EvidenceMessageProcessor")
//    public void processMessage(DomibusConnectorMessage message) {
//        try {
//            boolean isEvidenceTrigger = DomainModelHelper.isEvidenceTriggerMessage(message);
//            if (isEvidenceTrigger) {
//                evidenceTriggerStep.executeStep(message);
//            }
//
//            DomibusConnectorMessageDirection revertedDirection = DomibusConnectorMessageDirection.revert(message.getMessageDetails().getDirection());
//            DomibusConnectorMessage businessMsg = findBusinessMessageByMsgId.findBusinessMessageByIdAndDirection(message, revertedDirection);
//
//            //set ref to backend message id (backendId of business message)
//            String businessMessageBackendId = businessMsg.getMessageDetails().getBackendMessageId();
//            LOGGER.debug("Setting refToBackendMessageId to [{}]", businessMessageBackendId);
//            message.getMessageDetails().setRefToBackendMessageId(businessMessageBackendId);
//
//            //set ref to message id (EBMSID of business message)
//            String businessMessageEbmsId = businessMsg.getMessageDetails().getEbmsMessageId();
//            LOGGER.debug("Setting refToMessageId to [{}]", businessMessageEbmsId);
//            message.getMessageDetails().setRefToMessageId(businessMessageEbmsId);
//
//
//            validateMessageConfirmationStep.executeStep(message);
//
//            DomibusConnectorMessageConfirmation transportedConfirmation = message.getTransportedMessageConfirmations().get(0);
//
//            messageConfirmationStep.processConfirmationForMessage(businessMsg, transportedConfirmation);
//
//            //if business message is rejected, confirmed trigger cleanup routine
//            if (businessMsg.getMessageDetails().getConfirmed() != null || businessMsg.getMessageDetails().getRejected() != null) {
//                cleanupQueue.putOnQueue(businessMsg);
//            }
//
//            submitConfirmationAsEvidenceMessageStep.submitOppositeDirection(message.getConnectorMessageId(), businessMsg, transportedConfirmation);
//
//            if (isEvidenceTrigger && submitConfirmationAsEvidenceMessageStep.isSendCreatedTriggerEvidenceBack(businessMsg.getMessageLaneId())) {
//                //send generated evidence back...this would be the same direction as the business message...with new messageid
//                LOGGER.debug("Sending by trigger created confirmation message back to backend");
//                submitConfirmationAsEvidenceMessageStep.submitSameDirection(null, businessMsg, transportedConfirmation);
//            }
//
//            LOGGER.info(LoggingMarker.Log4jMarker.BUSINESS_LOG, "Successfully processed evidence [{}] in direction [{}] for business message [{}]",
//                    transportedConfirmation.getEvidenceType(),
//                    message.getMessageDetails().getDirection(),
//                    businessMsg.getConnectorMessageId());
//        } catch (DCEvidenceNotRelevantException dcEvidenceNotRelevantException) {
//            LOGGER.warn(LoggingMarker.Log4jMarker.BUSINESS_LOG, dcEvidenceNotRelevantException.getMessage());
//            LOGGER.debug(dcEvidenceNotRelevantException.getMessage(), dcEvidenceNotRelevantException);
//        }
//
//    }

}