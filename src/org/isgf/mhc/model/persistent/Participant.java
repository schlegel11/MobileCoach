package org.isgf.mhc.model.persistent;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.ui.UIModelObject;
import org.isgf.mhc.model.ui.UIParticipant;

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
	 * Stores if the {@link Participant} already performed a
	 * {@link ScreeningSurvey} of this {@link Intervention}
	 */
	@Getter
	@Setter
	private boolean		performedScreeningSurvey;

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
	 * Timestamp when the user finished the {@link ScreeningSurvey}
	 */
	@Getter
	@Setter
	private long		screeningSurveyPerformedTimestamp;

	/**
	 * Stores if the {@link Participant} is activated for the rule-based
	 * messaging; If a {@link Participant} should never participate in the
	 * {@link Intervention} based on this results from the
	 * {@link ScreeningSurvey} this should remain false
	 */
	@Getter
	@Setter
	private boolean		messagingActive;

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
	 * @see org.isgf.mhc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val screeningSurvey = ModelObject.findOne(ScreeningSurvey.class,
				Queries.SCREENING_SURVEY__BY_INTERVENTION_AND_GLOBAL_UNIQUE_ID,
				intervention, assignedScreeningSurveyGlobalUniqueId);

		String screeningSurveyName;
		if (screeningSurvey == null) {
			screeningSurveyName = Messages
					.getAdminString(AdminMessageStrings.UI_MODEL__UNKNOWN);
		} else if (screeningSurvey.getName().equals("")) {
			screeningSurveyName = ImplementationContants.DEFAULT_OBJECT_NAME;
		} else {
			screeningSurveyName = screeningSurvey.getName();
		}

		val participant = new UIParticipant(
				nickname.equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: nickname,
				organization,
				organizationUnit,
				new Date(createdTimestamp),
				screeningSurveyName,
				messagingActive ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__ACTIVE)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__INACTIVE),
				messagingActive);

		participant.setRelatedModelObject(this);

		return participant;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.isgf.mhc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#performOnDelete()
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
	}
}
