package ch.ethz.mc.ui.views.components.interventions.monitoring_messages;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
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
import lombok.val;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the monitoring message groups tab component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class MonitoringMessageGroupsTabComponent extends
		AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;
	@AutoGenerated
	private Button				deleteGroupButton;
	@AutoGenerated
	private Button				moveGroupRightButton;
	@AutoGenerated
	private Button				moveGroupLeftButton;
	@AutoGenerated
	private Button				renameGroupButton;
	@AutoGenerated
	private Button				newGroupButton;
	@AutoGenerated
	private TabSheet			monitoringMessageGroupsTabSheet;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected MonitoringMessageGroupsTabComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(newGroupButton,
				AdminMessageStrings.MONITORING_MESSAGE_GROUP_EDITING__NEW_GROUP);
		localize(
				renameGroupButton,
				AdminMessageStrings.MONITORING_MESSAGE_GROUP_EDITING__RENAME_GROUP);
		localize(
				moveGroupLeftButton,
				AdminMessageStrings.MONITORING_MESSAGE_GROUP_EDITING__MOVE_GROUP_LEFT);
		localize(
				moveGroupRightButton,
				AdminMessageStrings.MONITORING_MESSAGE_GROUP_EDITING__MOVE_GROUP_RIGHT);
		localize(
				deleteGroupButton,
				AdminMessageStrings.MONITORING_MESSAGE_GROUP_EDITING__DELETE_GROUP);

		// set button start state
		setNothingSelected();
	}

	protected void setNothingSelected() {
		renameGroupButton.setEnabled(false);
		deleteGroupButton.setEnabled(false);
		moveGroupLeftButton.setEnabled(false);
		moveGroupRightButton.setEnabled(false);
	}

	protected void setSomethingSelected() {
		renameGroupButton.setEnabled(true);
		deleteGroupButton.setEnabled(true);
		val tabSheet = getMonitoringMessageGroupsTabSheet();
		if (tabSheet.getTabPosition(tabSheet.getTab(tabSheet.getSelectedTab())) > 0) {
			moveGroupLeftButton.setEnabled(true);
		} else {
			moveGroupLeftButton.setEnabled(false);
		}
		if (tabSheet.getTabPosition(tabSheet.getTab(tabSheet.getSelectedTab())) < tabSheet
				.getComponentCount() - 1) {
			moveGroupRightButton.setEnabled(true);
		} else {
			moveGroupRightButton.setEnabled(false);
		}
	}

	public Tab addTabComponent(
			final MonitoringMessageGroup monitoringMessageGroup,
			final ObjectId interventionId) {
		final MonitoringMessageGroupEditComponent monitoringMessageGroupEditComponent = new MonitoringMessageGroupEditComponentWithController(
				monitoringMessageGroup, interventionId);
		monitoringMessageGroupEditComponent.setImmediate(false);
		monitoringMessageGroupEditComponent.setWidth("100.0%");
		monitoringMessageGroupEditComponent.setHeight("-1px");

		// specific table options
		monitoringMessageGroupEditComponent.getMonitoringMessageTable()
				.setSelectable(true);
		monitoringMessageGroupEditComponent.getMonitoringMessageTable()
				.setImmediate(true);

		// Add edit component to tab and return
		return monitoringMessageGroupsTabSheet.addTab(
				monitoringMessageGroupEditComponent,
				monitoringMessageGroup.getName(), null);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(false);

		// top-level component properties
		setWidth("100.0%");
		setHeight("-1px");

		// monitoringMessageGroupsTabSheet
		monitoringMessageGroupsTabSheet = new TabSheet();
		monitoringMessageGroupsTabSheet.setImmediate(true);
		monitoringMessageGroupsTabSheet.setWidth("100.0%");
		monitoringMessageGroupsTabSheet.setHeight("-1px");
		mainLayout.addComponent(monitoringMessageGroupsTabSheet);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);

		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildButtonLayout() {
		// common part: create layout
		buttonLayout = new HorizontalLayout();
		buttonLayout.setImmediate(false);
		buttonLayout.setWidth("-1px");
		buttonLayout.setHeight("-1px");
		buttonLayout.setMargin(true);
		buttonLayout.setSpacing(true);

		// newGroupButton
		newGroupButton = new Button();
		newGroupButton.setCaption("!!! New Group");
		newGroupButton.setIcon(new ThemeResource("img/add-icon-small.png"));
		newGroupButton.setImmediate(true);
		newGroupButton.setWidth("120px");
		newGroupButton.setHeight("-1px");
		buttonLayout.addComponent(newGroupButton);

		// renameGroupButton
		renameGroupButton = new Button();
		renameGroupButton.setCaption("!!! Rename Group");
		renameGroupButton.setImmediate(true);
		renameGroupButton.setWidth("120px");
		renameGroupButton.setHeight("-1px");
		buttonLayout.addComponent(renameGroupButton);

		// moveGroupLeftButton
		moveGroupLeftButton = new Button();
		moveGroupLeftButton.setCaption("!!! Move Group Left");
		moveGroupLeftButton.setIcon(new ThemeResource(
				"img/arrow-left-icon-small.png"));
		moveGroupLeftButton.setImmediate(true);
		moveGroupLeftButton.setWidth("150px");
		moveGroupLeftButton.setHeight("-1px");
		buttonLayout.addComponent(moveGroupLeftButton);

		// moveGroupRightButton
		moveGroupRightButton = new Button();
		moveGroupRightButton.setCaption("!!! Move Group Right");
		moveGroupRightButton.setIcon(new ThemeResource(
				"img/arrow-right-icon-small.png"));
		moveGroupRightButton.setImmediate(true);
		moveGroupRightButton.setWidth("150px");
		moveGroupRightButton.setHeight("-1px");
		buttonLayout.addComponent(moveGroupRightButton);

		// deleteGroupButton
		deleteGroupButton = new Button();
		deleteGroupButton.setCaption("!!! Delete Group");
		deleteGroupButton
				.setIcon(new ThemeResource("img/delete-icon-small.png"));
		deleteGroupButton.setImmediate(true);
		deleteGroupButton.setWidth("120px");
		deleteGroupButton.setHeight("-1px");
		buttonLayout.addComponent(deleteGroupButton);

		return buttonLayout;
	}
}
