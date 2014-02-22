package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;

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
	 * Defines if the {@link MonitoringMessage}s in the group will be sent in
	 * random order or in
	 * the order as they are stored in the {@link MonitoringMessageGroup}
	 */
	@Getter
	@Setter
	private boolean		sendInRandomOrder;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		val monitoringMessagesToDelete = ModelObject.find(
				MonitoringMessage.class,
				Queries.MONITORING_MESSAGES__BY_MONITORING_MESSAGE_GROUP,
				getId());

		ModelObject.delete(monitoringMessagesToDelete);
	}
}
