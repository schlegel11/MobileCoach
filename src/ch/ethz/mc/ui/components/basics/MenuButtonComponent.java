package ch.ethz.mc.ui.components.basics;

/* ##LICENSE## */
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

import ch.ethz.mc.ui.components.AbstractCustomComponent;

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
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
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
