package ch.ethz.mc.ui.views.components.interventions;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.MC;
import ch.ethz.mc.model.persistent.Intervention;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

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

		// Handle buttons
		val buttonClickListener = new ButtonClickListener();
		interventionBasicSettingsComponent.getSwitchInterventionButton()
				.addClickListener(buttonClickListener);
		interventionBasicSettingsComponent.getSwitchMessagingButton()
				.addClickListener(buttonClickListener);

		// Set start state
		adjust();
	}

	private void adjust() {
		getInterventionBasicSettingsAndModulesComponent().adjust(
				intervention.isActive(), intervention.isMonitoringActive(),
				intervention.getAssignedModuleIdentifiers());

		if (lastInterventionMonitoringState != intervention
				.isMonitoringActive()) {
			// Messaging status has been changed, so adapt UI
			interventionEditingContainerComponentWithController
					.setEditingDependingOnMessaging(!intervention
							.isMonitoringActive());
		}

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
			}
		}
	}

	public void switchIntervention() {
		log.debug("Switch intervention");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					MC.getInstance()
							.getInterventionExecutionManagerService()
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
					MC.getInstance()
							.getInterventionExecutionManagerService()
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
}
