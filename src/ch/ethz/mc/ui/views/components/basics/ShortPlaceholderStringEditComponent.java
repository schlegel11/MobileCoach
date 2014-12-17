package ch.ethz.mc.ui.views.components.basics;

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
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.views.components.AbstractStringValueEditComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides a string edit window for short placeholder strings
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
@Log4j2
public class ShortPlaceholderStringEditComponent extends
		AbstractStringValueEditComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private GridLayout			buttonLayout;
	@AutoGenerated
	private Button				okButton;
	@AutoGenerated
	private Button				cancelButton;
	@AutoGenerated
	private HorizontalLayout	editAreaLayout;
	@AutoGenerated
	private ListSelect			variableListSelect;
	@AutoGenerated
	private Embedded			arrowLeftIcon;
	@AutoGenerated
	private TextField			stringTextField;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public ShortPlaceholderStringEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(okButton, AdminMessageStrings.GENERAL__OK);
		localize(cancelButton, AdminMessageStrings.GENERAL__CANCEL);
		localize(
				variableListSelect,
				AdminMessageStrings.SHORT_PLACEHOLDER_STRING_EDITOR__OPTIONAL_SELECT_VARIABLE);

		stringTextField.setImmediate(true);

		variableListSelect.setNullSelectionAllowed(false);
		variableListSelect.setImmediate(true);
		variableListSelect.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final String selectedVariable = (String) event.getProperty()
						.getValue();
				if (selectedVariable != null) {
					log.debug(
							"Former text value of string text area is {} and cursor position is {}",
							stringTextField.getValue(),
							stringTextField.getCursorPosition());
					try {
						stringTextField.setValue(stringTextField.getValue()
								.substring(0,
										stringTextField.getCursorPosition())
								+ selectedVariable
								+ stringTextField.getValue().substring(
										stringTextField.getCursorPosition()));

						stringTextField.setCursorPosition(stringTextField
								.getCursorPosition()
								+ selectedVariable.length());
					} catch (final Exception e) {
						log.warn("Error occured while setting variable to string text area...fixing by setting text to the beginning (Workaround for Vaadin time shift)");

						stringTextField.setValue(stringTextField.getValue()
								+ selectedVariable);

						stringTextField.setCursorPosition(stringTextField
								.getValue().length());
					}

					variableListSelect.unselect(selectedVariable);
				}
			}
		});

		stringTextField.focus();
	}

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		okButton.addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(final ClickListener clickListener) {
		cancelButton.addClickListener(clickListener);
	}

	@Override
	public void setStringValue(final String value) {
		stringTextField.setValue(value);
	}

	@Override
	public String getStringValue() {
		return stringTextField.getValue();
	}

	@Override
	public void addVariables(final List<String> variables) {
		for (val variable : variables) {
			variableListSelect.addItem(variable);
		}
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("600px");
		mainLayout.setHeight("250px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("600px");
		setHeight("250px");

		// editAreaLayout
		editAreaLayout = buildEditAreaLayout();
		mainLayout.addComponent(editAreaLayout);
		mainLayout.setExpandRatio(editAreaLayout, 1.0f);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, new Alignment(48));

		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildEditAreaLayout() {
		// common part: create layout
		editAreaLayout = new HorizontalLayout();
		editAreaLayout.setImmediate(false);
		editAreaLayout.setWidth("100.0%");
		editAreaLayout.setHeight("100.0%");
		editAreaLayout.setMargin(false);
		editAreaLayout.setSpacing(true);

		// stringTextField
		stringTextField = new TextField();
		stringTextField.setImmediate(false);
		stringTextField.setWidth("250px");
		stringTextField.setHeight("-1px");
		editAreaLayout.addComponent(stringTextField);
		editAreaLayout
				.setComponentAlignment(stringTextField, new Alignment(48));

		// arrowLeftIcon
		arrowLeftIcon = new Embedded();
		arrowLeftIcon.setImmediate(false);
		arrowLeftIcon.setWidth("32px");
		arrowLeftIcon.setHeight("32px");
		arrowLeftIcon.setSource(new ThemeResource("img/arrow-left-icon.png"));
		arrowLeftIcon.setType(1);
		arrowLeftIcon.setMimeType("image/png");
		editAreaLayout.addComponent(arrowLeftIcon);
		editAreaLayout.setComponentAlignment(arrowLeftIcon, new Alignment(48));

		// variableListSelect
		variableListSelect = new ListSelect();
		variableListSelect
				.setCaption("!!! Existing variables to select (optional):");
		variableListSelect.setImmediate(false);
		variableListSelect.setWidth("100.0%");
		variableListSelect.setHeight("100.0%");
		editAreaLayout.addComponent(variableListSelect);
		editAreaLayout.setExpandRatio(variableListSelect, 0.4f);
		editAreaLayout.setComponentAlignment(variableListSelect, new Alignment(
				48));

		return editAreaLayout;
	}

	@AutoGenerated
	private GridLayout buildButtonLayout() {
		// common part: create layout
		buttonLayout = new GridLayout();
		buttonLayout.setImmediate(false);
		buttonLayout.setWidth("100.0%");
		buttonLayout.setHeight("-1px");
		buttonLayout.setMargin(true);
		buttonLayout.setSpacing(true);
		buttonLayout.setColumns(2);

		// cancelButton
		cancelButton = new Button();
		cancelButton.setCaption("!!! Cancel");
		cancelButton.setIcon(new ThemeResource("img/cancel-icon-small.png"));
		cancelButton.setImmediate(true);
		cancelButton.setWidth("140px");
		cancelButton.setHeight("-1px");
		buttonLayout.addComponent(cancelButton, 0, 0);
		buttonLayout.setComponentAlignment(cancelButton, new Alignment(34));

		// okButton
		okButton = new Button();
		okButton.setCaption("!!! OK");
		okButton.setIcon(new ThemeResource("img/ok-icon-small.png"));
		okButton.setImmediate(true);
		okButton.setWidth("140px");
		okButton.setHeight("-1px");
		buttonLayout.addComponent(okButton, 1, 0);
		buttonLayout.setComponentAlignment(okButton, new Alignment(9));

		return buttonLayout;
	}
}
