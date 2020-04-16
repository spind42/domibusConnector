
package eu.domibus.connector.backend.persistence.service;

import eu.domibus.connector.backend.domain.model.DomibusConnectorBackendClientInfo;
import eu.domibus.connector.backend.persistence.dao.BackendClientDao;
import eu.domibus.connector.backend.persistence.model.BackendClientInfo;
import eu.domibus.connector.backend.persistence.model.testutil.BackendPersistenceEntityCreator;
import eu.domibus.connector.domain.model.DomibusConnectorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;

/**
 *
 *
 */
public class BackendClientInfoPersistenceServiceImplTest {


    BackendClientDao backendClientDao;
    
    BackendClientInfoPersistenceServiceImpl backendInfoPersistenceService;
    
    @BeforeEach
    public void setUp() {
        backendClientDao = Mockito.mock(BackendClientDao.class);
        
        backendInfoPersistenceService = new BackendClientInfoPersistenceServiceImpl();
        backendInfoPersistenceService.setBackendClientDao(backendClientDao);
    }

    @Test
    public void testMapDbEntityToDomainEntity() {
        BackendClientInfo bob = createBackendClientInfoBob();
        
        DomibusConnectorBackendClientInfo domainBackendInfo = backendInfoPersistenceService.mapDbEntityToDomainEntity(bob);
     
        assertThat(domainBackendInfo.getBackendDescription()).isEqualTo("description");
        assertThat(domainBackendInfo.getBackendKeyAlias()).isEqualTo("keyalias");
        assertThat(domainBackendInfo.getBackendKeyPass()).isEqualTo("keypass");
        assertThat(domainBackendInfo.getBackendName()).isEqualTo("backendname");
        assertThat(domainBackendInfo.getBackendPushAddress()).isEqualTo("backendpushaddress");
        assertThat(domainBackendInfo.isDefaultBackend()).isTrue();
    }
    
    @Test
    public void testMapDbEntityToDomainEntity_entityIsNull_shouldReturnNull() {
        DomibusConnectorBackendClientInfo domainBackendInfo = backendInfoPersistenceService.mapDbEntityToDomainEntity(null);
        assertThat(domainBackendInfo).isNull();
    }

    //TODO: test mapping from domainToDbEntity
    
    @Test
    public void testGetBackendClientInfoByName() {
        BackendClientInfo bob = createBackendClientInfoBob();
        Mockito.when(backendClientDao.findOneBackendByBackendNameAndEnabledIsTrue(eq("bob"))).thenReturn(bob);
        DomibusConnectorBackendClientInfo backendClientInfoByName = backendInfoPersistenceService.getEnabledBackendClientInfoByName("bob");
        
        assertThat(backendClientInfoByName).isNotNull();        
    }

    @Test
    public void testFindByService() {
        BackendClientInfo bob = createBackendClientInfoBob();

        Mockito.when(backendClientDao.findByServicesAndEnabledIsTrue(eq("EPO-Service"))).thenReturn(Arrays.asList(new BackendClientInfo[] {bob}));

        DomibusConnectorService service = new DomibusConnectorService("EPO-Service", "");
        DomibusConnectorBackendClientInfo backendClientInfoByServiceName = backendInfoPersistenceService.getEnabledBackendClientInfoByService(service);

        assertThat(backendClientInfoByServiceName).isNotNull();
    }

    @Test
    public void testFindByService_multipleBackends_shouldThrow() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            BackendClientInfo bob = createBackendClientInfoBob();
            BackendClientInfo alice = createBackendClientInfoAlice();

            Mockito.when(backendClientDao.findByServicesAndEnabledIsTrue(eq("EPO-Service"))).thenReturn(Arrays.asList(new BackendClientInfo[] {bob, alice}));

            DomibusConnectorService service = new DomibusConnectorService("EPO-Service", "");
            DomibusConnectorBackendClientInfo backendClientInfoByServiceName = backendInfoPersistenceService.getEnabledBackendClientInfoByService(service);
        });
    }


    
    private BackendClientInfo createBackendClientInfoBob() {
        BackendClientInfo bob = BackendPersistenceEntityCreator.createBackendClientInfoBob();
        bob.setBackendDescription("description");
        bob.setBackendKeyAlias("keyalias");
        bob.setBackendKeyPass("keypass");
        bob.setBackendName("backendname");
        bob.setBackendPushAddress("backendpushaddress");
        bob.setDefaultBackend(true);
        bob.setId(20L);
        
        return bob;
    }

    private BackendClientInfo createBackendClientInfoAlice() {
        BackendClientInfo alice = BackendPersistenceEntityCreator.createBackendClientInfoBob();
        alice.setBackendName("alice");
        alice.setBackendDescription("description");
        alice.setBackendKeyAlias("keyalias");
        alice.setBackendKeyPass("keypass");
        alice.setBackendPushAddress("backendpushaddress");
        alice.setId(21L);

        return alice;
    }




}