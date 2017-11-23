package ch.ethz.mc.ui.views;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.ui.AdminNavigatorUI;
import ch.ethz.mc.ui.UISession;
import ch.ethz.mc.ui.components.AbstractCustomComponent;

import com.vaadin.navigator.View;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides methods for all {@link View}s
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public abstract class AbstractView extends VerticalLayout implements View {

	/**
	 * Returns the current {@link UISession}
	 * 
	 * @return
	 */
	protected UISession getUISession() {
		return getAdminUI().getUISession();
	}

	/**
	 * Returns the {@link AdminNavigatorUI}
	 * 
	 * @return
	 */
	protected AdminNavigatorUI getAdminUI() {
		return (AdminNavigatorUI) getUI();
	}

	/**
	 * Adds a tab to the given {@link Accordion} and allows to add an optional
	 * {@link ThemeResource} icon
	 * 
	 * @param accordion
	 * @param tabComponent
	 * @param accordionCaption
	 * @param accordionIcon
	 * @return
	 */
	protected Tab addTab(final Accordion accordion,
			final AbstractCustomComponent tabComponent,
			final AdminMessageStrings accordionCaption,
			final String accordionIcon) {
		if (accordionIcon == null) {
			return accordion.addTab(tabComponent,
					Messages.getAdminString(accordionCaption));
		} else {
			return accordion.addTab(tabComponent,
					Messages.getAdminString(accordionCaption),
					new ThemeResource(accordionIcon));
		}
	}
}
