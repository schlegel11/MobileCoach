package ch.ethz.mc.ui.components.main_view.interventions.external_systems;

import com.google.common.base.Joiner;
import com.google.gson.JsonObject;
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.ui.UIInterventionExternalSystem;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.ui.components.AbstractCustomComponent;
/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

/**
 * Provides the external systems edit component.
 *
 * @author Marcel Schlegel
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class ExternalSystemsEditComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;
	@AutoGenerated
	private VerticalLayout		textAreaLayout;
	@AutoGenerated
	private Button				deleteButton;
	@AutoGenerated
	private Button				renameButton;
	@AutoGenerated
	private Button				newButton;
	@AutoGenerated
	private Button				renewTokenButton;
	@AutoGenerated
	private Button				activeInactiveButton;
	@AutoGenerated
	private Button				fieldVariableMappingButton;
	@AutoGenerated
	private Table				externalSystemsTable;
	@AutoGenerated
	private TextArea			systemLoginJsonTextArea;
	@AutoGenerated
	private TextArea			systemLoginHttpAuthTextArea;
	@AutoGenerated
	private TextArea			systemExternalMessageJsonTextArea;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	protected ExternalSystemsEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(newButton, AdminMessageStrings.GENERAL__NEW);
		localize(renameButton, AdminMessageStrings.GENERAL__RENAME);
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);
		localize(renewTokenButton, AdminMessageStrings.INTERVENTION_EXTERNAL_SYSTEMS_EDITING__RENEW_TOKEN_BUTTON);
		localize(activeInactiveButton,
				AdminMessageStrings.INTERVENTION_EXTERNAL_SYSTEMS_EDITING__ACTIVE_INACTIVE_BUTTON);
		localize(fieldVariableMappingButton,
				AdminMessageStrings.INTERVENTION_EXTERNAL_SYSTEMS_EDITING__FIELD_VARIABLE_MAPPING_BUTTON);
		localize(systemLoginJsonTextArea,
				AdminMessageStrings.INTERVENTION_EXTERNAL_SYSTEMS_EDITING__SYSTEM_LOGIN_JSON_TEXT_AREA);
		localize(systemLoginHttpAuthTextArea,
				AdminMessageStrings.INTERVENTION_EXTERNAL_SYSTEMS_EDITING__SYSTEM_LOGIN_HTTP_AUTH_TEXT_AREA);
		localize(systemExternalMessageJsonTextArea,
				AdminMessageStrings.INTERVENTION_EXTERNAL_SYSTEMS_EDITING__SYSTEM_EXTERNAL_MESSAGE_JSON_TEXT_AREA);
		
		// set table formatter
		externalSystemsTable
				.setCellStyleGenerator(new Table.CellStyleGenerator() {
					@Override
					public String getStyle(final Table source,
							final Object itemId, final Object propertyId) {
						if (propertyId != null) {
							if (propertyId.equals(
									UIInterventionExternalSystem.STATUS)) {
								val uiIntervention = getUIModelObjectFromTableByObjectId(
										source, UIInterventionExternalSystem.class, itemId);
								if (uiIntervention
										.isBooleanStatus()) {
									return "active";
								} else {
									return "inactive";
								}
							}
						}

						return null;
					}
				});

		// set button start state
		setNothingSelected();
	}

	protected void setNothingSelected() {
		renameButton.setEnabled(false);
		deleteButton.setEnabled(false);
		renewTokenButton.setEnabled(false);
		activeInactiveButton.setEnabled(false);
		fieldVariableMappingButton.setEnabled(false);
		
		systemLoginJsonTextArea.setEnabled(false);
		systemLoginJsonTextArea.setReadOnly(false);
		systemLoginJsonTextArea.clear();
		systemLoginJsonTextArea.setReadOnly(true);
		
		systemLoginHttpAuthTextArea.setEnabled(false);
		systemLoginHttpAuthTextArea.setReadOnly(false);
		systemLoginHttpAuthTextArea.clear();
		systemLoginHttpAuthTextArea.setReadOnly(true);
		
		systemExternalMessageJsonTextArea.setEnabled(false);
		systemExternalMessageJsonTextArea.setReadOnly(false);
		systemExternalMessageJsonTextArea.clear();
		systemExternalMessageJsonTextArea.setReadOnly(true);
	}

	protected void setSomethingSelected() {
		renameButton.setEnabled(true);
		deleteButton.setEnabled(true);
		renewTokenButton.setEnabled(true);
		activeInactiveButton.setEnabled(true);
		fieldVariableMappingButton.setEnabled(true);
		systemLoginJsonTextArea.setEnabled(true);
		systemLoginHttpAuthTextArea.setEnabled(true);
		systemExternalMessageJsonTextArea.setEnabled(true);
	}
	
	protected void setLoginJsonTextAreaContent(JsonObject jsonObject) {
		systemLoginJsonTextArea.setReadOnly(false);
		systemLoginJsonTextArea.setValue(StringHelpers.createPrettyPrintedJSON(jsonObject));
		systemLoginJsonTextArea.setReadOnly(true);
	}
	
	protected void setLoginHttpAuthTextAreaContent(int clientVersion,
			String role, String serviceId, String token) {
		systemLoginHttpAuthTextArea.setReadOnly(false);
		systemLoginHttpAuthTextArea.setValue("'token' => '"
				+ Joiner.on(';').join(clientVersion, role, serviceId, token)
				+ "'");
		systemLoginHttpAuthTextArea.setReadOnly(true);
	}
	
	protected void setExternalMessageJsonTextAreaContent(JsonObject jsonObject) {
		systemExternalMessageJsonTextArea.setReadOnly(false);
		systemExternalMessageJsonTextArea.setValue(StringHelpers.createPrettyPrintedJSON(jsonObject));
		systemExternalMessageJsonTextArea.setReadOnly(true);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("100.0%");
		setHeight("-1px");

		// variablesTable
		externalSystemsTable = new Table();
		externalSystemsTable.setImmediate(false);
		externalSystemsTable.setWidth("100.0%");
		externalSystemsTable.setHeight("350px");
		mainLayout.addComponent(externalSystemsTable);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);
		
		// textAreaLayout
		textAreaLayout = buildTextAreaLayout();
		mainLayout.addComponent(textAreaLayout);

		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildButtonLayout() {
		// common part: create layout
		buttonLayout = new HorizontalLayout();
		buttonLayout.setImmediate(false);
		buttonLayout.setWidth("-1px");
		buttonLayout.setHeight("-1px");
		buttonLayout.setMargin(false);
		buttonLayout.setSpacing(true);

		// newButton
		newButton = new Button();
		newButton.setCaption("!!! New");
		newButton.setIcon(new ThemeResource("img/add-icon-small.png"));
		newButton.setImmediate(true);
		newButton.setWidth("100px");
		newButton.setHeight("-1px");
		buttonLayout.addComponent(newButton);

		// renameButton
		renameButton = new Button();
		renameButton.setCaption("!!! Rename");
		renameButton.setImmediate(true);
		renameButton.setWidth("100px");
		renameButton.setHeight("-1px");
		buttonLayout.addComponent(renameButton);

		// deleteButton
		deleteButton = new Button();
		deleteButton.setCaption("!!! Delete");
		deleteButton.setIcon(new ThemeResource("img/delete-icon-small.png"));
		deleteButton.setImmediate(true);
		deleteButton.setWidth("100px");
		deleteButton.setHeight("-1px");
		buttonLayout.addComponent(deleteButton);
		
		// renewTokenButton
		renewTokenButton = new Button();
		renewTokenButton.setCaption("!!! Renew Token");
		renewTokenButton.setImmediate(true);
		renewTokenButton.setWidth("100px");
		renewTokenButton.setHeight("-1px");
		buttonLayout.addComponent(renewTokenButton);
		
		// fieldVariableMappingButton
		fieldVariableMappingButton = new Button();
		fieldVariableMappingButton.setCaption("!!! Field Variable Mapping");
		fieldVariableMappingButton.setImmediate(true);
		fieldVariableMappingButton.setWidth("200px");
		fieldVariableMappingButton.setHeight("-1px");
		buttonLayout.addComponent(fieldVariableMappingButton);
		
		// activeInactiveButton
		activeInactiveButton = new Button();
		activeInactiveButton.setCaption("!!! Active Inactive");
		activeInactiveButton.setImmediate(true);
		activeInactiveButton.setWidth("180px");
		activeInactiveButton.setHeight("-1px");
		buttonLayout.addComponent(activeInactiveButton);
		
		return buttonLayout;
	}
	
	 // Build text areas for show the login parameters for a deepstream websocket or http login.
	 // Show external-message RPC parameters.
	@AutoGenerated
	private VerticalLayout buildTextAreaLayout() {
		// common part: create layout
		textAreaLayout = new VerticalLayout();
		textAreaLayout.setImmediate(false);
		textAreaLayout.setWidth("100.0%");
		textAreaLayout.setHeight("-1px");
		textAreaLayout.setMargin(false);
		textAreaLayout.setSpacing(true);
		
		// systemLoginJsonTextArea
		systemLoginJsonTextArea = new TextArea();
		systemLoginJsonTextArea.setCaption("!!! System login JSON Output");
		systemLoginJsonTextArea.setWidth("100.0%");
		systemLoginJsonTextArea.setHeight("-1px");
		systemLoginJsonTextArea.setImmediate(true);
		systemLoginJsonTextArea.setReadOnly(true);
		textAreaLayout.addComponent(systemLoginJsonTextArea);
		
		// systemLoginHttpAuthTextArea
		systemLoginHttpAuthTextArea = new TextArea();
		systemLoginHttpAuthTextArea.setCaption("!!! System login HTTP auth Output");
		systemLoginHttpAuthTextArea.setWidth("100.0%");
		systemLoginHttpAuthTextArea.setHeight("-1px");
		systemLoginHttpAuthTextArea.setImmediate(true);
		systemLoginHttpAuthTextArea.setReadOnly(true);
		textAreaLayout.addComponent(systemLoginHttpAuthTextArea);
		
		// systemExternalMessageJsonTextArea
		systemExternalMessageJsonTextArea = new TextArea();
		systemExternalMessageJsonTextArea.setCaption("!!! System message Output");
		systemExternalMessageJsonTextArea.setWidth("100.0%");
		systemExternalMessageJsonTextArea.setHeight("-1px");
		systemExternalMessageJsonTextArea.setImmediate(true);
		systemExternalMessageJsonTextArea.setReadOnly(true);
		textAreaLayout.addComponent(systemExternalMessageJsonTextArea);
		
		return textAreaLayout;
		
	}

}
