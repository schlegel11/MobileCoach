package ch.ethz.mc.model.persistent;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.ui.UIIntervention;
import ch.ethz.mc.model.ui.UIModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	 * Defines if the monitoring in this {@link Intervention} is active. If not
	 * the rule execution will not be executed also if the intervention is
	 * active.
	 */
	@Getter
	@Setter
	private boolean	monitoringActive;

	/**
	 * Defines if {@link ScreeningSurvey}s of participants where all relevant
	 * monitoring data is available will automatically be finished by the system
	 */
	@Getter
	@Setter
	private boolean	automaticallyFinishScreeningSurveys;

	/**
	 * The sender identification used to send messages to the
	 * {@link Participant}s
	 */
	@Getter
	@Setter
	private String	assignedSenderIdentification;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
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
				monitoringActive,
				monitoringActive ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__ACTIVE)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__INACTIVE),
				assignedSenderIdentification == null ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.AbstractSerializableTable#toTable()
	 */
	@Override
	@JsonIgnore
	public String toTable() {
		String table = wrapRow(wrapHeader("Name:") + wrapField(escape(name)));
		table += wrapRow(wrapHeader("Created:")
				+ wrapField(formatDate(created)));
		table += wrapRow(wrapHeader("Intervention Status:")
				+ wrapField(formatStatus(active)));
		table += wrapRow(wrapHeader("Monitoring Status:")
				+ wrapField(formatStatus(monitoringActive)));
		table += wrapRow(wrapHeader("Screening Surveys shall automatically be finished:")
				+ wrapField(formatYesNo(automaticallyFinishScreeningSurveys)));
		table += wrapRow(wrapHeader("Assigned Sender Identification:")
				+ wrapField(assignedSenderIdentification));
		return wrapTable(table);
	}
}
