package ch.ethz.mc.modules.message_contest;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health IS-Lab
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
