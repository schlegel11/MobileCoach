package ch.ethz.mc.model.persistent;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.ui.UIVariableWithParticipant;

/**
 * {@link ModelObject} to represent an {@link ParticipantVariableWithValue}
 * 
 * SystemVariables belong to the referenced {@link Participant} and consist of a
 * name
 * and a value.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public class ParticipantVariableWithValue extends AbstractVariableWithValue {
	/**
	 * Default constructor
	 */
	public ParticipantVariableWithValue(final ObjectId participant,
			final long lastUpdated, final String name, final String value) {
		super(name, value);

		this.participant = participant;
		this.lastUpdated = lastUpdated;
	}

	/**
	 * {@link Participant} to which this variable and its value belong to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	participant;

	/**
	 * The moment in time when the variable was updated the last time
	 */
	@Getter
	@Setter
	private long		lastUpdated;

	/**
	 * Creates a {@link UIVariableWithParticipant} with the belonging
	 * {@link Participant}
	 * 
	 * @param participantName
	 * @return
	 */
	public UIVariableWithParticipant toUIVariableWithParticipant(
			final String participantId, final String participantName,
			final String organization, final String organizationUnit) {
		final UIVariableWithParticipant variable;

		variable = new UIVariableWithParticipant(participantId,
				participantName, organization, organizationUnit, getName(),
				getValue(), new Date(getLastUpdated()));

		variable.setRelatedModelObject(this);

		return variable;
	}

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
