package org.isgf.mhc.ui.views.components.interventions;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.server.Intervention;
import org.isgf.mhc.model.ui.UIIntervention;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the all interventions tab component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class AllInterventionsTabComponentWithController extends
		AllInterventionsTabComponent {

	private UIIntervention									selectedUIIntervention			= null;
	private BeanItem<UIIntervention>						selectedUIInterventionBeanItem	= null;

	private final BeanContainer<ObjectId, UIIntervention>	beanContainer;

	public AllInterventionsTabComponentWithController() {
		super();

		// table options
		val allInterventionsEditComponent = getAllInterventionsEditComponent();
		val allInterventionsTable = getAllInterventionsEditComponent()
				.getAllInterventionsTable();
		allInterventionsTable.setSelectable(true);
		allInterventionsTable.setImmediate(true);

		// table content
		beanContainer = createBeanContainerForModelObjects(
				UIIntervention.class,
				getInterventionAdministrationManagerService()
						.getAllInterventions());

		allInterventionsTable.setContainerDataSource(beanContainer);
		allInterventionsTable.setSortContainerPropertyId(UIIntervention
				.getSortColumn());
		allInterventionsTable.setVisibleColumns(UIIntervention
				.getVisibleColumns());
		allInterventionsTable.setColumnHeaders(UIIntervention
				.getColumnHeaders());

		// handle selection change
		allInterventionsTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = allInterventionsTable.getValue();
				if (objectId == null) {
					allInterventionsEditComponent.setNothingSelected();
					selectedUIIntervention = null;
					selectedUIInterventionBeanItem = null;
				} else {
					selectedUIIntervention = getUIModelObjectFromTableByObjectId(
							allInterventionsTable, UIIntervention.class,
							objectId);
					selectedUIInterventionBeanItem = getBeanItemFromTableByObjectId(
							allInterventionsTable, UIIntervention.class,
							objectId);
					allInterventionsEditComponent.setSomethingSelected();
				}
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		allInterventionsEditComponent.getNewButton().addClickListener(
				buttonClickListener);
		allInterventionsEditComponent.getImportButton().addClickListener(
				buttonClickListener);
		allInterventionsEditComponent.getExportButton().addClickListener(
				buttonClickListener);
		allInterventionsEditComponent.getEditButton().addClickListener(
				buttonClickListener);
		allInterventionsEditComponent.getDuplicateButton().addClickListener(
				buttonClickListener);
		allInterventionsEditComponent.getDeleteButton().addClickListener(
				buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			val allInterventionsEditComponent = getAllInterventionsEditComponent();

			if (event.getButton() == allInterventionsEditComponent
					.getNewButton()) {
				createIntervention();
			} else if (event.getButton() == allInterventionsEditComponent
					.getImportButton()) {
				// TODO import action
			} else if (event.getButton() == allInterventionsEditComponent
					.getExportButton()) {
				// TODO export action
			} else if (event.getButton() == allInterventionsEditComponent
					.getEditButton()) {
				// TODO edit action
			} else if (event.getButton() == allInterventionsEditComponent
					.getDuplicateButton()) {
				// TODO duplicate action
			} else if (event.getButton() == allInterventionsEditComponent
					.getDeleteButton()) {
				deleteIntervention();
			}
		}
	}

	public void createIntervention() {
		log.debug("Create intervention");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_NEW_INTERVENTION,
				null, null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						final Intervention newIntervention;
						try {
							val newInterventionName = getStringValue();

							// Create intervention
							newIntervention = getInterventionAdministrationManagerService()
									.interventionCreate(newInterventionName);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						beanContainer.addItem(newIntervention.getId(),
								UIIntervention.class.cast(newIntervention
										.toUIModelObject()));
						getAllInterventionsEditComponent()
								.getAllInterventionsTable().select(
										newIntervention.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__INTERVENTION_CREATED);

						closeWindow();
					}
				}, null);
	}

	public void deleteIntervention() {
		log.debug("Delete intervention");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedIntervention = selectedUIIntervention.getRelatedModelObject(Intervention.class);

					// Delete intervention
					getInterventionAdministrationManagerService()
							.interventionDelete(selectedIntervention);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				getAllInterventionsEditComponent().getAllInterventionsTable()
						.removeItem(
								selectedUIIntervention.getRelatedModelObject(
										Intervention.class).getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__INTERVENTION_DELETED);

				closeWindow();
			}
		}, null);
	}

}
