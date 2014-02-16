package org.isgf.mhc.ui.views.components.access_control;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the access control edit component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class AccessControlEditComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;
	@AutoGenerated
	private Button				deleteButton;
	@AutoGenerated
	private Button				setPasswordButton;
	@AutoGenerated
	private Button				makeAdminButton;
	@AutoGenerated
	private Button				makeAuthorButton;
	@AutoGenerated
	private Button				newButton;
	@AutoGenerated
	private Table				accountsTable;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected AccessControlEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(newButton, AdminMessageStrings.GENERAL__NEW);
		localize(makeAuthorButton,
				AdminMessageStrings.ACCESS_CONTROL_TAB__MAKE_AUTHOR);
		localize(makeAdminButton,
				AdminMessageStrings.ACCESS_CONTROL_TAB__MAKE_ADMIN);
		localize(setPasswordButton,
				AdminMessageStrings.ACCESS_CONTROL_TAB__SET_PASSWORD);
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);

		// set button start state
		setNothingSelected();
	}

	protected void setNothingSelected() {
		makeAuthorButton.setEnabled(false);
		makeAdminButton.setEnabled(false);
		setPasswordButton.setEnabled(false);
		deleteButton.setEnabled(false);
	}

	protected void setSomethingSelected() {
		makeAuthorButton.setEnabled(true);
		makeAdminButton.setEnabled(true);
		setPasswordButton.setEnabled(true);
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

		// accountsTable
		accountsTable = new Table();
		accountsTable.setImmediate(false);
		accountsTable.setWidth("100.0%");
		accountsTable.setHeight("150px");
		mainLayout.addComponent(accountsTable);

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

		// makeAuthorButton
		makeAuthorButton = new Button();
		makeAuthorButton.setCaption("!!! Make Author");
		makeAuthorButton.setImmediate(true);
		makeAuthorButton.setWidth("100px");
		makeAuthorButton.setHeight("-1px");
		buttonLayout.addComponent(makeAuthorButton);

		// makeAdminButton
		makeAdminButton = new Button();
		makeAdminButton.setCaption("!!! Make Admin");
		makeAdminButton.setImmediate(true);
		makeAdminButton.setWidth("100px");
		makeAdminButton.setHeight("-1px");
		buttonLayout.addComponent(makeAdminButton);

		// setPasswordButton
		setPasswordButton = new Button();
		setPasswordButton.setCaption("!!! Set Password");
		setPasswordButton.setImmediate(true);
		setPasswordButton.setWidth("100px");
		setPasswordButton.setHeight("-1px");
		buttonLayout.addComponent(setPasswordButton);

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
