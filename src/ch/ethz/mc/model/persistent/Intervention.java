package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.ui.UIIntervention;
import ch.ethz.mc.model.ui.UIModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * {@link ModelObject} to represent an {@link Intervention}
 *
 * An {@link Intervention} describes the whole project consisting of a
 * {@link ScreeningSurvey}, {@link MonitoringRule}s and {@link Participant}s.
 * It's the heart of the whole system.
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class Intervention extends ModelObject {
	private static final long	serialVersionUID	= 226165325700903678L;

	/**
	 * The name of the {@link Intervention} as shown in the backend
	 */
	@Getter
	@Setter
	@NonNull
	private String				name;

	/**
	 * Timestamp when the {@link Intervention} has been created
	 */
	@Getter
	@Setter
	private long				created;

	/**
	 * Defines if the whole intervention is active. If this value is false, also
	 * the messaging and the {@link ScreeningSurvey}s of the intervention are
	 * not accessable.
	 */
	@Getter
	@Setter
	private boolean				active;

	/**
	 * Defines if the monitoring in this {@link Intervention} is active. If not
	 * the rule execution will not be executed also if the intervention is
	 * active.
	 */
	@Getter
	@Setter
	private boolean				monitoringActive;

	/**
	 * Defines if the dashboard of the intervention can be accessed.
	 */
	@Getter
	@Setter
	private boolean				dashboardEnabled;

	/**
	 * The path of the template for the dashboard
	 */
	@Getter
	@Setter
	@NonNull
	private String				dashboardTemplatePath;

	/**
	 * <strong>OPTIONAL:</strong> The password pattern (containing regular
	 * expressions) required to access the dashboard
	 */
	@Getter
	@Setter
	private String				dashboardPasswordPattern;

	/**
	 * <strong>OPTIONAL:</strong> The password required to access the deepstream
	 * interface
	 */
	@Getter
	@Setter
	private String				deepstreamPassword;

	/**
	 * Defines if {@link ScreeningSurvey}s of participants where all relevant
	 * monitoring data is available will automatically be finished by the system
	 */
	@Getter
	@Setter
	private boolean				automaticallyFinishScreeningSurveys;

	/**
	 * Defines which other interventions on a specific server instance should be
	 * checked for uniqueness regarding specific variable values
	 */
	@Getter
	@Setter
	private String[]			interventionsToCheckForUniqueness;

	/**
	 * Defines the monitoring starting days of the intervention
	 */
	@Getter
	@Setter
	private int[]				monitoringStartingDays;

	/**
	 * The sender identification used to send messages to the
	 * {@link Participant}s
	 */
	@Getter
	@Setter
	private String				assignedSenderIdentification;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val intervention = new UIIntervention(name, active, active
				? Messages.getAdminString(AdminMessageStrings.UI_MODEL__ACTIVE)
				: Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__INACTIVE),
				monitoringActive,
				monitoringActive
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__ACTIVE)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__INACTIVE),
				assignedSenderIdentification == null
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NOT_SET)
						: assignedSenderIdentification);

		intervention.setRelatedModelObject(this);

		return intervention;
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

		// Add screening surveys
		for (val screeningSurvey : ModelObject.find(ScreeningSurvey.class,
				Queries.SCREENING_SURVEY__BY_INTERVENTION, getId())) {
			screeningSurvey
					.collectThisAndRelatedModelObjectsForExport(exportList);
		}

		// Add intervention variables with values
		for (val interventionVariableWithValue : ModelObject.find(
				InterventionVariableWithValue.class,
				Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION,
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

		// Add micro dialogs
		for (val microDialogs : ModelObject.find(MicroDialog.class,
				Queries.MICRO_DIALOG__BY_INTERVENTION, getId())) {
			microDialogs.collectThisAndRelatedModelObjectsForExport(exportList);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
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
				Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION,
				getId());
		ModelObject.delete(interventionVariablesWithValuesToDelete);

		// Delete backend user intervention access
		val backendUserInterventionAccessToDelete = ModelObject.find(
				BackendUserInterventionAccess.class,
				Queries.BACKEND_USER_INTERVENTION_ACCESS__BY_INTERVENTION,
				getId());
		ModelObject.delete(backendUserInterventionAccessToDelete);

		// Delete monitoring rules
		val monitoringRulesToDelete = ModelObject.find(MonitoringRule.class,
				Queries.MONITORING_RULE__BY_INTERVENTION, getId());
		ModelObject.delete(monitoringRulesToDelete);

		// Delete monitoring message groups
		val monitoringMessageGroupsToDelete = ModelObject.find(
				MonitoringMessageGroup.class,
				Queries.MONITORING_MESSAGE_GROUP__BY_INTERVENTION, getId());
		ModelObject.delete(monitoringMessageGroupsToDelete);

		// Delete micro dialogs
		val microDialogsToDelete = ModelObject.find(MicroDialog.class,
				Queries.MICRO_DIALOG__BY_INTERVENTION, getId());
		ModelObject.delete(microDialogsToDelete);

		// Delete screening surveys
		val screeningSurveysToDelete = ModelObject.find(ScreeningSurvey.class,
				Queries.SCREENING_SURVEY__BY_INTERVENTION, getId());
		ModelObject.delete(screeningSurveysToDelete);
		
		// Delete intervention external services
		val interventionExternalServicesToDelete = ModelObject.find(
				InterventionExternalSystem.class,
				Queries.INTERVENTION_EXTERNAL_SYSTEM__BY_INTERVENTION,
				getId());
		ModelObject.delete(interventionExternalServicesToDelete);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.AbstractSerializableTable#toTable()
	 */
	@Override
	@JsonIgnore
	public String toTable() {
		String table = wrapRow(wrapHeader("Name:") + wrapField(escape(name)));
		table += wrapRow(
				wrapHeader("Created:") + wrapField(formatDate(created)));
		table += wrapRow(wrapHeader("Intervention Status:")
				+ wrapField(formatStatus(active)));
		table += wrapRow(wrapHeader("Monitoring Status:")
				+ wrapField(formatStatus(monitoringActive)));
		table += wrapRow(wrapHeader("Assigned Sender Identification:")
				+ wrapField(assignedSenderIdentification));
		table += wrapRow(wrapHeader(
				"Screening Surveys shall automatically be finished:")
				+ wrapField(formatYesNo(automaticallyFinishScreeningSurveys)));
		table += wrapRow(wrapHeader("Monitoring Starting Days:")
				+ wrapField(StringUtils.join(monitoringStartingDays, ',')));
		return wrapTable(table);
	}
}
