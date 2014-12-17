package ch.ethz.mc.ui.views.components.basics;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides a abstract rule edit component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class AbstractRuleEditComponent extends AbstractCustomComponent {
	@AutoGenerated
	private VerticalLayout				mainLayout;
	@AutoGenerated
	private VariableTextFieldComponent	ruleComparisonTermTextFieldComponent;
	@AutoGenerated
	private Embedded					arrowDownIcon2;
	@AutoGenerated
	private ComboBox					ruleEquationSignComboBox;
	@AutoGenerated
	private Embedded					arrowDownIcon1;
	@AutoGenerated
	private VariableTextFieldComponent	ruleTextFieldComponent;

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected AbstractRuleEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(
				ruleTextFieldComponent,
				AdminMessageStrings.ABSTRACT_RULE_EDITING__RULE_WITH_PLACEHOLDERS);
		localize(
				ruleComparisonTermTextFieldComponent,
				AdminMessageStrings.ABSTRACT_RULE_EDITING__RULE_COMPARISON_TERM_WITH_PLACEHOLDERS);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);

		// top-level component properties
		setWidth("100.0%");
		setHeight("-1px");

		// ruleTextFieldComponent
		ruleTextFieldComponent = new VariableTextFieldComponent();
		ruleTextFieldComponent.setCaption("!!! Rule (with placeholders):");
		ruleTextFieldComponent.setImmediate(false);
		ruleTextFieldComponent.setWidth("100.0%");
		ruleTextFieldComponent.setHeight("-1px");
		mainLayout.addComponent(ruleTextFieldComponent);

		// arrowDownIcon1
		arrowDownIcon1 = new Embedded();
		arrowDownIcon1.setImmediate(false);
		arrowDownIcon1.setWidth("32px");
		arrowDownIcon1.setHeight("32px");
		arrowDownIcon1.setSource(new ThemeResource("img/arrow-down-icon.png"));
		arrowDownIcon1.setType(1);
		arrowDownIcon1.setMimeType("image/png");
		mainLayout.addComponent(arrowDownIcon1);
		mainLayout.setComponentAlignment(arrowDownIcon1, new Alignment(48));

		// ruleEquationSignComboBox
		ruleEquationSignComboBox = new ComboBox();
		ruleEquationSignComboBox.setImmediate(false);
		ruleEquationSignComboBox.setWidth("100.0%");
		ruleEquationSignComboBox.setHeight("-1px");
		mainLayout.addComponent(ruleEquationSignComboBox);

		// arrowDownIcon2
		arrowDownIcon2 = new Embedded();
		arrowDownIcon2.setImmediate(false);
		arrowDownIcon2.setWidth("32px");
		arrowDownIcon2.setHeight("32px");
		arrowDownIcon2.setSource(new ThemeResource("img/arrow-down-icon.png"));
		arrowDownIcon2.setType(1);
		arrowDownIcon2.setMimeType("image/png");
		mainLayout.addComponent(arrowDownIcon2);
		mainLayout.setComponentAlignment(arrowDownIcon2, new Alignment(48));

		// ruleComparisonTermTextFieldComponent
		ruleComparisonTermTextFieldComponent = new VariableTextFieldComponent();
		ruleComparisonTermTextFieldComponent
				.setCaption("!!! Comparison term (with placeholders):");
		ruleComparisonTermTextFieldComponent.setImmediate(false);
		ruleComparisonTermTextFieldComponent.setWidth("100.0%");
		ruleComparisonTermTextFieldComponent.setHeight("-1px");
		mainLayout.addComponent(ruleComparisonTermTextFieldComponent);

		return mainLayout;
	}

}
