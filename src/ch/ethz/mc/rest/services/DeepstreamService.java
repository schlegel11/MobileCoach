package ch.ethz.mc.rest.services;

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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.services.RESTManagerService;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Service to read voting values and to vote/unvote using REST
 *
 * @author Andreas Filler
 */
@Path("/v01/deepstream")
@Log4j2
public class DeepstreamService extends AbstractService {
	private final Gson	gson;

	public DeepstreamService(final RESTManagerService restManagerService) {
		super(restManagerService);

		gson = new Gson();

		restManagerService.informDeepstreamAboutStartup();
	}

	@Data
	@AllArgsConstructor
	private class connectionData {
		private AuthData	authData;
		private AuthData	connectionData;
	}

	@Data
	@AllArgsConstructor
	private class AuthData {
		private String	user;
		private String	secret;
	}

	@POST
	@Path("/authorize")
	@Produces("application/json")
	public Response authorize(final String stringPayload) {
		checkDeepstreamAvailability();

		try {
			final JsonElement jsonElement = gson.fromJson(stringPayload,
					JsonElement.class);
			final JsonObject jsonObjectPayload = jsonElement.getAsJsonObject();

			val authData = (JsonObject) jsonObjectPayload.get("authData");

			val user = authData.get("user").getAsString();
			val secret = authData.get("secret").getAsString();
			val role = authData.get("role").getAsString();
			val interventionPassword = authData.get("intervention-password")
					.getAsString();

			// Check access
			log.debug(
					"Checking deepstream access for {} with role {} and password {}",
					user, role, interventionPassword);
			val accessGranted = restManagerService
					.checkDeepstreamAccessAndRetrieveUserId(user, secret, role,
							interventionPassword);

			if (!accessGranted) {
				return Response.status(Status.FORBIDDEN).build();
			}

			// Send response
			val responseServerData = new JsonObject();
			responseServerData.addProperty("user", user);
			responseServerData.addProperty("role", role);

			val responseData = new JsonObject();
			responseData.add("serverData", responseServerData);

			return Response.status(Status.OK).entity(gson.toJson(responseData))
					.build();
		} catch (final Exception e) {
			throw new WebApplicationException(
					Response.status(Status.FORBIDDEN)
							.entity("Could not authorize server/participant/supervisor for deepstream access: "
									+ e.getMessage()).build());
		}
	}

	@POST
	@Path("/register")
	@Produces("application/json")
	public Response register(final String stringPayload) {
		checkDeepstreamAvailability();

		try {
			final JsonElement jsonElement = gson.fromJson(stringPayload,
					JsonElement.class);
			final JsonObject jsonPayload = jsonElement.getAsJsonObject();

			String nickname = null;
			if (jsonPayload.has("nickname")) {
				nickname = jsonPayload.get("nickname").getAsString();
			}
			String relatedParticipant = null;
			if (jsonPayload.has("participant")) {
				relatedParticipant = jsonPayload.get("participant")
						.getAsString();
			}
			val interventionPattern = jsonPayload.get("intervention-pattern")
					.getAsString();
			val interventionPassword = jsonPayload.get("intervention-password")
					.getAsString();
			val requestedRole = jsonPayload.get("role").getAsString();

			// Create participant or supervisor
			val externalRegistration = restManagerService.createDeepstreamUser(
					nickname, relatedParticipant, interventionPattern,
					interventionPassword, requestedRole);

			if (externalRegistration == null) {
				throw new WebApplicationException(
						Response.status(Status.FORBIDDEN)
								.entity("Could not create participant/supervisor for deepstream access")
								.build());
			}

			// Send response
			val responseData = new JsonObject();
			responseData.addProperty("user",
					externalRegistration.getExternalId());
			responseData
					.addProperty("secret", externalRegistration.getSecret());

			return Response.status(Status.OK).entity(gson.toJson(responseData))
					.build();
		} catch (final Exception e) {
			throw new WebApplicationException(
					Response.status(Status.FORBIDDEN)
							.entity("Could not create participant/supervisor for deepstream access: "
									+ e.getMessage()).build());
		}
	}
}
