package org.isgf.mhc.ui.views.components.basics;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.ui.views.components.AbstractStringValueEditComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
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
public class StringEditComponent extends AbstractStringValueEditComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout	mainLayout;
	@AutoGenerated
	private GridLayout		buttonLayout;
	@AutoGenerated
	private Button			okButton;
	@AutoGenerated
	private Button			cancelButton;
	@AutoGenerated
	private TextArea		stringTextArea;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public StringEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(okButton, AdminMessageStrings.GENERAL__OK);
		localize(cancelButton, AdminMessageStrings.GENERAL__CANCEL);

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

		// stringTextArea
		stringTextArea = new TextArea();
		stringTextArea.setImmediate(false);
		stringTextArea.setWidth("100.0%");
		stringTextArea.setHeight("100.0%");
		stringTextArea.setNullSettingAllowed(true);
		mainLayout.addComponent(stringTextArea);
		mainLayout.setExpandRatio(stringTextArea, 1.0f);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, new Alignment(48));

		return mainLayout;
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
		// Not required in this implementation
	}
}
