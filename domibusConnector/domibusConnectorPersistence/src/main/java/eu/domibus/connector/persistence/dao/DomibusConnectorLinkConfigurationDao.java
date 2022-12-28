package eu.domibus.connector.persistence.dao;

import eu.domibus.connector.persistence.model.DC5LinkConfigJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DomibusConnectorLinkConfigurationDao extends JpaRepository<DC5LinkConfigJpaEntity, Long> {

    Optional<DC5LinkConfigJpaEntity> getOneByConfigName(String configName);

}
