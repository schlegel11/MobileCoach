package ch.ethz.mc.model.persistent;

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
}