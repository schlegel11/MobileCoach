package ch.ethz.mc.ui.views.components.screening_survey;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.model.persistent.ScreeningSurveySlideRule;
import ch.ethz.mc.model.ui.UIScreeningSurveySlide;
import ch.ethz.mc.ui.views.components.basics.AbstractRuleEditComponentWithController;
import ch.ethz.mc.ui.views.components.basics.AbstractRuleEditComponentWithController.TYPES;
import ch.ethz.mc.ui.views.components.basics.ShortPlaceholderStringEditComponent;
import ch.ethz.mc.ui.views.components.basics.ShortStringEditComponent;

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

		// Handle checkbox
		getInvalidWhenTrueCheckbox().addValueChangeListener(
				new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						val newValue = (Boolean) event.getProperty().getValue();

						getScreeningSurveyAdministrationManagerService()
								.screeningSurveySlideRuleChangeShowSameSlideBecauseValueNotValidWhenTrue(
										screeningSurveySlideRule, newValue);

						adjust();
					}
				});

		// Handle buttons
		val buttonClickListener = new ButtonClickListener();
		getStoreVariableTextFieldComponent().getButton().addClickListener(
				buttonClickListener);
		getStoreValueTextFieldComponent().getButton().addClickListener(
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
		getStoreValueTextFieldComponent().setValue(
				screeningSurveySlideRule.getValueToStoreToVariable());

		// Adjust checkbox
		getInvalidWhenTrueCheckbox().setValue(
				screeningSurveySlideRule
						.isShowSameSlideBecauseValueNotValidWhenTrue());

		// Adjust jump to slide combo boxes
		if (screeningSurveySlideRule
				.isShowSameSlideBecauseValueNotValidWhenTrue()) {
			getJumpIfTrueComboBox().setEnabled(false);
			getJumpIfFalseComboBox().setEnabled(false);
			getJumpIfTrueComboBox().setValue(null);
			getJumpIfFalseComboBox().setValue(null);
		} else {
			getJumpIfTrueComboBox().setEnabled(true);
			getJumpIfFalseComboBox().setEnabled(true);
		}
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getStoreVariableTextFieldComponent()
					.getButton()) {
				changeVariableToStoreValueTo();
			} else if (event.getButton() == getStoreValueTextFieldComponent()
					.getButton()) {
				changeValueToStore();
			}
		}
	}

	public void changeVariableToStoreValueTo() {
		log.debug("Edit variable to store value to");
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
							// Change variable to store value to
							getScreeningSurveyAdministrationManagerService()
									.screeningSurveySlideRuleChangeVariableToStoreValueTo(
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

	public void changeValueToStore() {
		log.debug("Edit value to store to variable");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VALUE,
				screeningSurveySlideRule.getValueToStoreToVariable(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change value to store to variable
							getScreeningSurveyAdministrationManagerService()
									.screeningSurveySlideRuleChangeValueToStoreToVariable(
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
