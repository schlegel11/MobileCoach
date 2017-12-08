package ch.ethz.mc.model.persistent.types;

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
/**
 * Supported {@link AnswerTypes}
 *
 * @author Andreas Filler
 */
public enum AnswerTypes {
	FREE_TEXT,
	FREE_TEXT_MULTILINE,
	FREE_TEXT_RAW,
	FREE_TEXT_MULTILINE_RAW,
	FREE_NUMBERS,
	FREE_NUMBERS_RAW,
	LIKERT,
	LIKERT_SILENT,
	LIKERT_SLIDER,
	SELECT_ONE,
	SELECT_MANY,
	SELECT_ONE_IMAGES,
	SELECT_MANY_IMAGES,
	CUSTOM;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}

	public String toJSONField() {
		return nameWithoutRaw().toLowerCase().replace("_", "-");
	}

	private String nameWithoutRaw() {
		switch (this) {
			case FREE_NUMBERS_RAW:
			case FREE_TEXT_MULTILINE_RAW:
			case FREE_TEXT_RAW:
				return name().replace("_RAW", "");
			default:
				return name();
		}
	}

	/**
	 * RAW types are not cleaned on saving
	 * 
	 * @return
	 */
	public boolean isRAW() {
		switch (this) {
			case FREE_NUMBERS_RAW:
			case FREE_TEXT_MULTILINE_RAW:
			case FREE_TEXT_RAW:
				return true;
			default:
				return false;
		}
	}
}
