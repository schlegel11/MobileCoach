package ch.ethz.mc.model.persistent.concepts;

/* ##LICENSE## */
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.ui.results.UIVariableWithParticipantForResults;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * {@link ModelObject} to represent a variable value combination
 *
 * A variable has a unique name and a value
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractVariableWithValue extends ModelObject {
	private static final long	serialVersionUID	= 3769136523060772191L;

	/**
	 * Name of the variable
	 */
	@Getter
	@Setter
	@NonNull
	private String				name;

	/**
	 * Value of the variable
	 */
	@Getter
	@Setter
	@NonNull
	private String				value;

	/**
	 * Creates a {@link UIVariableWithParticipantForResults} with the belonging
	 * {@link Participant}
	 *
	 * @param participantName
	 * @return
	 */
	public UIVariableWithParticipantForResults toUIVariableWithParticipantForResults(
			final String participantId, final String participantName) {
		final UIVariableWithParticipantForResults variable;

		variable = new UIVariableWithParticipantForResults(participantId,
				participantName, name, value);

		variable.setRelatedModelObject(this);

		return variable;
	}
}
