package eu.dc5.domain.model;

import eu.dc5.domain.repository.DC5EbmsRepo;
import eu.dc5.domain.repository.DC5MessageRepo;
import eu.dc5.domain.repository.DC5PayloadRepo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;


@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestPropertySource(properties = {
//        "spring.jpa.hibernate.ddl-auto=create", // apparently not needed
        "spring.jpa.show-sql=true"
})
class DC5MsgH2Tests {

    @Autowired
    private DC5MessageRepo msgRepo;

    @Autowired
    private DC5PayloadRepo payloadRepo;
    @Autowired
    private DC5EbmsRepo ebmsRepo;

    // Tests use the same H2 instance!

    @Test
    public void contextLoads() {
    }

    @Test
    public void can_persist_message() {
        final DC5MsgBusinessDocument dc5BusinessDocumentMessage = new DC5MsgBusinessDocument();
        final DC5MsgBusinessDocument save = msgRepo.save(dc5BusinessDocumentMessage);

        Assertions.assertThat(save.getId()).isGreaterThan(1000L);
    }

    @Test
    public void persisting_a_message_also_persits_ebms_segment() {
        // Arrange
        final DC5MsgBusinessDocument dc5BusinessDocumentMessage = new DC5MsgBusinessDocument();
        final DC5Ebms dc5Ebms = new DC5Ebms();
        dc5Ebms.setEbmsMessageId("foo");
        dc5BusinessDocumentMessage.setEbmsSegment(dc5Ebms);

        // Act
        final DC5MsgBusinessDocument save = msgRepo.save(dc5BusinessDocumentMessage);

        // Assert
//        Assertions.assertThat(Optional.empty()).isPresent(); // see it fail
        Assertions.assertThat(ebmsRepo.findByEbmsMessageId("foo")).isPresent();
    }

    @Test
    public void storing_a_message_with_payload_also_persists_the_payload() {
        // Arrange
        final DC5MsgBusinessDocument dc5BusinessDocumentMessage = new DC5MsgBusinessDocument();
        final DC5Payload payload = new DC5Payload(); // bidirectional mapping requires linking from both ends
        payload.setMessage(dc5BusinessDocumentMessage);
        dc5BusinessDocumentMessage.getPayload().add(payload);

        // Act
        final DC5MsgBusinessDocument save = msgRepo.saveAndFlush(dc5BusinessDocumentMessage);
        final DC5Payload persistedPayload = payloadRepo.findAll().get(0);

        // Assert
        Assertions.assertThat(persistedPayload.getMessage().getId()).isEqualTo(save.getId());
    }

    @Test
    public void can_persist_ebms_entity() {
        // Arrange
        final DC5Ebms dc5Ebms = new DC5Ebms();
        dc5Ebms.setSender(new DC5EcxAddress("ecxAddrSend", new DC5Party("ID_SENDER", "FOO"), new DC5Role("SENDER", "DIR")));
        dc5Ebms.setReceiver(new DC5EcxAddress("ecxAddrRec", new DC5Party("ID_RECEIVER", "BAZ"), new DC5Role("RECEIVER", "OPDIR")));

        // Act
        final Long id = ebmsRepo.save(dc5Ebms).getId();
        final DC5Ebms save = ebmsRepo.findById(id).get();

        // Assert
        Assertions.assertThat(save.getReceiver().getEcxAddress()).isEqualTo("ecxAddrRec");
        Assertions.assertThat(save.getReceiver().getParty().getPartyId()).isEqualTo("ID_RECEIVER");
        Assertions.assertThat(save.getReceiver().getRole().getRoleType()).isEqualTo("OPDIR");

        Assertions.assertThat(save.getSender().getEcxAddress()).isEqualTo("ecxAddrSend");
        Assertions.assertThat(save.getSender().getParty().getPartyId()).isEqualTo("ID_SENDER");
        Assertions.assertThat(save.getSender().getRole().getRoleType()).isEqualTo("DIR");
    }
}