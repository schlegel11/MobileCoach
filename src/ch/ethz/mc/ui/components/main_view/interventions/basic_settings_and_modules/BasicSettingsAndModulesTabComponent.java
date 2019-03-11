package ch.ethz.mc.ui.components.main_view.interventions.basic_settings_and_modules;

/* ##LICENSE## */
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.VerticalLayout;

import ch.ethz.mc.ui.components.AbstractCustomComponent;

/**
 * Provides the all interventions tab component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BasicSettingsAndModulesTabComponent
		extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout						mainLayout;
	@AutoGenerated
	private BasicSettingsAndModulesComponent	basicSettingsAndModulesComponent;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	protected BasicSettingsAndModulesTabComponent() {
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

		// interventionBasicSettingsAndModulesComponent
		basicSettingsAndModulesComponent = new BasicSettingsAndModulesComponent();
		basicSettingsAndModulesComponent.setImmediate(false);
		basicSettingsAndModulesComponent.setWidth("100.0%");
		basicSettingsAndModulesComponent.setHeight("-1px");
		mainLayout.addComponent(basicSettingsAndModulesComponent);

		return mainLayout;
	}
}
