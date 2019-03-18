package ch.ethz.mc.services.internal;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.memory.ExternalServiceRegistration;
import ch.ethz.mc.model.persistent.InterventionExternalService;
import ch.ethz.mc.tools.RuleEvaluator;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Manages all defined external service for a specific intervention.
 *
 * @author Marcel Schlegel
 */
@Log4j2
public class ExternalServicesManagerService {

	@Getter
	private static ExternalServicesManagerService	instance	= null;

	private final DeepstreamCommunicationService	deepstreamCommunicationService;

	private ExternalServicesManagerService(
			final DeepstreamCommunicationService deepstreamCommunicationService) {

		log.info("Starting service...");

		this.deepstreamCommunicationService = deepstreamCommunicationService;

		log.info("Started.");
	}

	public static ExternalServicesManagerService start(
			final DeepstreamCommunicationService deepstreamCommunicationService)
			throws Exception {
		if (instance == null) {
			instance = new ExternalServicesManagerService(
					deepstreamCommunicationService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");


		log.info("Stopped.");
	}

	public ExternalServiceRegistration createExternalService(final ObjectId interventionId, final String serviceName) {

		val serviceRegistration = deepstreamCommunicationService
				.registerExternalService(interventionId, serviceName);

		return serviceRegistration;
	}
	
	public void deleteExternalService(final InterventionExternalService externalService) {
		deepstreamCommunicationService.deleteExternalService(externalService);
	}

}
