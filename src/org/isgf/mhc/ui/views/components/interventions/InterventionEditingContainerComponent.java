package org.isgf.mhc.ui.views.components.interventions;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.server.Intervention;
import org.isgf.mhc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the intervention editing container component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class InterventionEditingContainerComponent extends
		AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private Accordion			contentAccordion;
	@AutoGenerated
	private HorizontalLayout	topAreaLayout;
	@AutoGenerated
	private Label				interventionTitleLabel;
	@AutoGenerated
	private Button				listAllInterventionsButton;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	private InterventionEditingContainerComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(
				listAllInterventionsButton,
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__LIST_ALL_INTERVENTIONS_BUTTON);
	}

	public InterventionEditingContainerComponent(final Intervention intervention) {
		this();

		localize(
				interventionTitleLabel,
				AdminMessageStrings.INTERVENTION_EDITING_CONTAINER__INTERVENTIONS_TITLE,
				intervention.getName());
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setStyleName("intervention-container");
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// topAreaLayout
		topAreaLayout = buildTopAreaLayout();
		mainLayout.addComponent(topAreaLayout);
		mainLayout.setComponentAlignment(topAreaLayout, new Alignment(48));

		// contentAccordion
		contentAccordion = new Accordion();
		contentAccordion.setImmediate(false);
		contentAccordion.setWidth("100.0%");
		contentAccordion.setHeight("100.0%");
		mainLayout.addComponent(contentAccordion);
		mainLayout.setExpandRatio(contentAccordion, 1.0f);
		mainLayout.setComponentAlignment(contentAccordion, new Alignment(48));

		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildTopAreaLayout() {
		// common part: create layout
		topAreaLayout = new HorizontalLayout();
		topAreaLayout.setStyleName("bold center");
		topAreaLayout.setImmediate(false);
		topAreaLayout.setWidth("100.0%");
		topAreaLayout.setHeight("-1px");
		topAreaLayout.setMargin(true);
		topAreaLayout.setSpacing(true);

		// listAllInterventionsButton
		listAllInterventionsButton = new Button();
		listAllInterventionsButton.setCaption("!!! Back To List");
		listAllInterventionsButton.setImmediate(true);
		listAllInterventionsButton.setWidth("100px");
		listAllInterventionsButton.setHeight("-1px");
		topAreaLayout.addComponent(listAllInterventionsButton);

		// interventionTitleLabel
		interventionTitleLabel = new Label();
		interventionTitleLabel.setStyleName("title-label");
		interventionTitleLabel.setImmediate(false);
		interventionTitleLabel.setWidth("100.0%");
		interventionTitleLabel.setHeight("-1px");
		interventionTitleLabel.setValue("!!! Intervention ABC");
		topAreaLayout.addComponent(interventionTitleLabel);
		topAreaLayout.setExpandRatio(interventionTitleLabel, 1.0f);
		topAreaLayout.setComponentAlignment(interventionTitleLabel,
				new Alignment(48));

		return topAreaLayout;
	}

}
