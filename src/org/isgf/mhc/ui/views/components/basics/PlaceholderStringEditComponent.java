package org.isgf.mhc.ui.views.components.basics;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.ui.views.components.AbstractStringValueEditComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the about window component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
@Log4j2
public class PlaceholderStringEditComponent extends
		AbstractStringValueEditComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private GridLayout			buttonLayout;
	@AutoGenerated
	private Button				okButton;
	@AutoGenerated
	private Button				cancelButton;
	@AutoGenerated
	private HorizontalLayout	editAreaLayout;
	@AutoGenerated
	private ListSelect			variableListSelect;
	@AutoGenerated
	private Embedded			arrowLeftIcon;
	@AutoGenerated
	private TextArea			stringTextArea;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public PlaceholderStringEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(okButton, AdminMessageStrings.GENERAL__OK);
		localize(cancelButton, AdminMessageStrings.GENERAL__CANCEL);
		localize(variableListSelect,
				AdminMessageStrings.PLACEHOLDER_STRING_EDITOR__SELECT_VARIABLE);

		stringTextArea.setImmediate(true);

		variableListSelect.setNullSelectionAllowed(false);
		variableListSelect.setImmediate(true);
		variableListSelect.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final String selectedVariable = (String) event.getProperty()
						.getValue();
				if (selectedVariable != null) {
					log.debug(
							"Former text value of string text area is {} and cursor position is {}",
							stringTextArea.getValue(),
							stringTextArea.getCursorPosition());
					try {
						stringTextArea.setValue(stringTextArea.getValue()
								.substring(0,
										stringTextArea.getCursorPosition())
								+ selectedVariable
								+ stringTextArea.getValue().substring(
										stringTextArea.getCursorPosition()));

						stringTextArea.setCursorPosition(stringTextArea
								.getCursorPosition()
								+ selectedVariable.length());
					} catch (final Exception e) {
						log.warn("Error occured while setting variable to string text area...fixing by setting text to the beginning (Workaround for Vaadin time shift)");

						stringTextArea.setValue(stringTextArea.getValue()
								+ selectedVariable);

						stringTextArea.setCursorPosition(stringTextArea
								.getValue().length());
					}

					variableListSelect.unselect(selectedVariable);
				}
			}
		});

		stringTextArea.focus();
	}

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		okButton.addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(final ClickListener clickListener) {
		cancelButton.addClickListener(clickListener);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("600px");
		mainLayout.setHeight("250px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("600px");
		setHeight("250px");

		// editAreaLayout
		editAreaLayout = buildEditAreaLayout();
		mainLayout.addComponent(editAreaLayout);
		mainLayout.setExpandRatio(editAreaLayout, 1.0f);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, new Alignment(48));

		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildEditAreaLayout() {
		// common part: create layout
		editAreaLayout = new HorizontalLayout();
		editAreaLayout.setImmediate(false);
		editAreaLayout.setWidth("100.0%");
		editAreaLayout.setHeight("100.0%");
		editAreaLayout.setMargin(false);
		editAreaLayout.setSpacing(true);

		// stringTextArea
		stringTextArea = new TextArea();
		stringTextArea.setImmediate(false);
		stringTextArea.setWidth("100.0%");
		stringTextArea.setHeight("100.0%");
		stringTextArea.setNullSettingAllowed(true);
		editAreaLayout.addComponent(stringTextArea);
		editAreaLayout.setExpandRatio(stringTextArea, 0.6f);
		editAreaLayout.setComponentAlignment(stringTextArea, new Alignment(33));

		// arrowLeftIcon
		arrowLeftIcon = new Embedded();
		arrowLeftIcon.setImmediate(false);
		arrowLeftIcon.setWidth("32px");
		arrowLeftIcon.setHeight("32px");
		arrowLeftIcon.setSource(new ThemeResource("img/arrow-left-icon.png"));
		arrowLeftIcon.setType(1);
		arrowLeftIcon.setMimeType("image/png");
		editAreaLayout.addComponent(arrowLeftIcon);
		editAreaLayout.setComponentAlignment(arrowLeftIcon, new Alignment(48));

		// variableListSelect
		variableListSelect = new ListSelect();
		variableListSelect
				.setCaption("!!! Select variable to add to the text:");
		variableListSelect.setImmediate(false);
		variableListSelect.setWidth("100.0%");
		variableListSelect.setHeight("100.0%");
		editAreaLayout.addComponent(variableListSelect);
		editAreaLayout.setExpandRatio(variableListSelect, 0.4f);
		editAreaLayout.setComponentAlignment(variableListSelect, new Alignment(
				48));

		return editAreaLayout;
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
		buttonLayout.setColumns(2);

		// cancelButton
		cancelButton = new Button();
		cancelButton.setCaption("!!! Cancel");
		cancelButton.setIcon(new ThemeResource("img/cancel-icon-small.png"));
		cancelButton.setImmediate(true);
		cancelButton.setWidth("140px");
		cancelButton.setHeight("-1px");
		buttonLayout.addComponent(cancelButton, 0, 0);
		buttonLayout.setComponentAlignment(cancelButton, new Alignment(34));

		// okButton
		okButton = new Button();
		okButton.setCaption("!!! OK");
		okButton.setIcon(new ThemeResource("img/ok-icon-small.png"));
		okButton.setImmediate(true);
		okButton.setWidth("140px");
		okButton.setHeight("-1px");
		buttonLayout.addComponent(okButton, 1, 0);
		buttonLayout.setComponentAlignment(okButton, new Alignment(9));

		return buttonLayout;
	}

	@Override
	public void setStringValue(final String value) {
		stringTextArea.setValue(value);
	}

	@Override
	public String getStringValue() {
		return stringTextArea.getValue();
	}

	@Override
	public void addVariables(final List<String> variables) {
		for (val variable : variables) {
			variableListSelect.addItem(variable);
		}
	}
}
