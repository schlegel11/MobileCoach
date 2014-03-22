package org.isgf.mhc.tools;

import lombok.val;

import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.model.server.concepts.AbstractRule;
import org.isgf.mhc.model.server.types.EquationSignTypes;

/**
 * Small helpers for {@link String}s
 * 
 * @author Andreas Filler
 */
public class StringHelpers {
	/**
	 * Creates a readable name representation of a rule's name
	 * 
	 * @param abstractRule
	 * @return
	 */
	public static String createRuleName(final AbstractRule abstractRule) {
		val name = new StringBuffer();

		if (abstractRule.getRuleWithPlaceholders() == null
				|| abstractRule.getRuleWithPlaceholders().equals("")) {
			if (abstractRule.getRuleEquationSign() != EquationSignTypes.IS_ALWAYS_TRUE
					&& abstractRule.getRuleEquationSign() != EquationSignTypes.IS_ALWAYS_FALSE) {
				name.append(ImplementationContants.DEFAULT_OBJECT_NAME + " ");
			}
		} else {
			name.append(abstractRule.getRuleWithPlaceholders() + " ");
		}

		name.append(abstractRule.getRuleEquationSign().toString());

		if (abstractRule.getRuleComparisonTermWithPlaceholders() == null
				|| abstractRule.getRuleComparisonTermWithPlaceholders().equals(
						"")) {
			if (abstractRule.getRuleEquationSign() != EquationSignTypes.IS_ALWAYS_TRUE
					&& abstractRule.getRuleEquationSign() != EquationSignTypes.IS_ALWAYS_FALSE) {
				name.append(" " + ImplementationContants.DEFAULT_OBJECT_NAME);
			}
		} else {
			name.append(" "
					+ abstractRule.getRuleComparisonTermWithPlaceholders());
		}

		return name.toString();
	}

}
