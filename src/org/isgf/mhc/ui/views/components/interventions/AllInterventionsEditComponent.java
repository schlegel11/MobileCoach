package org.isgf.mhc.ui.views.components.interventions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.ui.UIIntervention;
import org.isgf.mhc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the all interventions edit component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class AllInterventionsEditComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;
	@AutoGenerated
	private Button				deleteButton;
	@AutoGenerated
	private Button				duplicateButton;
	@AutoGenerated
	private Button				editButton;
	@AutoGenerated
	private Button				renameButton;
	@AutoGenerated
	private Button				exportButton;
	@AutoGenerated
	private Button				importButton;
	@AutoGenerated
	private Button				newButton;
	@AutoGenerated
	private Table				allInterventionsTable;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected AllInterventionsEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(newButton, AdminMessageStrings.GENERAL__NEW);
		localize(importButton, AdminMessageStrings.GENERAL__IMPORT);
		localize(exportButton, AdminMessageStrings.GENERAL__EXPORT);
		localize(renameButton, AdminMessageStrings.GENERAL__RENAME);
		localize(editButton, AdminMessageStrings.GENERAL__EDIT);
		localize(duplicateButton, AdminMessageStrings.GENERAL__DUPLICATE);
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);

		// set button start state
		setNothingSelected();

		// Hide some buttons from normal authors
		if (!getUISession().isAdmin()) {
			newButton.setVisible(false);
			importButton.setVisible(false);
			exportButton.setVisible(false);
			duplicateButton.setVisible(false);
			deleteButton.setVisible(false);
		}

		// set table formatter
		allInterventionsTable
				.setCellStyleGenerator(new Table.CellStyleGenerator() {
					@Override
					public String getStyle(final Table source,
							final Object itemId, final Object propertyId) {
						if (propertyId != null) {
							if (propertyId
									.equals(UIIntervention.INTERVENTION_STATUS)) {
								val uiIntervention = getUIModelObjectFromTableByObjectId(
										source, UIIntervention.class, itemId);
								if (uiIntervention
										.isBooleanInterventionStatus()) {
									return "active";
								} else {
									return "inactive";
								}
							} else if (propertyId
									.equals(UIIntervention.MESSAGING_STATUS)) {
								val uiIntervention = getUIModelObjectFromTableByObjectId(
										source, UIIntervention.class, itemId);
								if (uiIntervention.isBooleanMessagingStatus()) {
									return "active";
								} else {
									return "inactive";
								}
							}
						}

						return null;
					}
				});
	}

	protected void setNothingSelected() {
		exportButton.setEnabled(false);
		renameButton.setEnabled(false);
		editButton.setEnabled(false);
		duplicateButton.setEnabled(false);
		deleteButton.setEnabled(false);
	}

	protected void setSomethingSelected() {
		exportButton.setEnabled(true);
		renameButton.setEnabled(true);
		editButton.setEnabled(true);
		duplicateButton.setEnabled(true);
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

		// allInterventionsTable
		allInterventionsTable = new Table();
		allInterventionsTable.setImmediate(false);
		allInterventionsTable.setWidth("100.0%");
		allInterventionsTable.setHeight("250px");
		mainLayout.addComponent(allInterventionsTable);

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

		// importButton
		importButton = new Button();
		importButton.setCaption("!!! Import");
		importButton.setImmediate(true);
		importButton.setWidth("100px");
		importButton.setHeight("-1px");
		buttonLayout.addComponent(importButton);

		// exportButton
		exportButton = new Button();
		exportButton.setCaption("!!! Export");
		exportButton.setImmediate(true);
		exportButton.setWidth("100px");
		exportButton.setHeight("-1px");
		buttonLayout.addComponent(exportButton);

		// renameButton
		renameButton = new Button();
		renameButton.setCaption("!!! Rename");
		renameButton.setImmediate(true);
		renameButton.setWidth("100px");
		renameButton.setHeight("-1px");
		buttonLayout.addComponent(renameButton);

		// editButton
		editButton = new Button();
		editButton.setCaption("!!! Edit");
		editButton.setIcon(new ThemeResource("img/edit-icon-small.png"));
		editButton.setImmediate(true);
		editButton.setWidth("100px");
		editButton.setHeight("-1px");
		buttonLayout.addComponent(editButton);

		// duplicateButton
		duplicateButton = new Button();
		duplicateButton.setCaption("!!! Duplicate");
		duplicateButton.setImmediate(true);
		duplicateButton.setWidth("100px");
		duplicateButton.setHeight("-1px");
		buttonLayout.addComponent(duplicateButton);

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
