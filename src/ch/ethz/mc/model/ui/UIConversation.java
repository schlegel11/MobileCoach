package ch.ethz.mc.model.ui;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import ch.ethz.mobilecoach.model.persistent.ChatEnginePersistentState;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIConversation extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables	
	public static final String	PARTICIPANT_ID					= "participantId";
	public static final String	ENGINE_STATE					= "engineState";
	public static final String	STATUS					        = "status";
	public static final String	HASH					        = "hash";

	@PropertyId(PARTICIPANT_ID)
	private String				participantId;
	
	@PropertyId(STATUS)
	private String				status;
	
	@PropertyId(HASH)
	private String				hash;
	
	@PropertyId(ENGINE_STATE)
	private String				engineState;



	public static Object[] getVisibleColumns() {
		return new Object[] { PARTICIPANT_ID, STATUS, HASH, ENGINE_STATE };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				"Participant ID",
				"Status",
				"Hash",
				"Engine State"
				};
	}

	public static String getSortColumn() {
		return PARTICIPANT_ID;
	}

	@Override
	public String toString() {
		return getRelatedModelObject(ChatEnginePersistentState.class).getParticipantId().toString();
	}
}
