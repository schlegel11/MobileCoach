package ch.ethz.mc.ui.components.main_view.account;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.ui.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the account edit component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class AccountEditComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout	mainLayout;
	@AutoGenerated
	private Button			resetAllLocksButton;
	@AutoGenerated
	private Button			setPasswordButton;
	@AutoGenerated
	private Label			accountInformationLabel;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected AccountEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(setPasswordButton,
				AdminMessageStrings.ACCOUNT_TAB__SET_PASSWORD);
		localize(resetAllLocksButton,
				AdminMessageStrings.ACCOUNT_TAB__RESET_ALL_LOCKS);

		adjust("---", false);
	}

	public void adjust(final String username, final boolean isAdministrator) {
		localize(accountInformationLabel,
				AdminMessageStrings.ACCOUNT_TAB__ACCOUNT_INFORMATION, username,
				isAdministrator
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__ADMINISTRATOR)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__AUTHOR));

		if (isAdministrator) {
			resetAllLocksButton.setVisible(true);
			resetAllLocksButton.setEnabled(true);
		} else {
			resetAllLocksButton.setVisible(false);
			resetAllLocksButton.setEnabled(false);
		}
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("100.0%");
		setHeight("-1px");

		// accountInformationLabel
		accountInformationLabel = new Label();
		accountInformationLabel.setImmediate(false);
		accountInformationLabel.setWidth("-1px");
		accountInformationLabel.setHeight("-1px");
		accountInformationLabel
				.setValue("!!! Your account „abc“ is an Administrator");
		mainLayout.addComponent(accountInformationLabel);

		// setPasswordButton
		setPasswordButton = new Button();
		setPasswordButton.setCaption("!!! Set Password");
		setPasswordButton.setImmediate(true);
		setPasswordButton.setWidth("150px");
		setPasswordButton.setHeight("-1px");
		mainLayout.addComponent(setPasswordButton);

		// resetAllLocksButton
		resetAllLocksButton = new Button();
		resetAllLocksButton.setCaption("!!! Reset All Locks");
		resetAllLocksButton.setImmediate(true);
		resetAllLocksButton.setWidth("150px");
		resetAllLocksButton.setHeight("-1px");
		mainLayout.addComponent(resetAllLocksButton);

		return mainLayout;
	}

}