package eu.domibus.connector.controller.process;

import eu.domibus.connector.controller.exception.DomibusConnectorGatewaySubmissionException;
import eu.domibus.connector.controller.exception.DomibusConnectorMessageException;
import eu.domibus.connector.controller.service.DomibusConnectorBackendDeliveryService;
import eu.domibus.connector.controller.service.DomibusConnectorGatewaySubmissionService;
import eu.domibus.connector.domain.enums.DomibusConnectorEvidenceType;
import eu.domibus.connector.domain.enums.DomibusConnectorMessageDirection;
import eu.domibus.connector.domain.enums.DomibusConnectorRejectionReason;
import eu.domibus.connector.domain.model.DomibusConnectorMessage;
import eu.domibus.connector.domain.model.DomibusConnectorMessageConfirmation;
import eu.domibus.connector.domain.model.DomibusConnectorMessageDetails;
import eu.domibus.connector.domain.testutil.DomainEntityCreator;
import eu.domibus.connector.evidences.DomibusConnectorEvidencesToolkit;
import eu.domibus.connector.evidences.exception.DomibusConnectorEvidencesToolkitException;
import eu.domibus.connector.persistence.service.DomibusConnectorActionPersistenceService;
import eu.domibus.connector.persistence.service.DomibusConnectorEvidencePersistenceService;
import eu.domibus.connector.persistence.service.DomibusConnectorMessagePersistenceService;
import eu.domibus.connector.persistence.service.DomibusConnectorPersistAllBigDataOfMessageService;
import eu.domibus.connector.persistence.service.impl.BigDataWithMessagePersistenceServiceImpl;
import eu.domibus.connector.security.DomibusConnectorSecurityToolkit;
import eu.domibus.connector.security.exception.DomibusConnectorSecurityException;
import eu.domibus.connector.testutil.matcher.MockitoDomainMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;


public class BackendToGatewayMessageProcessorTest {

    @Mock
    private DomibusConnectorMessagePersistenceService messagePersistenceService;
    @Mock
    private DomibusConnectorEvidencePersistenceService evidencePersistenceService;
    @Mock
    private DomibusConnectorActionPersistenceService actionPersistenceService;
    @Mock
    private DomibusConnectorGatewaySubmissionService gwSubmissionService;
    @Mock
    private DomibusConnectorEvidencesToolkit evidencesToolkit;
    @Mock
    private DomibusConnectorSecurityToolkit securityToolkit;
    @Mock
    private DomibusConnectorBackendDeliveryService backendDeliveryService;
    @Mock
    private DomibusConnectorPersistAllBigDataOfMessageService bigDataPersistenceService;

    BackendToGatewayMessageProcessor backendToGatewayMessageProcessor;

    private List<DomibusConnectorMessage> toGwDeliveredMessages;

    private List<DomibusConnectorMessage> toBackendDeliveredMessages;

    @Before
    public void setUp() throws DomibusConnectorGatewaySubmissionException {
        MockitoAnnotations.initMocks(this);
        toGwDeliveredMessages = new ArrayList<>();
        toBackendDeliveredMessages = new ArrayList<>();
        backendToGatewayMessageProcessor = new BackendToGatewayMessageProcessor();
        backendToGatewayMessageProcessor.setActionPersistenceService(actionPersistenceService);
        backendToGatewayMessageProcessor.setEvidencePersistenceService(evidencePersistenceService);
        backendToGatewayMessageProcessor.setEvidencesToolkit(evidencesToolkit);
        backendToGatewayMessageProcessor.setGwSubmissionService(gwSubmissionService);
        backendToGatewayMessageProcessor.setActionPersistenceService(actionPersistenceService);
        backendToGatewayMessageProcessor.setMessagePersistenceService(messagePersistenceService);
        backendToGatewayMessageProcessor.setBackendDeliveryService(backendDeliveryService);
        backendToGatewayMessageProcessor.setSecurityToolkit(securityToolkit);
        backendToGatewayMessageProcessor.setMessageIdGenerator(() -> UUID.randomUUID().toString());
        backendToGatewayMessageProcessor.setBigDataPersistenceService(bigDataPersistenceService);
        
        Mockito.doAnswer( invoc -> toGwDeliveredMessages.add(invoc.getArgumentAt(0, DomibusConnectorMessage.class)))
                .when(gwSubmissionService).submitToGateway(any(DomibusConnectorMessage.class));

        Mockito.doAnswer( invoc -> toBackendDeliveredMessages.add(invoc.getArgumentAt(0, DomibusConnectorMessage.class)))
                .when(backendDeliveryService).deliverMessageToBackend(any(DomibusConnectorMessage.class));

        Mockito.when(bigDataPersistenceService.loadAllBigFilesFromMessage(any(DomibusConnectorMessage.class)))
                .thenAnswer(invoc -> invoc.getArgumentAt(0, DomibusConnectorMessage.class));

        
        
    }


