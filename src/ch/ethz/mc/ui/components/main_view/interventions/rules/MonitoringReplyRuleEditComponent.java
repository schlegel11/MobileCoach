package ch.ethz.mc.ui.components.main_view.interventions.rules;

/* ##LICENSE## */
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.components.AbstractClosableEditComponent;
import ch.ethz.mc.ui.components.basics.AbstractRuleEditComponentWithController;
import ch.ethz.mc.ui.components.basics.VariableTextFieldComponent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Provides a monitoring reply rule edit component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class MonitoringReplyRuleEditComponent
		extends AbstractClosableEditComponent {
	@AutoGenerated
	private VerticalLayout							mainLayout;

	@AutoGenerated
	private GridLayout								buttonLayout;

	@AutoGenerated
	private Button									closeButton;

	@AutoGenerated
	private VerticalLayout							switchesGroupLayout;

	@AutoGenerated
	private GridLayout								gridLayout2;

	@AutoGenerated
	private ComboBox								microDialogComboBox;

	@AutoGenerated
	private Label									microDialogLabel;

	@AutoGenerated
	private ComboBox								messageGroupComboBox;

	@AutoGenerated
	private Label									messageGroupLabel;

	@AutoGenerated
	private CheckBox								startMicroDialogCheckBox;

	@AutoGenerated
	private CheckBox								sendToSupervisorCheckBox;

	@AutoGenerated
	private CheckBox								sendMessageIfTrueCheckBox;

	@AutoGenerated
	private GridLayout								gridLayout1;

	@AutoGenerated
	private VariableTextFieldComponent				storeVariableTextFieldComponent;

	@AutoGenerated
	private Label									storeVariableLabel;

	@AutoGenerated
	private AbstractRuleEditComponentWithController	abstractRuleEditComponentWithController;

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		closeButton.addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(
			final ClickListener clickListener) {
		// not required
	}

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	protected MonitoringReplyRuleEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(storeVariableLabel,
				AdminMessageStrings.MONITORING_MESSAGE_EDITING__STORE_RESULT_TO_VARIABLE);
		localize(sendMessageIfTrueCheckBox,
				AdminMessageStrings.MONITORING_RULE_EDITING__SEND_MESSAGE_IF_TRUE);
		localize(sendToSupervisorCheckBox,
				AdminMessageStrings.MONITORING_RULE_EDITING__SEND_TO_SUPERVISOR);
		localize(startMicroDialogCheckBox,
				AdminMessageStrings.MONITORING_RULE_EDITING__START_MICRO_DIALOG_IF_TRUE);
		localize(messageGroupLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__MESSAGE_GROUP_TO_SEND_MESSAGES_FROM);
		localize(microDialogLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__MICRO_DIALOG_TO_START);

		localize(closeButton, AdminMessageStrings.GENERAL__CLOSE);

		sendMessageIfTrueCheckBox.setImmediate(true);
		sendToSupervisorCheckBox.setImmediate(true);
		startMicroDialogCheckBox.setImmediate(true);

		messageGroupComboBox.setImmediate(true);
		messageGroupComboBox.setNullSelectionAllowed(true);
		messageGroupComboBox.setTextInputAllowed(false);
		microDialogComboBox.setImmediate(true);
		microDialogComboBox.setNullSelectionAllowed(true);
		microDialogComboBox.setTextInputAllowed(false);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("1050px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);

		// top-level component properties
		setWidth("1050px");
		setHeight("-1px");

		// abstractRuleEditComponentWithController
		abstractRuleEditComponentWithController = new AbstractRuleEditComponentWithController();
		abstractRuleEditComponentWithController.setImmediate(false);
		abstractRuleEditComponentWithController.setWidth("100.0%");
		abstractRuleEditComponentWithController.setHeight("-1px");
		mainLayout.addComponent(abstractRuleEditComponentWithController);

		// switchesGroupLayout
		switchesGroupLayout = buildSwitchesGroupLayout();
		mainLayout.addComponent(switchesGroupLayout);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);

		return mainLayout;
	}

	@AutoGenerated
	private VerticalLayout buildSwitchesGroupLayout() {
		// common part: create layout
		switchesGroupLayout = new VerticalLayout();
		switchesGroupLayout.setImmediate(false);
		switchesGroupLayout.setWidth("100.0%");
		switchesGroupLayout.setHeight("-1px");
		switchesGroupLayout.setMargin(true);
		switchesGroupLayout.setSpacing(true);

		// gridLayout1
		gridLayout1 = buildGridLayout1();
		switchesGroupLayout.addComponent(gridLayout1);

		// sendMessageIfTrueCheckBox
		sendMessageIfTrueCheckBox = new CheckBox();
		sendMessageIfTrueCheckBox.setStyleName("bold");
		sendMessageIfTrueCheckBox
				.setCaption("!!! Send message if rule result is TRUE");
		sendMessageIfTrueCheckBox.setImmediate(false);
		sendMessageIfTrueCheckBox.setWidth("100.0%");
		sendMessageIfTrueCheckBox.setHeight("-1px");
		switchesGroupLayout.addComponent(sendMessageIfTrueCheckBox);

		// sendToSupervisorCheckBox
		sendToSupervisorCheckBox = new CheckBox();
		sendToSupervisorCheckBox.setStyleName("bold");
		sendToSupervisorCheckBox.setCaption(
				"!!! Send message to supervisor (NOT to participant)");
		sendToSupervisorCheckBox.setImmediate(false);
		sendToSupervisorCheckBox.setWidth("100.0%");
		sendToSupervisorCheckBox.setHeight("-1px");
		switchesGroupLayout.addComponent(sendToSupervisorCheckBox);

		// startMicroDialogCheckBox
		startMicroDialogCheckBox = new CheckBox();
		startMicroDialogCheckBox.setStyleName("bold");
		startMicroDialogCheckBox
				.setCaption("!!! Start micro dialog if rule result is TRUE");
		startMicroDialogCheckBox.setImmediate(false);
		startMicroDialogCheckBox.setWidth("100.0%");
		startMicroDialogCheckBox.setHeight("-1px");
		switchesGroupLayout.addComponent(startMicroDialogCheckBox);

		// gridLayout2
		gridLayout2 = buildGridLayout2();
		switchesGroupLayout.addComponent(gridLayout2);

		return switchesGroupLayout;
	}

	@AutoGenerated
	private GridLayout buildGridLayout1() {
		// common part: create layout
		gridLayout1 = new GridLayout();
		gridLayout1.setImmediate(false);
		gridLayout1.setWidth("100.0%");
		gridLayout1.setHeight("-1px");
		gridLayout1.setMargin(false);
		gridLayout1.setSpacing(true);
		gridLayout1.setColumns(2);

		// storeVariableLabel
		storeVariableLabel = new Label();
		storeVariableLabel.setImmediate(false);
		storeVariableLabel.setWidth("-1px");
		storeVariableLabel.setHeight("-1px");
		storeVariableLabel
				.setValue("!!! Store result to variable (if required):");
		gridLayout1.addComponent(storeVariableLabel, 0, 0);

		// storeVariableTextFieldComponent
		storeVariableTextFieldComponent = new VariableTextFieldComponent();
		storeVariableTextFieldComponent.setImmediate(false);
		storeVariableTextFieldComponent.setWidth("400px");
		storeVariableTextFieldComponent.setHeight("-1px");
		gridLayout1.addComponent(storeVariableTextFieldComponent, 1, 0);
		gridLayout1.setComponentAlignment(storeVariableTextFieldComponent,
				new Alignment(34));

		return gridLayout1;
	}

	@AutoGenerated
	private GridLayout buildGridLayout2() {
		// common part: create layout
		gridLayout2 = new GridLayout();
		gridLayout2.setImmediate(false);
		gridLayout2.setWidth("100.0%");
		gridLayout2.setHeight("-1px");
		gridLayout2.setMargin(false);
		gridLayout2.setSpacing(true);
		gridLayout2.setColumns(2);
		gridLayout2.setRows(2);

		// messageGroupLabel
		messageGroupLabel = new Label();
		messageGroupLabel.setImmediate(false);
		messageGroupLabel.setWidth("-1px");
		messageGroupLabel.setHeight("-1px");
		messageGroupLabel.setValue("!!! Message group to send messages from:");
		gridLayout2.addComponent(messageGroupLabel, 0, 0);

		// messageGroupComboBox
		messageGroupComboBox = new ComboBox();
		messageGroupComboBox.setImmediate(false);
		messageGroupComboBox.setWidth("400px");
		messageGroupComboBox.setHeight("-1px");
		gridLayout2.addComponent(messageGroupComboBox, 1, 0);
		gridLayout2.setComponentAlignment(messageGroupComboBox,
				new Alignment(34));

		// microDialogLabel
		microDialogLabel = new Label();
		microDialogLabel.setImmediate(false);
		microDialogLabel.setWidth("-1px");
		microDialogLabel.setHeight("-1px");
		microDialogLabel.setValue("!!! Micro dialog to start:");
		gridLayout2.addComponent(microDialogLabel, 0, 1);

		// microDialogComboBox
		microDialogComboBox = new ComboBox();
		microDialogComboBox.setImmediate(false);
		microDialogComboBox.setWidth("400px");
		microDialogComboBox.setHeight("-1px");
		gridLayout2.addComponent(microDialogComboBox, 1, 1);
		gridLayout2.setComponentAlignment(microDialogComboBox,
				new Alignment(34));

		return gridLayout2;
	}

	@AutoGenerated
	private GridLayout buildButtonLayout() {
		// common part: create layout
		buttonLayout = new GridLayout();
		buttonLayout.setImmediate(false);
		buttonLayout.setWidth("100.0%");
		buttonLayout.setHeight("-1px");
		buttonLayout.setMargin(true);
		buttonLayout.setSpacing(true);

		// closeButton
		closeButton = new Button();
		closeButton.setCaption("!!! Close");
		closeButton.setIcon(new ThemeResource("img/ok-icon-small.png"));
		closeButton.setImmediate(true);
		closeButton.setWidth("140px");
		closeButton.setHeight("-1px");
		buttonLayout.addComponent(closeButton, 0, 0);
		buttonLayout.setComponentAlignment(closeButton, new Alignment(48));

		return buttonLayout;
	}

}
