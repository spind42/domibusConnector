package eu.ecodex.dc5.domain.scope;

import eu.ecodex.dc5.domain.CurrentBusinessDomain;
import eu.ecodex.dc5.domain.DCBusinessDomainManager;
import eu.domibus.connector.domain.model.DomibusConnectorBusinessDomain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyNameException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BusinessScopedPropertySource extends EnumerablePropertySource<DomibusConnectorBusinessDomain> {

    private final static Logger LOGGER = LogManager.getLogger(BusinessScopedPropertySource.class);

    private final ApplicationContext applicationContext;

    public BusinessScopedPropertySource(ApplicationContext applicationContext) {
        super("BusinessDomain");
        this.applicationContext = applicationContext;
    }

//    @Override
//    public String getName() {
//        return this.name;
//    }


    @Override
    public DomibusConnectorBusinessDomain getSource() {
        if (CurrentBusinessDomain.getCurrentBusinessDomain() != null) {
            DCBusinessDomainManager businessDomainManager = applicationContext.getBean(DCBusinessDomainManager.class);
            Optional<DomibusConnectorBusinessDomain> businessDomain = businessDomainManager.getBusinessDomain(CurrentBusinessDomain.getCurrentBusinessDomain());
            return businessDomain.orElseThrow(() -> new IllegalArgumentException("No Business Domain found for id" + CurrentBusinessDomain.getCurrentBusinessDomain()));
        } else {
            return DomibusConnectorBusinessDomain.getDefaultBusinessDomain();
        }
    }

    @Override
    public String getProperty(String name) {
        String value = null;
        Map<ConfigurationPropertyName, String> m = getPropertyMap();
        try {
            value = m.get(ConfigurationPropertyName.of(name));
        } catch (InvalidConfigurationPropertyNameException ne) {
            //ignore if property name is invalid
            return null;
        }

        LOGGER.trace("Resolved property [{}={}]", name, value);
        return value;
    }

    private Map<ConfigurationPropertyName, String> getPropertyMap() {
        Map<ConfigurationPropertyName, String> m = new HashMap<>();
        if (CurrentBusinessDomain.getCurrentBusinessDomain() != null) {
            DCBusinessDomainManager businessDomainManager = applicationContext.getBean(DCBusinessDomainManager.class);

            businessDomainManager.getBusinessDomain(CurrentBusinessDomain.getCurrentBusinessDomain())
                    .map(DomibusConnectorBusinessDomain::getProperties)
                    .orElse(new HashMap<>())
                    .forEach((key, v) -> m.put(ConfigurationPropertyName.of(key), v));

        }
        return m;
    }

    @Override
    @NonNull
    public String[] getPropertyNames() {
        return getPropertyMap()
                .keySet()
                .stream()
                .map(ConfigurationPropertyName::toString)
                .toArray(String[]::new);
    }

}