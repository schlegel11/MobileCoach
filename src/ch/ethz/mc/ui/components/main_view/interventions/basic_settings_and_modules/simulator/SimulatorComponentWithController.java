package ch.ethz.mc.ui.components.main_view.interventions.basic_settings_and_modules.simulator;

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
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.tools.InternalDateTime;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

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
			if (event.getButton() == getNextHourButton()) {
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
