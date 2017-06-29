package ch.ethz.mc.ui.views.components.basics;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.ui.views.components.AbstractLStringValueEditComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides a string edit window for short strings
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class LocalizedShortStringEditComponent extends
		AbstractLStringValueEditComponent {

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
	private LocalizedTextField	stringTextField;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public LocalizedShortStringEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(okButton, AdminMessageStrings.GENERAL__OK);
		localize(cancelButton, AdminMessageStrings.GENERAL__CANCEL);

		// Add enter as click shortcut for default button
		okButton.setClickShortcut(KeyCode.ENTER);
		// Add ESC as click shortcut for cancel button
		cancelButton.setClickShortcut(KeyCode.ESCAPE);

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
	public void setLStringValue(final LString value) {
		stringTextField.setLStringValue(value.clone());
	}

	@Override
	public LString getLStringValue() {
		return stringTextField.getLStringValue();
	}

	@Override
	public void addVariables(final List<String> variables) {
		// Not required in this implementation
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("450px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("450px");
		setHeight("-1px");

		// stringTextField
		stringTextField = new LocalizedTextField();
		stringTextField.setImmediate(false);
		stringTextField.setWidth("100.0%");
		stringTextField.setHeight("-1px");
		mainLayout.addComponent(stringTextField);
		mainLayout.setComponentAlignment(stringTextField, new Alignment(48));

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
}
