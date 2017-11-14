package ch.ethz.mc.rest.services.v01;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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
import javax.ws.rs.core.Response.Status;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.services.types.GeneralSessionAttributeTypes;
import ch.ethz.mc.services.types.GeneralSessionAttributeValidatorTypes;

/**
 * Abstract class for all REST services
 *
 * @author Andreas Filler
 */
@Log4j2
public abstract class AbstractServiceV01 {

	@Getter
	RESTManagerService restManagerService;

	public AbstractServiceV01(final RESTManagerService restManagerService) {
		this.restManagerService = restManagerService;
	}

	/**
	 * Checks if the given session is valid for a specific participant and if
	 * the token fits to the given session; returns appropriate participant id
	 * afterwards
	 *
	 * @param token
	 * @param session
	 * @return
	 */
	protected ObjectId checkParticipantRelatedAccessAndReturnParticipantId(
			final String token, final HttpSession session) {
		log.debug("Checking if token {} fits to session {}", token,
				session.getId());
		if (token == null) {
			log.debug("REST access denied: Given token is null");
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("Access token missing").build());
		}
		if (session
				.getAttribute(GeneralSessionAttributeTypes.VALIDATOR
						.toString()) == null
				|| !session
						.getAttribute(GeneralSessionAttributeTypes.VALIDATOR
								.toString())
						.toString()
						.equals(GeneralSessionAttributeValidatorTypes.PARTICIPANT_RELATED
								.toString())) {
			log.debug(
					"REST access denied: Session timed out or is no survey/feedback session");
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("Session timed out").build());
		}
		if (session.getAttribute(
				GeneralSessionAttributeTypes.TOKEN.toString()) == null
				|| !token.equals(session.getAttribute(
						GeneralSessionAttributeTypes.TOKEN.toString()))) {
			log.debug(
					"REST access denied: Given token does not match token in session");
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("Wrong access token").build());
		}

		log.debug("Given token matches token in session");

		ObjectId participantId;
		try {
			participantId = (ObjectId) session.getAttribute(
					GeneralSessionAttributeTypes.CURRENT_PARTICIPANT
							.toString());
		} catch (final Exception e) {
			participantId = null;
		}

		if (participantId != null) {
			log.debug("Partipant {} fits to token {}", participantId, token);
			return participantId;
		} else {
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("The current session is not yet bound to a participant")
					.build());
		}
	}

	/**
	 * Checks if the given session is valid for dashboard access and if
	 * the token fits to the given session; returns appropriate intervention id
	 * afterwards
	 *
	 * @param token
	 * @param session
	 * @return
	 */
	protected ObjectId checkDashboardAccess(final String token,
			final HttpSession session) {
		log.debug("Checking if token {} fits to session {}", token,
				session.getId());
		if (token == null) {
			log.debug("REST access denied: Given token is null");
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("Access token missing").build());
		}
		if (session
				.getAttribute(GeneralSessionAttributeTypes.VALIDATOR
						.toString()) == null
				|| !session
						.getAttribute(GeneralSessionAttributeTypes.VALIDATOR
								.toString())
						.toString()
						.equals(GeneralSessionAttributeValidatorTypes.DASHBOARD_ACCESS
								.toString())) {
			log.debug(
					"REST access denied: Session timed out or is no dashboard session");
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("Session timed out").build());
		}
		if (session.getAttribute(
				GeneralSessionAttributeTypes.TOKEN.toString()) == null
				|| !token.equals(session.getAttribute(
						GeneralSessionAttributeTypes.TOKEN.toString()))) {
			log.debug(
					"REST access denied: Given token does not match token in session");
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("Wrong access token").build());
		}

		log.debug("Given token matches token in session");

		ObjectId interventionId;
		try {
			interventionId = (ObjectId) session.getAttribute(
					GeneralSessionAttributeTypes.CURRENT_SESSION.toString());
		} catch (final Exception e) {
			interventionId = null;
		}

		if (interventionId != null) {
			log.debug("Intervention {} fits to token {}", interventionId,
					token);
			return interventionId;
		} else {
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("The current session is not bound to an intervention")
					.build());
		}
	}

	/**
	 * Checks deepstream availability and throws exception if it's not available
	 */
	protected void checkDeepstreamAvailability() {
		if (!Constants.isDeepstreamActive()) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Deepstream is not active on this server").build());
		}
	}
}
