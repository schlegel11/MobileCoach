package ch.ethz.mc.rest.services.v02;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ch.ethz.mc.model.persistent.BackendUserInterventionAccess;
import ch.ethz.mc.services.RESTManagerService;
import lombok.extern.log4j.Log4j2;

/**
 * Service to provide dashboard backend functionalities using REST
 *
 * @author Andreas Filler
 */
@Path("/v02/dashboard")
@Log4j2
public class DashboardBackendServiceV02 extends AbstractServiceV02 {

	public DashboardBackendServiceV02(
			final RESTManagerService restManagerService) {
		super(restManagerService);
	}

	@GET
	@Path("/example/{group}/{variable}")
	@Produces("application/json")
	public String variableReadExternalGroupArray(
			@HeaderParam("user") final String user,
			@HeaderParam("password") final String password,
			@HeaderParam("interventionPattern") final String interventionPattern,
			@PathParam("group") final String group,
			@PathParam("variable") final String variable) {
		log.debug(
				"Externally read variable array {} of participants from group {}",
				variable, group);
		BackendUserInterventionAccess backendUserInterventionAccess;
		try {
			backendUserInterventionAccess = checkExternalBackendUserInterventionAccess(
					user, password, group, interventionPattern);
		} catch (final Exception e) {
			throw e;
		}

		try {
			return "TEST";
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
	}
}
