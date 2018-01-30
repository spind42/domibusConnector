
package eu.domibus.connector.ws.backend.link.spring;

import eu.domibus.connector.domain.transition.DomibsConnectorAcknowledgementType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.domain.transition.testutil.TransitionCreator;
import eu.domibus.connector.ws.backend.delivery.webservice.DomibusConnectorBackendDeliveryWSService;
import eu.domibus.connector.ws.backend.delivery.webservice.DomibusConnectorBackendDeliveryWebService;
import eu.domibus.connector.ws.backend.webservice.DomibusConnectorBackendWSService;
import eu.domibus.connector.ws.backend.webservice.DomibusConnectorBackendWebService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.MTOMFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.security.wss4j.PolicyBasedWSS4JStaxOutInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JStaxOutInterceptor;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author Stephan Spindler <stephan.spindler@extern.brz.gv.at>
 */
@RunWith(SpringRunner.class)
@Import(WSBackendLinkContextConfigurationITCase.TestConfiguration.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class WSBackendLinkContextConfigurationITCase {

    private final static Logger LOGGER = LoggerFactory.getLogger(WSBackendLinkContextConfigurationITCase.class);
    
    @SpringBootApplication(scanBasePackages="eu.domibus.connector.ws.backend.link", exclude = {
        DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})    
    public static class TestConfiguration {        
    }
    
    @LocalServerPort //use with WebEnvironment.RANDOM_PORT
    int port;
    
    @Value("${spring.webservices.path}")
    String webservicesPath;
//
//    @Autowired
//    GatewayLinkWsServiceProperties gatewayLinkWsServiceProperties;
//    
//    @MockBean
//    DomibusConnectorGatewayDeliveryService controllerDeliveryService;
    
    @Autowired
    DomibusConnectorBackendWebService backendWebService;
    
    @Autowired
    WSBackendLinkConfigurationProperties backendLinkConfigurationProperties;
    
//    @Before
//    public void setUp() {
//        Mockito.when(backendWebService.submitMessage(any(DomibusConnectorMessageType.class))).thenAnswer(new Answer<DomibsConnectorAcknowledgementType> () {
//            @Override
//            public DomibsConnectorAcknowledgementType answer(InvocationOnMock invocation) throws Throwable {
//                LOGGER.info("Message rcv: [{}]", invocation.getArgumentAt(0, DomibusConnectorMessageType.class));
//                DomibsConnectorAcknowledgementType answer = new DomibsConnectorAcknowledgementType();
//                answer.setResult(true);
//                return answer;
//            }
//        });        
//    }
    
    
    @Test
    public void testCallSoapService() throws InterruptedException, MalformedURLException {
        
        String publish = backendLinkConfigurationProperties.getBackendPublishAddress(); 
        
        String url = "http://localhost:" + port + webservicesPath + publish;
        System.out.println("URL " + url);
        
        System.out.println("sleep started server port " + port);
        Thread.sleep(30);
        System.out.println("sleep ended, calling service");        
        
        URL wsdlURL = new URL(url + "?wsdl"); 
        QName serviceName = DomibusConnectorBackendWSService.DomibusConnectorBackendWebService;
                            
        MTOMFeature mtom = new MTOMFeature();

        Service service = Service.create(wsdlURL, serviceName, mtom);
        
        
        DomibusConnectorBackendWebService backendWebServiceClient = service.getPort(DomibusConnectorBackendWebService.class);
        org.apache.cxf.endpoint.Client client = ClientProxy.getClient(backendWebServiceClient);
        org.apache.cxf.endpoint.Endpoint cxfEndpoint = client.getEndpoint();
        cxfEndpoint.getOutInterceptors().addAll(configureOutgoingInterceptors());
        
        DomibusConnectorMessageType msg = TransitionCreator.createMessage();
        
        DomibsConnectorAcknowledgementType response = backendWebServiceClient.submitMessage(msg);
        
        System.out.println("RESPONSE result: " + response.isResult());
        
        assertThat(response.isResult()).isTrue();
                
    }
    
    private  List<Interceptor<? extends Message>> configureOutgoingInterceptors() {
        List<Interceptor<? extends Message>> outInterceptors = new ArrayList<>();
        
        
        WSS4JStaxOutInterceptor outInterceptor = new PolicyBasedWSS4JStaxOutInterceptor();
        outInterceptor.setAllowMTOM(true);   
        
        Properties outProps = new Properties();
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        // Specify our username
        outProps.put(WSHandlerConstants.USER, "test");
        // Password type : plain text
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        // for hashed password use:
        //properties.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_DIGEST);
        // Callback used to retrieve password for given user.
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF,  new ClientPasswordCallback("test"));
        
        outInterceptors.add(outInterceptor);
        
        return outInterceptors;
    }
    
    
    
    
}

