package ch.ethz.mc.ui.views.components;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import com.vaadin.ui.Button;

/**
 * Extends an {@link AbstractCustomComponent} with the ability to confirm or
 * cancel
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public abstract class AbstractConfirmationComponent extends
		AbstractCustomComponent {
	/**
	 * Register the listener which is called when the OK button has been clicked
	 * 
	 * @param clickListener
	 */
	public abstract void registerOkButtonListener(
			final Button.ClickListener clickListener);

	/**
	 * Register the listener which is called when the Cancel button has been
	 * clicked
	 * 
	 * @param clickListener
	 */
	public abstract void registerCancelButtonListener(
			final Button.ClickListener clickListener);
}
