package org.isgf.mhc.ui.views.components.interventions;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.model.server.Intervention;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the all interventions tab component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class InterventionBasicSettingsTabComponentWithController extends
		InterventionBasicSettingsTabComponent {

	private final Intervention											intervention;

	private final InterventionEditingContainerComponentWithController	interventionEditingContainerComponentWithController;

	private boolean														lastInterventionMessagingActiveState	= false;

	public InterventionBasicSettingsTabComponentWithController(
			final Intervention intervention,
			final InterventionEditingContainerComponentWithController interventionEditingContainerComponentWithController) {
		super();

		this.intervention = intervention;
		this.interventionEditingContainerComponentWithController = interventionEditingContainerComponentWithController;

		lastInterventionMessagingActiveState = intervention.isMessagingActive();

		// Set the first time before other tabs are constructed
		interventionEditingContainerComponentWithController
				.setEditingDependingOnMessaging(!intervention
						.isMessagingActive());

		// Handle buttons
		val interventionBasicSettingsComponent = getInterventionBasicSettingsComponent();

		val buttonClickListener = new ButtonClickListener();
		interventionBasicSettingsComponent.getSwitchInterventionButton()
				.addClickListener(buttonClickListener);
		interventionBasicSettingsComponent.getSwitchMessagingButton()
				.addClickListener(buttonClickListener);

		// Set start state
		adjust();
	}

	private void adjust() {
		getInterventionBasicSettingsComponent().adjust(intervention.isActive(),
				intervention.isMessagingActive());

		if (lastInterventionMessagingActiveState != intervention
				.isMessagingActive()) {
			// Messaging status has been changed, so adapt UI
			interventionEditingContainerComponentWithController
					.setEditingDependingOnMessaging(!intervention
							.isMessagingActive());
		}

		lastInterventionMessagingActiveState = intervention.isMessagingActive();
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			val interventionBasicSettingsComponent = getInterventionBasicSettingsComponent();

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
					getInterventionAdministrationManagerService()
							.interventionSetActive(intervention,
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
					getInterventionAdministrationManagerService()
							.interventionSetMessagingActive(intervention,
									!intervention.isMessagingActive());
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				interventionEditingContainerComponentWithController.setEditingDependingOnMessaging(!intervention
						.isMessagingActive());

				adjust();
				closeWindow();
			}
		}, null);
	}
}
