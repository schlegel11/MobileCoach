package org.isgf.mhc.model.server;

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
	private ObjectId	interventionMessageGroup;

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
		final val monitoringMessage = new UIMonitoringMessage(
				textWithPlaceholders,
				linkedMediaObject != null,
				linkedMediaObject == null ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__YES)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NO),
				storeValueToVariableWithName != null ? storeValueToVariableWithName
						: "");

		monitoringMessage.setRelatedModelObject(this);

		return monitoringMessage;
	}
}
