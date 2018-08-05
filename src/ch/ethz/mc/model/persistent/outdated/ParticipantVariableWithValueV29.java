package ch.ethz.mc.model.persistent.outdated;

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
	private static final long serialVersionUID = 8029992540587011783L;

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
