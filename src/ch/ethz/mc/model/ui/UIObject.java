package ch.ethz.mc.model.ui;

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
import lombok.NoArgsConstructor;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;

/**
 * Basic class for objects that should be displayed in the UI
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public abstract class UIObject {
	/**
	 * Returns the appropriate localization {@link String}, filled with given
	 * placeholders (if provided)
	 * 
	 * @param adminMessageString
	 * @param values
	 * @return
	 */
	protected static String localize(
			final AdminMessageStrings adminMessageString,
			final Object... values) {
		return Messages.getAdminString(adminMessageString, values);
	}

	@Override
	public abstract String toString();
}
