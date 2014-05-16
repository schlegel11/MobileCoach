package org.isgf.mhc.ui.views.components.interventions;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.MHC;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.ui.UIModule;
import org.isgf.mhc.modules.AbstractModule;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the all interventions tab component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class InterventionBasicSettingsAndModulesTabComponentWithController
		extends InterventionBasicSettingsAndModulesTabComponent {

	private final Intervention											intervention;

	private final InterventionEditingContainerComponentWithController	interventionEditingContainerComponentWithController;

	private boolean														lastInterventionMonitoringState	= false;

	private AbstractModule												selectedModule					= null;

	private final BeanContainer<AbstractModule, UIModule>				beanContainer;

	public InterventionBasicSettingsAndModulesTabComponentWithController(
			final Intervention intervention,
			final InterventionEditingContainerComponentWithController interventionEditingContainerComponentWithController) {
		super();

		this.intervention = intervention;
		this.interventionEditingContainerComponentWithController = interventionEditingContainerComponentWithController;

		lastInterventionMonitoringState = intervention.isMonitoringActive();

		// Set the first time before other tabs are constructed
		interventionEditingContainerComponentWithController
				.setEditingDependingOnMessaging(!intervention
						.isMonitoringActive());

		val interventionBasicSettingsComponent = getInterventionBasicSettingsAndModulesComponent();

		// Handle modules table
		beanContainer = new BeanContainer<AbstractModule, UIModule>(
				UIModule.class);

		val modules = getInterventionAdministrationManagerService()
				.getRegisteredModules();
		val modulesTable = interventionBasicSettingsComponent.getModulesTable();
		modulesTable.setImmediate(true);
		modulesTable.setSelectable(true);
		modulesTable.setContainerDataSource(beanContainer);
		modulesTable.setSortContainerPropertyId(UIModule.getSortColumn());
		modulesTable.setVisibleColumns(UIModule.getVisibleColumns());
		modulesTable.setColumnHeaders(UIModule.getColumnHeaders());

		for (val moduleClass : modules) {
			AbstractModule module;
			try {
				module = moduleClass.getDeclaredConstructor(ObjectId.class)
						.newInstance(intervention.getId());
				beanContainer.addItem(module, module.toUIModule());
			} catch (final Exception e) {
				log.error("Error when creating new module instance: {}",
						e.getMessage());
			}
		}

		// Handle table selection change
		modulesTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = modulesTable.getValue();
				if (objectId == null) {
					selectedModule = null;
				} else {
					selectedModule = (AbstractModule) modulesTable.getValue();
				}
				adjust();
			}
		});

		// Handle buttons
		val buttonClickListener = new ButtonClickListener();
		interventionBasicSettingsComponent.getSwitchInterventionButton()
				.addClickListener(buttonClickListener);
		interventionBasicSettingsComponent.getSwitchMessagingButton()
				.addClickListener(buttonClickListener);
		interventionBasicSettingsComponent.getOpenModuleButton()
				.addClickListener(buttonClickListener);

		// Set start state
		adjust();
	}

	private void adjust() {
		getInterventionBasicSettingsAndModulesComponent().adjust(
				intervention.isActive(), intervention.isMonitoringActive(),
				selectedModule);

		if (lastInterventionMonitoringState != intervention
				.isMonitoringActive()) {
			// Messaging status has been changed, so adapt UI
			interventionEditingContainerComponentWithController
					.setEditingDependingOnMessaging(!intervention
							.isMonitoringActive());
		}

		lastInterventionMonitoringState = intervention.isMonitoringActive();
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			val interventionBasicSettingsComponent = getInterventionBasicSettingsAndModulesComponent();

			if (event.getButton() == interventionBasicSettingsComponent
					.getSwitchInterventionButton()) {
				switchIntervention();
			} else if (event.getButton() == interventionBasicSettingsComponent
					.getSwitchMessagingButton()) {
				switchMessaging();
			} else if (event.getButton() == interventionBasicSettingsComponent
					.getOpenModuleButton()) {
				openModule();
			}
		}
	}

	public void switchIntervention() {
		log.debug("Switch intervention");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					MHC.getInstance()
							.getInterventionExecutionManagerService()
							.interventionSetStatus(intervention,
									!intervention.isActive());
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				adjust();
				closeWindow();
			}
		}, null);
	}

	public void switchMessaging() {
		log.debug("Switch messaging");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					MHC.getInstance()
							.getInterventionExecutionManagerService()
							.interventionSetMonitoring(intervention,
									!intervention.isMonitoringActive());
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				interventionEditingContainerComponentWithController.setEditingDependingOnMessaging(!intervention
						.isMonitoringActive());

				adjust();
				closeWindow();
			}
		}, null);
	}

	public void openModule() {
		log.debug("Open module");

		showModalClosableEditWindow(selectedModule.getName(), selectedModule,
				null);
	}
}
