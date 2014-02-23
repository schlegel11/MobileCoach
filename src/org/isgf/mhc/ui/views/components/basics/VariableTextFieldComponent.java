package org.isgf.mhc.ui.views.components.basics;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

/**
 * Provides the variable text field component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class VariableTextFieldComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private HorizontalLayout	mainLayout;
	@AutoGenerated
	private Button				editButton;
	@AutoGenerated
	private TextField			variableTextField;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public VariableTextFieldComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(editButton, AdminMessageStrings.GENERAL__EDIT);
		variableTextField.setReadOnly(true);
	}

	@AutoGenerated
	private HorizontalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new HorizontalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(false);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("100.0%");
		setHeight("-1px");

		// variableTextField
		variableTextField = new TextField();
		variableTextField.setImmediate(false);
		variableTextField.setWidth("100.0%");
		variableTextField.setHeight("-1px");
		mainLayout.addComponent(variableTextField);
		mainLayout.setExpandRatio(variableTextField, 1.0f);
		mainLayout.setComponentAlignment(variableTextField, new Alignment(33));

		// editButton
		editButton = new Button();
		editButton.setCaption("!!! Edit");
		editButton.setIcon(new ThemeResource("img/edit-icon-small.png"));
		editButton.setImmediate(true);
		editButton.setWidth("100px");
		editButton.setHeight("-1px");
		mainLayout.addComponent(editButton);
		mainLayout.setComponentAlignment(editButton, new Alignment(34));

		return mainLayout;
	}

}
