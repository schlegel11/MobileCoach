package ch.ethz.mc.ui.views.components.interventions.monitoring_rules;

import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.ui.views.components.basics.AbstractMonitoringRulesEditComponentWithController;

/**
 * Implements the controller for the monitoring rules edit component for
 * {@link MonitoringReplyRule}s
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringRulesEditComponentWithController extends
		AbstractMonitoringRulesEditComponentWithController {

	public MonitoringRulesEditComponentWithController(
			final Intervention intervention) {
		super();
		log.debug("Init monitoring rules edit component with controller for monitoring rules");

		init(intervention);
	}
}
