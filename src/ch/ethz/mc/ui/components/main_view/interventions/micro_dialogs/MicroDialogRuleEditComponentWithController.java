package ch.ethz.mc.ui.components.main_view.interventions.micro_dialogs;

/* ##LICENSE## */
import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.memory.types.RuleTypes;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MicroDialog;
import ch.ethz.mc.model.persistent.MicroDialogMessage;
import ch.ethz.mc.model.persistent.MicroDialogRule;
import ch.ethz.mc.model.ui.UIMicroDialog;
import ch.ethz.mc.model.ui.UIMicroDialogElementInterface;
import ch.ethz.mc.ui.components.basics.AbstractRuleEditComponentWithController;
import ch.ethz.mc.ui.components.basics.ShortPlaceholderStringEditComponent;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the monitoring reply rule edit component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MicroDialogRuleEditComponentWithController
		extends MicroDialogRuleEditComponent {
	private final ObjectId									interventionId;

	private final AbstractRuleEditComponentWithController	ruleEditComponent;

	private final MicroDialogRule							microDialogRule;

	public MicroDialogRuleEditComponentWithController(
			final Intervention intervention, final ObjectId microDialogId,
			final ObjectId microDialogRuleId) {
		super();

		interventionId = intervention.getId();

		// Configure integrated components
		microDialogRule = getInterventionAdministrationManagerService()
				.getMicroDialogRule(microDialogRuleId);

		ruleEditComponent = getAbstractRuleEditComponentWithController();
		ruleEditComponent.init(intervention.getId(),
				RuleTypes.MICRO_DIALOG_RULES);
		ruleEditComponent.adjust(microDialogRule);

		/*
		 * Adjust own components
		 */
		// Handle combo boxes
		val nextMicroDialogWhenTrueComboBox = getNextMicroDialogWhenTrueComboBox();
		val allMicroDialogsOfIntervention = getInterventionAdministrationManagerService()
				.getAllMicroDialogsOfIntervention(interventionId);

		for (val microDialog : allMicroDialogsOfIntervention) {
			val uiMicroDialog = microDialog.toUIModelObject();

			if (!uiMicroDialog.getRelatedModelObject(MicroDialog.class).getId()
					.equals(microDialogId)) {
				nextMicroDialogWhenTrueComboBox.addItem(uiMicroDialog);
				if (microDialog.getId()
						.equals(microDialogRule.getNextMicroDialogWhenTrue())) {
					nextMicroDialogWhenTrueComboBox.select(uiMicroDialog);
				}
			}
		}
		nextMicroDialogWhenTrueComboBox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						final UIMicroDialog uiMicroDialog = (UIMicroDialog) event
								.getProperty().getValue();

						ObjectId newMicroDialogId;
						if (uiMicroDialog == null) {
							newMicroDialogId = null;
						} else {
							newMicroDialogId = uiMicroDialog
									.getRelatedModelObject(MicroDialog.class)
									.getId();
						}

						log.debug("Adjust dialog to jump to when true to {}",
								newMicroDialogId);
						getInterventionAdministrationManagerService()
								.microDialogRuleSetNextMicroDialogWhenTrue(
										microDialogRule, newMicroDialogId);

						adjust();

					}
				});

		val nextMicroDialogMessageWhenTrueComboBox = getNextMicroDialogMessageWhenTrueComboBox();
		val nextMicroDialogMessageWhenFalseComboBox = getNextMicroDialogMessageWhenFalseComboBox();
		val allMicroDialogMessagesInMicroDialog = getInterventionAdministrationManagerService()
				.getAllMicroDialogMessagesOfMicroDialog(microDialogId);

		for (val microDialogMessage : allMicroDialogMessagesInMicroDialog) {
			val uiMicroDialogMessage = microDialogMessage.toUIModelObject();

			nextMicroDialogMessageWhenTrueComboBox
					.addItem(uiMicroDialogMessage);
			if (microDialogMessage.getId().equals(
					microDialogRule.getNextMicroDialogMessageWhenTrue())) {
				nextMicroDialogMessageWhenTrueComboBox
						.select(uiMicroDialogMessage);
			}

			nextMicroDialogMessageWhenFalseComboBox
					.addItem(uiMicroDialogMessage);
			if (microDialogMessage.getId().equals(
					microDialogRule.getNextMicroDialogMessageWhenFalse())) {
				nextMicroDialogMessageWhenFalseComboBox
						.select(uiMicroDialogMessage);
			}
		}
		nextMicroDialogMessageWhenTrueComboBox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						final UIMicroDialogElementInterface uiMicroDialogMessage = (UIMicroDialogElementInterface) event
								.getProperty().getValue();

						ObjectId newMicroDialogMessageId;
						if (uiMicroDialogMessage == null) {
							newMicroDialogMessageId = null;
						} else {
							newMicroDialogMessageId = uiMicroDialogMessage
									.getRelatedModelObject(
											MicroDialogMessage.class)
									.getId();
						}

						log.debug(
								"Adjust dialog message to jump to when true to {}",
								newMicroDialogMessageId);
						getInterventionAdministrationManagerService()
								.microDialogRuleSetNextMicroDialogMessageWhenTrue(
										microDialogRule,
										newMicroDialogMessageId);

						adjust();

					}
				});
		nextMicroDialogMessageWhenFalseComboBox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						final UIMicroDialogElementInterface uiMicroDialogMessage = (UIMicroDialogElementInterface) event
								.getProperty().getValue();

						ObjectId newMicroDialogMessageId;
						if (uiMicroDialogMessage == null) {
							newMicroDialogMessageId = null;
						} else {
							newMicroDialogMessageId = uiMicroDialogMessage
									.getRelatedModelObject(
											MicroDialogMessage.class)
									.getId();
						}

						log.debug(
								"Adjust dialog message to jump to when false to {}",
								newMicroDialogMessageId);
						getInterventionAdministrationManagerService()
								.microDialogRuleSetNextMicroDialogMessageWhenFalse(
										microDialogRule,
										newMicroDialogMessageId);

						adjust();

					}
				});

		// Add button listeners
		val buttonClickListener = new ButtonClickListener();
		getStoreVariableTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);

		// Add other listeners
		getStopMicroDialogWhenTrueCheckBox()
				.setValue(microDialogRule.isStopMicroDialogWhenTrue());
		getStopMicroDialogWhenTrueCheckBox()
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						log.debug("Adjust stop micro dialog if true");
						val newValue = (boolean) event.getProperty().getValue();

						getInterventionAdministrationManagerService()
								.microDialogRuleSetStopMicroDialogWhenTrue(
										microDialogRule, newValue);

						if (newValue) {
							getLeaveDecisionPointWhenTrueCheckBox()
									.setValue(false);

							getNextMicroDialogWhenTrueComboBox().setValue(null);
							getNextMicroDialogMessageWhenTrueComboBox()
									.setValue(null);
							getNextMicroDialogMessageWhenFalseComboBox()
									.setValue(null);
						}

						adjust();
					}
				});

		getLeaveDecisionPointWhenTrueCheckBox()
				.setValue(microDialogRule.isLeaveDecisionPointWhenTrue());
		getLeaveDecisionPointWhenTrueCheckBox()
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						log.debug("Adjust leave decision point if true");
						val newValue = (boolean) event.getProperty().getValue();

						getInterventionAdministrationManagerService()
								.microDialogRuleSetLeaveDecisionPointWhenTrue(
										microDialogRule, newValue);

						if (newValue) {
							getStopMicroDialogWhenTrueCheckBox()
									.setValue(false);

							getNextMicroDialogWhenTrueComboBox().setValue(null);
							getNextMicroDialogMessageWhenTrueComboBox()
									.setValue(null);
							getNextMicroDialogMessageWhenFalseComboBox()
									.setValue(null);
						}

						adjust();
					}
				});

		// Adjust UI for first time
		adjust();
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getStoreVariableTextFieldComponent()
					.getButton()) {
				editStoreResultVariable();
			}
			event.getButton().setEnabled(true);
		}
	}

	private void adjust() {
		// Adjust store result variable
		getStoreVariableTextFieldComponent()
				.setValue(microDialogRule.getStoreValueToVariableWithName());

		// Adjust check boxes
		if (microDialogRule.isStopMicroDialogWhenTrue()) {
			getLeaveDecisionPointWhenTrueCheckBox().setEnabled(false);
		} else {
			getLeaveDecisionPointWhenTrueCheckBox().setEnabled(true);
		}

		if (microDialogRule.isLeaveDecisionPointWhenTrue()) {
			getStopMicroDialogWhenTrueCheckBox().setEnabled(false);
		} else {
			getStopMicroDialogWhenTrueCheckBox().setEnabled(true);
		}

		// Adjust combo boxes
		if (microDialogRule.isStopMicroDialogWhenTrue()
				|| microDialogRule.isLeaveDecisionPointWhenTrue()) {
			getJumpGridLayout().setEnabled(false);
		} else {
			getJumpGridLayout().setEnabled(true);
		}
	}

	public void editStoreResultVariable() {
		log.debug("Edit store result to variable");
		val allPossibleVariables = getInterventionAdministrationManagerService()
				.getAllWritableMonitoringRuleVariablesOfIntervention(
						interventionId);
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VARIABLE,
				microDialogRule.getStoreValueToVariableWithName(),
				allPossibleVariables, new ShortPlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change name
							getInterventionAdministrationManagerService()
									.microDialogRuleSetStoreResultToVariable(
											microDialogRule, getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}
				}, null);
	}
}
