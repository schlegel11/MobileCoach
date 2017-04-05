package ch.ethz.mc.tools;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab
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
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.memory.ReceivedMessage;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;

public class Simulator {
	private static Simulator				instance;

	private final List<ReceivedMessage>		simulatedReceivedSMS	= new ArrayList<ReceivedMessage>();
	private final List<SimulatorListener>	simulatorListeners		= new ArrayList<Simulator.SimulatorListener>();

	private Simulator() {
		// Do nothing
	}

	@Synchronized
	public static Simulator getInstance() {
		if (instance == null) {
			instance = new Simulator();
		}

		return instance;
	}

	// Method for system to look for new simulated messages
	public ReceivedMessage[] getSimulatedReceivedMessages() {
		val returnArray = simulatedReceivedSMS.toArray(new ReceivedMessage[0]);

		simulatedReceivedSMS.clear();

		return returnArray;
	}

	// Method to register/remove listener for incoming messages
	@Synchronized
	public void registerSimulatorListener(
			final SimulatorListener simulatorListener) {
		simulatorListeners.add(simulatorListener);
	}

	@Synchronized
	public void removeSimulatorListener(
			final SimulatorListener simulatorListener) {
		simulatorListeners.remove(simulatorListener);
	}

	// Methods for simulation
	@Synchronized
	public void simulateSMSBySystem(final String message) {
		for (final val simulatorListener : simulatorListeners) {
			try {
				simulatorListener.newSimulatedMessageFromSystem(message);
			} catch (final Exception e) {
				// do nothing

			}
		}
	}

	@Synchronized
	public void simulateSMSReplyByParticipant(
			final String senderIdentification, final String message) {
		val simulatedReceivedMessage = new ReceivedMessage();
		simulatedReceivedMessage.setType(DialogOptionTypes.SMS);
		simulatedReceivedMessage.setMessage(message);
		simulatedReceivedMessage.setReceivedTimestamp(InternalDateTime
				.currentTimeMillis());
		simulatedReceivedMessage.setSender(Constants.getSmsSimulationNumber());
		simulatedReceivedMessage.setRecipient(senderIdentification);
		simulatedReceivedSMS.add(simulatedReceivedMessage);
	}

	// Helper interface
	public interface SimulatorListener {
		void newSimulatedMessageFromSystem(final String message);
	}
}
