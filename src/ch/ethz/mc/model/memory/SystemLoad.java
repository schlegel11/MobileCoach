package ch.ethz.mc.model.memory;

/* ##LICENSE## */
import java.util.ArrayList;
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
	public ArrayList<String> getLoggedInUsers() {
		val countInfos = new ArrayList<String>();
		val iterator = loggedInUsers.keySet().iterator();

		while (iterator.hasNext()) {
			val key = iterator.next();
			countInfos.add(key + ": " + loggedInUsers.get(key));
		}

		return countInfos;
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
			log.info("Logged in users:");
			for (val countInfo : getLoggedInUsers()) {
				log.info(" * {}", countInfo);
			}
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
