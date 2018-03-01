package eu.domibus.connector.ws.backend.webservice;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 3.2.1
 * 2018-02-28T09:25:44.701+01:00
 * Generated source version: 3.2.1
 * 
 */
@WebServiceClient(name = "DomibusConnectorBackendWSService", 
                  wsdlLocation = "file:/C:/Entwicklung/git/connector/domibusConnector/domibusConnectorAPI/src/main/resources/wsdl/DomibusConnectorBackendWebService.wsdl",
                  targetNamespace = "http://connector.domibus.eu/ws/backend/webservice") 
public class DomibusConnectorBackendWSService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://connector.domibus.eu/ws/backend/webservice", "DomibusConnectorBackendWSService");
    public final static QName DomibusConnectorBackendWebService = new QName("http://connector.domibus.eu/ws/backend/webservice", "DomibusConnectorBackendWebService");
    static {
        URL url = null;
        try {
            url = new URL("file:/C:/Entwicklung/git/connector/domibusConnector/domibusConnectorAPI/src/main/resources/wsdl/DomibusConnectorBackendWebService.wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(DomibusConnectorBackendWSService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "file:/C:/Entwicklung/git/connector/domibusConnector/domibusConnectorAPI/src/main/resources/wsdl/DomibusConnectorBackendWebService.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public DomibusConnectorBackendWSService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public DomibusConnectorBackendWSService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public DomibusConnectorBackendWSService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    public DomibusConnectorBackendWSService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    public DomibusConnectorBackendWSService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    public DomibusConnectorBackendWSService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }    




    /**
     *
     * @return
     *     returns DomibusConnectorBackendWebService
     */
    @WebEndpoint(name = "DomibusConnectorBackendWebService")
    public DomibusConnectorBackendWebService getDomibusConnectorBackendWebService() {
        return super.getPort(DomibusConnectorBackendWebService, DomibusConnectorBackendWebService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns DomibusConnectorBackendWebService
     */
    @WebEndpoint(name = "DomibusConnectorBackendWebService")
    public DomibusConnectorBackendWebService getDomibusConnectorBackendWebService(WebServiceFeature... features) {
        return super.getPort(DomibusConnectorBackendWebService, DomibusConnectorBackendWebService.class, features);
    }

}
