package ch.ethz.mc.ui.views.components.interventions;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the intervention access edit component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class InterventionAccessEditComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;
	@AutoGenerated
	private Button				removeButton;
	@AutoGenerated
	private Button				addButton;
	@AutoGenerated
	private ComboBox			accountsSelectComboBox;
	@AutoGenerated
	private Table				accountsTable;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected InterventionAccessEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(addButton, AdminMessageStrings.GENERAL__ADD);
		localize(removeButton, AdminMessageStrings.GENERAL__REMOVE);

		// set button start state
		setNothingSelectedInTable();
		setNothingSelectedInComboBox();
	}

	protected void setNothingSelectedInTable() {
		removeButton.setEnabled(false);
	}

	protected void setSomethingSelectedInTable() {
		removeButton.setEnabled(true);
	}

	protected void setNothingSelectedInComboBox() {
		addButton.setEnabled(false);
	}

	protected void setSomethingSelectedInComboBox() {
		addButton.setEnabled(true);
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

		// accountsSelectComboBox
		accountsSelectComboBox = new ComboBox();
		accountsSelectComboBox.setImmediate(false);
		accountsSelectComboBox.setWidth("200px");
		accountsSelectComboBox.setHeight("-1px");
		buttonLayout.addComponent(accountsSelectComboBox);
		buttonLayout.setComponentAlignment(accountsSelectComboBox,
				new Alignment(48));

		// addButton
		addButton = new Button();
		addButton.setCaption("!!! Add");
		addButton.setIcon(new ThemeResource("img/add-icon-small.png"));
		addButton.setImmediate(true);
		addButton.setWidth("100px");
		addButton.setHeight("-1px");
		buttonLayout.addComponent(addButton);

		// removeButton
		removeButton = new Button();
		removeButton.setCaption("!!! Remove");
		removeButton.setIcon(new ThemeResource("img/delete-icon-small.png"));
		removeButton.setImmediate(true);
		removeButton.setWidth("100px");
		removeButton.setHeight("-1px");
		buttonLayout.addComponent(removeButton);

		return buttonLayout;
	}

}
