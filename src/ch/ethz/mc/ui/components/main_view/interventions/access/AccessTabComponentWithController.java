package ch.ethz.mc.ui.components.main_view.interventions.access;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.BackendUser;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.ui.UIBackendUser;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the intervention access tab component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class AccessTabComponentWithController extends AccessTabComponent {

	private final Intervention									intervention;

	private UIBackendUser										selectedUIBackendUserInTable	= null;
	private UIBackendUser										selectedUIBackendUserInComboBox	= null;

	private final BeanContainer<ObjectId, UIBackendUser>		tableBeanContainer;
	private final BeanContainer<UIBackendUser, UIBackendUser>	comboBoxBeanContainer;

	public AccessTabComponentWithController(final Intervention intervention) {
		super();

		this.intervention = intervention;

		// table options
		val accessControlEditComponent = getAccessEditComponent();
		val accountsTable = accessControlEditComponent.getAccountsTable();

		// table content
		val allBackendUsers = getInterventionAdministrationManagerService()
				.getAllBackendUsers();
		val backendUsersOfIntervention = getInterventionAdministrationManagerService()
				.getAllBackendUsersOfIntervention(intervention.getId());

		tableBeanContainer = createBeanContainerForModelObjects(
				UIBackendUser.class, backendUsersOfIntervention);

		accountsTable.setContainerDataSource(tableBeanContainer);
		accountsTable.setSortContainerPropertyId(UIBackendUser.getSortColumn());
		accountsTable.setVisibleColumns(UIBackendUser.getVisibleColumns());
		accountsTable.setColumnHeaders(UIBackendUser.getColumnHeaders());

		// handle table selection change
		accountsTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = accountsTable.getValue();
				if (objectId == null) {
					accessControlEditComponent.setNothingSelectedInTable();
					selectedUIBackendUserInTable = null;
				} else {
					selectedUIBackendUserInTable = getUIModelObjectFromTableByObjectId(
							accountsTable, UIBackendUser.class, objectId);
					accessControlEditComponent.setSomethingSelectedInTable();
				}
			}
		});

		// combo box content
		val accountsSelectComboList = accessControlEditComponent
				.getAccountsSelectComboBox();
		final List<BackendUser> backendUsersNotOfIntervention = new ArrayList<BackendUser>();
		allBackendUsersLoop: for (val backendUser : allBackendUsers) {
			if (backendUser.hasEditingBackendAccess()) {
				for (val backendUserOfIntervention : backendUsersOfIntervention) {
					if (backendUser.getId()
							.equals(backendUserOfIntervention.getId())) {
						continue allBackendUsersLoop;
					}
				}
				backendUsersNotOfIntervention.add(backendUser);
			}
		}
		comboBoxBeanContainer = createSimpleBeanContainerForModelObjects(
				UIBackendUser.class, backendUsersNotOfIntervention);
		comboBoxBeanContainer.sort(
				new String[] { UIBackendUser.getSortColumn() },
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
							selectedUIBackendUserInComboBox = null;
						} else {
							selectedUIBackendUserInComboBox = UIBackendUser.class
									.cast(accountsSelectComboList.getValue());
							accessControlEditComponent
									.setSomethingSelectedInComboBox();
						}
					}
				});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		accessControlEditComponent.getAddButton()
				.addClickListener(buttonClickListener);
		accessControlEditComponent.getRemoveButton()
				.addClickListener(buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			val accessControlEditComponent = getAccessEditComponent();

			if (event.getButton() == accessControlEditComponent
					.getAddButton()) {
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
			// Add backend user to intervention
			getInterventionAdministrationManagerService()
					.backendUserInterventionAccessCreate(
							selectedUIBackendUserInComboBox
									.getRelatedModelObject(BackendUser.class)
									.getId(),
							intervention.getId());
		} catch (final Exception e) {
			handleException(e);
			return;
		}

		// Adapt UI
		tableBeanContainer.addItem(
				selectedUIBackendUserInComboBox
						.getRelatedModelObject(BackendUser.class).getId(),
				selectedUIBackendUserInComboBox);
		getAccessEditComponent().getAccountsTable()
				.select(selectedUIBackendUserInComboBox
						.getRelatedModelObject(BackendUser.class).getId());
		getAccessEditComponent().getAccountsSelectComboBox()
				.removeItem(selectedUIBackendUserInComboBox);

		getAdminUI().showInformationNotification(
				AdminMessageStrings.NOTIFICATION__ACCOUNT_ADDED_TO_INTERVENTION);
	}

	public void removeAccountFromIntervention() {
		log.debug("Remove account");
		try {
			val selectedBackendUser = selectedUIBackendUserInTable
					.getRelatedModelObject(BackendUser.class);

			// Remove backend user from intervention
			getInterventionAdministrationManagerService()
					.backendUserInterventionAccessDelete(
							selectedBackendUser.getId(), intervention.getId());
		} catch (final Exception e) {
			handleException(e);
			return;
		}

		// Adapt UI
		comboBoxBeanContainer.addItem(selectedUIBackendUserInTable,
				selectedUIBackendUserInTable);
		comboBoxBeanContainer.sort(
				new String[] { UIBackendUser.getSortColumn() },
				new boolean[] { true });
		getAccessEditComponent().getAccountsTable()
				.removeItem(selectedUIBackendUserInTable
						.getRelatedModelObject(BackendUser.class).getId());

		getAdminUI().showInformationNotification(
				AdminMessageStrings.NOTIFICATION__ACCOUNT_REMOVED_FROM_INTERVENTION);
	}
}
