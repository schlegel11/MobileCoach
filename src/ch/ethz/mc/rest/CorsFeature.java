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
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.interceptors.CorsFilter;

import ch.ethz.mc.conf.ImplementationConstants;

/**
 * Service application for REST interface
 *
 * @author Andreas Filler
 */

@Provider
public class CorsFeature implements Feature {

	@Override
	public boolean configure(final FeatureContext context) {
		final CorsFilter corsFilter = new CorsFilter();
		corsFilter.getAllowedOrigins().add("*");
		corsFilter.setAllowedMethods("GET, POST, HEAD, OPTIONS, PUT, DELETE");
		corsFilter.setAllowedHeaders(
				"Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,Accept-Encoding,Accept-Language,Access-Control-Request-Method,Cache-Control,Connection,Host,Referer,User-Agent"
						+ ImplementationConstants.REST_API_ADDITIONAL_ALLOWED_HEADERS);
		context.register(corsFilter);
		return true;
	}
}