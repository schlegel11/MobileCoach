package ch.ethz.mc.ui.views.components.views;

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
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

/**
 * Provides the login view component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class ErrorViewComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private GridLayout	mainLayout;
	@AutoGenerated
	private Label		titleLabel;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public ErrorViewComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(titleLabel, AdminMessageStrings.ERROR_VIEW__ERROR_MESSAGE);
	}

	@AutoGenerated
	private GridLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new GridLayout();
		mainLayout.setStyleName("error-view");
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(true);

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// titleLabel
		titleLabel = new Label();
		titleLabel.setStyleName("title-label");
		titleLabel.setImmediate(false);
		titleLabel.setWidth("100.0%");
		titleLabel.setHeight("50px");
		titleLabel.setValue("!!! Sorry, an error occurred.");
		mainLayout.addComponent(titleLabel, 0, 0);
		mainLayout.setComponentAlignment(titleLabel, new Alignment(48));

		return mainLayout;
	}
}
