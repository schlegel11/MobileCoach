package org.isgf.mhc.ui.views.components.interventions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

import org.isgf.mhc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the intervention participants tab component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class InterventionParticipantsTabComponent extends
		AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout							mainLayout;
	@AutoGenerated
	private InterventionParticipantsEditComponent	interventionParticipantsEditComponent;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected InterventionParticipantsTabComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		// table options
		val participantsTable = interventionParticipantsEditComponent
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
		interventionParticipantsEditComponent = new InterventionParticipantsEditComponent();
		interventionParticipantsEditComponent.setImmediate(false);
		interventionParticipantsEditComponent.setWidth("100.0%");
		interventionParticipantsEditComponent.setHeight("-1px");
		mainLayout.addComponent(interventionParticipantsEditComponent);

		return mainLayout;
	}
}