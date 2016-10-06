package ch.ethz.mc.ui.views.components.interventions;

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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import ch.ethz.mc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the intervention variables tab component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class InterventionVariablesTabComponent extends
		AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout						mainLayout;
	@AutoGenerated
	private InterventionVariablesEditComponent	interventionVariablesEditComponent;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected InterventionVariablesTabComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		// table options
		val interventionVariablesTable = interventionVariablesEditComponent
				.getVariablesTable();
		interventionVariablesTable.setSelectable(true);
		interventionVariablesTable.setImmediate(true);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(false);

		// top-level component properties
		setWidth("100.0%");
		setHeight("-1px");

		// interventionVariablesEditComponent
		interventionVariablesEditComponent = new InterventionVariablesEditComponent();
		interventionVariablesEditComponent.setImmediate(false);
		interventionVariablesEditComponent.setWidth("100.0%");
		interventionVariablesEditComponent.setHeight("-1px");
		mainLayout.addComponent(interventionVariablesEditComponent);

		return mainLayout;
	}
}
