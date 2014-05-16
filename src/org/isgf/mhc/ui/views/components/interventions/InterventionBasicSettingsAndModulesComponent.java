package org.isgf.mhc.ui.views.components.interventions;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.conf.ThemeImageStrings;
import org.isgf.mhc.modules.AbstractModule;
import org.isgf.mhc.ui.views.components.AbstractCustomComponent;
import org.isgf.mhc.ui.views.components.simulator.SimulatorComponentWithController;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the XYZ component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class InterventionBasicSettingsAndModulesComponent extends
		AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout						mainLayout;
	@AutoGenerated
	private SimulatorComponentWithController	simulatorComponent;
	@AutoGenerated
	private Button								openModuleButton;
	@AutoGenerated
	private Table								modulesTable;
	@AutoGenerated
	private Label								modulesLabel;
	@AutoGenerated
	private Button								switchMessagingButton;
	@AutoGenerated
	private Button								switchInterventionButton;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected InterventionBasicSettingsAndModulesComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(
				switchInterventionButton,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_INTERVENTION_BUTTON_INACTIVE);
		localize(
				switchMessagingButton,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_MONITORING_BUTTON_INACTIVE);
		localize(modulesLabel, AdminMessageStrings.MODULES__LABEL);
		localize(openModuleButton, AdminMessageStrings.MODULES__OPEN_MODULE);

		// Deactivate simulator if not set
		if (!Constants.isSimulatedDateAndTime()) {
			simulatorComponent.setVisible(false);
		}

		// set start settings
		adjust(false, false, null);
	}

	protected void adjust(final boolean interventionStatus,
			final boolean messagingStatus, final AbstractModule selectedModule) {
		// Adjust intervention status
		if (interventionStatus) {
			switchInterventionButton.setIcon(new ThemeResource(
					ThemeImageStrings.ACTIVE_ICON_SMALL));
			localize(
					switchInterventionButton,
					AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_INTERVENTION_BUTTON_ACTIVE);

			switchMessagingButton.setEnabled(true);
		} else {
			switchInterventionButton.setIcon(new ThemeResource(
					ThemeImageStrings.INACTIVE_ICON_SMALL));
			localize(
					switchInterventionButton,
					AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_INTERVENTION_BUTTON_INACTIVE);

			switchMessagingButton.setEnabled(false);
		}

		// Adjust messaging status
		if (messagingStatus) {
			switchMessagingButton.setIcon(new ThemeResource(
					ThemeImageStrings.ACTIVE_ICON_SMALL));
			localize(
					switchMessagingButton,
					AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_MONITORING_BUTTON_ACTIVE);

			switchInterventionButton.setEnabled(false);
		} else {
			switchMessagingButton.setIcon(new ThemeResource(
					ThemeImageStrings.INACTIVE_ICON_SMALL));
			localize(
					switchMessagingButton,
					AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_MONITORING_BUTTON_INACTIVE);

			switchInterventionButton.setEnabled(true);
		}

		// Adjust open module button
		if (selectedModule == null) {
			openModuleButton.setEnabled(false);
		} else {
			openModuleButton.setEnabled(true);
		}
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("100.0%");
		setHeight("-1px");

		// switchInterventionButton
		switchInterventionButton = new Button();
		switchInterventionButton.setCaption("!!! Activate Intervention");
		switchInterventionButton.setImmediate(true);
		switchInterventionButton.setWidth("100.0%");
		switchInterventionButton.setHeight("-1px");
		mainLayout.addComponent(switchInterventionButton);

		// switchMessagingButton
		switchMessagingButton = new Button();
		switchMessagingButton.setCaption("!!! Activate Messaging");
		switchMessagingButton.setImmediate(true);
		switchMessagingButton.setWidth("100.0%");
		switchMessagingButton.setHeight("-1px");
		mainLayout.addComponent(switchMessagingButton);

		// modulesLabel
		modulesLabel = new Label();
		modulesLabel.setStyleName("bold");
		modulesLabel.setImmediate(false);
		modulesLabel.setWidth("-1px");
		modulesLabel.setHeight("-1px");
		modulesLabel.setValue("!!! Modules:");
		mainLayout.addComponent(modulesLabel);

		// modulesTable
		modulesTable = new Table();
		modulesTable.setImmediate(false);
		modulesTable.setWidth("100.0%");
		modulesTable.setHeight("100px");
		mainLayout.addComponent(modulesTable);

		// openModuleButton
		openModuleButton = new Button();
		openModuleButton.setCaption("!!! Open Module");
		openModuleButton.setImmediate(true);
		openModuleButton.setWidth("100.0%");
		openModuleButton.setHeight("-1px");
		mainLayout.addComponent(openModuleButton);

		// simulatorComponent
		simulatorComponent = new SimulatorComponentWithController();
		simulatorComponent.setImmediate(false);
		simulatorComponent.setWidth("100.0%");
		simulatorComponent.setHeight("-1px");
		mainLayout.addComponent(simulatorComponent);

		return mainLayout;
	}

}
