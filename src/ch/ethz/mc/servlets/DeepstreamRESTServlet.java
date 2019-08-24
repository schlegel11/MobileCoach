package ch.ethz.mc.servlets;

/* ##LICENSE## */
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
import org.apache.http.protocol.HTTP;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.DeepstreamConstants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.services.RESTManagerService;
import io.netty.handler.codec.http.HttpConstants;
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
	
	private JsonObject httpAuthTokenToJson(final String token) {

		String[] splitedToken = token.split(";");
		val authData = new JsonObject();

		if (splitedToken.length != 4) {
			log.warn(
					"Wrong HTTP token length. Expected 4 properties separated by semicolon, found {}",
					splitedToken.length);
			return authData;
		}
		authData.addProperty(DeepstreamConstants.REST_FIELD_CLIENT_VERSION, splitedToken[0]);
		authData.addProperty(DeepstreamConstants.REST_FIELD_ROLE, splitedToken[1]);
		authData.addProperty(DeepstreamConstants.REST_FIELD_SYSTEM_ID, splitedToken[2]);
		authData.addProperty(DeepstreamConstants.REST_FIELD_TOKEN, splitedToken[3]);

		return authData;
	}
	
	private boolean isWebSocketConnection(final JsonObject jsonObject) {

		val connectionData = (JsonObject) jsonObject
				.get(DeepstreamConstants.DS_FIELD_CONNECTION_DATA);
		val headers = (JsonObject) connectionData
				.get(DeepstreamConstants.DS_FIELD_HEADERS);
		
		return headers.has(HttpHeaders.UPGRADE.toLowerCase())
				&& headers.get(HttpHeaders.UPGRADE.toLowerCase()).getAsString()
						.equals(DeepstreamConstants.DS_FIELD_WEBSOCKET);
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
			
			JsonObject authData = (JsonObject) jsonObjectPayload
					.get(DeepstreamConstants.DS_FIELD_AUTH_DATA);
			
			// Check for HTTP login.
			if (!isWebSocketConnection(jsonObjectPayload)) {
				// Create from HTTP token a JSON document.
				authData = httpAuthTokenToJson(
						authData.get(DeepstreamConstants.REST_FIELD_TOKEN)
								.getAsString());
			}

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
			
			String interventionPassword = null;
			if (authData.has(DeepstreamConstants.REST_FIELD_INTERVENTION_PASSWORD)) {
				interventionPassword = authData.get(DeepstreamConstants.REST_FIELD_INTERVENTION_PASSWORD).getAsString();
			}
			
			// System fields
			String systemId = null;
			if(authData.has(DeepstreamConstants.REST_FIELD_SYSTEM_ID)) {
				systemId = authData
						.get(DeepstreamConstants.REST_FIELD_SYSTEM_ID)
						.getAsString();
			}
			String token = null;
			if(authData.has(DeepstreamConstants.REST_FIELD_TOKEN)) {
				token = authData
						.get(DeepstreamConstants.REST_FIELD_TOKEN)
						.getAsString();
			}

			if (systemId != null) {
				// Check access
				log.debug("Checking deepstream access for external system {}", systemId);
				val accessGranted = restManagerService.checkExternalSystemAccess(clientVersion, role, systemId,
						token);

				if (!accessGranted) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
					return;
				}

				// Send response
				val responseServerData = new JsonObject();
				responseServerData.addProperty(DeepstreamConstants.REST_FIELD_SYSTEM_ID, systemId);
				responseServerData.addProperty(DeepstreamConstants.REST_FIELD_ROLE, role);

				val responseClientData = new JsonObject();
				responseClientData.addProperty(DeepstreamConstants.REST_FIELD_SYSTEM_ID, systemId);
				
				val responseData = new JsonObject();
				responseData.addProperty(DeepstreamConstants.DS_FIELD_USERNAME, systemId + " " + role);
				responseData.add(DeepstreamConstants.DS_FIELD_CLIENT_DATA, responseClientData);
				responseData.add(DeepstreamConstants.DS_FIELD_SERVER_DATA, responseServerData);

				val responseAsBytes = gson.toJson(responseData).getBytes(Charsets.UTF_8);

				response.setContentType(MediaType.APPLICATION_JSON);
				response.setContentLength(responseAsBytes.length);

				response.getOutputStream().write(responseAsBytes);
				

			} else {

				if (user == null) {
					// Try to register
					val externalRegistration = restManagerService.createDeepstreamUser(nickname, relatedParticipant,
							interventionPattern, interventionPassword, role);

					if (externalRegistration == null) {
						log.warn("Could not create participant/supervisor for deepstream access");

						response.sendError(HttpServletResponse.SC_FORBIDDEN);
						return;
					}

					user = externalRegistration.getExternalId();

					// Send response
					val responseServerData = new JsonObject();
					responseServerData.addProperty(DeepstreamConstants.REST_FIELD_USER, user);
					responseServerData.addProperty(DeepstreamConstants.REST_FIELD_ROLE, role);

					val responseClientData = new JsonObject();
					responseClientData.addProperty(DeepstreamConstants.REST_FIELD_USER,
							externalRegistration.getExternalId());
					responseClientData.addProperty(DeepstreamConstants.REST_FIELD_SECRET,
							externalRegistration.getSecret());

					val responseData = new JsonObject();
					responseData.addProperty(DeepstreamConstants.DS_FIELD_USERNAME, user + " " + role);
					responseData.add(DeepstreamConstants.DS_FIELD_CLIENT_DATA, responseClientData);
					responseData.add(DeepstreamConstants.DS_FIELD_SERVER_DATA, responseServerData);

					val responseAsBytes = gson.toJson(responseData).getBytes(Charsets.UTF_8);

					response.setContentType(MediaType.APPLICATION_JSON);
					response.setContentLength(responseAsBytes.length);

					response.getOutputStream().write(responseAsBytes);
				} else {
					// Check access
					log.debug("Checking deepstream access for {} with role {} and password {}", user, role,
							interventionPassword);
					val accessGranted = restManagerService.checkDeepstreamAccess(clientVersion, user, secret, role,
							interventionPassword);

					if (!accessGranted) {
						response.sendError(HttpServletResponse.SC_FORBIDDEN);
						return;
					}

					// Send response
					val responseServerData = new JsonObject();
					responseServerData.addProperty(DeepstreamConstants.REST_FIELD_USER, user);
					responseServerData.addProperty(DeepstreamConstants.REST_FIELD_ROLE, role);

					val responseData = new JsonObject();
					responseData.addProperty(DeepstreamConstants.DS_FIELD_USERNAME, user + " " + role);
					responseData.add(DeepstreamConstants.DS_FIELD_SERVER_DATA, responseServerData);

					val responseAsBytes = gson.toJson(responseData).getBytes(Charsets.UTF_8);

					response.setContentType(MediaType.APPLICATION_JSON);
					response.setContentLength(responseAsBytes.length);

					response.getOutputStream().write(responseAsBytes);
				}
			}
		} catch (final Exception e) {
			log.warn("Could not register or authorize for deepstream access: ",
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
