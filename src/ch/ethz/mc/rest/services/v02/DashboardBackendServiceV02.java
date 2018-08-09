package ch.ethz.mc.rest.services.v02;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
	@Path("/validateLogin")
	@Produces("application/json")
	public boolean validateLogin(@HeaderParam("user") final String user,
			@HeaderParam("password") final String password,
			@HeaderParam("interventionPattern") final String interventionPattern,
			@HeaderParam("group") final String group) {
		log.debug(
				"Validating login for user '{}', intervention pattern '{}' and group '{}'",
				user, interventionPattern, group);
		try {
			checkExternalBackendUserInterventionAccess(user, password, group,
					interventionPattern);
		} catch (final Exception e) {
			throw e;
		}

		log.debug("Validation successful");

		return true;
	}
}
