package ch.ethz.mc.modules.message_contest;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health IS-Lab
 * 
 * For details see README.md file in the root folder of this project.
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
import java.text.DateFormat;
import java.util.Collection;
import java.util.Locale;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.persistent.ParticipantVariableWithValue;
import ch.ethz.mc.model.ui.UIVariableWithParticipant;
import ch.ethz.mc.ui.views.components.basics.StringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.converter.StringToDateConverter;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the MessageContestModule with a controller
 * 
 * @author Andreas Filler
 * 
 */
@SuppressWarnings("serial")
@Log4j2
public abstract class MessageContestModuleWithController extends
		MessageContestModule {

	private ObjectId											interventionId;

	private BeanContainer<ObjectId, UIVariableWithParticipant>	beanContainer	= null;

	private Collection<ObjectId>								selectedVariableIds;

	/**
	 * Returns the name of the variable to store for the selected partipants
	 * 
	 * @return
	 */
	protected abstract String getResultVariable();

	/**
	 * Returns the name of the variable where the values are stored in
	 * 
	 * @return
	 */
	protected abstract String getRelevantVariable();

	@Override
	public void prepareToShow(final ObjectId interventionId) {
		this.interventionId = interventionId;

		// Table options
		val relevantVariablesTable = getRelevantVariablesTable();
		relevantVariablesTable.setSelectable(true);
		relevantVariablesTable.setMultiSelect(true);
		relevantVariablesTable.setMultiSelectMode(MultiSelectMode.DEFAULT);
		relevantVariablesTable.setImmediate(true);

		// Init table
		beanContainer = createBeanContainerForModelObjects(
				UIVariableWithParticipant.class, null);
		relevantVariablesTable.setContainerDataSource(beanContainer);
		relevantVariablesTable
				.setSortContainerPropertyId(UIVariableWithParticipant
						.getSortColumn());
		relevantVariablesTable.setVisibleColumns(UIVariableWithParticipant
				.getVisibleColumns());
		relevantVariablesTable.setColumnHeaders(UIVariableWithParticipant
				.getColumnHeaders());
		relevantVariablesTable.setConverter(
				UIVariableWithParticipant.LAST_UPDATED,
				new StringToDateConverter() {
					@Override
					protected DateFormat getFormat(final Locale locale) {
						val dateFormat = DateFormat.getDateTimeInstance(
								DateFormat.MEDIUM, DateFormat.MEDIUM,
								Constants.getAdminLocale());
						return dateFormat;
					}
				});

		val participants = getInterventionAdministrationManagerService()
				.getAllParticipantsOfIntervention(interventionId);

		for (val participant : participants) {
			val variables = getInterventionAdministrationManagerService()
					.getAllParticipantVariablesUpdatedWithinLast28Days(
							participant.getId(), getRelevantVariable());

			for (val variable : variables) {
				val variableWithParticipant = variable
						.toUIVariableWithParticipant(
								participant.getId().toString(),
								participant.getNickname().equals("") ? Messages
										.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
										: participant.getNickname(),
								participant.getOrganization().equals("") ? Messages
										.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
										: participant.getOrganization(),
								participant.getOrganizationUnit().equals("") ? Messages
										.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
										: participant.getOrganizationUnit());

				beanContainer
						.addItem(variable.getId(), variableWithParticipant);
			}
		}

		relevantVariablesTable.sort();

		// handle button
		val buttonClickListener = new ButtonClickListener();
		getSetResultButton().addClickListener(buttonClickListener);

		// Listener for table
		relevantVariablesTable
				.addValueChangeListener(new ValueChangeListener() {

					@SuppressWarnings("unchecked")
					@Override
					public void valueChange(final ValueChangeEvent event) {
						selectedVariableIds = (Collection<ObjectId>) relevantVariablesTable
								.getValue();

						if (selectedVariableIds == null
								|| selectedVariableIds.size() == 0) {
							getSetResultButton().setEnabled(false);
						} else {
							getSetResultButton().setEnabled(true);
						}
					}
				});

		getSetResultButton().setEnabled(false);
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {

			if (event.getButton() == getSetResultButton()) {
				setResultValue();
			}
		}
	}

	public void setResultValue() {
		log.debug("Store variable values");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__SET_RESULT_VARIABLE_FOR_SELECTED_PARTICIPANTS,
				"", null, new StringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						// Check if message contains only valid strings
						val interventionExecutionManagerService = MC
								.getInstance()
								.getInterventionExecutionManagerService();

						for (val selectedUIVariableWithParticipant : selectedVariableIds) {
							val participantVariableWithValue = beanContainer
									.getItem(selectedUIVariableWithParticipant)
									.getBean()
									.getRelatedModelObject(
											ParticipantVariableWithValue.class);

							getInterventionAdministrationManagerService()
									.interventionVariableWithValueCreateOrUpdate(
											interventionId,
											getResultVariable(),
											getStringValue());

							interventionExecutionManagerService
									.participantAdjustVariableValue(
											participantVariableWithValue
													.getParticipant(),
											getResultVariable(),
											getStringValue());
						}

						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__VARIABLE_VALUE_CHANGED);

						closeWindow();
					}
				}, null);
	}
}
