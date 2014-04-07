package org.isgf.mhc.model.persistent.types;

/**
 * Supported dialog message status types
 * 
 * @author Andreas Filler
 */
public enum DialogMessageStatusTypes {
	IN_CREATION, PREPARED_FOR_SENDING, SENDING, SENT, SENT_AND_ANSWERED_BY_PARTICIPANT, SENT_BUT_NOT_ANSWERED_BY_PARTICIPANT, PROCESSED, RECEIVED_UNEXPECTEDLY
}
