package net.ponomar.calendar;

import java.util.LinkedHashMap;

import net.ponomar.utility.StringOp;

//Copyright 2012 Yuri Shardt

/**
 * This creates a calendar object that will store the relate information about
 * manipulating and dealing with different calendars and standards.
 * <p>
 * It uses the JDate object to store the Julian Date
 * 
 * @author Yuri Shardt
 **/
public class PCalendar implements Cloneable {

	// CONSTANTS
	private double difference; // Stores the associated difference in days between Julian and Gregorian
								// calendars
	private String type; // Stores which calendar I am dealing with. At present only Gregorian and
							// Julian.
	private JDate date; // Stores the date associated with the given calendar.
	public static final String JULIAN = "julian"; // Fixes the spelling of the Julian option
	public static final String GREGORIAN = "gregorian"; // Fixes the spelling of the Gregorian option
	private StringOp analyse = new StringOp();

	public PCalendar(JDate date1, String calendar, LinkedHashMap<String, Object> dayInfo) {
		date = date1;
		type = calendar;
		analyse.setDayInfo(dayInfo);
	}

	// Computes the Julian Day given the calendar type
	private double julianDay() {
		int y = date.getYear();
		int m = date.getMonth();
		int d = date.getDay();
		if (m < 3) {
			m = m + 12;
			y = y - 1;
		}
		double a = Math.floor(y / 100);
		double b = 0;
		if (type.equals(GREGORIAN) && (y > 1582 || y == 1582 && m > 10 || y == 1582 && m == 10 && d > 14)) {
			// Note if the user specifies the Gregorian calendar before its existence, it
			// will not be honoured.
			b = 2 - a + Math.floor(a / 4);
		}

		return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + d + b - 1524.5;
	}

	public double getJulianDay() {
		return julianDay();
	}

	public int getAM() {
		// returns the corresponding anno mundi given the date.
		difference = 0;
		if (type.equals(GREGORIAN)) {
			difference = getDiff();
		}
		PCalendar cutoff = new PCalendar(new JDate(9, 1, date.getYear()), JULIAN,
				(LinkedHashMap<String, Object>) analyse.getDayInfo().clone());
		double year = date.getYear();
		double am = 5508 - Math.floor(difference / 365) + year;
		if (julianDay() >= cutoff.julianDay()) {
			am = am + 1;
		}
		return (int) am;
	}

	public double getDiff() {
		PCalendar test = new PCalendar(new JDate(date.getMonth(), date.getDay(), date.getYear()), JULIAN,
				(LinkedHashMap<String, Object>) analyse.getDayInfo().clone());
		PCalendar test2 = new PCalendar(new JDate(date.getMonth(), date.getDay(), date.getYear()), GREGORIAN,
				(LinkedHashMap<String, Object>) analyse.getDayInfo().clone());
		return (test.julianDay() - test2.julianDay());
	}

	public Object clone() {
		return new PCalendar(date, type, (LinkedHashMap<String, Object>) analyse.getDayInfo().clone());
	}

	// RETURNS THE YEAR, MONTH, DAY IN THE JULIAN CALENDAR
	private int[] getJulianDate() {
		// We wish to obtain the Julian calendar parameters
		double julianDay = getJulianDay() + 0.5;
		int a = integerPart(julianDay);

		int b = a + 1524;
		int c = integerPart((b - 122.1) / 365.25);
		int d = integerPart((365.25 * c));
		int e = integerPart((b - d) / 30.6001);

		int[] values = new int[3];
		values[2] = b - d - integerPart(30.6001 * e);
		if (e < 14) {
			values[1] = e - 1;
		} else {
			values[1] = e - 13;
		}
		if (values[1] > 2) {
			values[0] = c - 4716;
		} else {
			values[0] = c - 4715;
		}

		return values;
	}

	// RETURNS THE YEAR, MONTH, DAY IN THE GREGORIAN CALENDAR
	private int[] getGregorianDate() {
		// We wish to obtain the Gregorian calendar parameters
		double julianDay = getJulianDay() + 0.5;
		int z = integerPart(julianDay);
		int a = z;
		// Again we will not honour Gregorian dates outside of the implementation period!
		if (z > 2299161) {
			int alpha = integerPart((z - 1867216.25) / 36524.25);
			a = z + 1 + alpha - integerPart(alpha / 4);
		}

		int b = a + 1524;
		int c = integerPart((b - 122.1) / 365.25);
		int d = integerPart((365.25 * c));
		int e = integerPart((b - d) / 30.6001);

		int[] values = new int[3];
		values[2] = b - d - integerPart(30.6001 * e);
		if (e < 14) {
			values[1] = e - 1;
		} else {
			values[1] = e - 13;
		}
		if (values[1] > 2) {
			values[0] = c - 4716;
		} else {
			values[0] = c - 4715;
		}

		return values;
	}

	private int integerPart(double number) {
		return (int) number - (int) (number % 1);
	}

	// RETURNS THE JULIAN CALENDAR YEAR
	public int getYearJ() {

		return getJulianDate()[0];
	}

	public int getMonthJ() {

		return getJulianDate()[1];
	}

	public int getDayJ() {

		return getJulianDate()[2];
	}

	// RETURNS THE GREGORIAN CALENDAR YEAR (if < 1582/10/15, returns Julian Calendar
	// Year)
	public int getYearG() {

		return getGregorianDate()[0];
	}

	public int getMonthG() {

		return getGregorianDate()[1];
	}

	public int getDayG() {

		return getGregorianDate()[2];
	}

	public static void main(String[] argz) {
		LinkedHashMap<String, Object> dayInfo = new LinkedHashMap<>();
		dayInfo.put("LS", "en/");
		PCalendar test = new PCalendar(new JDate(10, 4, 1957), GREGORIAN, dayInfo);
		System.out.println(test.julianDay());
		test = new PCalendar(new JDate(1, 27, 333), JULIAN, dayInfo);
		System.out.println(test.julianDay());
		test = new PCalendar(new JDate(1, 14, 2012), JULIAN, dayInfo);
		PCalendar test2 = new PCalendar(new JDate(1, 14, 2012), GREGORIAN, dayInfo);
		System.out.println(test.julianDay() - test2.julianDay());

		System.out.println("Anno Mundi: " + test.getAM());
		test = new PCalendar(new JDate(9, 14, 2012), JULIAN, dayInfo);
		System.out.println("Anno Mundi: " + test.getAM());
		test = new PCalendar(new JDate(9, 14, 458973), GREGORIAN, dayInfo);
		System.out.println("Anno Mundi: " + test.getAM());
	}

}
