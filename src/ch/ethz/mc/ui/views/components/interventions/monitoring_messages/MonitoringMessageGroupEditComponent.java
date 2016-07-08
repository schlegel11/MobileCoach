package ch.ethz.mc.ui.views.components.interventions.monitoring_messages;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
 *
 * For details see README.md file in the root folder of this project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.views.components.AbstractCustomComponent;
import ch.ethz.mc.ui.views.components.basics.VariableTextFieldComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the monitoring message group edit component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class MonitoringMessageGroupEditComponent extends
AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout				mainLayout;
	@AutoGenerated
	private HorizontalLayout			buttonLayout;
	@AutoGenerated
	private Button						deleteButton;
	@AutoGenerated
	private Button						moveDownButton;
	@AutoGenerated
	private Button						moveUpButton;
	@AutoGenerated
	private Button						editButton;
	@AutoGenerated
	private Button						newButton;
	@AutoGenerated
	private Table						monitoringMessageTable;
	@AutoGenerated
	private HorizontalLayout			validationExpresionLayout;
	@AutoGenerated
	private VariableTextFieldComponent	validationExpressionTextFieldComponent;
	@AutoGenerated
	private Label						validationExpressionLabel;
	@AutoGenerated
	private CheckBox					sendSamePositionIfSendingAsReplyCheckBox;
	@AutoGenerated
	private CheckBox					randomOrderCheckBox;
	@AutoGenerated
	private CheckBox					messagesExpectAnswerCheckBox;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected MonitoringMessageGroupEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(
				messagesExpectAnswerCheckBox,
				AdminMessageStrings.MONITORING_MESSAGE_GROUP_EDITING__MESSAGES_EXPECT_TO_BE_ANSWERED_BY_PARTICIPANT);
		localize(
				randomOrderCheckBox,
				AdminMessageStrings.MONITORING_MESSAGE_GROUP_EDITING__SEND_MESSAGE_IN_RANDOM_ORDER);
		localize(
				sendSamePositionIfSendingAsReplyCheckBox,
				AdminMessageStrings.MONITORING_MESSAGE_GROUP_EDITING__SEND_SAME_POSITION_IF_SENDING_AS_REPLY);

		localize(
				validationExpressionLabel,
				AdminMessageStrings.MONITORING_MESSAGE_GROUP_EDITING__VALIDATION_EXPRESSION_LABEL);

		localize(newButton, AdminMessageStrings.GENERAL__NEW);
		localize(editButton, AdminMessageStrings.GENERAL__EDIT);
		localize(moveUpButton, AdminMessageStrings.GENERAL__MOVE_UP);
		localize(moveDownButton, AdminMessageStrings.GENERAL__MOVE_DOWN);
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);

		sendSamePositionIfSendingAsReplyCheckBox.setImmediate(true);
		randomOrderCheckBox.setImmediate(true);
		messagesExpectAnswerCheckBox.setImmediate(true);

		// set button start state
		setNothingSelected();
	}

	protected void setNothingSelected() {
		editButton.setEnabled(false);
		moveUpButton.setEnabled(false);
		moveDownButton.setEnabled(false);
		deleteButton.setEnabled(false);
	}

	protected void setSomethingSelected() {
		editButton.setEnabled(true);
		moveUpButton.setEnabled(true);
		moveDownButton.setEnabled(true);
		deleteButton.setEnabled(true);
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

		// messagesExpectAnswerCheckBox
		messagesExpectAnswerCheckBox = new CheckBox();
		messagesExpectAnswerCheckBox
		.setCaption("!!! The messages in this group expect to be answered by the participant");
		messagesExpectAnswerCheckBox.setImmediate(false);
		messagesExpectAnswerCheckBox.setWidth("100.0%");
		messagesExpectAnswerCheckBox.setHeight("-1px");
		mainLayout.addComponent(messagesExpectAnswerCheckBox);

		// randomOrderCheckBox
		randomOrderCheckBox = new CheckBox();
		randomOrderCheckBox.setCaption("!!! Send messages in random order");
		randomOrderCheckBox.setImmediate(false);
		randomOrderCheckBox.setWidth("100.0%");
		randomOrderCheckBox.setHeight("-1px");
		mainLayout.addComponent(randomOrderCheckBox);

		// sendSamePositionIfSendingAsReplyCheckBox
		sendSamePositionIfSendingAsReplyCheckBox = new CheckBox();
		sendSamePositionIfSendingAsReplyCheckBox
		.setCaption("!!! Send message from same position if sending as reply to former message and answer");
		sendSamePositionIfSendingAsReplyCheckBox.setImmediate(false);
		sendSamePositionIfSendingAsReplyCheckBox.setWidth("100.0%");
		sendSamePositionIfSendingAsReplyCheckBox.setHeight("-1px");
		mainLayout.addComponent(sendSamePositionIfSendingAsReplyCheckBox);

		// validationExpresionLayout
		validationExpresionLayout = buildValidationExpresionLayout();
		mainLayout.addComponent(validationExpresionLayout);

		// monitoringMessageTable
		monitoringMessageTable = new Table();
		monitoringMessageTable.setImmediate(false);
		monitoringMessageTable.setWidth("100.0%");
		monitoringMessageTable.setHeight("250px");
		mainLayout.addComponent(monitoringMessageTable);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);

		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildValidationExpresionLayout() {
		// common part: create layout
		validationExpresionLayout = new HorizontalLayout();
		validationExpresionLayout.setImmediate(false);
		validationExpresionLayout.setWidth("100.0%");
		validationExpresionLayout.setHeight("-1px");
		validationExpresionLayout.setMargin(false);

		// validationExpressionLabel
		validationExpressionLabel = new Label();
		validationExpressionLabel.setImmediate(false);
		validationExpressionLabel.setWidth("-1px");
		validationExpressionLabel.setHeight("-1px");
		validationExpressionLabel
		.setValue("!!! Expression to validate result as correct (optional):");
		validationExpresionLayout.addComponent(validationExpressionLabel);

		// validationExpressionTextFieldComponent
		validationExpressionTextFieldComponent = new VariableTextFieldComponent();
		validationExpressionTextFieldComponent.setImmediate(false);
		validationExpressionTextFieldComponent.setWidth("400px");
		validationExpressionTextFieldComponent.setHeight("-1px");
		validationExpresionLayout
		.addComponent(validationExpressionTextFieldComponent);
		validationExpresionLayout.setComponentAlignment(
				validationExpressionTextFieldComponent, new Alignment(6));

		return validationExpresionLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildButtonLayout() {
		// common part: create layout
		buttonLayout = new HorizontalLayout();
		buttonLayout.setImmediate(false);
		buttonLayout.setWidth("-1px");
		buttonLayout.setHeight("-1px");
		buttonLayout.setMargin(false);
		buttonLayout.setSpacing(true);

		// newButton
		newButton = new Button();
		newButton.setCaption("!!! New");
		newButton.setIcon(new ThemeResource("img/add-icon-small.png"));
		newButton.setImmediate(true);
		newButton.setWidth("100px");
		newButton.setHeight("-1px");
		buttonLayout.addComponent(newButton);

		// editButton
		editButton = new Button();
		editButton.setCaption("!!! Edit");
		editButton.setIcon(new ThemeResource("img/edit-icon-small.png"));
		editButton.setImmediate(true);
		editButton.setWidth("100px");
		editButton.setHeight("-1px");
		buttonLayout.addComponent(editButton);

		// moveUpButton
		moveUpButton = new Button();
		moveUpButton.setCaption("!!! Move Up");
		moveUpButton.setIcon(new ThemeResource("img/arrow-up-icon-small.png"));
		moveUpButton.setImmediate(true);
		moveUpButton.setWidth("120px");
		moveUpButton.setHeight("-1px");
		buttonLayout.addComponent(moveUpButton);

		// moveDownButton
		moveDownButton = new Button();
		moveDownButton.setCaption("!!! Move Down");
		moveDownButton.setIcon(new ThemeResource(
				"img/arrow-down-icon-small.png"));
		moveDownButton.setImmediate(true);
		moveDownButton.setWidth("120px");
		moveDownButton.setHeight("-1px");
		buttonLayout.addComponent(moveDownButton);

		// deleteButton
		deleteButton = new Button();
		deleteButton.setCaption("!!! Delete");
		deleteButton.setIcon(new ThemeResource("img/delete-icon-small.png"));
		deleteButton.setImmediate(true);
		deleteButton.setWidth("100px");
		deleteButton.setHeight("-1px");
		buttonLayout.addComponent(deleteButton);

		return buttonLayout;
	}

}
