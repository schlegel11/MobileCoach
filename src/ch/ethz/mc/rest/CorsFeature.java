package ch.ethz.mc.rest;

/* ##LICENSE## */
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
				"Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,Accept-Encoding,Accept-Language,Access-Control-Request-Method,Cache-Control,Connection,Host,Referer,User-Agent,"
						+ ImplementationConstants.REST_API_ADDITIONAL_ALLOWED_HEADERS);
		context.register(corsFilter);
		return true;
	}
}