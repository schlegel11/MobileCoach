package ch.ethz.mc.model.persistent.outdated;

/* ##LICENSE## */
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
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
	private static final long		serialVersionUID	= 4006871903463626376L;

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
