package eu.domibus.connector.persistence.service.web;

import java.util.Date;
import java.util.List;

import eu.domibus.connector.web.dto.WebReportEntry;

public interface DomibusConnectorWebReportPersistenceService {

	public List<WebReportEntry> loadReportWithEvidences(Date fromDate, Date toDate);
	
	public List<WebReportEntry> loadReport(Date fromDate, Date toDate);
	
}
