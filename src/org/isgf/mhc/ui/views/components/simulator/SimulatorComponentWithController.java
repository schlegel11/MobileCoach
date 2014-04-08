package org.isgf.mhc.ui.views.components.simulator;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.ui.UISimulatedMessage;
import org.isgf.mhc.tools.InternalDateTime;
import org.isgf.mhc.tools.Simulator;
import org.isgf.mhc.tools.Simulator.SimulatorListener;

import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the simulator component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class SimulatorComponentWithController extends SimulatorComponent
		implements SimulatorListener {

	private int													lastMessage	= 0;

	private final BeanContainer<Integer, UISimulatedMessage>	beanContainer;

	private final DateFormat									dateFormat	= DateFormat
																					.getDateTimeInstance(
																							DateFormat.MEDIUM,
																							DateFormat.MEDIUM,
																							Constants
																									.getAdminLocale());

	public SimulatorComponentWithController() {
		super();

		// table options
		val messagesTable = getMessagesTable();
		messagesTable.setReadOnly(true);
		messagesTable.setImmediate(true);

		// table content
		beanContainer = new BeanContainer<Integer, UISimulatedMessage>(
				UISimulatedMessage.class);

		messagesTable.setContainerDataSource(beanContainer);
		messagesTable.setSortContainerPropertyId(UISimulatedMessage
				.getSortColumn());
		messagesTable.setVisibleColumns(UISimulatedMessage.getVisibleColumns());
		messagesTable.setColumnHeaders(UISimulatedMessage.getColumnHeaders());

		// handle time label
		val timeUpdateThread = new Thread("Time Update Thread") {

			@Override
			public void run() {
				while (!isInterrupted()) {
					log.debug("Updating time...");
					try {
						updateTime();
					} catch (final Exception e) {
						return;
					}

					try {
						TimeUnit.SECONDS
								.sleep(ImplementationContants.SIMULATOR_TIME_UPDATE_INTERVAL_IN_SECONDS);
					} catch (final InterruptedException e) {
						interrupt();
					}
				}
			}
		};
		timeUpdateThread.start();

		// handle buttons
		val instance = this;
		val buttonClickListener = new ButtonClickListener();
		getSendSimulatedMessageButton().addClickListener(buttonClickListener);
		getNextHourButton().addClickListener(buttonClickListener);

		if (Constants.isSimulatedDateAndTime()) {
			Simulator.getInstance().registerSimulatorListener(this);

			addDetachListener(new DetachListener() {

				@Override
				public void detach(final DetachEvent event) {
					try {
						synchronized (timeUpdateThread) {
							timeUpdateThread.interrupt();
							timeUpdateThread.join();
						}
					} catch (final InterruptedException e) {
						// do nothing
					}

					Simulator.getInstance().removeSimulatorListener(instance);
				}
			});
		}
	}

	@Synchronized
	protected void updateTime() {
		getCurrentTimeLabel()
				.setValue(
						Messages.getAdminString(
								AdminMessageStrings.SIMULATOR_COMPONENT__THE_CURRENT_SIMULATED_TIME_IS_X,
								dateFormat.format(new Date(InternalDateTime
										.currentTimeMillis()))));
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getSendSimulatedMessageButton()) {
				sendMessage();
			} else if (event.getButton() == getNextHourButton()) {
				jumpToNextHour();
			}
		}
	}

	public void jumpToNextHour() {
		log.debug("Set time to one hour in the future...");
		InternalDateTime.nextHour();

		updateTime();
	}

	public void sendMessage() {
		// TODO Auto-generated method stub

	}

	@Override
	public void newSimulatedMessageFromSystem(final String message) {
		addMessageToTable(true, message);
	}

	private void addMessageToTable(final boolean isSystemMessage,
			final String message) {
		log.debug("Adding message to table");
		val uiSimulatedMessage = new UISimulatedMessage(
				new Date(InternalDateTime.currentTimeMillis()),
				isSystemMessage ? Messages
						.getAdminString(AdminMessageStrings.SIMULATOR_COMPONENT__SYSTEM)
						: Messages
								.getAdminString(AdminMessageStrings.SIMULATOR_COMPONENT__PARTICIPANT),
				message);

		synchronized (beanContainer) {
			beanContainer.addItem(lastMessage++, uiSimulatedMessage);
		}
	}
}
