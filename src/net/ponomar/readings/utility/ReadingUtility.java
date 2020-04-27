package net.ponomar.readings.utility;

import java.util.Enumeration;
import java.util.Vector;

import net.ponomar.utility.OrderedHashtable;

/***************************************************************
 ReadingUtility.java ::
 This module consists of extracted methods from Main.Java and readings-related classes.
 **************************************************************/

public final class ReadingUtility {
	
    static final String READINGS_KEY = "Readings";

	public static void processMenaionPaschalReadings(OrderedHashtable[] menaionReadings, OrderedHashtable combinedReadings) {
		for (int k = 0; k < menaionReadings.length; k++) {
	        OrderedHashtable reading = (OrderedHashtable) menaionReadings[k].get(READINGS_KEY);
	        OrderedHashtable readings = (OrderedHashtable) reading.get(READINGS_KEY);
	        for (Enumeration e = readings.enumerateKeys(); e.hasMoreElements();) {
	            String element1 = e.nextElement().toString();
	            if (combinedReadings.get(element1) != null) {
	                combinedReadings.put(element1, combineWithExistingReading(combinedReadings, reading, readings, element1));
	            } else {
	                combinedReadings.put(element1, readingDoesNotExist(reading, readings, element1));
	            }
	        }
	    }
	}
	
	private static OrderedHashtable combineWithExistingReading(OrderedHashtable combinedReadings, OrderedHashtable reading,
			OrderedHashtable readings, String element1) {
		OrderedHashtable temp = (OrderedHashtable) combinedReadings.get(element1);
		Vector readings2 = (Vector) temp.get(READINGS_KEY);
		Vector rank = (Vector) temp.get("Rank");
		Vector tag = (Vector) temp.get("Tag");
		return putReadings(reading, readings, element1, temp, readings2, rank, tag);
	}
	
	private static OrderedHashtable readingDoesNotExist(OrderedHashtable reading, OrderedHashtable readings, String element1) {
		return putReadings(reading, readings, element1, new OrderedHashtable(), new Vector(), new Vector(), new Vector());
	}
	
	private static OrderedHashtable putReadings(OrderedHashtable reading, OrderedHashtable readings, String element1,
			OrderedHashtable temp, Vector readings2, Vector rank, Vector tag) {
		readings2.add(readings.get(element1));
		rank.add(reading.get("Rank"));
		tag.add(reading.get("Name"));
		temp.put(READINGS_KEY, readings2);
		temp.put("Rank", rank);
		temp.put("Tag", tag);
		return temp;
	}
}
