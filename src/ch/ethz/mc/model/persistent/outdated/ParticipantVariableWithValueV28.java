package ch.ethz.mc.model.persistent.outdated;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * CAUTION: Will only be used for conversion from data model 28 to 29
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
public class ParticipantVariableWithValueV28 extends AbstractVariableWithValue {
	private static final long serialVersionUID = 4556566163793166655L;

	/**
	 * Default constructor
	 */
	public ParticipantVariableWithValueV28(final ObjectId participant,
			final long timestamp, final String name, final String value) {
		super(name, value);

		this.participant = participant;
		this.timestamp = timestamp;
		describesMediaUpload = false;
	}

	/**
	 * {@link Participant} to which this variable and its value belong to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	participant;

	/**
	 * The moment in time when the variable was created
	 */
	@Getter
	@Setter
	private long		timestamp;

	@Getter
	@Setter
	private boolean		describesMediaUpload;
}
