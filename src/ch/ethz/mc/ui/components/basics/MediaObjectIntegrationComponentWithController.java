package ch.ethz.mc.ui.components.basics;

/* ##LICENSE## */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;

import com.vaadin.server.ErrorHandler;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.ThemeImageStrings;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.MediaObject;
import ch.ethz.mc.model.persistent.types.MediaObjectTypes;
import ch.ethz.mc.services.internal.FileStorageManagerService.FILE_STORES;
import ch.ethz.mc.tools.FileHelpers;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.ui.NotificationMessageException;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the media object integration component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MediaObjectIntegrationComponentWithController
		extends MediaObjectIntegrationComponent {

	private MediaObject								mediaObject	= null;
	private MediaObjectCreationOrDeleteionListener	listener;

	public MediaObjectIntegrationComponentWithController() {
		super();

		val buttonClickListener = new ButtonClickListener();
		getSetURLButton().addClickListener(buttonClickListener);
		getCreateHTMLButton().addClickListener(buttonClickListener);
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
			} else if (event.getButton() == getCreateHTMLButton()) {
				createHTML();
			}
		}
	}

	/**
	 * Adjusts the component to the current state
	 */
	private void adjust() {
		if (mediaObject == null) {
			adjustEmbeddedMediaObject(null, null,
					new ThemeResource(ThemeImageStrings.BLANK_MEDIA_OBJECT));

			getUploadComponent().setEnabled(true);
			getSetURLButton().setEnabled(true);
			getCreateHTMLButton().setEnabled(true);
			getDeleteButton().setEnabled(false);
		} else {
			String externalReference;
			if (mediaObject.getFileReference() != null) {
				externalReference = ImplementationConstants.FILE_STREAMING_SERVLET_PATH
						+ "/" + mediaObject.getId() + "/" + StringHelpers
								.cleanFilenameString(mediaObject.getName());
				log.debug("Streaming file {} with file servlet",
						externalReference);
			} else {
				externalReference = mediaObject.getUrlReference();
			}

			adjustEmbeddedMediaObject(mediaObject.getType(),
					mediaObject.getName(),
					new ExternalResource(externalReference));

			getUploadComponent().setEnabled(false);
			getSetURLButton().setEnabled(false);
			getCreateHTMLButton().setEnabled(false);
			getDeleteButton().setEnabled(true);

			// Care for HTML editing
			if (mediaObject.getType() == MediaObjectTypes.HTML_TEXT) {
				val file = getInterventionAdministrationManagerService()
						.mediaObjectGetFile(mediaObject, FILE_STORES.STORAGE);
				try {
					val fileContent = FileUtils.readFileToString(file);
					getTextArea().setValue(fileContent);
					getHtmlLabel().setValue(fileContent);
				} catch (final IOException e) {
					log.error("File could not be read for editing: {}",
							e.getMessage());
				}

				getSaveButton().addClickListener(new ClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							val htmlContent = getTextArea().getValue();

							FileUtils.writeStringToFile(file, htmlContent);

							getSaveButton().setEnabled(true);
							getAdminUI().showInformationNotification(
									AdminMessageStrings.NOTIFICATION__FILE_CHANGES_SAVED);

							getHtmlLabel().setValue(htmlContent);
						} catch (final IOException e) {
							log.error(
									"File could not be written after editing: {}",
									e.getMessage());
						}

					}
				});
			}
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
							createOrUpdateMediaObjectForURL(
									url.toExternalForm());
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

	/**
	 * Creates an empty HTML object
	 */
	private void createHTML() {
		log.debug("Create empty HTML object");

		try {
			val temporaryFile = File.createTempFile("HTML-snippet", ".html");

			createMediaObjectForFile(temporaryFile, "HTML-snippet.html",
					MediaObjectTypes.HTML_TEXT);
		} catch (final IOException e) {
			log.error("Could not create temporary file for HTML snippet: {}",
					e.getMessage());
		}
	}

	/*
	 * Media object creation/deletion for specific types
	 */

	private void createMediaObjectForFile(final File temporaryFile,
			final String originalFileName,
			final MediaObjectTypes originalFileType) {
		log.debug(
				"Create media object for file {} with original file name {} and type {}",
				temporaryFile, originalFileName, originalFileType);

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
		log.debug("Create media object for URL {}", url);

		if (mediaObject != null) {
			val mediaObjectToDelete = mediaObject;
			mediaObject = null;

			listener.updateLinkedMediaObjectId(null);

			adjust();

			getInterventionAdministrationManagerService()
					.mediaObjectDelete(mediaObjectToDelete);
		}

		val newMediaObject = getInterventionAdministrationManagerService()
				.mediaObjectCreateWithURL(url);

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
			val temporaryFileName = filename.replaceAll("[^A-Za-z0-9_. ]+",
					"_");
			if (filename.lastIndexOf(".") > -1) {
				temporaryFileExtension = filename
						.substring(filename.lastIndexOf(".")).toLowerCase();
			} else {
				return null;
			}

			originalFileName = filename;

			originalFileType = FileHelpers
					.getMediaObjectTypeForFileExtension(temporaryFileExtension);
			if (originalFileType == null) {
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

			getAdminUI().showErrorNotification(
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
