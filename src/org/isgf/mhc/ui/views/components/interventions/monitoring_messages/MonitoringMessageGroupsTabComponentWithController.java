package org.isgf.mhc.ui.views.components.interventions.monitoring_messages;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.server.Intervention;
import org.isgf.mhc.model.server.MonitoringMessageGroup;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;

/**
 * Extends the monitoring message groups tab with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringMessageGroupsTabComponentWithController extends
		MonitoringMessageGroupsTabComponent {

	private final Intervention					intervention;

	private MonitoringMessageGroup				selectedMonitoringMessageGroup	= null;

	private final List<MonitoringMessageGroup>	monitoringMessageGroups			= new ArrayList<MonitoringMessageGroup>();

	public MonitoringMessageGroupsTabComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		// Retrieve monitoring message groups to set current and fill tabs
		final Iterable<MonitoringMessageGroup> monitoringMessageGroupsIterable = getInterventionAdministrationManagerService()
				.getAllMonitoringMessageGroupsOfIntervention(
						intervention.getId());

		for (val monitoringMessageGroup : monitoringMessageGroupsIterable) {
			val newTab = addTabComponent(monitoringMessageGroup,
					intervention.getId());
			monitoringMessageGroups.add(monitoringMessageGroup);

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
							selectedMonitoringMessageGroup = monitoringMessageGroups
									.get(event.getTabSheet().getTabPosition(
											event.getTabSheet().getTab(
													event.getTabSheet()
															.getSelectedTab())));
							setSomethingSelected();
						}
					}
				});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getNewGroupButton().addClickListener(buttonClickListener);
		getRenameGroupButton().addClickListener(buttonClickListener);
		getDeleteGroupButton().addClickListener(buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getNewGroupButton()) {
				createGroup();
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
						monitoringMessageGroups.add(newMonitoringMessageGroup);
						val newTab = addTabComponent(newMonitoringMessageGroup,
								intervention.getId());
						getMonitoringMessageGroupsTabSheet().setSelectedTab(
								newTab);
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_GROUP_CREATED);

						closeWindow();
					}
				}, null);
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
				val tab = getMonitoringMessageGroupsTabSheet();
				tab.removeTab(tab.getTab(tab.getSelectedTab()));

				val selectedTab = tab.getSelectedTab();
				if (selectedTab == null) {
					setNothingSelected();
					selectedMonitoringMessageGroup = null;
				} else {
					selectedMonitoringMessageGroup = monitoringMessageGroups.get(tab
							.getTabPosition(tab.getTab(tab.getSelectedTab())));
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
