package ch.ethz.mc.ui.views.components;

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
import java.util.List;

/**
 * Extends an {@link AbstractConfirmationComponent} with the ability to set and
 * return
 * a {@link String} value
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public abstract class AbstractStringValueEditComponent
		extends AbstractConfirmationComponent {

	/**
	 * Set the current {@link String} value
	 * 
	 * @return
	 */
	public abstract void setStringValue(final String value);

	/**
	 * Return the current {@link String} value
	 * 
	 * @return
	 */
	public abstract String getStringValue();

	/**
	 * Adds variables to select to the component
	 * 
	 * @return
	 */
	public abstract void addVariables(final List<String> variables);
}
