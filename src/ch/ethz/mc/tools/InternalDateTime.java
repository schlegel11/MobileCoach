package ch.ethz.mc.tools;

/* ##LICENSE## */
import java.util.concurrent.TimeUnit;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.ImplementationConstants;
import lombok.extern.log4j.Log4j2;

/**
 * Helper class to simulate timestamps in the future
 *
 * @author Andreas Filler
 */
@Log4j2
public class InternalDateTime {
	private static boolean					fastForwardMode			= false;

	private static FastForwardModeThread	fastForwardModeThread	= null;

	private static int						minutesOffsetCount		= 0;

	private static int						hourOffsetCount			= 0;

	private final static long				minutesOffset			= ImplementationConstants.MINUTES_TO_TIME_IN_MILLIS_MULTIPLICATOR;
	private final static long				hourOffset				= ImplementationConstants.HOURS_TO_TIME_IN_MILLIS_MULTIPLICATOR;

	/**
	 * Returns the current simulated time
	 *
	 * @return
	 */
	public static long currentTimeMillis() {
		return System.currentTimeMillis() + minutesOffset * minutesOffsetCount
				+ hourOffset * hourOffsetCount;
	}

	/**
	 * Simulates a step one hour into the future
	 */
	public static void nextTenMinutes() {
		synchronized (MC.getInstance()) {
			minutesOffsetCount += 10;
		}
	}

	/**
	 * Simulates a step one hour into the future
	 */
	public static void nextHour() {
		synchronized (MC.getInstance()) {
			hourOffsetCount++;
		}
	}

	/**
	 * Simulates a step one day into the future
	 */
	public static void nextDay() {
		synchronized (MC.getInstance()) {
			hourOffsetCount += 24;
		}
	}

	/**
	 * Sets the status of the fast forward mode
	 *
	 * @param active
	 * @throws InterruptedException
	 */
	public static synchronized void setFastForwardMode(
			final boolean newStatus) {
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
