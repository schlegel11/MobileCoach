package ch.ethz.mc.ui.components.main_view.interventions;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ThemeImageStrings;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.ui.components.main_view.interventions.access.AccessTabComponentWithController;
import ch.ethz.mc.ui.components.main_view.interventions.basic_settings_and_modules.BasicSettingsAndModulesTabComponentWithController;
import ch.ethz.mc.ui.components.main_view.interventions.external_systems.ExternalSystemsTabComponentWithController;
import ch.ethz.mc.ui.components.main_view.interventions.micro_dialogs.MicroDialogsTabComponentWithController;
import ch.ethz.mc.ui.components.main_view.interventions.monitoring_groups_and_messages.MonitoringMessageGroupsTabComponentWithController;
import ch.ethz.mc.ui.components.main_view.interventions.participants.ParticipantsTabComponentWithController;
import ch.ethz.mc.ui.components.main_view.interventions.rules.MonitoringRulesEditComponentWithController;
import ch.ethz.mc.ui.components.main_view.interventions.surveys.InterventionScreeningSurveysTabComponentWithController;
import ch.ethz.mc.ui.components.main_view.interventions.variables.VariablesTabComponentWithController;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the intervention editing container component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class InterventionEditingContainerComponentWithController
		extends InterventionEditingContainerComponent
		implements SelectedTabChangeListener {

	private boolean			editingAllowed								= false;
	private final List<Tab>	availableTabsToSwitchDependingOnMessaging	= new ArrayList<Tab>();

	public InterventionEditingContainerComponentWithController(
			final AllInterventionsTabComponentWithController allInterventionsTabComponentWithController,
			final Intervention intervention) {
		super();

		// Localize
		localize(getInterventionTitleLabel(),
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__INTERVENTIONS_TITLE,
				intervention.getName());

		// Handle buttons
		getListAllInterventionsButton()
				.addClickListener(new Button.ClickListener() {

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
		addPointableTab(getContentTabSheet(),
				new BasicSettingsAndModulesTabComponentWithController(
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
		addPointableTab(getContentTabSheet(),
				new ParticipantsTabComponentWithController(intervention),
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__PARTICIPANTS_TAB,
				ThemeImageStrings.COMPONENT_ICON);

		// Add intervention variables tab
		registerToSetEditingDependingOnMessaging(addPointableTab(
				getContentTabSheet(),
				new VariablesTabComponentWithController(intervention),
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__VARIABLES_TAB,
				ThemeImageStrings.COMPONENT_ICON));

		// Add monitoring rules tab
		registerToSetEditingDependingOnMessaging(addPointableTab(
				getContentTabSheet(),
				new MonitoringRulesEditComponentWithController(intervention),
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__RULES_TAB,
				ThemeImageStrings.COMPONENT_ICON));

		// Add monitoring message groups tab
		registerToSetEditingDependingOnMessaging(addPointableTab(
				getContentTabSheet(),
				new MonitoringMessageGroupsTabComponentWithController(
						intervention),
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__MESSAGE_GROUPS_TAB,
				ThemeImageStrings.COMPONENT_ICON));

		// Add micro dialogs tab
		registerToSetEditingDependingOnMessaging(addPointableTab(
				getContentTabSheet(),
				new MicroDialogsTabComponentWithController(intervention),
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__MICRO_DIALOGS_TAB,
				ThemeImageStrings.COMPONENT_ICON));

		if (getUISession().isAdmin()) {
			// Add intervention access tab
			addPointableTab(getContentTabSheet(),
					new AccessTabComponentWithController(intervention),
					AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__ACCESS_TAB,
					ThemeImageStrings.COMPONENT_ICON);

			// Add external systems tab
			registerToSetEditingDependingOnMessaging(addPointableTab(
					getContentTabSheet(),
					new ExternalSystemsTabComponentWithController(
							intervention),
					AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__EXTERNAL_SYSTEMS_TAB,
					ThemeImageStrings.COMPONENT_ICON));
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

		if (selectedTab instanceof ParticipantsTabComponentWithController) {
			val interventionParticipantsTabWithController = (ParticipantsTabComponentWithController) selectedTab;
			interventionParticipantsTabWithController.adjust();
		} else if (selectedTab instanceof ExternalSystemsTabComponentWithController) {
			val externalSystemsTabComponentWithController = (ExternalSystemsTabComponentWithController) selectedTab;
			externalSystemsTabComponentWithController.adjust();
		}
	}
}
