package ch.ethz.mc.ui.components.main_view.interventions.access;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.BackendUser;
import ch.ethz.mc.model.persistent.BackendUserInterventionAccess;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.ui.UIBackendUser;
import ch.ethz.mc.model.ui.UIBackendUserInterventionAccess;
import ch.ethz.mc.ui.components.basics.ShortStringEditComponent;
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

	private final Intervention												intervention;

	private UIBackendUserInterventionAccess									selectedUIBackendUserInterventionAccessInTable	= null;
	private UIBackendUser													selectedUIBackendUserInComboBox					= null;

	private Table															backendUserInterventionAccessTable				= null;

	private final BeanContainer<ObjectId, UIBackendUserInterventionAccess>	tableBeanContainer;
	private final BeanContainer<UIBackendUser, UIBackendUser>				comboBoxBeanContainer;

	public AccessTabComponentWithController(final Intervention intervention) {
		super();

		this.intervention = intervention;

		// table options
		val accessControlEditComponent = getAccessEditComponent();
		backendUserInterventionAccessTable = accessControlEditComponent
				.getAccountsTable();

		// table content
		val allBackendUsers = getInterventionAdministrationManagerService()
				.getAllBackendUsers();
		val backendUsersInterventionAccessesOfIntervention = getInterventionAdministrationManagerService()
				.getAllBackendUserInterventionAcceessesOfIntervention(
						intervention.getId());

		tableBeanContainer = createBeanContainerForModelObjects(
				UIBackendUserInterventionAccess.class,
				backendUsersInterventionAccessesOfIntervention);

		backendUserInterventionAccessTable
				.setContainerDataSource(tableBeanContainer);
		backendUserInterventionAccessTable.setSortContainerPropertyId(
				UIBackendUserInterventionAccess.getSortColumn());
		backendUserInterventionAccessTable.setVisibleColumns(
				UIBackendUserInterventionAccess.getVisibleColumns());
		backendUserInterventionAccessTable.setColumnHeaders(
				UIBackendUserInterventionAccess.getColumnHeaders());

		// handle table selection change
		backendUserInterventionAccessTable
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						val objectId = backendUserInterventionAccessTable
								.getValue();
						if (objectId == null) {
							accessControlEditComponent
									.setNothingSelectedInTable();
							selectedUIBackendUserInterventionAccessInTable = null;
						} else {
							selectedUIBackendUserInterventionAccessInTable = getUIModelObjectFromTableByObjectId(
									backendUserInterventionAccessTable,
									UIBackendUserInterventionAccess.class,
									objectId);
							accessControlEditComponent
									.setSomethingSelectedInTable();
						}
					}
				});

		// combo box content
		val backendUserSelectsComboBox = accessControlEditComponent
				.getAccountsSelectComboBox();
		final List<BackendUser> backendUsersNotOfIntervention = new ArrayList<BackendUser>();
		allBackendUsersLoop: for (val backendUser : allBackendUsers) {
			for (val backendUsersInterventionAccess : backendUsersInterventionAccessesOfIntervention) {
				if (backendUser.getId().equals(
						backendUsersInterventionAccess.getBackendUser())) {
					continue allBackendUsersLoop;
				}
			}
			backendUsersNotOfIntervention.add(backendUser);
		}
		comboBoxBeanContainer = createSimpleBeanContainerForModelObjects(
				UIBackendUser.class, backendUsersNotOfIntervention);
		comboBoxBeanContainer.sort(
				new String[] { UIBackendUser.getSortColumn() },
				new boolean[] { true });

		backendUserSelectsComboBox
				.setContainerDataSource(comboBoxBeanContainer);

		// handle combo box selection change
		backendUserSelectsComboBox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						val uiModelObjectWrapper = backendUserSelectsComboBox
								.getValue();
						if (uiModelObjectWrapper == null) {
							accessControlEditComponent
									.setNothingSelectedInComboBox();
							selectedUIBackendUserInComboBox = null;
						} else {
							selectedUIBackendUserInComboBox = UIBackendUser.class
									.cast(backendUserSelectsComboBox
											.getValue());
							accessControlEditComponent
									.setSomethingSelectedInComboBox();
						}
					}
				});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		accessControlEditComponent.getAddButton()
				.addClickListener(buttonClickListener);
		accessControlEditComponent.getEditGroupPatternButton()
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
					.getEditGroupPatternButton()) {
				editGroupPattern();
			} else if (event.getButton() == accessControlEditComponent
					.getRemoveButton()) {
				removeAccountFromIntervention();
			}
		}
	}

	public void addAccountToIntervention() {
		log.debug("Add backend user intervention access");
		final BackendUserInterventionAccess backendUserInterventionAccess;
		try {
			// Add backend user to intervention
			backendUserInterventionAccess = getInterventionAdministrationManagerService()
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
		tableBeanContainer.addItem(backendUserInterventionAccess.getId(),
				(UIBackendUserInterventionAccess) backendUserInterventionAccess
						.toUIModelObject());
		backendUserInterventionAccessTable
				.select(selectedUIBackendUserInComboBox
						.getRelatedModelObject(BackendUser.class).getId());
		backendUserInterventionAccessTable.sort();

		getAccessEditComponent().getAccountsSelectComboBox()
				.removeItem(selectedUIBackendUserInComboBox);

		getAdminUI().showInformationNotification(
				AdminMessageStrings.NOTIFICATION__ACCOUNT_ADDED_TO_INTERVENTION);
	}

	public void editGroupPattern() {
		log.debug("Edit group pattern");
		val selectedUIBackendUserInterventionAccess = selectedUIBackendUserInterventionAccessInTable
				.getRelatedModelObject(BackendUserInterventionAccess.class);

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_GROUP_PATTERN,
				selectedUIBackendUserInterventionAccess.getGroupPattern(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Change value
						MC.getInstance()
								.getInterventionAdministrationManagerService()
								.backendUserInterventionAccessSetGroupPattern(
										selectedUIBackendUserInterventionAccess,
										getStringValue());

						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__GROUP_PATTERN_CHANGED);
						closeWindow();

						// Adapt UI
						tableBeanContainer.removeItem(
								selectedUIBackendUserInterventionAccess
										.getId());
						tableBeanContainer.addItem(
								selectedUIBackendUserInterventionAccess.getId(),
								(UIBackendUserInterventionAccess) selectedUIBackendUserInterventionAccess
										.toUIModelObject());

						backendUserInterventionAccessTable.sort();
					}
				}, null);
	}

	public void removeAccountFromIntervention() {
		log.debug("Remove backend user intervention access");
		val selectedBackendUserInterventionAccess = selectedUIBackendUserInterventionAccessInTable
				.getRelatedModelObject(BackendUserInterventionAccess.class);

		try {
			// Remove backend user from intervention
			getInterventionAdministrationManagerService()
					.backendUserInterventionAccessDelete(
							selectedBackendUserInterventionAccess);
		} catch (final Exception e) {
			handleException(e);
			return;
		}

		// Adapt UI
		val uiBackendUser = (UIBackendUser) getInterventionAdministrationManagerService()
				.getBackendUser(
						selectedBackendUserInterventionAccess.getBackendUser())
				.toUIModelObject();

		comboBoxBeanContainer.addItem(uiBackendUser, uiBackendUser);
		comboBoxBeanContainer.sort(
				new String[] { UIBackendUser.getSortColumn() },
				new boolean[] { true });

		backendUserInterventionAccessTable
				.removeItem(
						selectedUIBackendUserInterventionAccessInTable
								.getRelatedModelObject(
										BackendUserInterventionAccess.class)
								.getId());

		getAdminUI().showInformationNotification(
				AdminMessageStrings.NOTIFICATION__ACCOUNT_REMOVED_FROM_INTERVENTION);
	}
}
