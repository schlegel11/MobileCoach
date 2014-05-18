package org.isgf.mhc.model.persistent;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.persistent.types.DialogMessageStatusTypes;
import org.isgf.mhc.model.ui.UIDialogMessageReducedWithParticipant;
import org.isgf.mhc.model.ui.UIDialogMessageWithParticipant;
import org.isgf.mhc.tools.StringHelpers;

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
	 * Defines if an answer to this message is expected
	 */
	@Getter
	@Setter
	private boolean						messageExpectsAnswer;

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
	 * <strong>OPTIONAL:</strong> The {@link MonitoringMessage} used to create
	 * this {@link DialogMessage}
	 */
	@Getter
	@Setter
	private ObjectId					relatedMonitoringMessage;

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

	/**
	 * Create a {@link UIDialogMessageWithParticipant} with the belonging
	 * {@link Participant}
	 * 
	 * @param participantId
	 * @param participantName
	 * @return
	 */
	public UIDialogMessageWithParticipant toUIDialogMessageWithParticipant(
			final String participantId, final String participantName) {
		final val dialogMessage = new UIDialogMessageWithParticipant(
				participantId,
				participantName,
				String.valueOf(order + 1),
				status.toString(),
				message == null || message.equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: message,
				StringHelpers.createStringTimeStamp(shouldBeSentTimestamp),
				StringHelpers.createStringTimeStamp(sentTimestamp),
				answerReceived == null || answerReceived.equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: answerReceived,
				StringHelpers.createStringTimeStamp(answerReceivedTimestamp),
				manuallySent ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__YES)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NO),
				mediaContentViewed ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__YES)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NO));

		dialogMessage.setRelatedModelObject(this);

		return dialogMessage;
	}

	/**
	 * Create a {@link UIDialogMessageReducedWithParticipant} with the belonging
	 * {@link Participant}
	 * 
	 * @param participantId
	 * @param participantName
	 * @return
	 */
	public UIDialogMessageReducedWithParticipant toUIDialogMessageReducedWithParticipant(
			final String participantId, final String participantName) {
		final val dialogMessage = new UIDialogMessageReducedWithParticipant(
				participantId, participantName, status.toString(),
				sentTimestamp <= 0 ? null : new Date(sentTimestamp),
				answerReceived == null || answerReceived.equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: answerReceived, answerReceivedTimestamp <= 0 ? null
						: new Date(answerReceivedTimestamp));

		dialogMessage.setRelatedModelObject(this);

		return dialogMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		// Delete media object participant short URLs
		val mediaObjectParticipantShortURLsToDelete = ModelObject
				.find(MediaObjectParticipantShortURL.class,
						Queries.MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_RELATED_DIALOG_MESSAGE,
						getId());

		ModelObject.delete(mediaObjectParticipantShortURLsToDelete);
	}

}
