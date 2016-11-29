package ch.ethz.mc.services.internal;

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
import java.util.ArrayList;
import java.util.List;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.model.memory.ReceivedMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.modules.adapters.AbstractMessageRetrievalAdapter;
import ch.ethz.mc.modules.adapters.AbstractMessageSendingAdapter;
import ch.ethz.mc.tools.Simulator;

/**
 * Handles communication with SMS gateway
 *
 * @author Andreas Filler
 */
@Log4j2
public class CommunicationManagerService {
	private static CommunicationManagerService	instance	= null;

	private CommunicationManagerService() throws Exception {
		log.info("Starting service...");

		log.info("Started.");
	}

	public static CommunicationManagerService start() throws Exception {
		if (instance == null) {
			instance = new CommunicationManagerService();
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	@Synchronized
	public void stopMessageSending() {
		log.debug("Stopping sending threads...");

		for (final AbstractMessageSendingAdapter adapter : MC.getInstance()
				.getModuleManagerService()
				.getRegisteredAdapters(AbstractMessageSendingAdapter.class)) {
			adapter.shutdown();
		}
	}

	/**
	 * Sends a mail message (asynchronous)
	 *
	 * @param dialogOption
	 * @param dialogMessageId
	 * @param message
	 * @param metaData
	 * @param messageExpectsAnswer
	 */
	@Synchronized
	public void sendMessage(final DialogOption dialogOption,
			final ObjectId dialogMessageId, final String message,
			final String[] metaData, final boolean messageExpectsAnswer) {
		for (final AbstractMessageSendingAdapter adapter : MC.getInstance()
				.getModuleManagerService()
				.getRegisteredAdapters(AbstractMessageSendingAdapter.class)) {
			try {
				if (adapter.getSupportedDialogOptionType() == dialogOption
						.getType()) {
					adapter.sendMessage(dialogOption, dialogMessageId, message,
							metaData, messageExpectsAnswer);
				}
			} catch (final Exception e) {
				log.warn("Error at sending message with adapter {}: {}",
						adapter.getClass().getName(), e.getMessage());
			}
		}
	}

	/**
	 * Receives mail messages
	 *
	 * @return
	 */
	public List<ReceivedMessage> receiveMessages() {
		val receivedMessages = new ArrayList<ReceivedMessage>();

		// Add simulated messages if available
		CollectionUtils.addAll(receivedMessages, Simulator.getInstance()
				.getSimulatedReceivedMessages());

		for (final AbstractMessageRetrievalAdapter adapter : MC.getInstance()
				.getModuleManagerService()
				.getRegisteredAdapters(AbstractMessageRetrievalAdapter.class)) {

			try {
				receivedMessages.addAll(adapter.receiveMessages());
			} catch (final Exception e) {
				log.warn("Error at receiving messages with adapter {}: {}",
						adapter.getClass().getName(), e.getMessage());
			}
		}

		return receivedMessages;
	}
}
