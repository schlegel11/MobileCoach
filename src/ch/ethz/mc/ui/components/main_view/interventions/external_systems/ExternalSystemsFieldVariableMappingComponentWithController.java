package ch.ethz.mc.ui.components.main_view.interventions.external_systems;

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.InterventionExternalSystem;
import ch.ethz.mc.model.persistent.InterventionExternalSystemFieldVariableMapping;
import ch.ethz.mc.model.ui.UIInterventionExternalSystemFieldVariableMapping;
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
public class ExternalSystemsFieldVariableMappingComponentWithController extends ExternalSystemsFieldVariableMappingComponent {
	
	private final InterventionExternalSystem													interventionExternalSystem;

	private UIInterventionExternalSystemFieldVariableMapping									selectedUIExternalSystemMapping			= null;
	private BeanItem<UIInterventionExternalSystemFieldVariableMapping>							selectedUIExternalSystemMappingBeanItem	= null;

	private final BeanContainer<ObjectId, UIInterventionExternalSystemFieldVariableMapping>	beanContainer;

	public ExternalSystemsFieldVariableMappingComponentWithController(
			final InterventionExternalSystem interventionExternalSystem) {
		super();

		this.interventionExternalSystem = interventionExternalSystem;

		// table options
		val mappingTable = getMappingTable();
		mappingTable.setSelectable(true);
		mappingTable.setImmediate(true);

		// table content
		val externalSystemMappings = getInterventionAdministrationManagerService()
				.getAllExternalSystemFieldVariableMappingsOfExternalSystem(interventionExternalSystem.getId());

		beanContainer = createBeanContainerForModelObjects(
				UIInterventionExternalSystemFieldVariableMapping.class, externalSystemMappings);

		mappingTable.setContainerDataSource(beanContainer);
		mappingTable
				.setVisibleColumns(UIInterventionExternalSystemFieldVariableMapping.getVisibleColumns());
		mappingTable
				.setColumnHeaders(UIInterventionExternalSystemFieldVariableMapping.getColumnHeaders());

		// handle table selection change
		mappingTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = mappingTable.getValue();
				if (objectId == null) {
					setNothingSelected();
					selectedUIExternalSystemMapping = null;
					selectedUIExternalSystemMappingBeanItem = null;
				} else {
					selectedUIExternalSystemMapping = getUIModelObjectFromTableByObjectId(
							mappingTable, UIInterventionExternalSystemFieldVariableMapping.class,
							objectId);
					selectedUIExternalSystemMappingBeanItem = getBeanItemFromTableByObjectId(
							mappingTable, UIInterventionExternalSystemFieldVariableMapping.class,
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
				createExternalSystemFieldVariableMapping();
			} else if (event.getButton() == getEditButton()) {
				editExternalSystemFieldVariableMapping();
			} else if (event.getButton() == getDeleteButton()) {
				deleteExternalSystemFieldVariableMapping();
			}
		}
	}
	
