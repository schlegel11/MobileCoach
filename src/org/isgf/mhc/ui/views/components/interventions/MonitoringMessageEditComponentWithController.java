package org.isgf.mhc.ui.views.components.interventions;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.server.MonitoringMessage;
import org.isgf.mhc.ui.views.components.basics.MediaObjectIntegrationComponentWithController.MediaObjectCreationOrDeleteionListener;

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
		MonitoringMessageEditComponent implements
		MediaObjectCreationOrDeleteionListener {

	private final MonitoringMessage	monitoringMessage;

	public MonitoringMessageEditComponentWithController(
			final MonitoringMessage monitoringMessage) {
		super();

		this.monitoringMessage = monitoringMessage;
		// TODO a lot
		monitoringMessage.setTextWithPlaceholders(String.valueOf(System
				.currentTimeMillis()));

		// Handle media objec to component
		if (monitoringMessage.getLinkedMediaObject() == null) {
			getIntegratedMediaObjectComponent().setMediaObject(null, this);
		} else {
			val mediaObject = getInterventionAdministrationManagerService()
					.getMediaObject(monitoringMessage.getLinkedMediaObject());
			getIntegratedMediaObjectComponent().setMediaObject(mediaObject,
					this);
		}

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

	@Override
	public void updateLinkedMediaObjectId(final ObjectId mediaObjectId) {
		getInterventionAdministrationManagerService()
				.monitoringMessageSetLinkedMediaObject(monitoringMessage,
						mediaObjectId);
	}
}
