package ch.ethz.mc.ui.components.main_view.interventions;

import java.io.File;
/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.bson.types.ObjectId;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.memory.I18nStringsObject;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.services.InterventionAdministrationManagerService;
import ch.ethz.mc.tools.CSVExporter;
import ch.ethz.mc.tools.CSVImporter;
import ch.ethz.mc.tools.OnDemandFileDownloader;
import ch.ethz.mc.tools.OnDemandFileDownloader.OnDemandStreamResource;
import ch.ethz.mc.ui.components.basics.FileUploadComponentWithController;
import ch.ethz.mc.ui.components.basics.FileUploadComponentWithController.UploadListener;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the intervention i18n component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class InterventionI18nComponentenWithController
		extends InterventionI18nComponent {

	private final InterventionAdministrationManagerService ias;

	public InterventionI18nComponentenWithController(
			final Intervention intervention) {
		super();

		ias = getInterventionAdministrationManagerService();

		// Handle buttons
		val buttonClickListener = new ButtonClickListener();
		getImportButton().addClickListener(buttonClickListener);

		// Special handle for export buttons
		val allDataExportOnDemandFileDownloader = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					public InputStream getStream() {
						val i18nStringsObjectList = new ArrayList<I18nStringsObject>();

						// Dialog Messages
						val monitoringMessageGroups = ias
								.getAllMonitoringMessageGroupsOfIntervention(
										intervention.getId());

						for (val monitoringMessageGroup : monitoringMessageGroups) {
							val monitoringMessages = ias
									.getAllMonitoringMessagesOfMonitoringMessageGroup(
											monitoringMessageGroup.getId());

							for (val monitoringMessage : monitoringMessages) {
								val i18nStringsObject = new I18nStringsObject();
								i18nStringsObject
										.setId("mm-" + monitoringMessage.getId()
												.toHexString() + "-#");
								i18nStringsObject.setDescription(
										monitoringMessageGroup.getName());

								i18nStringsObject.setText(monitoringMessage
										.getTextWithPlaceholders());
								i18nStringsObject
										.setAnswerOptions(monitoringMessage
												.getAnswerOptionsWithPlaceholders());

								i18nStringsObjectList.add(i18nStringsObject);
							}
						}

						// Micro Dialogs
						val microDialogs = ias.getAllMicroDialogsOfIntervention(
								intervention.getId());

						for (val microDialog : microDialogs) {
							val microDialogMessages = ias
									.getAllMicroDialogMessagesOfMicroDialog(
											microDialog.getId());

							for (val microDialogMessage : microDialogMessages) {
								val i18nStringsObject = new I18nStringsObject();
								i18nStringsObject
										.setId("dm-" + microDialogMessage
												.getId().toHexString() + "-#");
								i18nStringsObject
										.setDescription(microDialog.getName());

								i18nStringsObject.setText(microDialogMessage
										.getTextWithPlaceholders());
								i18nStringsObject
										.setAnswerOptions(microDialogMessage
												.getAnswerOptionsWithPlaceholders());

								i18nStringsObjectList.add(i18nStringsObject);
							}
						}

						try {
							log.info("Converting table to CSV...");
							return CSVExporter.convertI18nStringsObjectsToCSV(
									i18nStringsObjectList);
						} catch (final IOException e) {
							log.error("Error at creating CSV: {}",
									e.getMessage());
						} finally {
							getExportButton().setEnabled(true);
						}

						return null;
					}

					@Override
					public String getFilename() {
						return "Internationalization_Strings_" + intervention
								.getName().replaceAll("[^A-Za-z0-9_. ]+", "_")
								+ ".csv";
					}
				});
		allDataExportOnDemandFileDownloader.extend(getExportButton());
		getExportButton().setDisableOnClick(true);
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {

			if (event.getButton() == getImportButton()) {
				importI18nCSV();
			}
		}
	}

	public void importI18nCSV() {
		log.debug("Import i18n CSV");

		val fileUploadComponentWithController = new FileUploadComponentWithController(
				".csv");
		fileUploadComponentWithController.setListener(new UploadListener() {
			@Override
			public void fileUploadReceived(final File file) {
				log.debug(
						"File upload sucessful, starting conversation of i18n string objects");
				try {
					val i18nStringObjects = CSVImporter
							.convertCSVToI18nStringsObjects(file);

					for (val i18nStringObject : i18nStringObjects) {
						val typeAndId = i18nStringObject.getId().split("-");
						val type = typeAndId[0];
						val id = typeAndId[1];
						val objectId = new ObjectId(id);

						switch (type) {
							case "mm":
								ias.monitoringMessageUpdateL18n(objectId,
										i18nStringObject.getText(),
										i18nStringObject.getAnswerOptions());
								break;
							case "dm":
								ias.microDialogMessageUpdateL18n(objectId,
										i18nStringObject.getText(),
										i18nStringObject.getAnswerOptions());
								break;
						}
					}

					getAdminUI().showInformationNotification(
							AdminMessageStrings.NOTIFICATION__L18N_IMPORTED);
				} catch (final Exception e) {
					getAdminUI().showWarningNotification(
							AdminMessageStrings.NOTIFICATION__L18N_IMPORT_FAILED);
				} finally {
					try {
						file.delete();
					} catch (final Exception f) {
						// Do nothing
					}
				}
			}
		});
		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__IMPORT_L18N,
				fileUploadComponentWithController, null);
	}

}