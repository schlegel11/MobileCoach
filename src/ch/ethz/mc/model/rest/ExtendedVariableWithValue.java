package ch.ethz.mc.model.rest;

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
import lombok.Getter;

/**
 * Wrapper for a variable with a value for REST (extended to describe if the
 * variable belongs to the participant)
 *
 * @author Andreas Filler
 */
public class ExtendedVariableWithValue extends VariableWithValue {
	public ExtendedVariableWithValue(final String variable, final String value,
			final boolean ownValue) {
		super(variable, value);
		this.ownValue = ownValue;
	}

	@Getter
	private final boolean	ownValue;
}
