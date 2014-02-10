package org.isgf.mhc.ui.views.components.access_control;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.server.Author;
import org.isgf.mhc.model.ui.UIAuthor;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
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
@Data
@EqualsAndHashCode(callSuper = false)
public class AccessControlTabComponentWithController extends
		AccessControlTabComponent {

	private UIAuthor			selectedUIAuthor			= null;
	private BeanItem<UIAuthor>	selectedUIAuthorBeanItem	= null;

	public AccessControlTabComponentWithController() {
		super();

		// table options
		val accessControlEditComponent = getAccessControlEditComponent();
		val accountsTable = accessControlEditComponent.getAccountsTable();
		accountsTable.setSelectable(true);
		accountsTable.setImmediate(true);

		// table content
		final val beanItemContainer = createBeanContainer(UIAuthor.class, MHC
				.getInstance().getInterventionAdministrationManagerService()
				.getAllAuthors());

		accountsTable.setContainerDataSource(beanItemContainer);
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
		log.debug("Create new account");
		// TODO Auto-generated method stub
	}

	public void deleteAccount() {
		// TODO Auto-generated method stub

	}

	public void setAccountPassword() {
		// TODO Auto-generated method stub

	}

	public void makeAccountAuthor() {
		try {
			val selectedAuthor = selectedUIAuthor
					.getRelatedModelObject(Author.class);
			MHC.getInstance().getInterventionAdministrationManagerService()
					.authorSetAuthor(selectedAuthor);
		} catch (final Exception e) {
			handleException(e);
			return;
		}

		// Adapt UI
		getStringItemProperty(selectedUIAuthorBeanItem, UIAuthor.TYPE)
				.setValue(
						Messages.getAdminString(AdminMessageStrings.UI_MODEL__AUTHOR));
	}

	public void makeAccountAdmin() {
		try {
			val selectedAuthor = selectedUIAuthor
					.getRelatedModelObject(Author.class);
			MHC.getInstance().getInterventionAdministrationManagerService()
					.authorSetAdmin(selectedAuthor);
		} catch (final Exception e) {
			log.error(e.getMessage());
			// TODO perfect message
			// getAdminUI().showErrorNotification(message, values);
			return;
		}

		// Adapt UI
		getStringItemProperty(selectedUIAuthorBeanItem, UIAuthor.TYPE)
				.setValue(
						Messages.getAdminString(AdminMessageStrings.UI_MODEL__ADMINISTRATOR));
	}
}
