package ch.ethz.mc.model.memory;

import java.util.Hashtable;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Enables an insight into the current system load
 *
 * @author Andreas Filler
 */
@Log4j2
public class SystemLoad {
	private static SystemLoad					instance	= null;

	private final Hashtable<String, Integer>	loggedInUsers;

	@Synchronized
	public static SystemLoad getInstance() {
		if (instance == null) {
			instance = new SystemLoad();
		}

		return instance;
	}

	private SystemLoad() {
		loggedInUsers = new Hashtable<>();
		messagingPerformedForParticipants = 0;

		incomingMessageWorkerRequiredMillis = 0;
		outgoingMessageWorkerRequiredMillis = 0;
		statisticsCreationRequiredMillis = 0;
		finishingUnfinishedScreeningSurveysRequiredMillis = 0;
		performContinuousMessagingRequiredMillis = 0;
	}

	@Synchronized
	public int getLoggedInUsers() {
		int count = 0;

		for (val key : loggedInUsers.keySet()) {
			count += loggedInUsers.get(key);
		}

		return count;
	}

	@Synchronized
	public void setLoggedInUsers(final String service, final int count) {
		loggedInUsers.put(service, count);
	}

	@Getter
	@Setter
	long	messagingPerformedForParticipants;

	@Getter
	@Setter
	long	incomingMessageWorkerRequiredMillis;

	@Getter
	@Setter
	long	outgoingMessageWorkerRequiredMillis;

	@Getter
	@Setter
	long	statisticsCreationRequiredMillis;

	@Getter
	@Setter
	long	finishingUnfinishedScreeningSurveysRequiredMillis;

	@Getter
	@Setter
	long	performContinuousMessagingRequiredMillis;

	@Getter
	@Setter
	double	performContinuousMessagingRequiredMillisPerParticipant;

	public void log() {
		synchronized (log) {
			log.info(
					"--------------------------------------------------------------------------------");
			log.info(
					"Logged in users:                                              {}",
					getLoggedInUsers());
			log.info(
					"Messaging performed for participants:                         {}",
					getMessagingPerformedForParticipants());
			log.info(
					"Incoming Message Worker required millis:                      {}",
					getIncomingMessageWorkerRequiredMillis());
			log.info(
					"Outgoing Message Worker required millis:                      {}",
					getOutgoingMessageWorkerRequiredMillis());
			log.info(
					"Statistics creation required millis:                          {}",
					getStatisticsCreationRequiredMillis());
			log.info(
					"Finish unfinished screening surveys required millis:          {}",
					getFinishingUnfinishedScreeningSurveysRequiredMillis());
			log.info(
					"Perform continuous messaging required millis:                 {}",
					getPerformContinuousMessagingRequiredMillis());
			log.info(
					"Perform continuous messaging required millis per participant: {}",
					getPerformContinuousMessagingRequiredMillisPerParticipant());
			log.info(
					"--------------------------------------------------------------------------------");
		}
	}
}
