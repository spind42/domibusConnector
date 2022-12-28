package eu.domibus.connector.controller.transport;

import eu.domibus.connector.controller.service.TransportStateService;
import eu.domibus.connector.domain.enums.TransportState;
import eu.domibus.connector.domain.model.DomibusConnectorLinkPartner;
import eu.ecodex.dc5.message.model.BackendMessageId;
import eu.ecodex.dc5.message.model.DC5Message;
import eu.domibus.connector.domain.model.DomibusConnectorTransportStep;
import eu.domibus.connector.persistence.service.DCMessagePersistenceService;
import eu.domibus.connector.persistence.service.DomibusConnectorEvidencePersistenceService;
import eu.domibus.connector.persistence.service.DomibusConnectorMessageErrorPersistenceService;
import eu.domibus.connector.persistence.service.TransportStepPersistenceService;
import eu.ecodex.dc5.message.model.EbmsMessageId;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

//@Service
//@Transactional
//@RequiredArgsConstructor
@Deprecated //replaced by transportflow
public class DomibusConnectorTransportStateService implements TransportStateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomibusConnectorTransportStateService.class);

//    private final DCMessagePersistenceService messagePersistenceService;
//    private final DomibusConnectorMessageErrorPersistenceService errorPersistenceService;
//    private final TransportStepPersistenceService transportStepPersistenceService;
//    private final DomibusConnectorEvidencePersistenceService evidencePeristenceService;


    @Override
    @Transactional
    public void updateTransportToGatewayStatus(TransportId transportId, DomibusConnectorTransportState transportState) {

        this.updateTransportStatus(transportId, transportState, (DC5Message m) -> {
            if (m.isBusinessMessage()) {
                m.getEbmsData().setEbmsMessageId(EbmsMessageId.ofString(transportState.getRemoteMessageId()));
//                messagePersistenceService.updateMessageDetails(m);
//                messagePersistenceService.setDeliveredToGateway(m);
                LOGGER.debug("Successfully updated business message [{}]", m);
            }
//            m.getTransportedMessageConfirmations().forEach(c -> {
//                try {
////                    evidencePeristenceService.setConfirmationAsTransportedToGateway(c);
//                } catch (Exception e) {
//                    //any issue here should not prevent commit!
//                    LOGGER.warn("Failed to set transport time stamp of confirmation!", e);
//                }
//            });
        });

    }


    @Override
    @Transactional
    public void updateTransportToBackendClientStatus(TransportId transportId, DomibusConnectorTransportState transportState) {
//        this.updateTransportStatus(transportId, transportState, (DC5Message m) -> {
//            if (isBusinessMessage(m)) {
//                m.getBackendData().setBackendMessageId(new BackendMessageId(transportState.getRemoteMessageId()));
////                messagePersistenceService.updateMessageDetails(m);
////                messagePersistenceService.setMessageDeliveredToNationalSystem(m);
//                LOGGER.debug("Successfully updated business message [{}]", m);
//            }
//            m.getTransportedMessageConfirmations().forEach(c -> {
//                try {
////                    evidencePeristenceService.setConfirmationAsTransportedToBackend(c);
//                } catch (Exception e) {
//                    //any issue here should not prevent commit!
//                    LOGGER.warn("Failed to set transport time stamp of confirmation!", e);
//                }
//            });
//        });
    }

    private static interface SuccessHandler {
        void success(DC5Message message);
    }


    private void updateTransportStatus(TransportId transportId, DomibusConnectorTransportState transportState, SuccessHandler successHandler) {
//        if (transportId == null) {
//            throw new IllegalArgumentException("TransportId is not allowed to be null!");
//        }
//        transportState.setConnectorTransportId(transportId);
//        if (transportState == null) {
//            throw new IllegalArgumentException("TransportState is not allowed to be null!");
//        }
//        DomibusConnectorTransportStep transportStep = transportStepPersistenceService.getTransportStepByTransportId(transportId);
//
//
//        if (StringUtils.isEmpty(transportStep.getRemoteMessageId())) {
//            transportStep.setRemoteMessageId(transportState.getRemoteMessageId());
//        }
//        if (StringUtils.isEmpty(transportStep.getTransportSystemMessageId())) {
//            transportStep.setTransportSystemMessageId(transportState.getTransportImplId());
//        }
//        DomibusConnectorTransportStep.DomibusConnectorTransportStepStatusUpdate statusUpdate = new DomibusConnectorTransportStep.DomibusConnectorTransportStepStatusUpdate();
//        statusUpdate.setCreated(LocalDateTime.now());
//        statusUpdate.setTransportState(transportState.getStatus());
//        transportStep.addTransportStatus(statusUpdate);
////        transportStepPersistenceService.update(transportStep);
//
//
//        DC5Message message = transportStep.getTransportedMessage().orElse(null);
//
//        if (message == null) {
//            //cannot update a transport for a null message maybe it's a evidence message, but they don't have
//            // a relation to connector message id yet...so cannot set transport state for them!
//            LOGGER.debug("#updateTransportToBackendStatus:: No message with transport id [{}] was found within database!", transportState.getConnectorTransportId());
//            return;
//        }
//
//        if (transportState.getStatus() == TransportState.ACCEPTED) {
//            successHandler.success(message);
//        } else if (transportState.getStatus() == TransportState.FAILED) {
//            transportState.getMessageErrorList().stream().forEach( error ->
////                    errorPersistenceService.persistMessageError(transportState.getConnectorTransportId().getTransportId(), error)
//            );
//        }

    }


    @Override
    public void updateTransportStatus(DomibusConnectorTransportState transportState) {
        this.updateTransportStatus(transportState.getConnectorTransportId(), transportState, (m) -> {});
    }

    @Override
    public TransportId createTransportFor(DC5Message message, DomibusConnectorLinkPartner.LinkPartnerName linkPartnerName) {

//        DomibusConnectorTransportStep transportStep = new DomibusConnectorTransportStep();
//        transportStep.setLinkPartnerName(linkPartnerName);
//        transportStep.setCreated(LocalDateTime.now());
//        transportStep.setTransportedMessage(message);
//        transportStep.setConnectorMessageId(message.getConnectorMessageId());
//
//        transportStep = transportStepPersistenceService.createNewTransportStep(transportStep);
//        LOGGER.debug("#createTransportFor:: created new transport step within database with id [{}]", transportStep.getTransportId());
//        return transportStep.getTransportId();
    return null;
    }


    @Override
    public List<DomibusConnectorTransportStep> getPendingTransportsForLinkPartner(DomibusConnectorLinkPartner.LinkPartnerName linkPartnerName) {
//        return transportStepPersistenceService.findPendingStepBy(linkPartnerName)
        return Collections.emptyList();
    }

    @Override
    public Optional<DomibusConnectorTransportStep> getTransportStepById(TransportId transportId) {
//        return transportStepPersistenceService.findStepById(transportId);
        return Optional.empty();
    }


}
