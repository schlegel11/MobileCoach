package ch.ethz.mc.tools;

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
