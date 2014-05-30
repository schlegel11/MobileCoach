package org.isgf.mhc.modules.message_contest;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Locale;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.persistent.ParticipantVariableWithValue;
import org.isgf.mhc.model.ui.UIVariableWithParticipant;
import org.isgf.mhc.tools.StringValidator;
import org.isgf.mhc.ui.views.components.basics.PlaceholderStringEditComponent;

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

	private final ObjectId										interventionId;

	private BeanContainer<ObjectId, UIVariableWithParticipant>	beanContainer	= null;

	private Collection<ObjectId>								selectedVariableIds;

	public MessageContestModuleWithController(final ObjectId interventionId) {
		super(interventionId);

		this.interventionId = interventionId;
	}

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
	public void prepareToShow() {
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
		val allPossibleMessageVariables = getInterventionAdministrationManagerService()
				.getAllPossibleMonitoringRuleVariablesOfIntervention(
						interventionId);
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__SET_RESULT_VARIABLE_FOR_SELECTED_PARTICIPANTS,
				"", allPossibleMessageVariables,
				new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						// Check if message contains only valid strings
						if (!StringValidator.isValidVariableText(
								getStringValue(), allPossibleMessageVariables)) {

							getAdminUI()
									.showWarningNotification(
											AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);

							return;
						} else {
							val interventionExecutionManagerService = MHC
									.getInstance()
									.getInterventionExecutionManagerService();

							for (val selectedUIVariableWithParticipant : selectedVariableIds) {
								val participantVariableWithValue = beanContainer
										.getItem(
												selectedUIVariableWithParticipant)
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
						}

						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__VARIABLE_VALUE_CHANGED);

						closeWindow();
					}
				}, null);
	}
}
