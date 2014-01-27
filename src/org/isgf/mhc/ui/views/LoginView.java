package org.isgf.mhc.ui.views;

import org.isgf.mhc.ui.AdminNavigatorUI;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;

/**
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class LoginView extends AbstractView implements View {

	@Override
	public void enter(final ViewChangeEvent event) {
		this.setSizeFull();

		// Layout with menu on left and view area on right
		final HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.setSizeFull();

		final Button button = new Button("LOGIN", new Button.ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				LoginView.this.getUISession().setLoggedIn(true);

				LoginView.this.getUI().getNavigator()
						.navigateTo(AdminNavigatorUI.VIEWS.MAIN.getLowerCase());
			}
		});
		this.addComponent(button);
	}
}
