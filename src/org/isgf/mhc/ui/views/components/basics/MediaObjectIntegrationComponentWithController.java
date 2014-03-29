package org.isgf.mhc.ui.views.components.basics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.ThemeImageStrings;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.MediaObject;
import org.isgf.mhc.model.server.types.MediaObjectTypes;

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
			getDeleteButton().setEnabled(false);
		} else {
			val externalReference = "files/" + mediaObject.getId() + "/"
					+ mediaObject.getName();
			log.debug("Streaming file {} with file servlet", externalReference);
			adjustEmbeddedMediaObject(mediaObject.getType(),
					mediaObject.getName(), new ExternalResource(
							externalReference));

			getUploadComponent().setEnabled(false);
			getDeleteButton().setEnabled(true);
		}
	}

	public void createMediaObject(final File temporaryFile,
			final String originalFileName,
			final MediaObjectTypes originalFileType) {
		val newMediaObject = getInterventionAdministrationManagerService()
				.mediaObjectCreate(temporaryFile, originalFileName,
						originalFileType);

		if (newMediaObject != null) {
			mediaObject = newMediaObject;
			listener.updateLinkedMediaObjectId(mediaObject.getId());
		}

		adjust();
	}

	public void destroyMediaObject() {
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
					|| temporaryFileExtension.equals(".jpg")) {
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

			createMediaObject(temporaryFile, originalFileName, originalFileType);

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
