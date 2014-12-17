package ch.ethz.mc.model.persistent;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
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
import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.types.MediaObjectTypes;

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
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
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
