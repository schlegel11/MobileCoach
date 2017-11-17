package ch.ethz.mc.ui.components.main_view.interventions.micro_dialogs;

import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MicroDialogDecisionPoint;
import lombok.val;

/**
 * Extends the monitoring rule edit component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class MicroDialogDecisionPointEditComponentWithController
		extends MicroDialogDecisionPointEditComponent {

	public MicroDialogDecisionPointEditComponentWithController(
			final Intervention intervention,
			final MicroDialogDecisionPoint microDialogDecisionPoint) {
		super();

		// Configure integrated components
		final val microDialogRulesEditComponentWithController = getMicroDialogRulesEditComponentWithController();
		microDialogRulesEditComponentWithController.init(intervention,
				microDialogDecisionPoint.getMicroDialog(),
				microDialogDecisionPoint.getId());
	}
}
