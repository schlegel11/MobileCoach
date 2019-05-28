package ch.ethz.mc.model.memory;

import java.util.ArrayList;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.model.rest.Variable;
import ch.ethz.mc.services.internal.CommunicationManagerService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains a message as received by the {@link CommunicationManagerService}
 *
 * @author Andreas Filler
 */
@ToString
public class ReceivedMessage {
	@Getter
	@Setter
	private DialogOptionTypes		type;

	@Getter
	@Setter
	private String					sender;

	@Getter
	@Setter
	private String					message;

	@Getter
	@Setter
	private boolean					typeIntention;

	@Getter
	@Setter
	private String					clientId;

	@Getter
	@Setter
	private int						relatedMessageIdBasedOnOrder;

	@Getter
	@Setter
	private String					intention;

	@Getter
	@Setter
	private String					content;

	@Getter
	@Setter
	private String					text;

	@Getter
	@Setter
	private long					receivedTimestamp;

	@Getter
	@Setter
	private long					clientTimestamp;

	@Getter
	@Setter
	private String					mediaURL;

	@Getter
	@Setter
	private String					mediaType;

	@Getter
	@Setter
	private String					externalSystemId;

	@Getter
	@Setter
	private boolean					externalSystem;

	@Getter
	private final List<Variable>	externalSystemVariables	= new ArrayList<>();

	public boolean addExternalSystemVariable(Variable variable) {
		return externalSystemVariables.add(variable);
	}

}
