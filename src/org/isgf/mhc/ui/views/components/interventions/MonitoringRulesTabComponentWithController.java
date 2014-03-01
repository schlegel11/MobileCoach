package org.isgf.mhc.ui.views.components.interventions;

import java.util.Hashtable;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.conf.ThemeImageStrings;
import org.isgf.mhc.model.server.Intervention;
import org.isgf.mhc.model.server.MonitoringMessageGroup;
import org.isgf.mhc.model.server.MonitoringRule;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Tree;

/**
 * Extends the monitoring rules tab with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringRulesTabComponentWithController extends
		MonitoringRulesTabComponent {

	private final Intervention							intervention;

	private final MonitoringRulesEditComponent			monitoringRulesEditComponent;
	private final Tree									rulesTree;

	private MonitoringRule								selectedMonitoringRule	= null;

	public static final String							NAME					= "name";
	public static final String							ICON					= "icon";

	private final ThemeResource							RULE_ICON				= new ThemeResource(
																						ThemeImageStrings.RULE_ICON_SMALL);
	private final ThemeResource							MESSAGE_RULE_ICON		= new ThemeResource(
																						ThemeImageStrings.MESSAGE_ICON_SMALL);

	private final HierarchicalContainer					container;

	private final Hashtable<ObjectId, MonitoringRule>	monitoringRuleCache		= new Hashtable<ObjectId, MonitoringRule>();

	public MonitoringRulesTabComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		monitoringRulesEditComponent = getMonitoringRulesEditComponent();
		rulesTree = monitoringRulesEditComponent.getRulesTree();

		selectedMonitoringRule = null;

		// tree content
		container = new HierarchicalContainer();
		container.addContainerProperty(NAME, String.class, null);
		container.addContainerProperty(ICON, ThemeResource.class, null);

		rulesTree.setContainerDataSource(container);

		rulesTree.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		rulesTree.setItemCaptionPropertyId(NAME);
		rulesTree.setItemIconPropertyId(ICON);

		recursiveAddItemsToTree(null);

		// tree listener
		rulesTree.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				selectedMonitoringRule = (MonitoringRule) event.getProperty()
						.getValue();

				adjust();
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		monitoringRulesEditComponent.getNewButton().addClickListener(
				buttonClickListener);
		monitoringRulesEditComponent.getEditButton().addClickListener(
				buttonClickListener);
		monitoringRulesEditComponent.getDeleteButton().addClickListener(
				buttonClickListener);
	}

	private void recursiveAddItemsToTree(final ObjectId parentMonitoringRuleId) {
		val existingMonitoringRules = getInterventionAdministrationManagerService()
				.getAllMonitoringRulesOfInterventionAndParent(
						intervention.getId(), parentMonitoringRuleId);

		for (final MonitoringRule monitoringRule : existingMonitoringRules) {
			if (parentMonitoringRuleId == null) {
				addItemToTree(monitoringRule, null);
			} else {
				addItemToTree(monitoringRule,
						monitoringRuleCache.get(parentMonitoringRuleId));
			}

			monitoringRuleCache.put(monitoringRule.getId(), monitoringRule);

			// Recursion to add all sub rules as well
			recursiveAddItemsToTree(monitoringRule.getId());
		}
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == monitoringRulesEditComponent
					.getNewButton()) {
				createRule();
			} else if (event.getButton() == monitoringRulesEditComponent
					.getEditButton()) {
				editRule();
			} else if (event.getButton() == monitoringRulesEditComponent
					.getDeleteButton()) {
				deleteRule();
			}
		}
	}

	private void adjust() {
		if (selectedMonitoringRule == null) {
			monitoringRulesEditComponent.setNothingSelected();
		} else {
			String resultVariable;
			if (selectedMonitoringRule.getStoreValueToVariableWithName() == null
					|| selectedMonitoringRule.getStoreValueToVariableWithName()
							.equals("")) {
				resultVariable = ImplementationContants.DEFAULT_OBJECT_NAME;
			} else {
				resultVariable = selectedMonitoringRule
						.getStoreValueToVariableWithName();
			}

			String sendMessage;
			if (selectedMonitoringRule.getRelatedMonitoringMessageGroup() == null) {
				sendMessage = Messages
						.getAdminString(AdminMessageStrings.MONITORING_RULES_EDITING__SEND_NO_MESSAGE);
			} else {
				final MonitoringMessageGroup monitoringMessageGroup = getInterventionAdministrationManagerService()
						.getMonitoringMessageGroup(
								selectedMonitoringRule
										.getRelatedMonitoringMessageGroup());
				if (monitoringMessageGroup == null) {
					sendMessage = Messages
							.getAdminString(AdminMessageStrings.MONITORING_RULES_EDITING__SEND_MESSAGE_FROM_ALREADY_DELETED_GROUP);
				} else {
					sendMessage = Messages
							.getAdminString(
									AdminMessageStrings.MONITORING_RULES_EDITING__SEND_MESSAGE_FROM_GROUP,
									monitoringMessageGroup.getName());
				}
				resultVariable = selectedMonitoringRule
						.getStoreValueToVariableWithName();
			}

			monitoringRulesEditComponent.setSomethingSelected(resultVariable,
					sendMessage);
		}
	}

	public void createRule() {
		log.debug("Create rule");
		final MonitoringRule newMonitoringRule = getInterventionAdministrationManagerService()
				.monitoringRuleCreate(
						intervention.getId(),
						selectedMonitoringRule == null ? null
								: selectedMonitoringRule.getId());

		// TODO
		// Adapt UI

		addItemToTree(newMonitoringRule, null);
		rulesTree.select(newMonitoringRule);

		// showModalModelObjectEditWindow(
		// AdminMessageStrings.ABSTRACT_MODEL_OBJECT_EDIT_WINDOW__CREATE_MONITORING_MESSAGE,
		// new MonitoringMessageEditComponentWithController(
		// newMonitoringMessage, interventionId),
		// new ExtendableButtonClickListener() {
		// @Override
		// public void buttonClick(final ClickEvent event) {
		// // Adapt UI
		// beanContainer.addItem(newMonitoringMessage.getId(),
		// UIMonitoringMessage.class
		// .cast(newMonitoringMessage
		// .toUIModelObject()));
		// getMonitoringMessageTable().select(
		// newMonitoringMessage.getId());
		// getAdminUI()
		// .showInformationNotification(
		// AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_CREATED);
		//
		// closeWindow();
		// }
		// });
	}

	@SuppressWarnings("unchecked")
	private void addItemToTree(final MonitoringRule monitoringRule,
			final MonitoringRule parentMonitoringRule) {
		// Create name and icon
		String name = monitoringRule.getRuleWithPlaceholders();
		if (name == null || name.equals("")) {
			name = ImplementationContants.DEFAULT_OBJECT_NAME;
		}
		// TODO remove after debugging
		name = monitoringRule.getOrder() + ". " + name;

		ThemeResource icon;
		if (monitoringRule.getStoreValueToVariableWithName() == null
				|| monitoringRule.getStoreValueToVariableWithName().equals("")) {
			icon = RULE_ICON;
		} else {
			icon = MESSAGE_RULE_ICON;
		}

		// Add item to tree
		val item = container.addItem(monitoringRule);
		item.getItemProperty(NAME).setValue(name);
		item.getItemProperty(ICON).setValue(icon);

		container.setChildrenAllowed(monitoringRule, true);

		// Care for sub structure at startup
		if (parentMonitoringRule != null) {
			container.setParent(monitoringRule, parentMonitoringRule);
		}
		// Care for sub structure when creating a new node
		if (selectedMonitoringRule != null) {
			container.setParent(monitoringRule, selectedMonitoringRule);
			rulesTree.expandItem(selectedMonitoringRule);
		}
	}

	public void editRule() {
		log.debug("Edit rule");

		// showModalModelObjectEditWindow(
		// AdminMessageStrings.ABSTRACT_MODEL_OBJECT_EDIT_WINDOW__EDIT_MONITORING_MESSAGE,
		// new MonitoringMessageEditComponentWithController(
		// selectedMonitoringMessage, interventionId),
		// new ExtendableButtonClickListener() {
		// @Override
		// public void buttonClick(final ClickEvent event) {
		// // Adapt UI
		// removeAndAdd(beanContainer, selectedMonitoringMessage);
		// getMonitoringMessageTable().sort();
		// getMonitoringMessageTable().select(
		// selectedMonitoringMessage.getId());
		// getAdminUI()
		// .showInformationNotification(
		// AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_UPDATED);
		//
		// closeWindow();
		// }
		// });
	}

	public void deleteRule() {
		log.debug("Delete rule");

		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					// Delete rule
					getInterventionAdministrationManagerService()
							.monitoringRuleDelete(selectedMonitoringRule);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				// TODO
				// rulesTree.removeItem(selectedUIMonitoringRule);

				getAdminUI()
						.showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MONITORING_RULE_DELETED);

				closeWindow();
			}
		}, null);
	}
}