    @Test
    public void testProcessMessage() throws DomibusConnectorEvidencesToolkitException {
        String backendMessageId = "backendmsg_1";
        DomibusConnectorMessage epoMessage = DomainEntityCreator.createEpoMessage();
        epoMessage.getMessageDetails().setBackendMessageId(backendMessageId);

        DomibusConnectorMessageConfirmation submissionAcceptanceConfirmation = DomainEntityCreator.createMessageSubmissionAcceptanceConfirmation();
        Mockito.when(evidencesToolkit.createEvidence(any(DomibusConnectorEvidenceType.class), any(DomibusConnectorMessage.class), eq(null), eq(null)))
                .thenReturn(submissionAcceptanceConfirmation);

        //test method
        backendToGatewayMessageProcessor.processMessage(epoMessage);

        //VERIFY
        //container should be built!
        Mockito.verify(securityToolkit, times(1)).buildContainer(eq(epoMessage));

        //verify that the evidence originalMessage is persisted into db (before put on queue!)
        Mockito.verify(messagePersistenceService, times(1))
                .persistMessageIntoDatabase(Mockito.argThat(MockitoDomainMatcher.eqToRefToMessageId("backendmsg_1")),
                        eq(DomibusConnectorMessageDirection.CONN_TO_NAT));


        //verify originalMessage deliverd to gw status is persisted
        Mockito.verify(messagePersistenceService, times(1)).setDeliveredToGateway(eq(epoMessage));

        //verify submission acceptance is delivered to gw
        assertThat(toGwDeliveredMessages).hasSize(1);
        DomibusConnectorMessage toGwMsg = toGwDeliveredMessages.get(0);
        assertThat(toGwMsg.getMessageConfirmations()).hasSize(1); //should contain one SUBMISSION_ACCEPTANCE evidence


        //verify originalMessage is handed over to backend for delivery
        assertThat(toBackendDeliveredMessages).hasSize(1);

        DomibusConnectorMessage message = toBackendDeliveredMessages.get(0);
        DomibusConnectorMessageDetails messageDetails = message.getMessageDetails();
        assertThat(messageDetails.getRefToMessageId()).isEqualTo(backendMessageId);
        assertThat(messageDetails.getBackendMessageId()).isEqualTo(backendMessageId);

    }

