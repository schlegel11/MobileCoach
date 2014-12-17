package ch.ethz.mc.model.ui;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
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
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UISimulatedMessage extends UIObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	TIMESTAMP	= "timestamp";
	public static final String	TYPE		= "type";
	public static final String	MESSAGE		= "message";

	@PropertyId(TIMESTAMP)
	private Date				timestamp;

	@PropertyId(TYPE)
	private String				type;

	@PropertyId(MESSAGE)
	private String				message;

	public static Object[] getVisibleColumns() {
		return new Object[] { TIMESTAMP, TYPE, MESSAGE };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__TIMESTAMP),
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGE_TYPE),
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGE_TEXT) };
	}

	public static String getSortColumn() {
		return TIMESTAMP;
	}

	@Override
	public String toString() {
		return message;
	}
}
