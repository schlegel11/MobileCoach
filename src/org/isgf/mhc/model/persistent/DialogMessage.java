package org.isgf.mhc.model.persistent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.persistent.types.DialogMessageStatusTypes;

/**
 * {@link ModelObject} to represent an {@link DialogMessage}
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class DialogMessage extends ModelObject {
	/**
	 * The recipient of the {@link DialogMessage}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId					participant;

	/**
	 * The position of the {@link DialogMessage} compared to all other
	 * {@link DialogMessage}s of a specific {@link Participant}
	 */
	@Getter
	@Setter
	private int							order;

	/**
	 * The status of the {@link DialogMessage}; all statuses follow each other
	 * except for the last two: a {@link DialogMessage} can only have one of
	 * them
	 */
	@Getter
	@Setter
	@NonNull
	private DialogMessageStatusTypes	status;

	/**
	 * The message sent to the {@link Participant}
	 */
	@Getter
	@Setter
	@NonNull
	private String						message;

	/**
	 * Timestamp when the {@link DialogMessage} should have been sent
	 */
	@Getter
	@Setter
	private long						shouldBeSentTimestamp;

	/**
	 * Timestamp when the {@link DialogMessage} has been sent
	 */
	@Getter
	@Setter
	private long						sentTimestamp;

	/**
	 * Timestamp when the {@link DialogMessage} is handled as unanswered
	 */
	@Getter
	@Setter
	private long						isUnansweredAfterTimestamp;

	/**
	 * Timestamp when the answer has been received
	 */
	@Getter
	@Setter
	private long						answerReceivedTimestamp;

	/**
	 * The response retrieved from the {@link Participant}
	 */
	@Getter
	@Setter
	private String						answerReceived;

	/**
	 * Marker showing if a response to a {@link DialogMessage} can not be
	 * automatically processed by the system
	 */
	@Getter
	@Setter
	private boolean						answerNotAutomaticallyProcessable;

	/**
	 * <strong>OPTIONAL:</strong> The {@link MonitoringRule} containing the
	 * {@link MonitoringReplyRule}s to
	 * execute after a timeout or reply
	 */
	@Getter
	@Setter
	private ObjectId					relatedMonitoringRuleForReplyRules;

	/**
	 * The information if a {@link Participant} viewed the presented media
	 * content (if
	 * integrated in the {@link MonitoringMessage})
	 */
	@Getter
	@Setter
	private boolean						mediaContentViewed;

	/**
	 * Marker showing if the {@link DialogMessage} has been sent manually by an
	 * {@link Author}
	 */
	@Getter
	@Setter
	private boolean						manuallySent;

}
