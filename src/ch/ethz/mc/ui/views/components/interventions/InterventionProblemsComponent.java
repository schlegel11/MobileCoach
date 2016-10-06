package ch.ethz.mc.ui.views.components.interventions;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab at the Health-IS Lab
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
import ch.ethz.mc.ui.views.components.AbstractClosableEditComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the intervention problems component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class InterventionProblemsComponent extends
		AbstractClosableEditComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private VerticalLayout		closeButtonLayout;
	@AutoGenerated
	private Button				closeButton;
	@AutoGenerated
	private Table				messageDialogTable;
	@AutoGenerated
	private Label				messageDialogLabel;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;
	@AutoGenerated
	private Button				refreshButton;
	@AutoGenerated
	private Button				sendMessageButton;
	@AutoGenerated
	private Button				solveButton;
	@AutoGenerated
	private Table				dialogMessagesTable;
	@AutoGenerated
	private Label				messageDialogProblemsLabel;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected InterventionProblemsComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(messageDialogProblemsLabel,
				AdminMessageStrings.PROBLEMS__DIALOG_MESSAGES_LABEL);
		localize(solveButton, AdminMessageStrings.GENERAL__SOLVE);
		localize(sendMessageButton, AdminMessageStrings.PROBLEMS__SEND_MESSAGE);
		localize(refreshButton, AdminMessageStrings.GENERAL__REFRESH);
		localize(closeButton, AdminMessageStrings.GENERAL__CLOSE);
		localize(messageDialogLabel,
				AdminMessageStrings.PROBLEMS__MESSAGE_DIALOG_LABEL);

		// set button start state
		setNothingSelected();
	}

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		closeButton.addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(final ClickListener clickListener) {
		// Not required
	}

	protected void setNothingSelected() {
		solveButton.setEnabled(false);
		sendMessageButton.setEnabled(false);
	}

	protected void setSomethingSelected() {
		solveButton.setEnabled(true);
		sendMessageButton.setEnabled(true);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("900px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("900px");
		setHeight("-1px");

		// messageDialogProblemsLabel
		messageDialogProblemsLabel = new Label();
		messageDialogProblemsLabel.setStyleName("bold");
		messageDialogProblemsLabel.setImmediate(false);
		messageDialogProblemsLabel.setWidth("-1px");
		messageDialogProblemsLabel.setHeight("-1px");
		messageDialogProblemsLabel
				.setValue("!!! The following replies could not be automatically processed:");
		mainLayout.addComponent(messageDialogProblemsLabel);

		// dialogMessagesTable
		dialogMessagesTable = new Table();
		dialogMessagesTable.setImmediate(false);
		dialogMessagesTable.setWidth("100.0%");
		dialogMessagesTable.setHeight("150px");
		mainLayout.addComponent(dialogMessagesTable);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);

		// messageDialogLabel
		messageDialogLabel = new Label();
		messageDialogLabel.setStyleName("bold");
		messageDialogLabel.setImmediate(false);
		messageDialogLabel.setWidth("-1px");
		messageDialogLabel.setHeight("-1px");
		messageDialogLabel
				.setValue("!!! Message dialog of selected participant:");
		mainLayout.addComponent(messageDialogLabel);

		// messageDialogTable
		messageDialogTable = new Table();
		messageDialogTable.setImmediate(false);
		messageDialogTable.setWidth("100.0%");
		messageDialogTable.setHeight("150px");
		mainLayout.addComponent(messageDialogTable);

		// closeButtonLayout
		closeButtonLayout = buildCloseButtonLayout();
		mainLayout.addComponent(closeButtonLayout);
		mainLayout.setComponentAlignment(closeButtonLayout, new Alignment(48));

		return mainLayout;
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

		// solveButton
		solveButton = new Button();
		solveButton.setCaption("!!! Solve");
		solveButton.setIcon(new ThemeResource("img/ok-icon-small.png"));
		solveButton.setImmediate(true);
		solveButton.setWidth("100px");
		solveButton.setHeight("-1px");
		buttonLayout.addComponent(solveButton);

		// sendMessageButton
		sendMessageButton = new Button();
		sendMessageButton.setCaption("!!! Send Message");
		sendMessageButton.setIcon(new ThemeResource(
				"img/message-icon-small.png"));
		sendMessageButton.setImmediate(true);
		sendMessageButton.setWidth("150px");
		sendMessageButton.setHeight("-1px");
		buttonLayout.addComponent(sendMessageButton);

		// refreshButton
		refreshButton = new Button();
		refreshButton.setCaption("!!! Refresh");
		refreshButton.setIcon(new ThemeResource("img/loading-icon-small.png"));
		refreshButton.setImmediate(true);
		refreshButton.setWidth("100px");
		refreshButton.setHeight("-1px");
		buttonLayout.addComponent(refreshButton);

		return buttonLayout;
	}

	@AutoGenerated
	private VerticalLayout buildCloseButtonLayout() {
		// common part: create layout
		closeButtonLayout = new VerticalLayout();
		closeButtonLayout.setImmediate(false);
		closeButtonLayout.setWidth("100.0%");
		closeButtonLayout.setHeight("-1px");
		closeButtonLayout.setMargin(true);

		// closeButton
		closeButton = new Button();
		closeButton.setCaption("!!! Close");
		closeButton.setImmediate(true);
		closeButton.setWidth("100px");
		closeButton.setHeight("-1px");
		closeButtonLayout.addComponent(closeButton);
		closeButtonLayout.setComponentAlignment(closeButton, new Alignment(48));

		return closeButtonLayout;
	}

}
