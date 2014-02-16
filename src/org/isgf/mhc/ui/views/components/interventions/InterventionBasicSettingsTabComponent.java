package org.isgf.mhc.ui.views.components.interventions;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the all interventions tab component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class InterventionBasicSettingsTabComponent extends
		AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout						mainLayout;
	@AutoGenerated
	private InterventionBasicSettingsComponent	interventionBasicSettingsComponent;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected InterventionBasicSettingsTabComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
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

		// interventionBasicSettingsComponent
		interventionBasicSettingsComponent = new InterventionBasicSettingsComponent();
		interventionBasicSettingsComponent.setImmediate(false);
		interventionBasicSettingsComponent.setWidth("100.0%");
		interventionBasicSettingsComponent.setHeight("-1px");
		mainLayout.addComponent(interventionBasicSettingsComponent);

		return mainLayout;
	}
}