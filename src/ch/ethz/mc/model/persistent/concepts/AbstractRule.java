package ch.ethz.mc.model.persistent.concepts;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;

/**
 * {@link ModelObject} to represent a rule
 * 
 * An {@link AbstractRule} consists of the rule itself, an equation sign and a
 * comparison term. The rule can be evaluated and checked against the comparison
 * term based on the equation sign.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractRule extends ModelObject {
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
}
