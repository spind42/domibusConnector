package eu.domibus.connector.ui.view.areas.pmodes;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import eu.domibus.connector.domain.model.DomibusConnectorBusinessDomain;
import eu.domibus.connector.ui.component.DomainSelect;
import eu.domibus.connector.ui.component.LumoLabel;
import eu.domibus.connector.ui.layout.DCVerticalLayoutWithTitleAndHelpButton;
import eu.domibus.connector.ui.service.WebKeystoreService.CertificateInfo;
import eu.domibus.connector.ui.service.WebPModeService;
import eu.domibus.connector.ui.view.areas.configuration.TabMetadata;
import eu.domibus.connector.ui.view.areas.configuration.util.ConfigurationUtil;
import eu.ecodex.dc5.message.model.DC5Action;
import eu.ecodex.dc5.message.model.DC5Party;
import eu.ecodex.dc5.message.model.DC5Service;
import eu.ecodex.dc5.pmode.DC5PmodeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@UIScope
@Route(value = DataTables.ROUTE, layout = PmodeLayout.class)
@Order(2)
@TabMetadata(title = "PMode-Set Data", tabGroup = PmodeLayout.TAB_GROUP_NAME)
public class DataTables extends DCVerticalLayoutWithTitleAndHelpButton implements AfterNavigationObserver {

	public static final String ROUTE = "pmodedata";
	
	public static final String TITLE = "PMode-Set Data";
	public static final String HELP_ID = "ui/pmodes/pmodeset_data.html";

	WebPModeService pmodeService;
	ConfigurationUtil util;
	private final DomainSelect domainSelect;

	DC5PmodeService.DomibusConnectorPModeSet activePModeSet;

	LumoLabel uploadedAt;
	LumoLabel noActivePModeSet;

	Div areaNoActivePModeSetDiv = new Div();
	VerticalLayout activePModeSetLayout = new VerticalLayout();

	Anchor downloadPModesAnchor = new Anchor();

	TextArea description = new TextArea("Description:");
	Button updateDescription = new Button("Update PMode-Set description");
	LumoLabel updateDescriptionResult = new LumoLabel();

	Grid<CertificateInfo> connectorstoreInformationGrid;
	TextField connectorstorePassword = new TextField("Connectorstore password:");
	LumoLabel connectorstoreResultLabel = new LumoLabel();
	Button updateConnectorstorePassword = new Button("Update connectorstore password");

	Grid<DC5Party> partyGrid;
	Grid<DC5Action> actionGrid;
	Grid<DC5Service> serviceGrid;

	Grid<DC5PmodeService.DomibusConnectorPModeSet> pModesGrid;

	public DataTables(@Autowired WebPModeService pmodeService, @Autowired ConfigurationUtil util, DomainSelect domainSelect) {
		super(HELP_ID, TITLE);
		
		this.pmodeService = pmodeService;
		this.util = util;
		this.domainSelect = domainSelect;
		domainSelect.addValueChangeListener(comboBoxBusinessDomainIdComponentValueChangeEvent -> this.refreshUI());

		//CAVE: activePModeSet can be null!!
		activePModeSet = this.pmodeService.getCurrentPModeSet(DomibusConnectorBusinessDomain.getDefaultBusinessDomainId()).orElse(null);

		createActivePmodeSetDiv();
		
		VerticalLayout histPModeSetsDiv = createPModeHistory();

		VerticalLayout main = new VerticalLayout(activePModeSetLayout, histPModeSetsDiv);
		main.setAlignItems(Alignment.STRETCH);
		main.setHeight("100%");
		add(main);
		setHeight("100vh");
		setWidth("100vw");
	}

