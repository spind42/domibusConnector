package eu.domibus.connector.domain.transformer;

import eu.domibus.connector.domain.enums.DomibusConnectorEvidenceType;
import eu.domibus.connector.domain.model.DetachedSignatureMimeType;
import eu.domibus.connector.domain.model.DomibusConnectorMessage;
import eu.domibus.connector.domain.model.DomibusConnectorMessageAttachment;
import eu.domibus.connector.domain.model.DomibusConnectorMessageConfirmation;
import eu.domibus.connector.domain.model.DomibusConnectorMessageContent;
import eu.domibus.connector.domain.model.DomibusConnectorMessageDetails;
import eu.domibus.connector.domain.model.DomibusConnectorMessageDocument;
import eu.domibus.connector.domain.model.DomibusConnectorMessageError;
import eu.domibus.connector.domain.model.builder.DomibusConnectorMessageContentBuilder;
import eu.domibus.connector.domain.model.builder.DomibusConnectorMessageDocumentBuilder;
import eu.domibus.connector.domain.testutil.DomainEntityCreator;
import eu.domibus.connector.domain.transformer.DomibusConnectorDomainMessageTransformer.CannotBeMappedToTransitionException;
import static eu.domibus.connector.domain.transformer.DomibusConnectorDomainMessageTransformer.transformDomainToTransition;
import eu.domibus.connector.domain.transition.DomibusConnectorDetachedSignatureType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageAttachmentType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageConfirmationType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageContentType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageDetailsType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageDocumentType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageErrorType;
import eu.domibus.connector.domain.transition.DomibusConnectorMessageType;
import eu.domibus.connector.domain.transition.testutil.TransitionCreator;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataHandler;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.Ignore;
import org.springframework.util.StreamUtils;

/**
 *
 *
 */
public class DomibusConnectorDomainMessageTransformerTest {
    
    public DomibusConnectorDomainMessageTransformerTest() {
    }

    
    @Test
    public void testTransformDomainToTransition() {
        DomibusConnectorMessage domainMessage = DomainEntityCreator.createMessage();
        
        DomibusConnectorMessageType messageType = DomibusConnectorDomainMessageTransformer.transformDomainToTransition(domainMessage);
                
        assertThat(messageType).as("transformed object is not allowed to be null").isNotNull();
        
        assertThat(messageType.getMessageDetails()).as("message details are not allowed to be null!").isNotNull();
        assertThat(messageType.getMessageContent()).as("message content is set in test entity!").isNotNull();
        assertThat(messageType.getMessageConfirmations()).as("must have 1 confirmation").hasSize(1); 
        assertThat(messageType.getMessageAttachments()).as("must have 1 message attachment").hasSize(1);
        assertThat(messageType.getMessageErrors()).as("must have 1 message error!").hasSize(1);
    }
    
    @Test(expected=CannotBeMappedToTransitionException.class)
    public void testTransformDomainToTransition_finalRecipientIsNull_shouldThrowException() {
        DomibusConnectorMessage domainMessage = DomainEntityCreator.createMessage();        
        domainMessage.getMessageDetails().setFinalRecipient(null);        
        DomibusConnectorMessageType messageType = DomibusConnectorDomainMessageTransformer.transformDomainToTransition(domainMessage);
    }
    
    @Test(expected=CannotBeMappedToTransitionException.class)
    public void testTransformDomainToTransition_originalSenderIsNull_shouldThrowException() {
        DomibusConnectorMessage domainMessage = DomainEntityCreator.createMessage();        
        domainMessage.getMessageDetails().setOriginalSender(null);        
        DomibusConnectorMessageType messageType = DomibusConnectorDomainMessageTransformer.transformDomainToTransition(domainMessage);
    }
    
    
    
    @Test(expected=CannotBeMappedToTransitionException.class)
    public void testTransformDomainToTransition_messageContentIsNull_shouldThrowException() {
        DomibusConnectorMessageConfirmation createMessageDeliveryConfirmation = DomainEntityCreator.createMessageDeliveryConfirmation();
        DomibusConnectorMessage domainMessage = new DomibusConnectorMessage(null, createMessageDeliveryConfirmation);
        DomibusConnectorDomainMessageTransformer.transformDomainToTransition(domainMessage);
    }
            
