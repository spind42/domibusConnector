package eu.domibus.connector.persistence.service;




import eu.domibus.connector.domain.enums.DomibusConnectorEvidenceType;
import eu.domibus.connector.domain.enums.DomibusConnectorMessageDirection;
import eu.domibus.connector.domain.model.*;
import eu.domibus.connector.domain.model.builder.DomibusConnectorPartyBuilder;
import eu.domibus.connector.domain.test.util.DomainEntityCreatorForPersistenceTests;
import eu.domibus.connector.domain.testutil.DomainEntityCreator;
import eu.domibus.connector.domain.transformer.util.DomibusConnectorBigDataReferenceMemoryBacked;
import eu.domibus.connector.persistence.service.exceptions.PersistenceException;
import eu.domibus.connector.persistence.testutil.SetupPersistenceContext;
import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

import static eu.domibus.connector.persistence.spring.PersistenceProfiles.STORAGE_DB_PROFILE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test for testing persistence service
 * 
 * creates a embedded h2 database for tests
 * this tests are _WRITING_ to the database, there is no rollback
 * 
 * then liquibase is used to initialize tables and basic testdata in
 * the testing database
 * 
 * additional testdata is loaded by dbunit in setUp method
 * 
 * database settings are configured in {@literal application-<profile>.properties}
 * 
 * @author {@literal Stephan Spindler <stephan.spindler@extern.brz.gv.at>}
 */
public class MessagePersistenceServiceITCase {

    static ConfigurableApplicationContext APPLICATION_CONTEXT;

    private DataSource ds;
    private DomibusConnectorMessagePersistenceService messagePersistenceService;


    @BeforeClass
    public static void InitClass() {
        Properties defaultProperties = SetupPersistenceContext.getDefaultProperties();
        Set<String> defaultProfiles = SetupPersistenceContext.getDefaultProfiles();
        defaultProfiles.add(STORAGE_DB_PROFILE_NAME);
        APPLICATION_CONTEXT = SetupPersistenceContext.startApplicationContext(defaultProperties, defaultProfiles);
    }

    @AfterClass
    public static void afterClass() {
        APPLICATION_CONTEXT.close();
    }

    @Before
    public void setUp() throws InterruptedException {
        
        //lookup type
        this.ds = APPLICATION_CONTEXT.getBean(DataSource.class);
        //lookup name
        this.messagePersistenceService = APPLICATION_CONTEXT.getBean(DomibusConnectorMessagePersistenceService.class);
    }


    @Test
    public void testPersistMessageIntoDatabase() throws PersistenceException, SQLException, AmbiguousTableNameException, DataSetException {
        String connectorMessageId = "msg9021";

        DomibusConnectorMessage message = DomainEntityCreatorForPersistenceTests.createMessage(connectorMessageId);
        //message.setDbMessageId(null);
        //MessageDirection messageDirection = MessageDirection.GW_TO_NAT;
        DomibusConnectorMessageDetails messageDetails = message.getMessageDetails();
        
        messageDetails.setConversationId("conversation4211");
        messageDetails.setEbmsMessageId("ebms4211");
        messageDetails.setBackendMessageId("backend4211");
                     
        DomibusConnectorParty fromPartyAT = DomainEntityCreatorForPersistenceTests.createPartyAT();
        DomibusConnectorParty toPartyDE = DomibusConnectorPartyBuilder.createBuilder()
                .copyPropertiesFrom(DomainEntityCreatorForPersistenceTests.createPartyDE())
                .withPartyIdType(null)
                .build();


        messageDetails.setFromParty(fromPartyAT);
        messageDetails.setToParty(toPartyDE);
        
                
        DomibusConnectorMessage persistedMessage = messagePersistenceService.persistMessageIntoDatabase(message, DomibusConnectorMessageDirection.GW_TO_NAT);
        
        assertThat(persistedMessage).isNotNull();

        //check result in DB
        DatabaseDataSourceConnection conn = new DatabaseDataSourceConnection(ds);
        QueryDataSet dataSet = new QueryDataSet(conn);
        dataSet.addTable("DOMIBUS_CONNECTOR_MESSAGE", 
                String.format("SELECT * FROM DOMIBUS_CONNECTOR_MESSAGE WHERE CONNECTOR_MESSAGE_ID='%s'", connectorMessageId));
       
        ITable domibusConnectorTable = dataSet.getTable("DOMIBUS_CONNECTOR_MESSAGE");
        
        String ebmsId = (String) domibusConnectorTable.getValue(0, "ebms_message_id");
        assertThat(ebmsId).isEqualTo("ebms4211");
        
        String conversationId = (String) domibusConnectorTable.getValue(0, "conversation_id");
        assertThat(conversationId).isEqualTo("conversation4211");


        DomibusConnectorMessageDetails persistedDetails = persistedMessage.getMessageDetails();



//        assertThat(persistedDetails.getFromParty().getPartyIdType())
//                .as("correct partyIdType of fromParty should be loaded from db!")
//                .isEqualTo("urn:oasis:names:tc:ebcore:partyid-type:iso3166-1");

        assertThat(persistedDetails.getToParty().getPartyIdType())
                .as("correct partyIdType of toParty should be loaded from db!")
                .isEqualTo("urn:oasis:names:tc:ebcore:partyid-type:iso3166-1");
    }

