package ch.ethz.mobilecoach.interventions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.bson.types.ObjectId;

import ch.ethz.mc.model.persistent.ParticipantVariableWithValue;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.services.internal.VariablesManagerService.InvalidVariableNameException;
import ch.ethz.mc.services.internal.VariablesManagerService.WriteProtectedVariableException;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mobilecoach.interventions.util.TimedValue;
import lombok.val;

public class PersonalityChange extends AbstractIntervention {
	
	public static final String TL_NOT_ENOUGH_BASELINE_VALUES = "NOT_ENOUGH_BASELINE_VALUES";
	public static final String TL_NOT_ENOUGH_RECENT_VALUES = "NOT_ENOUGH_RECENT_VALUES";
	public static final String TL_GREEN = "GREEN";
	public static final String TL_YELLOW = "YELLOW";
	public static final String TL_RED = "RED";

	public static final String COACH_NAME_VARIABLE = "$participantCoach";
	public static final String SUPPORT_NAME = "PersonalityCoach Team";	
	private static final long DAY = 24 * 3600 * 1000;
	
	public PersonalityChange(DatabaseManagerService db,
			VariablesManagerService vars) {
		super(db, vars);
	}
	
	private Iterable<ParticipantVariableWithValue> getValuesSince(long timestamp, String variableName, ObjectId participantId){
		String query = "{participant: #, name: #, timestamp: {$gte: #}}";
		return db.findSortedModelObjects(ParticipantVariableWithValue.class, query, "{timestamp: -1}", participantId, variableName, timestamp);
	}

	@Override
	public Object getDashboard(ObjectId participantId) {
		
		String opportunitiesVariable = "$impl_int_opportunities";
		String implementedVariable = "$impl_int_implemented";
		String traitVariable = "$trait_daily";
		
		Map<String, Object> result = new HashMap<>();
		long now = InternalDateTime.currentTimeMillis();		
		
		result.put("pam", new String[0]);
		
		// trait traffic light
		val traitValues = new ArrayList<TimedValue>();
		for (val v: getValuesSince(0L, traitVariable, participantId)){
			traitValues.add(new TimedValue(v.getValue(), v.getTimestamp()));
		}
		result.put("trait_values", traitValues); // for debugging
		String trafficLightValue = calculateTrafficLight(traitValues);
		result.put("trait_light", trafficLightValue);
		
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
		
		// log traffic light status to variable
		try {
			vars.writeVariableValueOfParticipant(participantId, "$dashboard_trafficlight", trafficLightValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static String calculateTrafficLight(List<TimedValue> traitValues){
		final int MIN_BASELINE_VALUES = 5;
		
		traitValues.sort(new Comparator<TimedValue>(){

			@Override
			public int compare(TimedValue o1, TimedValue o2) {
				Comparator<Double> natural = Comparator.<Double>naturalOrder();
				return natural.compare((double) o1.timestamp, (double) o2.timestamp); // this is completely safe because double has 52 bits base and current timestamp only uses 41 bits.
			}
			
		});
		
		Supplier<Stream<TimedValue>> baselineValues = null;
		boolean baselineComplete = false;
		
		// find baseline: min 5 data points within one week
		long WEEK = DAY * 7;
		for (TimedValue tv : traitValues){
			long t = tv.timestamp;
			baselineValues = () -> traitValues.stream().filter((v) -> v.timestamp >= t && v.timestamp < t + WEEK && v.containsDoubleValue());
			if (baselineValues.get().count() >= MIN_BASELINE_VALUES){
				baselineComplete = true;
				break;
			}
		}
		if (!baselineComplete){
			return TL_NOT_ENOUGH_BASELINE_VALUES;
		}
		
		// find recent values: at least one value within the last week
		long now = System.currentTimeMillis();
		Supplier<Stream<TimedValue>> recentValues = () -> traitValues.stream().filter((v) -> v.timestamp >= now - WEEK && v.timestamp <= now && v.containsDoubleValue());
		
		if (recentValues.get().count() < 1){
			return TL_NOT_ENOUGH_RECENT_VALUES;
		}
		
		// calculate baseline mean and variance
		DescriptiveStatistics ds = new DescriptiveStatistics();
		baselineValues.get().forEach((v) -> ds.addValue(Double.parseDouble(v.value)));
		
		double std = ds.getStandardDeviation();
		double mean = ds.getMean();
		
		// calculate mean of the recent values
		DescriptiveStatistics recent = new DescriptiveStatistics();
		recentValues.get().forEach((v) -> recent.addValue(Double.parseDouble(v.value)));
		
		double recentMean = recent.getMean();
		
		if (recentMean > mean + std * 0.5){
			return TL_GREEN;
		} else if (recentMean < mean - std * 0.5) {
			return TL_RED;
		} else {
			return TL_YELLOW;
		}
	}
}
