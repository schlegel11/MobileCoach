package ch.ethz.mc.ui.components.basics;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ServerSideCriterion;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.Tree.TreeTargetDetails;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.conf.ThemeImageStrings;
import ch.ethz.mc.model.memory.types.RecursiveRuleTypes;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MicroDialog;
import ch.ethz.mc.model.persistent.MicroDialogRule;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.concepts.AbstractMonitoringRule;
import ch.ethz.mc.model.persistent.types.MonitoringRuleTypes;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.ui.components.AbstractClosableEditComponent;
import ch.ethz.mc.ui.components.main_view.interventions.micro_dialogs.MicroDialogRuleEditComponentWithController;
import ch.ethz.mc.ui.components.main_view.interventions.rules.MonitoringReplyRuleEditComponentWithController;
import ch.ethz.mc.ui.components.main_view.interventions.rules.MonitoringRuleEditComponentWithController;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the abstract monitoring rules edit component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public abstract class AbstractMonitoringRulesEditComponentWithController
		extends AbstractMonitoringRulesEditComponent {

	private Intervention			intervention;

	private RecursiveRuleTypes		rulesType;

	private ObjectId				relatedMonitoringRuleId;
	private ObjectId				relatedMicroDialogId;
	private ObjectId				relatedMicroDialogDecisionPointId;

	private boolean					isGotAnswerRule;

	private Tree					rulesTree;

	private ObjectId				selectedMonitoringRuleId		= null;

	public static final String		NAME							= "name";
	public static final String		ICON							= "icon";

	private final ThemeResource		RULE_ICON						= new ThemeResource(
			ThemeImageStrings.RULE_ICON_SMALL);
	private final ThemeResource		SOLVING_RULE_ICON				= new ThemeResource(
			ThemeImageStrings.OK_ICON_SMALL);
	private final ThemeResource		DAILY_RULE_ICON					= new ThemeResource(
			ThemeImageStrings.CALENDAR_ICON_SMALL);
	private final ThemeResource		PERIODIC_RULE_ICON				= new ThemeResource(
			ThemeImageStrings.WATCH_ICON_SMALL);
	private final ThemeResource		UNEXPECTED_MESSAGE_RULE_ICON	= new ThemeResource(
			ThemeImageStrings.BUBBLE_ICON_SMALL);
	private final ThemeResource		INTENTION_RULE_ICON				= new ThemeResource(
			ThemeImageStrings.SIGNS_ICON_SMALL);
	private final ThemeResource		MESSAGE_RULE_ICON				= new ThemeResource(
			ThemeImageStrings.MESSAGE_ICON_SMALL);
	private final ThemeResource		SUPERVISOR_MESSAGE_RULE_ICON	= new ThemeResource(
			ThemeImageStrings.SUPERVISOR_ICON_SMALL);
	private final ThemeResource		STOP_RULE_ICON					= new ThemeResource(
			ThemeImageStrings.STOP_ICON_SMALL);
	private final ThemeResource		REDIRECT_RULE_ICON				= new ThemeResource(
			ThemeImageStrings.REDIRECT_ICON_SMALL);

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
		internalInit(intervention, null, null, null, false);
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
		internalInit(intervention, relatedMonitoringRuleId, null, null,
				isGotAnswerRule);
	}

	/**
	 * Initialization for {@link MicroDialogRule}s
	 *
	 * @param intervention
	 * @param relatedMicroDialogId
	 * @param relatedMicroDialogDecisionPoint
	 */
	public void init(final Intervention intervention,
			final ObjectId relatedMicroDialogId,
			final ObjectId relatedMicroDialogDecisionPoint) {
		internalInit(intervention, null, relatedMicroDialogId,
				relatedMicroDialogDecisionPoint, false);
	}

	/**
	 * Internal initialization for {@link MonitoringRule}s and
	 * {@link MonitoringReplyRule}s
	 *
	 * @param intervention
	 * @param relatedMonitoringRuleId
	 * @param relatedMicroDialogId
	 * @param relatedMicroDialogDecisionPointId
	 * @param isGotAnswerRule
	 */
	private void internalInit(final Intervention intervention,
			final ObjectId relatedMonitoringRuleId,
			final ObjectId relatedMicroDialogId,
			final ObjectId relatedMicroDialogDecisionPointId,
			final boolean isGotAnswerRule) {
		this.intervention = intervention;

		if (relatedMonitoringRuleId == null
				&& relatedMicroDialogDecisionPointId == null) {
			rulesType = RecursiveRuleTypes.MONITORING_RULES;
		} else if (relatedMonitoringRuleId != null) {
			rulesType = RecursiveRuleTypes.MONITORING_REPLY_RULES;

			// Adjust UI for MonitoringReplyRules to have a lower height and
			// remove additional info dialog
			getMonitoringRuleInfoLabel().setVisible(false);
			getTreeLayout().setHeight(130, Unit.PIXELS);
		} else if (relatedMicroDialogDecisionPointId != null) {
			rulesType = RecursiveRuleTypes.MICRO_DIALOG_RULES;

			// Adjust UI for MicroDialogRules
			getSendMessageLabel().setVisible(false);
			getSendMessageValue().setVisible(false);
			getMonitoringRuleInfoLabel().setVisible(false);
		}
		this.relatedMonitoringRuleId = relatedMonitoringRuleId;
		this.relatedMicroDialogId = relatedMicroDialogId;
		this.relatedMicroDialogDecisionPointId = relatedMicroDialogDecisionPointId;
		this.isGotAnswerRule = isGotAnswerRule;

		rulesTree = getRulesTree();

		selectedMonitoringRuleId = null;

		// Tree content
		final Set<ObjectId> monitoringRuleIdCache = new HashSet<ObjectId>();

		container = new HierarchicalContainer();
		container.addContainerProperty(NAME, String.class, null);
		container.addContainerProperty(ICON, ThemeResource.class, null);

		rulesTree.setContainerDataSource(container);

		rulesTree.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		rulesTree.setItemCaptionPropertyId(NAME);
		rulesTree.setItemIconPropertyId(ICON);

		rulesTree.setDragMode(TreeDragMode.NODE);
		rulesTree.setDropHandler(
				new TreeSortDropHandler(rulesTree, container, this));

		recursiveAddItemsToTree(null, monitoringRuleIdCache);

		// Clean old unlinked rules
		log.debug("Cleaning old unlinked rules");
		Iterable<? extends AbstractMonitoringRule> allMonitoringRules = null;
		switch (rulesType) {
			case MONITORING_RULES:
				allMonitoringRules = getInterventionAdministrationManagerService()
						.getAllMonitoringRulesOfIntervention(
								intervention.getId());
				break;
			case MONITORING_REPLY_RULES:
				allMonitoringRules = getInterventionAdministrationManagerService()
						.getAllMonitoringReplyRulesOfMonitoringRule(
								relatedMonitoringRuleId, isGotAnswerRule);
				break;
			case MICRO_DIALOG_RULES:
				allMonitoringRules = getInterventionAdministrationManagerService()
						.getAllMicroDialogRulesOfMicroDialogDecisionPoint(
								relatedMicroDialogDecisionPointId);
				break;
		}

		for (final AbstractMonitoringRule abstractMonitoringRule : allMonitoringRules) {
			if (!monitoringRuleIdCache
					.contains(abstractMonitoringRule.getId())) {
				log.warn("Deleting unlinked rule with id {}",
						abstractMonitoringRule.getId());

				switch (rulesType) {
					case MONITORING_RULES:
						getInterventionAdministrationManagerService()
								.monitoringRuleDelete(
										abstractMonitoringRule.getId());
						break;
					case MONITORING_REPLY_RULES:
						getInterventionAdministrationManagerService()
								.monitoringReplyRuleDelete(
										abstractMonitoringRule.getId());
						break;
					case MICRO_DIALOG_RULES:
						getInterventionAdministrationManagerService()
								.microDialogRuleDelete(
										abstractMonitoringRule.getId());
						break;
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
		Iterable<? extends AbstractMonitoringRule> existingMonitoringRules = null;
		switch (rulesType) {
			case MONITORING_RULES:
				existingMonitoringRules = getInterventionAdministrationManagerService()
						.getAllMonitoringRulesOfInterventionAndParent(
								intervention.getId(), parentMonitoringRuleId);
				break;
			case MONITORING_REPLY_RULES:
				existingMonitoringRules = getInterventionAdministrationManagerService()
						.getAllMonitoringReplyRulesOfMonitoringRuleAndParent(
								relatedMonitoringRuleId, parentMonitoringRuleId,
								isGotAnswerRule);
				break;
			case MICRO_DIALOG_RULES:
				existingMonitoringRules = getInterventionAdministrationManagerService()
						.getAllMicroDialogRulesOfMicroDialogDecisionPointAndParent(
								relatedMicroDialogDecisionPointId,
								parentMonitoringRuleId);
				break;
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
			AbstractMonitoringRule selectedMonitoringRule = null;
			MonitoringRuleTypes ruleType = null;
			switch (rulesType) {
				case MONITORING_RULES:
					selectedMonitoringRule = getInterventionAdministrationManagerService()
							.getMonitoringRule(selectedMonitoringRuleId);
					ruleType = ((MonitoringRule) selectedMonitoringRule)
							.getType();
					break;
				case MONITORING_REPLY_RULES:
					selectedMonitoringRule = getInterventionAdministrationManagerService()
							.getMonitoringReplyRule(selectedMonitoringRuleId);
					ruleType = null;
					break;
				case MICRO_DIALOG_RULES:
					selectedMonitoringRule = getInterventionAdministrationManagerService()
							.getMicroDialogRule(selectedMonitoringRuleId);
					ruleType = null;
					break;
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

			String sendMessage = null;
			if (selectedMonitoringRule.isSendMessageIfTrue()) {
				val recipient = selectedMonitoringRule
						.isSendMessageToSupervisor()
								? Messages.getAdminString(
										AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__TO_SUPERVISOR)
								: Messages.getAdminString(
										AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__TO_PARTICIPANT);
				if (selectedMonitoringRule
						.getRelatedMonitoringMessageGroup() == null) {
					sendMessage = Messages.getAdminString(
							AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_MESSAGE_BUT_NO_GROUP_SELECTED,
							recipient);
				} else {
					final MonitoringMessageGroup monitoringMessageGroup = getInterventionAdministrationManagerService()
							.getMonitoringMessageGroup(selectedMonitoringRule
									.getRelatedMonitoringMessageGroup());
					if (monitoringMessageGroup == null) {
						sendMessage = Messages.getAdminString(
								AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_MESSAGE_FROM_ALREADY_DELETED_GROUP,
								recipient);
					} else {
						sendMessage = Messages.getAdminString(
								AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_MESSAGE_FROM_GROUP,
								recipient, monitoringMessageGroup.getName());
					}
				}
			} else if (selectedMonitoringRule.isActivateMicroDialogIfTrue()) {
				if (selectedMonitoringRule.getRelatedMicroDialog() == null) {
					sendMessage = Messages.getAdminString(
							AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__ACTIVATE_MICRO_DIALOG_BUT_NO_MICRO_DIALOG_SELECTED);
				} else {
					final MicroDialog microDialog = getInterventionAdministrationManagerService()
							.getMicroDialog(selectedMonitoringRule
									.getRelatedMicroDialog());
					if (microDialog == null) {
						sendMessage = Messages.getAdminString(
								AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__ACTIVATE_MICRO_DIALOG_BUT_MICRO_DIALOG_ALREADY_DELETED);
					} else {
						sendMessage = Messages.getAdminString(
								AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__ACTIVATE_MICRO_DIALOG_WITH_NAME,
								microDialog.getName());
					}
				}
			} else {
				switch (rulesType) {
					case MONITORING_RULES:
						val selectedMonitoringRuleCasted = (MonitoringRule) selectedMonitoringRule;
						if (selectedMonitoringRuleCasted
								.isStopInterventionWhenTrue()) {
							sendMessage = Messages.getAdminString(
									AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_NO_MESSAGE_BUT_FINISH_INTERVENTION);
						} else {
							sendMessage = Messages.getAdminString(
									AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_NO_MESSAGE);
						}
						break;
					case MONITORING_REPLY_RULES:
						sendMessage = Messages.getAdminString(
								AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_NO_MESSAGE);
						break;
					case MICRO_DIALOG_RULES:
						sendMessage = "";
						break;
				}
			}

			setSomethingSelected(ruleType, resultVariable, sendMessage);
		}
	}

	/**
	 * Determines the appropriate icon of the rule
	 * 
	 * @param abstractMonitoringRule
	 * @return
	 */
	private ThemeResource getAppropriateIcon(
			final AbstractMonitoringRule abstractMonitoringRule) {
		ThemeResource icon;
		if (abstractMonitoringRule.isSendMessageIfTrue()
				|| abstractMonitoringRule.isActivateMicroDialogIfTrue()) {
			if (abstractMonitoringRule.isSendMessageToSupervisor()) {
				icon = SUPERVISOR_MESSAGE_RULE_ICON;
			} else {
				icon = MESSAGE_RULE_ICON;
			}
		} else if (rulesType == RecursiveRuleTypes.MONITORING_RULES) {
			val monitoringRule = (MonitoringRule) abstractMonitoringRule;
			if (monitoringRule.isStopInterventionWhenTrue()) {
				icon = STOP_RULE_ICON;
			} else if (monitoringRule.getType() == MonitoringRuleTypes.DAILY) {
				icon = DAILY_RULE_ICON;
			} else if (monitoringRule
					.getType() == MonitoringRuleTypes.PERIODIC) {
				icon = PERIODIC_RULE_ICON;
			} else if (monitoringRule
					.getType() == MonitoringRuleTypes.UNEXPECTED_MESSAGE) {
				icon = UNEXPECTED_MESSAGE_RULE_ICON;
			} else if (monitoringRule
					.getType() == MonitoringRuleTypes.USER_INTENTION) {
				icon = INTENTION_RULE_ICON;
			} else if (monitoringRule.isMarkCaseAsSolvedWhenTrue()) {
				icon = SOLVING_RULE_ICON;
			} else {
				icon = RULE_ICON;
			}
		} else if (rulesType == RecursiveRuleTypes.MICRO_DIALOG_RULES) {
			val microDialogRule = (MicroDialogRule) abstractMonitoringRule;
			if (microDialogRule.isStopMicroDialogWhenTrue()) {
				icon = STOP_RULE_ICON;
			} else if (microDialogRule
					.getNextMicroDialogMessageWhenTrue() != null
					|| microDialogRule
							.getNextMicroDialogMessageWhenFalse() != null) {
				icon = REDIRECT_RULE_ICON;
			} else if (microDialogRule.isLeaveDecisionPointWhenTrue()) {
				icon = SOLVING_RULE_ICON;
			} else {
				icon = RULE_ICON;
			}
		} else {
			icon = RULE_ICON;
		}
		return icon;
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
		switch (rulesType) {
			case MONITORING_RULES:
				newAbstractMonitoringRule = getInterventionAdministrationManagerService()
						.monitoringRuleCreate(intervention.getId(),
								selectedMonitoringRuleId == null ? null
										: selectedMonitoringRuleId);
				break;
			case MONITORING_REPLY_RULES:
				newAbstractMonitoringRule = getInterventionAdministrationManagerService()
						.monitoringReplyRuleCreate(relatedMonitoringRuleId,
								selectedMonitoringRuleId == null ? null
										: selectedMonitoringRuleId,
								isGotAnswerRule);
				break;
			case MICRO_DIALOG_RULES:
				newAbstractMonitoringRule = getInterventionAdministrationManagerService()
						.microDialogRuleCreate(
								relatedMicroDialogDecisionPointId,
								selectedMonitoringRuleId == null ? null
										: selectedMonitoringRuleId);
				break;
			default:
				newAbstractMonitoringRule = null;
				break;
		}

		final AbstractClosableEditComponent componentWithController;
		switch (rulesType) {
			case MONITORING_RULES:
				componentWithController = new MonitoringRuleEditComponentWithController(
						intervention, newAbstractMonitoringRule.getId());
				break;
			case MONITORING_REPLY_RULES:
				componentWithController = new MonitoringReplyRuleEditComponentWithController(
						intervention, newAbstractMonitoringRule.getId());
				break;
			case MICRO_DIALOG_RULES:
				componentWithController = new MicroDialogRuleEditComponentWithController(
						intervention, relatedMicroDialogId,
						newAbstractMonitoringRule.getId());
				break;
			default:
				componentWithController = null;
				break;
		}

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_RULE,
				componentWithController, new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						addItemToTree(newAbstractMonitoringRule.getId(),
								selectedMonitoringRuleId);
						rulesTree.select(newAbstractMonitoringRule.getId());

						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__RULE_CREATED);

						adjust();

						closeWindow();
					}
				});
	}

	@SuppressWarnings("unchecked")
	public void editRule() {
		log.debug("Edit rule");

		final AbstractClosableEditComponent componentWithController;
		switch (rulesType) {
			case MONITORING_RULES:
				componentWithController = new MonitoringRuleEditComponentWithController(
						intervention, selectedMonitoringRuleId);
				break;
			case MONITORING_REPLY_RULES:
				componentWithController = new MonitoringReplyRuleEditComponentWithController(
						intervention, selectedMonitoringRuleId);
				break;
			case MICRO_DIALOG_RULES:
				componentWithController = new MicroDialogRuleEditComponentWithController(
						intervention, relatedMicroDialogId,
						selectedMonitoringRuleId);
				break;
			default:
				componentWithController = null;
				break;
		}

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_RULE,
				componentWithController, new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adjust item and icon
						AbstractMonitoringRule selectedAbstractMonitoringRule = null;
						switch (rulesType) {
							case MONITORING_RULES:
								selectedAbstractMonitoringRule = getInterventionAdministrationManagerService()
										.getMonitoringRule(
												selectedMonitoringRuleId);
								break;
							case MONITORING_REPLY_RULES:
								selectedAbstractMonitoringRule = getInterventionAdministrationManagerService()
										.getMonitoringReplyRule(
												selectedMonitoringRuleId);
								break;
							case MICRO_DIALOG_RULES:
								selectedAbstractMonitoringRule = getInterventionAdministrationManagerService()
										.getMicroDialogRule(
												selectedMonitoringRuleId);
								break;
						}

						final String name = StringHelpers.createRuleName(
								selectedAbstractMonitoringRule, true);

						final ThemeResource icon = getAppropriateIcon(
								selectedAbstractMonitoringRule);

						val item = container.getItem(selectedMonitoringRuleId);
						item.getItemProperty(NAME).setValue(name);
						item.getItemProperty(ICON).setValue(icon);

						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__RULE_UPDATED);

						adjust();

						closeWindow();
					}

				});
	}

	@SuppressWarnings("unchecked")
	private void addItemToTree(final ObjectId monitoringRuleId,
			final ObjectId parentMonitoringRuleId) {
		// Create name and icon
		AbstractMonitoringRule abstractMonitoringRule = null;
		switch (rulesType) {
			case MONITORING_RULES:
				abstractMonitoringRule = getInterventionAdministrationManagerService()
						.getMonitoringRule(monitoringRuleId);
				break;
			case MONITORING_REPLY_RULES:
				abstractMonitoringRule = getInterventionAdministrationManagerService()
						.getMonitoringReplyRule(monitoringRuleId);
				break;
			case MICRO_DIALOG_RULES:
				abstractMonitoringRule = getInterventionAdministrationManagerService()
						.getMicroDialogRule(monitoringRuleId);
				break;
		}

		final String name = StringHelpers.createRuleName(abstractMonitoringRule,
				true);

		final ThemeResource icon = getAppropriateIcon(abstractMonitoringRule);

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
		if (sameLevelTargetItemId != null
				&& sourceItemId.equals(sameLevelTargetItemId)) {
			log.debug("Can't move item on itself");
			return;
		}

		switch (rulesType) {
			case MONITORING_RULES:
				getInterventionAdministrationManagerService()
						.monitoringRuleMove(movement.ordinal(), sourceItemId,
								parentItemId, sameLevelTargetItemId,
								intervention.getId());
				break;
			case MONITORING_REPLY_RULES:
				getInterventionAdministrationManagerService()
						.monitoringReplyRuleMove(movement.ordinal(),
								sourceItemId, parentItemId,
								sameLevelTargetItemId, relatedMonitoringRuleId,
								isGotAnswerRule);
				break;
			case MICRO_DIALOG_RULES:
				getInterventionAdministrationManagerService()
						.microDialogRuleMove(movement.ordinal(), sourceItemId,
								parentItemId, sameLevelTargetItemId,
								relatedMicroDialogDecisionPointId);
				break;
		}
	}

	/**
	 * Checks if {@link AbstractMonitoringRule} can be dragged
	 * 
	 * @param abstractMonitoringRuleId
	 * @return
	 */
	private boolean checkIfDragAllowed(
			final ObjectId abstractMonitoringRuleId) {
		if (rulesType == RecursiveRuleTypes.MONITORING_RULES) {
			val monitoringRule = getInterventionAdministrationManagerService()
					.getMonitoringRule(abstractMonitoringRuleId);
			if (monitoringRule.getType() == MonitoringRuleTypes.NORMAL) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public void duplicateRule() {
		log.debug("Duplicate rule");

		File temporaryBackupFile = null;
		switch (rulesType) {
			case MONITORING_RULES:
				temporaryBackupFile = getInterventionAdministrationManagerService()
						.monitoringRuleExport(selectedMonitoringRuleId);
				break;
			case MONITORING_REPLY_RULES:
				temporaryBackupFile = getInterventionAdministrationManagerService()
						.monitoringReplyRuleExport(selectedMonitoringRuleId);
				break;
			case MICRO_DIALOG_RULES:
				temporaryBackupFile = getInterventionAdministrationManagerService()
						.microDialogRuleExport(selectedMonitoringRuleId);
				break;
		}

		try {
			AbstractMonitoringRule importedMonitoringRule = null;
			switch (rulesType) {
				case MONITORING_RULES:
					importedMonitoringRule = getInterventionAdministrationManagerService()
							.monitoringRuleImport(temporaryBackupFile);
					break;
				case MONITORING_REPLY_RULES:
					importedMonitoringRule = getInterventionAdministrationManagerService()
							.monitoringReplyRuleImport(temporaryBackupFile);
					break;
				case MICRO_DIALOG_RULES:
					importedMonitoringRule = getInterventionAdministrationManagerService()
							.microDialogRuleImport(temporaryBackupFile);
					break;
			}

			if (importedMonitoringRule == null) {
				throw new Exception(
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
					switch (rulesType) {
						case MONITORING_RULES:
							getInterventionAdministrationManagerService()
									.monitoringRuleDelete(
											selectedMonitoringRuleId);
							break;
						case MONITORING_REPLY_RULES:
							getInterventionAdministrationManagerService()
									.monitoringReplyRuleDelete(
											selectedMonitoringRuleId);
							break;
						case MICRO_DIALOG_RULES:
							getInterventionAdministrationManagerService()
									.microDialogRuleDelete(
											selectedMonitoringRuleId);
							break;
					}
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				container.removeItemRecursively(selectedMonitoringRuleId);

				getAdminUI().showInformationNotification(
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
		public TreeSortDropHandler(final Tree tree,
				final HierarchicalContainer container,
				final AbstractMonitoringRulesEditComponentWithController component) {
			this.tree = tree;
			this.component = component;
		}

		@Override
		public AcceptCriterion getAcceptCriterion() {
			return new ServerSideCriterion() {
				ObjectId	lastId;
				boolean		lastResult;

				@Override
				public boolean accept(final DragAndDropEvent dragEvent) {
					// Make sure the drag source is the same tree
					final Transferable transferable = dragEvent
							.getTransferable();
					if (transferable.getSourceComponent() != tree
							|| !(transferable instanceof DataBoundTransferable)) {
						return false;
					}

					final ObjectId sourceItemId = (ObjectId) ((DataBoundTransferable) transferable)
							.getItemId();

					if (lastId != null && lastId == sourceItemId) {
						return lastResult;
					}

					lastId = sourceItemId;
					lastResult = component.checkIfDragAllowed(sourceItemId);

					return lastResult;
				}
			};
		}

		@Override
		public void drop(final DragAndDropEvent dropEvent) {
			// Called whenever a drop occurs on the component

			// Make sure the drag source is the same tree
			final Transferable transferable = dropEvent.getTransferable();
			if (transferable.getSourceComponent() != tree
					|| !(transferable instanceof DataBoundTransferable)) {
				return;
			}

			final TreeTargetDetails dropData = (TreeTargetDetails) dropEvent
					.getTargetDetails();

			final Object sourceItemId = ((DataBoundTransferable) transferable)
					.getItemId();

			final Object parentTargetItemId = dropData.getItemIdInto();

			final Object sameLevelTargetItemId = dropData.getItemIdOver();

			// Location describes on which part of the node the drop took
			// place
			final VerticalDropLocation location = dropData.getDropLocation();

			// Check if drag and drop is allowed for source item

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
