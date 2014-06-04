package org.isgf.mhc.tools;

import org.isgf.mhc.conf.ImplementationConstants;

/**
 * Helper class to simulate timestamps in the future
 * 
 * @author Andreas Filler
 */
public class InternalDateTime {
	private static int			hourOffsetCount	= 0;

	private final static long	hourOffset		= ImplementationConstants.HOURS_TO_TIME_IN_MILLIS_MULTIPLICATOR;

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
}
