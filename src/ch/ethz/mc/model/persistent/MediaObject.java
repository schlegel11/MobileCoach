package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import java.io.File;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.types.MediaObjectTypes;
import ch.ethz.mc.services.internal.FileStorageManagerService.FILE_STORES;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * {@link ModelObject} to represent an {@link MediaObject}
 *
 * {@link MediaObject}s represent media files (e.g. images or videos) that are
 * integrated in messages. They consist of a type description and a file
 * reference.
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class MediaObject extends ModelObject {
	private static final long	serialVersionUID	= -1319450594327240988L;

	/**
	 * The type of the {@link MediaObject}
	 */
	@Getter
	@Setter
	@NonNull
	private MediaObjectTypes	type;

	/**
	 * The file name shown to the backend user
	 */
	@Getter
	@Setter
	@NonNull
	private String				name;

	/**
	 * The reference to the file on the server
	 */
	@Getter
	@Setter
	private String				fileReference;

	/**
	 * The reference to the URL
	 */
	@Getter
	@Setter
	private String				urlReference;

	/**
	 * Alternative constructor to combine the creation of the file reference and
	 * the creation of the {@link MediaObject}
	 *
	 * @param type
	 *            The type of the {@link MediaObject}
	 * @param name
	 *            The file name shown to the user
	 * @param temporaryFileToStoreAndReference
	 *            The file to create a reference from that will be saved in the
	 *            {@link MediaObject}
	 * @throws Exception
	 *             If file can not be stored for any reason
	 */
	public MediaObject(final MediaObjectTypes type, final String name,
			final File temporaryFileToStoreAndReference) throws Exception {

		val fileReference = getFileStorageManagerService().storeFile(
				temporaryFileToStoreAndReference, FILE_STORES.STORAGE);

		if (fileReference == null) {
			throw new Exception("File cannot be stored");
		}

		setType(type);
		setName(name);
		setFileReference(fileReference);
	}

	/**
	 * Returns <code>true</code> when type is text based
	 * 
	 * @return
	 */
	@JsonIgnore
	public boolean isTextBased() {
		switch (type) {
			case HTML_TEXT:
			case URL:
				return true;
			case AUDIO:
			case IMAGE:
			case VIDEO:
				break;
		}

		return false;
	}

	/**
	 * Returns <code>true</code> when type is file based
	 * 
	 * @return
	 */
	@JsonIgnore
	public boolean isFileBased() {
		switch (type) {
			case HTML_TEXT:
			case AUDIO:
			case IMAGE:
			case VIDEO:
				return true;
			case URL:
				break;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
	 */
	@Override
	@JsonIgnore
	protected void performOnDelete() {
		if (fileReference != null) {
			log.debug("Deleting file with reference {}", fileReference);
			try {
				getFileStorageManagerService().deleteFile(fileReference,
						FILE_STORES.STORAGE);
			} catch (final Exception e) {
				log.warn(
						"File belonging to file reference {} could not be deleted: {}",
						fileReference, e.getMessage());
			}
		}

		// Delete media object participant short URLs
		val mediaObjectParticipantShortURLsToDelete = ModelObject.find(
				MediaObjectParticipantShortURL.class,
				Queries.MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_MEDIA_OBJECT,
				getId());

		ModelObject.delete(mediaObjectParticipantShortURLsToDelete);
	}
}
