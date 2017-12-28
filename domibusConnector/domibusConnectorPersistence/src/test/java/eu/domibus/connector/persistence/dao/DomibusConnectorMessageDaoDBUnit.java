/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.domibus.connector.persistence.dao;

import eu.domibus.connector.persistence.model.DomibusConnectorMessage;
import eu.domibus.connector.persistence.model.test.util.PersistenceEntityCreator;
import eu.domibus.connector.persistence.service.CommonPersistenceDBUnitITCase;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author {@literal Stephan Spindler <stephan.spindler@extern.brz.gv.at> }
 */
public class DomibusConnectorMessageDaoDBUnit extends CommonPersistenceDBUnitITCase {

    private DomibusConnectorMessageDao messageDao;

    
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.messageDao = applicationContext.getBean(DomibusConnectorMessageDao.class);
        
//        //Load testdata
        IDataSet dataSet = new FlatXmlDataSetBuilder().setColumnSensing(true).build((new ClassPathResource("database/testdata/dbunit/DomibusConnectorMessage.xml").getInputStream()));
        
        DatabaseDataSourceConnection conn = new DatabaseDataSourceConnection(ds);        
        DatabaseOperation.CLEAN_INSERT.execute(conn, dataSet);
        
    }
    
    
    @Test
    public void testFindOneMessage() {
        DomibusConnectorMessage msg = messageDao.findOne(73L);
        assertThat(msg).isNotNull();
        assertThat(msg.getHashValue()).isEqualTo("31fb9a629e9640c4723cbd101adafd32");        
    }
    
    @Test
    public void testFindOneMessage_doesNotExist_shouldRetNull() {
        DomibusConnectorMessage msg = messageDao.findOne(7231254123L);
        assertThat(msg).isNull();        
    }
    
    @Test
    public void testFindOneByEbmsMessageId() {
        DomibusConnectorMessage msg = messageDao.findOneByEbmsMessageId("c1039627-2db3-489c-af18-92b54e630b36@domibus.eu");
        assertThat(msg).isNotNull();
        assertThat(msg.getHashValue()).isEqualTo("31fb9a629e9640c4723cbd101adafd32");       
    }
    
    @Test
    public void testFindByConversationId() {
        List<DomibusConnectorMessage> msgs = messageDao.findByConversationId("aa062b42-2d35-440a-9cb6-c6e95a8679a8@domibus.eu");
        assertThat(msgs).hasSize(1);
        //assertThat(msg.getHashValue()).isEqualTo("31fb9a629e9640c4723cbd101adafd32");       
    }
    
    
    @Test
    public void testFindOutgoingUnconfirmedMessages() {
        List<DomibusConnectorMessage> msgs = messageDao.findOutgoingUnconfirmedMessages();
        assertThat(msgs).hasSize(2);
    }
    
    
    @Test
    public void testFindOutgoingMessagesNotRejectedAndWithoutDelivery() {
        List<DomibusConnectorMessage> msgs = messageDao.findOutgoingMessagesNotRejectedAndWithoutDelivery();
        assertThat(msgs).hasSize(1);
    }
    
    @Test
    public void testFindOutgoingMessagesNotRejectedNorConfirmedAndWithoutRelayREMMD() {
        List<DomibusConnectorMessage> msgs = messageDao.findOutgoingMessagesNotRejectedNorConfirmedAndWithoutRelayREMMD();
        assertThat(msgs).hasSize(1);
    }
    
    @Test
    public void testFindIncomingUnconfirmedMessages() {
        List<DomibusConnectorMessage> msgs = messageDao.findIncomingUnconfirmedMessages();
        assertThat(msgs).hasSize(1);
    }
    
    @Test(timeout=2000)
    public void testConfirmMessage() throws SQLException, DataSetException {
        int upd = messageDao.confirmMessage(74L);
        
        //check result in DB        
        DatabaseDataSourceConnection conn = new DatabaseDataSourceConnection(ds);
        QueryDataSet dataSet = new QueryDataSet(conn);
        dataSet.addTable("DOMIBUS_CONNECTOR_MESSAGE", "SELECT * FROM DOMIBUS_CONNECTOR_MESSAGE WHERE ID=74");
       
        ITable domibusConnectorTable = dataSet.getTable("DOMIBUS_CONNECTOR_MESSAGE");
        Date value = (Date) domibusConnectorTable.getValue(0, "CONFIRMED");
        assertThat(value).isCloseTo(new Date(), 2000);
        
        assertThat(upd).as("one row must be updated").isEqualTo(1);
    }
    
    @Test
    public void testRejectMessage() throws SQLException, DataSetException  {
        int upd = messageDao.rejectMessage(73L);
        
        //check result in DB
        DatabaseDataSourceConnection conn = new DatabaseDataSourceConnection(ds);
        QueryDataSet dataSet = new QueryDataSet(conn);
        dataSet.addTable("DOMIBUS_CONNECTOR_MESSAGE", "SELECT * FROM DOMIBUS_CONNECTOR_MESSAGE WHERE ID=73");
       
        ITable domibusConnectorTable = dataSet.getTable("DOMIBUS_CONNECTOR_MESSAGE");
        Date value = (Date) domibusConnectorTable.getValue(0, "REJECTED");
        assertThat(value).isCloseTo(new Date(), 2000);
        
        assertThat(upd).as("one row must be updated!").isEqualTo(1);
    }
    
    @Test
    public void testRejectedMessage_notExisting() {
        int upd = messageDao.rejectMessage(21321315123123L);
        
        assertThat(upd).as("there should be no updates!").isEqualTo(0);
    }
    
    @Test
    public void testSetMessageDeliveredToGateway() throws SQLException, AmbiguousTableNameException, DataSetException {
        
        int upd = messageDao.setMessageDeliveredToGateway(73L);
        
        assertThat(upd).as("exactly one row should be updated!").isEqualTo(1);
        
        //check result in DB
        DatabaseDataSourceConnection conn = new DatabaseDataSourceConnection(ds);
        QueryDataSet dataSet = new QueryDataSet(conn);
        dataSet.addTable("DOMIBUS_CONNECTOR_MESSAGE", "SELECT * FROM DOMIBUS_CONNECTOR_MESSAGE WHERE ID=73");
       
        ITable domibusConnectorTable = dataSet.getTable("DOMIBUS_CONNECTOR_MESSAGE");
        Date value = (Date) domibusConnectorTable.getValue(0, "delivered_gw");
        assertThat(value).isCloseTo(new Date(), 2000);

    }
    
    @Test
    public void testSetmessageDeliveredToBackend() throws SQLException, AmbiguousTableNameException, DataSetException {
        int upd = messageDao.setMessageDeliveredToBackend(74L);
        
        assertThat(upd).as("exactly one row should be updated!").isEqualTo(1);
        
        //check result in DB
        DatabaseDataSourceConnection conn = new DatabaseDataSourceConnection(ds);
        QueryDataSet dataSet = new QueryDataSet(conn);
        dataSet.addTable("DOMIBUS_CONNECTOR_MESSAGE", "SELECT * FROM DOMIBUS_CONNECTOR_MESSAGE WHERE ID=74");
       
        ITable domibusConnectorTable = dataSet.getTable("DOMIBUS_CONNECTOR_MESSAGE");
        Date value = (Date) domibusConnectorTable.getValue(0, "delivered_backend");
        assertThat(value).isCloseTo(new Date(), 2000);
        
    }
    
    @Test
    public void testSave() {
        DomibusConnectorMessage message = PersistenceEntityCreator.createSimpleDomibusConnectorMessage();
        
        message.setId(null);
        
        messageDao.save(message);
                
    }
    
}
