package org.isgf.mhc.modules.message_contest;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.ImplementationConstants;
import org.isgf.mhc.conf.Messages;

/**
 * @author Andreas Filler
 * 
 */
@SuppressWarnings("serial")
public class MessageContestMotivationalMessage extends
		MessageContestModuleWithController {

	public MessageContestMotivationalMessage(final ObjectId interventionId) {
		super(interventionId);
	}

	@Override
	protected String getResultVariable() {
		// FIXME Should be done cleaner
		return ImplementationConstants.MESSAGE_CONTEST_MOTIVATIONAL_RESULT_VARIABLE;
	}

	@Override
	protected String getRelevantVariable() {
		// FIXME Should be done cleaner
		return ImplementationConstants.MESSAGE_CONTEST_MOTIVATIONAL_RELEVANT_VARIABLE;
	}

	@Override
	public String getName() {
		// FIXME Should be done cleaner
		return Messages.getAdminString(
				AdminMessageStrings.MODULES__MESSAGE_CONTEST__NAME,
				"Motivational Message");
	}

}
