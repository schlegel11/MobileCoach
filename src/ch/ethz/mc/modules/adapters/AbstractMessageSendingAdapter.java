package ch.ethz.mc.modules.adapters;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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

import ch.ethz.mc.MC;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.modules.interfaces.ModuleAdapterInterface;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.tools.InternalDateTime;

public abstract class AbstractMessageSendingAdapter implements
ModuleAdapterInterface {

	private InterventionExecutionManagerService	interventionExecutionManagerService;

	/**
	 * Allows to send a message to a {@link Participant} using a specific
	 * gateway; additional meta data can be added if required for specific
	 * interfaces
	 *
	 * @param dialogOption
	 * @param dialogMessageId
	 * @param message
	 * @param metaData
	 * @param messageExpectsAnswer
	 */
	public abstract void sendMessage(DialogOption dialogOption,
			ObjectId dialogMessageId, String message, String[] metaData,
			boolean messageExpectsAnswer);

	/**
	 * Returns the {@link DialogOptionTypes} supported by this adapter
	 *
	 * @return
	 */
	public abstract DialogOptionTypes getSupportedDialogOptionType();

	/**
	 * Allows to react on a shutdown request, e.g. to stop outgoing messag
	 * threads
	 */
	public abstract void shutdown();

	/**
	 * Updates the status of a {@link DialogMessage} during the sending process
	 *
	 * @param dialogMessageId
	 * @param newStatus
	 */
	public void updateDialogMessageStatus(final ObjectId dialogMessageId,
			final DialogMessageStatusTypes newStatus) {
		if (interventionExecutionManagerService == null) {
			interventionExecutionManagerService = MC.getInstance()
					.getInterventionExecutionManagerService();
		}

		interventionExecutionManagerService
		.dialogMessageStatusChangesForSending(dialogMessageId,
				newStatus, InternalDateTime.currentTimeMillis());
	}
}
