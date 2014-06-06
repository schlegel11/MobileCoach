package ch.ethz.mc.tools;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Creates Ids that have a very, very, very high chance to be unique
 * 
 * @author Andreas Filler
 */
public class GlobalUniqueIdGenerator {
	/**
	 * Creates an Id that has a very, very, very high chance to be unique
	 * 
	 * @return
	 */
	public static String createGlobalUniqueId() {
		final String partOne = String.valueOf(InternalDateTime
				.currentTimeMillis());
		final String partTwo = RandomStringUtils.randomAlphanumeric(200);
		return partOne + "-" + partTwo;
	}
}
