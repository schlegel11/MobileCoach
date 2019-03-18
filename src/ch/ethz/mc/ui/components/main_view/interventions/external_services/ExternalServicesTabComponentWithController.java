package ch.ethz.mc.ui.components.main_view.interventions.external_services;

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.InterventionExternalService;
import ch.ethz.mc.model.persistent.InterventionVariableWithValue;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValueAccessTypes;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValuePrivacyTypes;
import ch.ethz.mc.model.ui.UIInterventionExternalService;
import ch.ethz.mc.model.ui.UIInterventionVariable;
import ch.ethz.mc.ui.NotificationMessageException;
import ch.ethz.mc.ui.components.basics.ShortStringEditComponent;
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
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the intervention variables tab component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class ExternalServicesTabComponentWithController extends ExternalServicesTabComponent {

	private final Intervention										intervention;

	private UIInterventionExternalService							selectedUIExternalService			= null;
	private BeanItem<UIInterventionExternalService>					selectedUIExternalServiceBeanItem	= null;

	private final BeanContainer<ObjectId, UIInterventionExternalService>	beanContainer;

	public ExternalServicesTabComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		// table options
		val interventionExternalServicesEditComponent = getExternalServicesEditComponent();
		val extrenalServicesTable = interventionExternalServicesEditComponent
				.getExternalServicesTable();

		// table content
		val variablesOfIntervention = getInterventionAdministrationManagerService()
				.getAllInterventionVariablesOfIntervention(
						intervention.getId());

		beanContainer = createBeanContainerForModelObjects(
				UIInterventionExternalService.class, variablesOfIntervention);

		extrenalServicesTable.setContainerDataSource(beanContainer);
		extrenalServicesTable.setSortContainerPropertyId(
				UIInterventionExternalService.getSortColumn());
		extrenalServicesTable
				.setVisibleColumns(UIInterventionExternalService.getVisibleColumns());
		extrenalServicesTable
				.setColumnHeaders(UIInterventionExternalService.getColumnHeaders());

		// handle table selection change
		extrenalServicesTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = extrenalServicesTable.getValue();
				if (objectId == null) {
					interventionExternalServicesEditComponent.setNothingSelected();
					selectedUIExternalService = null;
					selectedUIExternalServiceBeanItem = null;
				} else {
					selectedUIExternalService = getUIModelObjectFromTableByObjectId(
							extrenalServicesTable, UIInterventionExternalService.class,
							objectId);
					selectedUIExternalServiceBeanItem = getBeanItemFromTableByObjectId(
							extrenalServicesTable, UIInterventionExternalService.class,
							objectId);
					interventionExternalServicesEditComponent.setSomethingSelected();
				}
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		interventionExternalServicesEditComponent.getNewButton()
				.addClickListener(buttonClickListener);
		interventionExternalServicesEditComponent.getRenameButton()
				.addClickListener(buttonClickListener);
		interventionExternalServicesEditComponent.getEditButton()
				.addClickListener(buttonClickListener);
		interventionExternalServicesEditComponent.getDeleteButton()
				.addClickListener(buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			val accessControlEditComponent = getExternalServicesEditComponent();

			if (event.getButton() == accessControlEditComponent
					.getNewButton()) {
				createExternalService();
			} else if (event.getButton() == accessControlEditComponent
					.getRenameButton()) {
				renameVariable();
			} else if (event.getButton() == accessControlEditComponent
					.getEditButton()) {
				editVariableValue();
			}else if (event.getButton() == accessControlEditComponent
					.getDeleteButton()) {
				deleteExternalService();
			}
		}
	}

	public void createExternalService() {
		log.debug("Create external service");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_EXTERNAL_SERVICE,
				null, null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						InterventionExternalService newVariable;
						try {
							// Create new variable
							newVariable = getInterventionAdministrationManagerService()
									.interventionExternalServiceCreate(
											getStringValue(),
											intervention.getId());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						beanContainer.addItem(newVariable.getId(),
								UIInterventionExternalService.class
										.cast(newVariable.toUIModelObject()));
						getExternalServicesEditComponent().getExternalServicesTable()
								.select(newVariable.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__EXTERNAL_SERVICE_CREATED);

						closeWindow();
					}
				}, null);
	}

	public void renameVariable() {
		log.debug("Rename variable");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_EXTERNAL_SERVICE,
				selectedUIExternalService.getRelatedModelObject(
						InterventionVariableWithValue.class).getName(),
				null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							val selectedVariable = selectedUIExternalService
									.getRelatedModelObject(
											InterventionVariableWithValue.class);

							// Change name
							getInterventionAdministrationManagerService()
									.interventionVariableWithValueSetName(
											selectedVariable, getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						getStringItemProperty(selectedUIExternalServiceBeanItem,
								UIInterventionVariable.NAME)
										.setValue(selectedUIExternalService
												.getRelatedModelObject(
														InterventionVariableWithValue.class)
												.getName());

						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__EXTERNAL_SERVICE_RENAMED);
						closeWindow();
					}
				}, null);
	}


	public void editVariableValue() {
		log.debug("Edit variable value");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_VALUE_FOR_VARIABLE,
				selectedUIExternalService.getRelatedModelObject(
						InterventionVariableWithValue.class).getValue(),
				null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							val selectedVariable = selectedUIExternalService
									.getRelatedModelObject(
											InterventionVariableWithValue.class);

							// Change name
							getInterventionAdministrationManagerService()
									.interventionVariableWithValueSetValue(
											selectedVariable, getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						getStringItemProperty(selectedUIExternalServiceBeanItem,
								UIInterventionVariable.VALUE)
										.setValue(selectedUIExternalService
												.getRelatedModelObject(
														InterventionVariableWithValue.class)
												.getValue());

						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__VARIABLE_VALUE_CHANGED);
						closeWindow();
					}
				}, null);
	}

	public void deleteExternalService() {
		log.debug("Delete external service");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedVariable = selectedUIExternalService
							.getRelatedModelObject(
									InterventionExternalService.class);

					// Delete external service
					getInterventionAdministrationManagerService()
							.interventionExternalServiceDelete(
									selectedVariable);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				getExternalServicesEditComponent().getExternalServicesTable()
						.removeItem(selectedUIExternalService
								.getRelatedModelObject(
										InterventionExternalService.class)
								.getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__EXTERNAL_SERVICE_DELETED);

				closeWindow();
			}
		}, null);
	}

}
