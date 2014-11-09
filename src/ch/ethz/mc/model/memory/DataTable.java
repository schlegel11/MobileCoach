package ch.ethz.mc.model.memory;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.services.types.SystemVariables;

/**
 * Required for in memory organization of variables for the CSV export
 * 
 * @author Andreas Filler
 */
@Log4j2
public class DataTable {
	private final List<ObjectId>													participants;
	private final TreeSet<String>													statisticValuesHeaders;
	private final TreeSet<String>													variablesHeaders;

	private final Hashtable<ObjectId, Hashtable<String, String>>					statisticValuesOfParticipants;
	private final Hashtable<ObjectId, Hashtable<String, AbstractVariableWithValue>>	variablesWithValuesOfParticipants;

	public DataTable() {
		participants = new ArrayList<ObjectId>();
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
		private final List<String>	values;

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

		public void addAll(final TreeSet<String> values) {
			for (val value : values) {
				add(value);
			}
		}
	}

	public List<DataEntry> getEntries() {
		val entries = new ArrayList<DataEntry>();

		for (val participant : participants) {
			val entry = new DataEntry();

			val statisticValuesOfParticipant = statisticValuesOfParticipants
					.get(participant);
			val variablesWithValuesOfParticipant = variablesWithValuesOfParticipants
					.get(participant);

			entry.add(participant.toString());

			val participantName = variablesWithValuesOfParticipant
					.get(SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES.participantName
							.toVariableName());
			if (participantName == null) {
				entry.add(Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET));
			} else {
				entry.add(participantName.getValue());
			}

			for (val staticValuesHeader : statisticValuesHeaders) {
				entry.add(statisticValuesOfParticipant.get(staticValuesHeader));
			}
			for (val variableHeader : variablesHeaders) {
				val variable = variablesWithValuesOfParticipant
						.get(variableHeader);
				if (variable == null) {
					entry.add(Messages
							.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET));
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

		dataEntry
				.add(Messages
						.getAdminString(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_ID));
		dataEntry
				.add(Messages
						.getAdminString(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_NAME));

		dataEntry.addAll(statisticValuesHeaders);
		dataEntry.addAll(variablesHeaders);

		return dataEntry;
	}

	public void addEntry(
			final ObjectId participantId,
			final Hashtable<String, String> statisticValuesOfParticipant,
			final Hashtable<String, AbstractVariableWithValue> variablesWithValuesOfParticipant) {
		participants.add(participantId);

		statisticValuesOfParticipants.put(participantId,
				statisticValuesOfParticipant);
		variablesWithValuesOfParticipants.put(participantId,
				variablesWithValuesOfParticipant);

		statisticValuesHeaders.addAll(statisticValuesOfParticipant.keySet());
		variablesHeaders.addAll(variablesWithValuesOfParticipant.keySet());
	}
}
