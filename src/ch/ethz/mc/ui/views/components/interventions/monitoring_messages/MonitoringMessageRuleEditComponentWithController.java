package ch.ethz.mc.ui.views.components.interventions.monitoring_messages;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
