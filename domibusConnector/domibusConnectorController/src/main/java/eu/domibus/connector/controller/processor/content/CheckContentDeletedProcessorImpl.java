package eu.domibus.connector.controller.processor.content;

import eu.domibus.connector.controller.spring.ContentDeletionTimeoutConfigurationProperties;
import eu.domibus.connector.domain.enums.DomibusConnectorMessageDirection;
import eu.domibus.connector.domain.model.LargeFileReference;
import eu.domibus.connector.domain.model.DomibusConnectorMessage;
import eu.domibus.connector.domain.model.DomibusConnectorMessageId;
import eu.domibus.connector.persistence.service.LargeFilePersistenceService;
import eu.domibus.connector.persistence.service.DCMessagePersistenceService;
import eu.domibus.connector.persistence.service.exceptions.LargeFileDeletionException;
import eu.domibus.connector.tools.logging.LoggingMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CheckContentDeletedProcessorImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckContentDeletedProcessorImpl.class);

    LargeFilePersistenceService largeFilePersistenceService;
    DCMessagePersistenceService messagePersistenceService;
    ContentDeletionTimeoutConfigurationProperties contentDeletionTimeoutConfigurationProperties;

    @Autowired
    public void setLargeFilePersistenceService(LargeFilePersistenceService largeFilePersistenceService) {
        this.largeFilePersistenceService = largeFilePersistenceService;
    }

    @Autowired
    public void setMessagePersistenceService(DCMessagePersistenceService messagePersistenceService) {
        this.messagePersistenceService = messagePersistenceService;
    }


    public void checkContentDeletedProcessor() {
        Map<DomibusConnectorMessageId, List<LargeFileReference>> allAvailableReferences = largeFilePersistenceService.getAllAvailableReferences();


        List<LargeFileReference> referencesToDelete = allAvailableReferences
                .entrySet()
                .stream()
                .flatMap(e -> getDeleteableReferences(e.getKey(), e.getValue()).stream())
                .collect(Collectors.toList());

        referencesToDelete.forEach(ref -> {
            LOGGER.debug(LoggingMarker.BUSINESS_CONTENT_LOG, "Deleting reference with id [{}]", ref.getStorageIdReference());
            try {
                largeFilePersistenceService.deleteDomibusConnectorBigDataReference(ref);
            } catch (LargeFileDeletionException delException) {
                LOGGER.error(LoggingMarker.BUSINESS_CONTENT_LOG, "Was unable to delete the reference [{}] in the timer job. The data must be manually deleted by the administrator!", ref);
                LOGGER.error("Was unable to delete due exception: ", delException);
            }
        });
    }

    List<LargeFileReference> getDeleteableReferences(DomibusConnectorMessageId id, List<LargeFileReference> references) {
        DomibusConnectorMessage msg = messagePersistenceService.findMessageByConnectorMessageId(id.getConnectorMessageId());
        if (msg == null) {
            LOGGER.warn("No message with connector message id [{}] found in database. This content will NOT be deleted. Please check and remove manual!", id.getConnectorMessageId());
            return references;
        }


        DomibusConnectorMessageDirection direction = msg.getMessageDetails().getDirection();
        Date transportFinishedDate = null;
        boolean failed = false;
        if (direction == DomibusConnectorMessageDirection.BACKEND_TO_GATEWAY || direction == DomibusConnectorMessageDirection.CONNECTOR_TO_GATEWAY) {
            transportFinishedDate = msg.getMessageDetails().getDeliveredToGateway();
        } else if (direction == DomibusConnectorMessageDirection.GATEWAY_TO_BACKEND || direction == DomibusConnectorMessageDirection.CONNECTOR_TO_BACKEND){
            transportFinishedDate = msg.getMessageDetails().getDeliveredToBackend();
        } else {
            //should never end up here!
            assert(true);
        }

        if (msg.getMessageDetails().getFailed() != null) {
            failed = true;
            transportFinishedDate = msg.getMessageDetails().getFailed();
        }


        if (transportFinishedDate != null) {
            LOGGER.info(LoggingMarker.BUSINESS_CONTENT_LOG,
                    "Transport of message [{}] in direction [{}] has {} on [{}] - deleting content of this message",
                    msg, direction, failed ? "failed" : "finished", transportFinishedDate);

            return references;
        }
        return references;
    }

}
