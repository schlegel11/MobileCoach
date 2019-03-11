package ch.ethz.mc.ui.components.main_view.interventions.micro_dialogs;

/* ##LICENSE## */
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.ui.components.basics.AbstractMonitoringRulesEditComponentWithController;
import lombok.extern.log4j.Log4j2;

/**
 * Implements the controller for the monitoring rules edit component for
 * {@link MonitoringRule}s
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MicroDialogRulesEditComponentWithController
		extends AbstractMonitoringRulesEditComponentWithController {

	public MicroDialogRulesEditComponentWithController() {
		super();

		log.debug(
				"Init monitoring rules edit component with controller for micro dialog rules");
	}
}
