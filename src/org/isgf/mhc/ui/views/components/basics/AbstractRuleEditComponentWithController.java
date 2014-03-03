package org.isgf.mhc.ui.views.components.basics;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.server.concepts.AbstractRule;
import org.isgf.mhc.model.server.types.EquationSignTypes;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;

/**
 * Extends the abstract rule edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class AbstractRuleEditComponentWithController extends
		AbstractRuleEditComponent implements ValueChangeListener {

	private boolean								initDone	= false;

	private ObjectId							interventionId;

	private AbstractRule						rule;

	private final VariableTextFieldComponent	ruleComponent;
	private final VariableTextFieldComponent	ruleComparisonTermComponent;

	private final ComboBox						ruleEquationSignComboBox;

	public AbstractRuleEditComponentWithController() {
		super();

		ruleComponent = getRuleTextFieldComponent();
		ruleComparisonTermComponent = getRuleComparisonTermTextFieldComponent();

		ruleEquationSignComboBox = getRuleEquationSignComboBox();
		ruleEquationSignComboBox.setImmediate(true);
		ruleEquationSignComboBox.setTextInputAllowed(false);
		ruleEquationSignComboBox.setNewItemsAllowed(false);
		ruleEquationSignComboBox.setNullSelectionAllowed(false);

		for (val equationSignType : EquationSignTypes.values()) {
			ruleEquationSignComboBox.addItem(equationSignType);
		}

		ruleEquationSignComboBox.addValueChangeListener(this);

		val buttonClickListener = new ButtonClickListener();
		ruleComponent.getButton().addClickListener(buttonClickListener);
		ruleComparisonTermComponent.getButton().addClickListener(
				buttonClickListener);

		setEnabled(false);
	}

	public void init(final ObjectId interventionId) {
		this.interventionId = interventionId;

		initDone = true;
	}

	public void adjust(AbstractRule rule) {

		// Internal update when call with null
		if (rule == null) {
			rule = this.rule;
		}

		log.debug("Adjust for rule {}", rule);

		if (!ruleEquationSignComboBox.isSelected(rule.getRuleEquationSign())) {
			ruleEquationSignComboBox.select(rule.getRuleEquationSign());
		}

		ruleComponent.setValue(rule.getRuleWithPlaceholders());
		ruleComparisonTermComponent.setValue(rule
				.getRuleComparisonTermWithPlaceholders());

		this.rule = rule;

		if (initDone) {
			setEnabled(true);
		}
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == ruleComponent.getButton()) {
				editRuleTextWithPlaceholder();
			} else if (event.getButton() == ruleComparisonTermComponent
					.getButton()) {
				editRuleComparisonTextWithPlaceholder();
			}
		}
	}

	@Override
	public void valueChange(final ValueChangeEvent event) {
		log.debug("Changing rule equation sign to {}", event.getProperty()
				.getValue());
		getInterventionAdministrationManagerService()
				.abstractRuleChangeEquationSign(rule,
						(EquationSignTypes) event.getProperty().getValue());
	}

	public void editRuleTextWithPlaceholder() {
		log.debug("Edit rule with placeholder");
		val allPossibleMonitoringRulesVariables = getInterventionAdministrationManagerService()
				.getAllPossibleMonitoringRuleVariables(interventionId);
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_TEXT_WITH_PLACEHOLDERS,
				rule.getRuleWithPlaceholders(),
				allPossibleMonitoringRulesVariables,
				new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change rule text with placeholders
							getInterventionAdministrationManagerService()
									.abstractRuleChangeRuleWithPlaceholders(
											rule, getStringValue(),
											allPossibleMonitoringRulesVariables);
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
		val allPossibleMonitoringRulesVariables = getInterventionAdministrationManagerService()
				.getAllPossibleMonitoringRuleVariables(interventionId);
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_TEXT_WITH_PLACEHOLDERS,
				rule.getRuleComparisonTermWithPlaceholders(),
				allPossibleMonitoringRulesVariables,
				new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change rule comparison text with placeholders
							getInterventionAdministrationManagerService()
									.abstractRuleChangeRuleComparisonTermWithPlaceholders(
											rule, getStringValue(),
											allPossibleMonitoringRulesVariables);
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
