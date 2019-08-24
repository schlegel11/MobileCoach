package ch.ethz.mc.tools;

/* ##LICENSE## */
import ch.ethz.mc.model.persistent.types.MediaObjectTypes;

/**
 * Small helpers for {@link File}s
 *
 * @author Andreas Filler
 */
public class FileHelpers {
	/**
	 * Returns the appropriate {@link MediaObjectTypes} for the given file
	 * extension
	 * 
	 * @param fileExtension
	 * @return
	 */
	public static MediaObjectTypes getMediaObjectTypeForFileExtension(
			final String fileExtension) {
		if (fileExtension.equals(".png") || fileExtension.equals(".jpg")
				|| fileExtension.equals(".jpeg")
				|| fileExtension.equals(".gif")) {
			return MediaObjectTypes.IMAGE;
		} else if (fileExtension.equals(".mp4")) {
			return MediaObjectTypes.VIDEO;
		} else if (fileExtension.equals(".aac")
				|| fileExtension.equals(".m4a")) {
			return MediaObjectTypes.AUDIO;
		} else if (fileExtension.equals(".htm")
				|| fileExtension.equals(".html")) {
			return MediaObjectTypes.HTML_TEXT;
		} else {
			return null;
		}
	}
}
