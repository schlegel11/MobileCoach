package ch.ethz.mc.ui;

/* ##LICENSE## */
import java.io.Serializable;

import org.bson.types.ObjectId;

import com.vaadin.server.WrappedSession;

import ch.ethz.mc.conf.ImplementationConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

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
