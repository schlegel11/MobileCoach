package org.isgf.mhc.ui.views.components.basics;

import java.util.HashSet;
import java.util.Set;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.conf.ThemeImageStrings;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.persistent.MonitoringMessageGroup;
import org.isgf.mhc.model.persistent.MonitoringReplyRule;
import org.isgf.mhc.model.persistent.MonitoringRule;
import org.isgf.mhc.model.persistent.concepts.AbstractMonitoringRule;
import org.isgf.mhc.tools.StringHelpers;
import org.isgf.mhc.ui.views.components.AbstractClosableEditComponent;
import org.isgf.mhc.ui.views.components.interventions.monitoring_rules.MonitoringReplyRuleEditComponentWithController;
import org.isgf.mhc.ui.views.components.interventions.monitoring_rules.MonitoringRuleEditComponentWithController;

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
			if (parentMonitoringRuleId == null) {
				addItemToTree(abstractMonitoringRule.getId(), null);
			} else {
				addItemToTree(abstractMonitoringRule.getId(),
						parentMonitoringRuleId);
			}

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
				resultVariable = ImplementationContants.DEFAULT_OBJECT_NAME;
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
				sendMessage = Messages
						.getAdminString(AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_NO_MESSAGE);
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
						addItemToTree(newAbstractMonitoringRule.getId(), null);
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
			container.setParent(monitoringRuleId, selectedMonitoringRuleId);
			rulesTree.expandItem(selectedMonitoringRuleId);
		}
	}

	public void moveItem(final TreeSortDropHandler.MOVE movement,
			final ObjectId sourceItemId, final ObjectId targetItemId) {
		log.debug("Moving {} {} {}", sourceItemId, movement, targetItemId);
		if (isMonitoringRule) {
			getInterventionAdministrationManagerService().monitoringRuleMove(
					movement.ordinal(), sourceItemId, targetItemId,
					intervention.getId());
		} else {
			getInterventionAdministrationManagerService()
					.monitoringReplyRuleMove(movement.ordinal(), sourceItemId,
							targetItemId, relatedMonitoringRuleId,
							isGotAnswerRule);
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
			final Object targetItemId = dropData.getItemIdOver();

			// Location describes on which part of the node the drop took
			// place
			final VerticalDropLocation location = dropData.getDropLocation();

			moveNode(sourceItemId, targetItemId, location);

		}

		/**
		 * Move a node within a tree onto, above or below another node depending
		 * on the drop location.
		 * 
		 * @param sourceItemId
		 *            id of the item to move
		 * @param targetItemId
		 *            id of the item onto which the source node should be moved
		 * @param location
		 *            VerticalDropLocation indicating where the source node was
		 *            dropped relative to the target node
		 */
		private void moveNode(final Object sourceItemId,
				final Object targetItemId, final VerticalDropLocation location) {
			final HierarchicalContainer container = (HierarchicalContainer) tree
					.getContainerDataSource();

			// Sorting goes as
			// - If dropped ON a node, we append it as a child
			// - If dropped on the TOP part of a node, we move/add it before
			// the node
			// - If dropped on the BOTTOM part of a node, we move/add it
			// after the node
			if (location == VerticalDropLocation.MIDDLE) {
				if (container.setParent(sourceItemId, targetItemId)
						&& container.hasChildren(targetItemId)) {
					// move first in the container
					container.moveAfterSibling(sourceItemId, null);
					// Adjust monitoring rule
				}
				component.moveItem(TreeSortDropHandler.MOVE.AS_CHILD,
						(ObjectId) sourceItemId, (ObjectId) targetItemId);
			} else if (location == VerticalDropLocation.TOP) {
				final Object parentId = container.getParent(targetItemId);
				if (container.setParent(sourceItemId, parentId)) {
					// reorder only the two items, moving source above target
					container.moveAfterSibling(sourceItemId, targetItemId);
					container.moveAfterSibling(targetItemId, sourceItemId);
					// Adjust monitoring rule
				}
				component.moveItem(TreeSortDropHandler.MOVE.ABOVE,
						(ObjectId) sourceItemId, (ObjectId) targetItemId);
			} else if (location == VerticalDropLocation.BOTTOM) {
				final Object parentId = container.getParent(targetItemId);
				if (container.setParent(sourceItemId, parentId)) {
					container.moveAfterSibling(sourceItemId, targetItemId);
					// Adjust monitoring rule
				}
				component.moveItem(TreeSortDropHandler.MOVE.BELOW,
						(ObjectId) sourceItemId, (ObjectId) targetItemId);
			}
		}
	}
}
