package ch.ethz.mc.model.persistent.outdated;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * CAUTION: Will only be used for conversion from data model 11 to 12
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractRuleV12 extends ModelObject {
	/**
	 * Rule containing placeholders for variables
	 */
	@Getter
	@Setter
	@NonNull
	private String					ruleWithPlaceholders;

	/**
	 * Equation sign to compare the rule with the rule comparison term
	 */
	@Getter
	@Setter
	@NonNull
	private RuleEquationSignTypes	ruleEquationSign;

	/**
	 * The term containing placeholders to compare the rule with
	 */
	@Getter
	@Setter
	@NonNull
	private String					ruleComparisonTermWithPlaceholders;

	/**
	 * A comment for the author, not visible to any participant
	 */
	@Getter
	@Setter
	@NonNull
	private String					comment;
}
