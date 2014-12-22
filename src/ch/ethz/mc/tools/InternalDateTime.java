package ch.ethz.mc.tools;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.ImplementationConstants;

/**
 * Helper class to simulate timestamps in the future
 * 
 * @author Andreas Filler
 */
@Log4j2
public class InternalDateTime {
	private static boolean					fastForwardMode			= false;

	private static FastForwardModeThread	fastForwardModeThread	= null;

	private static int						hourOffsetCount			= 0;

	private final static long				hourOffset				= ImplementationConstants.HOURS_TO_TIME_IN_MILLIS_MULTIPLICATOR;

	/**
	 * Returns the current simulated time
	 * 
	 * @return
	 */
	public static long currentTimeMillis() {
		return System.currentTimeMillis() + hourOffset * hourOffsetCount;
	}

	/**
	 * Simulates a step one hour into the future
	 */
	public static void nextHour() {
		hourOffsetCount++;
	}

	/**
	 * Sets the status of the fast forward mode
	 * 
	 * @param active
	 * @throws InterruptedException
	 */
	public static synchronized void setFastForwardMode(final boolean newStatus) {
		fastForwardMode = newStatus;

		if (newStatus && fastForwardModeThread == null) {
			log.debug("Starting fast forward mode...");

			fastForwardModeThread = new FastForwardModeThread();
			fastForwardModeThread.start();
		}

		if (!newStatus && fastForwardModeThread != null) {
			log.debug("Stopping fast forward mode...");
			synchronized (fastForwardModeThread) {
				fastForwardModeThread.interrupt();
				try {
					fastForwardModeThread.join();
				} catch (final InterruptedException e) {
					// Not relevant
				}
				fastForwardModeThread = null;
			}
		}
	}

	/**
	 * Informs about the status of the fast forward mode
	 * 
	 * @return
	 */
	public static boolean isFastForwardMode() {
		return fastForwardMode;
	}

	private static class FastForwardModeThread extends Thread {
		@Override
		public void run() {
			log.debug("Fast forward thread started.");

			while (!isInterrupted()) {
				nextHour();

				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (final InterruptedException e) {
					interrupt();
				}
			}

			log.debug("Fast forward thread stopped.");
		}
	}
}
