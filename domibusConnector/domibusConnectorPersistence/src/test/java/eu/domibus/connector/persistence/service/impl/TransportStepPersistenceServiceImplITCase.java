package eu.domibus.connector.persistence.service.impl;

import eu.domibus.connector.controller.service.TransportStateService;
import eu.domibus.connector.domain.enums.TransportState;
import eu.domibus.connector.domain.model.DomibusConnectorLinkPartner;
import eu.ecodex.dc5.message.model.DC5Message;
import eu.ecodex.dc5.message.model.DC5MessageId;
import eu.domibus.connector.domain.model.DomibusConnectorTransportStep;
import eu.domibus.connector.domain.testutil.DomainEntityCreator;
import eu.domibus.connector.persistence.dao.CommonPersistenceTest;
import eu.domibus.connector.persistence.service.TransportStepPersistenceService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CommonPersistenceTest
@Disabled
class TransportStepPersistenceServiceImplITCase {

    @Autowired
    DataSource ds;

    @Autowired
    TransportStepPersistenceService transportStepPersistenceService;


    @Test
    void createNewTransportStep() {
        DC5Message m = DomainEntityCreator.createMessage();
        m.setConnectorMessageId(new DC5MessageId("id002"));

        DomibusConnectorTransportStep step = new DomibusConnectorTransportStep();
        DomibusConnectorLinkPartner.LinkPartnerName lp = new DomibusConnectorLinkPartner.LinkPartnerName("link2");

        step.setConnectorMessageId(new DC5MessageId("id002"));
        step.setTransportedMessage(m);
        step.setTransportId(new TransportStateService.TransportId("msg2_link2_1"));
        step.setAttempt(1);
        step.setLinkPartnerName(lp);

        transportStepPersistenceService.createNewTransportStep(step);
    }

    @Test
    void createNewTransportStepSetPending() {
        DC5Message m = DomainEntityCreator.createMessage();
        m.setConnectorMessageId(new DC5MessageId("id002"));

        DomibusConnectorTransportStep step = new DomibusConnectorTransportStep();
        DomibusConnectorLinkPartner.LinkPartnerName lp = new DomibusConnectorLinkPartner.LinkPartnerName("link4");

        DomibusConnectorTransportStep.DomibusConnectorTransportStepStatusUpdate statusUpdate = new DomibusConnectorTransportStep.DomibusConnectorTransportStepStatusUpdate();
        statusUpdate.setTransportState(TransportState.PENDING);
        statusUpdate.setCreated(LocalDateTime.now());

        step.setTransportedMessage(m);
        step.setTransportId(new TransportStateService.TransportId("msg3_link2_1"));
        step.setAttempt(1);
        step.setLinkPartnerName(lp);
        step.addStatusUpdate(statusUpdate);

        transportStepPersistenceService.createNewTransportStep(step);

        List<DomibusConnectorTransportStep> pendingStepBy = transportStepPersistenceService.findPendingStepBy(lp);
        assertThat(pendingStepBy).hasSize(1);

    }


}