package ch.ethz.mc.ui.views.components.basics;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab
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
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.ui.views.components.AbstractClosableEditComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Upload;

/**
 * Provides a simple component to upload files to the temporary directory for
 * further handling
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class FileUploadComponent extends AbstractClosableEditComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private HorizontalLayout	mainLayout;
	@AutoGenerated
	private Button				cancelButton;
	@AutoGenerated
	private Upload				uploadComponent;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public FileUploadComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		uploadComponent.setButtonCaption(Messages
				.getAdminString(AdminMessageStrings.GENERAL__UPLOAD));
		localize(cancelButton, AdminMessageStrings.GENERAL__CANCEL);

		uploadComponent.setEnabled(true);
	}

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		cancelButton.addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(final ClickListener clickListener) {
		// do nothing
	}

	@AutoGenerated
	private HorizontalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new HorizontalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("535px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);

		// top-level component properties
		setWidth("535px");
		setHeight("-1px");

		// uploadComponent
		uploadComponent = new Upload();
		uploadComponent.setStyleName("upload-component");
		uploadComponent.setImmediate(false);
		uploadComponent.setWidth("100.0%");
		uploadComponent.setHeight("-1px");
		mainLayout.addComponent(uploadComponent);
		mainLayout.setExpandRatio(uploadComponent, 1.0f);

		// cancelButton
		cancelButton = new Button();
		cancelButton.setCaption("!!! Cancel");
		cancelButton.setImmediate(true);
		cancelButton.setWidth("100px");
		cancelButton.setHeight("-1px");
		mainLayout.addComponent(cancelButton);
		mainLayout.setComponentAlignment(cancelButton, new Alignment(34));

		return mainLayout;
	}

}
