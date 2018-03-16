package ch.ethz.mc.ui.components.main_view.interventions;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
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
	private VerticalLayout	mainLayout;
	@AutoGenerated
	private VerticalLayout	closeButtonLayout;
	@AutoGenerated
	private Button			closeButton;
	@AutoGenerated
	private Label			remarkLabel;
	@AutoGenerated
	private VerticalLayout	verticalLayout_2;
	@AutoGenerated
	private Button			importButton;
	@AutoGenerated
	private Button			exportMessagesDialogsButton;
	@AutoGenerated
	private Button			exportSurveyFeedbacksButton;

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
		localize(exportSurveyFeedbacksButton,
				AdminMessageStrings.I18N__EXPORT_SURVEYS_AND_FEEDBACKS);
		localize(exportMessagesDialogsButton,
				AdminMessageStrings.I18N__EXPORT_MESSAGES_AND_DIALOGS);
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

		// verticalLayout_2
		verticalLayout_2 = buildVerticalLayout_2();
		mainLayout.addComponent(verticalLayout_2);
		mainLayout.setComponentAlignment(verticalLayout_2, new Alignment(48));

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
	private VerticalLayout buildVerticalLayout_2() {
		// common part: create layout
		verticalLayout_2 = new VerticalLayout();
		verticalLayout_2.setImmediate(false);
		verticalLayout_2.setWidth("100.0%");
		verticalLayout_2.setHeight("-1px");
		verticalLayout_2.setMargin(false);
		verticalLayout_2.setSpacing(true);

		// exportSurveyFeedbacksButton
		exportSurveyFeedbacksButton = new Button();
		exportSurveyFeedbacksButton.setCaption("!!! Export Surveys & Fedbacks");
		exportSurveyFeedbacksButton.setImmediate(true);
		exportSurveyFeedbacksButton.setWidth("200px");
		exportSurveyFeedbacksButton.setHeight("-1px");
		verticalLayout_2.addComponent(exportSurveyFeedbacksButton);
		verticalLayout_2.setComponentAlignment(exportSurveyFeedbacksButton,
				new Alignment(48));

		// exportMessagesDialogsButton
		exportMessagesDialogsButton = new Button();
		exportMessagesDialogsButton.setCaption("!!! Export Messages & Dialogs");
		exportMessagesDialogsButton.setImmediate(true);
		exportMessagesDialogsButton.setWidth("200px");
		exportMessagesDialogsButton.setHeight("-1px");
		verticalLayout_2.addComponent(exportMessagesDialogsButton);
		verticalLayout_2.setComponentAlignment(exportMessagesDialogsButton,
				new Alignment(48));

		// importButton
		importButton = new Button();
		importButton.setCaption("!!! Import");
		importButton.setImmediate(true);
		importButton.setWidth("200px");
		importButton.setHeight("-1px");
		verticalLayout_2.addComponent(importButton);
		verticalLayout_2.setComponentAlignment(importButton, new Alignment(48));

		return verticalLayout_2;
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
