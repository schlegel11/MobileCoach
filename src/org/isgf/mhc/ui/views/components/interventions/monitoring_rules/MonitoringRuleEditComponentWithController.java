package org.isgf.mhc.ui.views.components.interventions.monitoring_rules;

import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.server.Intervention;
import org.isgf.mhc.model.server.MonitoringRule;
import org.isgf.mhc.ui.views.components.basics.AbstractRuleEditComponentWithController;

/**
 * Extends the monitoring rule edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringRuleEditComponentWithController extends
		MonitoringRuleEditComponent {

	private final AbstractRuleEditComponentWithController			ruleEditComponent;

	private final MonitoringReplyRulesEditComponentWithController	monitoringReplyRulesEditComponentWithControllerIfAnswer;
	private final MonitoringReplyRulesEditComponentWithController	monitoringReplyRulesEditComponentWithControllerIfNoAnswer;

	private final MonitoringRule									monitoringRule;

	public MonitoringRuleEditComponentWithController(
			final Intervention intervention, final ObjectId monitoringRuleId) {
		super();

		// Configure integrated components
		monitoringRule = getInterventionAdministrationManagerService()
				.getMonitoringRule(monitoringRuleId);

		ruleEditComponent = getAbstractRuleEditComponentWithController();
		ruleEditComponent.init(intervention.getId());
		ruleEditComponent.adjust(monitoringRule);

		monitoringReplyRulesEditComponentWithControllerIfAnswer = getMonitoringReplyRulesEditComponentWithControllerIfAnswer();
		monitoringReplyRulesEditComponentWithControllerIfAnswer.init(
				monitoringRuleId, true);
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer = getMonitoringReplyRulesEditComponentWithControllerIfNoAnswer();
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer.init(
				monitoringRuleId, false);

		// Adjust own components
		// TODO
	}
}
