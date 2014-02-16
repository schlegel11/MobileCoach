package org.isgf.mhc.ui.views.components.interventions;

import java.util.ArrayList;
import java.util.List;

import lombok.val;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.server.Intervention;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet.Tab;

/**
 * Extends the intervention editing container component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class InterventionEditingContainerComponentWithController extends
		InterventionEditingContainerComponent {

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

		// Fill Accordion
		getContentAccordion()
				.addTab(new InterventionBasicSettingsTabComponentWithController(
						intervention, this),
						Messages.getAdminString(AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__BASIC_SETTINGS_TAB))
				.setStyleName("pointable");

		if (getUISession().isAdmin()) {
			// Add intervention access tab
			val interventionAccessTab = getContentAccordion()
					.addTab(new InterventionAccessTabComponentWithController(
							intervention),
							Messages.getAdminString(AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__ACCESS_TAB));
			interventionAccessTab.setStyleName("pointable");
			interventionAccessTab.getComponent().setEnabled(editingAllowed);

		}
		// TODO Just a reminder for other tabs, eg Message Groups, Rules and
		// Variables
		// availableTabsToSwitchDependingOnMessaging
		// .add(interventionAccessTab);
	}

	public void setEditingDependingOnMessaging(final boolean editingAllowed) {
		this.editingAllowed = editingAllowed;

		for (val tab : availableTabsToSwitchDependingOnMessaging) {
			if (tab.getComponent().isEnabled() != editingAllowed) {
				tab.getComponent().setEnabled(editingAllowed);
			}
		}
	}
}
