package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.types.AnswerTypes;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.persistent.types.DialogMessageTypes;
import ch.ethz.mc.model.persistent.types.MediaObjectTypes;
import ch.ethz.mc.model.persistent.types.TextFormatTypes;
import ch.ethz.mc.model.ui.UIDialogMessageProblemViewWithParticipant;
import ch.ethz.mc.model.ui.UIDialogMessageWithParticipant;
import ch.ethz.mc.model.ui.results.UIDialogMessageWithParticipantForResults;
import ch.ethz.mc.tools.StringHelpers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * {@link ModelObject} to represent an {@link DialogMessage}
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class DialogMessage extends ModelObject {
	private static final long			serialVersionUID	= 1929639243199456022L;

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
	 * <strong>OPTIONAL:</strong> The message will be pushed, but will not
	 * appear in the chat
	 */
	@Getter
	@Setter
	private boolean						pushOnly			= false;

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
	 * The {@link DialogMessageTypes} of the message
	 */
	@Getter
	@Setter
	@NonNull
	private DialogMessageTypes			type;

	/**
	 * The original id of the message on any client (if required)
	 */
	@Getter
	@Setter
	private String						clientId;

	/**
	 * The message sent to the {@link Participant}
	 */
	@Getter
	@Setter
	@NonNull
	private String						message;

	/**
	 * The message sent to the {@link Participant} (version for older
	 * technologies, e.g. email, SMS)
	 */
	@Getter
	@Setter
	@NonNull
	private String						messageWithForcedLinks;

	/**
	 * The format of the message text
	 */
	@Getter
	@Setter
	@NonNull
	private TextFormatTypes				textFormat;

	/**
	 * The {@link AnswerTypes} of the message
	 */
	@Getter
	@Setter
	private AnswerTypes					answerType;

	/**
	 * The {@link AnswerTypes} of the message
	 */
	@Getter
	@Setter
	private String						answerOptions;

	/**
	 * Optional survey link contained in message
	 */
	@Getter
	@Setter
	private String						surveyLink;

	/**
	 * Optional media object link contained in message
	 */
	@Getter
	@Setter
	private String						mediaObjectLink;

	/**
	 * Optional name of media object contained in message
	 */
	@Getter
	@Setter
	private String						mediaObjectName;

	/**
	 * Optional type of media object linked in message
	 */
	@Getter
	@Setter
	private MediaObjectTypes			mediaObjectType;

	/**
	 * Optional text based media object content
	 */
	@Getter
	@Setter
	private String						textBasedMediaObjectContent;

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
	 * Defines if the message has to be sent to the supervisor instead of the
	 * {@link Participant} (type of message)
	 */
	@Getter
	@Setter
	private boolean						supervisorMessage;

	/**
	 * Defines if an answer to this message is expected
	 */
	@Getter
	@Setter
	private boolean						messageExpectsAnswer;

	/**
	 * Defines if the answer can be cancelled, e.g. by showing an X beside the
	 * answer to enable the user to skip it
	 *
	 */
	@Getter
	@Setter
	private boolean						answerCanBeCancelled;

	/**
	 * Defines if this message should be sticky within a chat client
	 */
	@Getter
	@Setter
	private boolean						messageIsSticky;

	/**
	 * Defines if this message deactivates all open questions of former messages
	 *
	 */
	@Getter
	@Setter
	private boolean						messageDeactivatesAllOpenQuestions;

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
	 * The uncleaned response retrieved from the {@link Participant}
	 */
	@Getter
	@Setter
	private String						answerReceivedRaw;

	/**
	 * Marker showing if a response to a {@link DialogMessage} can not be
	 * automatically processed by the system
	 */
	@Getter
	@Setter
	private boolean						answerNotAutomaticallyProcessable;

	/**
	 * <strong>OPTIONAL:</strong> The {@link MonitoringRule} containing the
	 * {@link MonitoringReplyRule}s to execute after a timeout or reply
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
	 * <strong>OPTIONAL:</strong> The {@link MicroDialog} that should be
	 * activated when the appropriate point in time occurs
	 */
	@Getter
	@Setter
	private ObjectId					relatedMicroDialogForActivation;

	/**
	 * <strong>OPTIONAL:</strong> The {@link MicroDialogMessage} used to create
	 * this {@link DialogMessage}
	 */
	@Getter
	@Setter
	private ObjectId					relatedMicroDialogMessage;

	/**
	 * The information if a {@link Participant} viewed the presented media
	 * content (if integrated in the {@link MonitoringMessage})
	 */
	@Getter
	@Setter
	private boolean						mediaContentViewed;

	/**
	 * Marker showing if the {@link DialogMessage} has been sent manually by an
	 * {@link BackendUser}
	 */
	@Getter
	@Setter
	private boolean						manuallySent;
	
	/**
	 * Optional external service id if message are related to an external service action
	 */
	@Getter
	@Setter
	private ObjectId					externalService;

	/**
	 * Create a {@link UIDialogMessageWithParticipantForResults} with the
	 * belonging {@link Participant}
	 *
	 * @param participantId
	 * @param participantName
	 * @param language
	 * @param group
	 * @param organization
	 * @param organizationUnit
	 * @param containsMediaContent
	 * @return
	 */
	public UIDialogMessageWithParticipantForResults toUIDialogMessageWithParticipantForResults(
			final String participantId, final String participantName,
			final String language, final String group,
			final String organization, final String organizationUnit,
			final boolean containsMediaContent) {

		String typeString = null;
		switch (type) {
			case COMMAND:
				typeString = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__COMMAND_MESSAGE);
				break;
			case INTENTION:
				typeString = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__USER_INTENTION_MESSAGE);
				break;
			case PLAIN:
				typeString = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__PLAIN_TEXT_MESSAGE);
				break;
			case MICRO_DIALOG_ACTIVATION:
				typeString = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__MICRO_DIALOG_ACTIVATION_MESSAGE);
				break;
		}

		final val dialogMessage = new UIDialogMessageWithParticipantForResults(
				participantId, participantName, language,
				group == null ? Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__NOT_SET) : group,
				organization, organizationUnit,
				StringUtils.right("0000" + String.valueOf(order + 1), 5),
				status.toString(),
				supervisorMessage
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__SUPERVISOR_MESSAGE)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__PARTICIPANT_MESSAGE),
				typeString,
				message == null || message.equals("")
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NOT_SET)
						: message,
				StringHelpers.createStringTimestamp(shouldBeSentTimestamp),
				StringHelpers.createStringTimestamp(sentTimestamp),
				answerReceived == null || answerReceived.equals("")
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NOT_SET)
						: answerReceived,
				answerReceivedRaw == null || answerReceivedRaw.equals("")
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NOT_SET)
						: answerReceivedRaw,
				StringHelpers.createStringTimestamp(answerReceivedTimestamp),
				manuallySent
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__YES)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NO),
				containsMediaContent
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__YES)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NO),
				mediaContentViewed
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__YES)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NO));

		dialogMessage.setRelatedModelObject(this);

		return dialogMessage;
	}

	/**
	 * Create a {@link UIDialogMessageWithParticipant} with the belonging
	 * {@link Participant}
	 *
	 * @param participantId
	 * @param participantName
	 * @param language
	 * @param organization
	 * @param organizationUnit
	 * @param containsMediaContent
	 * @return
	 */
	public UIDialogMessageWithParticipant toUIDialogMessageWithParticipant(
			final String participantId, final String participantName,
			final String language, final String group,
			final String organization, final String organizationUnit,
			final boolean containsMediaContent) {

		String typeString = null;
		switch (type) {
			case COMMAND:
				typeString = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__COMMAND_MESSAGE);
				break;
			case INTENTION:
				typeString = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__USER_INTENTION_MESSAGE);
				break;
			case PLAIN:
				typeString = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__PLAIN_TEXT_MESSAGE);
				break;
			case MICRO_DIALOG_ACTIVATION:
				typeString = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__MICRO_DIALOG_ACTIVATION_MESSAGE);
				break;
		}

		final val dialogMessage = new UIDialogMessageWithParticipant(
				participantId, participantName, language,
				group == null ? Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__NOT_SET) : group,
				organization, organizationUnit, order + 1, status.toString(),
				supervisorMessage
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__SUPERVISOR_MESSAGE)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__PARTICIPANT_MESSAGE),
				typeString,
				message == null || message.equals("")
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NOT_SET)
						: message,
				shouldBeSentTimestamp <= 0 ? null
						: new Date(shouldBeSentTimestamp),
				sentTimestamp <= 0 ? null : new Date(sentTimestamp),
				answerReceived == null || answerReceived.equals("")
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NOT_SET)
						: answerReceived,
				answerReceivedRaw == null || answerReceivedRaw.equals("")
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NOT_SET)
						: answerReceivedRaw,
				answerReceivedTimestamp <= 0 ? null
						: new Date(answerReceivedTimestamp),
				manuallySent
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__YES)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NO),
				containsMediaContent
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__YES)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NO),
				mediaContentViewed
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__YES)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NO));

		dialogMessage.setRelatedModelObject(this);

		return dialogMessage;
	}

	/**
	 * Create a {@link UIDialogMessageProblemViewWithParticipant} with the
	 * belonging {@link Participant}
	 *
	 * @param participantId
	 * @param participantName
	 * @param language
	 * @param group
	 * @param organization
	 * @param organizationUnit
	 * @return
	 */
	public UIDialogMessageProblemViewWithParticipant toUIDialogMessageProblemViewWithParticipant(
			final String participantId, final String participantName,
			final String language, final String group,
			final String organization, final String organizationUnit) {

		String typeString = null;
		switch (type) {
			case COMMAND:
				typeString = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__COMMAND_MESSAGE);
				break;
			case INTENTION:
				typeString = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__USER_INTENTION_MESSAGE);
				break;
			case PLAIN:
				typeString = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__PLAIN_TEXT_MESSAGE);
				break;
			case MICRO_DIALOG_ACTIVATION:
				typeString = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__MICRO_DIALOG_ACTIVATION_MESSAGE);
				break;
		}

		final val dialogMessage = new UIDialogMessageProblemViewWithParticipant(
				participantId, participantName, language,
				group == null ? Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__NOT_SET) : group,
				organization, organizationUnit,
				supervisorMessage
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__SUPERVISOR_MESSAGE)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__PARTICIPANT_MESSAGE),
				typeString, message,
				sentTimestamp <= 0 ? null : new Date(sentTimestamp),
				answerReceived == null || answerReceived.equals("")
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NOT_SET)
						: answerReceived,
				answerReceivedRaw == null || answerReceivedRaw.equals("")
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NOT_SET)
						: answerReceivedRaw,
				answerReceivedTimestamp <= 0 ? null
						: new Date(answerReceivedTimestamp));

		dialogMessage.setRelatedModelObject(this);

		return dialogMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		// Delete media object participant short URLs
		val mediaObjectParticipantShortURLsToDelete = ModelObject.find(
				MediaObjectParticipantShortURL.class,
				Queries.MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_DIALOG_MESSAGE,
				getId());

		ModelObject.delete(mediaObjectParticipantShortURLsToDelete);
	}

}
