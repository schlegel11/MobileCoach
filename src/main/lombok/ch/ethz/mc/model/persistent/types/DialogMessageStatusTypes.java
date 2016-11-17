package ch.ethz.mc.model.persistent.types;

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
/**
 * Supported dialog message status types
 *
 * @author Andreas Filler
 */
public enum DialogMessageStatusTypes {
	IN_CREATION, PREPARED_FOR_SENDING, SENDING, SENT_AND_WAITING_FOR_ANSWER, SENT_BUT_NOT_WAITING_FOR_ANSWER, SENT_AND_ANSWERED_BY_PARTICIPANT, SENT_AND_ANSWERED_AND_PROCESSED, SENT_AND_NOT_ANSWERED_AND_PROCESSED, RECEIVED_UNEXPECTEDLY;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}
}
