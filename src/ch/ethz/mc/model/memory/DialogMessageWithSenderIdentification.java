package ch.ethz.mc.model.memory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ch.ethz.mc.model.persistent.DialogMessage;

/**
 * Wrapper for dialog messages to also contain the appropriate sender's
 * identification
 * 
 * @author Andreas Filler
 * 
 */
@ToString
@AllArgsConstructor
public class DialogMessageWithSenderIdentification {
	@Getter
	private final DialogMessage	dialogMessage;

	@Getter
	private final String		messageSenderIdentification;
}
