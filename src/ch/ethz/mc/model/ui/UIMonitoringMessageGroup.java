package ch.ethz.mc.model.ui;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIMonitoringMessageGroup extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	NAME			= "name";

	public static final String	EXPECTS_ANSWER	= "expectsAnswer";

	@PropertyId(NAME)
	private String				name;

	@PropertyId(EXPECTS_ANSWER)
	private boolean				expectsAnswer;

	@Override
	public String toString() {
		return name
				+ " ("
				+ (expectsAnswer ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__EXPECTS_ANSWER)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__EXPECTS_NO_ANSWER))
				+ ")";
	}
}
