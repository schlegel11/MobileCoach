package ch.ethz.mc.servlets;

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
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.ui.AdminNavigatorUI;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.SystemMessagesInfo;
import com.vaadin.server.SystemMessagesProvider;
import com.vaadin.server.VaadinServlet;

/**
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Admin UI", urlPatterns = { "/admin/*",
		"/VAADIN/*" }, initParams = {
				@WebInitParam(name = "pushmode", value = "automatic"),
				@WebInitParam(name = "closeIdleSessions", value = "true"),
				@WebInitParam(name = "heartbeatInterval", value = "300") }, asyncSupported = false, loadOnStartup = 1)
@VaadinServletConfiguration(productionMode = Constants.VAADIN_PRODUCTION_MODE, ui = AdminNavigatorUI.class)
@Log4j2
public class AdminServlet extends VaadinServlet
		implements SessionInitListener, SessionDestroyListener {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.server.VaadinServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(final ServletConfig servletConfig)
			throws ServletException {
		// Only start servlet if context is ready
		if (!MC.getInstance().isReady()) {
			log.error("Servlet {} can't be started. Context is not ready!",
					this.getClass());
			throw new ServletException("Context is not ready!");
		}

		log.info("Initializing servlet...");
		super.init(servletConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.server.VaadinServlet#servletInitialized()
	 */
	@Override
	protected void servletInitialized() throws ServletException {
		super.servletInitialized();

		// Session listeners
		getService().addSessionInitListener(this);
		getService().addSessionDestroyListener(this);

		// Customized error messages
		getService().setSystemMessagesProvider(new SystemMessagesProvider() {
			@Override
			public SystemMessages getSystemMessages(
					final SystemMessagesInfo systemMessagesInfo) {
				final CustomizedSystemMessages messages = new CustomizedSystemMessages();

				messages.setSessionExpiredCaption(Messages.getAdminString(
						AdminMessageStrings.SYSTEM_NOTIFICATION__SESSION_EXPIRED_CAPTION));
				messages.setSessionExpiredMessage(Messages.getAdminString(
						AdminMessageStrings.SYSTEM_NOTIFICATION__SESSION_EXPIRED_MESSAGE));

				messages.setInternalErrorCaption(Messages.getAdminString(
						AdminMessageStrings.SYSTEM_NOTIFICATION__INTERNAL_ERROR_CAPTION));
				messages.setInternalErrorMessage(Messages.getAdminString(
						AdminMessageStrings.SYSTEM_NOTIFICATION__INTERNAL_ERROR_MESSAGE));

				messages.setCommunicationErrorCaption(Messages.getAdminString(
						AdminMessageStrings.SYSTEM_NOTIFICATION__COMMUNICATION_ERROR_CAPTION));
				messages.setCommunicationErrorMessage(Messages.getAdminString(
						AdminMessageStrings.SYSTEM_NOTIFICATION__COMMUNICATION_ERROR_MESSAGE));

				messages.setCookiesDisabledCaption(Messages.getAdminString(
						AdminMessageStrings.SYSTEM_NOTIFICATION__COOKIES_DISABLED_CAPTION));
				messages.setCookiesDisabledMessage(Messages.getAdminString(
						AdminMessageStrings.SYSTEM_NOTIFICATION__COOKIES_DISABLED_MESSAGE));

				return messages;
			}
		});

		log.info("Servlet initialized.");
	}

	@Override
	public void sessionInit(final SessionInitEvent event)
			throws ServiceException {
		log.debug("Setting new session timeout");

		event.getSession().getSession().setMaxInactiveInterval(
				ImplementationConstants.UI_SESSION_TIMEOUT_IN_SECONDS);

		event.getSession().getSession().setAttribute(
				ImplementationConstants.UI_SESSION_ATTRIBUTE_DETECTOR, true);
	}

	@Override
	public void sessionDestroy(final SessionDestroyEvent event) {
		try {
			val sessionId = event.getSession().getSession().getId();

			log.debug("Session {} destroyed", sessionId);

			MC.getInstance().getLockingService()
					.releaseAllLocksOfSession(sessionId);
		} catch (final NullPointerException e) {
			// Session was no UI session
		}
	}
}