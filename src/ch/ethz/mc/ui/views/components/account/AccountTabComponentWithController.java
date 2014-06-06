package ch.ethz.mc.ui.views.components.account;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.Author;
import ch.ethz.mc.ui.views.components.basics.PasswordEditComponent;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the account tab component with a controller
 * 
 * @currentAccountAuthor Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class AccountTabComponentWithController extends AccountTabComponent {

	private final Author	currentAccountAuthor;

	public AccountTabComponentWithController(final ObjectId accountObjectId) {
		super();

		currentAccountAuthor = getInterventionAdministrationManagerService()
				.getAuthor(accountObjectId);

		// Localize
		getAccountEditComponent().adjust(currentAccountAuthor.getUsername(),
				currentAccountAuthor.isAdmin());

		val buttonClickListener = new ButtonClickListener();
		getAccountEditComponent().getSetPasswordButton().addClickListener(
				buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getAccountEditComponent()
					.getSetPasswordButton()) {
				setAccountPassword();
			}
		}
	}

	public void setAccountPassword() {
		log.debug("Set password");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__SET_PASSWORD,
				null, null, new PasswordEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change password
							getInterventionAdministrationManagerService()
									.authorChangePassword(currentAccountAuthor,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__PASSWORD_CHANGED);
						closeWindow();
					}
				}, null);
	}
}
