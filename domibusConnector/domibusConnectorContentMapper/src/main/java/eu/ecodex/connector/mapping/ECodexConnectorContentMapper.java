package eu.ecodex.connector.mapping;

import eu.domibus.connector.common.exception.ImplementationMissingException;
import eu.domibus.connector.common.message.Message;
import eu.domibus.connector.common.message.MessageContent;
import eu.ecodex.connector.mapping.exception.ECodexConnectorContentMapperException;

/**
 * Interface with methods to map national format XML to eCodex format and vice
 * versa. Inheritance must be configured.
 * 
 * @author riederb
 * 
 */
public interface ECodexConnectorContentMapper {

    /**
     * 
     * Method to map international eCodex XML to national format. Must be
     * overridden when ContentMapper is used by configuration. The national xml
     * content will be written into the messageContent object.
     * 
     * @param messageContent
     *            - a {@link MessageContent} object containing the eCodex xml
     *            Content.
     * @throws ECodexConnectorContentMapperException
     * @throws ImplementationMissingException
     */
    public void mapInternationalToNational(Message message) throws ECodexConnectorContentMapperException,
            ImplementationMissingException;

    /**
     * Method to map national XML to international eCodex format. Must be
     * overridden when ContentMapper is used by configuration. The eCodex xml
     * content will be written into the messageContent object.
     * 
     * @param messageContent
     *            - a {@link MessageContent} object containing the national xml
     *            Content.
     * @throws ECodexConnectorContentMapperException
     * @throws ImplementationMissingException
     */
    public void mapNationalToInternational(Message message) throws ECodexConnectorContentMapperException,
            ImplementationMissingException;

}
