package eu.ecodex.dc5.flow.flows;

import eu.domibus.connector.controller.exception.ErrorCode;
import eu.domibus.connector.domain.model.DomibusConnectorMessage;
import eu.ecodex.dc5.flow.api.DC5TransformToDomain;
import eu.ecodex.dc5.flow.api.StepFailedException;
import eu.ecodex.dc5.flow.events.NewMessageStoredEvent;
import eu.ecodex.dc5.flow.steps.DC5SaveMessageStep;
import eu.ecodex.dc5.flow.steps.DC5TransformMessageStep;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ReceiveMessageFlow {

    private final DC5TransformMessageStep transformMessageStep;
    private final DC5SaveMessageStep saveMessageStep;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public <T> ReceiveMessageFlowResult receiveMessage(T message, DC5TransformToDomain<T> transform) {
        try {
            DomibusConnectorMessage msg = transformMessageStep.transformMessage(message, transform);
            DomibusConnectorMessage dc5Msg = saveMessageStep.saveNewMessage(msg);
            eventPublisher.publishEvent(NewMessageStoredEvent.of(dc5Msg.getId()));
            return ReceiveMessageFlowResult.getSuccess();
        } catch (StepFailedException stepFailedException) {
            return new ReceiveMessageFlowResult(false,Optional.empty(), Optional.of(stepFailedException));
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class ReceiveMessageFlowResult {
        private final boolean success;
        private final Optional<ErrorCode> errorCodeOptional;
        private final Optional<Throwable> error;

        public static ReceiveMessageFlowResult getSuccess() {
            return new ReceiveMessageFlowResult(true, Optional.empty(), Optional.empty());
        }
    }



}