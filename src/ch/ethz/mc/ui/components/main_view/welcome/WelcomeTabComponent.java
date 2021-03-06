package ch.ethz.mc.ui.components.main_view.welcome;

/* ##LICENSE## */
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.components.AbstractCustomComponent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Provides the welcome tab component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class WelcomeTabComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout	mainLayout;
	@AutoGenerated
	private Label			welcomeLabel;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	protected WelcomeTabComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(welcomeLabel, AdminMessageStrings.WELCOME_TAB__WELCOME_MESSAGE,
				getUISession().getCurrentBackendUserUsername());
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setStyleName("welcome-tab");
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// welcomeLabel
		welcomeLabel = new Label();
		welcomeLabel.setStyleName("welcome-label");
		welcomeLabel.setImmediate(false);
		welcomeLabel.setWidth("-1px");
		welcomeLabel.setHeight("-1px");
		welcomeLabel.setValue("!!! Welcome to Mobile Coach!");
		mainLayout.addComponent(welcomeLabel);
		mainLayout.setComponentAlignment(welcomeLabel, new Alignment(48));

		return mainLayout;
	}
}
