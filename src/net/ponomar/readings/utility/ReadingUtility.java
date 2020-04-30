package net.ponomar.readings.utility;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Vector;

import net.ponomar.utility.Constants;
 
 
 

/***************************************************************
 ReadingUtility.java ::
 This module consists of extracted methods from Main.Java and readings-related classes.
 **************************************************************/

public final class ReadingUtility {
	
    static final String READINGS_KEY = Constants.READINGS;

	public static void processMenaionPaschalReadings(LinkedHashMap[] menaionReadings, LinkedHashMap<String, LinkedHashMap<String, Vector<String>>> combinedReadings) {
		for (LinkedHashMap menaionReading : menaionReadings) {
			LinkedHashMap reading = (LinkedHashMap) menaionReading.get(READINGS_KEY);
			LinkedHashMap readings = (LinkedHashMap) reading.get(READINGS_KEY);
			for (Enumeration<String> e = Collections.enumeration(readings.keySet()); e.hasMoreElements(); ) {
				String element1 = e.nextElement();
				if (combinedReadings.get(element1) != null) {
					combinedReadings.put(element1, combineWithExistingReading(combinedReadings, reading, readings, element1));
				} else {
					combinedReadings.put(element1, readingDoesNotExist(reading, readings, element1));
				}
			}
		}
	}
	
	private static LinkedHashMap<String, Vector<String>> combineWithExistingReading(LinkedHashMap<String, LinkedHashMap<String, Vector<String>>> combinedReadings, LinkedHashMap reading,
																			LinkedHashMap readings, String element1) {
		LinkedHashMap<String, Vector<String>> temp = combinedReadings.get(element1);
		Vector readings2 = temp.get(READINGS_KEY);
		Vector rank = temp.get("Rank");
		Vector tag = temp.get("Tag");
		return putReadings(reading, readings, element1, temp, readings2, rank, tag);
	}
	
	private static LinkedHashMap<String, Vector<String>> readingDoesNotExist(LinkedHashMap reading, LinkedHashMap readings, String element1) {
		return putReadings(reading, readings, element1, new LinkedHashMap<>(), new Vector(), new Vector(), new Vector());
	}
	
	private static LinkedHashMap<String, Vector<String>> putReadings(LinkedHashMap reading, LinkedHashMap readings, String element1,
			LinkedHashMap<String, Vector<String>> temp, Vector readings2, Vector rank, Vector tag) {
		readings2.add(readings.get(element1));
		rank.add(reading.get("Rank"));
		tag.add(reading.get("Name"));
		temp.put(READINGS_KEY, readings2);
		temp.put("Rank", rank);
		temp.put("Tag", tag);
		return temp;
	}
}
