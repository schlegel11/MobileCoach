package org.isgf.mhc.model.persistent;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.persistent.types.MediaObjectTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	/**
	 * The type of the {@link MediaObject}
	 */
	@Getter
	@Setter
	@NonNull
	private MediaObjectTypes	type;

	/**
	 * The file name shown to the user
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
	@NonNull
	private String				fileReference;

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
				temporaryFileToStoreAndReference);

		if (fileReference == null) {
			throw new Exception("File cannot be stored");
		}

		setType(type);
		setName(name);
		setFileReference(fileReference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#performOnDelete()
	 */
	@Override
	@JsonIgnore
	protected void performOnDelete() {
		log.debug("Deleting file with reference {}", fileReference);
		try {
			getFileStorageManagerService().deleteFile(fileReference);
		} catch (final Exception e) {
			log.warn(
					"File belonging to file reference {} could not be deleted: {}",
					fileReference, e.getMessage());
		}

		super.performOnDelete();
	}
}
