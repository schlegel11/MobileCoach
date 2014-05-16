package org.isgf.mhc.ui.views.components.interventions.monitoring_messages;

import java.util.Hashtable;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.persistent.MonitoringMessageGroup;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;

/**
 * Extends the monitoring message groups tab with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringMessageGroupsTabComponentWithController extends
		MonitoringMessageGroupsTabComponent {

	private final Intervention				intervention;

	private MonitoringMessageGroup			selectedMonitoringMessageGroup	= null;

	private final Hashtable<Tab, ObjectId>	tabsWithObjectIdsOfMessageGroup;

	public MonitoringMessageGroupsTabComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		tabsWithObjectIdsOfMessageGroup = new Hashtable<TabSheet.Tab, ObjectId>();

		// Retrieve monitoring message groups to set current and fill tabs
		final Iterable<MonitoringMessageGroup> monitoringMessageGroupsIterable = getInterventionAdministrationManagerService()
				.getAllMonitoringMessageGroupsOfIntervention(
						intervention.getId());

		for (val monitoringMessageGroup : monitoringMessageGroupsIterable) {
			val newTab = addTabComponent(monitoringMessageGroup,
					intervention.getId());

			tabsWithObjectIdsOfMessageGroup.put(newTab,
					monitoringMessageGroup.getId());

			if (getMonitoringMessageGroupsTabSheet().getComponentCount() == 1) {
				// First tab added
				selectedMonitoringMessageGroup = monitoringMessageGroup;
				getMonitoringMessageGroupsTabSheet().setSelectedTab(newTab);

				setSomethingSelected();
			}
		}

		// handle tab selection change
		getMonitoringMessageGroupsTabSheet().addSelectedTabChangeListener(
				new SelectedTabChangeListener() {

					@Override
					public void selectedTabChange(
							final SelectedTabChangeEvent event) {
						val selectedTab = event.getTabSheet().getSelectedTab();
						if (selectedTab == null) {
							setNothingSelected();
							selectedMonitoringMessageGroup = null;
						} else {
							val selectedTabObject = event.getTabSheet().getTab(
									selectedTab);
							val monitoringMessageGroupObjectId = tabsWithObjectIdsOfMessageGroup
									.get(selectedTabObject);
							selectedMonitoringMessageGroup = getInterventionAdministrationManagerService()
									.getMonitoringMessageGroup(
											monitoringMessageGroupObjectId);

							setSomethingSelected();
						}
					}
				});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getNewGroupButton().addClickListener(buttonClickListener);
		getRenameGroupButton().addClickListener(buttonClickListener);
		getMoveGroupLeftButton().addClickListener(buttonClickListener);
		getMoveGroupRightButton().addClickListener(buttonClickListener);
		getDeleteGroupButton().addClickListener(buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getNewGroupButton()) {
				createGroup();
			} else if (event.getButton() == getMoveGroupLeftButton()) {
				moveGroup(true);
			} else if (event.getButton() == getMoveGroupRightButton()) {
				moveGroup(false);
			} else if (event.getButton() == getRenameGroupButton()) {
				renameGroup();
			} else if (event.getButton() == getDeleteGroupButton()) {
				deleteGroup();
			}
		}
	}

	public void createGroup() {
		log.debug("Create group");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_MONITORING_MESSAGE_GROUP,
				null, null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						MonitoringMessageGroup newMonitoringMessageGroup;
						try {
							// Create new variable
							newMonitoringMessageGroup = getInterventionAdministrationManagerService()
									.monitoringMessageGroupCreate(
											getStringValue(),
											intervention.getId());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						selectedMonitoringMessageGroup = newMonitoringMessageGroup;

						val newTab = addTabComponent(newMonitoringMessageGroup,
								intervention.getId());

						tabsWithObjectIdsOfMessageGroup.put(newTab,
								newMonitoringMessageGroup.getId());

						getMonitoringMessageGroupsTabSheet().setSelectedTab(
								newTab);
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_GROUP_CREATED);

						closeWindow();
					}
				}, null);
	}

	public void moveGroup(final boolean moveLeft) {
		log.debug("Move group {}", moveLeft ? "left" : "right");

		val swappedMonitoringMessageGroup = getInterventionAdministrationManagerService()
				.monitoringMessageGroupMove(selectedMonitoringMessageGroup,
						moveLeft);

		if (swappedMonitoringMessageGroup == null) {
			log.debug("Message group is already at beginning/end of list");
			return;
		}

		val tabSheet = getMonitoringMessageGroupsTabSheet();
		val currentPosition = tabSheet.getTabPosition(tabSheet.getTab(tabSheet
				.getSelectedTab()));
		if (moveLeft) {
			tabSheet.setTabPosition(tabSheet.getTab(tabSheet.getSelectedTab()),
					currentPosition - 1);
		} else {
			tabSheet.setTabPosition(tabSheet.getTab(tabSheet.getSelectedTab()),
					currentPosition + 1);
		}

		setSomethingSelected();
	}

	public void renameGroup() {
		log.debug("Rename group");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_MONITORING_MESSAGE_GROUP,
				selectedMonitoringMessageGroup.getName(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change name
							getInterventionAdministrationManagerService()
									.monitoringMessageGroupChangeName(
											selectedMonitoringMessageGroup,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						val tab = getMonitoringMessageGroupsTabSheet();
						tab.getTab(tab.getSelectedTab()).setCaption(
								selectedMonitoringMessageGroup.getName());

						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_GROUP_RENAMED);
						closeWindow();
					}
				}, null);
	}

	public void deleteGroup() {
		log.debug("Delete group");

		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					// Delete group
					getInterventionAdministrationManagerService()
							.monitoringMessageGroupDelete(
									selectedMonitoringMessageGroup);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				val tabSheet = getMonitoringMessageGroupsTabSheet();

				tabsWithObjectIdsOfMessageGroup.remove(tabSheet.getTab(tabSheet
						.getSelectedTab()));

				tabSheet.removeTab(tabSheet.getTab(tabSheet.getSelectedTab()));

				val selectedTab = tabSheet.getSelectedTab();
				if (selectedTab == null) {
					setNothingSelected();
					selectedMonitoringMessageGroup = null;
				} else {
					val selectedTabObject = tabSheet.getTab(selectedTab);
					val monitoringMessageGroupObjectId = tabsWithObjectIdsOfMessageGroup.get(selectedTabObject);
					selectedMonitoringMessageGroup = getInterventionAdministrationManagerService()
							.getMonitoringMessageGroup(
									monitoringMessageGroupObjectId);

					setSomethingSelected();
				}

				getAdminUI()
						.showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_GROUP_DELETED);

				closeWindow();
			}
		}, null);
	}
}
