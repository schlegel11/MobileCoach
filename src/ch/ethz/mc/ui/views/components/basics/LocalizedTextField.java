package ch.ethz.mc.ui.views.components.basics;

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
import java.util.ArrayList;
import java.util.Locale;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.persistent.subelements.LString;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

/**
 * Provides a localized version of the text field component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class LocalizedTextField extends CustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private GridLayout			mainLayout;
	@AutoGenerated
	private TextField			textField;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;

	private LString				value;
	private Button				currentButton;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public LocalizedTextField() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		val buttons = new ArrayList<Button>();

		val clickListener = new Button.ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				for (val button : buttons) {
					if (event.getButton() == button) {
						if (value != null) {
							value.set((Locale) currentButton.getData(),
									textField.getValue());
						}

						currentButton = button;

						if (value != null) {
							textField.setValue(value.get((Locale) currentButton
									.getData()));
							textField.setCursorPosition(0);
							textField.setSelectionRange(0, 0);
						}

						button.setStyleName("selected", true);
					} else {
						button.setStyleName("selected", false);
					}
				}

				textField.focus();
			}
		};

		for (val locale : Constants.getInterventionLocales()) {
			val button = new Button(locale.getDisplayLanguage(), clickListener);
			button.setWidth("100px");
			button.setData(locale);
			buttonLayout.addComponent(button);

			if (buttons.size() == 0) {
				currentButton = button;
				button.setStyleName("selected", true);
			}

			buttons.add(button);
		}
	}

	public void setLStringValue(final LString value) {
		textField.setValue(value.get((Locale) currentButton.getData()));

		this.value = value;
	}

	public LString getLStringValue() {
		value.set((Locale) currentButton.getData(), textField.getValue());

		return value;
	}

	@Override
	public void setImmediate(final boolean immediate) {
		super.setImmediate(immediate);
		if (textField != null) {
			textField.setImmediate(immediate);
		}
	}

	@Override
	public void focus() {
		super.focus();
		if (textField != null) {
			textField.focus();
		}
	}

	@AutoGenerated
	private GridLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new GridLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(false);
		mainLayout.setRows(2);

		// top-level component properties
		setWidth("100.0%");
		setHeight("-1px");

		// buttonLayout
		buttonLayout = new HorizontalLayout();
		buttonLayout.setImmediate(false);
		buttonLayout.setWidth("-1px");
		buttonLayout.setHeight("30px");
		buttonLayout.setMargin(false);
		buttonLayout.setSpacing(true);
		mainLayout.addComponent(buttonLayout, 0, 0);

		// textField
		textField = new TextField();
		textField.setImmediate(false);
		textField.setWidth("100.0%");
		textField.setHeight("-1px");
		mainLayout.addComponent(textField, 0, 1);

		return mainLayout;
	}

}
