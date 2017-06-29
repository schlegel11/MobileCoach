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
import java.util.ArrayList;
import java.util.List;

import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ThemeImageStrings;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.ui.views.components.interventions.monitoring_messages.MonitoringMessageGroupsTabComponentWithController;
import ch.ethz.mc.ui.views.components.interventions.monitoring_rules.MonitoringRulesEditComponentWithController;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;

/**
 * Extends the intervention editing container component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class InterventionEditingContainerComponentWithController extends
		InterventionEditingContainerComponent implements
		SelectedTabChangeListener {

	private boolean			editingAllowed								= false;
	private final List<Tab>	availableTabsToSwitchDependingOnMessaging	= new ArrayList<Tab>();

	public InterventionEditingContainerComponentWithController(
			final AllInterventionsTabComponentWithController allInterventionsTabComponentWithController,
			final Intervention intervention) {
		super();

		// Localize
		localize(
				getInterventionTitleLabel(),
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__INTERVENTIONS_TITLE,
				intervention.getName());

		// Handle buttons
		getListAllInterventionsButton().addClickListener(
				new Button.ClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						allInterventionsTabComponentWithController
								.returnToInterventionList();
					}
				});

		// Handle tab sheet change
		getContentTabSheet().addSelectedTabChangeListener(this);

		// Fill tab sheet
		// Add basic settings tab
		addPointableTab(
				getContentTabSheet(),
				new InterventionBasicSettingsAndModulesTabComponentWithController(
						intervention, this),
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__BASIC_SETTINGS_AND_MODULES_TAB,
				ThemeImageStrings.COMPONENT_ICON);

		// Add intervention screening surveys tab
		registerToSetEditingDependingOnMessaging(addPointableTab(
				getContentTabSheet(),
				new InterventionScreeningSurveysTabComponentWithController(
						intervention),
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__SCREENING_SURVEYS_TAB,
				ThemeImageStrings.COMPONENT_ICON));

		// Add intervention participants tab
		addPointableTab(
				getContentTabSheet(),
				new InterventionParticipantsTabComponentWithController(
						intervention),
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__PARTICIPANTS_TAB,
				ThemeImageStrings.COMPONENT_ICON);

		// Add intervention variables tab
		registerToSetEditingDependingOnMessaging(addPointableTab(
				getContentTabSheet(),
				new InterventionVariablesTabComponentWithController(
						intervention),
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__VARIABLES_TAB,
				ThemeImageStrings.COMPONENT_ICON));

		// Add monitoring rules tab
		registerToSetEditingDependingOnMessaging(addPointableTab(
				getContentTabSheet(),
				new MonitoringRulesEditComponentWithController(intervention),
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__MONITORING_RULES_TAB,
				ThemeImageStrings.COMPONENT_ICON));

		// Add monitoring message groups tab
		registerToSetEditingDependingOnMessaging(addPointableTab(
				getContentTabSheet(),
				new MonitoringMessageGroupsTabComponentWithController(
						intervention),
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__MONITORING_MESSAGE_GROUPS_TAB,
				ThemeImageStrings.COMPONENT_ICON));

		if (getUISession().isAdmin()) {
			// Add intervention access tab
			addPointableTab(
					getContentTabSheet(),
					new InterventionAccessTabComponentWithController(
							intervention),
					AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__ACCESS_TAB,
					ThemeImageStrings.COMPONENT_ICON);
		}

	}

	private void registerToSetEditingDependingOnMessaging(final Tab tab) {
		tab.getComponent().setEnabled(editingAllowed);
		availableTabsToSwitchDependingOnMessaging.add(tab);
	}

	public void setEditingDependingOnMessaging(final boolean editingAllowed) {
		this.editingAllowed = editingAllowed;

		for (val tab : availableTabsToSwitchDependingOnMessaging) {
			if (tab.getComponent().isEnabled() != editingAllowed) {
				tab.getComponent().setEnabled(editingAllowed);
			}
		}
	}

	@Override
	public void selectedTabChange(final SelectedTabChangeEvent event) {
		val selectedTab = event.getTabSheet().getSelectedTab();
		log.debug("Changed tab to {}", selectedTab.getClass().getSimpleName());

		if (selectedTab instanceof InterventionParticipantsTabComponentWithController) {
			val interventionParticipantsTabWithController = (InterventionParticipantsTabComponentWithController) selectedTab;
			interventionParticipantsTabWithController.adjust();
		}
	}
}
