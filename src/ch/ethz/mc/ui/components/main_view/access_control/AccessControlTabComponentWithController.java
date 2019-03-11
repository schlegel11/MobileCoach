package ch.ethz.mc.ui.components.main_view.access_control;

/* ##LICENSE## */
import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.persistent.BackendUser;
import ch.ethz.mc.model.persistent.types.BackendUserTypes;
import ch.ethz.mc.model.ui.UIBackendUser;
import ch.ethz.mc.ui.components.basics.PasswordEditComponent;
import ch.ethz.mc.ui.components.basics.ShortStringEditComponent;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the access control tab component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class AccessControlTabComponentWithController
		extends AccessControlTabComponent {

	private UIBackendUser									selectedUIBackendUser			= null;
	private BeanItem<UIBackendUser>							selectedUIBackendUserBeanItem	= null;

	private final BeanContainer<ObjectId, UIBackendUser>	beanContainer;

	public AccessControlTabComponentWithController() {
		super();

		// table options
		val accessControlEditComponent = getAccessControlEditComponent();
		val accountsTable = accessControlEditComponent.getAccountsTable();

		// table content
		beanContainer = createBeanContainerForModelObjects(UIBackendUser.class,
				getInterventionAdministrationManagerService()
						.getAllBackendUsers());

		accountsTable.setContainerDataSource(beanContainer);
		accountsTable.setSortContainerPropertyId(UIBackendUser.getSortColumn());
		accountsTable.setVisibleColumns(UIBackendUser.getVisibleColumns());
		accountsTable.setColumnHeaders(UIBackendUser.getColumnHeaders());

		// handle selection change
		accountsTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = accountsTable.getValue();
				if (objectId == null) {
					accessControlEditComponent.setNothingSelected();
					selectedUIBackendUser = null;
					selectedUIBackendUserBeanItem = null;
				} else {
					selectedUIBackendUser = getUIModelObjectFromTableByObjectId(
							accountsTable, UIBackendUser.class, objectId);
					selectedUIBackendUserBeanItem = getBeanItemFromTableByObjectId(
							accountsTable, UIBackendUser.class, objectId);
					accessControlEditComponent.setSomethingSelected();
				}
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		accessControlEditComponent.getNewButton()
				.addClickListener(buttonClickListener);
		accessControlEditComponent.getMakeTeamManagerButton()
				.addClickListener(buttonClickListener);
		accessControlEditComponent.getMakeAuthorButton()
				.addClickListener(buttonClickListener);
		accessControlEditComponent.getMakeAdminButton()
				.addClickListener(buttonClickListener);
		accessControlEditComponent.getSetPasswordButton()
				.addClickListener(buttonClickListener);
		accessControlEditComponent.getDeleteButton()
				.addClickListener(buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			val accessControlEditComponent = getAccessControlEditComponent();

			if (event.getButton() == accessControlEditComponent
					.getNewButton()) {
				createAccount();
			} else if (event.getButton() == accessControlEditComponent
					.getMakeAdminButton()) {
				makeAccountAdmin();
			} else if (event.getButton() == accessControlEditComponent
					.getMakeTeamManagerButton()) {
				makeAccountTeamManager();
			} else if (event.getButton() == accessControlEditComponent
					.getMakeAuthorButton()) {
				makeAccountAuthor();
			} else if (event.getButton() == accessControlEditComponent
					.getSetPasswordButton()) {
				setAccountPassword();
			} else if (event.getButton() == accessControlEditComponent
					.getDeleteButton()) {
				deleteAccount();
			}
		}
	}

	public void createAccount() {
		log.debug("Create account");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_USERNAME_FOR_NEW_USER,
				null, null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						BackendUser newAuthor;
						try {
							val newUsername = getStringValue();

							// Validate username
							getInterventionAdministrationManagerService()
									.backendUserCheckValidAndUnique(
											newUsername);

							// Create new author
							newAuthor = getInterventionAdministrationManagerService()
									.backendUserCreate(newUsername);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						beanContainer.addItem(newAuthor.getId(),
								UIBackendUser.class
										.cast(newAuthor.toUIModelObject()));
						getAccessControlEditComponent().getAccountsTable()
								.select(newAuthor.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__ACCOUNT_CREATED);

						closeWindow();
					}
				}, null);
	}

	public void makeAccountTeamManager() {
		log.debug("Set account team manager");
		try {
			val selectedBackendUser = selectedUIBackendUser
					.getRelatedModelObject(BackendUser.class);
			getInterventionAdministrationManagerService().backendUserSetType(
					selectedBackendUser, BackendUserTypes.TEAM_MANAGER,
					getUISession().getCurrentBackendUserId());
		} catch (final Exception e) {
			handleException(e);
			return;
		}

		// Adapt UI
		getStringItemProperty(selectedUIBackendUserBeanItem, UIBackendUser.TYPE)
				.setValue(Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__TEAM_MANAGER));
		getAdminUI().showInformationNotification(
				AdminMessageStrings.NOTIFICATION__ACCOUNT_CHANGED_TO_TEAM_MANAGER);
	}

	public void makeAccountAuthor() {
		log.debug("Set account author");
		try {
			val selectedBackendUser = selectedUIBackendUser
					.getRelatedModelObject(BackendUser.class);
			getInterventionAdministrationManagerService().backendUserSetType(
					selectedBackendUser, BackendUserTypes.AUTHOR,
					getUISession().getCurrentBackendUserId());
		} catch (final Exception e) {
			handleException(e);
			return;
		}

		// Adapt UI
		getStringItemProperty(selectedUIBackendUserBeanItem, UIBackendUser.TYPE)
				.setValue(Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__AUTHOR));
		getAdminUI().showInformationNotification(
				AdminMessageStrings.NOTIFICATION__ACCOUNT_CHANGED_TO_AUTHOR);
	}

	public void makeAccountAdmin() {
		log.debug("Set account admin");
		try {
			val selectedBackendUser = selectedUIBackendUser
					.getRelatedModelObject(BackendUser.class);
			getInterventionAdministrationManagerService().backendUserSetType(
					selectedBackendUser, BackendUserTypes.ADMIN,
					getUISession().getCurrentBackendUserId());
		} catch (final Exception e) {
			handleException(e);
			return;
		}

		// Adapt UI
		getStringItemProperty(selectedUIBackendUserBeanItem, UIBackendUser.TYPE)
				.setValue(Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__ADMIN));
		getAdminUI().showInformationNotification(
				AdminMessageStrings.NOTIFICATION__ACCOUNT_CHANGED_TO_ADMIN);
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
							val selectedBackendUser = selectedUIBackendUser
									.getRelatedModelObject(BackendUser.class);

							// Change password
							getInterventionAdministrationManagerService()
									.backendUserSetPassword(selectedBackendUser,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__PASSWORD_CHANGED);
						closeWindow();
					}
				}, null);
	}

	public void deleteAccount() {
		log.debug("Delete account");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedBackendUser = selectedUIBackendUser
							.getRelatedModelObject(BackendUser.class);

					// Delete account
					getInterventionAdministrationManagerService()
							.backendUserDelete(
									getUISession().getCurrentBackendUserId(),
									selectedBackendUser);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				getAccessControlEditComponent().getAccountsTable()
						.removeItem(selectedUIBackendUser
								.getRelatedModelObject(BackendUser.class)
								.getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__ACCOUNT_DELETED);

				closeWindow();
			}
		}, null);
	}
}
