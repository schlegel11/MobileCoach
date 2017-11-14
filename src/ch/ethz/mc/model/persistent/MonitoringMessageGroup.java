package ch.ethz.mc.model.persistent;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.model.ui.UIMonitoringMessageGroup;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link ModelObject} to represent an {@link MonitoringMessageGroup}
 *
 * An {@link MonitoringMessageGroup} contains one ore more
 * {@link MonitoringMessage}s that will be sent to {@link Participant}s during
 * an {@link Intervention}.
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringMessageGroup extends ModelObject {
	/**
	 * The {@link Intervention} the {@link MonitoringMessageGroup} belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	intervention;

	/**
	 * The name of the {@link MonitoringMessageGroup} as shown in the backend
	 */
	@Getter
	@Setter
	@NonNull
	private String		name;

	/**
	 * Regular expression to validate result
	 */
	@Getter
	@Setter
	private String		validationExpression;

	/**
	 * The position of the {@link MonitoringMessageGroup} compared to all other
	 * {@link MonitoringMessageGroup}s
	 */
	@Getter
	@Setter
	private int			order;

	/**
	 * Defines if the {@link MonitoringMessage}s in the group will be sent in
	 * random order or in
	 * the order as they are stored in the {@link MonitoringMessageGroup}
	 */
	@Getter
	@Setter
	private boolean		sendInRandomOrder;
	/**
	 * Defines if the message is sent from the same position in this
	 * {@link MonitoringMessageGroup} as the original message if
	 * {@link MonitoringMessage} is sending as reply
	 */
	@Getter
	@Setter
	private boolean		sendSamePositionIfSendingAsReply;

	/**
	 * Defines if the {@link MonitoringMessage}s in the group expect to be
	 * answeres by the {@link Participant}
	 *
	 */
	@Getter
	@Setter
	private boolean		messagesExpectAnswer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		final val monitoringMessageGroup = new UIMonitoringMessageGroup(name,
				messagesExpectAnswer);

		monitoringMessageGroup.setRelatedModelObject(this);

		return monitoringMessageGroup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);

		// Add monitoring message
		for (val monitoringMessage : ModelObject.find(MonitoringMessage.class,
				Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
				getId())) {
			monitoringMessage
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
		val monitoringMessagesToDelete = ModelObject.find(
				MonitoringMessage.class,
				Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
				getId());

		ModelObject.delete(monitoringMessagesToDelete);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.AbstractSerializableTable#toTable()
	 */
	@Override
	@JsonIgnore
	public String toTable() {
		String table = wrapRow(wrapHeader("Name:") + wrapField(escape(name)));
		table += wrapRow(wrapHeader("Expect to be answered:")
				+ wrapField(formatYesNo(messagesExpectAnswer)));
		table += wrapRow(wrapHeader("Send in random order:")
				+ wrapField(formatYesNo(sendInRandomOrder)));
		table += wrapRow(
				wrapHeader("Send from same position if sending as reply:")
						+ wrapField(formatYesNo(sendInRandomOrder)));
		table += wrapRow(wrapHeader("Validation Expression:")
				+ wrapField(escape(validationExpression)));

		// Messages
		final StringBuffer buffer = new StringBuffer();
		val messages = ModelObject.findSorted(MonitoringMessage.class,
				Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
				Queries.MONITORING_MESSAGE__SORT_BY_ORDER_ASC, getId());

		for (val message : messages) {
			buffer.append(message.toTable());
		}

		if (buffer.length() > 0) {
			table += wrapRow(
					wrapHeader("Messages:") + wrapField(buffer.toString()));
		}

		return wrapTable(table);
	}
}
