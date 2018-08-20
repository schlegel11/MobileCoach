package ch.ethz.mc.model.persistent.outdated;

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
import ch.ethz.mc.model.ModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * CAUTION: Will only be used for conversion from data model 39 to 40
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class AuthorV39 extends ModelObject {
	private static final long	serialVersionUID	= 9068910637074662586L;

	/**
	 * Admin rights of {@link AuthorV39}
	 */
	@Getter
	@Setter
	private boolean				admin;

	/**
	 * Username of {@link AuthorV39} required to authenticate
	 */
	@Getter
	@Setter
	@NonNull
	private String				username;

	/**
	 * Hash of password of {@link AuthorV39} required to authenticate
	 */
	@Getter
	@Setter
	@NonNull
	private String				passwordHash;
}
