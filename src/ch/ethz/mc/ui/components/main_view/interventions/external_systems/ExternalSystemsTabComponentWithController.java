package ch.ethz.mc.ui.components.main_view.interventions.external_systems;

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
import org.bson.types.ObjectId;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.DeepstreamConstants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.InterventionExternalSystem;
import ch.ethz.mc.model.persistent.InterventionExternalSystemFieldVariableMapping;
import ch.ethz.mc.model.ui.UIInterventionExternalSystem;
import ch.ethz.mc.model.ui.UIInterventionExternalSystemFieldVariableMapping;
import ch.ethz.mc.ui.components.basics.ShortStringEditComponent;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the intervention variables tab component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class ExternalSystemsTabComponentWithController extends ExternalSystemsTabComponent {

	private final Intervention										intervention;

	private UIInterventionExternalSystem							selectedUIExternalSystem			= null;
	private BeanItem<UIInterventionExternalSystem>					selectedUIExternalSystemBeanItem	= null;

	private final BeanContainer<ObjectId, UIInterventionExternalSystem>	beanContainer;

	public ExternalSystemsTabComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		// table options
		val interventionExternalSystemsEditComponent = getExternalSystemsEditComponent();
		val extrenalSystemsTable = interventionExternalSystemsEditComponent
				.getExternalSystemsTable();

		// table content
		val externalSystemsOfIntervention = getInterventionAdministrationManagerService()
				.getAllExternalSystemsOfIntervention(
						intervention.getId());

		beanContainer = createBeanContainerForModelObjects(
				UIInterventionExternalSystem.class, externalSystemsOfIntervention);

		extrenalSystemsTable.setContainerDataSource(beanContainer);
		extrenalSystemsTable.setSortContainerPropertyId(
				UIInterventionExternalSystem.getSortColumn());
		extrenalSystemsTable
				.setVisibleColumns(UIInterventionExternalSystem.getVisibleColumns());
		extrenalSystemsTable
				.setColumnHeaders(UIInterventionExternalSystem.getColumnHeaders());

		// handle table selection change
		extrenalSystemsTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = extrenalSystemsTable.getValue();
				if (objectId == null) {
					interventionExternalSystemsEditComponent.setNothingSelected();
					selectedUIExternalSystem = null;
					selectedUIExternalSystemBeanItem = null;
				} else {
					selectedUIExternalSystem = getUIModelObjectFromTableByObjectId(
							extrenalSystemsTable, UIInterventionExternalSystem.class,
							objectId);
					selectedUIExternalSystemBeanItem = getBeanItemFromTableByObjectId(
							extrenalSystemsTable, UIInterventionExternalSystem.class,
							objectId);
					interventionExternalSystemsEditComponent.setSomethingSelected();
					
					val selectedExternalSystem = selectedUIExternalSystem
							.getRelatedModelObject(InterventionExternalSystem.class);
					fillLoginJsonTextArea(selectedExternalSystem);
					fillLoginHttpAuthTextArea(selectedExternalSystem);
					fillExternalMessageJsonTextArea(selectedExternalSystem);
				}
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		interventionExternalSystemsEditComponent.getNewButton()
				.addClickListener(buttonClickListener);
		interventionExternalSystemsEditComponent.getRenameButton()
				.addClickListener(buttonClickListener);
		interventionExternalSystemsEditComponent.getDeleteButton()
				.addClickListener(buttonClickListener);
		interventionExternalSystemsEditComponent.getRenewTokenButton()
				.addClickListener(buttonClickListener);
		interventionExternalSystemsEditComponent.getFieldVariableMappingButton()
				.addClickListener(buttonClickListener);
		interventionExternalSystemsEditComponent.getActiveInactiveButton()
				.addClickListener(buttonClickListener);
	}
	
	private void fillLoginJsonTextArea(InterventionExternalSystem externalSystem) {
		val jsonObject = new JsonObject();
		jsonObject.addProperty(DeepstreamConstants.REST_FIELD_CLIENT_VERSION,
				Constants.getDeepstreamMaxClientVersion());
		jsonObject.addProperty(DeepstreamConstants.REST_FIELD_SYSTEM_ID, externalSystem.getSystemId());
		jsonObject.addProperty(DeepstreamConstants.REST_FIELD_ROLE,
				ImplementationConstants.DEEPSTREAM_EXTERNAL_SYSTEM_ROLE);
		jsonObject.addProperty(DeepstreamConstants.REST_FIELD_TOKEN, externalSystem.getToken());
		
		getExternalSystemsEditComponent().setLoginJsonTextAreaContent(jsonObject);
	}
	
	private void fillLoginHttpAuthTextArea(
			InterventionExternalSystem externalSystem) {
		getExternalSystemsEditComponent().setLoginHttpAuthTextAreaContent(
				Constants.getDeepstreamMaxClientVersion(),
				ImplementationConstants.DEEPSTREAM_EXTERNAL_SYSTEM_ROLE,
				externalSystem.getSystemId(), externalSystem.getToken());
	}
	
	private void fillExternalMessageJsonTextArea(InterventionExternalSystem externalSystem) {
		val jsonObject = new JsonObject();
		jsonObject.addProperty(DeepstreamConstants.REST_FIELD_SYSTEM_ID, externalSystem.getSystemId());
		jsonObject.add(DeepstreamConstants.REST_FIELD_PARTICIPANTS, new JsonArray());

		val mappings = getInterventionAdministrationManagerService()
				.getAllExternalSystemFieldVariableMappingsOfExternalSystem(externalSystem.getId());
		val jsonVariableMappings = new JsonObject();
		for (InterventionExternalSystemFieldVariableMapping mapping : mappings) {
			val uiMappingModel = UIInterventionExternalSystemFieldVariableMapping.class
					.cast(mapping.toUIModelObject());

			jsonVariableMappings.addProperty(uiMappingModel.getFieldName(),
					uiMappingModel.getVariableWithValueName());
		}
		jsonObject.add(DeepstreamConstants.REST_FIELD_VARIABLES, jsonVariableMappings);

		getExternalSystemsEditComponent().setExternalMessageJsonTextAreaContent(jsonObject);
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			val accessControlEditComponent = getExternalSystemsEditComponent();

			if (event.getButton() == accessControlEditComponent
					.getNewButton()) {
				createExternalSystem();
			} else if (event.getButton() == accessControlEditComponent
					.getRenameButton()) {
				renameExternalSystem();
			} else if (event.getButton() == accessControlEditComponent
					.getDeleteButton()) {
				deleteExternalSystem();
			} else if (event.getButton() == accessControlEditComponent
					.getRenewTokenButton()) {
				renewExternalSystemToken();
			} else if (event.getButton() == accessControlEditComponent
					.getActiveInactiveButton()) {
				activeInactiveExternalSystem();
			} else if (event.getButton() == accessControlEditComponent
					.getFieldVariableMappingButton()) {
				openFieldVariableMappings();
			}
		}
	}
	
	public void adjust() {
		if(selectedUIExternalSystem == null) {
			return;
		}
		
		log.debug("Refresh JSON text areas for external system");
		
		val selectedExternalSystem = selectedUIExternalSystem
				.getRelatedModelObject(InterventionExternalSystem.class);
		
		fillLoginJsonTextArea(selectedExternalSystem);
		fillLoginHttpAuthTextArea(selectedExternalSystem);
		fillExternalMessageJsonTextArea(selectedExternalSystem);
	}

	public void createExternalSystem() {
		log.debug("Create external system");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_EXTERNAL_SYSTEM,
				null, null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						InterventionExternalSystem newExternalSystem;
						try {
							// Create new external system
							newExternalSystem = getInterventionAdministrationManagerService()
									.interventionExternalSystemCreate(
											getStringValue(),
											intervention.getId());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						beanContainer.addItem(newExternalSystem.getId(),
								UIInterventionExternalSystem.class
										.cast(newExternalSystem.toUIModelObject()));
						getExternalSystemsEditComponent().getExternalSystemsTable()
								.select(newExternalSystem.getId());
						val extrenalSystemsTable = getExternalSystemsEditComponent().getExternalSystemsTable();
						extrenalSystemsTable.sort();
						extrenalSystemsTable.select(null);
						extrenalSystemsTable.select(newExternalSystem.getId());
						
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__EXTERNAL_SYSTEM_CREATED);
						closeWindow();
					}
				}, null);
	}

	public void renameExternalSystem() {
		log.debug("Rename external system");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_EXTERNAL_SYSTEM,
				selectedUIExternalSystem.getRelatedModelObject(
						InterventionExternalSystem.class).getName(),
				null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						val selectedExternalSystem = selectedUIExternalSystem
								.getRelatedModelObject(
										InterventionExternalSystem.class);
						try {

							// Change name
							getInterventionAdministrationManagerService()
									.interventionExternalSystemSetName(
											selectedExternalSystem, getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						getStringItemProperty(selectedUIExternalSystemBeanItem,
								UIInterventionExternalSystem.NAME)
										.setValue(selectedUIExternalSystem
												.getRelatedModelObject(
														InterventionExternalSystem.class)
												.getName());
						val extrenalSystemsTable = getExternalSystemsEditComponent().getExternalSystemsTable();
						extrenalSystemsTable.sort();
						extrenalSystemsTable.select(null);
						extrenalSystemsTable.select(selectedExternalSystem.getId());

						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__EXTERNAL_SYSTEM_RENAMED);
						closeWindow();
					}
				}, null);
	}


	public void deleteExternalSystem() {
		log.debug("Delete external system");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedExternalSystem = selectedUIExternalSystem
							.getRelatedModelObject(
									InterventionExternalSystem.class);

					// Delete external system
					getInterventionAdministrationManagerService()
							.interventionExternalSystemDelete(
									selectedExternalSystem);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				getExternalSystemsEditComponent().getExternalSystemsTable()
						.removeItem(selectedUIExternalSystem
								.getRelatedModelObject(
										InterventionExternalSystem.class)
								.getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__EXTERNAL_SYSTEM_DELETED);

				closeWindow();
			}
		}, null);
	}
	
	public void renewExternalSystemToken() {
		log.debug("Renew token");
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedExternalSystem = selectedUIExternalSystem
							.getRelatedModelObject(InterventionExternalSystem.class);

					// Renew token
					getInterventionAdministrationManagerService()
							.interventionExternalSystemRenewToken(selectedExternalSystem);
					
					fillLoginJsonTextArea(selectedExternalSystem);
					fillLoginHttpAuthTextArea(selectedExternalSystem);
				} catch (final Exception e) {
					handleException(e);
					return;
				}

				// Adapt UI
				getStringItemProperty(selectedUIExternalSystemBeanItem, UIInterventionExternalSystem.TOKEN).setValue(
						selectedUIExternalSystem.getRelatedModelObject(InterventionExternalSystem.class).getToken());
				
				getAdminUI()
						.showInformationNotification(AdminMessageStrings.NOTIFICATION__EXTERNAL_SYSTEM_TOKEN_RENEWED);
				closeWindow();
			}
		}, null);
	}
	
	public void activeInactiveExternalSystem() {
		log.debug("Active/Inactive system");
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				val selectedExternalSystem = selectedUIExternalSystem
						.getRelatedModelObject(InterventionExternalSystem.class);
				try {
					// Change status
					getInterventionAdministrationManagerService().interventionExternalSystemSetStatus(
							selectedExternalSystem, !selectedExternalSystem.isActive());
				} catch (final Exception e) {
					handleException(e);
					return;
				}

				// Adapt UI
				removeAndAddModelObjectToBeanContainer(beanContainer, selectedExternalSystem);
				val extrenalSystemsTable = getExternalSystemsEditComponent().getExternalSystemsTable();
				extrenalSystemsTable.sort();
				extrenalSystemsTable.select(null);
				extrenalSystemsTable.select(selectedExternalSystem.getId());
				
				getAdminUI()
						.showInformationNotification(AdminMessageStrings.NOTIFICATION__EXTERNAL_SYSTEM_STATUS_CHANGED);
				closeWindow();
			}
		}, null);
	}
	
	public void openFieldVariableMappings() {
		val selectedExternalSystem = selectedUIExternalSystem
				.getRelatedModelObject(InterventionExternalSystem.class);

		log.debug("Open field variable mappings of external system {}", selectedExternalSystem.getSystemId());

		showModalClosableEditWindow(AdminMessageStrings.MAPPINGS__TITLE,
				new ExternalSystemsFieldVariableMappingComponentWithController(selectedExternalSystem),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						fillExternalMessageJsonTextArea(selectedExternalSystem);
						closeWindow();
					}
				}, selectedExternalSystem.getName());
	}

}
