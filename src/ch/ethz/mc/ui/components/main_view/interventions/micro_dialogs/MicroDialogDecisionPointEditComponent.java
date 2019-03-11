package ch.ethz.mc.ui.components.main_view.interventions.micro_dialogs;

/* ##LICENSE## */
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.components.AbstractClosableEditComponent;
import ch.ethz.mc.ui.components.basics.VariableTextFieldComponent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Provides a monitoring rule edit component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class MicroDialogDecisionPointEditComponent
		extends AbstractClosableEditComponent {
	@AutoGenerated
	private VerticalLayout								mainLayout;

	@AutoGenerated
	private VerticalLayout								bottomLayout;

	@AutoGenerated
	private GridLayout									buttonLayout;

	@AutoGenerated
	private Button										closeButton;

	@AutoGenerated
	private VerticalLayout								topLayout;

	@AutoGenerated
	private MicroDialogRulesEditComponentWithController	microDialogRulesEditComponentWithController;

	@AutoGenerated
	private VariableTextFieldComponent					commentVariableTextFieldComponent;

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		closeButton.addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(
			final ClickListener clickListener) {
		// not required
	}

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	protected MicroDialogDecisionPointEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(commentVariableTextFieldComponent,
				AdminMessageStrings.ABSTRACT_RULE_EDITING__COMMENT);

		localize(closeButton, AdminMessageStrings.GENERAL__CLOSE);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("950px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(false);

		// top-level component properties
		setWidth("950px");
		setHeight("-1px");

		// topLayout
		topLayout = buildTopLayout();
		mainLayout.addComponent(topLayout);

		// bottomLayout
		bottomLayout = buildBottomLayout();
		mainLayout.addComponent(bottomLayout);

		return mainLayout;
	}

	@AutoGenerated
	private VerticalLayout buildTopLayout() {
		// common part: create layout
		topLayout = new VerticalLayout();
		topLayout.setImmediate(false);
		topLayout.setWidth("100.0%");
		topLayout.setHeight("-1px");
		topLayout.setMargin(true);
		topLayout.setSpacing(true);

		// commentVariableTextFieldComponent
		commentVariableTextFieldComponent = new VariableTextFieldComponent();
		commentVariableTextFieldComponent.setCaption("!!! Comment");
		commentVariableTextFieldComponent.setImmediate(false);
		commentVariableTextFieldComponent.setWidth("100.0%");
		commentVariableTextFieldComponent.setHeight("-1px");
		topLayout.addComponent(commentVariableTextFieldComponent);

		// microDialogRulesEditComponentWithController
		microDialogRulesEditComponentWithController = new MicroDialogRulesEditComponentWithController();
		microDialogRulesEditComponentWithController.setImmediate(false);
		microDialogRulesEditComponentWithController.setWidth("100.0%");
		microDialogRulesEditComponentWithController.setHeight("-1px");
		topLayout.addComponent(microDialogRulesEditComponentWithController);

		return topLayout;
	}

	@AutoGenerated
	private VerticalLayout buildBottomLayout() {
		// common part: create layout
		bottomLayout = new VerticalLayout();
		bottomLayout.setImmediate(false);
		bottomLayout.setWidth("100.0%");
		bottomLayout.setHeight("-1px");
		bottomLayout.setMargin(false);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		bottomLayout.addComponent(buttonLayout);

		return bottomLayout;
	}

	@AutoGenerated
	private GridLayout buildButtonLayout() {
		// common part: create layout
		buttonLayout = new GridLayout();
		buttonLayout.setImmediate(false);
		buttonLayout.setWidth("100.0%");
		buttonLayout.setHeight("-1px");
		buttonLayout.setMargin(true);
		buttonLayout.setSpacing(true);

		// closeButton
		closeButton = new Button();
		closeButton.setCaption("!!! Close");
		closeButton.setIcon(new ThemeResource("img/ok-icon-small.png"));
		closeButton.setImmediate(true);
		closeButton.setWidth("140px");
		closeButton.setHeight("-1px");
		buttonLayout.addComponent(closeButton, 0, 0);
		buttonLayout.setComponentAlignment(closeButton, new Alignment(48));

		return buttonLayout;
	}

}
