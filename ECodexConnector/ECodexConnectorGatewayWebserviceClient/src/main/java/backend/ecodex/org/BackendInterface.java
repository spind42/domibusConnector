package backend.ecodex.org;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.6.1
 * 2012-08-08T10:04:35.429+02:00
 * Generated source version: 2.6.1
 * 
 */
@WebService(targetNamespace = "http://org.ecodex.backend", name = "BackendInterface")
@XmlSeeAlso({org.xmlsoap.schemas.soap.envelope.ObjectFactory.class, org.w3._2003._05.soap_envelope.ObjectFactory.class, org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.ObjectFactory.class, ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface BackendInterface {

    @WebMethod
    public void downloadMessage(
        @WebParam(partName = "downloadMessageRequest", name = "downloadMessageRequest", targetNamespace = "http://org.ecodex.backend")
        DownloadMessageRequest downloadMessageRequest,
        @WebParam(partName = "downloadMessageResponse", mode = WebParam.Mode.OUT, name = "downloadMessageResponse", targetNamespace = "http://org.ecodex.backend")
        javax.xml.ws.Holder<DownloadMessageResponse> downloadMessageResponse,
        @WebParam(partName = "ebMSHeaderInfo", mode = WebParam.Mode.OUT, name = "Messaging", targetNamespace = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", header = true)
        javax.xml.ws.Holder<org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging> ebMSHeaderInfo
    ) throws DownloadMessageFault;

    @WebMethod
    public void sendMessageWithReference(
        @WebParam(partName = "sendRequestURL", name = "sendRequestURL", targetNamespace = "http://org.ecodex.backend")
        SendRequestURL sendRequestURL,
        @WebParam(partName = "ebMSHeaderInfo", name = "Messaging", targetNamespace = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", header = true)
        org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging ebMSHeaderInfo
    ) throws SendMessageWithReferenceFault;

    @WebMethod
    public void sendMessage(
        @WebParam(partName = "sendRequest", name = "sendRequest", targetNamespace = "http://org.ecodex.backend")
        SendRequest sendRequest,
        @WebParam(partName = "ebMSHeaderInfo", name = "Messaging", targetNamespace = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", header = true)
        org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging ebMSHeaderInfo
    ) throws SendMessageFault;

    @WebResult(name = "listPendingMessagesResponse", targetNamespace = "http://org.ecodex.backend", partName = "listPendingMessagesResponse")
    @WebMethod
    public ListPendingMessagesResponse listPendingMessages(
        @WebParam(partName = "listPendingMessagesRequest", name = "listPendingMessagesRequest", targetNamespace = "http://org.ecodex.backend")
        java.lang.Object listPendingMessagesRequest
    ) throws ListPendingMessagesFault;
}