	public void createExternalSystemFieldVariableMapping() {
		log.debug("Create external system field variable mapping");

		val shortStringEditWithComboBoxComponent = new ShortStringEditWithComboBoxComponent();
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_AND_SELECT_VARIABLE_FOR_SYSTEM_MAPPING, null,
				getInterventionAdministrationManagerService()
						.getAllInterventionVariablesManageableByServiceOrLessRestrictive(interventionExternalSystem.getIntervention()),
				shortStringEditWithComboBoxComponent, new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						InterventionExternalSystemFieldVariableMapping newExternalSystemFieldVariableMapping;
						try {
							// Create new external system mapping
							val selectedVariable = shortStringEditWithComboBoxComponent.getSelectedVariable();

							newExternalSystemFieldVariableMapping = getInterventionAdministrationManagerService()
									.interventionExternalSystemFieldVariableMappingCreate(interventionExternalSystem,
											getStringValue(), selectedVariable);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						beanContainer.addItem(newExternalSystemFieldVariableMapping.getId(),
								UIInterventionExternalSystemFieldVariableMapping.class
										.cast(newExternalSystemFieldVariableMapping.toUIModelObject()));
						getMappingTable().select(newExternalSystemFieldVariableMapping.getId());

						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__EXTERNAL_SYSTEM_MAPPING_CREATED);
						closeWindow();
					}
				}, null);
	}
	
	public void editExternalSystemFieldVariableMapping() {
		log.debug("Edit external system field variable mapping");

		val shortStringEditWithComboBoxComponent = new ShortStringEditWithComboBoxComponent();
		val selectedVariableName = getStringItemProperty(selectedUIExternalSystemMappingBeanItem,
				UIInterventionExternalSystemFieldVariableMapping.VARIABLE_WITH_VALUE_NAME).getValue();
		val variables = getInterventionAdministrationManagerService()
				.getAllInterventionVariablesManageableByServiceOrLessRestrictive(interventionExternalSystem.getIntervention());

		shortStringEditWithComboBoxComponent.addVariables(variables);
		shortStringEditWithComboBoxComponent.setSelectedVariable(selectedVariableName);

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_AND_SELECT_VARIABLE_FOR_SYSTEM_MAPPING,
				selectedUIExternalSystemMapping
						.getRelatedModelObject(InterventionExternalSystemFieldVariableMapping.class)
						.getFieldName(),
				null, shortStringEditWithComboBoxComponent, new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						val selectedExternalSystemMapping = selectedUIExternalSystemMapping
								.getRelatedModelObject(InterventionExternalSystemFieldVariableMapping.class);
						try {
							// Edit external system mapping
							boolean edited = false;

							if (!getStringValue().equals(selectedExternalSystemMapping.getFieldName())) {
								getInterventionAdministrationManagerService()
										.interventionExternalSystemFieldVariableMappingSetFieldName(
												selectedExternalSystemMapping, getStringValue());

								// Adapt UI
								getStringItemProperty(selectedUIExternalSystemMappingBeanItem,
										UIInterventionExternalSystemFieldVariableMapping.FIELD_NAME)
												.setValue(selectedUIExternalSystemMapping
														.getRelatedModelObject(
																InterventionExternalSystemFieldVariableMapping.class)
														.getFieldName());
								edited = true;
							}

							if (shortStringEditWithComboBoxComponent.getSelectedVariable() != null
								&& !selectedVariableName.equals(shortStringEditWithComboBoxComponent.getSelectedVariable())) {
								getInterventionAdministrationManagerService()
										.interventionExternalSystemFieldVariableMappingSetInterventionVariableWithName(
												selectedExternalSystemMapping,
												shortStringEditWithComboBoxComponent.getSelectedVariable());

								// Adapt UI
								getStringItemProperty(selectedUIExternalSystemMappingBeanItem,
										UIInterventionExternalSystemFieldVariableMapping.VARIABLE_WITH_VALUE_NAME)
												.setValue(shortStringEditWithComboBoxComponent.getSelectedVariable());
								edited = true;
							}

							if (edited) {
								getAdminUI().showInformationNotification(
										AdminMessageStrings.NOTIFICATION__EXTERNAL_SYSTEM_MAPPING_UPDATED);
							}

						} catch (final Exception e) {
							handleException(e);
							return;
						}

						closeWindow();
					}
				}, null);
	}
	
	public void deleteExternalSystemFieldVariableMapping() {
		log.debug("Delete external system field variable mapping");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedExternalSystemMapping = selectedUIExternalSystemMapping
							.getRelatedModelObject(InterventionExternalSystemFieldVariableMapping.class);

					// Delete external system mapping
					getInterventionAdministrationManagerService()
							.interventionExternalSystemFieldVariableMappingDelete(selectedExternalSystemMapping);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				getMappingTable().removeItem(selectedUIExternalSystemMapping
						.getRelatedModelObject(InterventionExternalSystemFieldVariableMapping.class).getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__EXTERNAL_SYSTEM_MAPPING_DELETED);

				closeWindow();
			}
		}, null);
	}
}
