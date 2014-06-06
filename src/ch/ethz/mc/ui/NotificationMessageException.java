package ch.ethz.mc.ui;

import lombok.Getter;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;

@SuppressWarnings("serial")
public class NotificationMessageException extends Exception {
	@Getter
	private final AdminMessageStrings	notificationMessage;

	public NotificationMessageException(final AdminMessageStrings notificationMessage) {
		super(Messages.getAdminString(notificationMessage));
		this.notificationMessage = notificationMessage;
	}

}
