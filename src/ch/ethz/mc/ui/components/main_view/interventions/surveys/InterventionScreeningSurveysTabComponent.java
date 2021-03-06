package ch.ethz.mc.ui.components.main_view.interventions.surveys;

/* ##LICENSE## */
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.ui.VerticalLayout;

import ch.ethz.mc.ui.components.AbstractCustomComponent;

/**
 * Provides the intervention screening surveys tab component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class InterventionScreeningSurveysTabComponent
		extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout								mainLayout;
	@AutoGenerated
	private InterventionScreeningSurveysEditComponent	interventionScreeningSurveyEditComponent;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	protected InterventionScreeningSurveysTabComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		// table options
		val screeningSurveysTable = interventionScreeningSurveyEditComponent
				.getScreeningSurveysTable();
		screeningSurveysTable.setSelectable(true);
		screeningSurveysTable.setMultiSelect(true);
		screeningSurveysTable.setMultiSelectMode(MultiSelectMode.DEFAULT);
		screeningSurveysTable.setImmediate(true);
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

		// interventionScreeningSurveyEditComponent
		interventionScreeningSurveyEditComponent = new InterventionScreeningSurveysEditComponent();
		interventionScreeningSurveyEditComponent.setImmediate(false);
		interventionScreeningSurveyEditComponent.setWidth("100.0%");
		interventionScreeningSurveyEditComponent.setHeight("-1px");
		mainLayout.addComponent(interventionScreeningSurveyEditComponent);

		return mainLayout;
	}
}
