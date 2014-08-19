package ch.ethz.mc.modules.message_contest;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;

/**
 * @author Andreas Filler
 * 
 */
@SuppressWarnings("serial")
public class MessageContestDrinkingMessage extends
		MessageContestModuleWithController {

	@Override
	protected String getResultVariable() {
		// FIXME Should be done cleaner
		return ImplementationConstants.MESSAGE_CONTEST_DRINKING_RESULT_VARIABLE;
	}

	@Override
	protected String getRelevantVariable() {
		// FIXME Should be done cleaner
		return ImplementationConstants.MESSAGE_CONTEST_DRINKING_RELEVANT_VARIABLE;
	}

	@Override
	public String getName() {
		// FIXME Should be done cleaner
		return Messages.getAdminString(
				AdminMessageStrings.MODULES__MESSAGE_CONTEST__NAME,
				"Drinking Message (MC alcohol)");
	}

}
