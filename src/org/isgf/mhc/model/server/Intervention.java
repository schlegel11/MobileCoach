package org.isgf.mhc.model.server;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.ui.UIIntervention;
import org.isgf.mhc.model.ui.UIModelObject;

/**
 * {@link ModelObject} to represent an {@link Intervention}
 * 
 * An {@link Intervention} describes the whole project consisting of a
 * {@link ScreeningSurvey}, {@link MonitoringRule}s and {@link Participant}s.
 * It's the heart of the whole system.
 * 
 * @author Andreas Filler
 */
/**
 * @author Andreas Filler
 * 
 */
@NoArgsConstructor
@AllArgsConstructor
public class Intervention extends ModelObject {
	/**
	 * The name of the {@link Intervention} as shown in the backend
	 */
	@Getter
	@Setter
	@NonNull
	private String	name;

	/**
	 * Timestamp when the {@link Intervention} has been created
	 */
	@Getter
	@Setter
	private long	created;

	/**
	 * Defines if the whole intervention is active. If this value is false, also
	 * the messaging and the {@link ScreeningSurvey}s of the intervention are
	 * not accessable.
	 */
	@Getter
	@Setter
	private boolean	active;

	/**
	 * Defines if the messaging in this {@link Intervention} is active. If not
	 * the rule execution will not be executed also if the intervention is
	 * active.
	 */
	@Getter
	@Setter
	private boolean	messagingActive;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val intervention = new UIIntervention(
				name,
				active,
				active ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__ACTIVE)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__INACTIVE),
				messagingActive,
				messagingActive ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__ACTIVE)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__INACTIVE));

		intervention.setRelatedModelObject(this);

		return intervention;
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

		// Add intervention variables with values
		for (val interventionVariableWithValue : ModelObject.find(
				InterventionVariableWithValue.class,
				Queries.INTERVENTION_VARIABLES_WITH_VALUES__BY_INTERVENTION,
				getId())) {
			interventionVariableWithValue
					.collectThisAndRelatedModelObjectsForExport(exportList);
		}

		// Add monitoring rules
		for (val monitoringRules : ModelObject.find(MonitoringRule.class,
				Queries.MONITORING_RULE__BY_INTERVENTION, getId())) {
			monitoringRules
					.collectThisAndRelatedModelObjectsForExport(exportList);
		}

		// Add monitoring message groups
		for (val monitoringMessageGroups : ModelObject.find(
				MonitoringMessageGroup.class,
				Queries.MONITORING_MESSAGE_GROUP__BY_INTERVENTION, getId())) {
			monitoringMessageGroups
					.collectThisAndRelatedModelObjectsForExport(exportList);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		// Delete participant
		val participantsToDelete = ModelObject.find(Participant.class,
				Queries.PARTICIPANT__BY_INTERVENTION, getId());
		ModelObject.delete(participantsToDelete);

		// Delete intervention variables with values
		val interventionVariablesWithValuesToDelete = ModelObject.find(
				InterventionVariableWithValue.class,
				Queries.INTERVENTION_VARIABLES_WITH_VALUES__BY_INTERVENTION,
				getId());
		ModelObject.delete(interventionVariablesWithValuesToDelete);

		// Delete author intervention access
		val authorInterventionAccessToDelete = ModelObject.find(
				AuthorInterventionAccess.class,
				Queries.AUTHOR_INTERVENTION_ACCESS__BY_INTERVENTION, getId());
		ModelObject.delete(authorInterventionAccessToDelete);

		// Delete monitoring rules
		val monitoringRulesToDelete = ModelObject.find(MonitoringRule.class,
				Queries.MONITORING_RULE__BY_INTERVENTION, getId());
		ModelObject.delete(monitoringRulesToDelete);

		// Delete monitoring message groups
		val monitoringMessageGroupsToDelete = ModelObject.find(
				MonitoringMessageGroup.class,
				Queries.MONITORING_MESSAGE_GROUP__BY_INTERVENTION, getId());
		ModelObject.delete(monitoringMessageGroupsToDelete);

		// Delete screening surveys
		val screeningSurveysToDelete = ModelObject.find(ScreeningSurvey.class,
				Queries.SCREENING_SURVEY__BY_INTERVENTION, getId());
		ModelObject.delete(screeningSurveysToDelete);
	}
}
