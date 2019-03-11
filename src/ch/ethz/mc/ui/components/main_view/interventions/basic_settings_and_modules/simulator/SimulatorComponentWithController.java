package ch.ethz.mc.ui.components.main_view.interventions.basic_settings_and_modules.simulator;

/* ##LICENSE## */
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.tools.InternalDateTime;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the simulator component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class SimulatorComponentWithController extends SimulatorComponent {

	private final DateFormat dateFormat;

	public SimulatorComponentWithController() {
		super();

		// init date format
		dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.MEDIUM, Constants.getAdminLocale());

		// handle time label
		val timeUpdateThread = new Thread("Time Update Thread") {

			@Override
			public void run() {
				while (!isInterrupted()) {
					try {
						updateTime();
					} catch (final Exception e) {
						return;
					}

					try {
						TimeUnit.SECONDS.sleep(
								ImplementationConstants.SIMULATOR_TIME_UPDATE_INTERVAL_IN_SECONDS);
					} catch (final InterruptedException e) {
						interrupt();
					}
				}
			}
		};
		timeUpdateThread.start();

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getActivateFastForwadModeButton().addClickListener(buttonClickListener);
		getDeactivateFastForwardModeButton()
				.addClickListener(buttonClickListener);
		getNextTenMinuesButton().addClickListener(buttonClickListener);
		getNextHourButton().addClickListener(buttonClickListener);
		getNextDayButton().addClickListener(buttonClickListener);

		if (Constants.isSimulatedDateAndTime()) {
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
				}
			});
		}
	}

	@Synchronized
	protected void updateTime() {
		getCurrentTimeLabel().setValue(Messages.getAdminString(
				AdminMessageStrings.SIMULATOR_COMPONENT__THE_CURRENT_SIMULATED_TIME_IS_X,
				dateFormat
						.format(new Date(InternalDateTime.currentTimeMillis())),
				InternalDateTime.isFastForwardMode()));

		getAdminUI().push();
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getNextTenMinuesButton()) {
				jumpToNextTenMinutes();
			} else if (event.getButton() == getNextHourButton()) {
				jumpToNextHour();
			} else if (event.getButton() == getNextDayButton()) {
				jumpToNextDay();
			} else if (event.getButton() == getActivateFastForwadModeButton()) {
				setFastForwardMode(true);
			} else if (event
					.getButton() == getDeactivateFastForwardModeButton()) {
				setFastForwardMode(false);
			}
		}
	}

	public void jumpToNextTenMinutes() {
		log.debug("Set time to ten minutes in the future...");
		InternalDateTime.nextTenMinutes();

		updateTime();
	}

	public void jumpToNextHour() {
		log.debug("Set time to one hour in the future...");
		InternalDateTime.nextHour();

		updateTime();
	}

	public void jumpToNextDay() {
		log.debug("Set time to one day in the future...");
		InternalDateTime.nextDay();

		updateTime();
	}

	public void setFastForwardMode(final boolean active) {
		log.debug("Set fast forward mode {}", active ? "active" : "inactive");
		InternalDateTime.setFastForwardMode(active);

		updateTime();
	}
}
