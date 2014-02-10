package org.isgf.mhc.ui;

import lombok.Getter;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;

@SuppressWarnings("serial")
public class NotificationMessageException extends Exception {
	@Getter
	private final AdminMessageStrings	notificationMessage;

	public NotificationMessageException(final AdminMessageStrings notificationMessage) {
		super(Messages.getAdminString(notificationMessage));
		this.notificationMessage = notificationMessage;
	}

}
