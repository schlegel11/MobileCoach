package ch.ethz.mc.model.rest;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
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
import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for variables with values for REST (extended to describe if the
 * variable belongs to the participant)
 *
 * @author Andreas Filler
 */
@AllArgsConstructor
public class ExtendedListVariable {
	@Getter
	private final List<Variable>	variables	= new ArrayList<Variable>();
	@Getter
	@Setter
	private String					identifier;
	@Getter
	@Setter
	private boolean					ownValue;
}
