package ch.ethz.mc.conf;

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
 * Contains some implementation specific constants for Deepstream
 *
 * @author Andreas Filler
 */
public class DeepstreamConstants {
	public static final String	DS_FIELD_USERNAME					= "username";
	public static final String	DS_FIELD_AUTH_DATA					= "authData";
	public static final String	DS_FIELD_CLIENT_DATA				= "clientData";
	public static final String	DS_FIELD_SERVER_DATA				= "serverData";

	public static final String	REST_FIELD_PARTICIPANT				= "participant";
	public static final String	REST_FIELD_NICKNAME					= "nickname";
	public static final String	REST_FIELD_CLIENT_VERSION			= "client-version";
	public static final String	REST_FIELD_USER						= "user";
	public static final String	REST_FIELD_SECRET					= "secret";
	public static final String	REST_FIELD_ROLE						= "role";
	public static final String	REST_FIELD_INTERVENTION_PATTERN		= "intervention-pattern";
	public static final String	REST_FIELD_INTERVENTION_PASSWORD	= "intervention-password";

	public static final String	STATUS_ANSWERED_BY_USER				= "ANSWERED_BY_USER";
	public static final String	STATUS_NOT_ANSWERED_BY_USER			= "NOT_ANSWERED_BY_USER";
	public static final String	STATUS_SENT_BY_SERVER				= "SENT_BY_SERVER";
	public static final String	STATUS_SENT_BY_USER					= "SENT_BY_USER";
	public static final String	TYPE_PLAIN							= "PLAIN";
	public static final String	TYPE_INTENTION						= "INTENTION";
	public static final String	TYPE_COMMAND						= "COMMAND";

	public static final String	RPC_REGISTER						= "register";
	public static final String	RPC_REST_TOKEN						= "rest-token";
	public static final String	RPC_PUSH_TOKEN						= "push-token";
	public static final String	RPC_USER_MESSAGE					= "user-message";
	public static final String	RPC_USER_INTENTION					= "user-intention";
	public static final String	RPC_USER_VARIABLE					= "user-variable";
	public static final String	RPC_MESSAGE_DIFF					= "message-diff";

	public static final String	PATH_MESSAGES						= "messages/";
	public static final String	PATH_MESSAGE_UPDATE					= "message-update/";
	public static final String	PATH_LIST							= "list/";

	public static final String	NICKNAME							= "nickname";
	public static final String	PARTICIPANT							= "participant";
	public static final String	USER								= "user";
	public static final String	SECRET								= "secret";
	public static final String	ROLE								= "role";
	public static final String	INTERVENTION_PATTERN				= "intervention-pattern";
	public static final String	INTERVENTION_PASSWORD				= "intervention-password";
	public static final String	ID									= "id";
	public static final String	TYPE								= "type";
	public static final String	OPTIONS								= "options";
	public static final String	STATUS								= "status";
	public static final String	CONTENT								= "content";
	public static final String	PLATFORM							= "platform";
	public static final String	TOKEN								= "token";
	public static final String	VARIABLE							= "variable";
	public static final String	VALUE								= "value";
	public static final String	SERVER_TIMESTAMP					= "server-timestamp";
	public static final String	RELATED_MESSAGE_ID					= "related-message-id";
	public static final String	USER_MESSAGE						= "user-message";
	public static final String	USER_INTENTION						= "user-intention";
	public static final String	USER_CONTENT						= "user-content";
	public static final String	USER_TEXT							= "user-text";
	public static final String	USER_TIMESTAMP						= "user-timestamp";
	public static final String	CLIENT_ID							= "client-id";
	public static final String	LAST_MODIFIED						= "last-modified";
	public static final String	STICKY								= "sticky";
	public static final String	CONTAINS_SURVEY						= "contains-survey";
	public static final String	EXPECTS_ANSWER						= "expects-answer";
	public static final String	MESSAGE_TIMESTAMP					= "message-timestamp";
	public static final String	SERVER_MESSAGE						= "server-message";
	public static final String	CONTAINS_MEDIA						= "contains-media";
	public static final String	MEDIA_NAME							= "media-name";
	public static final String	MEDIA_TYPE							= "media-type";
	public static final String	ANSWER_FORMAT						= "answer-format";

	public static final String	PLATFORM_IOS						= "ios";
	public static final String	PLATFORM_ANDROID					= "android";
}
