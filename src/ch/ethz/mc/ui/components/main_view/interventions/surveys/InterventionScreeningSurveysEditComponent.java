package ch.ethz.mc.ui.components.main_view.interventions.surveys;

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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.ui.UIScreeningSurvey;
import ch.ethz.mc.ui.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the intervention screening surveys edit component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class InterventionScreeningSurveysEditComponent
		extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private HorizontalLayout	buttonLayout2;
	@AutoGenerated
	private Button				showButton;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;
	@AutoGenerated
	private Button				deleteButton;
	@AutoGenerated
	private Button				duplicateButton;
	@AutoGenerated
	private Button				editButton;
	@AutoGenerated
	private Button				switchStatusButton;
	@AutoGenerated
	private Button				switchTypeButton;
	@AutoGenerated
	private Button				renameButton;
	@AutoGenerated
	private Button				exportButton;
	@AutoGenerated
	private Button				importButton;
	@AutoGenerated
	private Button				newButton;
	@AutoGenerated
	private Table				screeningSurveysTable;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	protected InterventionScreeningSurveysEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(newButton, AdminMessageStrings.GENERAL__NEW);
		localize(importButton, AdminMessageStrings.GENERAL__IMPORT);
		localize(exportButton, AdminMessageStrings.GENERAL__EXPORT);
		localize(switchTypeButton, AdminMessageStrings.GENERAL__SWITCH_TYPE);
		localize(switchStatusButton,
				AdminMessageStrings.GENERAL__SWITCH_STATUS);
		localize(renameButton, AdminMessageStrings.GENERAL__RENAME);
		localize(editButton, AdminMessageStrings.GENERAL__EDIT);
		localize(duplicateButton, AdminMessageStrings.GENERAL__DUPLICATE);
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);
		localize(showButton,
				AdminMessageStrings.INTERVENTION_SCREENING_SURVEY_EDITING__SHOW);

		// set button start state
		setNothingSelected();

		// set table formatter
		screeningSurveysTable
				.setCellStyleGenerator(new Table.CellStyleGenerator() {
					@Override
					public String getStyle(final Table source,
							final Object itemId, final Object propertyId) {
						if (propertyId != null) {
							if (propertyId.equals(
									UIScreeningSurvey.SCREENING_SURVEY_STATUS)) {
								val uiScreeningSurvey = getUIModelObjectFromTableByObjectId(
										source, UIScreeningSurvey.class,
										itemId);
								if (uiScreeningSurvey
										.isBooleanScreeningSurveyStatus()) {
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
		switchTypeButton.setEnabled(false);
		switchStatusButton.setEnabled(false);
		renameButton.setEnabled(false);
		editButton.setEnabled(false);
		duplicateButton.setEnabled(false);
		deleteButton.setEnabled(false);
		showButton.setEnabled(false);
	}

	protected void setSomethingSelected(final boolean multiple) {
		if (multiple) {
			exportButton.setEnabled(false);
			switchTypeButton.setEnabled(false);
			switchStatusButton.setEnabled(true);
			renameButton.setEnabled(false);
			editButton.setEnabled(false);
			duplicateButton.setEnabled(false);
			deleteButton.setEnabled(false);
			showButton.setEnabled(false);
		} else {
			exportButton.setEnabled(true);
			switchTypeButton.setEnabled(true);
			switchStatusButton.setEnabled(true);
			renameButton.setEnabled(true);
			editButton.setEnabled(true);
			duplicateButton.setEnabled(true);
			deleteButton.setEnabled(true);
			showButton.setEnabled(true);
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

		// screeningSurveysTable
		screeningSurveysTable = new Table();
		screeningSurveysTable.setImmediate(false);
		screeningSurveysTable.setWidth("100.0%");
		screeningSurveysTable.setHeight("350px");
		mainLayout.addComponent(screeningSurveysTable);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);

		// buttonLayout2
		buttonLayout2 = buildButtonLayout2();
		mainLayout.addComponent(buttonLayout2);

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

		// switchTypeButton
		switchTypeButton = new Button();
		switchTypeButton.setCaption("!!! Switch Type");
		switchTypeButton.setImmediate(true);
		switchTypeButton.setWidth("100px");
		switchTypeButton.setHeight("-1px");
		buttonLayout.addComponent(switchTypeButton);

		// switchStatusButton
		switchStatusButton = new Button();
		switchStatusButton.setCaption("!!! Switch Status");
		switchStatusButton.setImmediate(true);
		switchStatusButton.setWidth("100px");
		switchStatusButton.setHeight("-1px");
		buttonLayout.addComponent(switchStatusButton);

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

	@AutoGenerated
	private HorizontalLayout buildButtonLayout2() {
		// common part: create layout
		buttonLayout2 = new HorizontalLayout();
		buttonLayout2.setImmediate(false);
		buttonLayout2.setWidth("-1px");
		buttonLayout2.setHeight("-1px");
		buttonLayout2.setMargin(false);
		buttonLayout2.setSpacing(true);

		// showButton
		showButton = new Button();
		showButton.setCaption("!!! Show");
		showButton.setIcon(new ThemeResource("img/play-icon-small.png"));
		showButton.setImmediate(true);
		showButton.setWidth("100px");
		showButton.setHeight("-1px");
		buttonLayout2.addComponent(showButton);

		return buttonLayout2;
	}

}
