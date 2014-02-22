package org.isgf.mhc.ui.views.components.interventions;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
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
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;
	@AutoGenerated
	private Button				deleteButton;
	@AutoGenerated
	private Button				moveDownButton;
	@AutoGenerated
	private Button				moveUpButton;
	@AutoGenerated
	private Button				editButton;
	@AutoGenerated
	private Button				newButton;
	@AutoGenerated
	private Table				monitoringMessageTable;
	@AutoGenerated
	private CheckBox			randomOrderCheckBox;

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
				randomOrderCheckBox,
				AdminMessageStrings.MONITORING_MESSAGE_GROUP_EDITING__SEND_MESSAGE_IN_RANDOM_ORDER);

		localize(newButton, AdminMessageStrings.GENERAL__NEW);
		localize(editButton, AdminMessageStrings.GENERAL__EDIT);
		localize(moveUpButton, AdminMessageStrings.GENERAL__MOVE_UP);
		localize(moveDownButton, AdminMessageStrings.GENERAL__MOVE_DOWN);
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);

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

		// randomOrderCheckBox
		randomOrderCheckBox = new CheckBox();
		randomOrderCheckBox.setCaption("!!! Send messages in random order");
		randomOrderCheckBox.setImmediate(false);
		randomOrderCheckBox.setWidth("100.0%");
		randomOrderCheckBox.setHeight("-1px");
		mainLayout.addComponent(randomOrderCheckBox);

		// monitoringMessageTable
		monitoringMessageTable = new Table();
		monitoringMessageTable.setImmediate(false);
		monitoringMessageTable.setWidth("100.0%");
		monitoringMessageTable.setHeight("150px");
		mainLayout.addComponent(monitoringMessageTable);

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
