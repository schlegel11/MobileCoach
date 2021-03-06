package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import java.util.List;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

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
	private static final long	serialVersionUID	= -961410122505516257L;

	/**
	 * The {@link Participant} which belongs to this {@link DialogStatus}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			participant;

	/**
	 * Daily unique {@link String} used to check if the {@link Participant} has
	 * already be checked for rule execution today
	 */
	@Getter
	@Setter
	@NonNull
	private String				dateIndexOfLastDailyMonitoringProcessing;

	/**
	 * The last {@link ScreeningSurveySlide} the {@link Participant} visited
	 */
	@Getter
	@Setter
	private ObjectId			lastVisitedScreeningSurveySlide;

	/**
	 * Stores the reference to the {@link ScreeningSurveySlide} last visited by
	 * the {@link Participant} in an independent way; This enables to reference
	 * a {@link ScreeningSurveySlide} also after independent export/import
	 * to/from another system
	 */
	@Getter
	@Setter
	private String				lastVisitedScreeningSurveySlideGlobalUniqueId;

	/**
	 * The timestamp when the last {@link ScreeningSurveySlide} was visited by
	 * the {@link Participant}
	 */
	@Getter
	@Setter
	private long				lastVisitedScreeningSurveySlideTimestamp;

	/**
	 * Stores if the {@link Participant} has all information stored that are
	 * required to participate in the Monitoring
	 */
	@Getter
	@Setter
	private boolean				dataForMonitoringParticipationAvailable;

	/**
	 * Timestamp when the user started the {@link ScreeningSurvey}
	 */
	@Getter
	@Setter
	private long				screeningSurveyStartedTimestamp;

	/**
	 * Timestamp when the user finished the {@link ScreeningSurvey}
	 */
	@Getter
	@Setter
	private long				screeningSurveyPerformedTimestamp;

	/**
	 * Stores if the {@link Participant} already performed a
	 * {@link ScreeningSurvey} of the related {@link Intervention}
	 */
	@Getter
	@Setter
	private boolean				screeningSurveyPerformed;

	/**
	 * Timestamp when the user started the monitoring
	 */
	@Getter
	@Setter
	private long				monitoringStartedTimestamp;

	/**
	 * Timestamp when the user finished the monitoring
	 */
	@Getter
	@Setter
	private long				monitoringPerformedTimestamp;

	/**
	 * Stores if the {@link Participant} performed the whole monitoring process
	 */
	@Getter
	@Setter
	private boolean				monitoringPerformed;

	/**
	 * Number of days the {@link Participant} participated in the monitoring
	 */
	@Getter
	@Setter
	private int					monitoringDaysParticipated;

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
