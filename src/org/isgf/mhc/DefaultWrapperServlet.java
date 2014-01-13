package org.isgf.mhc;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to map all files in the "static"-folder to the /static/ URL path
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Default", value = "/static/*", asyncSupported = true, loadOnStartup = 1)
public class DefaultWrapperServlet extends HttpServlet {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(final HttpServletRequest req,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		final RequestDispatcher rd = this.getServletContext()
				.getNamedDispatcher("default");

		final HttpServletRequest wrapped = new HttpServletRequestWrapper(req) {
			@Override
			public String getServletPath() {
				return "static";
			}
		};

		rd.forward(wrapped, resp);
	}
}
