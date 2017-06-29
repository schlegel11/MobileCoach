package ch.ethz.mc.ui;

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
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.MC;
import ch.ethz.mc.conf.ImplementationConstants;

/**
 * Listener to care for a participants session to manage REST access
 *
 * @author Andreas Filler
 */
@Log4j2
public class UISessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(final HttpSessionEvent se) {
		val session = se.getSession();
		log.debug("Session {} has been created", session.getId());
	}

	@Override
	public void sessionDestroyed(final HttpSessionEvent se) {
		val session = se.getSession();
		log.debug("Session {} has been destroyed", session.getId());

		// Check for VAADIN UI session
		if (session
				.getAttribute(ImplementationConstants.UI_SESSION_ATTRIBUTE_DETECTOR) != null
				&& (boolean) session
						.getAttribute(ImplementationConstants.UI_SESSION_ATTRIBUTE_DETECTOR) == true) {
			log.debug("Admin UI Session {} destroyed", session.getId());

			MC.getInstance().getLockingService()
					.releaseAllLocksOfSession(session.getId());
		}
	}
}