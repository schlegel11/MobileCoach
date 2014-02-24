package org.isgf.mhc.servlets;

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

import org.isgf.mhc.MHC;

/**
 * Servlet to map all files in the "static"-folder to the /static/ URL path
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Default", value = "/static/*", asyncSupported = true, loadOnStartup = 1)
@Log4j2
public class DefaultWrapperServlet extends HttpServlet {
	/**
	 * @see Servlet#init(ServletConfig)
	 */
	@Override
	public void init(final ServletConfig servletConfig) throws ServletException {
		// Only start servlet if context is ready
		if (!MHC.getInstance().isReady()) {
			log.error("Servlet {} can't be started. Context is not ready!",
					this.getClass());
			throw new ServletException("Context is not ready!");
		}

		log.info("Initializing servlet...");

		log.info("Servlet initialized.");
		super.init(servletConfig);
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
