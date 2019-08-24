package ch.ethz.mc.ui.components.basics;

/* ##LICENSE## */
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Audio;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Video;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.persistent.MediaObject;
import ch.ethz.mc.model.persistent.types.MediaObjectTypes;
import ch.ethz.mc.ui.components.AbstractCustomComponent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Provides the {@link MediaObject} integration component to show, upload and
 * delete media objects
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class MediaObjectIntegrationComponent
		extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private AbsoluteLayout	mainLayout;
	@AutoGenerated
	private Button			createHTMLButton;
	@AutoGenerated
	private Button			setURLButton;
	@AutoGenerated
	private Panel			contentObjectPanel;
	@AutoGenerated
	private GridLayout		contentObjectPanelGridLayout;
	@AutoGenerated
	private Label			contentObjectLabel;
	@AutoGenerated
	private Button			deleteButton;
	@AutoGenerated
	private Upload			uploadComponent;
	@Getter
	private Button			saveButton;

	@Getter
	private TextArea		textArea;

	@Getter
	private Label			htmlLabel;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	public MediaObjectIntegrationComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		uploadComponent.setButtonCaption(
				Messages.getAdminString(AdminMessageStrings.GENERAL__UPLOAD));
		localize(createHTMLButton,
				AdminMessageStrings.MEDIA_OBJECT_INTEGRATION_COMPONENT__CREATE_HTML);
		localize(setURLButton, AdminMessageStrings.GENERAL__SET_URL);
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);
		localize(contentObjectLabel,
				AdminMessageStrings.MEDIA_OBJECT_INTEGRATION_COMPONENT__NO_MEDIA_SET);

		uploadComponent.setEnabled(false);
		deleteButton.setEnabled(false);
	}

	protected void adjustEmbeddedMediaObject(final MediaObjectTypes type,
			final String name, final Resource resource) {

		AbstractComponent mediaComponent = null;

		if (type == null) {
			mediaComponent = new Image(null, resource);
			localize(contentObjectLabel,
					AdminMessageStrings.MEDIA_OBJECT_INTEGRATION_COMPONENT__NO_MEDIA_SET);
			saveButton = null;
			textArea = null;
		} else {
			switch (type) {
				case HTML_TEXT:
					contentObjectLabel.setValue(name);

					final HorizontalLayout horizontalLayout = new HorizontalLayout();
					horizontalLayout.setWidth("100.0%");
					horizontalLayout.setHeight("100.0%");

					htmlLabel = new Label();
					htmlLabel.setImmediate(false);
					htmlLabel.setContentMode(ContentMode.HTML);
					htmlLabel.setWidth("100.0%");
					// htmlLabel.setHeight("100.0%");

					horizontalLayout.addComponent(htmlLabel);

					final AbsoluteLayout absoluteLayout = new AbsoluteLayout();
					absoluteLayout.setWidth("100.0%");
					absoluteLayout.setHeight("100.0%");

					saveButton = new Button();
					saveButton.setCaption("!!! Save");
					saveButton.setImmediate(true);
					saveButton.setWidth("100px");
					saveButton.setHeight("-1px");
					saveButton.setDisableOnClick(true);

					textArea = new TextArea();
					textArea.setImmediate(true);
					textArea.setWidth("100.0%");
					textArea.setHeight("100.0%");

					absoluteLayout.addComponent(textArea,
							"top:0.0px;right:0.0px;bottom:0.0px;left:0.0px;");
					absoluteLayout.addComponent(saveButton,
							"right:10.0px;bottom:10.0px;");

					final HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel(
							horizontalLayout, absoluteLayout);
					horizontalSplitPanel.setWidth("100.0%");
					horizontalSplitPanel.setHeight("100.0%");
					horizontalSplitPanel.setMinSplitPosition(20f,
							Unit.PERCENTAGE);
					horizontalSplitPanel.setMaxSplitPosition(70f,
							Unit.PERCENTAGE);
					mediaComponent = horizontalSplitPanel;

					localize(saveButton, AdminMessageStrings.GENERAL__SAVE);

					break;
				case URL:
					contentObjectLabel.setValue("URL:");
					final Link link = new Link(name, resource);
					link.setStyleName("media-link");
					link.setImmediate(false);
					link.setWidth("100.0%");
					link.setHeight("100.0%");
					link.setTargetName("_blank");
					mediaComponent = link;
					break;
				case AUDIO:
					contentObjectLabel.setValue(name);
					mediaComponent = new Audio("", resource);
					break;
				case IMAGE:
					contentObjectLabel.setValue(name);
					mediaComponent = new Image("", resource);
					break;
				case VIDEO:
					contentObjectLabel.setValue(name);
					mediaComponent = new Video("", resource);
					break;
			}
		}

		mediaComponent.setSizeFull();
		contentObjectPanelGridLayout.removeAllComponents();
		contentObjectPanelGridLayout.addComponent(mediaComponent, 0, 0);
	}

	@AutoGenerated
	private AbsoluteLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new AbsoluteLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// uploadComponent
		uploadComponent = new Upload();
		uploadComponent.setStyleName("upload-component");
		uploadComponent.setImmediate(false);
		uploadComponent.setWidth("100.0%");
		uploadComponent.setHeight("-1px");
		mainLayout.addComponent(uploadComponent,
				"right:105.0px;bottom:0.0px;left:0.0px;");

		// deleteButton
		deleteButton = new Button();
		deleteButton.setCaption("!!! Delete");
		deleteButton.setImmediate(true);
		deleteButton.setWidth("100px");
		deleteButton.setHeight("-1px");
		mainLayout.addComponent(deleteButton, "right:0.0px;bottom:0.0px;");

		// contentObjectLabel
		contentObjectLabel = new Label();
		contentObjectLabel.setImmediate(false);
		contentObjectLabel.setWidth("100.0%");
		contentObjectLabel.setHeight("25px");
		contentObjectLabel.setValue("!!! (no file uploaded)");
		mainLayout.addComponent(contentObjectLabel,
				"top:3.0px;right:105.0px;left:115.0px;");

		// contentObjectPanel
		contentObjectPanel = buildContentObjectPanel();
		mainLayout.addComponent(contentObjectPanel,
				"top:30.0px;right:0.0px;bottom:30.0px;left:0.0px;");

		// setURLButton
		setURLButton = new Button();
		setURLButton.setCaption("!!! Set URL");
		setURLButton.setImmediate(true);
		setURLButton.setWidth("100px");
		setURLButton.setHeight("-1px");
		mainLayout.addComponent(setURLButton, "top:0.0px;right:0.0px;");

		// createHTMLButton
		createHTMLButton = new Button();
		createHTMLButton.setCaption("!!! Create HTML");
		createHTMLButton.setImmediate(true);
		createHTMLButton.setWidth("100px");
		createHTMLButton.setHeight("-1px");
		mainLayout.addComponent(createHTMLButton, "left:0.0px;");

		return mainLayout;
	}

	@AutoGenerated
	private Panel buildContentObjectPanel() {
		// common part: create layout
		contentObjectPanel = new Panel();
		contentObjectPanel.setImmediate(false);
		contentObjectPanel.setWidth("100.0%");
		contentObjectPanel.setHeight("100.0%");

		// contentObjectPanelGridLayout
		contentObjectPanelGridLayout = new GridLayout();
		contentObjectPanelGridLayout.setStyleName("media-content");
		contentObjectPanelGridLayout.setImmediate(false);
		contentObjectPanelGridLayout.setWidth("100.0%");
		contentObjectPanelGridLayout.setHeight("100.0%");
		contentObjectPanelGridLayout.setMargin(false);
		contentObjectPanel.setContent(contentObjectPanelGridLayout);

		return contentObjectPanel;
	}

}
