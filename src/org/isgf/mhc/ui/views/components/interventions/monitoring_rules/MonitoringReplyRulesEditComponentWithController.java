package org.isgf.mhc.ui.views.components.interventions.monitoring_rules;

import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.model.server.MonitoringRule;
import org.isgf.mhc.ui.views.components.basics.AbstractMonitoringRulesEditComponentWithController;

/**
 * Implements the controller for the monitoring rules edit component for
 * {@link MonitoringRule}s
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringReplyRulesEditComponentWithController extends
		AbstractMonitoringRulesEditComponentWithController {

	public MonitoringReplyRulesEditComponentWithController() {
		super();

		log.debug("Init monitoring rules edit component with controller for monitoring reply rules");
	}
}
