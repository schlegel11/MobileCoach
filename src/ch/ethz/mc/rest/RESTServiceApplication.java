package ch.ethz.mc.rest;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.rest.services.AppService;
import ch.ethz.mc.rest.services.CreditsService;
import ch.ethz.mc.rest.services.ImageUploadService;
import ch.ethz.mc.rest.services.OneSignalAppService;
import ch.ethz.mc.rest.services.VariableAccessService;
import ch.ethz.mc.rest.services.VotingService;
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

	private final Set<Object>	services;

	public RESTServiceApplication() {
		log.info("Starting REST application...");
		services = new HashSet<Object>();

		final val restManagerService = MC.getInstance().getRestManagerService();
		final val matterMostService = MC.getInstance().getMattermostManagementService();

		// Variable access service
		services.add(new CreditsService(restManagerService));
		services.add(new ImageUploadService(restManagerService));
		services.add(new VariableAccessService(restManagerService));
		services.add(new VotingService(restManagerService));
		services.add(new AppService(restManagerService, matterMostService));
		services.add(new OneSignalAppService(restManagerService, matterMostService));

		log.info("Started.");
	}
	
	@Override
	public Set<Object> getSingletons() {
		return services;
	}
}