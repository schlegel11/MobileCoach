package ch.ethz.mc.model.persistent.concepts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.model.ui.UIVariable;
import ch.ethz.mc.model.ui.results.UIVariableWithParticipantForResults;

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
	/**
	 * Name of the variable
	 */
	@Getter
	@Setter
	@NonNull
	private String	name;

	/**
	 * Value of the variable
	 */
	@Getter
	@Setter
	@NonNull
	private String	value;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		final val variable = new UIVariable(name, value);

		variable.setRelatedModelObject(this);

		return variable;
	}

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
