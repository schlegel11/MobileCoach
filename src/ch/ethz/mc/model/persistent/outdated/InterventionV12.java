package ch.ethz.mc.model.persistent.outdated;

/* ##LICENSE## */
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
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
