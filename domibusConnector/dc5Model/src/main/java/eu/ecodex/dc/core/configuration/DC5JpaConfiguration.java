package eu.ecodex.dc.core.configuration;


import eu.ecodex.dc.core.model.DC5PersistenceSettings;
import eu.ecodex.dc.core.repository.DC5EbmsRepo;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackageClasses = DC5EbmsRepo.class)
@EntityScan(basePackageClasses = DC5PersistenceSettings.class)
public class DC5JpaConfiguration {

}
