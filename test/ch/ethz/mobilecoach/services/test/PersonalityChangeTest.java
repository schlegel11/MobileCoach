package ch.ethz.mobilecoach.services.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.ethz.mobilecoach.interventions.PersonalityChange;
import ch.ethz.mobilecoach.interventions.util.TimedValue;


public class PersonalityChangeTest {

    @Test
    public void testTrafficLightCalculation() {
    	long now = System.currentTimeMillis();
    	long DAY = 3600 * 24 * 1000;
    	
    	// try with no values
    	List<TimedValue> values = new ArrayList<TimedValue>();
    	assertEquals(PersonalityChange.TL_NOT_ENOUGH_BASELINE_VALUES, PersonalityChange.calculateTrafficLight(values));
    	
    	// try with 4 values and 2 invalid ones
    	
    	values.add(new TimedValue("", now - DAY * 8));
    	values.add(new TimedValue("A", now - DAY * 8));
    	
    	values.add(new TimedValue("1", now - DAY * 8));
    	values.add(new TimedValue("1", now - DAY * 9));
    	values.add(new TimedValue("1", now - DAY * 10));
    	values.add(new TimedValue("1", now - DAY * 11));
    	assertEquals(PersonalityChange.TL_NOT_ENOUGH_BASELINE_VALUES, PersonalityChange.calculateTrafficLight(values));
    	
    	// try with 5 values: baseline is okay now
    	values.add(new TimedValue("1", now - DAY * 12));
    	assertEquals(PersonalityChange.TL_NOT_ENOUGH_RECENT_VALUES, PersonalityChange.calculateTrafficLight(values));
    	
    	// comparison now works
    	values.add(new TimedValue("1", now - DAY * 1));
    	assertEquals(PersonalityChange.TL_YELLOW, PersonalityChange.calculateTrafficLight(values));
    	
    	values.add(new TimedValue("2", now - DAY * 2));
    	assertEquals(PersonalityChange.TL_GREEN, PersonalityChange.calculateTrafficLight(values));
    	
    	values.add(new TimedValue("-2.0", now - DAY * 3));
    	assertEquals(PersonalityChange.TL_RED, PersonalityChange.calculateTrafficLight(values));
    }

}