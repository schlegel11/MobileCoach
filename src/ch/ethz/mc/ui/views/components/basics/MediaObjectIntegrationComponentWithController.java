package ch.ethz.mc.ui.views.components.basics;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab at the Health-IS Lab
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.ThemeImageStrings;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.MediaObject;
import ch.ethz.mc.model.persistent.types.MediaObjectTypes;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.ui.NotificationMessageException;

import com.vaadin.server.ErrorHandler;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

/**
 * Extends the media object integration component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MediaObjectIntegrationComponentWithController extends
MediaObjectIntegrationComponent {

	private MediaObject								mediaObject	= null;
	private MediaObjectCreationOrDeleteionListener	listener;

	public MediaObjectIntegrationComponentWithController() {
		super();

		val buttonClickListener = new ButtonClickListener();
		getSetURLButton().addClickListener(buttonClickListener);
		getDeleteButton().addClickListener(buttonClickListener);

		val uploader = new Uploader();
		getUploadComponent().setReceiver(uploader);
		getUploadComponent().addStartedListener(uploader);
		getUploadComponent().addSucceededListener(uploader);
		getUploadComponent().addFailedListener(uploader);
		getUploadComponent().setErrorHandler(uploader);
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getDeleteButton()) {
				destroyMediaObject();
			} else if (event.getButton() == getSetURLButton()) {
				setURL();
			}
		}
	}

	/**
	 * Adjusts the component to the current state
	 */
	private void adjust() {
		if (mediaObject == null) {
			adjustEmbeddedMediaObject(null, null, new ThemeResource(
					ThemeImageStrings.BLANK_MEDIA_OBJECT));

			getUploadComponent().setEnabled(true);
			getSetURLButton().setEnabled(true);
			getDeleteButton().setEnabled(false);
		} else {
			String externalReference;
			if (mediaObject.getFileReference() != null) {
				externalReference = ImplementationConstants.FILE_STREAMING_SERVLET_PATH
						+ "/"
						+ mediaObject.getId()
						+ "/"
						+ StringHelpers.cleanFilenameString(mediaObject
								.getName());
				log.debug("Streaming file {} with file servlet",
						externalReference);
			} else {
				externalReference = mediaObject.getUrlReference();
			}

			adjustEmbeddedMediaObject(mediaObject.getType(),
					mediaObject.getName(), new ExternalResource(
							externalReference));

			getUploadComponent().setEnabled(false);
			getSetURLButton().setEnabled(false);
			getDeleteButton().setEnabled(true);
		}
	}

	/**
	 * Requests and sets a new URL for a URL media object
	 */
	private void setURL() {
		log.debug("Set url for media object");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_EXTERNAL_URL,
				mediaObject == null ? "" : mediaObject.getUrlReference(), null,
						new ShortStringEditComponent(),
						new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change URL
							val url = new URL(getStringValue());
							createOrUpdateMediaObjectForURL(url
									.toExternalForm());
						} catch (final Exception e) {
							log.debug("Given URL cannot be set: {}",
									e.getMessage());
							handleException(new NotificationMessageException(
									AdminMessageStrings.NOTIFICATION__GIVEN_URL_NOT_VALID));
							return;
						}

						// Change was successful
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__URL_SET);
						closeWindow();
					}
				}, null);
	}

	/*
	 * Media object creation/deletion for specific types
	 */

	private void createMediaObjectForFile(final File temporaryFile,
			final String originalFileName,
			final MediaObjectTypes originalFileType) {
		val newMediaObject = getInterventionAdministrationManagerService()
				.mediaObjectCreateWithFile(temporaryFile, originalFileName,
						originalFileType);

		if (newMediaObject != null) {
			mediaObject = newMediaObject;
			listener.updateLinkedMediaObjectId(mediaObject.getId());
		}

		adjust();
	}

	private void createOrUpdateMediaObjectForURL(final String url) {
		if (mediaObject != null) {
			val mediaObjectToDelete = mediaObject;
			mediaObject = null;

			listener.updateLinkedMediaObjectId(null);

			adjust();

			getInterventionAdministrationManagerService().mediaObjectDelete(
					mediaObjectToDelete);
		}

		val newMediaObject = getInterventionAdministrationManagerService()
				.mediaObjectCreateWithURL(url, MediaObjectTypes.URL);

		if (newMediaObject != null) {
			mediaObject = newMediaObject;
			listener.updateLinkedMediaObjectId(mediaObject.getId());
		}

		adjust();
	}

	private void destroyMediaObject() {
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {

				val mediaObjectToDelete = mediaObject;
				mediaObject = null;

				listener.updateLinkedMediaObjectId(null);

				adjust();

				getInterventionAdministrationManagerService()
				.mediaObjectDelete(mediaObjectToDelete);

				closeWindow();
			}
		}, null);
	}

	/**
	 * Set the media object to edit in this component or null if non exists,
	 * yet; A listener is necessary as well, to inform about creation and
	 * deletion of media objects
	 *
	 * @param mediaObject
	 */
	public void setMediaObject(final MediaObject mediaObject,
			final MediaObjectCreationOrDeleteionListener listener) {
		this.mediaObject = mediaObject;
		this.listener = listener;

		adjust();
	}

	/**
	 * Handles the file upload
	 *
	 * @author Andreas Filler
	 */
	private class Uploader implements Receiver, StartedListener,
	SucceededListener, FailedListener, ErrorHandler {
		public File					temporaryFile;
		public String				originalFileName;
		private MediaObjectTypes	originalFileType;

		@Override
		@SneakyThrows(FileNotFoundException.class)
		public OutputStream receiveUpload(final String filename,
				final String mimeType) {

			String temporaryFileExtension = null;
			val temporaryFileName = filename
					.replaceAll("[^A-Za-z0-9_. ]+", "_");
			if (filename.lastIndexOf(".") > -1) {
				temporaryFileExtension = filename.substring(
						filename.lastIndexOf(".")).toLowerCase();
			} else {
				return null;
			}

			originalFileName = filename;

			if (temporaryFileExtension.equals(".png")
					|| temporaryFileExtension.equals(".jpg")
					|| temporaryFileExtension.equals(".jpeg")
					|| temporaryFileExtension.equals(".gif")) {
				originalFileType = MediaObjectTypes.IMAGE;
			} else if (temporaryFileExtension.equals(".mp4")) {
				originalFileType = MediaObjectTypes.VIDEO;
			} else if (temporaryFileExtension.equals(".aac")
					|| temporaryFileExtension.equals(".m4a")) {
				originalFileType = MediaObjectTypes.AUDIO;
			} else {
				return null;
			}

			try {
				temporaryFile = File.createTempFile(temporaryFileName,
						temporaryFileExtension);
				log.debug("Using temporary file {} for upload",
						temporaryFile.getAbsolutePath());
			} catch (final IOException e) {
				return null;
			}

			return new FileOutputStream(temporaryFile);
		}

		@Override
		public void uploadFailed(final FailedEvent event) {
			log.debug("Upload failed");

			reset();

			getAdminUI()
			.showErrorNotification(
					AdminMessageStrings.NOTIFICATION__UPLOAD_FAILED_OR_UNSUPPORTED_FILE_TYPE);
		}

		@Override
		public void uploadSucceeded(final SucceededEvent event) {
			log.debug("Upload succeeded");

			createMediaObjectForFile(temporaryFile, originalFileName,
					originalFileType);

			reset();

			getAdminUI().showInformationNotification(
					AdminMessageStrings.NOTIFICATION__UPLOAD_SUCCESSFUL);
		}

		@Override
		public void uploadStarted(final StartedEvent event) {
			log.debug("Upload started");

			getAdminUI().setEnabled(false);
		}

		@Override
		public void error(final com.vaadin.server.ErrorEvent event) {
			log.debug("Upload threw error");
			// Do nothing
		}

		private void reset() {
			log.debug("Cleaning up");
			if (temporaryFile != null && temporaryFile.exists()) {
				try {
					temporaryFile.delete();
				} catch (final Exception e) {
					// Nothing to do
				}
			}

			temporaryFile = null;
			originalFileName = null;
			originalFileType = null;

			getAdminUI().setEnabled(true);
		}
	};

	/**
	 * Informs the parent {@link ModelObject} about the creation or deletion of
	 * media objects
	 *
	 * @author Andreas Filler
	 */
	public interface MediaObjectCreationOrDeleteionListener {
		/**
		 * Informs the parent {@link ModelObject} about the creation or deletion
		 * of media objects
		 *
		 * @param mediaObjectId
		 */
		public void updateLinkedMediaObjectId(final ObjectId mediaObjectId);
	}
}
