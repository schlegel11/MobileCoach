package org.isgf.mhc.model.persistent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;

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
	private ObjectId	participant;

	/**
	 * The message sent to the {@link Participant}
	 */
	@Getter
	@Setter
	@NonNull
	private String		message;

	/**
	 * Timestamp when the {@link DialogMessage} should been sent
	 */
	@Getter
	@Setter
	private long		shouldBeSentTimestamp;

	/**
	 * Timestamp when the {@link DialogMessage} has been sent
	 */
	@Getter
	@Setter
	private long		sentTimestamp;

	/**
	 * Timestamp when the {@link DialogMessage} is handled as unanswered
	 */
	@Getter
	@Setter
	private long		isUnansweredAfterTimestamp;

	/**
	 * Marker if message has been answered by {@link Participant}
	 */
	@Getter
	@Setter
	private boolean		answeredByParticipant;

	/**
	 * The response retrieved from the {@link Participant}
	 */
	@Getter
	@Setter
	private String		answerFromParticipant;

	/**
	 * Timestamp when the answer has been received
	 */
	@Getter
	@Setter
	private long		answerReceivedTimestamp;

	/**
	 * <strong>OPTIONAL:</strong> The {@link MonitoringRule} containing the
	 * {@link MonitoringReplyRule}s to
	 * execute after a timeout or reply
	 */
	@Getter
	@Setter
	private ObjectId	monitoringRuleContainingReplyRules;

	/**
	 * Marks if the {@link DialogMessage} has been completely proceeded if a
	 * timeout has been set before
	 */
	@Getter
	@Setter
	private boolean		proceededAfterTimeoutOrAnswer;

	/**
	 * Marker showing if the {@link DialogMessage} has been sent manually by an
	 * {@link Author}
	 */
	@Getter
	@Setter
	private boolean		manuallySent;

	/**
	 * The information if a {@link Participant} viewed the presented media
	 * content (if
	 * integrated in the {@link MonitoringMessage})
	 */
	@Getter
	@Setter
	private boolean		mediaContentViewed;

	/**
	 * Marker showing if a response to a {@link DialogMessage} can not be
	 * automatically processed by the system
	 */
	@Getter
	@Setter
	private boolean		notAutomaticallyProcessable;
}
