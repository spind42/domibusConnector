
package eu.domibus.connector.gateway.link.spring;


import eu.domibus.connector.ws.gateway.delivery.webservice.DomibusConnectorGatewayDeliveryWSService;
import eu.domibus.connector.ws.gateway.delivery.webservice.DomibusConnectorGatewayDeliveryWebService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWSService;
import eu.domibus.connector.ws.gateway.submission.webservice.DomibusConnectorGatewaySubmissionWebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;


/**
 * Configures the GW-Link to use webservices
 * 
 */
@Configuration
@Profile("gwlink-ws")
@ImportResource("classpath:/spring/context/DomibusConnectorGatewayLinkContext.xml")
public class GatewayLinkContextConfiguration {

    private final static Logger LOGGER = LoggerFactory.getLogger(GatewayLinkContextConfiguration.class);

}