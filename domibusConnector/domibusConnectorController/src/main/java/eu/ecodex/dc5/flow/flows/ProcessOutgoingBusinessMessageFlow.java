package eu.ecodex.dc5.flow.flows;

import eu.domibus.connector.controller.processor.DomibusConnectorMessageProcessor;
import eu.domibus.connector.controller.spring.ConnectorMessageProcessingProperties;
import eu.domibus.connector.domain.enums.MessageTargetSource;
import eu.domibus.connector.domain.enums.TransportState;
import eu.ecodex.dc5.flow.api.StepFailedException;
import eu.ecodex.dc5.flow.events.MessageReadyForTransportEvent;
import eu.ecodex.dc5.flow.events.NewMessageStoredEvent;
import eu.ecodex.dc5.flow.events.OutgoingBusinessMessageTransportEvent;
import eu.ecodex.dc5.flow.steps.*;
import eu.ecodex.dc5.message.ConfirmationCreatorService;
import eu.domibus.connector.domain.enums.DomibusConnectorRejectionReason;
import eu.ecodex.dc5.message.model.DC5Confirmation;
import eu.domibus.connector.lib.logging.MDC;
import eu.domibus.connector.tools.LoggingMDCPropertyNames;
import eu.ecodex.dc5.message.model.DC5MessageId;
import eu.ecodex.dc5.message.model.MessageModelHelper;
import eu.ecodex.dc5.message.repo.DC5MessageRepo;
import eu.ecodex.dc5.message.validation.OutgoingBusinessMessageRules;
import eu.ecodex.dc5.message.validation.OutgoingMessageRules;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import eu.domibus.connector.domain.enums.DomibusConnectorEvidenceType;
import eu.ecodex.dc5.message.model.DC5Message;
import eu.domibus.connector.evidences.exception.DomibusConnectorEvidencesToolkitException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Takes a originalMessage from backend and creates evidences for it
 * and also wraps it into an asic container and delivers the
 * originalMessage to the gw
 */
@Component
@RequiredArgsConstructor
public class ProcessOutgoingBusinessMessageFlow implements DomibusConnectorMessageProcessor {

