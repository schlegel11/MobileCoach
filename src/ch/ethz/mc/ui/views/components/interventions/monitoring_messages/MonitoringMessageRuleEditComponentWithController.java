package ch.ethz.mc.ui.views.components.interventions.monitoring_messages;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.persistent.MonitoringMessageRule;
import ch.ethz.mc.ui.views.components.basics.AbstractRuleEditComponentWithController;
import ch.ethz.mc.ui.views.components.basics.AbstractRuleEditComponentWithController.TYPES;

/**
 * Extends the monitoring message rule edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class MonitoringMessageRuleEditComponentWithController extends
		MonitoringMessageRuleEditComponent {

	private final AbstractRuleEditComponentWithController	ruleEditComponent;

	public MonitoringMessageRuleEditComponentWithController(
			final MonitoringMessageRule monitoringMessageRule,
			final ObjectId interventionId) {
		super();

		// Configure integrated components
		ruleEditComponent = getAbstractRuleEditComponentWithController();
		ruleEditComponent.init(interventionId, TYPES.MONITORING_MESSAGE_RULES);
		ruleEditComponent.adjust(monitoringMessageRule);

		/*
		 * Adjust own components
		 */
	}
}
