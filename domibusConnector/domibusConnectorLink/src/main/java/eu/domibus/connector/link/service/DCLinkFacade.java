package eu.domibus.connector.link.service;

import com.google.common.base.CaseFormat;
import eu.domibus.connector.domain.enums.ConfigurationSource;
import eu.domibus.connector.domain.enums.LinkType;
import eu.domibus.connector.domain.model.DomibusConnectorLinkConfiguration;
import eu.domibus.connector.domain.model.DomibusConnectorLinkPartner;
import eu.domibus.connector.link.api.ActiveLinkPartner;
import eu.domibus.connector.link.api.exception.LinkPluginException;
import eu.domibus.connector.persistence.service.DCLinkPersistenceService;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ConditionalOnBean(DCLinkPluginConfiguration.class)
public class DCLinkFacade {

    private final DCActiveLinkManagerService linkManager;
    private final DCLinkPersistenceService dcLinkPersistenceService;
    private final DCLinkPluginConfigurationProperties lnkConfig;

    public DCLinkFacade(DCActiveLinkManagerService linkManager,
                        DCLinkPersistenceService dcLinkPersistenceService,
                        DCLinkPluginConfigurationProperties props) {
        this.linkManager = linkManager;
        this.dcLinkPersistenceService = dcLinkPersistenceService;
        this.lnkConfig = props;
    }


    public boolean isActive(DomibusConnectorLinkPartner d) {
        if (d.getLinkPartnerName() == null) {
            throw new IllegalArgumentException("LinkPartner name is not allowed to be null!");
        }
        return linkManager.getActiveLinkPartnerByName(d.getLinkPartnerName()).isPresent();
    }

    public void shutdownLinkPartner(DomibusConnectorLinkPartner linkPartner) {
        linkManager.shutdownLinkPartner(linkPartner.getLinkPartnerName());
    }

    public List<DomibusConnectorLinkPartner> getAllLinksOfType(LinkType linkType) {
        return getAllLinks().stream()
                .filter(l -> l.getLinkType() == linkType)
                .collect(Collectors.toList());
    }

    public List<DomibusConnectorLinkPartner> getAllLinks() {
        List<DomibusConnectorLinkPartner> allLinks = new ArrayList<>();

        allLinks.addAll(dcLinkPersistenceService.getAllLinks());
        allLinks.addAll(lnkConfig.getBackend().stream().flatMap(b -> mapCnfg(b, LinkType.BACKEND)).collect(Collectors.toList()));
        allLinks.addAll(mapCnfg(lnkConfig.getGateway(), LinkType.GATEWAY).collect(Collectors.toList()));

        return allLinks;
    }

    private Stream<DomibusConnectorLinkPartner> mapCnfg(DCLinkPluginConfigurationProperties.DCLnkPropertyConfig b, LinkType linkType) {
        DomibusConnectorLinkConfiguration linkConfig = new DomibusConnectorLinkConfiguration();
        BeanUtils.copyProperties(b.getLinkConfig(), linkConfig);
        linkConfig.setProperties(mapKebabCaseToCamelCase(b.getLinkConfig().getProperties()));
        linkConfig.setConfigurationSource(ConfigurationSource.ENV);
        return b.getLinkPartners().stream().map(p -> {
            DomibusConnectorLinkPartner p1 = new DomibusConnectorLinkPartner();
            BeanUtils.copyProperties(p, p1);
            p1.setConfigurationSource(ConfigurationSource.ENV);
            p1.setLinkConfiguration(linkConfig);
            p1.setLinkType(linkType);
            p1.setProperties(mapKebabCaseToCamelCase(p.getProperties()));
            return p1;
        });
    }

    //convert property names from KebabCase to CamelCase
    // eg.:  cn-name to cnName
    private Map<String, String> mapKebabCaseToCamelCase(Map<String, String> properties) {
        Map<String, String> map = properties
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, e.getKey()),e -> e.getValue()));
        return map;
    }

    public Optional<DomibusConnectorLinkPartner> loadLinkPartner(DomibusConnectorLinkPartner.LinkPartnerName name) {
        return getAllLinks().stream().filter(l -> name.equals(l.getLinkPartnerName())).findAny();
    }

    public void startLinkPartner(DomibusConnectorLinkPartner linkPartner) {
        Optional<ActiveLinkPartner> activeLinkPartner = this.linkManager.activateLinkPartner(linkPartner);
        if (!activeLinkPartner.isPresent()) {
            throw new LinkPluginException("Start failed!");
        }
    }

    public void deleteLinkPartner(DomibusConnectorLinkPartner linkPartner) {
        try {
            linkManager.shutdownLinkPartner(linkPartner.getLinkPartnerName());
        } catch (LinkPluginException exception) {
            //handle
        }
        if (ConfigurationSource.DB == linkPartner.getConfigurationSource()) {
            dcLinkPersistenceService.deleteLinkPartner(linkPartner);
        }

    }

    public void updateLinkPartner(DomibusConnectorLinkPartner linkPartner) {
        if (linkPartner.getConfigurationSource() == ConfigurationSource.DB) {
            dcLinkPersistenceService.updateLinkPartner(linkPartner);
        }
    }

    public Optional<DomibusConnectorLinkConfiguration> loadLinkConfig(DomibusConnectorLinkConfiguration.LinkConfigName configName) {
        List<DomibusConnectorLinkConfiguration> allConfigs = getAllConfigurations();
        return allConfigs.stream()
                .filter(c -> configName.equals(c.getConfigName()))
                .findAny();
    }

    private List<DomibusConnectorLinkConfiguration> getAllConfigurations() {
        return getAllLinks().stream()
                .map(DomibusConnectorLinkPartner::getLinkConfiguration)
                .distinct()
                .collect(Collectors.toList());
    }

    public void updateLinkConfig(DomibusConnectorLinkConfiguration linkConfig) {
        if (linkConfig.getConfigurationSource() == ConfigurationSource.DB) {
            dcLinkPersistenceService.updateLinkConfig(linkConfig);
        }
    }
}