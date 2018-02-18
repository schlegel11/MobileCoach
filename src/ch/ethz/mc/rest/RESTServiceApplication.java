package ch.ethz.mc.rest;

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
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.rest.services.v01.CreditsServiceV01;
import ch.ethz.mc.rest.services.v01.ImageUploadServiceV01;
import ch.ethz.mc.rest.services.v01.VariableAccessServiceV01;
import ch.ethz.mc.rest.services.v01.VotingServiceV01;
import ch.ethz.mc.rest.services.v02.CreditsServiceV02;
import ch.ethz.mc.rest.services.v02.ImageUploadServiceV02;
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
		services.add(new ImageUploadServiceV02(restManagerService));
		services.add(new VariableAccessServiceV02(restManagerService));
		services.add(new VotingServiceV02(restManagerService));

		log.info("Started.");
	}

	@Override
	public Set<Object> getSingletons() {
		return services;
	}
}