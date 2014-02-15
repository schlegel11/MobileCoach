package org.isgf.mhc.ui.views.components.interventions;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.server.Intervention;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the intervention editing container component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class InterventionEditingContainerComponentWithController extends
		InterventionEditingContainerComponent {

	public InterventionEditingContainerComponentWithController(
			final AllInterventionsTabComponentWithController allInterventionsTabComponentWithController,
			final Intervention intervention) {
		super(intervention);

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
						intervention),
						Messages.getAdminString(AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__BASIC_SETTINGS_TAB));
	}
}
