package org.isgf.mhc.ui.views.components.basics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.AdminMessageStrings;

import com.vaadin.server.ErrorHandler;
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
public class FileUploadComponentWithController extends FileUploadComponent {

	private UploadListener	listener;

	public FileUploadComponentWithController() {
		super();

		val uploader = new Uploader();
		getUploadComponent().setReceiver(uploader);
		getUploadComponent().addStartedListener(uploader);
		getUploadComponent().addSucceededListener(uploader);
		getUploadComponent().addFailedListener(uploader);
		getUploadComponent().setErrorHandler(uploader);
	}

	public void setListener(final UploadListener listener) {
		this.listener = listener;
	}

	/**
	 * Handles the file upload
	 * 
	 * @author Andreas Filler
	 */
	private class Uploader implements Receiver, StartedListener,
			SucceededListener, FailedListener, ErrorHandler {
		public File	temporaryFile;

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

			if (!temporaryFileExtension.toLowerCase().equals(".mhc")) {
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

			if (listener != null) {
				listener.fileUploadReceived(temporaryFile);
			}

			reset();

			getCancelButton().click();
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

			getAdminUI().setEnabled(true);
		}
	};

	/**
	 * Informs the parent container about the completed file upload
	 * 
	 * @author Andreas Filler
	 */
	public interface UploadListener {
		/**
		 * Informs the parent container about the completed file upload
		 * 
		 * @param mediaObjectId
		 */
		public void fileUploadReceived(final File file);
	}
}