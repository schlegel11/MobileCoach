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

	private final Intervention	intervention;

	public InterventionBasicSettingsTabComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		// handle buttons
		val interventionBasicSettingsComponent = getInterventionBasicSettingsComponent();

		val buttonClickListener = new ButtonClickListener();
		interventionBasicSettingsComponent.getSwitchInterventionButton()
				.addClickListener(buttonClickListener);
		interventionBasicSettingsComponent.getSwitchMessagingButton()
				.addClickListener(buttonClickListener);

		// Handle sliders
		// TODO slider listeners

		// set start state
		adjust();
	}

	private void adjust() {
		getInterventionBasicSettingsComponent().adjust(intervention.isActive(),
				intervention.isMessagingActive(),
				intervention.getHourOfDailyRuleExecutionStart(),
				intervention.getSecondsDelayBetweenParticipantsRuleExecution());

		// TODO also all other accordion tabs have to be deactivated when
		// messaging
		// is switched to active
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
		// TODO Auto-generated method stub

		adjust();
	}

	public void switchMessaging() {
		log.debug("Switch messaging");
		// TODO Auto-generated method stub

		adjust();
	}
}
