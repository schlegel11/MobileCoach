package ch.ethz.mc.model.persistent.types;

/**
 * Supported dialog message status types
 * 
 * @author Andreas Filler
 */
public enum DialogMessageStatusTypes {
	IN_CREATION, PREPARED_FOR_SENDING, SENDING, SENT_AND_WAITING_FOR_ANSWER, SENT_BUT_NOT_WAITING_FOR_ANSWER, SENT_AND_ANSWERED_BY_PARTICIPANT, SENT_AND_ANSWERED_AND_PROCESSED, SENT_AND_NOT_ANSWERED_AND_PROCESSED, RECEIVED_UNEXPECTEDLY;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}
}