    /**
     * Test if an the test EpoMessage can be persisted into database
     *
     */
    @Test
    public void testPersistEpoMessageIntoDatabase() throws PersistenceException, SQLException, AmbiguousTableNameException, DataSetException {
        String connectorMessageId = "msgid8972_epo";
        DomibusConnectorMessage epoMessage = DomainEntityCreator.createEpoMessage();
        epoMessage.setConnectorMessageId(connectorMessageId);

        epoMessage.getMessageDetails().setEbmsMessageId("ebms9000");
        epoMessage.getMessageDetails().setConversationId("conversation4000");

        epoMessage = messagePersistenceService.persistMessageIntoDatabase(epoMessage, DomibusConnectorMessageDirection.GW_TO_NAT);

        assertThat(epoMessage).isNotNull();

        //check result in DB
        DatabaseDataSourceConnection conn = new DatabaseDataSourceConnection(ds);
        QueryDataSet dataSet = new QueryDataSet(conn);
        dataSet.addTable("DOMIBUS_CONNECTOR_MESSAGE",
                String.format("SELECT * FROM DOMIBUS_CONNECTOR_MESSAGE WHERE CONNECTOR_MESSAGE_ID='%s'", connectorMessageId));

        ITable domibusConnectorTable = dataSet.getTable("DOMIBUS_CONNECTOR_MESSAGE");

        String ebmsId = (String) domibusConnectorTable.getValue(0, "ebms_message_id");
        assertThat(ebmsId).isEqualTo("ebms9000");

        String conversationId = (String) domibusConnectorTable.getValue(0, "conversation_id");
        assertThat(conversationId).isEqualTo("conversation4000");

    }
    
    /*
     * test that, the persist throws an exception if an service is used which is not 
     * configured in database!
     *   
     * TODO: improve specification to throw more specific exception!   
     *
     */
    @Test(timeout=20000, expected=PersistenceException.class) //(expected=TransientPropertyValueException.class)
    public void testPersistMessageIntoDatabase_serviceNotInDatabase_shouldThrowException() throws PersistenceException, SQLException, AmbiguousTableNameException, DataSetException {
        String connectorMessageId = "msg0021";

        DomibusConnectorMessage message = DomainEntityCreatorForPersistenceTests.createMessage(connectorMessageId);
        //message.setDbMessageId(null);
        //MessageDirection messageDirection = MessageDirection.GW_TO_NAT;
        DomibusConnectorMessageDetails messageDetails = message.getMessageDetails();
        
        messageDetails.setConversationId("conversation421");
        messageDetails.setEbmsMessageId("ebms421");
        messageDetails.setBackendMessageId("backend421");
        
        DomibusConnectorService serviceUnkown = DomainEntityCreatorForPersistenceTests.createServiceUnknown();
        messageDetails.setService(serviceUnkown); //set Unknown service

        //should throw exception, because UknownService is not configured in DB!
        messagePersistenceService.persistMessageIntoDatabase(message, DomibusConnectorMessageDirection.GW_TO_NAT);
    }
    
    
    /**
     * tests complete message, if can be stored to DB
     * and also loaded again from DB    
     * 
     * test restore evidenceMessage!
     */
    @Test(timeout=20000)
    public void testPersistMessageIntoDatabase_testContentPersist() throws PersistenceException, SQLException, AmbiguousTableNameException, DataSetException {
        String ebmsId = "ebamHUGO1";
        DomibusConnectorMessage message = DomainEntityCreatorForPersistenceTests.createMessage("superid23");
        message.getMessageDetails().setEbmsMessageId(ebmsId);
        message.getMessageDetails().setBackendMessageId("fklwefa");
        
        DomibusConnectorBigDataReferenceMemoryBacked attachRef = 
                new DomibusConnectorBigDataReferenceMemoryBacked("hallo welt".getBytes());
        DomibusConnectorMessageAttachment attach1 = 
                new DomibusConnectorMessageAttachment(attachRef, "idf");        
        message.addAttachment(attach1);
        
        DomibusConnectorMessageConfirmation confirmation = new DomibusConnectorMessageConfirmation(DomibusConnectorEvidenceType.DELIVERY, "hallowelt".getBytes());
        message.addConfirmation(confirmation);
        
        
        DomibusConnectorMessageDirection messageDirection = DomibusConnectorMessageDirection.GW_TO_NAT;
        messagePersistenceService.persistMessageIntoDatabase(message, messageDirection);
                
        
        
        //load persisted message again from db and run checks
        DomibusConnectorMessage messageToCheck = messagePersistenceService.findMessageByEbmsId(ebmsId);
        
        assertThat(messageToCheck.getMessageContent()).isNotNull();
        
        assertThat(messageToCheck.getMessageAttachments()).as("should contain two attachments!").hasSize(2);
        DomibusConnectorMessageAttachment messageAttachment = messageToCheck
                .getMessageAttachments()
                .stream()
                .filter(a -> "idf".equals(a.getIdentifier()))
                .findFirst()
                .get();


    }

