package ch.ethz.mc.ui.views;

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
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.ui.views.components.views.ErrorViewComponent;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;

/**
 * Provides error view
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class ErrorView extends AbstractView implements View {
	private ErrorViewComponent	errorViewComponent;

	@Override
	public void enter(final ViewChangeEvent event) {
		log.debug("Entered ERROR view");

		setSizeFull();

		// Create view and listeners
		errorViewComponent = new ErrorViewComponent();

		// Add view
		this.addComponent(errorViewComponent);
	}
}
