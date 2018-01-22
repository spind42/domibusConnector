/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.domibus.connector.controller.service;


import eu.domibus.connector.controller.test.util.DomibusConnectorBigDataReferenceInMemory;
import eu.domibus.connector.domain.model.DomibusConnectorBigDataReference;
import java.io.File;
import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import org.apache.log4j.lf5.util.StreamUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.domibus.connector.controller.service.IncomingMessageServiceITCase.TestConfiguration;
import eu.domibus.connector.domain.model.DomibusConnectorMessage;
import eu.domibus.connector.domain.model.DomibusConnectorMessageContent;
import eu.domibus.connector.domain.model.DomibusConnectorMessageDetails;
import eu.domibus.connector.domain.model.builder.DomibusConnectorActionBuilder;
import eu.domibus.connector.domain.model.builder.DomibusConnectorMessageAttachmentBuilder;
import eu.domibus.connector.domain.model.builder.DomibusConnectorMessageBuilder;
import eu.domibus.connector.domain.model.builder.DomibusConnectorPartyBuilder;
import eu.domibus.connector.domain.model.builder.DomibusConnectorServiceBuilder;

/**
 *
 * @author {@literal Stephan Spindler <stephan.spindler@extern.brz.gv.at> }
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestConfiguration.class})
@TestPropertySource("classpath:application-test.properties")
public class IncomingMessageServiceITCase {

    public static String TEST_FILE_RESULTS_DIR_PROPERTY_NAME = "test.file.results";
    private File testResultsFolder;
    private String testDateAsString;
    
    @SpringBootApplication(scanBasePackages = {"eu.domibus.connector"})
    static class TestConfiguration {

        @Bean
        public static PropertySourcesPlaceholderConfigurer
                propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }
    
    @Autowired
    DomibusConnectorGatewayDeliveryService rcvMessageFromGwService;
    
    @MockBean
    DomibusConnectorGatewaySubmissionService sendMessageToGwService;
    
    @MockBean
    DomibusConnectorBackendSubmissionService sendMessageToBackendService;
    
//    @MockBean
//    DomibusConnectorPersistenceService persistenceService;
//    
//    @Autowired
//    DomibusConnectorIncomingController domibusConnectorController;
    
    @Before
    public void setUp() {
        String dir = System.getenv().getOrDefault(TEST_FILE_RESULTS_DIR_PROPERTY_NAME, "./target/testfileresults/");
        dir = dir + "/" + IncomingMessageServiceITCase.class.getSimpleName();
        testResultsFolder = new File(dir);
        testResultsFolder.mkdirs();

        DateFormatter simpleDateFormatter = new DateFormatter();
        simpleDateFormatter.setPattern("yyyy-MM-dd-hh-mm");
        testDateAsString = simpleDateFormatter.print(new Date(), Locale.ENGLISH);
    }
    
    
    @Test
    public void testReceiveMessageFromGw() throws IOException {
        
        //messageBuilder.setMessageDetails(loadMessageDetailsFromPropertyFile("/testmessages/msg1/message.properties"));
        DomibusConnectorMessage loadMessageFrom = loadMessageFrom("/testmessages/msg1/");
        
        assertThat(loadMessageFrom).isNotNull();
        
        rcvMessageFromGwService.deliverMessageFromGateway(loadMessageFrom);
        
    }
    
    private DomibusConnectorMessage loadMessageFrom(String basePath) throws IOException {
        DomibusConnectorMessageBuilder messageBuilder = DomibusConnectorMessageBuilder.createBuilder();
        
        Properties messageProps = new Properties();
        
        messageProps.load(loadRelativeResource(basePath, "message.properties"));
                
        //add message details
        messageBuilder.setMessageDetails(loadMessageDetailsFromProperties(messageProps));
        
        //add xml content
        DomibusConnectorMessageContent content = new DomibusConnectorMessageContent();
        content.setXmlContent(StreamUtils.getBytes(loadRelativeResource(basePath, "content.xml")));
        messageBuilder.setMessageContent(content);
        
        
        //add asic container attachment
        messageBuilder.addAttachment(DomibusConnectorMessageAttachmentBuilder.createBuilder()
                .setAttachment(loadRelativeResourceAsByteArray(basePath, "ASIC-S.asics"))
                .setIdentifier("ASIC-S")
                .build());
        
        //add token xml
        messageBuilder.addAttachment(DomibusConnectorMessageAttachmentBuilder.createBuilder()
                .setAttachment(loadRelativeResourceAsByteArray(basePath, "Token.xml"))
                .setIdentifier("tokenXML")
                .build());
        
        //add submission evidence
        messageBuilder.addAttachment(DomibusConnectorMessageAttachmentBuilder.createBuilder()
                .setAttachment(loadRelativeResourceAsByteArray(basePath, "SUBMISSION_ACCEPTANCE.xml"))
                .setIdentifier("SUBMISSION_ACCEPTANCE")
                .build());
                        
        return messageBuilder.build();
    }
    
    private DomibusConnectorBigDataReference loadRelativeResourceAsByteArray(String base, String relative) {
        InputStream inputStream = loadRelativeResource(base, relative);

        DomibusConnectorBigDataReferenceInMemory inMemory = new DomibusConnectorBigDataReferenceInMemory();
        inMemory.setInputStream(inputStream);
        inMemory.setReadable(true);
        return inMemory;
    }
    
    private InputStream loadRelativeResource(String base, String relative) {
        String resource = base + "/" + relative;
        InputStream inputStream = getClass().getResourceAsStream(resource);
        if (inputStream == null) {
            throw new RuntimeException(String.format("Ressource %s could not be load!", resource));
        }
        return inputStream;
    }
    
    

    private DomibusConnectorMessageDetails loadMessageDetailsFromProperties(Properties messageProps) {        
        DomibusConnectorMessageDetails messageDetails = new DomibusConnectorMessageDetails();
        
        messageDetails.setAction(DomibusConnectorActionBuilder.createBuilder()
                .setAction(messageProps.getProperty("action"))
                .withDocumentRequired(false)
                .build()
        );
        
        messageDetails.setFromParty(DomibusConnectorPartyBuilder.createBuilder()
                .setPartyId(messageProps.getProperty("from.party.id"))
                .setRole(messageProps.getProperty("from.party.role"))
                .build()
        );
        
        messageDetails.setToParty(DomibusConnectorPartyBuilder.createBuilder()
                .setPartyId(messageProps.getProperty("to.party.id"))
                .setRole(messageProps.getProperty("to.party.role"))
                .build()
        );
        
        messageDetails.setService(DomibusConnectorServiceBuilder.createBuilder()
                .setService(messageProps.getProperty("service"))
                .build()
        );
        
        messageDetails.setConversationId(messageProps.getProperty("conversation.id"));
        
        messageDetails.setEbmsMessageId(messageProps.getProperty("ebms.message.id"));
        
        
        return messageDetails;
    }
    
    
}
