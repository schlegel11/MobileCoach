package org.isgf.mhc.ui.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class LoginViewComponent extends CustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout	mainLayout;
	@AutoGenerated
	private Panel			loginPanel;
	@AutoGenerated
	private AbsoluteLayout	loginPanelLayout;
	@AutoGenerated
	private Button			loginButton;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public LoginViewComponent() {
		this.buildMainLayout();
		this.setCompositionRoot(this.mainLayout);

		// TODO add user code here
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		this.mainLayout = new VerticalLayout();
		this.mainLayout.setStyleName("login-view");
		this.mainLayout.setImmediate(false);
		this.mainLayout.setWidth("100%");
		this.mainLayout.setHeight("100%");
		this.mainLayout.setMargin(false);

		// top-level component properties
		this.setWidth("100.0%");
		this.setHeight("100.0%");

		// loginPanel
		this.loginPanel = this.buildLoginPanel();
		this.mainLayout.addComponent(this.loginPanel);
		this.mainLayout.setComponentAlignment(this.loginPanel,
				new Alignment(48));

		return this.mainLayout;
	}

	@AutoGenerated
	private Panel buildLoginPanel() {
		// common part: create layout
		this.loginPanel = new Panel();
		this.loginPanel.setImmediate(false);
		this.loginPanel.setWidth("506px");
		this.loginPanel.setHeight("292px");

		// loginPanelLayout
		this.loginPanelLayout = this.buildLoginPanelLayout();
		this.loginPanel.setContent(this.loginPanelLayout);

		return this.loginPanel;
	}

	@AutoGenerated
	private AbsoluteLayout buildLoginPanelLayout() {
		// common part: create layout
		this.loginPanelLayout = new AbsoluteLayout();
		this.loginPanelLayout.setImmediate(false);
		this.loginPanelLayout.setWidth("100.0%");
		this.loginPanelLayout.setHeight("100.0%");

		// loginButton
		this.loginButton = new Button();
		this.loginButton.setCaption("Login");
		this.loginButton.setImmediate(true);
		this.loginButton.setWidth("140px");
		this.loginButton.setHeight("-1px");
		this.loginPanelLayout.addComponent(this.loginButton,
				"top:241.0px;right:19.0px;");

		return this.loginPanelLayout;
	}
}
