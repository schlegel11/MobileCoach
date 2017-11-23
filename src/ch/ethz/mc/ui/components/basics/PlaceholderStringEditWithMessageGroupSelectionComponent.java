package ch.ethz.mc.ui.components.basics;

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
import java.util.List;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.ui.UIMonitoringMessageGroup;
import ch.ethz.mc.ui.components.AbstractStringValueEditComponent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Provides a string edit window for placeholder strings with a checkbox that
 * can be used for not a specific reason
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
@Log4j2
public class PlaceholderStringEditWithMessageGroupSelectionComponent
		extends AbstractStringValueEditComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout			mainLayout;
	@AutoGenerated
	private GridLayout				buttonLayout;
	@AutoGenerated
	private Button					okButton;
	@AutoGenerated
	private Button					cancelButton;
	@AutoGenerated
	private VerticalLayout			verticalLayout_2;
	@AutoGenerated
	private GridLayout				gridLayout_1;
	@Getter
	@AutoGenerated
	private CheckBox				sendToSupervisorComboBox;
	@AutoGenerated
	private Slider					minutesUntilHandledAsNotAnsweredSlider;
	@AutoGenerated
	private Label					minutesUntilHandledAsNotAnsweredLabel;
	@AutoGenerated
	private ComboBox				monitoringMessageGroupComboBox;
	@AutoGenerated
	private Label					messageGroupLabel;
	@AutoGenerated
	private Label					orLabel;
	@AutoGenerated
	private HorizontalLayout		editAreaLayout;
	@AutoGenerated
	private ListSelect				variableListSelect;
	@AutoGenerated
	private Embedded				arrowLeftIcon;
	@AutoGenerated
	private TextArea				stringTextArea;
	@Getter
	private MonitoringMessageGroup	selectedMonitoringMessageGroup	= null;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 *
	 * @param allPossibleMessageGroups
	 */
	public PlaceholderStringEditWithMessageGroupSelectionComponent(
			final Iterable<MonitoringMessageGroup> monitoringMessageGroups) {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(okButton, AdminMessageStrings.GENERAL__OK);
		localize(cancelButton, AdminMessageStrings.GENERAL__CANCEL);
		localize(variableListSelect,
				AdminMessageStrings.PLACEHOLDER_STRING_EDITOR__SELECT_VARIABLE);
		localize(orLabel, AdminMessageStrings.GENERAL__OR);
		localize(messageGroupLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__MESSAGE_GROUP_TO_SEND_MESSAGES_FROM);
		localize(minutesUntilHandledAsNotAnsweredLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__MINUTES_AFTER_SENDING_UNTIL_HANDLED_AS_NOT_ANSWERED);
		localize(sendToSupervisorComboBox,
				AdminMessageStrings.MONITORING_RULE_EDITING__SEND_TO_SUPERVISOR);

		stringTextArea.setImmediate(true);

		monitoringMessageGroupComboBox.setImmediate(true);
		monitoringMessageGroupComboBox.setNullSelectionAllowed(true);
		monitoringMessageGroupComboBox.setTextInputAllowed(false);

		variableListSelect.setNullSelectionAllowed(false);
		variableListSelect.setImmediate(true);
		variableListSelect.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final String selectedVariable = (String) event.getProperty()
						.getValue();
				if (selectedVariable != null) {
					log.debug(
							"Former text value of string text area is {} and cursor position is {}",
							stringTextArea.getValue(),
							stringTextArea.getCursorPosition());
					try {
						stringTextArea.setValue(stringTextArea.getValue()
								.substring(0,
										stringTextArea.getCursorPosition())
								+ selectedVariable
								+ stringTextArea.getValue().substring(
										stringTextArea.getCursorPosition()));

						stringTextArea.setCursorPosition(
								stringTextArea.getCursorPosition()
										+ selectedVariable.length());
					} catch (final Exception e) {
						log.warn(
								"Error occured while setting variable to string text area...fixing by setting text to the beginning (Workaround for Vaadin time shift)");

						stringTextArea.setValue(
								stringTextArea.getValue() + selectedVariable);

						stringTextArea.setCursorPosition(
								stringTextArea.getValue().length());
					}

					variableListSelect.unselect(selectedVariable);
				}
			}
		});

		// Add enter as click shortcut for default button
		okButton.setClickShortcut(KeyCode.ENTER);
		// Add ESC as click shortcut for cancel button
		cancelButton.setClickShortcut(KeyCode.ESCAPE);

		// Handle combo box
		for (val monitoringMessageGroup : monitoringMessageGroups) {
			val uiMonitoringMessageGroup = monitoringMessageGroup
					.toUIModelObject();
			monitoringMessageGroupComboBox.addItem(uiMonitoringMessageGroup);
		}
		monitoringMessageGroupComboBox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						final UIMonitoringMessageGroup uiMonitoringMessageGroup = (UIMonitoringMessageGroup) event
								.getProperty().getValue();

						if (uiMonitoringMessageGroup == null) {
							selectedMonitoringMessageGroup = null;

							getStringTextArea().setEnabled(true);
							getVariableListSelect().setEnabled(true);
						} else {
							selectedMonitoringMessageGroup = uiMonitoringMessageGroup
									.getRelatedModelObject(
											MonitoringMessageGroup.class);

							getStringTextArea().setValue("");
							getStringTextArea().setEnabled(false);
							getVariableListSelect().setEnabled(false);
						}

						log.debug(
								"Adjust related monitoring message group to {}",
								selectedMonitoringMessageGroup);

						if (selectedMonitoringMessageGroup != null
								&& selectedMonitoringMessageGroup
										.isMessagesExpectAnswer()
								&& sendToSupervisorComboBox.isEnabled()) {
							if (sendToSupervisorComboBox.getValue()) {
								sendToSupervisorComboBox.setValue(false);
							}
							sendToSupervisorComboBox.setEnabled(false);
						} else if (!sendToSupervisorComboBox.isEnabled()) {
							sendToSupervisorComboBox.setEnabled(true);
						}

						if (selectedMonitoringMessageGroup != null
								&& selectedMonitoringMessageGroup
										.isMessagesExpectAnswer()) {
							minutesUntilHandledAsNotAnsweredSlider
									.setEnabled(true);
						} else {
							minutesUntilHandledAsNotAnsweredSlider
									.setEnabled(false);
						}
					}
				});

		// Handle slider
		minutesUntilHandledAsNotAnsweredSlider.setImmediate(true);
		minutesUntilHandledAsNotAnsweredSlider.setMin(
				ImplementationConstants.MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MIN);
		minutesUntilHandledAsNotAnsweredSlider.setMax(
				ImplementationConstants.MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX_MANUAL_MESSAGE);
		minutesUntilHandledAsNotAnsweredSlider.setEnabled(false);

		stringTextArea.focus();
	}

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		okButton.addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(
			final ClickListener clickListener) {
		cancelButton.addClickListener(clickListener);
	}

	@Override
	public void setStringValue(final String value) {
		stringTextArea.setValue(value);
	}

	@Override
	public String getStringValue() {
		return stringTextArea.getValue();
	}

	@Override
	public void addVariables(final List<String> variables) {
		for (val variable : variables) {
			variableListSelect.addItem(variable);
		}
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("750px");
		mainLayout.setHeight("400px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("750px");
		setHeight("400px");

		// editAreaLayout
		editAreaLayout = buildEditAreaLayout();
		mainLayout.addComponent(editAreaLayout);
		mainLayout.setExpandRatio(editAreaLayout, 1.0f);

		// verticalLayout_2
		verticalLayout_2 = buildVerticalLayout_2();
		mainLayout.addComponent(verticalLayout_2);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, new Alignment(48));

		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildEditAreaLayout() {
		// common part: create layout
		editAreaLayout = new HorizontalLayout();
		editAreaLayout.setImmediate(false);
		editAreaLayout.setWidth("100.0%");
		editAreaLayout.setHeight("100.0%");
		editAreaLayout.setMargin(false);
		editAreaLayout.setSpacing(true);

		// stringTextArea
		stringTextArea = new TextArea();
		stringTextArea.setImmediate(false);
		stringTextArea.setWidth("100.0%");
		stringTextArea.setHeight("100.0%");
		stringTextArea.setNullSettingAllowed(true);
		editAreaLayout.addComponent(stringTextArea);
		editAreaLayout.setExpandRatio(stringTextArea, 0.6f);
		editAreaLayout.setComponentAlignment(stringTextArea, new Alignment(33));

		// arrowLeftIcon
		arrowLeftIcon = new Embedded();
		arrowLeftIcon.setImmediate(false);
		arrowLeftIcon.setWidth("32px");
		arrowLeftIcon.setHeight("32px");
		arrowLeftIcon.setSource(new ThemeResource("img/arrow-left-icon.png"));
		arrowLeftIcon.setType(1);
		arrowLeftIcon.setMimeType("image/png");
		editAreaLayout.addComponent(arrowLeftIcon);
		editAreaLayout.setComponentAlignment(arrowLeftIcon, new Alignment(48));

		// variableListSelect
		variableListSelect = new ListSelect();
		variableListSelect
				.setCaption("!!! Select variable to add to the text:");
		variableListSelect.setImmediate(false);
		variableListSelect.setWidth("100.0%");
		variableListSelect.setHeight("100.0%");
		editAreaLayout.addComponent(variableListSelect);
		editAreaLayout.setExpandRatio(variableListSelect, 0.4f);
		editAreaLayout.setComponentAlignment(variableListSelect,
				new Alignment(48));

		return editAreaLayout;
	}

	@AutoGenerated
	private VerticalLayout buildVerticalLayout_2() {
		// common part: create layout
		verticalLayout_2 = new VerticalLayout();
		verticalLayout_2.setImmediate(false);
		verticalLayout_2.setWidth("100.0%");
		verticalLayout_2.setHeight("-1px");
		verticalLayout_2.setMargin(false);
		verticalLayout_2.setSpacing(true);

		// orLabel
		orLabel = new Label();
		orLabel.setImmediate(false);
		orLabel.setWidth("-1px");
		orLabel.setHeight("-1px");
		orLabel.setValue("!!! OR");
		verticalLayout_2.addComponent(orLabel);
		verticalLayout_2.setComponentAlignment(orLabel, new Alignment(48));

		// gridLayout_1
		gridLayout_1 = buildGridLayout_1();
		verticalLayout_2.addComponent(gridLayout_1);

		return verticalLayout_2;
	}

	@AutoGenerated
	private GridLayout buildGridLayout_1() {
		// common part: create layout
		gridLayout_1 = new GridLayout();
		gridLayout_1.setImmediate(false);
		gridLayout_1.setWidth("100.0%");
		gridLayout_1.setHeight("-1px");
		gridLayout_1.setMargin(false);
		gridLayout_1.setSpacing(true);
		gridLayout_1.setColumns(2);
		gridLayout_1.setRows(3);

		// messageGroupLabel
		messageGroupLabel = new Label();
		messageGroupLabel.setImmediate(false);
		messageGroupLabel.setWidth("-1px");
		messageGroupLabel.setHeight("-1px");
		messageGroupLabel.setValue("!!! Message group to send messages from:");
		gridLayout_1.addComponent(messageGroupLabel, 0, 0);

		// monitoringMessageGroupComboBox
		monitoringMessageGroupComboBox = new ComboBox();
		monitoringMessageGroupComboBox.setImmediate(false);
		monitoringMessageGroupComboBox.setWidth("270px");
		monitoringMessageGroupComboBox.setHeight("-1px");
		gridLayout_1.addComponent(monitoringMessageGroupComboBox, 1, 0);
		gridLayout_1.setComponentAlignment(monitoringMessageGroupComboBox,
				new Alignment(34));

		// minutesUntilHandledAsNotAnsweredLabel
		minutesUntilHandledAsNotAnsweredLabel = new Label();
		minutesUntilHandledAsNotAnsweredLabel.setImmediate(false);
		minutesUntilHandledAsNotAnsweredLabel.setWidth("-1px");
		minutesUntilHandledAsNotAnsweredLabel.setHeight("-1px");
		minutesUntilHandledAsNotAnsweredLabel
				.setValue("!!! Minutes until handled as not answered:");
		gridLayout_1.addComponent(minutesUntilHandledAsNotAnsweredLabel, 0, 1);

		// minutesUntilHandledAsNotAnsweredSlider
		minutesUntilHandledAsNotAnsweredSlider = new Slider();
		minutesUntilHandledAsNotAnsweredSlider.setImmediate(false);
		minutesUntilHandledAsNotAnsweredSlider.setWidth("270px");
		minutesUntilHandledAsNotAnsweredSlider.setHeight("-1px");
		gridLayout_1.addComponent(minutesUntilHandledAsNotAnsweredSlider, 1, 1);
		gridLayout_1.setComponentAlignment(
				minutesUntilHandledAsNotAnsweredSlider, new Alignment(34));

		// sendToSupervisorComboBox
		sendToSupervisorComboBox = new CheckBox();
		sendToSupervisorComboBox.setCaption("!!! Check Box Caption");
		sendToSupervisorComboBox.setImmediate(false);
		sendToSupervisorComboBox.setWidth("-1px");
		sendToSupervisorComboBox.setHeight("-1px");
		gridLayout_1.addComponent(sendToSupervisorComboBox, 0, 2);

		return gridLayout_1;
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
		buttonLayout.setColumns(2);

		// cancelButton
		cancelButton = new Button();
		cancelButton.setCaption("!!! Cancel");
		cancelButton.setIcon(new ThemeResource("img/cancel-icon-small.png"));
		cancelButton.setImmediate(true);
		cancelButton.setWidth("140px");
		cancelButton.setHeight("-1px");
		buttonLayout.addComponent(cancelButton, 0, 0);
		buttonLayout.setComponentAlignment(cancelButton, new Alignment(34));

		// okButton
		okButton = new Button();
		okButton.setCaption("!!! OK");
		okButton.setIcon(new ThemeResource("img/ok-icon-small.png"));
		okButton.setImmediate(true);
		okButton.setWidth("140px");
		okButton.setHeight("-1px");
		buttonLayout.addComponent(okButton, 1, 0);
		buttonLayout.setComponentAlignment(okButton, new Alignment(9));

		return buttonLayout;
	}
}