    @Test
    public void testTransformMessageConfirmationDomainToTransition() {
        DomibusConnectorMessageConfirmation messageDeliveryConfirmation = DomainEntityCreator.createMessageDeliveryConfirmation();
        DomibusConnectorMessageConfirmationType messageConfirmationTO = 
                DomibusConnectorDomainMessageTransformer.transformMessageConfirmationDomainToTransition(messageDeliveryConfirmation);
        
        //assertThat(messageConfirmationTO.getConfirmation()).isEqualTo("EVIDENCE1_DELIVERY".getBytes());
        assertThat(messageConfirmationTO.getConfirmation()).isNotNull(); //TODO: better check!
        assertThat(messageConfirmationTO.getConfirmationType().name()).isEqualTo(DomibusConnectorEvidenceType.DELIVERY.name());
        
    }
    
    @Test(expected=CannotBeMappedToTransitionException.class)
    public void testTransformMessageConfirmationDomainToTransition_getEvidenceIsNull_shouldThrowIllegalArgumentException() {
         DomibusConnectorMessageConfirmation messageDeliveryConfirmation = DomainEntityCreator.createMessageDeliveryConfirmation();
         messageDeliveryConfirmation.setEvidence(null); //set evidence to null to provoke exception
         
         DomibusConnectorMessageConfirmationType messageConfirmationTO = 
                DomibusConnectorDomainMessageTransformer.transformMessageConfirmationDomainToTransition(messageDeliveryConfirmation);         
    }
    
    @Test
    public void testTransformMessageAttachmentDomainToTransition() throws IOException {
        DomibusConnectorMessageAttachment messageAttachment = DomainEntityCreator.createSimpleMessageAttachment();
        
        DomibusConnectorMessageAttachmentType attachmentTO = 
                DomibusConnectorDomainMessageTransformer.transformMessageAttachmentDomainToTransition(messageAttachment);
                                
        assertThat(attachmentTO.getAttachment()).isNotNull(); //TODO: better check!        
        assertThat(attachmentTO.getIdentifier()).isEqualTo("identifier");
        compareDataHandlerContent(attachmentTO.getAttachment(), "attachment");      
        
        assertThat(attachmentTO.getMimeType()).isEqualTo("application/garbage");
        assertThat(attachmentTO.getName()).isEqualTo("name");
        
    }
    
