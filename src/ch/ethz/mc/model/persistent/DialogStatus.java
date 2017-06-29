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

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;

/**
 * {@link ModelObject} to represent an {@link DialogStatus}
 * 
 * A {@link DialogStatus} describes the participation status of a
 * {@link Participant} for the related {@link Intervention}
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class DialogStatus extends ModelObject {
	/**
	 * The {@link Participant} which belongs to this {@link DialogStatus}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	participant;

	/**
	 * Daily unique {@link String} used to check if the {@link Participant} has
	 * already be checked for rule execution today
	 */
	@Getter
	@Setter
	@NonNull
	private String		dateIndexOfLastDailyMonitoringProcessing;

	/**
	 * The last {@link ScreeningSurveySlide} the {@link Participant} visited
	 */
	@Getter
	@Setter
	private ObjectId	lastVisitedScreeningSurveySlide;

	/**
	 * Stores the reference to the {@link ScreeningSurveySlide} last visited by
	 * the {@link Participant} in an independent way; This enables to reference
	 * a {@link ScreeningSurveySlide} also after independent export/import
	 * to/from another system
	 */
	@Getter
	@Setter
	private String		lastVisitedScreeningSurveySlideGlobalUniqueId;

	/**
	 * The timestamp when the last {@link ScreeningSurveySlide} was visited by
	 * the {@link Participant}
	 */
	@Getter
	@Setter
	private long		lastVisitedScreeningSurveySlideTimestamp;

	/**
	 * Stores if the {@link Participant} has all information stored that are
	 * required to participate in the Monitoring
	 */
	@Getter
	@Setter
	private boolean		dataForMonitoringParticipationAvailable;

	/**
	 * Timestamp when the user started the {@link ScreeningSurvey}
	 */
	@Getter
	@Setter
	private long		screeningSurveyStartedTimestamp;

	/**
	 * Timestamp when the user finished the {@link ScreeningSurvey}
	 */
	@Getter
	@Setter
	private long		screeningSurveyPerformedTimestamp;

	/**
	 * Stores if the {@link Participant} already performed a
	 * {@link ScreeningSurvey} of the related {@link Intervention}
	 */
	@Getter
	@Setter
	private boolean		screeningSurveyPerformed;

	/**
	 * Timestamp when the user started the monitoring
	 */
	@Getter
	@Setter
	private long		monitoringStartedTimestamp;

	/**
	 * Timestamp when the user finished the monitoring
	 */
	@Getter
	@Setter
	private long		monitoringPerformedTimestamp;

	/**
	 * Stores if the {@link Participant} performed the whole
	 * monitoring process
	 */
	@Getter
	@Setter
	private boolean		monitoringPerformed;

	/**
	 * Number of days the {@link Participant} participated in the monitoring
	 */
	@Getter
	@Setter
	private int			monitoringDaysParticipated;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}
}
