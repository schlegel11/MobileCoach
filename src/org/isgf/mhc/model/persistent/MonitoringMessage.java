package org.isgf.mhc.model.persistent;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.bson.types.ObjectId;
import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.ui.UIModelObject;
import org.isgf.mhc.model.ui.UIMonitoringMessage;

/**
 * {@link ModelObject} to represent an {@link MonitoringMessage}
 * 
 * {@link MonitoringMessage}s will be sent to the {@link Participant} during
 * an {@link Intervention}. {@link MonitoringMessage}s are grouped in
 * {@link MonitoringMessageGroup}s.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringMessage extends ModelObject {
	/**
	 * The {@link MonitoringMessageGroup} this {@link MonitoringMessage} belongs
	 * to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	monitoringMessageGroup;

	/**
	 * The message text containing placeholders for variables
	 */
	@Getter
	@Setter
	@NonNull
	private String		textWithPlaceholders;

	/**
	 * The position of the {@link MonitoringMessage} compared to all other
	 * {@link MonitoringMessage}s in the same {@link MonitoringMessageGroup}
	 */
	@Getter
	@Setter
	private int			order;

	/**
	 * <strong>OPTIONAL:</strong> The {@link MediaObject} used/presented in this
	 * {@link MonitoringMessage}
	 */
	@Getter
	@Setter
	private ObjectId	linkedMediaObject;

	/**
	 * <strong>OPTIONAL:</strong> If the result of the {@link MonitoringMessage}
	 * should be
	 * stored, the name of the appropriate variable can be set here.
	 */
	@Getter
	@Setter
	private String		storeValueToVariableWithName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		int messageRules = 0;
		val monitoringRules = MHC.getInstance()
				.getInterventionAdministrationManagerService()
				.getAllMonitoringMessageRulesOfMonitoringMessage(getId());

		if (monitoringRules != null) {
			val screeningSurveySlideRulesIterator = monitoringRules.iterator();

			while (screeningSurveySlideRulesIterator.hasNext()) {
				screeningSurveySlideRulesIterator.next();
				messageRules++;
			}
		}

		final val monitoringMessage = new UIMonitoringMessage(
				order,
				textWithPlaceholders.length() > 160 ? textWithPlaceholders
						.substring(0, 160) + "..." : textWithPlaceholders,
				linkedMediaObject != null,
				linkedMediaObject != null ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__YES)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NO),
				storeValueToVariableWithName != null ? storeValueToVariableWithName
						: "", messageRules);

		monitoringMessage.setRelatedModelObject(this);

		return monitoringMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.isgf.mhc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);

		// Linked media object
		if (linkedMediaObject != null) {
			exportList.add(ModelObject
					.get(MediaObject.class, linkedMediaObject));
		}

		// Add monitoring message rule
		for (val monitoringMessageRule : ModelObject
				.find(MonitoringMessageRule.class,
						Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE,
						getId())) {
			monitoringMessageRule
					.collectThisAndRelatedModelObjectsForExport(exportList);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#performOnDelete()
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
		val rulesToDelete = ModelObject
				.find(MonitoringMessageRule.class,
						Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE,
						getId());
		ModelObject.delete(rulesToDelete);
	}

}
