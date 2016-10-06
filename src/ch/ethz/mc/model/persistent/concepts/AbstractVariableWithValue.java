package ch.ethz.mc.model.persistent.concepts;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab at the Health-IS Lab
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.Participant;
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
