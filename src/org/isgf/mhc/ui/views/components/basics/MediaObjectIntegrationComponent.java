package org.isgf.mhc.ui.views.components.basics;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.server.MediaObject;
import org.isgf.mhc.model.server.types.MediaObjectTypes;
import org.isgf.mhc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Audio;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Video;

/**
 * Provides the {@link MediaObject} integration component to show, upload and
 * delete media objects
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class MediaObjectIntegrationComponent extends
		AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private AbsoluteLayout	mainLayout;
	@AutoGenerated
	private Label			contentObjectLabel;
	@AutoGenerated
	private Panel			contentObjectPanel;
	@AutoGenerated
	private VerticalLayout	contentObjectLayout;
	@AutoGenerated
	private Button			deleteButton;
	@AutoGenerated
	private Upload			uploadComponent;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public MediaObjectIntegrationComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		uploadComponent.setButtonCaption(Messages
				.getAdminString(AdminMessageStrings.GENERAL__UPLOAD));
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);
		localize(
				contentObjectLabel,
				AdminMessageStrings.MEDIA_OBJECT_INTEGRATION_COMPONENT__NO_FILE_UPLOADED);

		uploadComponent.setEnabled(false);
		deleteButton.setEnabled(false);
	}

	protected void adjustEmbeddedMediaObject(final MediaObjectTypes type,
			final String fileName, final Resource resource) {

		AbstractComponent mediaComponent = null;

		if (type == null) {
			mediaComponent = new Image(null, resource);
			localize(
					contentObjectLabel,
					AdminMessageStrings.MEDIA_OBJECT_INTEGRATION_COMPONENT__NO_FILE_UPLOADED);
		} else {
			contentObjectLabel.setValue(fileName);
			switch (type) {
				case AUDIO:
					mediaComponent = new Audio("", resource);
					break;
				case HTML_TEXT:
					mediaComponent = new BrowserFrame("", resource);
					break;
				case IMAGE:
					mediaComponent = new Image("", resource);
					break;
				case VIDEO:
					mediaComponent = new Video("", resource);
					break;
			}
		}

		mediaComponent.setSizeFull();
		contentObjectPanel.setContent(mediaComponent);
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

		// contentObjectPanel
		contentObjectPanel = buildContentObjectPanel();
		mainLayout.addComponent(contentObjectPanel,
				"top:20.0px;right:0.0px;bottom:30.0px;left:0.0px;");

		// contentObjectLabel
		contentObjectLabel = new Label();
		contentObjectLabel.setImmediate(false);
		contentObjectLabel.setWidth("100.0%");
		contentObjectLabel.setHeight("25px");
		contentObjectLabel.setValue("!!! (no file uploaded)");
		mainLayout.addComponent(contentObjectLabel,
				"top:0.0px;right:5.0px;left:0.0px;");

		return mainLayout;
	}

	@AutoGenerated
	private Panel buildContentObjectPanel() {
		// common part: create layout
		contentObjectPanel = new Panel();
		contentObjectPanel.setImmediate(false);
		contentObjectPanel.setWidth("100.0%");
		contentObjectPanel.setHeight("100.0%");

		// contentObjectLayout
		contentObjectLayout = new VerticalLayout();
		contentObjectLayout.setImmediate(false);
		contentObjectLayout.setWidth("100.0%");
		contentObjectLayout.setHeight("100.0%");
		contentObjectLayout.setMargin(false);
		contentObjectPanel.setContent(contentObjectLayout);

		return contentObjectPanel;
	}

}
