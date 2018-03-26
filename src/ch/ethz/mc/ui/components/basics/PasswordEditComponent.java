package ch.ethz.mc.ui.components.basics;

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
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.components.AbstractStringValueEditComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the about window component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class PasswordEditComponent extends AbstractStringValueEditComponent
		implements TextChangeListener {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout	mainLayout;
	@AutoGenerated
	private GridLayout		buttonLayout;
	@AutoGenerated
	private Button			okButton;
	@AutoGenerated
	private Button			cancelButton;
	@AutoGenerated
	private PasswordField	stringPasswordFieldCheck;
	@AutoGenerated
	private PasswordField	stringPasswordField;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	public PasswordEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(okButton, AdminMessageStrings.GENERAL__OK);
		localize(cancelButton, AdminMessageStrings.GENERAL__CANCEL);

		stringPasswordField.setValue("");
		stringPasswordFieldCheck.setValue("");
		stringPasswordField.addTextChangeListener(this);
		stringPasswordFieldCheck.addTextChangeListener(this);

		// Add enter as click shortcut for default button
		okButton.setClickShortcut(KeyCode.ENTER);
		// Add ESC as click shortcut for cancel button
		cancelButton.setClickShortcut(KeyCode.ESCAPE);

		stringPasswordField.focus();
	}

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		okButton.addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(
			final ClickListener clickListener) {
		cancelButton.addClickListener(clickListener);
	}

	@Override
	public void setStringValue(final String value) {
		// Do nothing...it's about passwords

	}

	@Override
	public String getStringValue() {
		return stringPasswordField.getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.event.FieldEvents.TextChangeListener#textChange(com.vaadin
	 * .event.FieldEvents.TextChangeEvent)
	 */
	@Override
	public void textChange(final TextChangeEvent event) {
		String password1 = null;
		String password2 = null;

		if (event.getSource() == stringPasswordField) {
			password1 = stringPasswordFieldCheck.getValue();
			password2 = event.getText();
		} else if (event.getSource() == stringPasswordFieldCheck) {
			password1 = stringPasswordField.getValue();
			password2 = event.getText();
		}

		if (password1.equals(password2) && !okButton.isEnabled()) {
			okButton.setEnabled(true);
		} else if (!password1.equals(password2) && okButton.isEnabled()) {
			okButton.setEnabled(false);
		}
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("400px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("400px");
		setHeight("-1px");

		// stringPasswordField
		stringPasswordField = new PasswordField();
		stringPasswordField.setImmediate(false);
		stringPasswordField.setWidth("100.0%");
		stringPasswordField.setHeight("-1px");
		mainLayout.addComponent(stringPasswordField);
		mainLayout.setComponentAlignment(stringPasswordField,
				new Alignment(48));

		// stringPasswordFieldCheck
		stringPasswordFieldCheck = new PasswordField();
		stringPasswordFieldCheck.setImmediate(false);
		stringPasswordFieldCheck.setWidth("100.0%");
		stringPasswordFieldCheck.setHeight("-1px");
		mainLayout.addComponent(stringPasswordFieldCheck);
		mainLayout.setComponentAlignment(stringPasswordFieldCheck,
				new Alignment(48));

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, new Alignment(48));

		return mainLayout;
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

	@Override
	public void addVariables(final List<String> variables) {
		// Not required in this implementation
	}

}