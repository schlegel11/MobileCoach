package ch.ethz.mc.ui.components.main_view.interventions.micro_dialogs;

/* ##LICENSE## */
import org.bson.types.ObjectId;

import ch.ethz.mc.model.memory.types.RuleTypes;
import ch.ethz.mc.model.persistent.MicroDialogMessageRule;
import ch.ethz.mc.ui.components.basics.AbstractRuleEditComponentWithController;

/**
 * Extends the micro dialog message rule edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class MicroDialogMessageRuleEditComponentWithController
		extends MicroDialogMessageRuleEditComponent {

	private final AbstractRuleEditComponentWithController ruleEditComponent;

	public MicroDialogMessageRuleEditComponentWithController(
			final MicroDialogMessageRule microDialogMessageRule,
			final ObjectId interventionId) {
		super();

		// Configure integrated components
		ruleEditComponent = getAbstractRuleEditComponentWithController();
		ruleEditComponent.init(interventionId,
				RuleTypes.MICRO_DIALOG_MESSAGE_RULES);
		ruleEditComponent.adjust(microDialogMessageRule);

		/*
		 * Adjust own components
		 */
	}
}
