package org.isgf.mhc.ui.views.components.access_control;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

import org.isgf.mhc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the access control tab component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AccessControlTabComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout				mainLayout;
	@AutoGenerated
	private AccessControlEditComponent	accessControlEditComponent;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected AccessControlTabComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		// table options
		val accountsTable = accessControlEditComponent.getAccountsTable();
		accountsTable.setSelectable(true);
		accountsTable.setImmediate(true);
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

		// accessControlEditComponent
		accessControlEditComponent = new AccessControlEditComponent();
		accessControlEditComponent.setImmediate(false);
		accessControlEditComponent.setWidth("100.0%");
		accessControlEditComponent.setHeight("-1px");
		mainLayout.addComponent(accessControlEditComponent);

		return mainLayout;
	}
}
