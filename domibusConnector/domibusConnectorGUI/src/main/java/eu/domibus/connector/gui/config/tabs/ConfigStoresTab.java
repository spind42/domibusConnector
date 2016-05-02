package eu.domibus.connector.gui.config.tabs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import eu.domibus.connector.gui.config.properties.ConnectorProperties;
import eu.domibus.connector.gui.layout.SpringUtilities;

public class ConfigStoresTab extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -400237610852262555L;

	public ConfigStoresTab() {
		JPanel helpPanel = ConfigTabHelper.buildHelpPanel("Stores Configuration Help", "StoresConfigurationHelp.htm");
		BorderLayout mgr = new BorderLayout();
		setLayout(mgr);
		add(helpPanel, BorderLayout.EAST);
		
		JPanel disp = new JPanel();
		
		JPanel evidenceStorePanel = buildEvidenceStore();
		
		disp.add(evidenceStorePanel);
		
		JPanel securityStorePanel = buildSecurityStore();
		
		disp.add(securityStorePanel);
		
		JPanel truststorePanel = buildTruststorePanel();
		
		disp.add(truststorePanel);
		
		add(disp);
	}

	private JPanel buildTruststorePanel() {
		JPanel truststorePanel = new JPanel(new SpringLayout());

		final JFileChooser fc3 = new JFileChooser();
		final JTextField log3 = new JTextField(40);

		final JButton truststorePath = ConfigTabHelper.addFileChooserRow(truststorePanel, ConnectorProperties.STORE_TRUSTSTORE_PATH_LABEL, ConnectorProperties.truststorePathValue, ConnectorProperties.STORE_TRUSTSTORE_PATH_HELP, log3);
		truststorePath.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				    int returnVal = fc3.showOpenDialog(ConfigStoresTab.this);

		            if (returnVal == JFileChooser.APPROVE_OPTION) {
		            	ConnectorProperties.truststorePathValue = fc3.getSelectedFile().getAbsolutePath();
		            	log3.setText(fc3.getSelectedFile().getAbsolutePath());
		            }
		   
			}
		});
		
		final JFormattedTextField truststorePWValue = ConfigTabHelper.addTextFieldRow(null, truststorePanel, ConnectorProperties.STORE_TRUSTSTORE_PW_LABEL, ConnectorProperties.truststorePasswordValue, ConnectorProperties.STORE_TRUSTSTORE_PW_HELP, 40);
		truststorePWValue.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				ConnectorProperties.truststorePasswordValue = truststorePWValue.getText();
			}
		});
		truststorePanel.add(new JLabel(""));
		
        SpringUtilities.makeCompactGrid(truststorePanel,
                2, 3, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
        
        truststorePanel.setOpaque(true);
		return truststorePanel;
	}

	private JPanel buildSecurityStore() {
		JPanel securityStorePanel = new JPanel(new SpringLayout());

		final JFileChooser fc2 = new JFileChooser();
		final JTextField log2 = new JTextField(40);

		final JButton secKeystorePath = ConfigTabHelper.addFileChooserRow(securityStorePanel, ConnectorProperties.STORE_SECURITY_KEYSTORE_PATH_LABEL, ConnectorProperties.securityKeystorePathValue, ConnectorProperties.STORE_SECURITY_KEYSTORE_PATH_HELP, log2);
		secKeystorePath.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				    int returnVal = fc2.showOpenDialog(ConfigStoresTab.this);

		            if (returnVal == JFileChooser.APPROVE_OPTION) {
		            	ConnectorProperties.securityKeystorePathValue = fc2.getSelectedFile().getAbsolutePath();
		            	log2.setText(fc2.getSelectedFile().getAbsolutePath());
		            }
		   
			}
		});
		
		final JFormattedTextField secStoreValue = ConfigTabHelper.addTextFieldRow(null, securityStorePanel, ConnectorProperties.STORE_SECURITY_KEYSTORE_PW_LABEL, ConnectorProperties.securityKeystorePasswordValue, ConnectorProperties.STORE_SECURITY_KEYSTORE_PW_HELP, 40);
		secStoreValue.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				ConnectorProperties.securityKeystorePasswordValue = secStoreValue.getText();
			}
		});
		securityStorePanel.add(new JLabel(""));
		
		final JFormattedTextField secKeyAliasValue = ConfigTabHelper.addTextFieldRow(null, securityStorePanel, ConnectorProperties.STORE_SECURITY_KEY_ALIAS_LABEL, ConnectorProperties.securityKeyAliasValue, ConnectorProperties.STORE_SECURITY_KEY_ALIAS_HELP, 40);
		secKeyAliasValue.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				ConnectorProperties.securityKeyAliasValue = secKeyAliasValue.getText();
			}
		});
		securityStorePanel.add(new JLabel(""));
		
		final JFormattedTextField secKeyPWValue = ConfigTabHelper.addTextFieldRow(null, securityStorePanel, ConnectorProperties.STORE_SECURITY_KEY_PW_LABEL, ConnectorProperties.securityKeyPasswordValue, ConnectorProperties.STORE_SECURITY_KEY_PW_HELP, 40);
		secKeyPWValue.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				ConnectorProperties.securityKeyPasswordValue = secKeyPWValue.getText();
			}
		});
		securityStorePanel.add(new JLabel(""));
	
        SpringUtilities.makeCompactGrid(securityStorePanel,
                4, 3, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
        
        securityStorePanel.setOpaque(true);
		return securityStorePanel;
	}

	private JPanel buildEvidenceStore() {
		JPanel evidenceStorePanel = new JPanel(new SpringLayout());

		final JFileChooser fc = new JFileChooser();
		final JTextField log = new JTextField(40);
		final JButton evKeystorePath = ConfigTabHelper.addFileChooserRow(evidenceStorePanel, ConnectorProperties.STORE_EVIDENCES_KEYSTORE_PATH_LABEL, ConnectorProperties.evidencesKeystorePathValue, ConnectorProperties.STORE_EVIDENCES_KEYSTORE_PATH_HELP, log);
		evKeystorePath.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				    int returnVal = fc.showOpenDialog(ConfigStoresTab.this);

		            if (returnVal == JFileChooser.APPROVE_OPTION) {
		            	ConnectorProperties.evidencesKeystorePathValue = fc.getSelectedFile().getAbsolutePath();
		            	log.setText(fc.getSelectedFile().getAbsolutePath());
		            }
		   
			}
		});
		
		final JFormattedTextField evStoreValue = ConfigTabHelper.addTextFieldRow(null, evidenceStorePanel, ConnectorProperties.STORE_EVIDENCES_KEYSTORE_PW_LABEL, ConnectorProperties.evidencesKeystorePasswordValue, ConnectorProperties.STORE_EVIDENCES_KEYSTORE_PW_HELP, 40);
		evStoreValue.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				ConnectorProperties.evidencesKeystorePasswordValue = evStoreValue.getText();
			}
		});
		evidenceStorePanel.add(new JLabel(""));
		
		final JFormattedTextField evKeyAliasValue = ConfigTabHelper.addTextFieldRow(null, evidenceStorePanel, ConnectorProperties.STORE_EVIDENCES_KEY_ALIAS_LABEL, ConnectorProperties.evidencesKeyAliasValue, ConnectorProperties.STORE_EVIDENCES_KEY_ALIAS_HELP, 40);
		evKeyAliasValue.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				ConnectorProperties.evidencesKeyAliasValue = evKeyAliasValue.getText();
			}
		});
		evidenceStorePanel.add(new JLabel(""));
		
		final JFormattedTextField evKeyPWValue = ConfigTabHelper.addTextFieldRow(null, evidenceStorePanel, ConnectorProperties.STORE_EVIDENCES_KEY_PW_LABEL, ConnectorProperties.evidencesKeyPasswordValue, ConnectorProperties.STORE_EVIDENCES_KEY_PW_HELP, 40);
		evKeyPWValue.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				ConnectorProperties.evidencesKeyPasswordValue = evKeyPWValue.getText();
			}
		});
		evidenceStorePanel.add(new JLabel(""));
	
        SpringUtilities.makeCompactGrid(evidenceStorePanel,
                4, 3, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
        
        evidenceStorePanel.setOpaque(true);
		return evidenceStorePanel;
	}

}