package ch.ethz.mc.ui.components.main_view.interventions;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.components.AbstractClosableEditComponent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Provides the intervention i18n component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class InterventionI18nComponent extends AbstractClosableEditComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private VerticalLayout		closeButtonLayout;
	@AutoGenerated
	private Button				closeButton;
	@AutoGenerated
	private Label				remarkLabel;
	@AutoGenerated
	private HorizontalLayout	variablesButtonLayout;
	@AutoGenerated
	private Button				importButton;
	@AutoGenerated
	private Button				exportButton;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	protected InterventionI18nComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(exportButton, AdminMessageStrings.GENERAL__EXPORT);
		localize(importButton, AdminMessageStrings.GENERAL__IMPORT);
		localize(remarkLabel, AdminMessageStrings.I18N__REMARK);

		localize(closeButton, AdminMessageStrings.GENERAL__CLOSE);
	}

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		closeButton.addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(
			final ClickListener clickListener) {
		// Not required
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("500px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("500px");
		setHeight("-1px");

		// variablesButtonLayout
		variablesButtonLayout = buildVariablesButtonLayout();
		mainLayout.addComponent(variablesButtonLayout);
		mainLayout.setComponentAlignment(variablesButtonLayout,
				new Alignment(20));

		// remarkLabel
		remarkLabel = new Label();
		remarkLabel.setImmediate(false);
		remarkLabel.setWidth("100.0%");
		remarkLabel.setHeight("-1px");
		remarkLabel.setValue("!!! Remark…");
		mainLayout.addComponent(remarkLabel);
		mainLayout.setComponentAlignment(remarkLabel, new Alignment(20));

		// closeButtonLayout
		closeButtonLayout = buildCloseButtonLayout();
		mainLayout.addComponent(closeButtonLayout);
		mainLayout.setComponentAlignment(closeButtonLayout, new Alignment(48));

		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildVariablesButtonLayout() {
		// common part: create layout
		variablesButtonLayout = new HorizontalLayout();
		variablesButtonLayout.setImmediate(false);
		variablesButtonLayout.setWidth("-1px");
		variablesButtonLayout.setHeight("-1px");
		variablesButtonLayout.setMargin(false);
		variablesButtonLayout.setSpacing(true);

		// exportButton
		exportButton = new Button();
		exportButton.setCaption("!!! Export");
		exportButton.setImmediate(true);
		exportButton.setWidth("100px");
		exportButton.setHeight("-1px");
		variablesButtonLayout.addComponent(exportButton);

		// importButton
		importButton = new Button();
		importButton.setCaption("!!! Import");
		importButton.setImmediate(true);
		importButton.setWidth("100px");
		importButton.setHeight("-1px");
		variablesButtonLayout.addComponent(importButton);

		return variablesButtonLayout;
	}

	@AutoGenerated
	private VerticalLayout buildCloseButtonLayout() {
		// common part: create layout
		closeButtonLayout = new VerticalLayout();
		closeButtonLayout.setImmediate(false);
		closeButtonLayout.setWidth("100.0%");
		closeButtonLayout.setHeight("-1px");
		closeButtonLayout.setMargin(true);

		// closeButton
		closeButton = new Button();
		closeButton.setCaption("!!! Close");
		closeButton.setImmediate(true);
		closeButton.setWidth("100px");
		closeButton.setHeight("-1px");
		closeButtonLayout.addComponent(closeButton);
		closeButtonLayout.setComponentAlignment(closeButton, new Alignment(48));

		return closeButtonLayout;
	}

}