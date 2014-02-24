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

		// Handle media object to component
		if (monitoringMessage.getLinkedMediaObject() == null) {
			getIntegratedMediaObjectComponent().setMediaObject(null, this);
		} else {
			val mediaObject = getInterventionAdministrationManagerService()
					.getMediaObject(monitoringMessage.getLinkedMediaObject());
			getIntegratedMediaObjectComponent().setMediaObject(mediaObject,
					this);
		}

		// Adjust UI
		adjust();

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getTextWithPlaceholdersTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);
		getStoreVariableTextFieldComponent().getButton().addClickListener(
				buttonClickListener);
	}

	private void adjust() {
		getTextWithPlaceholdersTextFieldComponent().setValue(
				monitoringMessage.getTextWithPlaceholders());
		getStoreVariableTextFieldComponent().setValue(
				monitoringMessage.getStoreValueToVariableWithName());
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getTextWithPlaceholdersTextFieldComponent()
					.getButton()) {
				editTextWithPlaceholder();
			} else if (event.getButton() == getStoreVariableTextFieldComponent()
					.getButton()) {
				editStoreVariable();
			}
		}
	}

	public void editTextWithPlaceholder() {
		// TODO Auto-generated method stub

	}

	public void editStoreVariable() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateLinkedMediaObjectId(final ObjectId mediaObjectId) {
		getInterventionAdministrationManagerService()
				.monitoringMessageSetLinkedMediaObject(monitoringMessage,
						mediaObjectId);
	}
}
