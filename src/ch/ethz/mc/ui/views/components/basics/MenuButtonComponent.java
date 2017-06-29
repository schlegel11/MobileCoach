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
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

/**
 * Provides the menu button component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class MenuButtonComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private GridLayout	mainLayout;
	@AutoGenerated
	private Label		buttonLabel;
	@AutoGenerated
	private Embedded	buttonIcon;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public MenuButtonComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		// *nothing*
	}

	@AutoGenerated
	private GridLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new GridLayout();
		mainLayout.setStyleName("menu-button");
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);
		mainLayout.setRows(2);

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// buttonIcon
		buttonIcon = new Embedded();
		buttonIcon.setImmediate(false);
		buttonIcon.setWidth("32px");
		buttonIcon.setHeight("32px");
		buttonIcon.setSource(new ThemeResource("img/loading-icon.png"));
		buttonIcon.setType(1);
		buttonIcon.setMimeType("image/png");
		mainLayout.addComponent(buttonIcon, 0, 0);
		mainLayout.setComponentAlignment(buttonIcon, new Alignment(48));

		// buttonLabel
		buttonLabel = new Label();
		buttonLabel.setStyleName("button-label");
		buttonLabel.setImmediate(false);
		buttonLabel.setWidth("100.0%");
		buttonLabel.setHeight("100.0%");
		buttonLabel.setValue("!!! Button Title");
		mainLayout.addComponent(buttonLabel, 0, 1);
		mainLayout.setComponentAlignment(buttonLabel, new Alignment(48));

		return mainLayout;
	}
}
