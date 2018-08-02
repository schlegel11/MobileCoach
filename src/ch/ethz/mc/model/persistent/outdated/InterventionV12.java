package ch.ethz.mc.model.persistent.outdated;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * CAUTION: Will only be used for conversion from data model 11 to 12
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class InterventionV12 extends ModelObject {
	private static final long	serialVersionUID	= -7616106521786215356L;

	/**
	 * The name of the {@link InterventionV12} as shown in the backend
	 */
	@Getter
	@Setter
	@NonNull
	private String				name;

	/**
	 * Timestamp when the {@link InterventionV12} has been created
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
	 * Defines if the monitoring in this {@link InterventionV12} is active. If
	 * not the rule execution will not be executed also if the intervention is
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
	 * checked for uniqueness regarding sepcific variable values
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
}
