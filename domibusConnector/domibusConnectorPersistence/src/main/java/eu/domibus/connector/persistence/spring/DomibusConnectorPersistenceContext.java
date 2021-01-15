package eu.domibus.connector.persistence.spring;

import eu.domibus.connector.persistence.model.PDomibusConnectorPersistenceModel;
import eu.domibus.connector.persistence.largefiles.provider.LargeFilePersistenceServiceFilesystemImpl;
import eu.domibus.connector.persistence.largefiles.provider.LargeFilePersistenceServiceJpaImpl;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import eu.domibus.connector.persistence.dao.PackageDomibusConnectorRepositories;

import javax.sql.DataSource;

import static eu.domibus.connector.persistence.spring.PersistenceProfiles.STORAGE_DB_PROFILE_NAME;
import static eu.domibus.connector.persistence.spring.PersistenceProfiles.STORAGE_FS_PROFILE_NAME;

/**
 *
 * @author {@literal Stephan Spindler <stephan.spindler@extern.brz.gv.at> }
 */
@Configuration
@EntityScan(basePackageClasses={PDomibusConnectorPersistenceModel.class})
@EnableJpaRepositories(basePackageClasses = {PackageDomibusConnectorRepositories.class} )
@EnableTransactionManagement
@PropertySource("classpath:/eu/domibus/connector/persistence/config/default-persistence-config.properties")
public class DomibusConnectorPersistenceContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomibusConnectorPersistenceContext.class);


    @Bean
    @Profile(STORAGE_FS_PROFILE_NAME)
    public LargeFilePersistenceServiceFilesystemImpl domibusConnectorBigDataPersistenceServiceFilesystemImpl() {
        return new LargeFilePersistenceServiceFilesystemImpl();
    }

    @Bean
    @Profile(STORAGE_DB_PROFILE_NAME)
    public LargeFilePersistenceServiceJpaImpl domibusConnectorBigDataPersistenceServiceJpaImpl() {
        return new LargeFilePersistenceServiceJpaImpl();
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSourceProperties dataSourceConfigurationProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        DataSourceProperties properties = dataSourceConfigurationProperties();
        LOGGER.debug("Creating data source with properties: [{}]", properties);
        return properties.initializeDataSourceBuilder().build();
    }

}
