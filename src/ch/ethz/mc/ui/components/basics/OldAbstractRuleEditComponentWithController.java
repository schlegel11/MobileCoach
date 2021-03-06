package ch.ethz.mc.ui.components.basics;

/* ##LICENSE## */
import java.util.List;

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.memory.types.RuleTypes;
import ch.ethz.mc.model.persistent.concepts.AbstractRule;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the abstract rule edit component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class OldAbstractRuleEditComponentWithController
		extends OldAbstractRuleEditComponent implements ValueChangeListener {

	private RuleTypes							type;

	private boolean								initDone	= false;

	private ObjectId							rulesRelatedModelObjectId;

	private AbstractRule						rule;

	private final VariableTextFieldComponent	ruleComponent;
	private final VariableTextFieldComponent	ruleComparisonTermComponent;

	private final ComboBox						ruleEquationSignComboBox;

	private final VariableTextFieldComponent	commentComponent;

	public OldAbstractRuleEditComponentWithController() {
		super();

		commentComponent = getCommentVariableTextFieldComponent();

		ruleComponent = getRuleTextFieldComponent();
		ruleComparisonTermComponent = getRuleComparisonTermTextFieldComponent();

		ruleEquationSignComboBox = getRuleEquationSignComboBox();
		ruleEquationSignComboBox.setImmediate(true);
		ruleEquationSignComboBox.setTextInputAllowed(false);
		ruleEquationSignComboBox.setNewItemsAllowed(false);
		ruleEquationSignComboBox.setNullSelectionAllowed(false);
		ruleEquationSignComboBox.setPageLength(20);

		for (val equationSignType : RuleEquationSignTypes.values()) {
			ruleEquationSignComboBox.addItem(equationSignType);
		}

		val buttonClickListener = new ButtonClickListener();
		commentComponent.getButton().addClickListener(buttonClickListener);
		ruleComponent.getButton().addClickListener(buttonClickListener);
		ruleComparisonTermComponent.getButton()
				.addClickListener(buttonClickListener);

		setEnabled(false);
	}

	public void init(final ObjectId rulesRelatedModelObjectId,
			final RuleTypes type) {
		this.rulesRelatedModelObjectId = rulesRelatedModelObjectId;
		this.type = type;

		initDone = true;
	}

	public void adjust(AbstractRule rule) {
		boolean firstRun = false;

		// Internal update when call with null
		if (rule == null) {
			rule = this.rule;
		} else {
			firstRun = true;
		}

		log.debug("Adjust for rule {}", rule);

		if (!ruleEquationSignComboBox.isSelected(rule.getRuleEquationSign())) {
			ruleEquationSignComboBox.select(rule.getRuleEquationSign());
		}
		if (firstRun) {
			ruleEquationSignComboBox.addValueChangeListener(this);
		}

		ruleComponent.setValue(rule.getRuleWithPlaceholders());
		ruleComparisonTermComponent
				.setValue(rule.getRuleComparisonTermWithPlaceholders());

		commentComponent.setValue(rule.getComment());

		this.rule = rule;

		if (initDone) {
			setEnabled(true);
		}
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == commentComponent.getButton()) {
				editCommentText();
			} else if (event.getButton() == ruleComponent.getButton()) {
				editRuleTextWithPlaceholder();
			} else if (event.getButton() == ruleComparisonTermComponent
					.getButton()) {
				editRuleComparisonTextWithPlaceholder();
			}
			event.getButton().setEnabled(true);
		}
	}

	@Override
	public void valueChange(final ValueChangeEvent event) {
		log.debug("Changing rule equation sign to {}",
				event.getProperty().getValue());
		getInterventionAdministrationManagerService()
				.abstractRuleSetEquationSign(rule,
						(RuleEquationSignTypes) event.getProperty().getValue());
	}

	public void editCommentText() {
		log.debug("Edit comment");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_COMMENT,
				rule.getComment(), null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change comment
							getInterventionAdministrationManagerService()
									.abstractRuleSetComment(rule,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust(null);

						closeWindow();
					}
				}, null);
	}

	public void editRuleTextWithPlaceholder() {
		log.debug("Edit rule with placeholder");
		final List<String> allPossibleVariables;
		switch (type) {
			case MONITORING_RULES:
			case MICRO_DIALOG_RULES:
			case MONITORING_MESSAGE_RULES:
			case MICRO_DIALOG_MESSAGE_RULES:
				allPossibleVariables = getInterventionAdministrationManagerService()
						.getAllPossibleMonitoringRuleVariablesOfIntervention(
								rulesRelatedModelObjectId);
				break;
			case SCREENING_SURVEY_RULES:
			case FEEDBACK_RULES:
				allPossibleVariables = getSurveyAdministrationManagerService()
						.getAllPossibleScreenigSurveyVariablesOfScreeningSurvey(
								rulesRelatedModelObjectId);
				break;
			default:
				allPossibleVariables = null;
		}
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_RULE_WITH_PLACEHOLDERS,
				rule.getRuleWithPlaceholders(), allPossibleVariables,
				new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change rule text with placeholders
							getInterventionAdministrationManagerService()
									.abstractRuleSetRuleWithPlaceholders(rule,
											getStringValue(),
											allPossibleVariables);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust(null);

						closeWindow();
					}
				}, null);
	}

	public void editRuleComparisonTextWithPlaceholder() {
		log.debug("Edit rule comparison term with placeholder");
		final List<String> allPossibleVariables;
		switch (type) {
			case MONITORING_RULES:
			case MONITORING_MESSAGE_RULES:
			case MICRO_DIALOG_MESSAGE_RULES:
				allPossibleVariables = getInterventionAdministrationManagerService()
						.getAllPossibleMonitoringRuleVariablesOfIntervention(
								rulesRelatedModelObjectId);
				break;
			case SCREENING_SURVEY_RULES:
			case FEEDBACK_RULES:
				allPossibleVariables = getSurveyAdministrationManagerService()
						.getAllPossibleScreenigSurveyVariablesOfScreeningSurvey(
								rulesRelatedModelObjectId);
				break;
			default:
				allPossibleVariables = null;
		}
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_TEXT_WITH_PLACEHOLDERS,
				rule.getRuleComparisonTermWithPlaceholders(),
				allPossibleVariables, new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change rule comparison text with placeholders
							getInterventionAdministrationManagerService()
									.abstractRuleSetRuleComparisonTermWithPlaceholders(
											rule, getStringValue(),
											allPossibleVariables);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust(null);

						closeWindow();
					}
				}, null);
	}
}
