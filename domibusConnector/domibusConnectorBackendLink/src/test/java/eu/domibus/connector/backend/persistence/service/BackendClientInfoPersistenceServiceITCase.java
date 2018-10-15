package eu.domibus.connector.backend.persistence.service;

import eu.domibus.connector.backend.domain.model.DomibusConnectorBackendClientInfo;
import eu.domibus.connector.domain.model.DomibusConnectorService;
import eu.domibus.connector.persistence.testutil.SetupPersistenceContext;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class BackendClientInfoPersistenceServiceITCase {

    private static ConfigurableApplicationContext APPLICATION_CONTEXT;

    private DataSource dataSource;
    private ConfigurableApplicationContext applicationContext;
    private BackendClientInfoPersistenceService backendClientInfoPersistenceService;

    @SpringBootApplication(scanBasePackages={"eu.domibus.connector.persistence", "eu.domibus.connector.backend.persistence"})
    static class TestConfiguration {
    }

    @BeforeClass
    public static void beforeClass() {
        APPLICATION_CONTEXT = SetupPersistenceContext.startApplicationContext(TestConfiguration.class);
    }

    @Before
    public void setUp() throws IOException, DatabaseUnitException, SQLException {
        this.applicationContext = APPLICATION_CONTEXT;

        this.dataSource = applicationContext.getBean(DataSource.class);
        this.backendClientInfoPersistenceService = applicationContext.getBean(BackendClientInfoPersistenceService.class);

//        this.transactionTemplate = new TransactionTemplate(applicationContext.getBean(PlatformTransactionManager.class));

        //Load testdata
        IDataSet dataSet = new FlatXmlDataSetBuilder().setColumnSensing(true).build((new ClassPathResource("database/testdata/dbunit/BackendClient.xml").getInputStream()));

        DatabaseDataSourceConnection conn = new DatabaseDataSourceConnection(dataSource);
        DatabaseOperation.CLEAN_INSERT.execute(conn, dataSet);
    }

    @Test
    public void testUpdate() throws SQLException, DataSetException {
        DomibusConnectorBackendClientInfo backendClientInfo = new DomibusConnectorBackendClientInfo();
        backendClientInfo.setBackendName("alice");
        backendClientInfo.setBackendPushAddress("my-push-address");
        backendClientInfo.setBackendKeyAlias("key-alias");

        backendClientInfoPersistenceService.save(backendClientInfo);

        //check db
        DatabaseDataSourceConnection conn = new DatabaseDataSourceConnection(dataSource);
        ITable queryAlice = conn.createQueryTable("QUERY_ALICE", "SELECT * FROM DOMIBUS_CONNECTOR_BACKEND_INFO WHERE BACKEND_NAME = 'cn=alice'");

        BigDecimal id = (BigDecimal) queryAlice.getValue(0, "id");
        assertThat(id).isEqualTo(BigDecimal.valueOf(90));

    }

    @Test
    public void testLoadUpdate() throws SQLException, DataSetException {
        DomibusConnectorBackendClientInfo backendClientInfo = backendClientInfoPersistenceService.getBackendClientInfoByName("cn=alice");

        backendClientInfo.setDefaultBackend(true);
        backendClientInfo.setBackendPushAddress("my-push-address");
        backendClientInfo.setBackendKeyAlias("key-alias");

        backendClientInfoPersistenceService.save(backendClientInfo);

        //check db
        DatabaseDataSourceConnection conn = new DatabaseDataSourceConnection(dataSource);
        ITable queryAlice = conn.createQueryTable("QUERY_ALICE", "SELECT * FROM DOMIBUS_CONNECTOR_BACKEND_INFO WHERE BACKEND_NAME = 'cn=alice'");

        BigDecimal id = (BigDecimal) queryAlice.getValue(0, "id");
        assertThat(id).isEqualTo(BigDecimal.valueOf(90));
        String name = (String) queryAlice.getValue(0, "backend_name");
        assertThat(name).isEqualTo("cn=alice");

        //load again from db
        backendClientInfo = backendClientInfoPersistenceService.getBackendClientInfoByName("cn=alice");
        assertThat(backendClientInfo).isNotNull();

    }


    @Test
    public void findEnabledByName() {
        DomibusConnectorBackendClientInfo alice = backendClientInfoPersistenceService.getEnabledBackendClientInfoByName("cn=alice");
        assertThat(alice).isNotNull();
    }

    @Test
    public void findEnabledByName_isNotEnabled_shouldReturnNull() {
        DomibusConnectorBackendClientInfo notEnabledBackend = backendClientInfoPersistenceService.getEnabledBackendClientInfoByName("not_enabled");
        assertThat(notEnabledBackend).isNull();
    }

    @Test
    public void findEnabledByService() {
        DomibusConnectorService service = new DomibusConnectorService("EPO", "service_type");

        DomibusConnectorBackendClientInfo notEnabledBackend = backendClientInfoPersistenceService.getEnabledBackendClientInfoByService(service);
        assertThat(notEnabledBackend).isNotNull();
    }


}