    @Test
    public void testProcessMessage_securityToolkitFailure() throws DomibusConnectorEvidencesToolkitException {
        DomibusConnectorMessage epoMessage = DomainEntityCreator.createEpoMessage();

        Mockito.doThrow(new DomibusConnectorSecurityException("A security toolkit error occured!"))
                .when(securityToolkit).buildContainer(any());

        DomibusConnectorMessageConfirmation submissionRejectionConfirmation = DomainEntityCreator.createMessageSubmissionRejectionConfirmation();
//        Mockito.when(evidencesToolkit.createEvidence(any(DomibusConnectorEvidenceType.class), any(DomibusConnectorMessage.class), eq(null), eq(null)))
//                .thenThrow(new DomibusConnectorEvidencesToolkitException("A error occured!"));

        Mockito.when(evidencesToolkit.createEvidence(eq(DomibusConnectorEvidenceType.SUBMISSION_REJECTION),
                    any(DomibusConnectorMessage.class),
                    eq(DomibusConnectorRejectionReason.OTHER),
                    any(String.class)))
                .thenReturn(submissionRejectionConfirmation);

        //test method
        try {
            backendToGatewayMessageProcessor.processMessage(epoMessage);
        } catch (DomibusConnectorMessageException ex) {
            assertThat(ex.getCause()).isOfAnyClassIn(DomibusConnectorSecurityException.class);
        }

        //VERIFY
        //container should be built!
        Mockito.verify(securityToolkit, times(1)).buildContainer(eq(epoMessage));

        Mockito.verify(evidencesToolkit, times(1))
                .createEvidence(eq(DomibusConnectorEvidenceType.SUBMISSION_REJECTION), eq(epoMessage), eq(DomibusConnectorRejectionReason.OTHER),
                any(String.class));

        //verify evidence is persisted!
        Mockito.verify(evidencePersistenceService, times(1)).persistEvidenceForMessageIntoDatabase(eq(epoMessage), eq(submissionRejectionConfirmation));


        //verify originalMessage rejection status is persisted
        Mockito.verify(messagePersistenceService, times(0)).setDeliveredToGateway(any());


        //verify rejection is delivered to the backend
        assertThat(toBackendDeliveredMessages).hasSize(1);
        Mockito.verify(evidencePersistenceService, times(1)).setEvidenceDeliveredToNationalSystem(eq(epoMessage), eq(submissionRejectionConfirmation));

    }

    @Test
    public void testProcessMessage_gwSubmissionFailure() throws DomibusConnectorEvidencesToolkitException, DomibusConnectorGatewaySubmissionException {
        DomibusConnectorMessage epoMessage = DomainEntityCreator.createEpoMessage();

//        Mockito.doThrow(new DomibusConnectorSecurityException("A security toolkit error occured!"))
//                .when(securityToolkit).buildContainer(any());

        Mockito.doThrow(new DomibusConnectorGatewaySubmissionException("GW Submission Failure!"))
                .when(gwSubmissionService).submitToGateway(any());

        DomibusConnectorMessageConfirmation submissionRejectionConfirmation = DomainEntityCreator.createMessageSubmissionRejectionConfirmation();
//        Mockito.when(evidencesToolkit.createEvidence(any(DomibusConnectorEvidenceType.class), any(DomibusConnectorMessage.class), eq(null), eq(null)))
//                .thenThrow(new DomibusConnectorEvidencesToolkitException("A error occured!"));

        Mockito.when(evidencesToolkit.createEvidence(eq(DomibusConnectorEvidenceType.SUBMISSION_REJECTION),
                any(DomibusConnectorMessage.class),
                eq(DomibusConnectorRejectionReason.OTHER),
                any(String.class)))
                .thenReturn(submissionRejectionConfirmation);

        //test method
        try {
            backendToGatewayMessageProcessor.processMessage(epoMessage);
        } catch (DomibusConnectorMessageException ex) {
            assertThat(ex.getCause()).isOfAnyClassIn(DomibusConnectorGatewaySubmissionException.class);
        }

        //VERIFY
        //container should be built!
        Mockito.verify(securityToolkit, times(1)).buildContainer(eq(epoMessage));

        Mockito.verify(evidencesToolkit, times(1))
                .createEvidence(eq(DomibusConnectorEvidenceType.SUBMISSION_REJECTION), eq(epoMessage), eq(DomibusConnectorRejectionReason.OTHER),
                        any(String.class));

        //verify evidence is persisted!
        Mockito.verify(evidencePersistenceService, times(1)).persistEvidenceForMessageIntoDatabase(eq(epoMessage), eq(submissionRejectionConfirmation));


        //verify originalMessage rejection status is persisted
        Mockito.verify(messagePersistenceService, times(0)).setDeliveredToGateway(any());


        //verify rejection is delivered to the backend
        assertThat(toBackendDeliveredMessages).hasSize(1);
        Mockito.verify(evidencePersistenceService, times(1)).setEvidenceDeliveredToNationalSystem(eq(epoMessage), eq(submissionRejectionConfirmation));
    }

}