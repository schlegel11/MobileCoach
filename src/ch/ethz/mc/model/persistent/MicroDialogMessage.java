package ch.ethz.mc.model.persistent;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.concepts.MicroDialogElementInterface;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.model.persistent.types.AnswerTypes;
import ch.ethz.mc.model.ui.UIMicroDialogElementInterface;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.tools.StringHelpers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * {@link ModelObject} to represent an {@link MicroDialogMessage}
 *
 * {@link MicroDialogMessage}s will be sent to the {@link Participant} during an
 * {@link Intervention}. {@link MicroDialogMessage}s are part of
 * {@link MicroDialog}s
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class MicroDialogMessage extends ModelObject
		implements MicroDialogElementInterface {
	/**
	 * The {@link MicroDialog} this {@link MicroDialogMessage} belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	microDialog;

	/**
	 * The position of the {@link MicroDialogMessage} compared to all other
	 * {@link MicroDialogElementInterface}s in the same {@link MicroDialog}
	 */
	@Getter
	@Setter
	private int			order;

	/**
	 * The message text containing placeholders for variables
	 */
	@Getter
	@Setter
	@NonNull
	private LString		textWithPlaceholders;

	/**
	 * The message itself is a command for the client
	 */
	@Getter
	@Setter
	private boolean		commandMessage;

	/**
	 * <strong>OPTIONAL:</strong> The {@link MediaObject} used/presented in this
	 * {@link MicroDialogMessage}
	 */
	@Getter
	@Setter
	private ObjectId	linkedMediaObject;

	/**
	 * <strong>OPTIONAL:</strong> The intermediate {@link ScreeningSurvey}
	 * used/presented in this {@link MicroDialogMessage}
	 */
	@Getter
	@Setter
	private ObjectId	linkedIntermediateSurvey;

	/**
	 * Defines if the {@link MicroDialogMessage} expects to be answered by the
	 * {@link Participant}
	 *
	 */
	@Getter
	@Setter
	private boolean		messageExpectsAnswer;

	/**
	 * Defines if the {@link MicroDialogMessage}s in the group expect to be
	 * answeres by the {@link Participant}
	 *
	 */
	@Getter
	@Setter
	private boolean		messageBlocksMicroDialogUntilAnswered;

	/**
	 * <strong>OPTIONAL:</strong> If the result of the
	 * {@link MicroDialogMessage} should be stored, the name of the appropriate
	 * variable can be set here.
	 */
	@Getter
	@Setter
	private String		storeValueToVariableWithName;

	/**
	 * <strong>OPTIONAL:</strong> The value the variable should have if the user
	 * does not reply
	 */
	@Getter
	@Setter
	private String		noReplyValue;

	/**
	 * The type the answer (if required) should have.
	 */
	@Getter
	@Setter
	@NonNull
	private AnswerTypes	answerType;

	/**
	 * <strong>OPTIONAL if sendMessageIfTrue is false:</strong> The minutes a
	 * {@link Participant} has to answer the message before it's handled as
	 * unanswered
	 */
	@Getter
	@Setter
	private int			minutesUntilMessageIsHandledAsUnanswered;

	/**
	 * The answer options required to display the answer selection properly; the
	 * options should be given one per line and with a colon as separator
	 * between key and value, e.g.:
	 * 
	 * <pre>
	 * great:1<br/>normal:0<br/>terrible:-1
	 * </pre>
	 * 
	 * For custom answer types the key value system can be used to create own
	 * key value pairs, e.g.:
	 * 
	 * <pre>
	 * type:currency-selection<br/>symbol:$currencySymbol<br/>terrible:-1
	 * </pre>
	 * 
	 * These options will be automatically parsed to JSON; if a line contains
	 * more than one colon the last one will be taken.
	 * 
	 */
	@Getter
	@Setter
	@NonNull
	private LString		answerOptionsWithPlaceholders;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		int messageRules = 0;
		val microDialogMessageRules = MC.getInstance()
				.getInterventionAdministrationManagerService()
				.getAllMicroDialogMessageRulesOfMicroDialogMessage(getId());

		if (microDialogMessageRules != null) {
			val microDialogMessageRulesIterator = microDialogMessageRules
					.iterator();

			while (microDialogMessageRulesIterator.hasNext()) {
				microDialogMessageRulesIterator.next();
				messageRules++;
			}
		}

		final val microDialogMessage = new UIMicroDialogElementInterface(
				getOrder(),
				Messages.getAdminString(AdminMessageStrings.UI_MODEL__MESSAGE),
				true, textWithPlaceholders.toShortenedString(80),
				commandMessage
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__YES)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NO),
				linkedMediaObject != null
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__YES)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NO),
				linkedIntermediateSurvey != null
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__YES)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NO),
				messageExpectsAnswer ? answerType.toString()
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__EXPECTS_NO_ANSWER),
				storeValueToVariableWithName != null
						? storeValueToVariableWithName : "",
				messageRules);

		microDialogMessage.setRelatedModelObject(this);

		return microDialogMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	public void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);

		// Linked media object
		if (linkedMediaObject != null) {
			exportList
					.add(ModelObject.get(MediaObject.class, linkedMediaObject));
		}

		// Add micro dialog message rule
		for (val microDialogMessageRule : ModelObject.find(
				MicroDialogMessageRule.class,
				Queries.MICRO_DIALOG_MESSAGE_RULE__BY_MICRO_DIALOG_MESSAGE,
				getId())) {
			microDialogMessageRule
					.collectThisAndRelatedModelObjectsForExport(exportList);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		if (linkedMediaObject != null) {
			val mediaObjectToDelete = ModelObject.get(MediaObject.class,
					linkedMediaObject);

			if (mediaObjectToDelete != null) {
				ModelObject.delete(mediaObjectToDelete);
			}
		}

		// Delete sub rules
		val rulesToDelete = ModelObject.find(MicroDialogMessageRule.class,
				Queries.MICRO_DIALOG_MESSAGE_RULE__BY_MICRO_DIALOG_MESSAGE,
				getId());
		ModelObject.delete(rulesToDelete);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.AbstractSerializableTable#toTable()
	 */
	@Override
	@JsonIgnore
	public String toTable() {
		String table = wrapRow(
				wrapHeader("Text:") + wrapField(escape(textWithPlaceholders)));

		if (linkedMediaObject != null) {
			val mediaObject = ModelObject.get(MediaObject.class,
					linkedMediaObject);
			if (mediaObject != null) {
				String externalReference;
				if (mediaObject.getFileReference() != null) {
					externalReference = "javascript:showMediaObject('"
							+ mediaObject.getId() + "/" + StringHelpers
									.cleanFilenameString(mediaObject.getName())
							+ "')";
				} else {
					externalReference = mediaObject.getUrlReference();
				}

				table += wrapRow(wrapHeader("Linked Media Object:") + wrapField(
						createLink(externalReference, mediaObject.getName())));
			} else {
				table += wrapRow(wrapHeader("Linked Media Object:") + wrapField(
						formatWarning("Media Object set, but not found")));
			}
		}

		if (linkedIntermediateSurvey != null) {
			val linkedSurvey = ModelObject.get(ScreeningSurvey.class,
					linkedIntermediateSurvey);
			if (linkedSurvey != null) {
				table += wrapRow(wrapHeader("Linked intermediate survey:")
						+ wrapField(escape(linkedSurvey.getName())));
			} else {
				table += wrapRow(
						wrapHeader("Linked intermediate survey:") + wrapField(
								formatWarning("Survey set, but not found")));
			}
		}

		table += wrapRow(wrapHeader("Store value to variable:")
				+ wrapField(escape(storeValueToVariableWithName)));

		// Micro Dialog Message Rules
		final StringBuffer buffer = new StringBuffer();
		val rules = ModelObject.findSorted(MicroDialogMessageRule.class,
				Queries.MICRO_DIALOG_MESSAGE_RULE__BY_MICRO_DIALOG_MESSAGE,
				Queries.MICRO_DIALOG_MESSAGE_RULE__SORT_BY_ORDER_ASC, getId());

		for (val rule : rules) {
			buffer.append(rule.toTable());
		}

		if (buffer.length() > 0) {
			table += wrapRow(
					wrapHeader("Rules:") + wrapField(buffer.toString()));
		}

		return wrapTable(table);
	}
}
