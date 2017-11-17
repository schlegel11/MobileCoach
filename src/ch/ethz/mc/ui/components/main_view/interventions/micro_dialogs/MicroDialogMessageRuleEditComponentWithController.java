package ch.ethz.mc.ui.components.main_view.interventions.micro_dialogs;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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
