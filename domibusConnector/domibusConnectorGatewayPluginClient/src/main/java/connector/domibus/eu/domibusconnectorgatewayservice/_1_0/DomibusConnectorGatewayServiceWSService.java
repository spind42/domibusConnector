package connector.domibus.eu.domibusconnectorgatewayservice._1_0;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 3.1.3
 * 2017-11-28T15:09:33.988+01:00
 * Generated source version: 3.1.3
 * 
 */
@WebServiceClient(name = "DomibusConnectorGatewayServiceWSService", 
                  wsdlLocation = "file:/D:/Entwicklung/git/connector/domibusConnector/domibusConnectorGatewayPluginClient/src/main/resources/wsdl/DomibusConnectorGatewayService.wsdl",
                  targetNamespace = "http://eu.domibus.connector/DomibusConnectorGatewayService/1.0/") 
public class DomibusConnectorGatewayServiceWSService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://eu.domibus.connector/DomibusConnectorGatewayService/1.0/", "DomibusConnectorGatewayServiceWSService");
    public final static QName DomibusConnectorGatewayServiceWS = new QName("http://eu.domibus.connector/DomibusConnectorGatewayService/1.0/", "DomibusConnectorGatewayServiceWS");
    static {
        URL url = null;
        try {
            url = new URL("file:/D:/Entwicklung/git/connector/domibusConnector/domibusConnectorGatewayPluginClient/src/main/resources/wsdl/DomibusConnectorGatewayService.wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(DomibusConnectorGatewayServiceWSService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "file:/D:/Entwicklung/git/connector/domibusConnector/domibusConnectorGatewayPluginClient/src/main/resources/wsdl/DomibusConnectorGatewayService.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public DomibusConnectorGatewayServiceWSService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public DomibusConnectorGatewayServiceWSService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public DomibusConnectorGatewayServiceWSService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    public DomibusConnectorGatewayServiceWSService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    public DomibusConnectorGatewayServiceWSService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    public DomibusConnectorGatewayServiceWSService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }    




    /**
     *
     * @return
     *     returns DomibusConnectorGatewayServiceInterface
     */
    @WebEndpoint(name = "DomibusConnectorGatewayServiceWS")
    public DomibusConnectorGatewayServiceInterface getDomibusConnectorGatewayServiceWS() {
        return super.getPort(DomibusConnectorGatewayServiceWS, DomibusConnectorGatewayServiceInterface.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns DomibusConnectorGatewayServiceInterface
     */
    @WebEndpoint(name = "DomibusConnectorGatewayServiceWS")
    public DomibusConnectorGatewayServiceInterface getDomibusConnectorGatewayServiceWS(WebServiceFeature... features) {
        return super.getPort(DomibusConnectorGatewayServiceWS, DomibusConnectorGatewayServiceInterface.class, features);
    }

}
