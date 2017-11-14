package ch.ethz.mc.modules;

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
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ui.UIModule;
import ch.ethz.mc.ui.views.components.AbstractClosableEditComponent;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;

/**
 * Abstract module to be extended as module
 * 
 * @author Andreas Filler
 * 
 */
@SuppressWarnings("serial")
@Log4j2
public abstract class AbstractModule extends AbstractClosableEditComponent {

	public AbstractModule() {
		log.debug("Abstract module prepared to show in list");
	}

	/**
	 * Called before the window itself is shown
	 */
	public abstract void prepareToShow(final ObjectId interventionId);

	/**
	 * The name of the module
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * The button to close the module
	 * 
	 * @return
	 */
	protected abstract Button getCloseButton();

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		getCloseButton().addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(
			final ClickListener clickListener) {
		// not required
	}

	public UIModule toUIModule() {
		final UIModule uiModule = new UIModule(getName(), getClass());
		return uiModule;
	}
}
