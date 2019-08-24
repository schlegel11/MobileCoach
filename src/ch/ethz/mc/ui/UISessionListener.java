package ch.ethz.mc.ui;

/* ##LICENSE## */
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.ImplementationConstants;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Listener to care for session management
 *
 * @author Andreas Filler
 */
@Log4j2
public class UISessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(final HttpSessionEvent se) {
		val session = se.getSession();
		log.debug("UI Session {} has been created", session.getId());

		session.setAttribute(
				ImplementationConstants.UI_SESSION_ATTRIBUTE_DETECTOR, true);
	}

	@Override
	public void sessionDestroyed(final HttpSessionEvent se) {
		val session = se.getSession();
		log.debug("UI Session {} has been destroyed", session.getId());

		// Check for VAADIN UI session
		if (session.getAttribute(
				ImplementationConstants.UI_SESSION_ATTRIBUTE_DETECTOR) != null
				&& (boolean) session.getAttribute(
						ImplementationConstants.UI_SESSION_ATTRIBUTE_DETECTOR) == true) {

			val sessionId = session.getId();
			log.debug("UI Session {} destroyed", sessionId);

			MC.getInstance().getLockingService()
					.releaseAllLocksOfSession(sessionId);

			log.debug("UI Session locks of {} removed", sessionId);
		}
	}
}