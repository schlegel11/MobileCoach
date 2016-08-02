package ch.ethz.mc.model.persistent;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.model.ui.UIMonitoringMessage;

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
	private LString		textWithPlaceholders;

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
	 * <strong>OPTIONAL:</strong> The intermediate {@link ScreeningSurvey}
	 * used/presented in this {@link MonitoringMessage}
	 */
	@Getter
	@Setter
	private ObjectId	linkedIntermediateSurvey;

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
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		int messageRules = 0;
		val monitoringRules = MC.getInstance()
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
				textWithPlaceholders.toShortenedString(80),
				linkedMediaObject != null ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__YES)
						: Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NO),
				linkedIntermediateSurvey != null ? Messages
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
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	public void collectThisAndRelatedModelObjectsForExport(
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
		val rulesToDelete = ModelObject
				.find(MonitoringMessageRule.class,
						Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE,
						getId());
		ModelObject.delete(rulesToDelete);
	}

}
