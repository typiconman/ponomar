package net.ponomar.readings.utility;

import java.util.Enumeration;
import java.util.Vector;

import net.ponomar.utility.Constants;
import net.ponomar.utility.IOrderedHashtable;
import net.ponomar.utility.OrderedHashtable;
import net.ponomar.utility.OrderedHashtable;

/***************************************************************
 ReadingUtility.java ::
 This module consists of extracted methods from Main.Java and readings-related classes.
 **************************************************************/

public final class ReadingUtility {
	
    static final String READINGS_KEY = Constants.READINGS;

	public static void processMenaionPaschalReadings(IOrderedHashtable[] menaionReadings, IOrderedHashtable combinedReadings) {
		for (IOrderedHashtable menaionReading : menaionReadings) {
			IOrderedHashtable reading = (IOrderedHashtable) menaionReading.get(READINGS_KEY);
			IOrderedHashtable readings = (IOrderedHashtable) reading.get(READINGS_KEY);
			for (Enumeration<String> e = readings.enumerateKeys(); e.hasMoreElements(); ) {
				String element1 = e.nextElement().toString();
				if (combinedReadings.get(element1) != null) {
					combinedReadings.put(element1, combineWithExistingReading(combinedReadings, reading, readings, element1));
				} else {
					combinedReadings.put(element1, readingDoesNotExist(reading, readings, element1));
				}
			}
		}
	}
	
	private static IOrderedHashtable combineWithExistingReading(IOrderedHashtable combinedReadings, IOrderedHashtable reading,
			IOrderedHashtable readings, String element1) {
		IOrderedHashtable temp = (IOrderedHashtable) combinedReadings.get(element1);
		Vector readings2 = (Vector) temp.get(READINGS_KEY);
		Vector rank = (Vector) temp.get("Rank");
		Vector tag = (Vector) temp.get("Tag");
		return putReadings(reading, readings, element1, temp, readings2, rank, tag);
	}
	
	private static IOrderedHashtable readingDoesNotExist(IOrderedHashtable reading, IOrderedHashtable readings, String element1) {
		return putReadings(reading, readings, element1, new OrderedHashtable(), new Vector(), new Vector(), new Vector());
	}
	
	private static IOrderedHashtable putReadings(IOrderedHashtable reading, IOrderedHashtable readings, String element1,
			IOrderedHashtable temp, Vector readings2, Vector rank, Vector tag) {
		readings2.add(readings.get(element1));
		rank.add(reading.get("Rank"));
		tag.add(reading.get("Name"));
		temp.put(READINGS_KEY, readings2);
		temp.put("Rank", rank);
		temp.put("Tag", tag);
		return temp;
	}
}
