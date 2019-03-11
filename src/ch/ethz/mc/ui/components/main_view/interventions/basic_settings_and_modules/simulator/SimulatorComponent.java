package ch.ethz.mc.ui.components.main_view.interventions.basic_settings_and_modules.simulator;

/* ##LICENSE## */
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.components.AbstractCustomComponent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Provides the simulator component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class SimulatorComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private HorizontalLayout	buttonsLayout;
	@AutoGenerated
	private Button				deactivateFastForwardModeButton;
	@AutoGenerated
	private Button				activateFastForwadModeButton;
	@AutoGenerated
	private Button				nextDayButton;
	@AutoGenerated
	private Button				nextHourButton;
	@AutoGenerated
	private Button				nextTenMinuesButton;
	@AutoGenerated
	private Label				currentTimeLabel;
	@AutoGenerated
	private Label				simulatorLabel;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	protected SimulatorComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(nextTenMinuesButton,
				AdminMessageStrings.SIMULATOR_COMPONENT__JUMP_TEN_MINUTES_TO_THE_FUTURE);
		localize(nextHourButton,
				AdminMessageStrings.SIMULATOR_COMPONENT__JUMP_ONE_HOUR_TO_THE_FUTURE);
		localize(nextDayButton,
				AdminMessageStrings.SIMULATOR_COMPONENT__JUMP_ONE_DAY_TO_THE_FUTURE);
		localize(activateFastForwadModeButton,
				AdminMessageStrings.SIMULATOR_COMPONENT__ACTIVATE_FAST_FORWARD_MODE);
		localize(deactivateFastForwardModeButton,
				AdminMessageStrings.SIMULATOR_COMPONENT__DEACTIVATE_FAST_FORWARD_MODE);
		localize(currentTimeLabel,
				AdminMessageStrings.SIMULATOR_COMPONENT__THE_CURRENT_SIMULATED_TIME_IS_X);
		localize(simulatorLabel,
				AdminMessageStrings.SIMULATOR_COMPONENT__SIMULATOR);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("76px");
		mainLayout.setMargin(false);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("100.0%");
		setHeight("76px");

		// simulatorLabel
		simulatorLabel = new Label();
		simulatorLabel.setStyleName("bold");
		simulatorLabel.setImmediate(false);
		simulatorLabel.setWidth("-1px");
		simulatorLabel.setHeight("-1px");
		simulatorLabel.setValue("!!! SIMULATOR");
		mainLayout.addComponent(simulatorLabel);

		// currentTimeLabel
		currentTimeLabel = new Label();
		currentTimeLabel.setImmediate(false);
		currentTimeLabel.setWidth("-1px");
		currentTimeLabel.setHeight("-1px");
		currentTimeLabel.setValue("!!! The current simulated time is #####");
		mainLayout.addComponent(currentTimeLabel);

		// buttonsLayout
		buttonsLayout = buildButtonsLayout();
		mainLayout.addComponent(buttonsLayout);

		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildButtonsLayout() {
		// common part: create layout
		buttonsLayout = new HorizontalLayout();
		buttonsLayout.setImmediate(false);
		buttonsLayout.setWidth("-1px");
		buttonsLayout.setHeight("-1px");
		buttonsLayout.setMargin(false);
		buttonsLayout.setSpacing(true);

		// nextTenMinuesButton
		nextTenMinuesButton = new Button();
		nextTenMinuesButton.setCaption("!!! Jump 10 Minutes to the Future!");
		nextTenMinuesButton.setImmediate(true);
		nextTenMinuesButton.setWidth("200px");
		nextTenMinuesButton.setHeight("-1px");
		buttonsLayout.addComponent(nextTenMinuesButton);

		// nextHourButton
		nextHourButton = new Button();
		nextHourButton.setCaption("!!! Jump one HOUR to the Future!");
		nextHourButton.setImmediate(true);
		nextHourButton.setWidth("200px");
		nextHourButton.setHeight("-1px");
		buttonsLayout.addComponent(nextHourButton);

		// nextDayButton
		nextDayButton = new Button();
		nextDayButton.setCaption("!!! Jump one DAY to the Future!");
		nextDayButton.setImmediate(true);
		nextDayButton.setWidth("200px");
		nextDayButton.setHeight("-1px");
		buttonsLayout.addComponent(nextDayButton);

		// activateFastForwadModeButton
		activateFastForwadModeButton = new Button();
		activateFastForwadModeButton
				.setCaption("!!! Activate Fast Forward Mode");
		activateFastForwadModeButton.setImmediate(true);
		activateFastForwadModeButton.setWidth("200px");
		activateFastForwadModeButton.setHeight("-1px");
		buttonsLayout.addComponent(activateFastForwadModeButton);

		// deactivateFastForwardModeButton
		deactivateFastForwardModeButton = new Button();
		deactivateFastForwardModeButton
				.setCaption("!!! Deactivate Fast Forward Mode");
		deactivateFastForwardModeButton.setImmediate(true);
		deactivateFastForwardModeButton.setWidth("200px");
		deactivateFastForwardModeButton.setHeight("-1px");
		buttonsLayout.addComponent(deactivateFastForwardModeButton);

		return buttonsLayout;
	}

}
