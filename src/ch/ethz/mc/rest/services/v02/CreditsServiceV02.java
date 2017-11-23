package ch.ethz.mc.rest.services.v02;

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
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.rest.OK;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.tools.StringValidator;

/**
 * Service to collect credits using REST
 *
 * @author Andreas Filler
 */
@Path("/v02/credits")
@Log4j2
public class CreditsServiceV02 extends AbstractServiceV02 {

	public CreditsServiceV02(final RESTManagerService restManagerService) {
		super(restManagerService);
	}

	/*
	 * Write functions
	 */
	@GET
	@Path("/storeCredit/{variable}/{creditName}")
	@Produces("application/json")
	public Response storeCredit(@HeaderParam("user") final String user,
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@PathParam("creditName") final String creditName) {
		log.debug("Token {}: Storing credit for {} on {}", token, creditName,
				variable);
		ObjectId participantId;
		try {
			participantId = checkExternalParticipantAccessAndReturnParticipantId(
					user, token);
		} catch (final Exception e) {
			throw e;
		}

		try {
			if (!StringValidator
					.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
							+ variable.trim())) {
				throw new Exception("The variable name is not valid");
			}

			restManagerService.writeCredit(participantId, variable.trim(),
					creditName);
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not store credit: " + e.getMessage())
					.build());
		}

		return Response.ok(new OK()).build();
	}
}
