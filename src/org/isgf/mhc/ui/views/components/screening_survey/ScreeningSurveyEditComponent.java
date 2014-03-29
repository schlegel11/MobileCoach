package org.isgf.mhc.ui.views.components.screening_survey;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.ui.views.components.AbstractClosableEditComponent;
import org.isgf.mhc.ui.views.components.basics.VariableTextFieldComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the screening survey edit component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class ScreeningSurveyEditComponent extends
		AbstractClosableEditComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout				mainLayout;
	@AutoGenerated
	private GridLayout					closeButtonLayout;
	@AutoGenerated
	private Button						closeButton;
	@AutoGenerated
	private VerticalLayout				activeOrInactiveLayout;
	@AutoGenerated
	private HorizontalLayout			fedbacksButtonLayout;
	@AutoGenerated
	private Button						deleteFeedbackButton;
	@AutoGenerated
	private Button						editFeedbackButton;
	@AutoGenerated
	private Button						renameFeedbackButton;
	@AutoGenerated
	private Button						newFeedbackButton;
	@AutoGenerated
	private Table						feedbacksTable;
	@AutoGenerated
	private Label						feedbacksLabel;
	@AutoGenerated
	private HorizontalLayout			buttonLayout;
	@AutoGenerated
	private Button						deleteButton;
	@AutoGenerated
	private Button						moveDownButton;
	@AutoGenerated
	private Button						moveUpButton;
	@AutoGenerated
	private Button						editButton;
	@AutoGenerated
	private Button						newButton;
	@AutoGenerated
	private Table						screeningSurveySlidesTable;
	@AutoGenerated
	private Label						screeningSurveySlidesLabel;
	@AutoGenerated
	private GridLayout					switchesLayoutGroup;
	@AutoGenerated
	private ComboBox					templatePathComboBox;
	@AutoGenerated
	private Label						templatePathLabel;
	@AutoGenerated
	private VariableTextFieldComponent	passwordTextFieldComponent;
	@AutoGenerated
	private Label						passwordLabel;
	@AutoGenerated
	private Button						switchScreeningSurveyButton;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected ScreeningSurveyEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(
				switchScreeningSurveyButton,
				AdminMessageStrings.SCREENING_SURVEY_EDITING__SWITCH_BUTTON_INACTIVE);

		localize(
				passwordLabel,
				AdminMessageStrings.SCREENING_SURVEY_EDITING__PASSWORD_TO_PARTICIPATE);
		localize(templatePathLabel,
				AdminMessageStrings.SCREENING_SURVEY_EDITING__TEMPLATE_PATH);

		localize(
				screeningSurveySlidesLabel,
				AdminMessageStrings.SCREENING_SURVEY_EDITING__SCREENING_SURVEY_SLIDES);

		localize(feedbacksLabel,
				AdminMessageStrings.SCREENING_SURVEY_EDITING__FEEDBACKS);

		localize(newButton, AdminMessageStrings.GENERAL__NEW);
		localize(editButton, AdminMessageStrings.GENERAL__EDIT);
		localize(moveUpButton, AdminMessageStrings.GENERAL__MOVE_UP);
		localize(moveDownButton, AdminMessageStrings.GENERAL__MOVE_DOWN);
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);

		localize(newFeedbackButton, AdminMessageStrings.GENERAL__NEW);
		localize(renameFeedbackButton, AdminMessageStrings.GENERAL__RENAME);
		localize(editFeedbackButton, AdminMessageStrings.GENERAL__EDIT);
		localize(deleteFeedbackButton, AdminMessageStrings.GENERAL__DELETE);

		localize(closeButton, AdminMessageStrings.GENERAL__CLOSE);

		// set button start state
		setSlideSelected(false);
		setFeedbackSelected(false);

		// adjust tables
		screeningSurveySlidesTable.setSelectable(true);
		screeningSurveySlidesTable.setImmediate(true);
		feedbacksTable.setSelectable(true);
		feedbacksTable.setImmediate(true);

		// adjust combo box
		templatePathComboBox.setImmediate(true);
		templatePathComboBox.setNullSelectionAllowed(false);
		templatePathComboBox.setTextInputAllowed(false);
	}

	protected void setSlideSelected(final boolean selection) {
		editButton.setEnabled(selection);
		moveUpButton.setEnabled(selection);
		moveDownButton.setEnabled(selection);
		deleteButton.setEnabled(selection);
	}

	protected void setFeedbackSelected(final boolean selection) {
		renameFeedbackButton.setEnabled(selection);
		editFeedbackButton.setEnabled(selection);
		deleteFeedbackButton.setEnabled(selection);
	}

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		closeButton.addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(final ClickListener clickListener) {
		// not required
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("700px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("700px");
		setHeight("-1px");

		// switchScreeningSurveyButton
		switchScreeningSurveyButton = new Button();
		switchScreeningSurveyButton
				.setCaption("!!! Activate/deactivate Screening Survey");
		switchScreeningSurveyButton.setImmediate(true);
		switchScreeningSurveyButton.setWidth("100.0%");
		switchScreeningSurveyButton.setHeight("-1px");
		mainLayout.addComponent(switchScreeningSurveyButton);

		// activeOrInactiveLayout
		activeOrInactiveLayout = buildActiveOrInactiveLayout();
		mainLayout.addComponent(activeOrInactiveLayout);

		// closeButtonLayout
		closeButtonLayout = buildCloseButtonLayout();
		mainLayout.addComponent(closeButtonLayout);
		mainLayout.setComponentAlignment(closeButtonLayout, new Alignment(48));

		return mainLayout;
	}

	@AutoGenerated
	private VerticalLayout buildActiveOrInactiveLayout() {
		// common part: create layout
		activeOrInactiveLayout = new VerticalLayout();
		activeOrInactiveLayout.setImmediate(false);
		activeOrInactiveLayout.setWidth("100.0%");
		activeOrInactiveLayout.setHeight("-1px");
		activeOrInactiveLayout.setMargin(false);
		activeOrInactiveLayout.setSpacing(true);

		// switchesLayoutGroup
		switchesLayoutGroup = buildSwitchesLayoutGroup();
		activeOrInactiveLayout.addComponent(switchesLayoutGroup);

		// screeningSurveySlidesLabel
		screeningSurveySlidesLabel = new Label();
		screeningSurveySlidesLabel.setStyleName("bold");
		screeningSurveySlidesLabel.setImmediate(false);
		screeningSurveySlidesLabel.setWidth("-1px");
		screeningSurveySlidesLabel.setHeight("-1px");
		screeningSurveySlidesLabel.setValue("!!! Screening Survey Slides:");
		activeOrInactiveLayout.addComponent(screeningSurveySlidesLabel);

		// screeningSurveySlidesTable
		screeningSurveySlidesTable = new Table();
		screeningSurveySlidesTable.setImmediate(false);
		screeningSurveySlidesTable.setWidth("100.0%");
		screeningSurveySlidesTable.setHeight("150px");
		activeOrInactiveLayout.addComponent(screeningSurveySlidesTable);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		activeOrInactiveLayout.addComponent(buttonLayout);

		// feedbacksLabel
		feedbacksLabel = new Label();
		feedbacksLabel.setStyleName("bold");
		feedbacksLabel.setImmediate(false);
		feedbacksLabel.setWidth("-1px");
		feedbacksLabel.setHeight("-1px");
		feedbacksLabel.setValue("!!! Feedbacks");
		activeOrInactiveLayout.addComponent(feedbacksLabel);

		// feedbacksTable
		feedbacksTable = new Table();
		feedbacksTable.setImmediate(false);
		feedbacksTable.setWidth("100.0%");
		feedbacksTable.setHeight("150px");
		activeOrInactiveLayout.addComponent(feedbacksTable);

		// fedbacksButtonLayout
		fedbacksButtonLayout = buildFedbacksButtonLayout();
		activeOrInactiveLayout.addComponent(fedbacksButtonLayout);

		return activeOrInactiveLayout;
	}

	@AutoGenerated
	private GridLayout buildSwitchesLayoutGroup() {
		// common part: create layout
		switchesLayoutGroup = new GridLayout();
		switchesLayoutGroup.setImmediate(false);
		switchesLayoutGroup.setWidth("100.0%");
		switchesLayoutGroup.setHeight("-1px");
		switchesLayoutGroup.setMargin(false);
		switchesLayoutGroup.setSpacing(true);
		switchesLayoutGroup.setColumns(2);
		switchesLayoutGroup.setRows(2);

		// passwordLabel
		passwordLabel = new Label();
		passwordLabel.setImmediate(false);
		passwordLabel.setWidth("-1px");
		passwordLabel.setHeight("-1px");
		passwordLabel
				.setValue("!!! Password required to participate (optional)");
		switchesLayoutGroup.addComponent(passwordLabel, 0, 0);

		// passwordTextFieldComponent
		passwordTextFieldComponent = new VariableTextFieldComponent();
		passwordTextFieldComponent.setImmediate(false);
		passwordTextFieldComponent.setWidth("350px");
		passwordTextFieldComponent.setHeight("-1px");
		switchesLayoutGroup.addComponent(passwordTextFieldComponent, 1, 0);
		switchesLayoutGroup.setComponentAlignment(passwordTextFieldComponent,
				new Alignment(6));

		// templatePathLabel
		templatePathLabel = new Label();
		templatePathLabel.setImmediate(false);
		templatePathLabel.setWidth("-1px");
		templatePathLabel.setHeight("-1px");
		templatePathLabel.setValue("!!! Template path:");
		switchesLayoutGroup.addComponent(templatePathLabel, 0, 1);

		// templatePathComboBox
		templatePathComboBox = new ComboBox();
		templatePathComboBox.setImmediate(false);
		templatePathComboBox.setWidth("350px");
		templatePathComboBox.setHeight("-1px");
		switchesLayoutGroup.addComponent(templatePathComboBox, 1, 1);
		switchesLayoutGroup.setComponentAlignment(templatePathComboBox,
				new Alignment(6));

		return switchesLayoutGroup;
	}

	@AutoGenerated
	private HorizontalLayout buildButtonLayout() {
		// common part: create layout
		buttonLayout = new HorizontalLayout();
		buttonLayout.setImmediate(false);
		buttonLayout.setWidth("-1px");
		buttonLayout.setHeight("-1px");
		buttonLayout.setMargin(false);
		buttonLayout.setSpacing(true);

		// newButton
		newButton = new Button();
		newButton.setCaption("!!! New");
		newButton.setIcon(new ThemeResource("img/add-icon-small.png"));
		newButton.setImmediate(true);
		newButton.setWidth("100px");
		newButton.setHeight("-1px");
		buttonLayout.addComponent(newButton);

		// editButton
		editButton = new Button();
		editButton.setCaption("!!! Edit");
		editButton.setIcon(new ThemeResource("img/edit-icon-small.png"));
		editButton.setImmediate(true);
		editButton.setWidth("100px");
		editButton.setHeight("-1px");
		buttonLayout.addComponent(editButton);

		// moveUpButton
		moveUpButton = new Button();
		moveUpButton.setCaption("!!! Move Up");
		moveUpButton.setIcon(new ThemeResource("img/arrow-up-icon-small.png"));
		moveUpButton.setImmediate(true);
		moveUpButton.setWidth("120px");
		moveUpButton.setHeight("-1px");
		buttonLayout.addComponent(moveUpButton);

		// moveDownButton
		moveDownButton = new Button();
		moveDownButton.setCaption("!!! Move Down");
		moveDownButton.setIcon(new ThemeResource(
				"img/arrow-down-icon-small.png"));
		moveDownButton.setImmediate(true);
		moveDownButton.setWidth("120px");
		moveDownButton.setHeight("-1px");
		buttonLayout.addComponent(moveDownButton);

		// deleteButton
		deleteButton = new Button();
		deleteButton.setCaption("!!! Delete");
		deleteButton.setIcon(new ThemeResource("img/delete-icon-small.png"));
		deleteButton.setImmediate(true);
		deleteButton.setWidth("100px");
		deleteButton.setHeight("-1px");
		buttonLayout.addComponent(deleteButton);

		return buttonLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildFedbacksButtonLayout() {
		// common part: create layout
		fedbacksButtonLayout = new HorizontalLayout();
		fedbacksButtonLayout.setImmediate(false);
		fedbacksButtonLayout.setWidth("-1px");
		fedbacksButtonLayout.setHeight("-1px");
		fedbacksButtonLayout.setMargin(false);
		fedbacksButtonLayout.setSpacing(true);

		// newFeedbackButton
		newFeedbackButton = new Button();
		newFeedbackButton.setCaption("!!! New");
		newFeedbackButton.setIcon(new ThemeResource("img/add-icon-small.png"));
		newFeedbackButton.setImmediate(true);
		newFeedbackButton.setWidth("100px");
		newFeedbackButton.setHeight("-1px");
		fedbacksButtonLayout.addComponent(newFeedbackButton);

		// renameFeedbackButton
		renameFeedbackButton = new Button();
		renameFeedbackButton.setCaption("!!! Rename");
		renameFeedbackButton.setImmediate(true);
		renameFeedbackButton.setWidth("100px");
		renameFeedbackButton.setHeight("-1px");
		fedbacksButtonLayout.addComponent(renameFeedbackButton);

		// editFeedbackButton
		editFeedbackButton = new Button();
		editFeedbackButton.setCaption("!!! Edit");
		editFeedbackButton
				.setIcon(new ThemeResource("img/edit-icon-small.png"));
		editFeedbackButton.setImmediate(true);
		editFeedbackButton.setWidth("100px");
		editFeedbackButton.setHeight("-1px");
		fedbacksButtonLayout.addComponent(editFeedbackButton);

		// deleteFeedbackButton
		deleteFeedbackButton = new Button();
		deleteFeedbackButton.setCaption("!!! Delete");
		deleteFeedbackButton.setIcon(new ThemeResource(
				"img/delete-icon-small.png"));
		deleteFeedbackButton.setImmediate(true);
		deleteFeedbackButton.setWidth("100px");
		deleteFeedbackButton.setHeight("-1px");
		fedbacksButtonLayout.addComponent(deleteFeedbackButton);

		return fedbacksButtonLayout;
	}

	@AutoGenerated
	private GridLayout buildCloseButtonLayout() {
		// common part: create layout
		closeButtonLayout = new GridLayout();
		closeButtonLayout.setImmediate(false);
		closeButtonLayout.setWidth("100.0%");
		closeButtonLayout.setHeight("-1px");
		closeButtonLayout.setMargin(true);
		closeButtonLayout.setSpacing(true);

		// closeButton
		closeButton = new Button();
		closeButton.setCaption("!!! Close");
		closeButton.setIcon(new ThemeResource("img/ok-icon-small.png"));
		closeButton.setImmediate(true);
		closeButton.setWidth("140px");
		closeButton.setHeight("-1px");
		closeButtonLayout.addComponent(closeButton, 0, 0);
		closeButtonLayout.setComponentAlignment(closeButton, new Alignment(48));

		return closeButtonLayout;
	}

}
