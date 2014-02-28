package org.isgf.mhc.ui.views.components.interventions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.model.server.Intervention;
import org.isgf.mhc.model.server.ScreeningSurvey;
import org.isgf.mhc.model.ui.UIScreeningSurvey;
import org.isgf.mhc.tools.OnDemandFileDownloader;
import org.isgf.mhc.tools.OnDemandFileDownloader.OnDemandStreamResource;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the intervention screening surveys tab component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class InterventionScreeningSurveysTabComponentWithController extends
		InterventionScreeningSurveysTabComponent {

	private final Intervention									intervention;

	private UIScreeningSurvey									selectedUIScreeningSurvey		= null;
	private BeanItem<UIScreeningSurvey>							selectedUIInterventionBeanItem	= null;

	private final BeanContainer<ObjectId, UIScreeningSurvey>	beanContainer;

	public InterventionScreeningSurveysTabComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		// table options
		val screeningSurveysEditComponent = getInterventionScreeningSurveyEditComponent();
		val screeningSurveysTable = getInterventionScreeningSurveyEditComponent()
				.getScreeningSurveysTable();
		screeningSurveysTable.setSelectable(true);
		screeningSurveysTable.setImmediate(true);

		// table content
		beanContainer = createBeanContainerForModelObjects(
				UIScreeningSurvey.class,
				getScreeningSurveyAdministrationManagerService()
						.getAllScreeningSurveysOfIntervention(
								intervention.getId()));

		screeningSurveysTable.setContainerDataSource(beanContainer);
		screeningSurveysTable.setSortContainerPropertyId(UIScreeningSurvey
				.getSortColumn());
		screeningSurveysTable.setVisibleColumns(UIScreeningSurvey
				.getVisibleColumns());
		screeningSurveysTable.setColumnHeaders(UIScreeningSurvey
				.getColumnHeaders());

		// handle selection change
		screeningSurveysTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = screeningSurveysTable.getValue();
				if (objectId == null) {
					screeningSurveysEditComponent.setNothingSelected();
					selectedUIScreeningSurvey = null;
					selectedUIInterventionBeanItem = null;
				} else {
					selectedUIScreeningSurvey = getUIModelObjectFromTableByObjectId(
							screeningSurveysTable, UIScreeningSurvey.class,
							objectId);
					selectedUIInterventionBeanItem = getBeanItemFromTableByObjectId(
							screeningSurveysTable, UIScreeningSurvey.class,
							objectId);
					screeningSurveysEditComponent.setSomethingSelected();
				}
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		screeningSurveysEditComponent.getNewButton().addClickListener(
				buttonClickListener);
		screeningSurveysEditComponent.getImportButton().addClickListener(
				buttonClickListener);
		screeningSurveysEditComponent.getRenameButton().addClickListener(
				buttonClickListener);
		screeningSurveysEditComponent.getEditButton().addClickListener(
				buttonClickListener);
		screeningSurveysEditComponent.getDuplicateButton().addClickListener(
				buttonClickListener);
		screeningSurveysEditComponent.getDeleteButton().addClickListener(
				buttonClickListener);
		screeningSurveysEditComponent.getShowButton().addClickListener(
				buttonClickListener);

		// Special handle for export button
		val onDemandFileDownloader = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					@SneakyThrows(FileNotFoundException.class)
					public InputStream getStream() {
						return new FileInputStream(
								getScreeningSurveyAdministrationManagerService()
										.screeningSurveyExport(
												selectedUIScreeningSurvey
														.getRelatedModelObject(ScreeningSurvey.class)));
					}

					@Override
					public String getFilename() {
						return intervention.getName().replaceAll(
								"[^A-Za-z0-9_. ]+", "_")
								+ "_"
								+ selectedUIScreeningSurvey
										.getScreeningSurveyName().replaceAll(
												"[^A-Za-z0-9_. ]+", "_")
								+ Constants.getFileExtension();
					}
				});
		onDemandFileDownloader.extend(screeningSurveysEditComponent
				.getExportButton());
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			val interventionScreeningSurveyEditComponent = getInterventionScreeningSurveyEditComponent();

			if (event.getButton() == interventionScreeningSurveyEditComponent
					.getNewButton()) {
				createScreeningSurvey();
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getImportButton()) {
				// TODO import action
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getExportButton()) {
				// TODO export action
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getRenameButton()) {
				renameScreeningSurvey();
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getEditButton()) {
				editScreeningSurvey();
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getDuplicateButton()) {
				// TODO duplicate action
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getDeleteButton()) {
				deleteScreeningSurvey();
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getShowButton()) {
				showScreeningSurvey();
			}
		}
	}

	public void createScreeningSurvey() {
		log.debug("Create screening survey");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_SCREENING_SURVEY,
				null, null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						final ScreeningSurvey newScreeningSurvey;
						try {
							val newScreeningSurveyName = getStringValue();

							// Create intervention
							newScreeningSurvey = getScreeningSurveyAdministrationManagerService()
									.screeningSurveyCreate(
											newScreeningSurveyName,
											intervention.getId(),
											intervention.getGlobalUniqueId());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						beanContainer.addItem(newScreeningSurvey.getId(),
								UIScreeningSurvey.class.cast(newScreeningSurvey
										.toUIModelObject()));
						getInterventionScreeningSurveyEditComponent()
								.getScreeningSurveysTable().select(
										newScreeningSurvey.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_CREATED);

						closeWindow();
					}
				}, null);
	}

	public void renameScreeningSurvey() {
		log.debug("Rename screening survey");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_SCREENING_SURVEY,
				selectedUIScreeningSurvey.getRelatedModelObject(
						ScreeningSurvey.class).getName(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							val selectedScreeningSurvey = selectedUIScreeningSurvey
									.getRelatedModelObject(ScreeningSurvey.class);

							// Change name
							getScreeningSurveyAdministrationManagerService()
									.screeningSurveyChangeName(
											selectedScreeningSurvey,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						getStringItemProperty(selectedUIInterventionBeanItem,
								UIScreeningSurvey.SCREENING_SURVEY_NAME)
								.setValue(
										selectedUIScreeningSurvey
												.getRelatedModelObject(
														ScreeningSurvey.class)
												.getName());

						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_RENAMED);
						closeWindow();
					}
				}, null);
	}

	public void editScreeningSurvey() {
		log.debug("Edit screening survey");

		val screeningSurvey = selectedUIScreeningSurvey
				.getRelatedModelObject(ScreeningSurvey.class);

		// TODO open window to edit screening survey
	}

	public void deleteScreeningSurvey() {
		log.debug("Delete screening survey");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedScreeningSurvey = selectedUIScreeningSurvey.getRelatedModelObject(ScreeningSurvey.class);

					// Delete intervention
					getScreeningSurveyAdministrationManagerService()
							.screeningSurveyDelete(selectedScreeningSurvey);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				getInterventionScreeningSurveyEditComponent()
						.getScreeningSurveysTable().removeItem(
								selectedUIScreeningSurvey
										.getRelatedModelObject(
												ScreeningSurvey.class).getId());
				getAdminUI()
						.showInformationNotification(
								AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_DELETED);

				closeWindow();
			}
		}, null);
	}

	public void showScreeningSurvey() {
		log.debug("Show screening survey");

		val screeningSurvey = selectedUIScreeningSurvey
				.getRelatedModelObject(ScreeningSurvey.class);

		final String url = getAdminUI()
				.getPage()
				.getLocation()
				.toString()
				.substring(
						0,
						getAdminUI().getPage().getLocation().toString()
								.lastIndexOf("/") + 1)
				+ screeningSurvey.getId() + "/";

		getAdminUI().getPage().open(url, "_blank");
	}

}