    public static final String BACKEND_TO_GW_MESSAGE_PROCESSOR_BEAN_NAME = "OutgoingBusinessMessageFlow";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessOutgoingBusinessMessageFlow.class);

    private final BuildECodexContainerStep buildECodexContainerStep;
    private final MessageConfirmationStep messageConfirmationStep;
    private final ConfirmationCreatorService confirmationCreatorService;
    private final SubmitConfirmationAsEvidenceMessageStep submitAsEvidenceMessageToLink;
    private final LookupGatewayNameStep lookupGatewayNameStep;
    private final VerifyPModesStep verifyPModesStep;
    private final Validator validator;
    private final DC5MessageRepo messageRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final ConnectorMessageProcessingProperties processingProperties;

    @EventListener
    @MDC(name = LoggingMDCPropertyNames.MDC_DC_MESSAGE_PROCESSOR_PROPERTY_NAME, value = BACKEND_TO_GW_MESSAGE_PROCESSOR_BEAN_NAME)
    public void processOutgoingBusinessMessageTransportEvent(OutgoingBusinessMessageTransportEvent event) {
        TransportState transportState = event.getTransportState();
        DC5Message transportedMessage = event.getMessage();
        if (!MessageModelHelper.isOutgoingBusinessMessage(transportedMessage)) {
            throw new IllegalArgumentException("TransportEvent does not reference a outgoing business message!");
        }
        if (transportState == TransportState.FAILED) {

            //message failed to be transported to gw
            DC5Confirmation submissionRejection = confirmationCreatorService.createConfirmation(DomibusConnectorEvidenceType.SUBMISSION_REJECTION,
                    transportedMessage,
                    DomibusConnectorRejectionReason.GW_REJECTION,
                    "");

            submitConfirmationMsg(transportedMessage, submissionRejection);
            //submit trigger message...

        }

        if (transportState == TransportState.ACCEPTED && processingProperties.isSendGeneratedEvidencesToBackend()) {

            DC5Confirmation submissionAcceptance = transportedMessage.getTransportedMessageConfirmation(); //should be submission acceptance
            if (submissionAcceptance != null &&
                    submissionAcceptance.getEvidenceType() != DomibusConnectorEvidenceType.SUBMISSION_ACCEPTANCE) {
                throw new IllegalStateException("Cannot continue, the wrong evidence has been packed into outgoing business message!");
            }
            submitConfirmationMsg(transportedMessage, submissionAcceptance);
            //submit trigger message...

        }

    }

    /**
     * Create a evidence message with the given confirmation and send it in
     * opposite direction to given business message
     * @param businessMessage - the given business message
     * @param confirmation - the confirmation
     */
    private void submitConfirmationMsg(DC5Message businessMessage, DC5Confirmation confirmation) {
        DC5Message.DC5MessageBuilder builder = DC5Message.builder();
        DC5Message evidenceMessage = builder
                .source(businessMessage.getTarget())
                .target(businessMessage.getSource())
                .transportedMessageConfirmation(confirmation)
                .businessDomainId(businessMessage.getBusinessDomainId())
                .refToConnectorMessageId(businessMessage.getConnectorMessageId())
                .connectorMessageId(DC5MessageId.ofRandom())
                .build();
        messageRepo.save(evidenceMessage);
        NewMessageStoredEvent newMessageStoredEvent = NewMessageStoredEvent.of(evidenceMessage.getId());
        eventPublisher.publishEvent(newMessageStoredEvent);
    }

    @MDC(name = LoggingMDCPropertyNames.MDC_DC_MESSAGE_PROCESSOR_PROPERTY_NAME, value = BACKEND_TO_GW_MESSAGE_PROCESSOR_BEAN_NAME)
    public void processMessage(DC5Message message) {
        try (org.slf4j.MDC.MDCCloseable var = org.slf4j.MDC.putCloseable(LoggingMDCPropertyNames.MDC_BACKEND_MESSAGE_ID_PROPERTY_NAME, message.getBackendData().getBackendMessageId().toString())) {

            //run jsr303 validation
            validateOutgoingMessage(message);

            //verify p-Modes step
            verifyPModesStep.verifyOutgoing(message);

            //buildEcodexContainerStep
            buildECodexContainerStep.executeStep(message);

            //set gateway name
            lookupGatewayNameStep.executeStep(message);


            //create confirmation
            DC5Confirmation submissionAcceptanceConfirmation = confirmationCreatorService.createConfirmation(DomibusConnectorEvidenceType.SUBMISSION_ACCEPTANCE, message, null, null);
            message.setTransportedMessageConfirmation(submissionAcceptanceConfirmation);


//            LOGGER.info(LoggingMarker.BUSINESS_LOG, "Put message with backendId [{}] to Gateway Link [{}] on toLink Queue.",
//                    message.getBackendData().getBackendMessageId(),
//                    message.getGatewayLinkName());

            MessageReadyForTransportEvent messageReadyForTransportEvent =
                    MessageReadyForTransportEvent.of(message.getId(), message.getGatewayLinkName(), MessageTargetSource.GATEWAY);
            eventPublisher.publishEvent(messageReadyForTransportEvent);
            // to be continued with processOutgoingBusinessMessageEvent

        } catch (StepFailedException secEcx) {
            DomibusConnectorEvidenceType evidenceType = DomibusConnectorEvidenceType.SUBMISSION_REJECTION;
            LOGGER.error("Could not process message [{}]! Rejecting message with [{}]",  message, evidenceType);

            DC5Confirmation submissionRejction = confirmationCreatorService.createConfirmation(evidenceType, message, DomibusConnectorRejectionReason.OTHER, "");
            messageConfirmationStep.processConfirmationForMessage(message, submissionRejction);
            submitAsEvidenceMessageToLink.submitOppositeDirection(null, message, submissionRejction);

//            DomibusConnectorMessageExceptionBuilder.createBuilder()
//                    .setMessage(message)
//                    .setText("Could not generate evidence submission acceptance! ")
//                    .setSource(this.getClass())
//                    .setCause(secEcx)
//                    .buildAndThrow();

        } catch (DomibusConnectorEvidencesToolkitException ete) {
            LOGGER.error("Could not generate evidence [{}] for originalMessage [{}]!", DomibusConnectorEvidenceType.SUBMISSION_ACCEPTANCE, message);

            DC5Confirmation submissionRejection = confirmationCreatorService.createConfirmation(DomibusConnectorEvidenceType.SUBMISSION_REJECTION, message, DomibusConnectorRejectionReason.OTHER, "");
            submitAsEvidenceMessageToLink.submitOppositeDirection(null, message, submissionRejection);

//            DomibusConnectorMessageExceptionBuilder.createBuilder()
//                    .setMessage(message)
//                    .setText("Could not generate evidence submission acceptance! ")
//                    .setSource(this.getClass())
//                    .setCause(ete)
//                    .buildAndThrow();
        }
    }

    private void validateOutgoingMessage(DC5Message message) {
        Set<ConstraintViolation<DC5Message>> validate = validator.validate(message, OutgoingMessageRules.class, OutgoingBusinessMessageRules.class);

        if (!validate.isEmpty()) {
            throw new IllegalArgumentException("Message is not valid due: " + validate
                    .stream()
                    .map(v -> v.getPropertyPath() + " : " + v.getMessage())
                    .collect(Collectors.joining("\n")));
        }
    }

}