    private void compareDataHandlerContent(DataHandler dh, String content) {       
        try {
            InputStream is = dh.getInputStream();
            byte[] attachmentBytes = IOUtils.readBytesFromStream(is);
            assertThat(new String(attachmentBytes, "UTF-8")).isEqualTo(content);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    
    
    @Test(expected=CannotBeMappedToTransitionException.class)
    public void testTransformMessageConfirmationDomainToTransition_getEvidenceTypeIsNull_shouldThrowIllegalArgumentException() {
         DomibusConnectorMessageConfirmation messageDeliveryConfirmation = DomainEntityCreator.createMessageDeliveryConfirmation();
         messageDeliveryConfirmation.setEvidenceType(null); //set evidence to null to provoke exception
         
         DomibusConnectorMessageConfirmationType messageConfirmationTO = 
                DomibusConnectorDomainMessageTransformer.transformMessageConfirmationDomainToTransition(messageDeliveryConfirmation);         
    }
    
    @Test
    public void testTransformMessageContentDomainToTransition() {
        DomibusConnectorMessage domainMessage = DomainEntityCreator.createMessage();
        DomibusConnectorMessageContent messageContent = domainMessage.getMessageContent();
        
        DomibusConnectorMessageContentType messageContentTO = DomibusConnectorDomainMessageTransformer.transformMessageContentDomainToTransition(messageContent);        
                
        assertThat(messageContentTO).as("message content is set in test entity!").isNotNull();
        //assertThat(messageContentTO.getXmlContent()).isEqualTo(domainMessage.getMessageContent().getXmlContent());
        assertThat(messageContentTO.getXmlContent()).isNotNull(); //TODO better check?
        
        assertThat(messageContentTO.getDocument()).as("document of messageContent must be mapped!").isNotNull();                
    }
    
    @Test
    public void testTransformMessageContentDomainToTransition_testMapDocument() {
        DomibusConnectorMessage domainMessage = DomainEntityCreator.createMessage();        
       
 
        DomibusConnectorMessageContentType messageContentTO = 
                DomibusConnectorDomainMessageTransformer.transformMessageContentDomainToTransition(domainMessage.getMessageContent());        
        DomibusConnectorMessageDocumentType document = messageContentTO.getDocument();
                
        assertThat(document.getDocument()).isNotNull();
        compareDataHandlerContent(document.getDocument(), "documentbytes");
        //assertThat(document.getDocument()).isEqualTo("documentbytes".getBytes());
        assertThat(document.getDocumentName()).isEqualTo("Document1.pdf");
        assertThat(document.getDetachedSignature()).as("detached signature must not be null!").isNotNull();
        
        DomibusConnectorDetachedSignatureType detachedSignature = document.getDetachedSignature();
        //DetachedSignature detachedSignature = new DetachedSignature("detachedSignature".getBytes(), "signaturename", DetachedSignatureMimeType.BINARY);
        assertThat(detachedSignature.getDetachedSignature()).isEqualTo("detachedSignature".getBytes());
        assertThat(detachedSignature.getDetachedSignatureName()).isEqualTo("signaturename");
        assertThat(detachedSignature.getMimeType().name()).isEqualTo(DetachedSignatureMimeType.BINARY.name());
    }
    
    @Test
    public void testTransformMessageContentDomainToTransition_noDetachedSignature() {
        DomibusConnectorMessageContent messageContent = DomainEntityCreator.createMessageContentWithDocumentWithNoSignature();
        
        DomibusConnectorMessageContentType messageContentTO = DomibusConnectorDomainMessageTransformer.transformMessageContentDomainToTransition(messageContent);        
                
        assertThat(messageContentTO).as("message content is set in test entity!").isNotNull();
        //assertThat(messageContentTO.getXmlContent()).isEqualTo(domainMessage.getMessageContent().getXmlContent());
        assertThat(messageContentTO.getXmlContent()).isNotNull(); //TODO better check?
        
        assertThat(messageContentTO.getDocument()).as("document of messageContent must be mapped!").isNotNull();                
    }
    
    @Test
    public void testTransformMessageContentDomainToTransition_noPdfDocument() {
        DomibusConnectorMessageContent messageContent = DomainEntityCreator.createMessageContentWithDocumentWithNoPdfDocument();
        
        DomibusConnectorMessageContentType messageContentTO = DomibusConnectorDomainMessageTransformer.transformMessageContentDomainToTransition(messageContent);        
                
        assertThat(messageContentTO).as("message content is set in test entity!").isNotNull();
        //assertThat(messageContentTO.getXmlContent()).isEqualTo(domainMessage.getMessageContent().getXmlContent());
        assertThat(messageContentTO.getXmlContent()).isNotNull(); //TODO better check?
        
        assertThat(messageContentTO.getDocument()).as("document of messageContent must be null!").isNull();                
    }
    
    
    @Test
    public void testTransformMessageDetailsDomainToTransition() { 
        DomibusConnectorMessageDetails messageDetails = DomainEntityCreator.createDomibusConnectorMessageDetails();
        
        DomibusConnectorMessageDetailsType messageDetailsType = DomibusConnectorDomainMessageTransformer.transformMessageDetailsDomainToTransition(messageDetails);
                
        assertThat(messageDetailsType.getBackendMessageId()).as("backendMessageId must be mapped").isEqualTo("national1");
        assertThat(messageDetailsType.getEbmsMessageId()).as("ebmsMessageId must be mapped").isEqualTo("ebms1");
        assertThat(messageDetailsType.getConversationId()).as("conversationId must be mapped!").isEqualTo("conversation1");
        assertThat(messageDetailsType.getFinalRecipient()).as("finalRecipient must be mapped!").isEqualTo("finalRecipient");
        assertThat(messageDetailsType.getOriginalSender()).as("originalSender must be mapped!").isEqualTo("originalSender");
        assertThat(messageDetailsType.getRefToMessageId()).as("RefToMessageId must be mapped!").isEqualTo("refToMessageId");
        assertThat(messageDetailsType.getAction()).as("Action must be mapped!").isNotNull();
        assertThat(messageDetailsType.getFromParty()).as("FromParty must be mapped!").isNotNull();
        assertThat(messageDetailsType.getToParty()).as("toParty must be mapped!").isNotNull();
        assertThat(messageDetailsType.getService()).as("service must be mappted!").isNotNull();        
    }
    
    
    

    @Test
    public void testTransformTransitionToDomain() {
        DomibusConnectorMessage domainMessage = DomibusConnectorDomainMessageTransformer.transformTransitionToDomain(TransitionCreator.createMessage());
        
        assertThat(domainMessage).as("converted domainMessage must not be null!").isNotNull();
        assertThat(domainMessage.getMessageDetails()).as("message details must not be null!").isNotNull();
        assertThat(domainMessage.getMessageContent()).as("message content must not be null!").isNotNull();
        assertThat(domainMessage.getMessageConfirmations()).as("message confirmations contains 1!").hasSize(1);        
        assertThat(domainMessage.getMessageErrors()).as("message errors contains 1!").hasSize(1);        
        assertThat(domainMessage.getMessageAttachments()).as("message attachments contains 1!").hasSize(1);                
    }
    
    @Test
    public void testTransformTransitionToDomain_withMessageContentNull() {
        DomibusConnectorMessageType transitionMessage = TransitionCreator.createMessage();
        transitionMessage.setMessageContent(null);
        
        DomibusConnectorMessage domainMessage = DomibusConnectorDomainMessageTransformer.transformTransitionToDomain(transitionMessage);
        
        assertThat(domainMessage).as("converted domainMessage must not be null!").isNotNull();
        assertThat(domainMessage.getMessageDetails()).as("message details must not be null!").isNotNull();
        assertThat(domainMessage.getMessageContent()).as("message content must be null!").isNull();
        assertThat(domainMessage.getMessageConfirmations()).as("message confirmations contains 1!").hasSize(1);        
        assertThat(domainMessage.getMessageErrors()).as("message errors contains 1!").hasSize(1);        
        assertThat(domainMessage.getMessageAttachments()).as("message attachments contains 1!").hasSize(1);                
    }
    
    @Test
    public void testTransformMessageContentTransitionToDomain() {
        DomibusConnectorMessageContentType messageContentTO = TransitionCreator.createMessageContent();
        
        DomibusConnectorMessageContent messageContent = DomibusConnectorDomainMessageTransformer.transformMessageContentTransitionToDomain(messageContentTO);
        
        assertThat(messageContent).isNotNull();
        assertThat(messageContent.getXmlContent()).isNotNull(); //TODO compare byte[]
        assertThat(messageContent.getDocument()).isNotNull();
        
        DomibusConnectorMessageDocument document = messageContent.getDocument();
        assertThat(document.getDocument()).isNotNull();
        assertThat(document.getDetachedSignature()).isNotNull();
    }
    
    @Test
    public void testTransformMessageContentTransitionToDomain_withDocumentNull() {
        DomibusConnectorMessageContentType messageContentTO = TransitionCreator.createMessageContent();
        messageContentTO.setDocument(null);
        
        DomibusConnectorMessageContent messageContent = DomibusConnectorDomainMessageTransformer.transformMessageContentTransitionToDomain(messageContentTO);
        
        assertThat(messageContent).isNotNull();
        assertThat(messageContent.getXmlContent()).isNotNull(); //TODO compare byte[]
        assertThat(messageContent.getDocument()).isNull();
    }

    @Test
    public void testTransformMessageContentTransitionToDomain_withDocumentDetachedSignatureNull() {
        DomibusConnectorMessageContentType messageContentTO = TransitionCreator.createMessageContent();
        messageContentTO.getDocument().setDetachedSignature(null);
        
        DomibusConnectorMessageContent messageContent = DomibusConnectorDomainMessageTransformer.transformMessageContentTransitionToDomain(messageContentTO);
        
        assertThat(messageContent).isNotNull();
        assertThat(messageContent.getXmlContent()).isNotNull();
        assertThat(messageContent.getDocument().getDetachedSignature()).isNull();
    }
    

    @Test
    public void testTransformMessageDetailsTransitionToDomain() {
        DomibusConnectorMessageDetailsType messageDetailsTO = TransitionCreator.createMessageDetails();
        
        DomibusConnectorMessageDetails messageDetails = DomibusConnectorDomainMessageTransformer.transformMessageDetailsTransitionToDomain(messageDetailsTO);
        
        assertThat(messageDetails.getBackendMessageId()).as("backend message id must match").isEqualTo("backendMessageId");
        assertThat(messageDetails.getConversationId()).as("conversation id must match").isEqualTo("conversationId");
        assertThat(messageDetails.getFinalRecipient()).as("final recipient must match").isEqualTo("finalRecipient");
        assertThat(messageDetails.getOriginalSender()).as("original sender must match").isEqualTo("originalSender");
        assertThat(messageDetails.getRefToMessageId()).as("refToMessageid").isEqualTo("refToMessageId");
        
        assertThat(messageDetails.getAction().getAction()).isEqualTo("action");
        assertThat(messageDetails.getAction().isDocumentRequired()).isTrue(); 
        
        assertThat(messageDetails.getService().getService()).isEqualTo("service");
        assertThat(messageDetails.getService().getServiceType()).isEqualTo("serviceType");
        
        assertThat(messageDetails.getFromParty().getPartyId()).isEqualTo("AT");
        assertThat(messageDetails.getFromParty().getPartyIdType()).isEqualTo("urn:oasis:names:tc:ebcore:partyid-type:iso3166-1");
        assertThat(messageDetails.getFromParty().getRole()).isEqualTo("GW");
        
        assertThat(messageDetails.getToParty().getPartyId()).isEqualTo("DE");
        assertThat(messageDetails.getToParty().getPartyIdType()).isEqualTo("urn:oasis:names:tc:ebcore:partyid-type:iso3166-1");
        assertThat(messageDetails.getToParty().getRole()).isEqualTo("GW");
        
    }    
    
    @Test
    public void testTransformMessageAttachmentTransitionToDomain() throws IOException {
        DomibusConnectorMessageAttachmentType messageAttachmentTO = TransitionCreator.createMessageAttachment();
        DomibusConnectorMessageAttachment attachment = 
                DomibusConnectorDomainMessageTransformer.transformMessageAttachmentTransitionToDomain(messageAttachmentTO);    
        
        byte[] attachmentBytes = StreamUtils.copyToByteArray(attachment.getAttachment().getInputStream());
        
        assertThat(attachmentBytes).isEqualTo("attachment".getBytes());
        assertThat(attachment.getDescription()).isEqualTo("description");
        assertThat(attachment.getIdentifier()).isEqualTo("identifier");
        assertThat(attachment.getMimeType()).isEqualTo("application/octet-stream");
        assertThat(attachment.getName()).isEqualTo("name");
    }
    
    @Test
    public void testTransformMessageErrorTransitionToDomain() {
        DomibusConnectorMessageErrorType messageErrorTO = TransitionCreator.createMessageError();
        DomibusConnectorMessageError error = 
                DomibusConnectorDomainMessageTransformer.transformMessageErrorTransitionToDomain(messageErrorTO);
        
        assertThat(error.getDetails()).isEqualTo("error details");
        assertThat(error.getText()).isEqualTo("error message");
        assertThat(error.getSource()).isEqualTo("error source");
    }
    
    @Test
    public void testTransformMessageConfirmationTransitionToDomain() throws UnsupportedEncodingException {
        DomibusConnectorMessageConfirmationType messageConfirmationTO = TransitionCreator.createMessageConfirmationType_DELIVERY();
        DomibusConnectorMessageConfirmation confirmation = 
                DomibusConnectorDomainMessageTransformer.transformMessageConfirmationTransitionToDomain(messageConfirmationTO);
        
        //TODO: repair check!
        //assertThat(new String(confirmation.getEvidence(), "UTF-8")).isEqualTo("<DELIVERY></DELIVERY>");
        assertThat(confirmation.getEvidence()).isNotEmpty();
        assertThat(confirmation.getEvidenceType()).isEqualTo(DomibusConnectorEvidenceType.DELIVERY);

    }
}