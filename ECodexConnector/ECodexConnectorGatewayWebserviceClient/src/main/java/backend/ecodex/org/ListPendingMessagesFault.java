
package backend.ecodex.org;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.6.1
 * 2012-08-08T10:04:35.382+02:00
 * Generated source version: 2.6.1
 */

@WebFault(name = "FaultDetail", targetNamespace = "http://org.ecodex.backend")
public class ListPendingMessagesFault extends Exception {
    
    private backend.ecodex.org.FaultDetail faultDetail;

    public ListPendingMessagesFault() {
        super();
    }
    
    public ListPendingMessagesFault(String message) {
        super(message);
    }
    
    public ListPendingMessagesFault(String message, Throwable cause) {
        super(message, cause);
    }

    public ListPendingMessagesFault(String message, backend.ecodex.org.FaultDetail faultDetail) {
        super(message);
        this.faultDetail = faultDetail;
    }

    public ListPendingMessagesFault(String message, backend.ecodex.org.FaultDetail faultDetail, Throwable cause) {
        super(message, cause);
        this.faultDetail = faultDetail;
    }

    public backend.ecodex.org.FaultDetail getFaultInfo() {
        return this.faultDetail;
    }
}
