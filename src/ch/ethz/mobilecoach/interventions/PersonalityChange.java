package ch.ethz.mobilecoach.interventions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.ParticipantVariableWithValue;
import ch.ethz.mc.model.rest.CollectionOfVariables;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mc.tools.StringValidator;
import lombok.Getter;
import lombok.val;

public class PersonalityChange extends AbstractIntervention {

	public static final String COACH_NAME_VARIABLE = "$participantCoach";
	public static final String SUPPORT_NAME = "PersonalityCoach Team";
	
	public PersonalityChange(DatabaseManagerService db,
			VariablesManagerService vars) {
		super(db, vars);
	}
	
	private Iterable<ParticipantVariableWithValue> getValuesSince(long timestamp, String variableName, ObjectId participantId){
		String query = "{participantId: #, name: #, timestamp: {$gte: #}}";
		return db.findSortedModelObjects(ParticipantVariableWithValue.class, query, "{timestamp: -1}", participantId, variableName, timestamp);
	}

	@Override
	public Object getDashboard(ObjectId participantId) {
		
		String pamVariable = "$pam_affect";
		String opportunitiesVariable = "$impl_int_opportunities";
		String implementedVariable = "$impl_int_implemented";
		final int MAX_PAM_VALUES = 10;
		Map<String, Object> result = new HashMap<>();
		long now = InternalDateTime.currentTimeMillis();
		final long DAY = 24 * 3600 * 1000;

		// PAM values
		val iterator = getValuesSince(0, pamVariable, participantId).iterator();
		val pamValues = new ArrayList<String>();
		while (iterator.hasNext() && pamValues.size() < MAX_PAM_VALUES){
			pamValues.add(iterator.next().getValue());
		}
		result.put("pam", pamValues);
		
		// opportunities: past 15 days
		val opportunityValues = new ArrayList<TimedValue>();
		for (val v: getValuesSince(now - 15 * DAY, opportunitiesVariable, participantId)){
			opportunityValues.add(new TimedValue(v.getValue(), v.getTimestamp()));
		}
		result.put("opportunities", opportunityValues);
		
		// implemented: past 15 days
		val implementedValues = new ArrayList<TimedValue>();
		for (val v: getValuesSince(now - 15 * DAY, implementedVariable, participantId)){
			implementedValues.add(new TimedValue(v.getValue(), v.getTimestamp()));
		}
		result.put("implemented", implementedValues);		
		
		return result;
	}
	
	public class TimedValue {
		@Getter
		private String value;
		@Getter
		private long timestamp;
		
		public TimedValue(String value, long timestamp){
			this.value = value;
			this.timestamp = timestamp;
		}		
	}
}
