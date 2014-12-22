package ch.ethz.mc.model.memory;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health IS-Lab
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
import lombok.ToString;
import ch.ethz.mc.services.internal.CommunicationManagerService;

/**
 * Contains a message as received by the {@link CommunicationManagerService}
 * 
 * @author Andreas Filler
 */
@ToString
public class ReceivedMessage {
	@Getter
	@Setter
	private String	recipient;

	@Getter
	@Setter
	private String	sender;

	@Getter
	@Setter
	private String	message;

	@Getter
	@Setter
	private long	receivedTimestamp;
}
