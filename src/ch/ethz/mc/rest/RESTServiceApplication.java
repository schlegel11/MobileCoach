package ch.ethz.mc.rest;

/* ##LICENSE## */
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.types.SMSServiceType;
import ch.ethz.mc.rest.services.v01.CreditsServiceV01;
import ch.ethz.mc.rest.services.v01.ImageUploadServiceV01;
import ch.ethz.mc.rest.services.v01.VariableAccessServiceV01;
import ch.ethz.mc.rest.services.v01.VotingServiceV01;
import ch.ethz.mc.rest.services.v02.CreditsServiceV02;
import ch.ethz.mc.rest.services.v02.DashboardBackendServiceV02;
import ch.ethz.mc.rest.services.v02.MediaUploadServiceV02;
import ch.ethz.mc.rest.services.v02.TWILIOMessageRetrievalServiceV02;
import ch.ethz.mc.rest.services.v02.VariableAccessServiceV02;
import ch.ethz.mc.rest.services.v02.VotingServiceV02;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Service application for REST interface
 *
 * @author Andreas Filler
 */
@ApplicationPath("/" + ImplementationConstants.REST_API_PATH)
@Log4j2
public class RESTServiceApplication extends Application {

	private final Set<Object> services;

	public RESTServiceApplication() {
		log.info("Starting REST application...");
		services = new HashSet<Object>();

		val restManagerService = MC.getInstance().getRestManagerService();

		// v01 Services
		services.add(new CreditsServiceV01(restManagerService));
		services.add(new ImageUploadServiceV01(restManagerService));
		services.add(new VariableAccessServiceV01(restManagerService));
		services.add(new VotingServiceV01(restManagerService));

		// v02 Services
		services.add(new CreditsServiceV02(restManagerService));
		services.add(new DashboardBackendServiceV02(restManagerService));
		services.add(new MediaUploadServiceV02(restManagerService));
		services.add(new VariableAccessServiceV02(restManagerService));
		services.add(new VotingServiceV02(restManagerService));

		if (Constants.getSmsServiceType() == SMSServiceType.TWILIO) {
			services.add(
					new TWILIOMessageRetrievalServiceV02(restManagerService));
		}

		log.info("Started.");
	}

	@Override
	public Set<Object> getSingletons() {
		return services;
	}
}