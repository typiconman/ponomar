package net.ponomar.readings.utility;

import java.util.LinkedHashMap;
import java.util.ArrayList;

import net.ponomar.utility.Constants;
 
 
 

/***************************************************************
 ReadingUtility.java ::
 This module consists of extracted methods from Main.Java and readings-related classes.
 **************************************************************/

public final class ReadingUtility {
	
    static final String READINGS_KEY = Constants.READINGS;

	public static void processMenaionPaschalReadings(LinkedHashMap[] menaionReadings, LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> combinedReadings) {
		for (LinkedHashMap menaionReading : menaionReadings) {
			LinkedHashMap reading = (LinkedHashMap) menaionReading.get(READINGS_KEY);
			LinkedHashMap readings = (LinkedHashMap) reading.get(READINGS_KEY);
			readings.forEach((k,v) -> iterationOverReadings(combinedReadings, reading, readings, k.toString()));

		}
	}

	private static void iterationOverReadings(LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> combinedReadings,
			LinkedHashMap reading, LinkedHashMap readings, String key) {
		if (combinedReadings.get(key) != null) {
			combinedReadings.put(key, combineWithExistingReading(combinedReadings, reading, readings, key));
		} else {
			combinedReadings.put(key, readingDoesNotExist(reading, readings, key));
		}
	}
	
	private static LinkedHashMap<String, ArrayList<String>> combineWithExistingReading(LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> combinedReadings, LinkedHashMap reading,
																			LinkedHashMap readings, String element1) {
		LinkedHashMap<String, ArrayList<String>> temp = combinedReadings.get(element1);
		ArrayList readings2 = temp.get(READINGS_KEY);
		ArrayList rank = temp.get("Rank");
		ArrayList tag = temp.get("Tag");
		return putReadings(reading, readings, element1, temp, readings2, rank, tag);
	}
	
	private static LinkedHashMap<String, ArrayList<String>> readingDoesNotExist(LinkedHashMap reading, LinkedHashMap readings, String element1) {
		return putReadings(reading, readings, element1, new LinkedHashMap<>(), new ArrayList(), new ArrayList(), new ArrayList());
	}
	
	private static LinkedHashMap<String, ArrayList<String>> putReadings(LinkedHashMap reading, LinkedHashMap readings, String element1,
			LinkedHashMap<String, ArrayList<String>> temp, ArrayList readings2, ArrayList rank, ArrayList tag) {
		readings2.add(readings.get(element1));
		rank.add(reading.get("Rank"));
		tag.add(reading.get("Name"));
		temp.put(READINGS_KEY, readings2);
		temp.put("Rank", rank);
		temp.put("Tag", tag);
		return temp;
	}
}
