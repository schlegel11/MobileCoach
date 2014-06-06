package ch.ethz.mc.tools;

import java.util.ArrayList;
import java.util.List;

import lombok.Synchronized;
import lombok.val;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.memory.ReceivedMessage;

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
	public void simulateSMSReplyByParticipant(final String message) {
		val simulatedReceivedMessage = new ReceivedMessage();
		simulatedReceivedMessage.setMessage(message);
		simulatedReceivedMessage.setReceivedTimestamp(InternalDateTime
				.currentTimeMillis());
		simulatedReceivedMessage.setSender(Constants.getSmsSimulationNumber());
		simulatedReceivedMessage
				.setRecipient(Constants.getSmsPhoneNumberFrom());
		simulatedReceivedSMS.add(simulatedReceivedMessage);
	}

	// Helper interface
	public interface SimulatorListener {
		void newSimulatedMessageFromSystem(final String message);
	}
}
