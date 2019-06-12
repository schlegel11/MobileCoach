package ch.ethz.mc.ui.components.main_view.interventions.access;

/* ##LICENSE## */
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.VerticalLayout;

import ch.ethz.mc.ui.components.AbstractCustomComponent;

/**
 * Provides the intervention access tab component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AccessTabComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private AccessEditComponent	accessEditComponent;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	protected AccessTabComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		// table options
		val accountsTable = accessEditComponent.getAccountsTable();
		accountsTable.setSelectable(true);
		accountsTable.setImmediate(true);

		// combo box options
		val accountsSelectComboBox = accessEditComponent
				.getAccountsSelectComboBox();
		accountsSelectComboBox.setTextInputAllowed(false);
		accountsSelectComboBox.setNewItemsAllowed(false);
		accountsSelectComboBox.setImmediate(true);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(false);

		// top-level component properties
		setWidth("100.0%");
		setHeight("-1px");

		// interventionAccessEditComponent
		accessEditComponent = new AccessEditComponent();
		accessEditComponent.setImmediate(false);
		accessEditComponent.setWidth("100.0%");
		accessEditComponent.setHeight("-1px");
		mainLayout.addComponent(accessEditComponent);

		return mainLayout;
	}
}
