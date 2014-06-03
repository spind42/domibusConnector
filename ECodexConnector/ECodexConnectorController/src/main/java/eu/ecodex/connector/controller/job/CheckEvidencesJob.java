package eu.ecodex.connector.controller.job;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ecodex.connector.controller.check.CheckOutgoing;
import eu.ecodex.connector.controller.exception.ECodexConnectorControllerException;
import eu.ecodex.connector.monitoring.ECodexConnectorMonitor;

public class CheckEvidencesJob {

    static Logger LOGGER = LoggerFactory.getLogger(CheckEvidencesJob.class);

    private CheckOutgoing checkOutgoing;
    private ECodexConnectorMonitor connectorMonitor;

    public void setConnectorMonitor(ECodexConnectorMonitor connectorMonitor) {
        this.connectorMonitor = connectorMonitor;
    }

    public void checkEvidences() throws ECodexConnectorControllerException {
        LOGGER.debug("Job for checking evidence timer triggered.");
        Date start = new Date();
        connectorMonitor.setLastCalledEvidenceTimeoutCheck(start);
        checkOutgoing.checkEvidences();

        LOGGER.debug("Job for checking evidence timer finished in {} ms.",
                (System.currentTimeMillis() - start.getTime()));
    }

    public void setCheckOutgoing(CheckOutgoing checkOutgoing) {
        this.checkOutgoing = checkOutgoing;
    }

}
