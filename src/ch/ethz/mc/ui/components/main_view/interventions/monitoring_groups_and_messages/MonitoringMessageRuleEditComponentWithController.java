package ch.ethz.mc.ui.components.main_view.interventions.monitoring_groups_and_messages;

/* ##LICENSE## */
import org.bson.types.ObjectId;

import ch.ethz.mc.model.memory.types.RuleTypes;
import ch.ethz.mc.model.persistent.MonitoringMessageRule;
import ch.ethz.mc.ui.components.basics.AbstractRuleEditComponentWithController;

/**
 * Extends the monitoring message rule edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class MonitoringMessageRuleEditComponentWithController
		extends MonitoringMessageRuleEditComponent {

	private final AbstractRuleEditComponentWithController ruleEditComponent;

	public MonitoringMessageRuleEditComponentWithController(
			final MonitoringMessageRule monitoringMessageRule,
			final ObjectId interventionId) {
		super();

		// Configure integrated components
		ruleEditComponent = getAbstractRuleEditComponentWithController();
		ruleEditComponent.init(interventionId,
				RuleTypes.MONITORING_MESSAGE_RULES);
		ruleEditComponent.adjust(monitoringMessageRule);

		/*
		 * Adjust own components
		 */
	}
}
