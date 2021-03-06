package ch.ethz.mc.ui.components.main_view.interventions.rules;

/* ##LICENSE## */
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.ui.components.basics.AbstractMonitoringRulesEditComponentWithController;

/**
 * Implements the controller for the monitoring rules edit component for
 * {@link MonitoringRule}s
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringReplyRulesEditComponentWithController
		extends AbstractMonitoringRulesEditComponentWithController {

	public MonitoringReplyRulesEditComponentWithController() {
		super();

		log.debug(
				"Init monitoring rules edit component with controller for monitoring reply rules");
	}
}
