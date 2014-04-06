package org.isgf.mhc.model.persistent.types;

/**
 * Supported dialog message status types
 * 
 * @author Andreas Filler
 */
public enum DialogMessageStatusTypes {
	PREPARED_FOR_SENDING, SENDING, SENT, SENT_AND_ANSWERED_BY_PARTICIPANT, SENT_BUT_NOT_ANSWERED_BY_PARTICIPANT, PROCESSED, RECEIVED_UNEXPECTEDLY, INTERVENTION_FINISHED_BEFORE_PROCESSING_FINISHED
}
