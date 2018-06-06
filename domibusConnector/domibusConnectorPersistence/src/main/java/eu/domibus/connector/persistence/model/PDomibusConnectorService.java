package eu.domibus.connector.persistence.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "DOMIBUS_CONNECTOR_SERVICE")
public class PDomibusConnectorService {

    @Id
    @Column(name = "SERVICE")
    private String service;

    @Column(name = "SERVICE_TYPE", nullable = false)
    private String serviceType;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public String toString() {
        ToStringBuilder toString = new ToStringBuilder(this);
        toString.append("service", service);
        toString.append("serviceType", serviceType);
        return toString.build();
    }
}