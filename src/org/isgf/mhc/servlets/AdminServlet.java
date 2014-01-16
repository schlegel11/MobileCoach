package org.isgf.mhc.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.ui.AdminUI;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

/**
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Admin UI", value = { "/admin/*", "/VAADIN/*" }, initParams = { @WebInitParam(name = "pushmode", value = "automatic") }, asyncSupported = true, loadOnStartup = 1)
@VaadinServletConfiguration(productionMode = Constants.PRODUCTION_MODE, ui = AdminUI.class)
@Log4j2
public class AdminServlet extends VaadinServlet {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.server.VaadinServlet#init(javax.servlet.ServletConfig)
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
		super.init(servletConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.server.VaadinServlet#servletInitialized()
	 */
	@Override
	protected void servletInitialized() throws ServletException {
		super.servletInitialized();
		log.info("Servlet initialized.");
	}
}