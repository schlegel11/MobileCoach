package ch.ethz.mc.model.persistent;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.model.ui.UIParticipant;

/**
 * {@link ModelObject} to represent an {@link Participant}
 *
 * A {@link Participant} is the person who participates in {@link Intervention}
 * s. To communicate with the {@link Participant} the system needs to know its
 * name. It furthermore stores if the {@link Participant} already performed the
 * screening survey and if the messaging is active for her/him.
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class Participant extends ModelObject {
	/**
	 * The {@link Intervention} the {@link Participant} participates in
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	intervention;

	/**
	 * The timestamp when the {@link Participant} has been created
	 */
	@Getter
	@Setter
	private long		createdTimestamp;

	/**
	 * The nickname of the {@link Participant}
	 */
	@Getter
	@Setter
	@NonNull
	private String		nickname;

	/**
	 * The language of the {@link Participant}
	 */
	@Getter
	@Setter
	@NonNull
	private Locale		language;

	/**
	 * The participation group of the {@link Participant}
	 */
	@Getter
	@Setter
	private String		group;

	/**
	 * The {@link ScreeningSurvey} the {@link Participant} participates in
	 */
	@Getter
	@Setter
	private ObjectId	assignedScreeningSurvey;

	/**
	 * Stores the reference to the {@link ScreeningSurvey} started by the
	 * {@link Participant} in an independent way; This enables to reference a
	 * {@link ScreeningSurvey} also after independent export/import to/from
	 * another system
	 */
	@Getter
	@Setter
	private String		assignedScreeningSurveyGlobalUniqueId;

	/**
	 * The {@link Feedback} the {@link Participant} participates in
	 */
	@Getter
	@Setter
	private ObjectId	assignedFeedback;

	/**
	 * Stores the reference to the {@link Feedback} started by the
	 * {@link Participant} in an independent way; This enables to reference a
	 * {@link Feedback} also after independent export/import to/from
	 * another system
	 */
	@Getter
	@Setter
	private String		assignedFeedbackGlobalUniqueId;

	/**
	 * Stores if the {@link Participant} is activated for the rule-based
	 * messaging; If a {@link Participant} should never participate in the
	 * {@link Intervention} based on this results from the
	 * {@link ScreeningSurvey} this should remain false
	 */
	@Getter
	@Setter
	private boolean		monitoringActive;

	/**
	 * The organization the {@link Participant} belongs to; can e.g. be used for
	 * groups
	 */
	@Getter
	@Setter
	@NonNull
	private String		organization;

	/**
	 * The organization unit the {@link Participant} belongs to; can e.g. be
	 * used for groups
	 */
	@Getter
	@Setter
	@NonNull
	private String		organizationUnit;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val screeningSurvey = ModelObject.get(ScreeningSurvey.class,
				assignedScreeningSurvey);

		String screeningSurveyName;
		if (screeningSurvey == null) {
			screeningSurveyName = Messages
					.getAdminString(AdminMessageStrings.UI_MODEL__UNKNOWN);
		} else if (screeningSurvey.getName().equals("")) {
			screeningSurveyName = ImplementationConstants.DEFAULT_OBJECT_NAME;
		} else {
			screeningSurveyName = screeningSurvey.getName().toString();
		}

		boolean screeningSurveyStatus = false;
		boolean dataForMonitoringAvailable = false;
		boolean monitoringStatus = false;

		val dialogStatus = MC.getInstance()
				.getInterventionAdministrationManagerService()
				.getDialogStatusOfParticipant(getId());

		if (dialogStatus == null) {
			screeningSurveyStatus = false;
			dataForMonitoringAvailable = false;
			monitoringStatus = false;
		} else {
			screeningSurveyStatus = dialogStatus.isScreeningSurveyPerformed();
			dataForMonitoringAvailable = dialogStatus
					.isDataForMonitoringParticipationAvailable();
			monitoringStatus = dialogStatus.isMonitoringPerformed();
		}

		val participant = new UIParticipant(
				getId().toString(),
				nickname.equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: nickname,
				language.getDisplayLanguage() + " (" + language.toLanguageTag()
						+ ")",
						group == null ? Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: group,
				organization,
				organizationUnit,
				new Date(createdTimestamp),
				screeningSurveyName,
				screeningSurveyStatus ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__FINISHED)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NOT_FINISHED),
				screeningSurveyStatus,
				dataForMonitoringAvailable ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__YES)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NO),
				dataForMonitoringAvailable,
				monitoringStatus ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__FINISHED)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NOT_FINISHED),
				monitoringStatus,
				monitoringActive ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__ACTIVE)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__INACTIVE),
				monitoringActive);

		participant.setRelatedModelObject(this);

		return participant;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	public void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);

		// Add participant variable with value
		for (val participantVariableWithValue : ModelObject.find(
				ParticipantVariableWithValue.class,
				Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT,
				getId())) {
			participantVariableWithValue
					.collectThisAndRelatedModelObjectsForExport(exportList);
		}

		// Add dialog option
		for (val dialogOption : ModelObject.find(DialogOption.class,
				Queries.DIALOG_OPTION__BY_PARTICIPANT, getId())) {
			dialogOption.collectThisAndRelatedModelObjectsForExport(exportList);
		}

		// Add dialog status
		for (val dialogStatus : ModelObject.find(DialogStatus.class,
				Queries.DIALOG_STATUS__BY_PARTICIPANT, getId())) {
			dialogStatus.collectThisAndRelatedModelObjectsForExport(exportList);
		}

		/*
		 * DialogMessages and SystemUniqueIds are not added to reduce complexity
		 * (Participants can only be copied before monitoring started)
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		// Delete dialog options
		val dialogOptionsToDelete = ModelObject.find(DialogOption.class,
				Queries.DIALOG_OPTION__BY_PARTICIPANT, getId());
		ModelObject.delete(dialogOptionsToDelete);

		// Delete participant variables with values
		val participantVariablesWithValuesToDelete = ModelObject.find(
				ParticipantVariableWithValue.class,
				Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT,
				getId());
		ModelObject.delete(participantVariablesWithValuesToDelete);

		// Delete dialog messages
		val dialogMessagesToDelete = ModelObject.find(DialogMessage.class,
				Queries.DIALOG_MESSAGE__BY_PARTICIPANT, getId());
		ModelObject.delete(dialogMessagesToDelete);

		// Delete dialog status
		val dialogStatusesToDelete = ModelObject.find(DialogStatus.class,
				Queries.DIALOG_STATUS__BY_PARTICIPANT, getId());
		ModelObject.delete(dialogStatusesToDelete);
	}
}
