package ch.ethz.mc.ui.views.components.interventions.monitoring_rules;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab
 * 
 * For details see README.md file in the root folder of this project.
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
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.ui.views.components.basics.AbstractMonitoringRulesEditComponentWithController;

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
