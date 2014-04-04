package org.isgf.mhc.ui.views.components.interventions.monitoring_messages;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.persistent.MonitoringMessage;
import org.isgf.mhc.ui.views.components.basics.MediaObjectIntegrationComponentWithController.MediaObjectCreationOrDeleteionListener;
import org.isgf.mhc.ui.views.components.basics.PlaceholderStringEditComponent;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;

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

	private final ObjectId			interventionId;

	public MonitoringMessageEditComponentWithController(
			final MonitoringMessage monitoringMessage,
			final ObjectId interventionId) {
		super();

		this.monitoringMessage = monitoringMessage;
		this.interventionId = interventionId;

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
				editStoreResultToVariable();
			}
		}
	}

	public void editTextWithPlaceholder() {
		log.debug("Edit text with placeholder");
		val allPossibleMessageVariables = getInterventionAdministrationManagerService()
				.getAllPossibleMessageVariablesOfIntervention(interventionId);
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_TEXT_WITH_PLACEHOLDERS,
				monitoringMessage.getTextWithPlaceholders(),
				allPossibleMessageVariables,
				new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change text with placeholders
							getInterventionAdministrationManagerService()
									.monitoringMessageSetTextWithPlaceholders(
											monitoringMessage,
											getStringValue(),
											allPossibleMessageVariables);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}
				}, null);
	}

	public void editStoreResultToVariable() {
		log.debug("Edit store result to variable");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VARIABLE,
				monitoringMessage.getStoreValueToVariableWithName(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change store result to variable
							getInterventionAdministrationManagerService()
									.monitoringMessageSetStoreResultToVariable(
											monitoringMessage, getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}
				}, null);
	}

	@Override
	public void updateLinkedMediaObjectId(final ObjectId mediaObjectId) {
		getInterventionAdministrationManagerService()
				.monitoringMessageSetLinkedMediaObject(monitoringMessage,
						mediaObjectId);
	}
}
