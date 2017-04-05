package ch.ethz.mc.ui.views.components.interventions;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab
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
import lombok.val;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.ui.UIIntervention;
import ch.ethz.mc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
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
	private Button				openModuleButton;
	@AutoGenerated
	private Table				modulesTable;
	@AutoGenerated
	private Label				modulesLabel;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;
	@AutoGenerated
	private Button				deleteButton;
	@AutoGenerated
	private Button				duplicateButton;
	@AutoGenerated
	private Button				editButton;
	@AutoGenerated
	private Button				problemsButton;
	@AutoGenerated
	private Button				resultsButton;
	@AutoGenerated
	private Button				renameButton;
	@AutoGenerated
	private Button				reportButton;
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
		localize(reportButton, AdminMessageStrings.GENERAL__REPORT);
		localize(renameButton, AdminMessageStrings.GENERAL__RENAME);
		localize(resultsButton, AdminMessageStrings.GENERAL__RESULTS);
		localize(problemsButton, AdminMessageStrings.GENERAL__PROBLEMS);
		localize(editButton, AdminMessageStrings.GENERAL__EDIT);
		localize(duplicateButton, AdminMessageStrings.GENERAL__DUPLICATE);
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);

		localize(modulesLabel, AdminMessageStrings.MODULES__LABEL);
		localize(openModuleButton, AdminMessageStrings.MODULES__OPEN_MODULE);

		// set button start state
		adjust(false, false);

		// Hide some buttons from normal authors
		if (!getUISession().isAdmin()) {
			newButton.setVisible(false);
			importButton.setVisible(false);
			exportButton.setVisible(false);
			duplicateButton.setVisible(false);
			deleteButton.setVisible(false);
		}

		// FIXME Module system has to be reimplemented in a more modular way in
		// an upcoming release - Hide modules table from now on
		modulesLabel.setVisible(false);
		modulesTable.setVisible(false);
		openModuleButton.setVisible(false);

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
									.equals(UIIntervention.MONITORING_STATUS)) {
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

	protected void adjust(final boolean interventionSelected,
			final boolean moduleSelected) {
		exportButton.setEnabled(interventionSelected);
		reportButton.setEnabled(interventionSelected);
		renameButton.setEnabled(interventionSelected);
		resultsButton.setEnabled(interventionSelected);
		problemsButton.setEnabled(interventionSelected);
		editButton.setEnabled(interventionSelected);
		duplicateButton.setEnabled(interventionSelected);
		deleteButton.setEnabled(interventionSelected);

		if (interventionSelected && moduleSelected) {
			openModuleButton.setEnabled(true);
		} else {
			openModuleButton.setEnabled(false);
		}
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
		allInterventionsTable.setHeight("150px");
		mainLayout.addComponent(allInterventionsTable);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);

		// modulesLabel
		modulesLabel = new Label();
		modulesLabel.setStyleName("bold");
		modulesLabel.setImmediate(false);
		modulesLabel.setWidth("-1px");
		modulesLabel.setHeight("-1px");
		modulesLabel.setValue("!!! Modules:");
		mainLayout.addComponent(modulesLabel);

		// modulesTable
		modulesTable = new Table();
		modulesTable.setImmediate(false);
		modulesTable.setWidth("100.0%");
		modulesTable.setHeight("150px");
		mainLayout.addComponent(modulesTable);

		// openModuleButton
		openModuleButton = new Button();
		openModuleButton.setCaption("!!! Open Module");
		openModuleButton.setImmediate(true);
		openModuleButton.setWidth("100.0%");
		openModuleButton.setHeight("-1px");
		mainLayout.addComponent(openModuleButton);

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

		// reportButton
		reportButton = new Button();
		reportButton.setCaption("!!! Report");
		reportButton.setIcon(new ThemeResource("img/results-icon-small.png"));
		reportButton.setImmediate(true);
		reportButton.setWidth("100px");
		reportButton.setHeight("-1px");
		buttonLayout.addComponent(reportButton);

		// renameButton
		renameButton = new Button();
		renameButton.setCaption("!!! Rename");
		renameButton.setImmediate(true);
		renameButton.setWidth("100px");
		renameButton.setHeight("-1px");
		buttonLayout.addComponent(renameButton);

		// resultsButton
		resultsButton = new Button();
		resultsButton.setCaption("!!! Results");
		resultsButton.setIcon(new ThemeResource("img/results-icon-small.png"));
		resultsButton.setImmediate(true);
		resultsButton.setWidth("100px");
		resultsButton.setHeight("-1px");
		buttonLayout.addComponent(resultsButton);

		// problemsButton
		problemsButton = new Button();
		problemsButton.setCaption("!!! Problems");
		problemsButton
				.setIcon(new ThemeResource("img/problems-icon-small.png"));
		problemsButton.setImmediate(true);
		problemsButton.setWidth("100px");
		problemsButton.setHeight("-1px");
		buttonLayout.addComponent(problemsButton);

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
