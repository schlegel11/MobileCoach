package ch.ethz.mc.ui.views.components.access_control;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
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
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.persistent.Author;
import ch.ethz.mc.model.ui.UIAuthor;
import ch.ethz.mc.ui.views.components.basics.PasswordEditComponent;
import ch.ethz.mc.ui.views.components.basics.ShortStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the access control tab component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class AccessControlTabComponentWithController extends
		AccessControlTabComponent {

	private UIAuthor								selectedUIAuthor			= null;
	private BeanItem<UIAuthor>						selectedUIAuthorBeanItem	= null;

	private final BeanContainer<ObjectId, UIAuthor>	beanContainer;

	public AccessControlTabComponentWithController() {
		super();

		// table options
		val accessControlEditComponent = getAccessControlEditComponent();
		val accountsTable = accessControlEditComponent.getAccountsTable();

		// table content
		beanContainer = createBeanContainerForModelObjects(UIAuthor.class,
				getInterventionAdministrationManagerService().getAllAuthors());

		accountsTable.setContainerDataSource(beanContainer);
		accountsTable.setSortContainerPropertyId(UIAuthor.getSortColumn());
		accountsTable.setVisibleColumns(UIAuthor.getVisibleColumns());
		accountsTable.setColumnHeaders(UIAuthor.getColumnHeaders());

		// handle selection change
		accountsTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = accountsTable.getValue();
				if (objectId == null) {
					accessControlEditComponent.setNothingSelected();
					selectedUIAuthor = null;
					selectedUIAuthorBeanItem = null;
				} else {
					selectedUIAuthor = getUIModelObjectFromTableByObjectId(
							accountsTable, UIAuthor.class, objectId);
					selectedUIAuthorBeanItem = getBeanItemFromTableByObjectId(
							accountsTable, UIAuthor.class, objectId);
					accessControlEditComponent.setSomethingSelected();
				}
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		accessControlEditComponent.getNewButton().addClickListener(
				buttonClickListener);
		accessControlEditComponent.getMakeAuthorButton().addClickListener(
				buttonClickListener);
		accessControlEditComponent.getMakeAdminButton().addClickListener(
				buttonClickListener);
		accessControlEditComponent.getSetPasswordButton().addClickListener(
				buttonClickListener);
		accessControlEditComponent.getDeleteButton().addClickListener(
				buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			val accessControlEditComponent = getAccessControlEditComponent();

			if (event.getButton() == accessControlEditComponent.getNewButton()) {
				createAccount();
			} else if (event.getButton() == accessControlEditComponent
					.getMakeAdminButton()) {
				makeAccountAdmin();
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
						Author newAuthor;
						try {
							val newUsername = getStringValue();

							// Validate username
							getInterventionAdministrationManagerService()
									.authorCheckValidAndUnique(newUsername);

							// Create new author
							newAuthor = getInterventionAdministrationManagerService()
									.authorCreate(newUsername);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						beanContainer.addItem(newAuthor.getId(), UIAuthor.class
								.cast(newAuthor.toUIModelObject()));
						getAccessControlEditComponent().getAccountsTable()
								.select(newAuthor.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__ACCOUNT_CREATED);

						closeWindow();
					}
				}, null);
	}

	public void makeAccountAuthor() {
		log.debug("Set account author");
		try {
			val selectedAuthor = selectedUIAuthor
					.getRelatedModelObject(Author.class);
			getInterventionAdministrationManagerService().authorSetAuthor(
					selectedAuthor, getUISession().getCurrentAuthorId());
		} catch (final Exception e) {
			handleException(e);
			return;
		}

		// Adapt UI
		getStringItemProperty(selectedUIAuthorBeanItem, UIAuthor.TYPE)
				.setValue(
						Messages.getAdminString(AdminMessageStrings.UI_MODEL__AUTHOR));
		getAdminUI().showInformationNotification(
				AdminMessageStrings.NOTIFICATION__ACCOUNT_CHANGED_TO_AUTHOR);
	}

	public void makeAccountAdmin() {
		log.debug("Set account admin");
		try {
			val selectedAuthor = selectedUIAuthor
					.getRelatedModelObject(Author.class);

			// Set admin
			getInterventionAdministrationManagerService().authorSetAdmin(
					selectedAuthor);
		} catch (final Exception e) {
			handleException(e);
			return;
		}

		// Adapt UI
		getStringItemProperty(selectedUIAuthorBeanItem, UIAuthor.TYPE)
				.setValue(
						Messages.getAdminString(AdminMessageStrings.UI_MODEL__ADMINISTRATOR));
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
							val selectedAuthor = selectedUIAuthor
									.getRelatedModelObject(Author.class);

							// Change password
							getInterventionAdministrationManagerService()
									.authorChangePassword(selectedAuthor,
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

	public void deleteAccount() {
		log.debug("Delete account");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedAuthor = selectedUIAuthor.getRelatedModelObject(Author.class);

					// Delete account
					getInterventionAdministrationManagerService()
							.authorDelete(getUISession().getCurrentAuthorId(),
									selectedAuthor);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				getAccessControlEditComponent().getAccountsTable().removeItem(
						selectedUIAuthor.getRelatedModelObject(Author.class)
								.getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__ACCOUNT_DELETED);

				closeWindow();
			}
		}, null);
	}
}
