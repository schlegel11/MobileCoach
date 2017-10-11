package ch.ethz.mc.ui.views.components.interventions;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.ArrayUtils;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.ui.views.components.basics.ShortStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ListSelect;

/**
 * Extends the all interventions tab component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class InterventionBasicSettingsAndModulesTabComponentWithController
		extends InterventionBasicSettingsAndModulesTabComponent {

	private final Intervention											intervention;

	private final InterventionEditingContainerComponentWithController	interventionEditingContainerComponentWithController;

	private boolean														lastInterventionMonitoringState	= false;

	@Data
	@AllArgsConstructor
	private class InterventionWrapper {
		String	name;
		String	objectId;

		@Override
		public String toString() {
			if (name.equals("")) {
				return ImplementationConstants.DEFAULT_OBJECT_NAME;
			} else {
				return name;
			}
		}
	}

	public InterventionBasicSettingsAndModulesTabComponentWithController(
			final Intervention intervention,
			final InterventionEditingContainerComponentWithController interventionEditingContainerComponentWithController) {
		super();

		this.intervention = intervention;
		this.interventionEditingContainerComponentWithController = interventionEditingContainerComponentWithController;

		lastInterventionMonitoringState = intervention.isMonitoringActive();

		// Set the first time before other tabs are constructed
		interventionEditingContainerComponentWithController
				.setEditingDependingOnMessaging(!intervention
						.isMonitoringActive());

		val interventionBasicSettingsComponent = getInterventionBasicSettingsAndModulesComponent();

		// Handle combo box
		val senderIdentifications = Constants.getSmsPhoneNumberFrom();

		val senderIdentificationComboBox = interventionBasicSettingsComponent
				.getSenderIdentificationSelectionComboBox();
		for (val senderIdentification : senderIdentifications) {
			senderIdentificationComboBox.addItem(senderIdentification);
			if (intervention.getAssignedSenderIdentification() != null
					&& intervention.getAssignedSenderIdentification().equals(
							senderIdentification)) {
				senderIdentificationComboBox.select(senderIdentification);
			}
		}
		senderIdentificationComboBox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						final String senderIdentification = (String) event
								.getProperty().getValue();

						log.debug("Adjust sender identification to {}",
								senderIdentification);
						getInterventionAdministrationManagerService()
								.interventionChangeSenderIdentification(
										intervention, senderIdentification);

						adjust();

					}
				});

		val templatePaths = getSurveyAdministrationManagerService()
				.getAllTemplatePaths();

		val templatePathComboBox = interventionBasicSettingsComponent
				.getDashboardTemplatePathComboBox();
		for (val templatePath : templatePaths) {
			templatePathComboBox.addItem(templatePath);
			if (intervention.getDashboardTemplatePath().equals(templatePath)) {
				templatePathComboBox.select(templatePath);
			}
		}
		templatePathComboBox.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final String templatePath = (String) event.getProperty()
						.getValue();

				log.debug("Adjust dashboard template path to {}", templatePath);
				getInterventionAdministrationManagerService()
						.interventionChangeDashboardTemplatePath(intervention,
								templatePath);

				adjust();

			}
		});

		// Handle buttons
		val buttonClickListener = new ButtonClickListener();
		interventionBasicSettingsComponent.getSwitchInterventionButton()
				.addClickListener(buttonClickListener);
		interventionBasicSettingsComponent.getSwitchMessagingButton()
				.addClickListener(buttonClickListener);
		interventionBasicSettingsComponent
				.getDashboardPasswordTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);
		interventionBasicSettingsComponent
				.getDeepstreamPasswordTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);

		// Handle checkboxes
		val dashboardEnabledCheckbox = getInterventionBasicSettingsAndModulesComponent()
				.getDashboardEnabledCheckbox();
		dashboardEnabledCheckbox.setValue(intervention.isDashboardEnabled());

		dashboardEnabledCheckbox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						getInterventionAdministrationManagerService()
								.interventionSetDashboardEnabled(
										intervention,
										(boolean) event.getProperty()
												.getValue());
					}
				});

		val finishScreeningSurveyCheckbox = getInterventionBasicSettingsAndModulesComponent()
				.getFinishScreeningSurveysCheckbox();
		finishScreeningSurveyCheckbox.setValue(intervention
				.isAutomaticallyFinishScreeningSurveys());

		finishScreeningSurveyCheckbox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						getInterventionAdministrationManagerService()
								.interventionSetAutomaticallyFinishScreeningSurveys(
										intervention,
										(boolean) event.getProperty()
												.getValue());
					}
				});

		for (val startingDay : intervention.getMonitoringStartingDays()) {
			switch (startingDay) {
				case 1:
					getInterventionBasicSettingsAndModulesComponent()
							.getMondayCheckbox().setValue(true);
					break;
				case 2:
					getInterventionBasicSettingsAndModulesComponent()
							.getTuesdayCheckbox().setValue(true);
					break;
				case 3:
					getInterventionBasicSettingsAndModulesComponent()
							.getWednesdayCheckbox().setValue(true);
					break;
				case 4:
					getInterventionBasicSettingsAndModulesComponent()
							.getThursdayCheckbox().setValue(true);
					break;
				case 5:
					getInterventionBasicSettingsAndModulesComponent()
							.getFridayCheckbox().setValue(true);
					break;
				case 6:
					getInterventionBasicSettingsAndModulesComponent()
							.getSaturdayCheckbox().setValue(true);
					break;
				case 7:
					getInterventionBasicSettingsAndModulesComponent()
							.getSundayCheckbox().setValue(true);
					break;
			}
		}

		final ValueChangeListener startingDayValueChangeListener = new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				if (event.getProperty() == getInterventionBasicSettingsAndModulesComponent()
						.getMondayCheckbox()) {
					getInterventionAdministrationManagerService()
							.interventionSetStartingDay(intervention, 1,
									(boolean) event.getProperty().getValue());
				} else if (event.getProperty() == getInterventionBasicSettingsAndModulesComponent()
						.getTuesdayCheckbox()) {
					getInterventionAdministrationManagerService()
							.interventionSetStartingDay(intervention, 2,
									(boolean) event.getProperty().getValue());
				} else if (event.getProperty() == getInterventionBasicSettingsAndModulesComponent()
						.getWednesdayCheckbox()) {
					getInterventionAdministrationManagerService()
							.interventionSetStartingDay(intervention, 3,
									(boolean) event.getProperty().getValue());
				} else if (event.getProperty() == getInterventionBasicSettingsAndModulesComponent()
						.getThursdayCheckbox()) {
					getInterventionAdministrationManagerService()
							.interventionSetStartingDay(intervention, 4,
									(boolean) event.getProperty().getValue());
				} else if (event.getProperty() == getInterventionBasicSettingsAndModulesComponent()
						.getFridayCheckbox()) {
					getInterventionAdministrationManagerService()
							.interventionSetStartingDay(intervention, 5,
									(boolean) event.getProperty().getValue());
				} else if (event.getProperty() == getInterventionBasicSettingsAndModulesComponent()
						.getSaturdayCheckbox()) {
					getInterventionAdministrationManagerService()
							.interventionSetStartingDay(intervention, 6,
									(boolean) event.getProperty().getValue());
				} else if (event.getProperty() == getInterventionBasicSettingsAndModulesComponent()
						.getSundayCheckbox()) {
					getInterventionAdministrationManagerService()
							.interventionSetStartingDay(intervention, 7,
									(boolean) event.getProperty().getValue());
				}
			}
		};

		getInterventionBasicSettingsAndModulesComponent().getMondayCheckbox()
				.addValueChangeListener(startingDayValueChangeListener);
		getInterventionBasicSettingsAndModulesComponent().getTuesdayCheckbox()
				.addValueChangeListener(startingDayValueChangeListener);
		getInterventionBasicSettingsAndModulesComponent()
				.getWednesdayCheckbox().addValueChangeListener(
						startingDayValueChangeListener);
		getInterventionBasicSettingsAndModulesComponent().getThursdayCheckbox()
				.addValueChangeListener(startingDayValueChangeListener);
		getInterventionBasicSettingsAndModulesComponent().getFridayCheckbox()
				.addValueChangeListener(startingDayValueChangeListener);
		getInterventionBasicSettingsAndModulesComponent().getSaturdayCheckbox()
				.addValueChangeListener(startingDayValueChangeListener);
		getInterventionBasicSettingsAndModulesComponent().getSundayCheckbox()
				.addValueChangeListener(startingDayValueChangeListener);

		// Handle list
		val uniquenessList = getInterventionBasicSettingsAndModulesComponent()
				.getUniquenessList();
		val otherInterventions = getInterventionAdministrationManagerService()
				.getAllInterventions();

		for (val otherIntervention : otherInterventions) {
			if (!intervention.getId().equals(otherIntervention.getId())) {
				val interventionWrapper = new InterventionWrapper(
						otherIntervention.getName(), otherIntervention.getId()
								.toHexString());
				uniquenessList.addItem(interventionWrapper);

				if (ArrayUtils.contains(
						intervention.getInterventionsToCheckForUniqueness(),
						otherIntervention.getId().toHexString())) {
					uniquenessList.select(interventionWrapper);
				}
			}
		}

		uniquenessList.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val newSelection = (Set<?>) ((ListSelect) event.getProperty())
						.getValue();

				val interventionsToCheckForParticipantUniqueness = new String[newSelection
						.size()];
				val iterator = newSelection.iterator();
				int i = 0;
				while (iterator.hasNext()) {
					interventionsToCheckForParticipantUniqueness[i] = ((InterventionWrapper) iterator
							.next()).getObjectId();
					i++;
				}

				getInterventionAdministrationManagerService()
						.interventionSetInterventionsToCheckForUniqueness(
								intervention,
								interventionsToCheckForParticipantUniqueness);
			}
		});

		// Set start state
		adjust();
	}

	private void adjust() {

		getInterventionBasicSettingsAndModulesComponent().adjust(
				intervention.isActive(), intervention.isMonitoringActive());

		val interventionBasicSettingsComponent = getInterventionBasicSettingsAndModulesComponent();

		if (lastInterventionMonitoringState != intervention
				.isMonitoringActive()) {
			// Messaging status has been changed, so adapt UI
			interventionEditingContainerComponentWithController
					.setEditingDependingOnMessaging(!intervention
							.isMonitoringActive());
		}

		// Adjust password text fields
		interventionBasicSettingsComponent
				.getDashboardPasswordTextFieldComponent().setValue(
						intervention.getDashboardPasswordPattern());
		interventionBasicSettingsComponent
				.getDeepstreamPasswordTextFieldComponent().setValue(
						intervention.getDeepstreamPassword());

		lastInterventionMonitoringState = intervention.isMonitoringActive();
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			val interventionBasicSettingsComponent = getInterventionBasicSettingsAndModulesComponent();

			if (event.getButton() == interventionBasicSettingsComponent
					.getSwitchInterventionButton()) {
				switchIntervention();
			} else if (event.getButton() == interventionBasicSettingsComponent
					.getSwitchMessagingButton()) {
				switchMessaging();
			} else if (event.getButton() == interventionBasicSettingsComponent
					.getDashboardPasswordTextFieldComponent().getButton()) {
				editDashboardPassword();
			} else if (event.getButton() == interventionBasicSettingsComponent
					.getDeepstreamPasswordTextFieldComponent().getButton()) {
				editDeepstreamPassword();
			}
			event.getButton().setEnabled(true);
		}
	}

	public void switchIntervention() {
		log.debug("Switch intervention");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					getInterventionExecutionManagerService()
							.interventionSetStatus(intervention,
									!intervention.isActive());
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				adjust();
				closeWindow();
			}
		}, null);
	}

	public void switchMessaging() {
		log.debug("Switch messaging");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					getInterventionExecutionManagerService()
							.interventionSetMonitoring(intervention,
									!intervention.isMonitoringActive());
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				interventionEditingContainerComponentWithController.setEditingDependingOnMessaging(!intervention
						.isMonitoringActive());

				adjust();
				closeWindow();
			}
		}, null);
	}

	public void editDashboardPassword() {
		log.debug("Edit dashboard password");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_PASSWORD_PATTERN,
				intervention.getDashboardPasswordPattern(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change password
							getInterventionAdministrationManagerService()
									.interventionChangeDashboardPasswordPattern(
											intervention, getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}
				}, null);
	}

	public void editDeepstreamPassword() {
		log.debug("Edit deepstream password");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_PASSWORD,
				intervention.getDeepstreamPassword(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change password
							getInterventionAdministrationManagerService()
									.interventionChangeDeepstreamPassword(
											intervention, getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}
				}, null);
	}
}
