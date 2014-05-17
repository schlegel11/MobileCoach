package org.isgf.mhc.ui.views.components.screening_survey;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.persistent.ScreeningSurveySlide;
import org.isgf.mhc.model.persistent.ScreeningSurveySlideRule;
import org.isgf.mhc.model.ui.UIScreeningSurveySlide;
import org.isgf.mhc.ui.views.components.basics.AbstractRuleEditComponentWithController;
import org.isgf.mhc.ui.views.components.basics.AbstractRuleEditComponentWithController.TYPES;
import org.isgf.mhc.ui.views.components.basics.ShortPlaceholderStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the screening survey slid rule edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class ScreeningSurveySlideRuleEditComponentWithController extends
		ScreeningSurveySlideRuleEditComponent {

	private final AbstractRuleEditComponentWithController	ruleEditComponent;

	private final ScreeningSurveySlideRule					screeningSurveySlideRule;

	private final ObjectId									screeningSurveyId;

	private class JumpIfTrueFalseValueChangeListener implements
			ValueChangeListener {
		private boolean	isTrueCase	= false;

		public void setCase(final boolean isTrueCase) {
			this.isTrueCase = isTrueCase;
		}

		@Override
		public void valueChange(final ValueChangeEvent event) {
			ObjectId selectedScreeningSurveySlide = null;
			if (event.getProperty().getValue() != null) {
				selectedScreeningSurveySlide = ((UIScreeningSurveySlide) event
						.getProperty().getValue()).getRelatedModelObject(
						ScreeningSurveySlide.class).getId();
			}

			log.debug("Adjust case {} to screening survey slide {}",
					isTrueCase ? "true" : "false", selectedScreeningSurveySlide);
			getScreeningSurveyAdministrationManagerService()
					.screeningSurveySlideRuleSetJumpToSlide(
							screeningSurveySlideRule, isTrueCase,
							selectedScreeningSurveySlide);
		}
	}

	public ScreeningSurveySlideRuleEditComponentWithController(
			final ScreeningSurveySlideRule screeningSurveySlideRule,
			final ObjectId screeningSurveyId) {
		super();

		this.screeningSurveySlideRule = screeningSurveySlideRule;
		this.screeningSurveyId = screeningSurveyId;

		// Configure integrated components
		ruleEditComponent = getAbstractRuleEditComponentWithController();
		ruleEditComponent.init(screeningSurveyId, TYPES.SCREENING_SURVEY_RULES);
		ruleEditComponent.adjust(screeningSurveySlideRule);

		/*
		 * Adjust own components
		 */
		// Handle combo boxes
		val allScreeningSurveySlidesOfScreeningSurvey = getScreeningSurveyAdministrationManagerService()
				.getAllScreeningSurveySlidesOfScreeningSurvey(screeningSurveyId);

		val jumpIfTrueComboBox = getJumpIfTrueComboBox();
		val jumpIfFalseComboBox = getJumpIfFalseComboBox();
		for (val screeningSurveySlide : allScreeningSurveySlidesOfScreeningSurvey) {
			val uiScreeningSurveySlide = screeningSurveySlide.toUIModelObject();
			jumpIfTrueComboBox.addItem(uiScreeningSurveySlide);
			jumpIfFalseComboBox.addItem(uiScreeningSurveySlide);
			if (screeningSurveySlide.getId().equals(
					screeningSurveySlideRule
							.getNextScreeningSurveySlideWhenTrue())) {
				jumpIfTrueComboBox.select(uiScreeningSurveySlide);
			}
			if (screeningSurveySlide.getId().equals(
					screeningSurveySlideRule
							.getNextScreeningSurveySlideWhenFalse())) {
				jumpIfFalseComboBox.select(uiScreeningSurveySlide);
			}
		}

		// Handle buttons
		val buttonClickListener = new ButtonClickListener();
		getStoreVariableTextFieldComponent().getButton().addClickListener(
				buttonClickListener);

		// Handle combo boxes
		val jumpIfTrueValueChangeListener = new JumpIfTrueFalseValueChangeListener();
		jumpIfTrueValueChangeListener.setCase(true);
		val jumpIfFalseValueChangeListener = new JumpIfTrueFalseValueChangeListener();
		jumpIfFalseValueChangeListener.setCase(false);
		jumpIfTrueComboBox
				.addValueChangeListener(jumpIfTrueValueChangeListener);
		jumpIfFalseComboBox
				.addValueChangeListener(jumpIfFalseValueChangeListener);

		adjust();
	}

	private void adjust() {
		// Adjust variable text fields
		getStoreVariableTextFieldComponent().setValue(
				screeningSurveySlideRule.getStoreValueToVariableWithName());
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getStoreVariableTextFieldComponent()
					.getButton()) {
				changeStoreResultVariable();
			}
		}
	}

	public void changeStoreResultVariable() {
		log.debug("Edit store result to variable");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllWritableScreenigSurveyVariablesOfScreeningSurvey(
						screeningSurveyId);
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VARIABLE,
				screeningSurveySlideRule.getStoreValueToVariableWithName(),
				allPossibleVariables,
				new ShortPlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change store result to variable
							getScreeningSurveyAdministrationManagerService()
									.screeningSurveySlideRuleChangeStoreResultToVariable(
											screeningSurveySlideRule,
											getStringValue());
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
