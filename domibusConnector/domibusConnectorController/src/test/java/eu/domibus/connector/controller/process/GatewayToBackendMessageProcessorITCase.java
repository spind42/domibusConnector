package eu.domibus.connector.controller.process;


import eu.domibus.connector.controller.test.util.ITCaseTestContext;
import eu.domibus.connector.controller.test.util.LoadStoreMessageFromPath;
import eu.domibus.connector.domain.enums.DomibusConnectorMessageDirection;
import eu.domibus.connector.domain.model.DomibusConnectorMessage;
import eu.domibus.connector.persistence.service.DomibusConnectorMessagePersistenceService;
import eu.domibus.connector.persistence.service.DomibusConnectorPersistAllBigDataOfMessageService;
import eu.domibus.connector.persistence.service.impl.BigDataWithMessagePersistenceServiceImpl;
import eu.domibus.connector.security.container.service.TokenIssuerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ITCaseTestContext.class, TokenIssuerFactory.class})
@TestPropertySource("classpath:application-test.properties")
@Commit
@ActiveProfiles("ITCaseTestContext")
@Sql(scripts = "/testdata.sql") //adds testdata to database like domibus-blue party
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GatewayToBackendMessageProcessorITCase {


    @Autowired
    @Qualifier("GatewayToBackendMessageProcessor")
    private DomibusConnectorMessageProcessor gatewayToBackendMessageProcessor;

    @Autowired
    @Qualifier(ITCaseTestContext.TO_GW_DELIVERD_MESSAGES_LIST_BEAN_NAME)
    private BlockingQueue<DomibusConnectorMessage> toGatewayDeliveredMessages;

    @Autowired
    @Qualifier(ITCaseTestContext.TO_BACKEND_DELIVERD_MESSAGES_LIST_BEAN_NAME)
    private BlockingQueue<DomibusConnectorMessage> toBackendDeliveredMessages;

    @Autowired
    private DomibusConnectorMessagePersistenceService messagePersistenceService;

    @Autowired
    private DomibusConnectorPersistAllBigDataOfMessageService bigDataWithMessagePersistenceService;

    @Test(timeout=20000)
    public void testProcessMessage() throws IOException {
        //create test originalMessage and persist originalMessage into DB
        DomibusConnectorMessage message = LoadStoreMessageFromPath.loadMessageFrom(new ClassPathResource("/testmessages/msg2/"));
        message.setConnectorMessageId("msg3");


        message = messagePersistenceService.persistMessageIntoDatabase(message, DomibusConnectorMessageDirection.GW_TO_NAT);
        message = bigDataWithMessagePersistenceService.persistAllBigFilesFromMessage(message);
        message = messagePersistenceService.mergeMessageWithDatabase(message);
        message = bigDataWithMessagePersistenceService.loadAllBigFilesFromMessage(message);

        //start test
        gatewayToBackendMessageProcessor.processMessage(message);


        //validate test results
        assertThat(toBackendDeliveredMessages).as("One originalMessage should be delivered to backendlink").hasSize(1);

    }



}