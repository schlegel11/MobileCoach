package ch.ethz.mc.ui.components.main_view.interventions.monitoring_groups_and_messages;

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
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.ui.components.basics.ShortStringEditComponent;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the monitoring message groups tab with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringMessageGroupsTabComponentWithController
		extends MonitoringMessageGroupsTabComponent {

	private final Intervention intervention;

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

			if (getMonitoringMessageGroupsTabSheet().getComponentCount() == 1) {
				// First tab added
				getMonitoringMessageGroupsTabSheet().setSelectedTab(newTab);
			}
		}

		if (getMonitoringMessageGroupsTabSheet().getComponentCount() > 0) {
			setSomethingSelected();
		}

		// handle tab selection change
		getMonitoringMessageGroupsTabSheet()
				.addSelectedTabChangeListener(new SelectedTabChangeListener() {

					@Override
					public void selectedTabChange(
							final SelectedTabChangeEvent event) {
						log.debug("New group selected");

						val selectedTab = event.getTabSheet().getSelectedTab();
						if (selectedTab == null) {
							setNothingSelected();
						} else {
							refreshRelatedMonitoringMessageGroup();
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

	private MonitoringMessageGroup getRelatedMonitoringMessageGroup() {
		val tabSheet = getMonitoringMessageGroupsTabSheet();
		val component = (MonitoringMessageGroupEditComponentWithController) tabSheet
				.getSelectedTab();

		return component.getMonitoringMessageGroup();
	}

	private void refreshRelatedMonitoringMessageGroup() {
		val tabSheet = getMonitoringMessageGroupsTabSheet();
		val component = (MonitoringMessageGroupEditComponentWithController) tabSheet
				.getSelectedTab();

		component.setMonitoringMessageGroup(
				getInterventionAdministrationManagerService()
						.getMonitoringMessageGroup(
								component.getMonitoringMessageGroup().getId()));
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
						val newTab = addTabComponent(newMonitoringMessageGroup,
								intervention.getId());

						getMonitoringMessageGroupsTabSheet()
								.setSelectedTab(newTab);
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_GROUP_CREATED);

						closeWindow();
					}
				}, null);
	}

	public void moveGroup(final boolean moveLeft) {
		log.debug("Move group {}", moveLeft ? "left" : "right");

		val monitoringMessageGroup = getRelatedMonitoringMessageGroup();

		val swappedMonitoringMessageGroup = getInterventionAdministrationManagerService()
				.monitoringMessageGroupMove(monitoringMessageGroup, moveLeft);

		if (!swappedMonitoringMessageGroup) {
			log.debug("Message group is already at beginning/end of list");
			return;
		}

		val tabSheet = getMonitoringMessageGroupsTabSheet();
		val currentPosition = tabSheet
				.getTabPosition(tabSheet.getTab(tabSheet.getSelectedTab()));
		if (moveLeft) {
			tabSheet.setTabPosition(tabSheet.getTab(tabSheet.getSelectedTab()),
					currentPosition - 1);
		} else {
			tabSheet.setTabPosition(tabSheet.getTab(tabSheet.getSelectedTab()),
					currentPosition + 1);
		}

		refreshRelatedMonitoringMessageGroup();
		setSomethingSelected();
	}

	public void renameGroup() {
		log.debug("Rename group");

		val monitoringMessageGroup = getRelatedMonitoringMessageGroup();

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_MONITORING_MESSAGE_GROUP,
				monitoringMessageGroup.getName(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change name
							getInterventionAdministrationManagerService()
									.monitoringMessageGroupSetName(
											monitoringMessageGroup,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						val tab = getMonitoringMessageGroupsTabSheet();
						tab.getTab(tab.getSelectedTab())
								.setCaption(monitoringMessageGroup.getName());

						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_GROUP_RENAMED);
						closeWindow();
					}
				}, null);
	}

	public void deleteGroup() {
		log.debug("Delete group");

		val monitoringMessageGroup = getRelatedMonitoringMessageGroup();

		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					// Delete group
					getInterventionAdministrationManagerService()
							.monitoringMessageGroupDelete(
									monitoringMessageGroup);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				val tabSheet = getMonitoringMessageGroupsTabSheet();

				tabSheet.removeTab(tabSheet.getTab(tabSheet.getSelectedTab()));

				val selectedTab = tabSheet.getSelectedTab();
				if (selectedTab == null) {
					setNothingSelected();
				} else {
					refreshRelatedMonitoringMessageGroup();
					setSomethingSelected();
				}

				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_GROUP_DELETED);

				closeWindow();
			}
		}, null);
	}
}
