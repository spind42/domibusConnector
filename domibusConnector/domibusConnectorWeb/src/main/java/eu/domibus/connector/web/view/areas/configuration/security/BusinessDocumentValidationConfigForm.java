package eu.domibus.connector.web.view.areas.configuration.security;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.Binder;
import eu.domibus.connector.domain.enums.AdvancedElectronicSystemType;
import eu.domibus.connector.web.ui.fields.SignatureValidationConfigurationField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.gatanaso.MultiselectComboBox;


@Component
@Scope("prototype")
public class BusinessDocumentValidationConfigForm extends FormLayout {

    private final TextField country = new TextField();
    private final TextField serviceProvider = new TextField();

    private final Select<AdvancedElectronicSystemType> defaultAdvancedSystemType = new Select<>(AdvancedElectronicSystemType.values());
    private final Checkbox allowSystemTypeOverrideByClient = new Checkbox();

    private final MultiselectComboBox<AdvancedElectronicSystemType> allowedAdvancedSystemTypes = new MultiselectComboBox<>(null, AdvancedElectronicSystemType.values());

    @SuppressWarnings("FieldCanBeLocal")
    private final SignatureValidationConfigurationField signatureValidation;

    public BusinessDocumentValidationConfigForm(SignatureValidationConfigurationField signatureValidation) {
        this.signatureValidation = signatureValidation;
        this.setResponsiveSteps(new ResponsiveStep("30cm", 1, ResponsiveStep.LabelsPosition.ASIDE));

        addFormItem(country, "Country");
        addFormItem(serviceProvider, "Service Provider");
        addFormItem(allowedAdvancedSystemTypes, "Allowed AdvancedSystemTypes");
        addFormItem(defaultAdvancedSystemType, "Default AdvancedSystemType");
        addFormItem(allowSystemTypeOverrideByClient, "Allow client to set AdvancedSystemType on message");
        addFormItem(signatureValidation, "Signature Validation Config");
    }

    public void bindInstanceFields(Binder b) {
        b.bindInstanceFields(this);
    }

}
