package ch.ethz.mc.model.persistent;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.ui.UIDialogMessageProblemViewWithParticipant;
import ch.ethz.mc.model.ui.UIDialogMessageWithParticipant;
import ch.ethz.mc.model.ui.results.UIDialogMessageWithParticipantForResults;
import ch.ethz.mc.tools.StringHelpers;

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
	 * Create a {@link UIDialogMessageWithParticipantForResults} with the
	 * belonging {@link Participant}
	 *
	 * @param participantId
	 * @param participantName
	 * @param participantLanguage
	 * @param organization
	 * @param organizationUnit
	 * @param containsMediaContent
	 * @return
	 */
	public UIDialogMessageWithParticipantForResults toUIDialogMessageWithParticipantForResults(
			final String participantId, final String participantName,
			final String group, final String language,
			final String organization, final String organizationUnit,
			final boolean containsMediaContent) {

		final val dialogMessage = new UIDialogMessageWithParticipantForResults(
				participantId,
				participantName,
				language,
				group == null ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: group,
						organization,
						organizationUnit,
						StringUtils.right("0000" + String.valueOf(order + 1), 5),
						status.toString(),
						message == null || message.equals("") ? Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
								: message,
								StringHelpers.createStringTimeStamp(shouldBeSentTimestamp),
								StringHelpers.createStringTimeStamp(sentTimestamp),
								answerReceived == null || answerReceived.equals("") ? Messages
										.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
										: answerReceived,
										answerReceivedRaw == null || answerReceivedRaw.equals("") ? Messages
												.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
												: answerReceivedRaw,
												StringHelpers.createStringTimeStamp(answerReceivedTimestamp),
												manuallySent ? Messages
														.getAdminString(AdminMessageStrings.UI_MODEL__YES)
														: Messages
														.getAdminString(AdminMessageStrings.UI_MODEL__NO),
														containsMediaContent ? Messages
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
	 * Create a {@link UIDialogMessageWithParticipant} with the
	 * belonging {@link Participant}
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
		final val dialogMessage = new UIDialogMessageWithParticipant(
				participantId,
				participantName,
				language,
				group == null ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: group,
						organization,
						organizationUnit,
						order + 1,
						status.toString(),
						message == null || message.equals("") ? Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
								: message,
				shouldBeSentTimestamp <= 0 ? null : new Date(
						shouldBeSentTimestamp),
				sentTimestamp <= 0 ? null : new Date(sentTimestamp),
				answerReceived == null || answerReceived.equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: answerReceived,
				answerReceivedRaw == null || answerReceivedRaw.equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: answerReceivedRaw,
				answerReceivedTimestamp <= 0 ? null : new Date(
						answerReceivedTimestamp),
				manuallySent ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__YES)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NO),
				containsMediaContent ? Messages
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
		final val dialogMessage = new UIDialogMessageProblemViewWithParticipant(
				participantId,
				participantName,
				language,
				group == null ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: group,
				organization,
				organizationUnit,
				message,
				sentTimestamp <= 0 ? null : new Date(sentTimestamp),
				answerReceived == null || answerReceived.equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: answerReceived,
				answerReceivedRaw == null || answerReceivedRaw.equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: answerReceivedRaw,
				answerReceivedTimestamp <= 0 ? null : new Date(
						answerReceivedTimestamp));

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
		val mediaObjectParticipantShortURLsToDelete = ModelObject
				.find(MediaObjectParticipantShortURL.class,
						Queries.MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_RELATED_DIALOG_MESSAGE,
						getId());

		ModelObject.delete(mediaObjectParticipantShortURLsToDelete);
	}

}
