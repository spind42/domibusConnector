package eu.domibus.connector.controller.transport;

import eu.domibus.connector.controller.service.SubmitToLinkService;
import eu.domibus.connector.domain.model.DomibusConnectorMessage;
import eu.domibus.connector.domain.model.DomibusConnectorMessageId;
import eu.domibus.connector.domain.model.DomibusConnectorTransportStep;
import eu.domibus.connector.domain.model.helper.DomainModelHelper;
import eu.domibus.connector.persistence.service.DCMessagePersistenceService;
import org.springframework.stereotype.Service;

@Service
public class DCTransportRetryServiceImpl implements DCTransportRetryService {

    private final SubmitToLinkService submitToLinkService;
    private final DCMessagePersistenceService messagePersistenceService;

    public DCTransportRetryServiceImpl(SubmitToLinkService submitToLinkService,
                                       DCMessagePersistenceService messagePersistenceService) {
        this.submitToLinkService = submitToLinkService;
        this.messagePersistenceService = messagePersistenceService;
    }

    @Override
    public void retryTransport(DomibusConnectorTransportStep step) {
        DomibusConnectorMessage transportedMessage = step.getTransportedMessage();
        submitToLinkService.submitToLink(transportedMessage);
    }


    @Override
    public boolean isRetryAble(DomibusConnectorTransportStep step) {
        boolean retryPossible = true;
        DomibusConnectorMessage msg = step.getTransportedMessage();
        if (DomainModelHelper.isBusinessMessage(msg)) {
            retryPossible = !messagePersistenceService.checkMessageConfirmedOrRejected(msg);
        }
        return retryPossible;
    }

}
