package eu.domibus.connector.ws.backend.delivery.webservice;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 3.2.1
 * 2018-02-28T09:25:44.591+01:00
 * Generated source version: 3.2.1
 * 
 */
@WebServiceClient(name = "DomibusConnectorBackendDeliveryWSService", 
                  wsdlLocation = "file:/C:/Entwicklung/git/connector/domibusConnector/domibusConnectorAPI/src/main/resources/wsdl/DomibusConnectorBackendDeliveryWebService.wsdl",
                  targetNamespace = "http://connector.domibus.eu/ws/backend/delivery/webservice/") 
public class DomibusConnectorBackendDeliveryWSService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://connector.domibus.eu/ws/backend/delivery/webservice/", "DomibusConnectorBackendDeliveryWSService");
    public final static QName DomibusConnectorBackendDeliveryWebService = new QName("http://connector.domibus.eu/ws/backend/delivery/webservice/", "DomibusConnectorBackendDeliveryWebService");
    static {
        URL url = null;
        try {
            url = new URL("file:/C:/Entwicklung/git/connector/domibusConnector/domibusConnectorAPI/src/main/resources/wsdl/DomibusConnectorBackendDeliveryWebService.wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(DomibusConnectorBackendDeliveryWSService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "file:/C:/Entwicklung/git/connector/domibusConnector/domibusConnectorAPI/src/main/resources/wsdl/DomibusConnectorBackendDeliveryWebService.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public DomibusConnectorBackendDeliveryWSService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public DomibusConnectorBackendDeliveryWSService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public DomibusConnectorBackendDeliveryWSService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    public DomibusConnectorBackendDeliveryWSService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    public DomibusConnectorBackendDeliveryWSService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    public DomibusConnectorBackendDeliveryWSService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }    




    /**
     *
     * @return
     *     returns DomibusConnectorBackendDeliveryWebService
     */
    @WebEndpoint(name = "DomibusConnectorBackendDeliveryWebService")
    public DomibusConnectorBackendDeliveryWebService getDomibusConnectorBackendDeliveryWebService() {
        return super.getPort(DomibusConnectorBackendDeliveryWebService, DomibusConnectorBackendDeliveryWebService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns DomibusConnectorBackendDeliveryWebService
     */
    @WebEndpoint(name = "DomibusConnectorBackendDeliveryWebService")
    public DomibusConnectorBackendDeliveryWebService getDomibusConnectorBackendDeliveryWebService(WebServiceFeature... features) {
        return super.getPort(DomibusConnectorBackendDeliveryWebService, DomibusConnectorBackendDeliveryWebService.class, features);
    }

}
