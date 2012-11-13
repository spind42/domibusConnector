package eu.ecodex.connector.evidences;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.annotation.Resource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.ecodex.connector.evidences.exception.EvidencesToolkitException;
import eu.ecodex.connector.evidences.type.RejectionReason;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test/context/testContext.xml")
public class ECodexConnectorEvidencesToolkitTest {

    private static Logger LOG = LoggerFactory.getLogger(ECodexConnectorEvidencesToolkitTest.class);

    @Resource
    private ECodexConnectorEvidencesToolkit evidencesToolkit;

    @Test
    public void testCreateSubmissionAcceptance() {
        LOG.debug("Started testCreateSubmissionAcceptance");
        try {
            byte[] evidence = evidencesToolkit.createSubmissionAcceptance("nationalMessageId1", new String(
                    "originalMessage").getBytes(), "someSenderAddress", "someRecipientAddress");
            String evidencePretty = prettyPrint(evidence);
            LOG.debug(evidencePretty);
        } catch (EvidencesToolkitException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
            Assert.fail();
        } catch (TransformerException e) {
            e.printStackTrace();
            Assert.fail();
        }
        LOG.debug("Finished testCreateSubmissionAcceptance");
    }

    @Test
    public void testCreateSubmissionRejection() {
        LOG.debug("Started testCreateSubmissionRejection");
        try {
            byte[] evidence = evidencesToolkit.createSubmissionRejection(RejectionReason.OTHER, "nationalMessageId1",
                    new String("originalMessage").getBytes(), "someSenderAddress", "someRecipientAddress");
            String evidencePretty = prettyPrint(evidence);
            LOG.debug(evidencePretty);
        } catch (EvidencesToolkitException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
            Assert.fail();
        } catch (TransformerException e) {
            e.printStackTrace();
            Assert.fail();
        }
        LOG.debug("Finished testCreateSubmissionRejection");
    }

    private String prettyPrint(byte[] input) throws TransformerFactoryConfigurationError, TransformerException {
        // Instantiate transformer input
        Source xmlInput = new StreamSource(new ByteArrayInputStream(input));
        StreamResult xmlOutput = new StreamResult(new StringWriter());

        // Configure transformer
        Transformer transformer = TransformerFactory.newInstance().newTransformer(); // An
                                                                                     // identity
                                                                                     // transformer
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "testing.dtd");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(xmlInput, xmlOutput);

        return xmlOutput.getWriter().toString();
    }

}