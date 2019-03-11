package ch.ethz.mc.rest.services.v02;

/* ##LICENSE## */
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.persistent.BackendUser;
import ch.ethz.mc.model.persistent.BackendUserInterventionAccess;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.services.types.GeneralSessionAttributeTypes;
import ch.ethz.mc.services.types.GeneralSessionAttributeValidatorTypes;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Abstract class for all REST services
 *
 * @author Andreas Filler
 */
@Log4j2
public abstract class AbstractServiceV02 {

	@Getter
	RESTManagerService restManagerService;

	public AbstractServiceV02(final RESTManagerService restManagerService) {
		this.restManagerService = restManagerService;
	}

	/**
	 * Checks if the given session is valid for dashboard access and if the
	 * token fits to the given session; returns appropriate intervention id
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
	 * Checks if the given token is valid for a specific external participant
	 * and returns appropriate participant id afterwards
	 *
	 * @param externalParticipant
	 * @param token
	 * @return
	 */
	protected ObjectId checkExternalParticipantAccessAndReturnParticipantId(
			final String externalParticipantId, final String token) {
		log.debug("Checking if token {} fits to external participant {}", token,
				externalParticipantId);
		if (StringUtils.isBlank(externalParticipantId)
				|| StringUtils.isBlank(token)) {
			log.debug(
					"REST access denied: Given external participant id or token is null");
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("External participant id or access token missing")
					.build());
		}

		val participantId = restManagerService
				.checkExternalParticipantAccessAndReturnParticipantId(
						externalParticipantId, token);
		if (participantId == null) {
			log.debug(
					"REST access denied: External participant or token not matching or unknown");
			throw new WebApplicationException(
					Response.status(Status.UNAUTHORIZED)
							.entity("Wrong access token").build());
		} else {
			return participantId;
		}
	}

	/**
	 * Checks the access rights of the given {@link BackendUser} for the given
	 * group and intervention and returns {@link BackendUserInterventionAccess},
	 * otherwise null
	 * 
	 * @param username
	 * @param password
	 * @param group
	 * @param interventionPattern
	 * @return
	 */
	protected BackendUserInterventionAccess checkExternalBackendUserInterventionAccess(
			final String username, final String password, final String group,
			final String interventionPattern) {
		val backendUserInterventionAccess = restManagerService
				.checkExternalBackendUserInterventionAccess(username, password,
						group, interventionPattern);

		if (backendUserInterventionAccess == null) {
			throw new WebApplicationException(
					Response.status(Status.UNAUTHORIZED)
							.entity("Unauthorized access").build());
		}

		return backendUserInterventionAccess;
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
