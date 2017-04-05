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
import ch.ethz.mc.modules.AbstractModule;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIModule extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String				NAME	= "name";

	@PropertyId(NAME)
	private String							name;

	private Class<? extends AbstractModule>	moduleClass;

	public static Object[] getVisibleColumns() {
		return new Object[] { NAME };
	}

	public static String[] getColumnHeaders() {
		return new String[] { localize(AdminMessageStrings.MODULES__MODULE_NAME) };
	}

	public static String getSortColumn() {
		return NAME;
	}

	@Override
	public String toString() {
		return name;
	}
}