	private void createActivePmodeSetDiv() {
		activePModeSetLayout.setWidth("100vw");

		noActivePModeSet = createChapterText("No active PModes-Set found! Please import PModes and Connectorstore!");
		noActivePModeSet.getStyle().set("color", "red");

		activePModeSetLayout.add(areaNoActivePModeSetDiv);

		LumoLabel activePModeSetLabel = createChapterText("Active PMode Set data:");

		activePModeSetLayout.add(domainSelect);

		activePModeSetLayout.add(activePModeSetLabel);

		LumoLabel uploadedAtHeader = new LumoLabel("Active PMode Set uploaded at: ");
		uploadedAt = new LumoLabel();
		activePModeSetLayout.add(uploadedAtHeader);
		activePModeSetLayout.add(uploadedAt);
		
		LumoLabel downloadActivePModesButton = new LumoLabel("Download active PModes");

		downloadPModesAnchor.getElement().setAttribute("download", true);
		downloadPModesAnchor.setTarget("_blank");
		downloadPModesAnchor.setTitle("Download active PModes");
		downloadPModesAnchor.add(downloadActivePModesButton);
		activePModeSetLayout.add(downloadPModesAnchor);

		activePModeSetLayout.add(description);

		updateDescription.addClickListener(e -> {
			updateDescriptionResult.setText("");
			if(StringUtils.isEmpty(description.getValue())) {
				updateDescriptionResult.setText("Description must not be empty!");
				updateDescriptionResult.getStyle().set("color", "red");
				return;
			}
			activePModeSet.setDescription(description.getValue());
			pmodeService.updateActivePModeSetDescription(activePModeSet);
			updateDescriptionResult.setText("Description updated.");
			updateDescriptionResult.getStyle().set("color", "green");

		});
		activePModeSetLayout.add(updateDescription);
		activePModeSetLayout.add(updateDescriptionResult);


		
		activePModeSetLayout.add(createServicesDiv());
		activePModeSetLayout.add(createActionsDiv());
		activePModeSetLayout.add(createPartiesDiv());
		activePModeSetLayout.add(createConnectorstoreDiv());

	}
	
	private VerticalLayout createPModeHistory() {
		VerticalLayout histPModeSetDiv = new VerticalLayout();
		histPModeSetDiv.setWidth("100vw");

		LumoLabel histPModeSetLabel = createChapterText("Previous PMode Sets:");
		
		histPModeSetDiv.add(histPModeSetLabel);
		
		pModesGrid = new Grid<DC5PmodeService.DomibusConnectorPModeSet>();


		pModesGrid.addColumn(DC5PmodeService.DomibusConnectorPModeSet::getCreateDate).setHeader("Created date").setWidth("500px").setSortable(true).setResizable(true);
		pModesGrid.addColumn(DC5PmodeService.DomibusConnectorPModeSet::getDescription).setHeader("Description").setWidth("500px").setSortable(true).setResizable(true);
		pModesGrid.addComponentColumn(domibusConnectorPModeSet -> createDownloadPModesAnchor(domibusConnectorPModeSet)).setHeader("PModes").setWidth("200px").setSortable(false).setResizable(true);
		pModesGrid.setWidth("1220px");
		pModesGrid.setHeight("320px");
		pModesGrid.setMultiSort(true);

		histPModeSetDiv.add(pModesGrid);
		
		return histPModeSetDiv;
	}
	
	private Anchor createDownloadPModesAnchor(DC5PmodeService.DomibusConnectorPModeSet pModeSet) {
		LumoLabel downloadPModesButton = new LumoLabel("download");
		Anchor downloadPModesAnchor = new Anchor();

//		if(pModeSet.getpModes()!=null && pModeSet.getCreateDate()!=null) {
//			final StreamResource resource = new StreamResource("pModes-"+pModeSet.getCreateDate().toString()+".xml",
//				() -> new ByteArrayInputStream(pModeSet.getpModes()));
//
//			downloadPModesAnchor.setHref(resource);
//		}else {
//			downloadPModesAnchor.setEnabled(false);
//		}
		downloadPModesAnchor.getElement().setAttribute("download", true);
		downloadPModesAnchor.setTarget("_blank");
		downloadPModesAnchor.setTitle("Download PModes");
		downloadPModesAnchor.add(downloadPModesButton);
		
		return downloadPModesAnchor;
	}
	
	private VerticalLayout createConnectorstoreDiv() {
		VerticalLayout connectorstore = new VerticalLayout();
		
		LumoLabel connectorstoreLabel = createGridTitleText("Connectorstore contents:");
		connectorstore.add(connectorstoreLabel);
		
		connectorstoreInformationGrid = util.createKeystoreInformationGrid();
		connectorstoreInformationGrid.setVisible(false);
			
		connectorstore.add(connectorstoreInformationGrid);
		
		connectorstore.add(connectorstorePassword);

		connectorstore.add(connectorstoreResultLabel);
		
		updateConnectorstorePassword.addClickListener(e -> {
			activePModeSet.getConnectorstore().setPasswordPlain(connectorstorePassword.getValue());
			try {
				pmodeService.updateConnectorstorePassword(activePModeSet, connectorstorePassword.getValue());

				reloadPage();
			}catch(Exception e1) {
				String text = e1.getMessage();
				if(e1.getCause()!=null) {
					text += e1.getCause().getMessage();
				}
				connectorstoreResultLabel.setText("Exception updating password! "+text);
				connectorstoreResultLabel.getStyle().set("color", "red");
			}

		});
		connectorstore.add(updateConnectorstorePassword);
		
		return connectorstore;
	}

