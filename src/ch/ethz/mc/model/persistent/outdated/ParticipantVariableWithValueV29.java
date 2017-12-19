package ch.ethz.mc.model.persistent.outdated;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * CAUTION: Will only be used for conversion from data model 28 to 29
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
public class ParticipantVariableWithValueV29 extends AbstractVariableWithValue {
	/**
	 * Default constructor
	 */
	public ParticipantVariableWithValueV29(final ObjectId participant,
			final long timestamp, final String name, final String value) {
		super(name, value);

		this.participant = participant;
		this.timestamp = timestamp;
		describesMediaUpload = false;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	public static class FormerVariableValue {
		@Getter
		@Setter
		private long	timestamp;

		@Getter
		@Setter
		@NonNull
		private String	value;

		@Getter
		@Setter
		private boolean	describesMediaUpload;
	}

	/**
	 * {@link Participant} to which this variable and its value belong to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId						participant;

	/**
	 * The moment in time when the variable was created
	 */
	@Getter
	@Setter
	private long							timestamp;

	@Getter
	@Setter
	private boolean							describesMediaUpload;

	@Getter
	private final List<FormerVariableValue>	formerVariableValues	= new ArrayList<FormerVariableValue>();

	/**
	 * Remembers former variable value
	 */
	public void rememberFormerValue() {
		val formerValue = new FormerVariableValue(getTimestamp(), getValue(),
				isDescribesMediaUpload());
		formerVariableValues.add(formerValue);
	}
}
