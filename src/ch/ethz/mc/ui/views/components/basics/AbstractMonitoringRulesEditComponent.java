package ch.ethz.mc.ui.views.components.basics;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab at the Health-IS Lab
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
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the abstract monitoring rules edit component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class AbstractMonitoringRulesEditComponent extends
AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;
	@AutoGenerated
	private Button				deleteButton;
	@AutoGenerated
	private Button				duplicateButton;
	@AutoGenerated
	private Button				collapseButton;
	@AutoGenerated
	private Button				expandButton;
	@AutoGenerated
	private Button				editButton;
	@AutoGenerated
	private Button				newButton;
	@AutoGenerated
	private HorizontalLayout	treeLayout;
	@AutoGenerated
	private VerticalLayout		ruleDetailsLayout;
	@AutoGenerated
	private Label				monitoringRuleInfoLabel;
	@AutoGenerated
	private Label				sendMessageValue;
	@AutoGenerated
	private Label				sendMessageLabel;
	@AutoGenerated
	private Label				resultVariableValue;
	@AutoGenerated
	private Label				resultVariableLabel;
	@AutoGenerated
	private Panel				rulesTreePanel;
	@AutoGenerated
	private GridLayout			rulesTreePanelLayout;
	@AutoGenerated
	private Tree				rulesTree;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected AbstractMonitoringRulesEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(newButton, AdminMessageStrings.GENERAL__NEW);
		localize(editButton, AdminMessageStrings.GENERAL__EDIT);
		localize(expandButton, AdminMessageStrings.GENERAL__EXPAND);
		localize(collapseButton, AdminMessageStrings.GENERAL__COLLAPSE);
		localize(duplicateButton, AdminMessageStrings.GENERAL__DUPLICATE);
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);

		localize(
				resultVariableLabel,
				AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__RESULT_VARIABLE_OF_SELECTED_RULE);
		localize(
				sendMessageLabel,
				AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__SEND_MESSAGE_AFTER_EXECUTION_OF_SELECTED_RULE);

		monitoringRuleInfoLabel.setContentMode(ContentMode.HTML);

		localize(
				monitoringRuleInfoLabel,
				AdminMessageStrings.ABSTRACT_MONITORING_RULES_EDITING__RULE_INFO_LABEL);

		// adjust rules tree
		rulesTree.setImmediate(true);
		rulesTree.setMultiSelect(false);
		rulesTree.setSelectable(true);

		// set button start state
		setNothingSelected();
	}

	protected void setNothingSelected() {
		editButton.setEnabled(false);
		expandButton.setEnabled(false);
		collapseButton.setEnabled(false);
		duplicateButton.setEnabled(false);
		deleteButton.setEnabled(false);

		resultVariableValue.setValue("");
		sendMessageValue.setValue("");
	}

	protected void setSomethingSelected(final String resultVariable,
			final String sendMessage) {
		editButton.setEnabled(true);
		expandButton.setEnabled(true);
		collapseButton.setEnabled(true);
		duplicateButton.setEnabled(true);
		deleteButton.setEnabled(true);

		resultVariableValue.setValue(resultVariable);
		sendMessageValue.setValue(sendMessage);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("100.0%");
		setHeight("-1px");

		// treeLayout
		treeLayout = buildTreeLayout();
		mainLayout.addComponent(treeLayout);
		mainLayout.setExpandRatio(treeLayout, 1.0f);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);

		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildTreeLayout() {
		// common part: create layout
		treeLayout = new HorizontalLayout();
		treeLayout.setImmediate(false);
		treeLayout.setWidth("100.0%");
		treeLayout.setHeight("350px");
		treeLayout.setMargin(false);
		treeLayout.setSpacing(true);

		// rulesTreePanel
		rulesTreePanel = buildRulesTreePanel();
		treeLayout.addComponent(rulesTreePanel);
		treeLayout.setExpandRatio(rulesTreePanel, 1.0f);

		// ruleDetailsLayout
		ruleDetailsLayout = buildRuleDetailsLayout();
		treeLayout.addComponent(ruleDetailsLayout);

		return treeLayout;
	}

	@AutoGenerated
	private Panel buildRulesTreePanel() {
		// common part: create layout
		rulesTreePanel = new Panel();
		rulesTreePanel.setImmediate(false);
		rulesTreePanel.setWidth("100.0%");
		rulesTreePanel.setHeight("100.0%");

		// rulesTreePanelLayout
		rulesTreePanelLayout = buildRulesTreePanelLayout();
		rulesTreePanel.setContent(rulesTreePanelLayout);

		return rulesTreePanel;
	}

	@AutoGenerated
	private GridLayout buildRulesTreePanelLayout() {
		// common part: create layout
		rulesTreePanelLayout = new GridLayout();
		rulesTreePanelLayout.setImmediate(false);
		rulesTreePanelLayout.setWidth("100.0%");
		rulesTreePanelLayout.setHeight("-1px");
		rulesTreePanelLayout.setMargin(false);

		// rulesTree
		rulesTree = new Tree();
		rulesTree.setImmediate(false);
		rulesTree.setWidth("100.0%");
		rulesTree.setHeight("-1px");
		rulesTreePanelLayout.addComponent(rulesTree, 0, 0);

		return rulesTreePanelLayout;
	}

	@AutoGenerated
	private VerticalLayout buildRuleDetailsLayout() {
		// common part: create layout
		ruleDetailsLayout = new VerticalLayout();
		ruleDetailsLayout.setImmediate(false);
		ruleDetailsLayout.setWidth("350px");
		ruleDetailsLayout.setHeight("100.0%");
		ruleDetailsLayout.setMargin(true);

		// resultVariableLabel
		resultVariableLabel = new Label();
		resultVariableLabel.setStyleName("bold");
		resultVariableLabel.setImmediate(false);
		resultVariableLabel.setWidth("-1px");
		resultVariableLabel.setHeight("25px");
		resultVariableLabel.setValue("!!! Result variable of selected rule:");
		ruleDetailsLayout.addComponent(resultVariableLabel);

		// resultVariableValue
		resultVariableValue = new Label();
		resultVariableValue.setImmediate(false);
		resultVariableValue.setWidth("-1px");
		resultVariableValue.setHeight("25px");
		resultVariableValue.setValue("...");
		ruleDetailsLayout.addComponent(resultVariableValue);

		// sendMessageLabel
		sendMessageLabel = new Label();
		sendMessageLabel.setStyleName("bold");
		sendMessageLabel.setImmediate(false);
		sendMessageLabel.setWidth("-1px");
		sendMessageLabel.setHeight("25px");
		sendMessageLabel
		.setValue("!!! Send message after executing selected rule:");
		ruleDetailsLayout.addComponent(sendMessageLabel);

		// sendMessageValue
		sendMessageValue = new Label();
		sendMessageValue.setImmediate(false);
		sendMessageValue.setWidth("-1px");
		sendMessageValue.setHeight("25px");
		sendMessageValue.setValue("...");
		ruleDetailsLayout.addComponent(sendMessageValue);

		// monitoringRuleInfoLabel
		monitoringRuleInfoLabel = new Label();
		monitoringRuleInfoLabel.setImmediate(false);
		monitoringRuleInfoLabel.setWidth("-1px");
		monitoringRuleInfoLabel.setHeight("-1px");
		monitoringRuleInfoLabel.setValue("!!! INFO");
		ruleDetailsLayout.addComponent(monitoringRuleInfoLabel);
		ruleDetailsLayout.setExpandRatio(monitoringRuleInfoLabel, 1.0f);
		ruleDetailsLayout.setComponentAlignment(monitoringRuleInfoLabel,
				new Alignment(33));

		return ruleDetailsLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildButtonLayout() {
		// common part: create layout
		buttonLayout = new HorizontalLayout();
		buttonLayout.setImmediate(false);
		buttonLayout.setWidth("-1px");
		buttonLayout.setHeight("-1px");
		buttonLayout.setMargin(false);
		buttonLayout.setSpacing(true);

		// newButton
		newButton = new Button();
		newButton.setCaption("!!! New");
		newButton.setIcon(new ThemeResource("img/add-icon-small.png"));
		newButton.setImmediate(true);
		newButton.setWidth("100px");
		newButton.setHeight("-1px");
		buttonLayout.addComponent(newButton);

		// editButton
		editButton = new Button();
		editButton.setCaption("!!! Edit");
		editButton.setIcon(new ThemeResource("img/edit-icon-small.png"));
		editButton.setImmediate(true);
		editButton.setWidth("100px");
		editButton.setHeight("-1px");
		buttonLayout.addComponent(editButton);

		// expandButton
		expandButton = new Button();
		expandButton.setCaption("!!! Expand");
		expandButton.setImmediate(true);
		expandButton.setWidth("100px");
		expandButton.setHeight("-1px");
		buttonLayout.addComponent(expandButton);

		// collapseButton
		collapseButton = new Button();
		collapseButton.setCaption("!!! Collapse");
		collapseButton.setImmediate(true);
		collapseButton.setWidth("100px");
		collapseButton.setHeight("-1px");
		buttonLayout.addComponent(collapseButton);

		// duplicateButton
		duplicateButton = new Button();
		duplicateButton.setCaption("!!! Duplicate");
		duplicateButton.setImmediate(true);
		duplicateButton.setWidth("100px");
		duplicateButton.setHeight("-1px");
		buttonLayout.addComponent(duplicateButton);

		// deleteButton
		deleteButton = new Button();
		deleteButton.setCaption("!!! Delete");
		deleteButton.setIcon(new ThemeResource("img/delete-icon-small.png"));
		deleteButton.setImmediate(true);
		deleteButton.setWidth("100px");
		deleteButton.setHeight("-1px");
		buttonLayout.addComponent(deleteButton);

		return buttonLayout;
	}

}
