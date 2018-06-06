package eu.domibus.connector.controller.exception.handling;

import eu.domibus.connector.controller.exception.DomibusConnectorMessageException;
import eu.domibus.connector.controller.exception.DomibusConnectorMessageExceptionBuilder;
import eu.domibus.connector.controller.process.DomibusConnectorMessageProcessor;
import eu.domibus.connector.controller.test.util.ConnectorControllerTestDomainCreator;
import eu.domibus.connector.domain.model.DomibusConnectorMessage;
import eu.domibus.connector.domain.model.DomibusConnectorMessageError;
import eu.domibus.connector.persistence.service.DomibusConnectorMessageErrorPersistenceService;
import javax.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {StoreMessageExceptionIntoDatabaseAspectTest.TestContextConfiguration.class})
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public class StoreMessageExceptionIntoDatabaseAspectTest {
    
    @Configuration
    @EnableAspectJAutoProxy
    @ComponentScan(basePackages="eu.domibus.connector.controller.exception.handling")
    public static class TestContextConfiguration {
        
        @Bean
        public DomibusConnectorMessageErrorPersistenceService myMockedPersistenceService() {
            DomibusConnectorMessageErrorPersistenceService mock = Mockito.mock(DomibusConnectorMessageErrorPersistenceService.class);
            return mock;
        }
        
        @Bean("nopassException")
        public DomibusConnectorMessageProcessor messageProcessor() {
            return new NoPassExceptionMessageProcessor();
        }
        
        @Bean("passException")
        public DomibusConnectorMessageProcessor passExceptionMessageProcessor() {
            return new PassExceptionMessageProcessor();
        }
        
    }
    
    @Resource(name="passException")
    DomibusConnectorMessageProcessor passExceptionProcessor;
    
    @Resource(name="nopassException")
    DomibusConnectorMessageProcessor noPassExceptionProcessor;
    
    @Autowired
    DomibusConnectorMessageErrorPersistenceService persistenceService;
    
    public StoreMessageExceptionIntoDatabaseAspectTest() {
    }

    @Test
    public void testIfExceptionIsPersisted() {
        DomibusConnectorMessage message = ConnectorControllerTestDomainCreator.createMessage();
        message.setConnectorMessageId("testid");
        try {
            passExceptionProcessor.processMessage(message);
        } catch (DomibusConnectorMessageException ex) {
            //do nothing...
        }
        
        //test if persistence service was called
        Mockito.verify(persistenceService, Mockito.times(1))
                .persistMessageError(eq("testid"), any(DomibusConnectorMessageError.class));
    }
    
    @Test(expected=DomibusConnectorMessageException.class)
    public void testPassException() {        
        DomibusConnectorMessage message = ConnectorControllerTestDomainCreator.createMessage();
        message.setConnectorMessageId("testid");
        passExceptionProcessor.processMessage(message);
    }
    
    @Test
    public void testAspectNotPassException() {        
        DomibusConnectorMessage message = ConnectorControllerTestDomainCreator.createMessage();
        message.setConnectorMessageId("testid");
        noPassExceptionProcessor.processMessage(message);
        
        //test if persistence service was called
        Mockito.verify(persistenceService, Mockito.times(1))
                .persistMessageError(eq("testid"), any(DomibusConnectorMessageError.class));
    }
    
    
    private static class NoPassExceptionMessageProcessor implements DomibusConnectorMessageProcessor {

        @Override
        @StoreMessageExceptionIntoDatabase(passException=false)
        public void processMessage(DomibusConnectorMessage message) {
            throw DomibusConnectorMessageExceptionBuilder.createBuilder()
                    .setText("i am an exception!")
                    .setSourceObject(this)
                    .setMessage(message)
                    .build();
        }
        
    }
    
    private static class PassExceptionMessageProcessor implements DomibusConnectorMessageProcessor {

        @Override
        @StoreMessageExceptionIntoDatabase(passException=true)
        public void processMessage(DomibusConnectorMessage message) {
            throw DomibusConnectorMessageExceptionBuilder.createBuilder()
                    .setText("i am an exception!")
                    .setSourceObject(this)
                    .setMessage(message)
                    .build();
        }
        
    }
    
}