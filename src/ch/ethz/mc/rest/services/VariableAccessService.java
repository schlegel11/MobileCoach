package ch.ethz.mc.rest.services;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.model.rest.VariableWithValue;
import ch.ethz.mc.services.RESTManagerService;

/**
 * Service to read/write variables using REST
 *
 * @author Andreas Filler
 */
@Path("/v01/variable")
@Log4j2
public class VariableAccessService {

	RESTManagerService	restManagerService;

	public VariableAccessService(final RESTManagerService restManagerService) {
		this.restManagerService = restManagerService;
	}

	/*
	 * Read functions
	 */
	@GET
	@Path("/read/{variable}")
	@Produces("application/json")
	public VariableWithValue readVariable(
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable) {
		log.debug("Token {}: Read variable call with variable {}", token,
				variable);
		checkAccess(token);

		try {
			return restManagerService.readVariable(variable);
		} catch (final Exception e) {
			throw new WebApplicationException(Response.noContent()
					.entity("Could not retrieve variable").build());
		}
	}

	/*
	 * Write functions
	 */

	/*
	 * Helper methods
	 */

	private void checkAccess(final String token) {
		if (token == null) {
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("Access token missing").build());
		}

		// Check access
		if (!restManagerService.validateToken(token)) {
			throw new WebApplicationException(Response.notAcceptable(null)
					.entity("Access token unknown/timed out").build());
		}
	}
}
