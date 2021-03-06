package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.model.persistent.types.AnswerTypes;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.model.ui.UIMonitoringMessage;
import ch.ethz.mc.tools.StringHelpers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * {@link ModelObject} to represent an {@link MonitoringMessage}
 *
 * {@link MonitoringMessage}s will be sent to the {@link Participant} during an
 * {@link Intervention}. {@link MonitoringMessage}s are grouped in
 * {@link MonitoringMessageGroup}s.
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringMessage extends ModelObject {
	private static final long	serialVersionUID	= -8135818586097915678L;

	/**
	 * The {@link MonitoringMessageGroup} this {@link MonitoringMessage} belongs
	 * to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			monitoringMessageGroup;

	/**
	 * The message text containing placeholders for variables
	 */
	@Getter
	@Setter
	@NonNull
	private LString				textWithPlaceholders;

	/**
	 * The message itself is a command for the client
	 */
	@Getter
	@Setter
	private boolean				commandMessage;

	/**
	 * The position of the {@link MonitoringMessage} compared to all other
	 * {@link MonitoringMessage}s in the same {@link MonitoringMessageGroup}
	 */
	@Getter
	@Setter
	private int					order;

	/**
	 * <strong>OPTIONAL:</strong> The message will be pushed, but will not
	 * appear in the chat
	 */
	@Getter
	@Setter
	private boolean				pushOnly			= false;

	/**
	 * <strong>OPTIONAL:</strong> The {@link MediaObject} used/presented in this
	 * {@link MonitoringMessage}
	 */
	@Getter
	@Setter
	private ObjectId			linkedMediaObject;

	/**
	 * <strong>OPTIONAL:</strong> The intermediate {@link ScreeningSurvey}
	 * used/presented in this {@link MonitoringMessage}
	 */
	@Getter
	@Setter
	private ObjectId			linkedIntermediateSurvey;

	/**
	 * <strong>OPTIONAL:</strong> If the result of the {@link MonitoringMessage}
	 * should be stored, the name of the appropriate variable can be set here.
	 */
	@Getter
	@Setter
	private String				storeValueToVariableWithName;

	/**
	 * The type the answer (if required) should have.
	 */
	@Getter
	@Setter
	@NonNull
	private AnswerTypes			answerType;

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
	private LString				answerOptionsWithPlaceholders;

	/**
	 * <strong>The unique identifier used for i18n
	 */
	@Getter
	@Setter
	@NonNull
	private String				i18nIdentifier;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		int messageRules = 0;
		val monitoringMessageRules = MC.getInstance()
				.getInterventionAdministrationManagerService()
				.getAllMonitoringMessageRulesOfMonitoringMessage(getId());

		if (monitoringMessageRules != null) {
			val monitoringMessageRulesIterator = monitoringMessageRules
					.iterator();

			while (monitoringMessageRulesIterator.hasNext()) {
				monitoringMessageRulesIterator.next();
				messageRules++;
			}
		}

		final val monitoringMessage = new UIMonitoringMessage(order,
				textWithPlaceholders.toShortenedString(80),
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
				answerType.toString(), storeValueToVariableWithName != null
						? storeValueToVariableWithName : "",
				messageRules);

		monitoringMessage.setRelatedModelObject(this);

		return monitoringMessage;
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

		// Add monitoring message rule
		for (val monitoringMessageRule : ModelObject.find(
				MonitoringMessageRule.class,
				Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE,
				getId())) {
			monitoringMessageRule
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
		val rulesToDelete = ModelObject.find(MonitoringMessageRule.class,
				Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE,
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

		// Monitoring Message Rules
		final StringBuffer buffer = new StringBuffer();
		val rules = ModelObject.findSorted(MonitoringMessageRule.class,
				Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE,
				Queries.MONITORING_MESSAGE_RULE__SORT_BY_ORDER_ASC, getId());

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
