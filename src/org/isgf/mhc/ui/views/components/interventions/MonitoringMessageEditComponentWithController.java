package org.isgf.mhc.ui.views.components.interventions;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.model.server.MonitoringMessage;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the monitoring message edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringMessageEditComponentWithController extends
		MonitoringMessageEditComponent {

	private final MonitoringMessage	monitoringMessage;

	public MonitoringMessageEditComponentWithController(
			final MonitoringMessage monitoringMessage) {
		super();

		this.monitoringMessage = monitoringMessage;
		// TODO a lot
		monitoringMessage.setTextWithPlaceholders(String.valueOf(System
				.currentTimeMillis()));

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		// getNewGroupButton().addClickListener(buttonClickListener);
		// getRenameGroupButton().addClickListener(buttonClickListener);
		// getDeleteGroupButton().addClickListener(buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			// if (event.getButton() == getNewGroupButton()) {
			// createGroup();
			// } else if (event.getButton() == getRenameGroupButton()) {
			// renameGroup();
			// } else if (event.getButton() == getDeleteGroupButton()) {
			// deleteGroup();
			// }
		}
	}

}
