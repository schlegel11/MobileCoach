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

import org.apache.commons.lang3.StringUtils;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.memory.MessagesDialogsI18nStringsObject;
import ch.ethz.mc.model.memory.SurveysFeedbacksI18nStringsObject;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MicroDialogDecisionPoint;
import ch.ethz.mc.model.persistent.MicroDialogMessage;
import ch.ethz.mc.services.InterventionAdministrationManagerService;
import ch.ethz.mc.services.SurveyAdministrationManagerService;
import ch.ethz.mc.tools.CSVExporter;
import ch.ethz.mc.tools.CSVImporter;
import ch.ethz.mc.tools.OnDemandFileDownloader;
import ch.ethz.mc.tools.OnDemandFileDownloader.OnDemandStreamResource;
import ch.ethz.mc.tools.StringHelpers;
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

	private final InterventionAdministrationManagerService	ias;
	private final SurveyAdministrationManagerService		sas;

	private Intervention									intervention;

	public InterventionI18nComponentenWithController(
			final Intervention intervention) {
		super();

		ias = getInterventionAdministrationManagerService();
		sas = getSurveyAdministrationManagerService();
		this.intervention = intervention;

		// Handle buttons
		val buttonClickListener = new ButtonClickListener();
		getImportMessagesDialogsButton().addClickListener(buttonClickListener);
		getImportSurveysFeedbacksButton().addClickListener(buttonClickListener);

		// Special handle for export buttons
		val exportMessagesAndDialogsOnDemandFileDownloader = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					public InputStream getStream() {
						val i18nStringsObjectList = new ArrayList<MessagesDialogsI18nStringsObject>();

						// Monitoring Messages
						val monitoringMessageGroups = ias
								.getAllMonitoringMessageGroupsOfIntervention(
										intervention.getId());

						for (val monitoringMessageGroup : monitoringMessageGroups) {
							val monitoringMessages = ias
									.getAllMonitoringMessagesOfMonitoringMessageGroup(
											monitoringMessageGroup.getId());

							for (val monitoringMessage : monitoringMessages) {
								val i18nStringsObject = new MessagesDialogsI18nStringsObject();
								i18nStringsObject.setId("mm_"
										+ monitoringMessage.getI18nIdentifier()
										+ "_#");
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
							val microDialogElements = ias
									.getAllMicroDialogElementsOfMicroDialog(
											microDialog.getId());

							for (val microDialogElement : microDialogElements) {
								if (microDialogElement instanceof MicroDialogMessage) {
									// Micro dialog message
									val microDialogMessage = (MicroDialogMessage) microDialogElement;

									val i18nStringsObject = new MessagesDialogsI18nStringsObject();
									i18nStringsObject.setId("dm_"
											+ microDialogMessage
													.getI18nIdentifier()
											+ "_#");

									final StringBuffer constraints = new StringBuffer();
									val microDialogMessageRules = ias
											.getAllMicroDialogMessageRulesOfMicroDialogMessage(
													microDialogMessage.getId());

									for (val microDialogMessageRule : microDialogMessageRules) {
										if (constraints.length() > 0) {
											constraints.append(" and ");
										}

										constraints.append("["
												+ StringHelpers.createRuleName(
														microDialogMessageRule,
														true)
												+ "]");
									}

									i18nStringsObject.setDescription(
											microDialog.getName()
													+ (constraints.length() > 0
															? " only when "
																	+ constraints
																			.toString()
															: ""));

									i18nStringsObject.setText(microDialogMessage
											.getTextWithPlaceholders());
									i18nStringsObject
											.setAnswerOptions(microDialogMessage
													.getAnswerOptionsWithPlaceholders());

									i18nStringsObjectList
											.add(i18nStringsObject);
								} else if (microDialogElement instanceof MicroDialogDecisionPoint) {
									// Micro dialog decision point
									val microDialogDecisionPoint = (MicroDialogDecisionPoint) microDialogElement;

									val i18nStringsObject = new MessagesDialogsI18nStringsObject();
									i18nStringsObject.setId("dp");
									i18nStringsObject.setDescription(microDialog
											.getName()
											+ " Decision Point"
											+ (!StringUtils.isBlank(
													microDialogDecisionPoint
															.getComment())
																	? " [" + microDialogDecisionPoint
																			.getComment()
																			+ "]"
																	: ""));

									i18nStringsObjectList
											.add(i18nStringsObject);
								}

							}
						}

						try {
							log.info(
									"Converting messages and dialogs to CSV...");
							return CSVExporter
									.convertMessagesDialogsI18nStringsObjectsToCSV(
											i18nStringsObjectList);
						} catch (final IOException e) {
							log.error("Error at creating CSV: {}",
									e.getMessage());
						} finally {
							getExportMessagesDialogsButton().setEnabled(true);
						}

						return null;
					}

					@Override
					public String getFilename() {
						return "Internationalization_Strings_Messages_and_Dialogs_"
								+ StringHelpers.cleanFilenameString(
										intervention.getName())
								+ ".csv";
					}
				});
		exportMessagesAndDialogsOnDemandFileDownloader
				.extend(getExportMessagesDialogsButton());
		getExportMessagesDialogsButton().setDisableOnClick(true);

		val exportSurveysAndFeedbacksOnDemandFileDownloader = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					public InputStream getStream() {
						val i18nStringsObjectList = new ArrayList<SurveysFeedbacksI18nStringsObject>();

						// Surveys
						val screeningSurveys = sas
								.getAllScreeningSurveysOfIntervention(
										intervention.getId());

						for (val screeningSurvey : screeningSurveys) {
							SurveysFeedbacksI18nStringsObject i18nStringsObject = new SurveysFeedbacksI18nStringsObject();
							i18nStringsObject.setId(
									"su_" + screeningSurvey.getI18nIdentifier()
											+ "_#");
							i18nStringsObject.setDescription(
									screeningSurvey.isIntermediateSurvey()
											? "Intermediate Survey"
											: "Screening Survey");
							i18nStringsObject
									.setTitle(screeningSurvey.getName());

							i18nStringsObjectList.add(i18nStringsObject);

							// Survey Slides
							val surveySlides = sas
									.getAllScreeningSurveySlidesOfScreeningSurvey(
											screeningSurvey.getId());

							for (val surveySlide : surveySlides) {
								i18nStringsObject = new SurveysFeedbacksI18nStringsObject();
								i18nStringsObject.setId(
										"ss_" + surveySlide.getI18nIdentifier()
												+ "_#");
								i18nStringsObject.setDescription("Survey Slide "
										+ surveySlide.getComment());
								i18nStringsObject.setTitle(
										surveySlide.getTitleWithPlaceholders());
								i18nStringsObject.setErrorMessage(surveySlide
										.getValidationErrorMessage());

								i18nStringsObjectList.add(i18nStringsObject);

								val questions = surveySlide.getQuestions();
								for (int i = 0; i < questions.size(); i++) {
									val question = questions.get(i);

									i18nStringsObject = new SurveysFeedbacksI18nStringsObject();
									i18nStringsObject.setId("sq_"
											+ surveySlide.getI18nIdentifier()
											+ "." + i + "_#");
									i18nStringsObject.setDescription("Question "
											+ (i + 1) + " of Survey Slide "
											+ surveySlide.getComment());
									i18nStringsObject.setText(question
											.getQuestionWithPlaceholders());

									i18nStringsObjectList
											.add(i18nStringsObject);

									val answers = question
											.getAnswersWithPlaceholders();

									for (int j = 0; j < answers.length; j++) {
										val answer = answers[j];

										i18nStringsObject = new SurveysFeedbacksI18nStringsObject();
										i18nStringsObject.setId("qa_"
												+ surveySlide
														.getI18nIdentifier()
												+ "." + i + "." + j + "_#");
										i18nStringsObject.setDescription(
												"Answer " + (j + 1)
														+ " of Question "
														+ (i + 1)
														+ " of Survey Slide "
														+ surveySlide
																.getComment());
										i18nStringsObject.setText(answer);

										i18nStringsObjectList
												.add(i18nStringsObject);
									}
								}

							}

							// Feedbacks
							val feedbacks = sas
									.getAllFeedbacksOfScreeningSurvey(
											screeningSurvey.getId());

							for (val feedback : feedbacks) {
								i18nStringsObject = new SurveysFeedbacksI18nStringsObject();
								i18nStringsObject.setId("fb_"
										+ feedback.getI18nIdentifier() + "_#");
								i18nStringsObject.setDescription("Feedback");
								i18nStringsObject.setTitle(feedback.getName());

								i18nStringsObjectList.add(i18nStringsObject);

								// Feedback Slides
								val feedbackSlides = sas
										.getAllFeedbackSlidesOfFeedback(
												feedback.getId());

								for (val feedbackSlide : feedbackSlides) {
									i18nStringsObject = new SurveysFeedbacksI18nStringsObject();
									i18nStringsObject.setId("fs_"
											+ feedbackSlide.getI18nIdentifier()
											+ "_#");
									i18nStringsObject.setDescription(
											"Feedback Slide " + feedbackSlide
													.getComment());
									i18nStringsObject.setTitle(feedbackSlide
											.getTitleWithPlaceholders());
									i18nStringsObject.setText(feedbackSlide
											.getTextWithPlaceholders());

									i18nStringsObjectList
											.add(i18nStringsObject);
								}
							}
						}

						try {
							log.info(
									"Converting surveys and feedbacks to CSV...");
							return CSVExporter
									.convertSurveysFeedbacksI18nStringsObjectsToCSV(
											i18nStringsObjectList);
						} catch (final IOException e) {
							log.error("Error at creating CSV: {}",
									e.getMessage());
						} finally {
							getExportSurveysFeedbacksButton().setEnabled(true);
						}

						return null;
					}

					@Override
					public String getFilename() {
						return "Internationalization_Strings_Surveys_and_Feedbacks_"
								+ StringHelpers.cleanFilenameString(
										intervention.getName())
								+ ".csv";
					}
				});
		exportSurveysAndFeedbacksOnDemandFileDownloader
				.extend(getExportSurveysFeedbacksButton());
		getExportSurveysFeedbacksButton().setDisableOnClick(true);
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {

			if (event.getButton() == getImportMessagesDialogsButton()) {
				importMessagesDialogsI18nCSV();
			} else if (event.getButton() == getImportSurveysFeedbacksButton()) {
				importSurveysFeedbacksI18nCSV();
			}
		}
	}

	public void importMessagesDialogsI18nCSV() {
		log.debug("Import i18n CSV");

		val fileUploadComponentWithController = new FileUploadComponentWithController(
				".csv");
		fileUploadComponentWithController.setListener(new UploadListener() {
			@Override
			public void fileUploadReceived(final File file) {
				log.debug(
						"File upload successful, starting conversation of i18n string objects for messages and dialogs");
				try {
					val i18nStringObjects = CSVImporter
							.convertCSVToI18nMessagesDialogsStringsObjects(
									file);

					int updates = 0;

					log.debug("{} objects found in i18n import",
							i18nStringObjects.size());

					for (val i18nStringObject : i18nStringObjects) {
						val typeAndId = i18nStringObject.getId().split("_");
						val type = typeAndId[0];
						val i18nIdentifier = typeAndId[1];
						val check = typeAndId[2];

						if (check.equals("#")) {
							switch (type) {
								case "mm":
									updates += ias.monitoringMessageUpdateL18n(
											intervention.getId(),
											i18nIdentifier,
											i18nStringObject.getText(),
											i18nStringObject
													.getAnswerOptions());
									break;
								case "dm":
									updates += ias.microDialogMessageUpdateL18n(
											intervention.getId(),
											i18nIdentifier,
											i18nStringObject.getText(),
											i18nStringObject
													.getAnswerOptions());
									break;
							}
						}
					}

					log.debug("{} objects updated", updates);
					getAdminUI().showInformationNotification(
							AdminMessageStrings.NOTIFICATION__I18N_IMPORTED);
				} catch (final Exception e) {
					getAdminUI().showWarningNotification(
							AdminMessageStrings.NOTIFICATION__I18N_IMPORT_FAILED);
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
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__IMPORT_I18N,
				fileUploadComponentWithController, null);
	}

	public void importSurveysFeedbacksI18nCSV() {
		log.debug("Import i18n CSV");

		val fileUploadComponentWithController = new FileUploadComponentWithController(
				".csv");
		fileUploadComponentWithController.setListener(new UploadListener() {
			@Override
			public void fileUploadReceived(final File file) {
				log.debug(
						"File upload successful, starting conversation of i18n string objects for surveys and feedbacks");
				try {
					val i18nStringObjects = CSVImporter
							.convertCSVToI18nSurveysFeedbacksStringsObjects(
									file);

					int updates = 0;

					log.debug("{} objects found in i18n import",
							i18nStringObjects.size());

					for (val i18nStringObject : i18nStringObjects) {
						val typeAndId = i18nStringObject.getId().split("_");
						val type = typeAndId[0];
						val i18nIdentifierAndOthers = typeAndId[1].split("\\.");
						val i18nIdentifier = i18nIdentifierAndOthers[0];
						int arrayPosition = 0;
						int subArrayPosition = 0;
						if (i18nIdentifierAndOthers.length > 1) {
							arrayPosition = Integer
									.parseInt(i18nIdentifierAndOthers[1]);
						}
						if (i18nIdentifierAndOthers.length > 2) {
							subArrayPosition = Integer
									.parseInt(i18nIdentifierAndOthers[2]);
						}
						val check = typeAndId[2];

						if (check.equals("#")) {
							switch (type) {
								case "su":
									updates += sas.screeningSurveyUpdateL18n(
											intervention.getId(),
											i18nIdentifier,
											i18nStringObject.getTitle());
									break;
								case "ss":
									updates += sas
											.screeningSurveySlideUpdateL18n(
													intervention.getId(),
													i18nIdentifier,
													i18nStringObject.getTitle(),
													i18nStringObject
															.getErrorMessage());
									break;
								case "sq":
									updates += sas
											.screeningSurveySlideQuestionUpdateL18n(
													intervention.getId(),
													i18nIdentifier,
													arrayPosition,
													i18nStringObject.getText());
									break;
								case "qa":
									updates += sas
											.screeningSurveySlideQuestionAnswerUpdateL18n(
													intervention.getId(),
													i18nIdentifier,
													arrayPosition,
													subArrayPosition,
													i18nStringObject.getText());
									break;
								case "fb":
									updates += sas.feedbackUpdateL18n(
											intervention.getId(),
											i18nIdentifier,
											i18nStringObject.getTitle());
									break;
								case "fs":
									updates += sas.feedbackSlideUpdateL18n(
											intervention.getId(),
											i18nIdentifier,
											i18nStringObject.getTitle(),
											i18nStringObject.getText());
									break;
							}
						}
					}

					log.debug("{} objects updated", updates);
					getAdminUI().showInformationNotification(
							AdminMessageStrings.NOTIFICATION__I18N_IMPORTED);
				} catch (final Exception e) {
					getAdminUI().showWarningNotification(
							AdminMessageStrings.NOTIFICATION__I18N_IMPORT_FAILED);
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
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__IMPORT_I18N,
				fileUploadComponentWithController, null);
	}

}
