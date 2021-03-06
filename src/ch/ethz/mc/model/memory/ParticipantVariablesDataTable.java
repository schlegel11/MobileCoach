package ch.ethz.mc.model.memory;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Required for in memory organization of variables for the CSV export
 *
 * @author Andreas Filler
 */
@Log4j2
public class ParticipantVariablesDataTable {
	private final List<ObjectId>													participantIds;
	private final Hashtable<ObjectId, Participant>									participants;

	private final TreeSet<String>													statisticValuesHeaders;
	private final TreeSet<String>													variablesHeaders;

	private final Hashtable<ObjectId, Hashtable<String, String>>					statisticValuesOfParticipants;
	private final Hashtable<ObjectId, Hashtable<String, AbstractVariableWithValue>>	variablesWithValuesOfParticipants;

	public ParticipantVariablesDataTable() {
		log.debug("Creating new participant variables data table for export");
		participantIds = new ArrayList<ObjectId>();
		participants = new Hashtable<ObjectId, Participant>();

		statisticValuesHeaders = new TreeSet<String>();
		variablesHeaders = new TreeSet<String>();

		statisticValuesOfParticipants = new Hashtable<ObjectId, Hashtable<String, String>>();
		variablesWithValuesOfParticipants = new Hashtable<ObjectId, Hashtable<String, AbstractVariableWithValue>>();
	}

	/**
	 * Represents one column in the table
	 *
	 * @author Andreas Filler
	 */
	public class DataEntry {
		private final List<String> values;

		public DataEntry() {
			values = new ArrayList<String>();
		}

		public String[] toStringArray() {
			return values.toArray(new String[0]);
		}

		private String clean(final String value) {
			return value.replace("\n", "").replace("\r", "");
		}

		public void add(final String value) {
			if (value == null) {
				values.add("");
			} else {
				values.add(clean(value));
			}
		}

		public void addAll(final Set<String> values) {
			for (val value : values) {
				add(value);
			}
		}
	}

	public List<DataEntry> getEntries() {
		val entries = new ArrayList<DataEntry>();

		for (val participantId : participantIds) {
			val entry = new DataEntry();

			val participant = participants.get(participantId);

			val statisticValuesOfParticipant = statisticValuesOfParticipants
					.get(participantId);
			val variablesWithValuesOfParticipant = variablesWithValuesOfParticipants
					.get(participantId);

			entry.add(participantId.toString());

			entry.add(participant.getNickname());
			entry.add(participant.getLanguage().getDisplayLanguage());
			entry.add(participant.getGroup());
			entry.add(participant.getOrganization());
			entry.add(participant.getOrganizationUnit());

			for (val staticValuesHeader : statisticValuesHeaders) {
				entry.add(statisticValuesOfParticipant.get(staticValuesHeader));
			}
			for (val variableHeader : variablesHeaders) {
				val variable = variablesWithValuesOfParticipant
						.get(variableHeader);
				if (variable == null) {
					entry.add(Messages.getAdminString(
							AdminMessageStrings.UI_MODEL__NOT_SET));
				} else {
					entry.add(variable.getValue());
				}
			}

			entries.add(entry);
		}

		return entries;
	}

	public DataEntry getHeaders() {
		final val dataEntry = new DataEntry();

		dataEntry.add(Messages.getAdminString(
				AdminMessageStrings.UI_COLUMNS__PARTICIPANT_ID));
		dataEntry.add(Messages.getAdminString(
				AdminMessageStrings.UI_COLUMNS__PARTICIPANT_NAME));
		dataEntry.add(Messages
				.getAdminString(AdminMessageStrings.UI_COLUMNS__LANGUAGE));
		dataEntry.add(
				Messages.getAdminString(AdminMessageStrings.UI_COLUMNS__GROUP));
		dataEntry.add(Messages
				.getAdminString(AdminMessageStrings.UI_COLUMNS__ORGANIZATION));
		dataEntry.add(Messages.getAdminString(
				AdminMessageStrings.UI_COLUMNS__ORGANIZATION_UNIT));

		dataEntry.addAll(statisticValuesHeaders);
		dataEntry.addAll(variablesHeaders);

		return dataEntry;
	}

	public void addEntry(final ObjectId participantId,
			final Participant participant,
			final Hashtable<String, String> statisticValuesOfParticipant,
			final Hashtable<String, AbstractVariableWithValue> variablesWithValuesOfParticipant) {
		participantIds.add(participantId);
		participants.put(participantId, participant);

		statisticValuesOfParticipants.put(participantId,
				statisticValuesOfParticipant);
		variablesWithValuesOfParticipants.put(participantId,
				variablesWithValuesOfParticipant);

		statisticValuesHeaders.addAll(statisticValuesOfParticipant.keySet());
		variablesHeaders.addAll(variablesWithValuesOfParticipant.keySet());
	}
}
