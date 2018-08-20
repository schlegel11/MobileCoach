package ch.ethz.mc.ui;

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
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.ImplementationConstants;

import com.vaadin.server.WrappedSession;

/**
 * Contains information about the user currently using the
 * {@link AdminNavigatorUI}
 *
 * @author Andreas Filler
 */
@Log4j2
public class UISession implements Serializable {
	private static final long	serialVersionUID			= 1L;

	private boolean				isLoggedIn					= false;

	@Getter
	@Setter
	private boolean				isAdmin						= false;

	@Getter
	@Setter
	private ObjectId			currentBackendUserId		= null;

	@Getter
	@Setter
	private String				currentBackendUserUsername	= null;

	@Getter
	@Setter
	private String				baseURL						= null;

	@Getter
	private String				sessionId					= null;

	private WrappedSession		session						= null;

	public UISession(final WrappedSession session) {
		sessionId = session.getId();
		this.session = session;

		session.setAttribute(
				ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE_EXPECTED,
				false);
	}

	public ObjectId getCurrentBackendUserParticipantId() {
		if (session.getAttribute(
				ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE) != null) {
			try {
				val participant = (ObjectId) session.getAttribute(
						ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE);

				return participant;
			} catch (final Exception e) {
				log.warn(
						"Error when getting author participant id from session: {}",
						e.getMessage());
			}
		}

		return null;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(final boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;

		if (isLoggedIn == true) {
			session.setAttribute(
					ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE_EXPECTED,
					true);
			session.setAttribute(
					ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE_DESCRIPTION,
					currentBackendUserUsername);
		} else {
			session.setAttribute(
					ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE_EXPECTED,
					false);
			session.setAttribute(
					ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE_DESCRIPTION,
					null);
		}
	}

	public void resetParticipantExpection() {
		if (isLoggedIn) {
			session.setAttribute(
					ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE_EXPECTED,
					true);
			session.setAttribute(
					ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE,
					null);
		}
	}

	public void clearWrappedSession() {
		session.setAttribute(
				ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE_EXPECTED,
				false);
		session.setAttribute(
				ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE_DESCRIPTION,
				null);
		session.setAttribute(
				ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE, null);
	}
}
