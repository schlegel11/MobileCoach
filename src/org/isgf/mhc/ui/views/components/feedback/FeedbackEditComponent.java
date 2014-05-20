package org.isgf.mhc.ui.views.components.feedback;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.ui.views.components.AbstractClosableEditComponent;

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
public class FeedbackEditComponent extends AbstractClosableEditComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private GridLayout			closeButtonLayout;
	@AutoGenerated
	private Button				closeButton;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;
	@AutoGenerated
	private Button				deleteButton;
	@AutoGenerated
	private Button				moveDownButton;
	@AutoGenerated
	private Button				moveUpButton;
	@AutoGenerated
	private Button				duplicateButton;
	@AutoGenerated
	private Button				editButton;
	@AutoGenerated
	private Button				newButton;
	@AutoGenerated
	private Table				feedbackSlidesTable;
	@AutoGenerated
	private Label				feedbackSlidesLabel;
	@AutoGenerated
	private GridLayout			switchesLayoutGroup;
	@AutoGenerated
	private ComboBox			templatePathComboBox;
	@AutoGenerated
	private Label				templatePathLabel;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected FeedbackEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(templatePathLabel,
				AdminMessageStrings.SCREENING_SURVEY_EDITING__TEMPLATE_PATH);

		localize(feedbackSlidesLabel,
				AdminMessageStrings.SCREENING_SURVEY_EDITING__FEEDBACK_SLIDES);

		localize(newButton, AdminMessageStrings.GENERAL__NEW);
		localize(editButton, AdminMessageStrings.GENERAL__EDIT);
		localize(duplicateButton, AdminMessageStrings.GENERAL__DUPLICATE);
		localize(moveUpButton, AdminMessageStrings.GENERAL__MOVE_UP);
		localize(moveDownButton, AdminMessageStrings.GENERAL__MOVE_DOWN);
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);

		localize(closeButton, AdminMessageStrings.GENERAL__CLOSE);

		// set button start state
		setSlideSelected(false);

		// adjust tables
		feedbackSlidesTable.setSelectable(true);
		feedbackSlidesTable.setImmediate(true);

		// adjust combo box
		templatePathComboBox.setImmediate(true);
		templatePathComboBox.setNullSelectionAllowed(false);
		templatePathComboBox.setTextInputAllowed(false);
	}

	protected void setSlideSelected(final boolean selection) {
		editButton.setEnabled(selection);
		duplicateButton.setEnabled(selection);
		moveUpButton.setEnabled(selection);
		moveDownButton.setEnabled(selection);
		deleteButton.setEnabled(selection);
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

		// switchesLayoutGroup
		switchesLayoutGroup = buildSwitchesLayoutGroup();
		mainLayout.addComponent(switchesLayoutGroup);

		// feedbackSlidesLabel
		feedbackSlidesLabel = new Label();
		feedbackSlidesLabel.setStyleName("bold");
		feedbackSlidesLabel.setImmediate(false);
		feedbackSlidesLabel.setWidth("-1px");
		feedbackSlidesLabel.setHeight("-1px");
		feedbackSlidesLabel.setValue("!!! Feedback Slides:");
		mainLayout.addComponent(feedbackSlidesLabel);

		// feedbackSlidesTable
		feedbackSlidesTable = new Table();
		feedbackSlidesTable.setImmediate(false);
		feedbackSlidesTable.setWidth("100.0%");
		feedbackSlidesTable.setHeight("300px");
		mainLayout.addComponent(feedbackSlidesTable);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);

		// closeButtonLayout
		closeButtonLayout = buildCloseButtonLayout();
		mainLayout.addComponent(closeButtonLayout);

		return mainLayout;
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

		// templatePathLabel
		templatePathLabel = new Label();
		templatePathLabel.setImmediate(false);
		templatePathLabel.setWidth("-1px");
		templatePathLabel.setHeight("-1px");
		templatePathLabel.setValue("!!! Template path:");
		switchesLayoutGroup.addComponent(templatePathLabel, 0, 0);

		// templatePathComboBox
		templatePathComboBox = new ComboBox();
		templatePathComboBox.setImmediate(false);
		templatePathComboBox.setWidth("350px");
		templatePathComboBox.setHeight("-1px");
		switchesLayoutGroup.addComponent(templatePathComboBox, 1, 0);
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

		// duplicateButton
		duplicateButton = new Button();
		duplicateButton.setCaption("!!! Duplicate");
		duplicateButton.setImmediate(true);
		duplicateButton.setWidth("100px");
		duplicateButton.setHeight("-1px");
		buttonLayout.addComponent(duplicateButton);

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
