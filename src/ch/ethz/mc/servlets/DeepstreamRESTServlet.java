package ch.ethz.mc.servlets;

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
import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.DeepstreamConstants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.services.RESTManagerService;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Servlet to enable registration and authorization for the deepstream server
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Deepstream Registration and Authorization", urlPatterns = "/"
		+ ImplementationConstants.DEEPSTREAM_SERVLET_PATH
		+ "/*", asyncSupported = true, loadOnStartup = 1)
@Log4j2
public class DeepstreamRESTServlet extends HttpServlet {
	private RESTManagerService	restManagerService;

	private Gson				gson;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	@Override
	public void init(final ServletConfig servletConfig)
			throws ServletException {
		super.init(servletConfig);
		// Only start servlet if context is ready
		if (!MC.getInstance().isReady()) {
			log.error("Servlet {} can't be started. Context is not ready!",
					this.getClass());
			throw new ServletException("Context is not ready!");
		}

		log.info("Initializing servlet...");

		restManagerService = MC.getInstance().getRestManagerService();

		gson = new Gson();

		restManagerService.informDeepstreamAboutStartup();

		log.info("Servlet initialized.");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request,
			final HttpServletResponse response)
			throws ServletException, IOException {
		log.debug("Deepstream servlet call");

		checkDeepstreamAvailability();

		request.setCharacterEncoding("UTF-8");

		val stringPayload = IOUtils.toString(request.getReader());

		try {
			final JsonElement jsonElement = gson.fromJson(stringPayload,
					JsonElement.class);
			final JsonObject jsonObjectPayload = jsonElement.getAsJsonObject();

			val authData = (JsonObject) jsonObjectPayload
					.get(DeepstreamConstants.DS_FIELD_AUTH_DATA);

			val clientVersion = authData
					.get(DeepstreamConstants.REST_FIELD_CLIENT_VERSION)
					.getAsInt();
			String user = null;
			if (authData.has(DeepstreamConstants.REST_FIELD_USER)) {
				user = authData.get(DeepstreamConstants.REST_FIELD_USER)
						.getAsString();
			}
			String secret = null;
			if (authData.has(DeepstreamConstants.REST_FIELD_SECRET)) {
				secret = authData.get(DeepstreamConstants.REST_FIELD_SECRET)
						.getAsString();
			}
			val role = authData.get(DeepstreamConstants.REST_FIELD_ROLE)
					.getAsString();
			String nickname = null;
			if (authData.has(DeepstreamConstants.REST_FIELD_NICKNAME)) {
				nickname = authData.get(DeepstreamConstants.REST_FIELD_NICKNAME)
						.getAsString();
			}
			String relatedParticipant = null;
			if (authData.has(DeepstreamConstants.REST_FIELD_PARTICIPANT)) {
				relatedParticipant = authData
						.get(DeepstreamConstants.REST_FIELD_PARTICIPANT)
						.getAsString();
			}
			String interventionPattern = null;
			if (authData
					.has(DeepstreamConstants.REST_FIELD_INTERVENTION_PATTERN)) {
				interventionPattern = authData
						.get(DeepstreamConstants.REST_FIELD_INTERVENTION_PATTERN)
						.getAsString();
			}
			val interventionPassword = authData
					.get(DeepstreamConstants.REST_FIELD_INTERVENTION_PASSWORD)
					.getAsString();

			if (user == null) {
				// Try to register
				val externalRegistration = restManagerService
						.createDeepstreamUser(nickname, relatedParticipant,
								interventionPattern, interventionPassword,
								role);

				if (externalRegistration == null) {
					log.warn(
							"Could not create participant/supervisor for deepstream access");

					response.sendError(HttpServletResponse.SC_FORBIDDEN);
					return;
				}

				user = externalRegistration.getExternalId();

				// Send response
				val responseServerData = new JsonObject();
				responseServerData
						.addProperty(DeepstreamConstants.REST_FIELD_USER, user);
				responseServerData
						.addProperty(DeepstreamConstants.REST_FIELD_ROLE, role);

				val responseClientData = new JsonObject();
				responseClientData.addProperty(
						DeepstreamConstants.REST_FIELD_USER,
						externalRegistration.getExternalId());
				responseClientData.addProperty(
						DeepstreamConstants.REST_FIELD_SECRET,
						externalRegistration.getSecret());

				val responseData = new JsonObject();
				responseData.addProperty(DeepstreamConstants.DS_FIELD_USERNAME,
						user + " " + role);
				responseData.add(DeepstreamConstants.DS_FIELD_CLIENT_DATA,
						responseClientData);
				responseData.add(DeepstreamConstants.DS_FIELD_SERVER_DATA,
						responseServerData);

				val responseAsBytes = gson.toJson(responseData)
						.getBytes(Charsets.UTF_8);

				response.setContentType(MediaType.APPLICATION_JSON);
				response.setContentLength(responseAsBytes.length);

				response.getOutputStream().write(responseAsBytes);
			} else {
				// Check access
				log.debug(
						"Checking deepstream access for {} with role {} and password {}",
						user, role, interventionPassword);
				val accessGranted = restManagerService.checkDeepstreamAccess(
						clientVersion, user, secret, role,
						interventionPassword);

				if (!accessGranted) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
					return;
				}

				// Send response
				val responseServerData = new JsonObject();
				responseServerData
						.addProperty(DeepstreamConstants.REST_FIELD_USER, user);
				responseServerData
						.addProperty(DeepstreamConstants.REST_FIELD_ROLE, role);

				val responseData = new JsonObject();
				responseData.addProperty(DeepstreamConstants.DS_FIELD_USERNAME,
						user + " " + role);
				responseData.add(DeepstreamConstants.DS_FIELD_SERVER_DATA,
						responseServerData);

				val responseAsBytes = gson.toJson(responseData)
						.getBytes(Charsets.UTF_8);

				response.setContentType(MediaType.APPLICATION_JSON);
				response.setContentLength(responseAsBytes.length);

				response.getOutputStream().write(responseAsBytes);
			}
		} catch (final Exception e) {
			log.warn(
					"Could not register or authorize for deepstream access: ",
					e.getMessage());

			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
	}

	/**
	 * Checks deepstream availability and throws exception if it's not available
	 */
	private void checkDeepstreamAvailability() {
		if (!Constants.isDeepstreamActive()) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Deepstream is not active on this server").build());
		}
	}
}
