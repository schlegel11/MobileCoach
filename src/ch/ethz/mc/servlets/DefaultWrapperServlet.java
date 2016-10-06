package ch.ethz.mc.servlets;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab at the Health-IS Lab
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
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.MC;

/**
 * Servlet to map all files in the "static"-folder to the /static/ URL path
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Default", urlPatterns = "/static/*", asyncSupported = true, loadOnStartup = 1)
@Log4j2
public class DefaultWrapperServlet extends HttpServlet {
	/**
	 * @see Servlet#init(ServletConfig)
	 */
	@Override
	public void init(final ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		// Only start servlet if context is ready
		if (!MC.getInstance().isReady()) {
			log.error("Servlet {} can't be started. Context is not ready!",
					this.getClass());
			throw new ServletException("Context is not ready!");
		}

		log.info("Initializing servlet...");

		log.info("Servlet initialized.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		log.debug("Serving static {}", request.getPathInfo());

		final RequestDispatcher requestDispatcher = getServletContext()
				.getNamedDispatcher("default");

		final HttpServletRequest wrapped = new HttpServletRequestWrapper(
				request) {
			@Override
			public String getServletPath() {
				return "/static";
			}
		};

		requestDispatcher.forward(wrapped, response);
	}
}
