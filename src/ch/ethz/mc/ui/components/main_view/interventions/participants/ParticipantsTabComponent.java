package ch.ethz.mc.ui.components.main_view.interventions.participants;

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
import lombok.val;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.ui.VerticalLayout;

import ch.ethz.mc.ui.components.AbstractCustomComponent;

/**
 * Provides the intervention participants tab component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class ParticipantsTabComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout				mainLayout;
	@AutoGenerated
	private ParticipantsEditComponent	participantsEditComponent;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected ParticipantsTabComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		// table options
		val participantsTable = participantsEditComponent
				.getParticipantsTable();
		participantsTable.setSelectable(true);
		participantsTable.setMultiSelect(true);
		participantsTable.setMultiSelectMode(MultiSelectMode.DEFAULT);
		participantsTable.setImmediate(true);
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

		// interventionParticipantsEditComponent
		participantsEditComponent = new ParticipantsEditComponent();
		participantsEditComponent.setImmediate(false);
		participantsEditComponent.setWidth("100.0%");
		participantsEditComponent.setHeight("-1px");
		mainLayout.addComponent(participantsEditComponent);

		return mainLayout;
	}
}
