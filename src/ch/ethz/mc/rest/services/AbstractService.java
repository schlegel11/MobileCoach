package ch.ethz.mc.rest.services;

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
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.services.types.GeneralSessionAttributeTypes;

/**
 * Abstract class for all REST services
 *
 * @author Andreas Filler
 */
@Log4j2
public abstract class AbstractService {

	@Getter
	RESTManagerService	restManagerService;

	public AbstractService(final RESTManagerService restManagerService) {
		this.restManagerService = restManagerService;
	}

	/**
	 * Checks if the given session is valid and if the token fits to the given
	 * session. Then return appropriate participant id
	 *
	 * @param token
	 * @param session
	 */
	protected ObjectId checkAccessAndReturnParticipantId(final String token,
			final HttpSession session) {
		log.debug("Checking if token {} fits to session {}", token,
				session.getId());
		if (token == null) {
			log.debug("REST access denied: Given token is null");
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("Access token missing").build());
		}
		if (session.getAttribute(GeneralSessionAttributeTypes.VALIDATOR
				.toString()) == null
				|| (boolean) session
				.getAttribute(GeneralSessionAttributeTypes.VALIDATOR
						.toString()) == false) {
			log.debug("REST access denied: Session timed out or is no survey/feedback");
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("Session timed out").build());
		}
		if (session.getAttribute(GeneralSessionAttributeTypes.TOKEN.toString()) == null
				|| !token.equals(session
						.getAttribute(GeneralSessionAttributeTypes.TOKEN
								.toString()))) {
			log.debug("REST access denied: Given token does not match token in session");
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("Wrong access token").build());
		}

		log.debug("Given token matches token in session");

		ObjectId participantId;
		try {
			participantId = (ObjectId) session
					.getAttribute(GeneralSessionAttributeTypes.CURRENT_PARTICIPANT
							.toString());
		} catch (final Exception e) {
			participantId = null;
		}

		if (participantId != null) {
			log.debug("Partipant {} fits to token {}", participantId, token);
			return participantId;
		} else {
			throw new WebApplicationException(
					Response.notAcceptable(null)
					.entity("The current session is not yet bound to a participant")
					.build());
		}
	}
}