    @Test(timeout=20000)
    public void testMergeMessageWithDatabase() throws PersistenceException, SQLException, AmbiguousTableNameException, DataSetException {
        DomibusConnectorMessage message = DomainEntityCreatorForPersistenceTests.createMessage("superid");
        message.getMessageDetails().setEbmsMessageId("ebamdsafae3");
        message.getMessageDetails().setBackendMessageId("adfsöljabafklwefa");
        
        DomibusConnectorMessageDirection messageDirection = DomibusConnectorMessageDirection.GW_TO_NAT;
        messagePersistenceService.persistMessageIntoDatabase(message, messageDirection);
                
        //TODO: make changes to message

        message = messagePersistenceService.mergeMessageWithDatabase(message);


        //message.getMessageDetails()

    }

    @Test(timeout=20000, expected=PersistenceException.class)
    public void testMergeMessageWithDatabase_doesNotExistInDatabase() throws PersistenceException {
        DomibusConnectorMessage message = DomainEntityCreatorForPersistenceTests.createMessage();
        messagePersistenceService.mergeMessageWithDatabase(message);
    }

    @Test(timeout=20000)
    public void testFindMessageByNationalId_doesNotExist_shouldBeNull() {
        String nationalIdString = "TEST1";
        DomibusConnectorMessage findMessageByNationalId = messagePersistenceService.findMessageByNationalId(nationalIdString);

        assertThat(findMessageByNationalId).isNull();
    }


    //TODO: test find message & check fromParty, toParty, service, action

    @Test(timeout=20000)
    public void findMessageBy() {
        messagePersistenceService.findMessageByConnectorMessageId("msg1");

    }


    
    
    //TODO: test other methods/use cases
    /**
     *  void persistMessageIntoDatabase(Message message, MessageDirection direction) throws PersistenceException;

    void mergeMessageWithDatabase(Message message);

    void persistEvidenceForMessageIntoDatabase(Message message, byte[] evidence, EvidenceType evidenceType);

    Message findMessageByNationalId(String nationalMessageId);

    Message findMessageByEbmsId(String ebmsMessageId);

    void setEvidenceDeliveredToGateway(Message message, EvidenceType evidenceType);

    void setEvidenceDeliveredToNationalSystem(Message message, EvidenceType evidenceType);

    void setDeliveredToGateway(Message message);

    void setMessageDeliveredToNationalSystem(Message message);

    List<Message> findOutgoingUnconfirmedMessages();

    List<Message> findIncomingUnconfirmedMessages();

    void confirmMessage(Message message);

    void rejectMessage(Message message);

    DomibusConnectorAction getAction(String action);

    DomibusConnectorAction getRelayREMMDAcceptanceRejectionAction();

    DomibusConnectorAction getRelayREMMDFailure();

    DomibusConnectorAction getDeliveryNonDeliveryToRecipientAction();

    DomibusConnectorAction getRetrievalNonRetrievalToRecipientAction();

    DomibusConnectorService getService(String service);

    DomibusConnectorParty getParty(String partyId, String role);

    DomibusConnectorParty getPartyByPartyId(String partyId);

    List<Message> findMessagesByConversationId(String conversationId);

    void persistMessageError(MessageError messageError);

    List<MessageError> getMessageErrors(Message message) throws Exception;

    void persistMessageErrorFromException(Message message, Throwable ex, Class<?> source) throws PersistenceException;

	List<Message> findOutgoingMessagesNotRejectedAndWithoutDelivery();

	List<Message> findOutgoingMessagesNotRejectedNorConfirmedAndWithoutRelayREMMD();
     */
}
