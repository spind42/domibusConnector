package eu.domibus.webadmin.spring;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import javax.annotation.PostConstruct;
import java.util.*;

@Configuration
public class PropertiesLogger {
    private static final Logger log = LoggerFactory.getLogger(PropertiesLogger.class);

    @Autowired
    private AbstractEnvironment environment;

    @PostConstruct
    public void printProperties() {

        log.info("**** APPLICATION PROPERTIES SOURCES ****");

        Set<String> properties = new TreeSet<>();
        for (PropertiesPropertySource p : findPropertiesPropertySources()) {
            log.debug(p.toString());
            properties.addAll(Arrays.asList(p.getPropertyNames()));
        }

        log.debug("**** APPLICATION PROPERTIES VALUES ****");
        print(properties);

    }

    private List<PropertiesPropertySource> findPropertiesPropertySources() {
        List<PropertiesPropertySource> propertiesPropertySources = new LinkedList<>();
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (propertySource instanceof PropertiesPropertySource) {
                propertiesPropertySources.add((PropertiesPropertySource) propertySource);
            }
        }
        return propertiesPropertySources;
    }

    private void print(Set<String> properties) {
        for (String propertyName : properties) {
            log.debug("{}={}", propertyName, environment.getProperty(propertyName));
        }
    }

}
