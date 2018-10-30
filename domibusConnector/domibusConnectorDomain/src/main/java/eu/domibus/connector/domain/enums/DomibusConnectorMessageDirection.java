package eu.domibus.connector.domain.enums;

public enum DomibusConnectorMessageDirection {
    /**
     * used to mark a transport which is received by the backend
     * and transported to the gateway
     */
    BACKEND_TO_GATEWAY,
    /**
     * used to mark a transport which is received by the gateway
     * and transported to the backend
     */
    GATEWAY_TO_BACKEND,
    /**
     * used to mark a transport which is generated by the connector
     * (usually confirmation messages, like relayREMMDRejection, deliveryRejection
     * and transported to the gateway
     */
    CONNECTOR_TO_GATEWAY,
    /**
     * used to mark a transport which is generated by the connector
     * (usually confirmation messages, like SUBMISSION_ACCEPTANCE, SUBMISSION_REJECTION
     * and transported back to the gateway
     */
    CONNECTOR_TO_BACKEND;

}
