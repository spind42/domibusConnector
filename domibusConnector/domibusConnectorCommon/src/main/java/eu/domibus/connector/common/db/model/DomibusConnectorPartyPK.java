package eu.domibus.connector.common.db.model;

import java.io.Serializable;

import javax.persistence.Column;

public class DomibusConnectorPartyPK implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5954185507117364904L;

    @Column(name = "PARTY_ID")
    private String partyId;
    @Column(name = "ROLE")
    private String role;

    public DomibusConnectorPartyPK() {
    }

    public DomibusConnectorPartyPK(String partyId, String role) {
        super();
        this.partyId = partyId;
        this.role = role;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
