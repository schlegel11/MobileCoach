package ch.ethz.mc.ui.components.main_view.interventions;

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
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the intervention editing container component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class InterventionEditingContainerComponent
		extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private TabSheet			contentTabSheet;
	@AutoGenerated
	private HorizontalLayout	topAreaLayout;
	@AutoGenerated
	private Label				interventionTitleLabel;
	@AutoGenerated
	private Button				listAllInterventionsButton;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected InterventionEditingContainerComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(listAllInterventionsButton,
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__LIST_ALL_INTERVENTIONS_BUTTON);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setStyleName("intervention-container");
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// topAreaLayout
		topAreaLayout = buildTopAreaLayout();
		mainLayout.addComponent(topAreaLayout);
		mainLayout.setComponentAlignment(topAreaLayout, new Alignment(48));

		// contentTabSheet
		contentTabSheet = new TabSheet();
		contentTabSheet.setImmediate(false);
		contentTabSheet.setWidth("100.0%");
		contentTabSheet.setHeight("100.0%");
		mainLayout.addComponent(contentTabSheet);
		mainLayout.setExpandRatio(contentTabSheet, 1.0f);

		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildTopAreaLayout() {
		// common part: create layout
		topAreaLayout = new HorizontalLayout();
		topAreaLayout.setStyleName("bold center");
		topAreaLayout.setImmediate(false);
		topAreaLayout.setWidth("100.0%");
		topAreaLayout.setHeight("-1px");
		topAreaLayout.setMargin(true);
		topAreaLayout.setSpacing(true);

		// listAllInterventionsButton
		listAllInterventionsButton = new Button();
		listAllInterventionsButton.setCaption("!!! Back To List");
		listAllInterventionsButton.setImmediate(true);
		listAllInterventionsButton.setWidth("100px");
		listAllInterventionsButton.setHeight("-1px");
		topAreaLayout.addComponent(listAllInterventionsButton);

		// interventionTitleLabel
		interventionTitleLabel = new Label();
		interventionTitleLabel.setStyleName("title-label");
		interventionTitleLabel.setImmediate(false);
		interventionTitleLabel.setWidth("100.0%");
		interventionTitleLabel.setHeight("-1px");
		interventionTitleLabel.setValue("!!! Intervention ABC");
		topAreaLayout.addComponent(interventionTitleLabel);
		topAreaLayout.setExpandRatio(interventionTitleLabel, 1.0f);
		topAreaLayout.setComponentAlignment(interventionTitleLabel,
				new Alignment(48));

		return topAreaLayout;
	}

}