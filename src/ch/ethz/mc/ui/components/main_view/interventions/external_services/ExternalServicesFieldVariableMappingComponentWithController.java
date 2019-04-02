package ch.ethz.mc.ui.components.main_view.interventions.external_services;

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.InterventionExternalService;
import ch.ethz.mc.model.persistent.InterventionExternalServiceFieldVariableMapping;
import ch.ethz.mc.model.ui.UIInterventionExternalServiceFieldVariableMapping;
import ch.ethz.mc.ui.components.basics.ShortStringEditWithComboBoxComponent;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the intervention variables tab component with a controller
 *
 * @author Marcel Schlegel
 */
@SuppressWarnings("serial")
@Log4j2
public class ExternalServicesFieldVariableMappingComponentWithController extends ExternalServicesFieldVariableMappingComponent {
	
	private final InterventionExternalService													interventionExternalService;

	private UIInterventionExternalServiceFieldVariableMapping									selectedUIExternalServiceMapping			= null;
	private BeanItem<UIInterventionExternalServiceFieldVariableMapping>							selectedUIExternalServiceMappingBeanItem	= null;

	private final BeanContainer<ObjectId, UIInterventionExternalServiceFieldVariableMapping>	beanContainer;

	public ExternalServicesFieldVariableMappingComponentWithController(
			final InterventionExternalService interventionExternalService) {
		super();

		this.interventionExternalService = interventionExternalService;

		// table options
		val mappingTable = getMappingTable();
		mappingTable.setSelectable(true);
		mappingTable.setImmediate(true);

		// table content
		val externalServiceMappings = getInterventionAdministrationManagerService()
				.getAllExternalServiceFieldVariableMappingsOfExternalService(interventionExternalService.getId());

		beanContainer = createBeanContainerForModelObjects(
				UIInterventionExternalServiceFieldVariableMapping.class, externalServiceMappings);

		mappingTable.setContainerDataSource(beanContainer);
		mappingTable
				.setVisibleColumns(UIInterventionExternalServiceFieldVariableMapping.getVisibleColumns());
		mappingTable
				.setColumnHeaders(UIInterventionExternalServiceFieldVariableMapping.getColumnHeaders());

		// handle table selection change
		mappingTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = mappingTable.getValue();
				if (objectId == null) {
					setNothingSelected();
					selectedUIExternalServiceMapping = null;
					selectedUIExternalServiceMappingBeanItem = null;
				} else {
					selectedUIExternalServiceMapping = getUIModelObjectFromTableByObjectId(
							mappingTable, UIInterventionExternalServiceFieldVariableMapping.class,
							objectId);
					selectedUIExternalServiceMappingBeanItem = getBeanItemFromTableByObjectId(
							mappingTable, UIInterventionExternalServiceFieldVariableMapping.class,
							objectId);
					setSomethingSelected();
				}
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getNewButton()
				.addClickListener(buttonClickListener);
		getEditButton()
				.addClickListener(buttonClickListener);
		getDeleteButton()
				.addClickListener(buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {

			if (event.getButton() == getNewButton()) {
				createExternalServiceFieldVariableMapping();
			} else if (event.getButton() == getEditButton()) {
				editExternalServiceFieldVariableMapping();
			} else if (event.getButton() == getDeleteButton()) {
				deleteExternalServiceFieldVariableMapping();
			}
		}
	}
	
	public void createExternalServiceFieldVariableMapping() {
		log.debug("Create external service field variable mapping");

		val shortStringEditWithComboBoxComponent = new ShortStringEditWithComboBoxComponent();
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_AND_SELECT_VARIABLE_FOR_SERVICE_MAPPING, null,
				getInterventionAdministrationManagerService()
						.getAllInterventionVariablesManageableByServiceOrLessRestrictive(interventionExternalService.getIntervention()),
				shortStringEditWithComboBoxComponent, new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						InterventionExternalServiceFieldVariableMapping newExternalServiceFieldVariableMapping;
						try {
							// Create new external service mapping
							val selectedVariable = shortStringEditWithComboBoxComponent.getSelectedVariable();

							newExternalServiceFieldVariableMapping = getInterventionAdministrationManagerService()
									.interventionExternalServiceFieldVariableMappingCreate(interventionExternalService,
											getStringValue(), selectedVariable);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						beanContainer.addItem(newExternalServiceFieldVariableMapping.getId(),
								UIInterventionExternalServiceFieldVariableMapping.class
										.cast(newExternalServiceFieldVariableMapping.toUIModelObject()));
						getMappingTable().select(newExternalServiceFieldVariableMapping.getId());

						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__EXTERNAL_SERVICE_MAPPING_CREATED);
						closeWindow();
					}
				}, null);
	}
	
	public void editExternalServiceFieldVariableMapping() {
		log.debug("Edit external service field variable mapping");

		val shortStringEditWithComboBoxComponent = new ShortStringEditWithComboBoxComponent();
		val selectedVariableName = getStringItemProperty(selectedUIExternalServiceMappingBeanItem,
				UIInterventionExternalServiceFieldVariableMapping.VARIABLE_WITH_VALUE_NAME).getValue();
		val variables = getInterventionAdministrationManagerService()
				.getAllInterventionVariablesManageableByServiceOrLessRestrictive(interventionExternalService.getIntervention());

		shortStringEditWithComboBoxComponent.addVariables(variables);
		shortStringEditWithComboBoxComponent.setSelectedVariable(selectedVariableName);

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_AND_SELECT_VARIABLE_FOR_SERVICE_MAPPING,
				selectedUIExternalServiceMapping
						.getRelatedModelObject(InterventionExternalServiceFieldVariableMapping.class)
						.getJsonFieldName(),
				null, shortStringEditWithComboBoxComponent, new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						val selectedExternalServiceMapping = selectedUIExternalServiceMapping
								.getRelatedModelObject(InterventionExternalServiceFieldVariableMapping.class);
						try {
							// Edit external service mapping
							boolean edited = false;

							if (!getStringValue().equals(selectedExternalServiceMapping.getJsonFieldName())) {
								getInterventionAdministrationManagerService()
										.interventionExternalServiceFieldVariableMappingSetJsonFieldName(
												selectedExternalServiceMapping, getStringValue());

								// Adapt UI
								getStringItemProperty(selectedUIExternalServiceMappingBeanItem,
										UIInterventionExternalServiceFieldVariableMapping.JSON_FIELD_NAME)
												.setValue(selectedUIExternalServiceMapping
														.getRelatedModelObject(
																InterventionExternalServiceFieldVariableMapping.class)
														.getJsonFieldName());
								edited = true;
							}

							if (shortStringEditWithComboBoxComponent.getSelectedVariable() != null
								&& !selectedVariableName.equals(shortStringEditWithComboBoxComponent.getSelectedVariable())) {
								getInterventionAdministrationManagerService()
										.interventionExternalServiceFieldVariableMappingSetInterventionVariableWithName(
												selectedExternalServiceMapping,
												shortStringEditWithComboBoxComponent.getSelectedVariable());

								// Adapt UI
								getStringItemProperty(selectedUIExternalServiceMappingBeanItem,
										UIInterventionExternalServiceFieldVariableMapping.VARIABLE_WITH_VALUE_NAME)
												.setValue(shortStringEditWithComboBoxComponent.getSelectedVariable());
								edited = true;
							}

							if (edited) {
								getAdminUI().showInformationNotification(
										AdminMessageStrings.NOTIFICATION__EXTERNAL_SERVICE_MAPPING_UPDATED);
							}

						} catch (final Exception e) {
							handleException(e);
							return;
						}

						closeWindow();
					}
				}, null);
	}
	
	public void deleteExternalServiceFieldVariableMapping() {
		log.debug("Delete external service field variable mapping");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedExternalServiceMapping = selectedUIExternalServiceMapping
							.getRelatedModelObject(InterventionExternalServiceFieldVariableMapping.class);

					// Delete external service mapping
					getInterventionAdministrationManagerService()
							.interventionExternalServiceFieldVariableMappingDelete(selectedExternalServiceMapping);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				getMappingTable().removeItem(selectedUIExternalServiceMapping
						.getRelatedModelObject(InterventionExternalServiceFieldVariableMapping.class).getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__EXTERNAL_SERVICE_MAPPING_DELETED);

				closeWindow();
			}
		}, null);
	}
}
