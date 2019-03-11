package ch.ethz.mc.tools;

/* ##LICENSE## */
import org.apache.commons.lang3.RandomStringUtils;

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
		final String partOne = String
				.valueOf(InternalDateTime.currentTimeMillis());
		final String partTwo = RandomStringUtils.randomAlphanumeric(200);
		return partOne + "-" + partTwo;
	}

	/**
	 * Creates an Id that has a very, very high chance to be unique
	 *
	 * @return
	 */
	public static String createSimpleGlobalUniqueId() {
		final String partOne = String
				.valueOf(InternalDateTime.currentTimeMillis());
		final String partTwo = RandomStringUtils.randomAlphanumeric(36);
		return partOne + "-" + partTwo;
	}
}
