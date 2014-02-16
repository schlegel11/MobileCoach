package org.isgf.mhc.ui.views.components.interventions;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.server.Author;
import org.isgf.mhc.model.server.Intervention;
import org.isgf.mhc.model.ui.UIAuthor;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the intervention access tab component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class InterventionAccessTabComponentWithController extends
		InterventionAccessTabComponent {

	private final Intervention						intervention;

	private UIAuthor								selectedUIAuthorInTable		= null;
	private UIAuthor								selectedUIAuthorInComboBox	= null;

	private final BeanContainer<ObjectId, UIAuthor>	tableBeanContainer;
	private final BeanContainer<UIAuthor, UIAuthor>	comboBoxBeanContainer;

	public InterventionAccessTabComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		// table options
		val accessControlEditComponent = getInterventionAccessEditComponent();
		val accountsTable = accessControlEditComponent.getAccountsTable();

		// table content
		val allAuthors = getInterventionAdministrationManagerService()
				.getAllAuthors();
		val authorsOfIntervention = getInterventionAdministrationManagerService()
				.getAllAuthorsOfIntervention(intervention.getId());

		tableBeanContainer = createBeanContainerForModelObjects(UIAuthor.class,
				authorsOfIntervention);

		accountsTable.setContainerDataSource(tableBeanContainer);
		accountsTable.setSortContainerPropertyId(UIAuthor.getSortColumn());
		accountsTable.setVisibleColumns(UIAuthor.getVisibleColumns());
		accountsTable.setColumnHeaders(UIAuthor.getColumnHeaders());

		// handle table selection change
		accountsTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = accountsTable.getValue();
				if (objectId == null) {
					accessControlEditComponent.setNothingSelectedInTable();
					selectedUIAuthorInTable = null;
				} else {
					selectedUIAuthorInTable = getUIModelObjectFromTableByObjectId(
							accountsTable, UIAuthor.class, objectId);
					accessControlEditComponent.setSomethingSelectedInTable();
				}
			}
		});

		// combo box content
		val accountsSelectComboList = accessControlEditComponent
				.getAccountsSelectComboBox();
		final List<Author> authorsNotOfIntervention = new ArrayList<Author>();
		allAuthorsLoop: for (val author : allAuthors) {
			for (val authorOfIntervention : authorsOfIntervention) {
				if (author.getId().equals(authorOfIntervention.getId())) {
					continue allAuthorsLoop;
				}
			}
			authorsNotOfIntervention.add(author);
		}
		comboBoxBeanContainer = createSimpleBeanContainerForModelObjects(
				UIAuthor.class, authorsNotOfIntervention);
		comboBoxBeanContainer.sort(new String[] { UIAuthor.getSortColumn() },
				new boolean[] { true });

		accountsSelectComboList.setContainerDataSource(comboBoxBeanContainer);

		// handle combo box selection change
		accountsSelectComboList
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						val uiModelObjectWrapper = accountsSelectComboList
								.getValue();
						if (uiModelObjectWrapper == null) {
							accessControlEditComponent
									.setNothingSelectedInComboBox();
							selectedUIAuthorInComboBox = null;
						} else {
							selectedUIAuthorInComboBox = UIAuthor.class
									.cast(accountsSelectComboList.getValue());
							accessControlEditComponent
									.setSomethingSelectedInComboBox();
						}
					}
				});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		accessControlEditComponent.getAddButton().addClickListener(
				buttonClickListener);
		accessControlEditComponent.getRemoveButton().addClickListener(
				buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			val accessControlEditComponent = getInterventionAccessEditComponent();

			if (event.getButton() == accessControlEditComponent.getAddButton()) {
				addAccountToIntervention();
			} else if (event.getButton() == accessControlEditComponent
					.getRemoveButton()) {
				removeAccountFromIntervention();
			}
		}
	}

	public void addAccountToIntervention() {
		log.debug("Add account");
		try {
			// Add author to intervention
			getInterventionAdministrationManagerService()
					.authorInterventionAccessCreate(
							selectedUIAuthorInComboBox.getRelatedModelObject(
									Author.class).getId(), intervention.getId());
		} catch (final Exception e) {
			handleException(e);
			return;
		}

		// Adapt UI
		tableBeanContainer.addItem(selectedUIAuthorInComboBox
				.getRelatedModelObject(Author.class).getId(),
				selectedUIAuthorInComboBox);
		getInterventionAccessEditComponent().getAccountsTable().select(
				selectedUIAuthorInComboBox.getRelatedModelObject(Author.class)
						.getId());
		getInterventionAccessEditComponent().getAccountsSelectComboBox()
				.removeItem(selectedUIAuthorInComboBox);

		getAdminUI()
				.showInformationNotification(
						AdminMessageStrings.NOTIFICATION__ACCOUNT_ADDED_TO_INTERVENTION);
	}

	public void removeAccountFromIntervention() {
		log.debug("Remove account");
		try {
			val selectedAuthor = selectedUIAuthorInTable
					.getRelatedModelObject(Author.class);

			// Remove author from intervention
			getInterventionAdministrationManagerService()
					.authorInterventionAccessDelete(selectedAuthor.getId(),
							intervention.getId());
		} catch (final Exception e) {
			handleException(e);
			return;
		}

		// Adapt UI
		comboBoxBeanContainer.addItem(selectedUIAuthorInTable,
				selectedUIAuthorInTable);
		comboBoxBeanContainer.sort(new String[] { UIAuthor.getSortColumn() },
				new boolean[] { true });
		getInterventionAccessEditComponent().getAccountsTable().removeItem(
				selectedUIAuthorInTable.getRelatedModelObject(Author.class)
						.getId());

		getAdminUI()
				.showInformationNotification(
						AdminMessageStrings.NOTIFICATION__ACCOUNT_REMOVED_FROM_INTERVENTION);
	}
}
