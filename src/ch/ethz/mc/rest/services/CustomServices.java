package ch.ethz.mc.rest.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.rest.CollectionOfVariables;
import ch.ethz.mc.model.rest.Variable;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.tools.StringValidator;
import ch.ethz.mobilecoach.interventions.AbstractIntervention;
import ch.ethz.mobilecoach.interventions.PersonalityChange;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Serves Intervention-specific services
 *
 * @author Dominik Rüegger
 */
@Path("v01/custom")
@Log4j2
public class CustomServices extends AbstractService {
	
	DatabaseManagerService db;
	VariablesManagerService vars;
	Map<String, AbstractIntervention> interventionMap = new HashMap<>();

	public CustomServices(final RESTManagerService restManagerService, final DatabaseManagerService db, final VariablesManagerService vars) {
		super(restManagerService);
		this.db = db;
		this.vars = vars;
		
		// TODO: these should be added only when MobileCoach is configured to do so, to prevent security problems
		interventionMap.put("PersonalityChange", new PersonalityChange(db, vars));
	}

	
	@GET
	@Path("/{intervention}/dashboard/{variables}")
	@Produces("application/json")
	public Object dashboard(@HeaderParam("Authentication") final String token,
			@PathParam("intervention") final String intervention,
			@PathParam("variables") final String variables,
			@Context final HttpServletRequest request) {
		
		ObjectId participantId = restManagerService.checkTokenAndGetParticipantId(token);
		Object customDashboard = null;
		Map<String, Object> result = new HashMap<>();
		Map<String, String> variableMap = new HashMap<>();
		
		if (interventionMap.containsKey(intervention)){
			customDashboard = interventionMap.get(intervention).getDashboard(participantId);
		}
		
		result.put("custom", customDashboard);
		
		// adapted from VariableAccessService:
		try {
			final val variableNames = variables.split(",");
			for (val variable : variableNames) {
				if (!StringValidator
						.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
								+ variable.trim())) {
					throw new Exception("The variable name is not valid");
				}
				
				Variable v = restManagerService.readVariable(participantId, variable.trim(), false);
				variableMap.put(ImplementationConstants.VARIABLE_PREFIX + v.getVariable(), v.getValue());
			}

			result.put("variables", variableMap);
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
		
		return result;
	}
}
