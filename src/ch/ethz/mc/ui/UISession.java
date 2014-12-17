package ch.ethz.mc.ui;

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
import java.io.Serializable;

import lombok.Data;

import org.bson.types.ObjectId;

@Data
public class UISession implements Serializable {
	private static final long	serialVersionUID		= 1L;

	boolean						isLoggedIn				= false;

	boolean						isAdmin					= false;

	ObjectId					currentAuthorId			= null;

	String						currentAuthorUsername	= null;
}
