package ch.ethz.mc.tools;

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
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Creates Ids that have a very, very, very high chance to be unique
 *
 * @author Andreas Filler
 */
public class GlobalUniqueIdGenerator {
	/**
	 * Creates an Id that has a very, very, very high chance to be unique
	 *
	 * @return
	 */
	public static String createGlobalUniqueId() {
		final String partOne = String.valueOf(InternalDateTime
				.currentTimeMillis());
		final String partTwo = RandomStringUtils.randomAlphanumeric(200);
		return partOne + "-" + partTwo;
	}
}