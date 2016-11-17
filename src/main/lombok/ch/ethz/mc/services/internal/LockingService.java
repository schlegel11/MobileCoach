package ch.ethz.mc.services.internal;

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
import java.util.ArrayList;
import java.util.Hashtable;

import lombok.Synchronized;
import lombok.val;
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
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.ui.UISession;

/**
 * @author Andreas Filler
 */
@Log4j2
public class LockingService {
	private final Hashtable<UISession, ObjectId>	lockedInterventionsByUISession;

	private static LockingService					instance	= null;

	private LockingService() {
		lockedInterventionsByUISession = new Hashtable<UISession, ObjectId>();
	}

	public static LockingService start() throws Exception {
		log.info("Starting service...");
		if (instance == null) {
			instance = new LockingService();
		}
		log.info("Started.");
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	/*
	 * Class methods
	 */

	/**
	 * Checks and sets lock for the given {@link UISession} and {@link ObjectId}
	 *
	 * @param uiSession
	 * @param objectId
	 * @return Returns if lock was successful
	 */
	@Synchronized
	public boolean checkAndSetLockForUISession(final UISession uiSession,
			final ObjectId objectId) {
		for (final ObjectId objectIdToCompare : lockedInterventionsByUISession
				.values()) {
			if (objectIdToCompare != null && objectId.equals(objectIdToCompare)) {
				log.debug(
						"Can't lock intervention {} because it's already locked by {}",
						objectId, uiSession.getCurrentAuthorId());
				return false;
			}
		}

		lockedInterventionsByUISession.put(uiSession, objectId);
		log.debug("Locked {} for UI session of {} (currently {} locks)",
				objectId, uiSession.getCurrentAuthorId(),
				lockedInterventionsByUISession.size());
		return true;
	}

	/**
	 * Releases the lock of the given {@link UISession}
	 *
	 * @param uiSession
	 */
	@Synchronized
	public void releaseLockOfUISession(final UISession uiSession) {
		log.debug("Releasing lock of UI session");
		lockedInterventionsByUISession.remove(uiSession);
		log.debug("{} locks remaining", lockedInterventionsByUISession.size());
	}

	/**
	 * Releases all locks of the given session
	 *
	 * @param sessionId
	 */
	@Synchronized
	public void releaseAllLocksOfSession(final String sessionId) {
		log.debug("Releasing locks of session {}", sessionId);

		val uiSessionsToReleaseLock = new ArrayList<UISession>();

		for (val uiSessionToCheck : lockedInterventionsByUISession.keySet()) {
			if (uiSessionToCheck.getSessionId().equals(sessionId)) {
				uiSessionsToReleaseLock.add(uiSessionToCheck);
			}
		}

		for (val uiSessionToReleaseLock : uiSessionsToReleaseLock) {
			releaseLockOfUISession(uiSessionToReleaseLock);
		}
	}
}
