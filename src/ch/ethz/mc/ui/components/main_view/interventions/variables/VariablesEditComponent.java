package ch.ethz.mc.ui.components.main_view.interventions.variables;

/* ##LICENSE## */
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the intervention variables edit component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class VariablesEditComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;
	@AutoGenerated
	private Button				deleteButton;
	@AutoGenerated
	private Button				editButton;
	@AutoGenerated
	private Button				switchAccessButton;
	@AutoGenerated
	private Button				switchPrivacyButton;
	@AutoGenerated
	private Button				renameButton;
	@AutoGenerated
	private Button				newButton;
	@AutoGenerated
	private Table				variablesTable;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	protected VariablesEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(newButton, AdminMessageStrings.GENERAL__NEW);
		localize(renameButton, AdminMessageStrings.GENERAL__RENAME);
		localize(editButton, AdminMessageStrings.GENERAL__EDIT);
		localize(switchPrivacyButton,
				AdminMessageStrings.INTERVENTION_VARIABLES_EDITING__SWITCH_PRIVACY_BUTTON);
		localize(switchAccessButton,
				AdminMessageStrings.INTERVENTION_VARIABLES_EDITING__SWITCH_ACCESS_BUTTON);
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);

		// set button start state
		setNothingSelected();
	}

	protected void setNothingSelected() {
		renameButton.setEnabled(false);
		editButton.setEnabled(false);
		switchPrivacyButton.setEnabled(false);
		switchAccessButton.setEnabled(false);
		deleteButton.setEnabled(false);
	}

	protected void setSomethingSelected() {
		renameButton.setEnabled(true);
		editButton.setEnabled(true);
		switchPrivacyButton.setEnabled(true);
		switchAccessButton.setEnabled(true);
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

		// variablesTable
		variablesTable = new Table();
		variablesTable.setImmediate(false);
		variablesTable.setWidth("100.0%");
		variablesTable.setHeight("350px");
		mainLayout.addComponent(variablesTable);

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

		// renameButton
		renameButton = new Button();
		renameButton.setCaption("!!! Rename");
		renameButton.setImmediate(true);
		renameButton.setWidth("100px");
		renameButton.setHeight("-1px");
		buttonLayout.addComponent(renameButton);

		// switchPrivacyButton
		switchPrivacyButton = new Button();
		switchPrivacyButton.setCaption("!!! Switch Privacy");
		switchPrivacyButton.setImmediate(true);
		switchPrivacyButton.setWidth("130px");
		switchPrivacyButton.setHeight("-1px");
		buttonLayout.addComponent(switchPrivacyButton);

		// switchAccessButton
		switchAccessButton = new Button();
		switchAccessButton.setCaption("!!! Switch Access");
		switchAccessButton.setImmediate(true);
		switchAccessButton.setWidth("130px");
		switchAccessButton.setHeight("-1px");
		buttonLayout.addComponent(switchAccessButton);

		// editButton
		editButton = new Button();
		editButton.setCaption("!!! Edit");
		editButton.setIcon(new ThemeResource("img/edit-icon-small.png"));
		editButton.setImmediate(true);
		editButton.setWidth("100px");
		editButton.setHeight("-1px");
		buttonLayout.addComponent(editButton);

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
