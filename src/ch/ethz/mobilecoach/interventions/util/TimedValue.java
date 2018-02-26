package ch.ethz.mobilecoach.interventions.util;

import lombok.Getter;

public class TimedValue {
	@Getter
	public final String value;
	@Getter
	public final long timestamp;
	
	public TimedValue(String value, long timestamp){
		this.value = value;
		this.timestamp = timestamp;
	}
	
	public boolean containsDoubleValue(){
		if (value == null || value.isEmpty()){
			return false;
		}
		
		try {
			Double.parseDouble(value);
			return true;
		} catch (Exception e){
			return false;
		}
	}
}
