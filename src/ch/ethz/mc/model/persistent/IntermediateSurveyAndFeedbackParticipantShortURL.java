package ch.ethz.mc.model.persistent;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import lombok.val;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.ModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link ModelObject} to represent an
 * {@link IntermediateSurveyAndFeedbackParticipantShortURL}
 *
 * A {@link IntermediateSurveyAndFeedbackParticipantShortURL} is used to create
 * and
 * verify unique Ids for specific {@link ScreeningSurvey}s or {@link Feedback}
 * of {@link Participant}s to create URLs
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class IntermediateSurveyAndFeedbackParticipantShortURL extends
ModelObject {
	/**
	 * The short id defining this
	 * {@link IntermediateSurveyAndFeedbackParticipantShortURL}
	 */
	@Getter
	@Setter
	private long		shortId;

	@Getter
	@Setter
	private String		secret;

	@Getter
	@Setter
	@NonNull
	private ObjectId	participant;

	@Getter
	@Setter
	private ObjectId	survey;

	@Getter
	@Setter
	private ObjectId	feedback;

	/**
	 * Calculates a URL that can be checked for validity
	 *
	 * @return
	 */
	@JsonIgnore
	public String calculateURL() {
		val shortIdString = String.valueOf(shortId);

		String checksum = null;
		try {
			checksum = calculateChecksum(shortIdString);
		} catch (final Exception e) {
			// Will never happen
		}

		return Constants.getSurveyLinkingBaseURL() + checksum
				+ Long.toString(shortId, 36) + secret;
	}

	/**
	 * Checks secret in given idPart
	 *
	 * @param idPart
	 * @return
	 */
	@JsonIgnore
	public boolean validateSecretInGivenIdPart(final String idPart) {
		if (idPart.length() < 6) {
			return false;
		}

		val extractedSecret = idPart.substring(idPart.length() - 4,
				idPart.length());

		if (secret.equals(extractedSecret)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Validates the given id part and returns the appropriate shortId
	 *
	 * @param idPart
	 * @return
	 */
	@JsonIgnore
	public static long validateURLIdPartAndReturnShortId(final String idPart)
			throws Exception {

		if (idPart.length() < 6) {
			throw new Exception("The following id part is not valid: " + idPart);
		}

		val shortId = String.valueOf(Long.parseLong(
				idPart.substring(1, idPart.length() - 4), 36));
		final String checksum = calculateChecksum(shortId);

		if (checksum.equals(idPart.substring(0, 1))) {
			return Long.parseLong(shortId);
		} else {
			throw new Exception("The following id part is not valid: " + idPart);
		}
	}

	/**
	 * Calculates a checksum for the given short id string
	 *
	 * @param shortIdString
	 * @return
	 * @throws Exception
	 */
	@JsonIgnore
	private static String calculateChecksum(final String shortIdString)
			throws Exception {
		int value = 0;

		for (int i = 0; i < shortIdString.length(); i++) {
			value += Integer.parseInt(shortIdString.substring(i, i + 1));
		}

		final String valueAsString = String.valueOf(value);

		final String checksum = valueAsString
				.substring(valueAsString.length() - 1);

		return checksum;
	}
}
