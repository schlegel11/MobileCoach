package org.isgf.mhc.ui.views.components;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the about window component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class AboutTextComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout	mainLayout;
	@AutoGenerated
	private RichTextArea	aboutText;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public AboutTextComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		aboutText
				.setValue("<div class=\"about-text\">"
						+ Messages
								.getAdminString(AdminMessageStrings.ABOUT_WINDOW__HTML_TEXT)
						+ "</div>");
		aboutText.setReadOnly(true);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setStyleName("about-window");
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// aboutText
		aboutText = new RichTextArea();
		aboutText.setImmediate(false);
		aboutText.setWidth("100.0%");
		aboutText.setHeight("100.0%");
		mainLayout.addComponent(aboutText);
		mainLayout.setComponentAlignment(aboutText, new Alignment(48));

		return mainLayout;
	}

}
