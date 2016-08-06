package ch.ethz.mc.ui.views.components.basics;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
 *
 * For details see README.md file in the root folder of this project.
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
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang.NullArgumentException;
import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.conf.ThemeImageStrings;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.concepts.AbstractMonitoringRule;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.ui.views.components.AbstractClosableEditComponent;
import ch.ethz.mc.ui.views.components.interventions.monitoring_rules.MonitoringReplyRuleEditComponentWithController;
import ch.ethz.mc.ui.views.components.interventions.monitoring_rules.MonitoringRuleEditComponentWithController;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.Tree.TreeTargetDetails;

/**
 * Extends the abstract monitoring rules edit component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public abstract class AbstractMonitoringRulesEditComponentWithController extends
AbstractMonitoringRulesEditComponent {

	private Intervention			intervention;

	// If set we deal with a MonitoringReplyRule
	private ObjectId				relatedMonitoringRuleId;

	private boolean					isMonitoringRule;
	private boolean					isGotAnswerRule;

	private Tree					rulesTree;

	private ObjectId				selectedMonitoringRuleId	= null;

	public static final String		NAME						= "name";
	public static final String		ICON						= "icon";

	private final ThemeResource		RULE_ICON					= new ThemeResource(
			ThemeImageStrings.RULE_ICON_SMALL);
	private final ThemeResource		MESSAGE_RULE_ICON			= new ThemeResource(
			ThemeImageStrings.MESSAGE_ICON_SMALL);
	private final ThemeResource		STOP_RULE_ICON				= new ThemeResource(
			ThemeImageStrings.STOP_ICON_SMALL);

	private HierarchicalContainer	container;

	public AbstractMonitoringRulesEditComponentWithController() {
		super();
	}

	/**
	 * Initialization for {@link MonitoringRule}s
	 *
	 * @param intervention
	 */
	public void init(final Intervention intervention) {
		internalInit(intervention, null, false);
	}

	/**
	 * Initialization for {@link MonitoringReplyRule}s
	 *
	 * @param intervention
	 * @param relatedMonitoringRuleId
	 * @param isGotAnswerRule
	 */
	public void init(final Intervention intervention,
			final ObjectId relatedMonitoringRuleId,
			final boolean isGotAnswerRule) {
		internalInit(intervention, relatedMonitoringRuleId, isGotAnswerRule);
	}

	/**
	 * Internal initialization for {@link MonitoringRule}s and
	 * {@link MonitoringReplyRule}s
	 *
	 * @param intervention
	 * @param relatedMonitoringRuleId
	 * @param isGotAnswerRule
	 */
	private void internalInit(final Intervention intervention,
			final ObjectId relatedMonitoringRuleId,
			final boolean isGotAnswerRule) {
		this.intervention = intervention;

		if (relatedMonitoringRuleId == null) {
			isMonitoringRule = true;
		} else {
			isMonitoringRule = false;

			// Adjust UI of MonitoringReplyRules to have a lower height
			getMonitoringRuleInfoLabel().setVisible(false);
			getTreeLayout().setHeight(130, Unit.PIXELS);
		}
		this.relatedMonitoringRuleId = relatedMonitoringRuleId;
		this.isGotAnswerRule = isGotAnswerRule;

		rulesTree = getRulesTree();

		selectedMonitoringRuleId = null;

		// tree content
		final Set<ObjectId> monitoringRuleIdCache = new HashSet<ObjectId>();

		container = new HierarchicalContainer();
		container.addContainerProperty(NAME, String.class, null);
		container.addContainerProperty(ICON, ThemeResource.class, null);

		rulesTree.setContainerDataSource(container);

		rulesTree.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		rulesTree.setItemCaptionPropertyId(NAME);
		rulesTree.setItemIconPropertyId(ICON);

		rulesTree.setDragMode(TreeDragMode.NODE);
		rulesTree.setDropHandler(new TreeSortDropHandler(rulesTree, container,
				this));

		recursiveAddItemsToTree(null, monitoringRuleIdCache);

		// clean old unlinked rules
		log.debug("Cleaning old unlinked rules");
		Iterable<? extends AbstractMonitoringRule> allMonitoringRules;
		if (isMonitoringRule) {
			allMonitoringRules = getInterventionAdministrationManagerService()
					.getAllMonitoringRulesOfIntervention(intervention.getId());
		} else {
			allMonitoringRules = getInterventionAdministrationManagerService()
					.getAllMonitoringReplyRulesOfMonitoringRule(
							relatedMonitoringRuleId, isGotAnswerRule);
		}
		for (final AbstractMonitoringRule abstractMonitoringRule : allMonitoringRules) {
			if (!monitoringRuleIdCache.contains(abstractMonitoringRule.getId())) {
				log.warn("Deleting unlinked rule with id {}",
						abstractMonitoringRule.getId());
				if (isMonitoringRule) {
					getInterventionAdministrationManagerService()
					.monitoringRuleDelete(
							abstractMonitoringRule.getId());
				} else {
					getInterventionAdministrationManagerService()
					.monitoringReplyRuleDelete(
							abstractMonitoringRule.getId());
				}
			}
		}

		// tree listener
		rulesTree.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				selectedMonitoringRuleId = (ObjectId) event.getProperty()
						.getValue();

				if (selectedMonitoringRuleId == null) {
					log.debug("Unselected monitoring rule");
				} else {
					log.debug("Selected monitoring rule {}",
							selectedMonitoringRuleId);
				}

				adjust();
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getNewButton().addClickListener(buttonClickListener);
		getEditButton().addClickListener(buttonClickListener);
		getExpandButton().addClickListener(buttonClickListener);
		getCollapseButton().addClickListener(buttonClickListener);
		getDuplicateButton().addClickListener(buttonClickListener);
		getDeleteButton().addClickListener(buttonClickListener);
	}

	private void recursiveAddItemsToTree(final ObjectId parentMonitoringRuleId,
			final Set<ObjectId> monitoringRuleIdCache) {
		Iterable<? extends AbstractMonitoringRule> existingMonitoringRules;
		if (isMonitoringRule) {
			existingMonitoringRules = getInterventionAdministrationManagerService()
					.getAllMonitoringRulesOfInterventionAndParent(
							intervention.getId(), parentMonitoringRuleId);
		} else {
			existingMonitoringRules = getInterventionAdministrationManagerService()
					.getAllMonitoringReplyRulesOfMonitoringRuleAndParent(
							relatedMonitoringRuleId, parentMonitoringRuleId,
							isGotAnswerRule);
		}

		for (final AbstractMonitoringRule abstractMonitoringRule : existingMonitoringRules) {
			addItemToTree(abstractMonitoringRule.getId(),
					parentMonitoringRuleId);

			monitoringRuleIdCache.add(abstractMonitoringRule.getId());

			// Recursion to add all sub rules as well
			recursiveAddItemsToTree(abstractMonitoringRule.getId(),
					monitoringRuleIdCache);
		}
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getNewButton()) {
				createRule();
			} else if (event.getButton() == getEditButton()) {
				editRule();
			} else if (event.getButton() == getExpandButton()) {
				expandOrCollapseTree(true);
			} else if (event.getButton() == getCollapseButton()) {
				expandOrCollapseTree(false);
			} else if (event.getButton() == getDuplicateButton()) {
				duplicateRule();
			} else if (event.getButton() == getDeleteButton()) {
				deleteRule();
			}
		}
	}

	private void adjust() {
		if (selectedMonitoringRuleId == null) {
			setNothingSelected();
		} else {
			final AbstractMonitoringRule selectedMonitoringRule;
			if (isMonitoringRule) {
				selectedMonitoringRule = getInterventionAdministrationManagerService()
						.getMonitoringRule(selectedMonitoringRuleId);
			} else {
				selectedMonitoringRule = getInterventionAdministrationManagerService()
						.getMonitoringReplyRule(selectedMonitoringRuleId);
			}

			String resultVariable;
			if (selectedMonitoringRule.getStoreValueToVariableWithName() == null
					|| selectedMonitoringRule.getStoreValueToVariableWithName()
					.equals("")) {
				resultVariable = ImplementationConstants.DEFAULT_OBJECT_NAME;
			} else {
				resultVariable = selectedMonitoringRule
						.getStoreValueToVariableWithName();
			}

			String sendMessage;
			if (selectedMonitoringRule.isSendMessageIfTrue()) {
				if (selectedMonitoringRule.getRelatedMonitoringMessageGroup() == null) {
					sendMessage = Messages
							.getAdminString(AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_MESSAGE_BUT_NO_GROUP_SELECTED);
				} else {
					final MonitoringMessageGroup monitoringMessageGroup = getInterventionAdministrationManagerService()
							.getMonitoringMessageGroup(
									selectedMonitoringRule
									.getRelatedMonitoringMessageGroup());
					if (monitoringMessageGroup == null) {
						sendMessage = Messages
								.getAdminString(AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_MESSAGE_FROM_ALREADY_DELETED_GROUP);
					} else {
						sendMessage = Messages
								.getAdminString(
										AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_MESSAGE_FROM_GROUP,
										monitoringMessageGroup.getName());
					}
				}
			} else {
				if (isMonitoringRule) {
					val selectedMonitoringRuleCasted = (MonitoringRule) selectedMonitoringRule;
					if (selectedMonitoringRuleCasted
							.isStopInterventionWhenTrue()) {
						sendMessage = Messages
								.getAdminString(AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_NO_MESSAGE_BUT_FINISH_INTERVENTION);
					} else {
						sendMessage = Messages
								.getAdminString(AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_NO_MESSAGE);
					}
				} else {
					sendMessage = Messages
							.getAdminString(AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_NO_MESSAGE);
				}
			}

			setSomethingSelected(resultVariable, sendMessage);
		}
	}

	public void expandOrCollapseTree(final boolean expandTree) {
		if (expandTree) {
			rulesTree.expandItemsRecursively(selectedMonitoringRuleId);
		} else {
			rulesTree.collapseItemsRecursively(selectedMonitoringRuleId);
		}
	}

	public void createRule() {
		log.debug("Create rule");
		final AbstractMonitoringRule newAbstractMonitoringRule;
		if (isMonitoringRule) {
			newAbstractMonitoringRule = getInterventionAdministrationManagerService()
					.monitoringRuleCreate(
							intervention.getId(),
							selectedMonitoringRuleId == null ? null
									: selectedMonitoringRuleId);
		} else {
			newAbstractMonitoringRule = getInterventionAdministrationManagerService()
					.monitoringReplyRuleCreate(
							relatedMonitoringRuleId,
							selectedMonitoringRuleId == null ? null
									: selectedMonitoringRuleId, isGotAnswerRule);
		}

		AbstractClosableEditComponent componentWithController;
		if (isMonitoringRule) {
			componentWithController = new MonitoringRuleEditComponentWithController(
					intervention, newAbstractMonitoringRule.getId());
		} else {
			componentWithController = new MonitoringReplyRuleEditComponentWithController(
					intervention, newAbstractMonitoringRule.getId());
		}

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_MONITORING_RULE,
				componentWithController, new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						addItemToTree(newAbstractMonitoringRule.getId(),
								selectedMonitoringRuleId);
						rulesTree.select(newAbstractMonitoringRule.getId());

						getAdminUI()
						.showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MONITORING_RULE_CREATED);

						adjust();

						closeWindow();
					}
				});
	}

	@SuppressWarnings("unchecked")
	public void editRule() {
		log.debug("Edit rule");

		AbstractClosableEditComponent componentWithController;
		if (isMonitoringRule) {
			componentWithController = new MonitoringRuleEditComponentWithController(
					intervention, selectedMonitoringRuleId);
		} else {
			componentWithController = new MonitoringReplyRuleEditComponentWithController(
					intervention, selectedMonitoringRuleId);
		}

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_MONITORING_RULE,
				componentWithController, new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adjust item and icon
						AbstractMonitoringRule selectedAbstractMonitoringRule;
						if (isMonitoringRule) {
							selectedAbstractMonitoringRule = getInterventionAdministrationManagerService()
									.getMonitoringRule(selectedMonitoringRuleId);
						} else {
							selectedAbstractMonitoringRule = getInterventionAdministrationManagerService()
									.getMonitoringReplyRule(
											selectedMonitoringRuleId);
						}

						final String name = StringHelpers
								.createRuleName(selectedAbstractMonitoringRule);

						ThemeResource icon;
						if (selectedAbstractMonitoringRule
								.isSendMessageIfTrue()) {
							icon = MESSAGE_RULE_ICON;
						} else if (isMonitoringRule
								&& ((MonitoringRule) selectedAbstractMonitoringRule)
								.isStopInterventionWhenTrue()) {
							icon = STOP_RULE_ICON;
						} else {
							icon = RULE_ICON;
						}

						val item = container.getItem(selectedMonitoringRuleId);
						item.getItemProperty(NAME).setValue(name);
						item.getItemProperty(ICON).setValue(icon);

						getAdminUI()
						.showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MONITORING_RULE_UPDATED);

						adjust();

						closeWindow();
					}
				});
	}

	@SuppressWarnings("unchecked")
	private void addItemToTree(final ObjectId monitoringRuleId,
			final ObjectId parentMonitoringRuleId) {
		// Create name and icon
		AbstractMonitoringRule abstractMonitoringRule;
		if (isMonitoringRule) {
			abstractMonitoringRule = getInterventionAdministrationManagerService()
					.getMonitoringRule(monitoringRuleId);
		} else {
			abstractMonitoringRule = getInterventionAdministrationManagerService()
					.getMonitoringReplyRule(monitoringRuleId);
		}

		final String name = StringHelpers
				.createRuleName(abstractMonitoringRule);

		ThemeResource icon;
		if (abstractMonitoringRule.isSendMessageIfTrue()) {
			icon = MESSAGE_RULE_ICON;
		} else if (isMonitoringRule
				&& ((MonitoringRule) abstractMonitoringRule)
				.isStopInterventionWhenTrue()) {
			icon = STOP_RULE_ICON;
		} else {
			icon = RULE_ICON;
		}

		// Add item to tree
		val item = container.addItem(monitoringRuleId);
		item.getItemProperty(NAME).setValue(name);
		item.getItemProperty(ICON).setValue(icon);

		container.setChildrenAllowed(monitoringRuleId, true);

		// Care for sub structure at startup
		if (parentMonitoringRuleId != null) {
			container.setParent(monitoringRuleId, parentMonitoringRuleId);
		}
		// Care for sub structure when creating a new node
		if (selectedMonitoringRuleId != null) {
			rulesTree.expandItem(selectedMonitoringRuleId);
		}
	}

	public void moveItem(final TreeSortDropHandler.MOVE movement,
			final ObjectId sourceItemId, final ObjectId parentItemId,
			final ObjectId sameLevelTargetItemId) {
		log.debug("Moving {} {} (parent: {} / same level target: {} )",
				sourceItemId, movement, parentItemId, sameLevelTargetItemId);
		if (isMonitoringRule) {
			getInterventionAdministrationManagerService().monitoringRuleMove(
					movement.ordinal(), sourceItemId, parentItemId,
					sameLevelTargetItemId, intervention.getId());
		} else {
			getInterventionAdministrationManagerService()
			.monitoringReplyRuleMove(movement.ordinal(), sourceItemId,
					parentItemId, sameLevelTargetItemId,
					relatedMonitoringRuleId, isGotAnswerRule);
		}
	}

	public void duplicateRule() {
		log.debug("Duplicate rule");

		final File temporaryBackupFile;
		if (isMonitoringRule) {
			temporaryBackupFile = getInterventionAdministrationManagerService()
					.monitoringRuleExport(selectedMonitoringRuleId);
		} else {
			temporaryBackupFile = getInterventionAdministrationManagerService()
					.monitoringReplyRuleExport(selectedMonitoringRuleId);
		}

		try {
			final AbstractMonitoringRule importedMonitoringRule;
			if (isMonitoringRule) {
				importedMonitoringRule = getInterventionAdministrationManagerService()
						.monitoringRuleImport(temporaryBackupFile);
			} else {
				importedMonitoringRule = getInterventionAdministrationManagerService()
						.monitoringReplyRuleImport(temporaryBackupFile);
			}

			if (importedMonitoringRule == null) {
				throw new NullArgumentException(
						"Imported monitoring rule not found in import");
			}

			// Adapt UI
			addItemToTree(importedMonitoringRule.getId(),
					importedMonitoringRule.getIsSubRuleOfMonitoringRule());
			rulesTree.select(importedMonitoringRule.getId());

			adjust();

			getAdminUI().showInformationNotification(
					AdminMessageStrings.NOTIFICATION__RULE_DUPLICATED);
		} catch (final Exception e) {
			getAdminUI().showWarningNotification(
					AdminMessageStrings.NOTIFICATION__RULE_DUPLICATION_FAILED);
		}

		try {
			temporaryBackupFile.delete();
		} catch (final Exception f) {
			// Do nothing
		}
	}

	public void deleteRule() {
		log.debug("Delete rule");

		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					// Delete rule
					if (isMonitoringRule) {
						getInterventionAdministrationManagerService()
						.monitoringRuleDelete(selectedMonitoringRuleId);
					} else {
						getInterventionAdministrationManagerService()
						.monitoringReplyRuleDelete(
								selectedMonitoringRuleId);
					}
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				container.removeItemRecursively(selectedMonitoringRuleId);

				getAdminUI()
				.showInformationNotification(
						AdminMessageStrings.NOTIFICATION__MONITORING_RULE_DELETED);

				rulesTree.select(null);

				closeWindow();
			}
		}, null);
	}

	private static class TreeSortDropHandler implements DropHandler {
		private final Tree													tree;

		private final AbstractMonitoringRulesEditComponentWithController	component;

		public static enum MOVE {
			AS_CHILD, ABOVE, BELOW
		};

		/**
		 * Manages the drag & drop events on the rules tree
		 *
		 * @param tree
		 * @param container
		 */
		public TreeSortDropHandler(
				final Tree tree,
				final HierarchicalContainer container,
				final AbstractMonitoringRulesEditComponentWithController component) {
			this.tree = tree;
			this.component = component;
		}

		@Override
		public AcceptCriterion getAcceptCriterion() {
			return AcceptAll.get();
		}

		@Override
		public void drop(final DragAndDropEvent dropEvent) {
			// Called whenever a drop occurs on the component

			// Make sure the drag source is the same tree
			final Transferable t = dropEvent.getTransferable();

			// see the comment in getAcceptCriterion()
			if (t.getSourceComponent() != tree
					|| !(t instanceof DataBoundTransferable)) {
				return;
			}

			final TreeTargetDetails dropData = (TreeTargetDetails) dropEvent
					.getTargetDetails();

			final Object sourceItemId = ((DataBoundTransferable) t).getItemId();

			final Object parentTargetItemId = dropData.getItemIdInto();

			final Object sameLevelTargetItemId = dropData.getItemIdOver();

			// Location describes on which part of the node the drop took
			// place
			final VerticalDropLocation location = dropData.getDropLocation();

			moveNode(sourceItemId, parentTargetItemId, sameLevelTargetItemId,
					location);

		}

		/**
		 * Move a node within a tree onto, above or below another node depending
		 * on the drop location.
		 *
		 * @param sourceItemId
		 *            id of the item to move
		 * @param parentTargetItemId
		 *            id of the item onto which the source node should be moved
		 *            (relevant for MIDDLE)
		 * @param sameLevelTargetItemId
		 *            id of the item over which the source node should be moved
		 *            (relevant for BOTTOM)
		 * @param afterItemId
		 *            id of the item after which the source node should be moved
		 *            (relevant for TOP)
		 * @param location
		 *            VerticalDropLocation indicating where the source node was
		 *            dropped relative to the target node
		 */
		private void moveNode(final Object sourceItemId,
				final Object parentTargetItemId,
				final Object sameLevelTargetItemId,
				final VerticalDropLocation location) {
			final HierarchicalContainer container = (HierarchicalContainer) tree
					.getContainerDataSource();

			// Sorting goes as
			// - If dropped ON a node, we append it as a child
			// - If dropped on the TOP part of a node, we move/add it before
			// the node
			// - If dropped on the BOTTOM part of a node, we move/add it
			// after the node
			if (location == VerticalDropLocation.MIDDLE) {
				if (container.setParent(sourceItemId, parentTargetItemId)
						&& container.hasChildren(parentTargetItemId)) {
					// move first in the container
					container.moveAfterSibling(sourceItemId, null);
				}
				// Adjust monitoring rule
				component.moveItem(TreeSortDropHandler.MOVE.AS_CHILD,
						(ObjectId) sourceItemId, (ObjectId) parentTargetItemId,
						(ObjectId) sameLevelTargetItemId);
			} else if (location == VerticalDropLocation.TOP) {
				final Object parentItemId = container
						.getParent(sameLevelTargetItemId);
				if (container.setParent(sourceItemId, parentItemId)) {
					// Reorder only the two items, moving source above target
					container.moveAfterSibling(sourceItemId,
							sameLevelTargetItemId);
					container.moveAfterSibling(sameLevelTargetItemId,
							sourceItemId);
				}
				// Adjust monitoring rule
				component.moveItem(TreeSortDropHandler.MOVE.ABOVE,
						(ObjectId) sourceItemId, (ObjectId) parentItemId,
						(ObjectId) sameLevelTargetItemId);
			} else if (location == VerticalDropLocation.BOTTOM) {
				final Object parentItemId = container
						.getParent(sameLevelTargetItemId);
				if (container.setParent(sourceItemId, parentItemId)) {
					container.moveAfterSibling(sourceItemId,
							sameLevelTargetItemId);
				}
				// Adjust monitoring rule
				component.moveItem(TreeSortDropHandler.MOVE.BELOW,
						(ObjectId) sourceItemId, (ObjectId) parentItemId,
						(ObjectId) sameLevelTargetItemId);
			}
		}
	}
}