	private Div createServicesDiv() {
		Div services = new Div();

		LumoLabel servicesLabel = createGridTitleText("Services within active PMode-Set:");
		services.add(servicesLabel);

		serviceGrid = new Grid<DC5Service>();

		serviceGrid.addColumn(DC5Service::getService).setHeader("Service").setWidth("500px").setSortable(true).setResizable(true);
		serviceGrid.addColumn(DC5Service::getServiceType).setHeader("Service Type").setWidth("500px").setSortable(true).setResizable(true);
		serviceGrid.setWidth("1020px");
		serviceGrid.setHeight("320px");
		serviceGrid.setMultiSort(true);
		serviceGrid.setVisible(false);

		services.add(serviceGrid);
		return services;
	}

	private Div createActionsDiv() {
		Div actions = new Div();

		LumoLabel actionsLabel = createGridTitleText("Actions within active PMode-Set:");
		actions.add(actionsLabel);

		actionGrid = new Grid<DC5Action>();


		actionGrid.addColumn(DC5Action::getAction).setHeader("Action").setWidth("600px").setSortable(true).setResizable(true);
		actionGrid.setWidth("620px");
		actionGrid.setHeight("320px");
		actionGrid.setMultiSort(true);
		actionGrid.setVisible(false);

		actions.add(actionGrid);
		return actions;
	}

	private Div createPartiesDiv() {
		Div parties = new Div();
		
		LumoLabel partiesLabel = createGridTitleText("Parties within active PMode-Set:");
		parties.add(partiesLabel);

		partyGrid = new Grid<DC5Party>();


		partyGrid.addColumn(DC5Party::getPartyId).setHeader("Party ID").setWidth("250px").setSortable(true).setResizable(true);
		partyGrid.addColumn(DC5Party::getPartyIdType).setHeader("Party ID Type").setWidth("500px").setSortable(true).setResizable(true);
//		partyGrid.addColumn(DC5Party::getRole).setHeader("Role").setWidth("500px").setSortable(true).setResizable(true);
//		partyGrid.addColumn(DC5Party::getRoleType).setHeader("Role Type").setWidth("500px").setSortable(true).setResizable(true);
		partyGrid.setWidth("1760px");
		partyGrid.setHeight("320px");
		partyGrid.setMultiSort(true);
		partyGrid.setVisible(false);


		parties.add(partyGrid);
		return parties;
	}



//	public void reloadParties() {
//		partyGrid.setItems(this.pmodeService.getPartyList());
//
//	}
//
//	public void reloadActions() {
//		actionGrid.setItems(this.pmodeService.getActionList());
//
//	}
//
//	public void reloadServices() {
//		serviceGrid.setItems(this.pmodeService.getServiceList());
//
//	}
	
	private void reloadPage() {
		UI.getCurrent().navigate(DataTables.class);
	}


	private LumoLabel createChapterText(String text) {
		LumoLabel label = new LumoLabel();
		label.setText(text);
		label.getStyle().set("font-size", "20px");
		
		label.getStyle().set("font-style", "bold");
		return label;
	}
	
	private LumoLabel createGridTitleText(String text) {
		LumoLabel label = new LumoLabel();
		label.setText(text);
		label.getStyle().set("font-size", "20px");
		
		label.getStyle().set("font-style", "italic");
		return label;
	}

	private void refreshUI() {
		activePModeSet = this.pmodeService.getCurrentPModeSet(domainSelect.getValue()).orElse(null);
		areaNoActivePModeSetDiv.removeAll();
		final ArrayList<DC5PmodeService.DomibusConnectorPModeSet> objects = new ArrayList<>();
		objects.add(pmodeService.getCurrentPModeSet(domainSelect.getValue()).orElse(null));
		pModesGrid.setItems(objects);
	}

	@Override
	public void afterNavigation(AfterNavigationEvent arg0) {
		refreshUI();
	}

}
