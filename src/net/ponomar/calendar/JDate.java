package net.ponomar.calendar;

import java.util.*;

import net.ponomar.internationalization.LanguagePack;
import net.ponomar.utility.Constants;
 
 
import net.ponomar.utility.RuleBasedNumber;
import net.ponomar.utility.StringOp;

/*
 JDate.java is part of the Ponomar program.
 Copyright 2006, 2007 Aleksandr Andreev.
 aleksandr.andreev@gmail.com

 Ponomar is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 3
 of the License, or (at your option) any later version.

 While Ponomar is distributed in the hope that it will be useful,
 it comes with ABSOLUTELY NO WARRANTY, without even the implied warranties of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for details.
*/

/**
 * The purpose of this class is to provide an easy interface to manipulate
 * Julian dates
 * <p>
 * A JDate object is created by specifying the month, day, year. Internally, the
 * mm/dd/yyyy is converted to a Julian date (a long). Operations can be
 * performed easily with this Julian date. In the end, an mm/dd/yyyy can be
 * obtained back from the JDate object
 * 
 * @author Aleksandr Andreev
 */
public class JDate implements Comparable<JDate>, Cloneable
{
	private long mnJday;

	private final static int[] daysInMonth = new int[]
	{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

	private final static int[] daysInMonthLeap = new int[]
	{31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	
	private static LanguagePack phrases;//=new LanguagePack();
	private static String[] monthNames;//=Phrases.obtainValues((String)Phrases.Phrases.get("3"));

	//private final static String monthNames[] = new String[]
	//{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

	private static String[] dayNames;// = Phrases.obtainValues((String)Phrases.Phrases.get("2"));
        private static String[] civilMonthNames;//=Phrases.obtainValues((String)Phrases.Phrases.get("4"));


	//private final static String monthNames[] = new String[]
	//{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

	private static String[] civilDayNames;// = Phrases.obtainValues((String)Phrases.Phrases.get("5"));
	//new String[]
	//{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
	private static String[] errors;//=Phrases.obtainValues((String)Phrases.Phrases.get("Errors"));
	private static String format;
        private static StringOp analyse=new StringOp();


	// Two "overloaded" modulo methods to replace the existing % operator
	// Because Java doesn't handle direct operator overloading
	private int mod(int divisor, int modulo)
	{
		int temp = divisor % modulo;
	
		if (temp == 0)
		{
			temp = modulo;
		}

		return temp;
	} 

	private int mod(long divisor, int modulo)
	{
		int temp = (int)(divisor % modulo);

		if (temp == 0)
		{
			temp = modulo;
		}
	
		return temp;
	}

	/**
	 * Constructor to create a JDate object.<p>
	 * For more information, read: http://en.wikipedia.org/wiki/Julian_date
	 * @param  month the month (Jan = 1 — Dec = 12)
	 * @param  day  the day (from 1 to daysInMonth[month - 1]
	 * @param  year  the year (AD)
	 * @throws IllegalArgumentException if argument is invalid 
	 */
	public JDate(int month, int day, int year) throws IllegalArgumentException
	{
		if (month < 1 || month > 12)
		{
			throw (new IllegalArgumentException(errors[7]) );
		}

		if (day < 0)
		{
			throw (new IllegalArgumentException(errors[8]+" " + day) );
		}

		if (year % 4 == 0)
		{
			// leap year
			if (day > daysInMonthLeap[month - 1])
			{
				throw (new IllegalArgumentException(errors[8]+" " + day + " "+errors[9]+" " + month));
			}
		}
		else
		{
			if (day > daysInMonth[month - 1])
			{
				throw (new IllegalArgumentException(errors[8]+" " + day + " "+errors[9]+" " + month));
			}
		}

		// construct instruments
		int a = (int)Math.floor((14 - month) / 12);
		int y = year + 4800 - a;
		int m = month + 12 * a - 3;

		// construct a julian day
		mnJday = (long)(day + Math.floor((153 * m + 2) / 5) + 365 * y + Math.floor(y / 4)) - 32083;
	}

	/**
	 * Constructor to create a JDate object.
	 * @param  jday A Julian date
	 * @throws IllegalArgumentException if the Julian date < 0
	 */
	public JDate(long jday) throws IllegalArgumentException
	{
		if (jday < 0)
		{
			throw (new IllegalArgumentException(errors[9]) );
		}

		mnJday = jday;
	}

	/**
	 * Constructor to create a JDate object initialized to the day when the constructor was called
	 */
	protected JDate()
	{
		Date now = new Date(); 
		long mils = now.getTime(); // number of miliseconds since Greg. Jan 1 1970, 00:00:00 GMT
		
		// convert the number of miliseconds to the number of days since Jan 1 1970 Greg
		long days = (long)Math.floor(mils / 86400000);
		// the Julian Day for Jan 1 1970 Gregorian is 2 440 588
		mnJday = days + 2440588;
	}

	/**
	 * A method for obtaining the year from a JDate object
	 * @return integer with the year of the JDate object
	 */
	public int getYear()
	{
		long jbar = mnJday + 32083;
		// Compute the number of four-year Julian cycles that have elapsed since mn_jday
		// There are 1461 days in each cycle
		// Multiply this number by four (years/cycle)
		int n1 = (int)(jbar / 1461) * 4;
		// Compute the number of days since the last four-year cycle
		// Divide by 365 days in a year
		int n2 = (int)(jbar % 1461) / 365;
		int da = (int)(jbar % 1461);
		// Add one if we are after December 31, since JDate starts March 1
		int adj = (da % 365) > 306 ? 1 : 0;

		return (int)(Math.floor(n1 + n2) - 4800 + adj);
	}

	/**
	 * A method for obtaining the month from a JDate object
	 * @return integer with the month of the JDate object
	 */
	public int getMonth()
	{
	
            long jbar = mnJday + 32083;
		// Take jbar modulo 1461 to get the number of days since the last four-year cycle
		int da = (int)(jbar % 1461);
		// Take da modulo 365 to get the number of days since the last 1 March
		int m = mod(da, 365);
		// now, subtract off days for each of the months
		int j = 2;
		while (m > daysInMonthLeap[j])
		{
			m -= daysInMonthLeap[j];
			j++;
			if (j == 12)
			{
				j = 0;
			}
		}
			
		return j + 1;
	}

	/**
	 * A method for obtaining the day from a JDate object
	 * @return integer with the day of the JDate object
	 */
	public int getDay()
	{
		long jbar = mnJday + 32083;
		// repeat above steps until m
		int da = mod(jbar, 1461);
		int m;
		if (da == 1461)
		{
			m = 29; // FEBRUARY 29
		}
		else
		{
			m  = mod(da, 365);
			// this number is the number of days since the last March 1
			// now, take off days for each month
			int k = 2;
			while (m > daysInMonth[k])
			{
				m -= daysInMonth[k];
				k++;
				if (k == 12)
				{
					k = 0;
				}
			}
		}
		return m; // the number of days that will remain at the end
	}

	/**
	 * A method for obtaining the Julian date from a JDate object
	 * @return long with the Julian date
	 */
	public long getJulianDay()
	{
		return mnJday;
	}

	/**
	 * A method for obtaining the Gregorian date from a JDate object
	 * @return {@link Date} object with the date on the Gregorian calendar.<p>
	 * If year < 1563, returns the date on the proleptic Gregorian calendar
	 */
	protected Date getGregorianDate()
	{
		double j1;

		if (mnJday >= 2299160.5) 
		{
			double tmp = Math.floor(((mnJday - 1867216.0) - 0.25) / 36524.25);
			j1 = mnJday + 1 + tmp - Math.floor(0.25 * tmp);
		}
		else
		{
			j1 = (double)mnJday;
		}

		double j2 = j1 + 1524.0;
		double j3 = Math.floor(6680.0 + ((j2 - 2439870.0) - 122.1) / 365.25);
		double j4 = Math.floor(j3 * 365.25);
		double j5 = Math.floor((j2 - j4) / 30.6001);

		int d = (int)Math.floor(j2 - j4 - Math.floor(j5 * 30.6001));
		int m = (int)Math.floor(j5 - 1.0);
		if (m > 12)
		{
			m -= 12;
		}
		int y = (int)Math.floor(j3 - 4715.0);

		if (m > 2)
		{
			--y;
		}
		if (y <= 0)
		{
			--y;
		}

		return new Date(y - 1900, m - 1, d); // return the date object
	}
	
	public String getGregorianDateS(LinkedHashMap<String, Object> dayInfo)
	{
            analyse.setDayInfo(dayInfo);
            phrases=new LanguagePack(dayInfo);
            dayNames = phrases.obtainValues(phrases.getPhrases().get("2"));
         civilMonthNames=phrases.obtainValues(phrases.getPhrases().get("4"));
        	monthNames=phrases.obtainValues(phrases.getPhrases().get("3"));
                civilDayNames = phrases.obtainValues(phrases.getPhrases().get("5"));
	errors=phrases.obtainValues(phrases.getPhrases().get("Errors"));
		//GIVES THE STRING IN THE LOCAL FORMAT.
		double j1;

		if (mnJday >= 2299160.5) 
		{
			double tmp = Math.floor(((mnJday - 1867216.0) - 0.25) / 36524.25);
			j1 = mnJday + 1 + tmp - Math.floor(0.25 * tmp);
		}
		else
		{
			j1 = (double)mnJday;
		}

		double j2 = j1 + 1524.0;
		double j3 = Math.floor(6680.0 + ((j2 - 2439870.0) - 122.1) / 365.25);
		double j4 = Math.floor(j3 * 365.25);
		double j5 = Math.floor((j2 - j4) / 30.6001);

		int d = (int)Math.floor(j2 - j4 - Math.floor(j5 * 30.6001));
		int m = (int)Math.floor(j5 - 1.0);
		if (m > 12)
		{
			m -= 12;
		}
		int y = (int)Math.floor(j3 - 4715.0);

		if (m > 2)
		{
			--y;
		}
		if (y <= 0)
		{
			--y;
		}
		format= phrases.getPhrases().get("DateFormat");
		int dow =getDayOfWeek();
		int year = y;
		int month = m;
		int day = d;
                if(analyse.getDayInfo().get(Constants.IDEOGRAPHIC)==null)
                {
                    format=format.replace("WW",civilDayNames[dow]);
		format=format.replace("DD",String.valueOf(day));
		format=format.replace("MM",civilMonthNames[month-1]);
		format=format.replace("YY",String.valueOf(year));
		format=Character.toUpperCase(format.charAt(0))+format.substring(1);
                }
                else
                {
                if (analyse.getDayInfo().get(Constants.IDEOGRAPHIC).equals("1"))
                {
                    RuleBasedNumber convertN=new RuleBasedNumber(analyse.getDayInfo());
                    format=format.replace("WW",civilDayNames[dow]);
                    format=format.replace("DD",convertN.getFormattedNumber(Long.parseLong(String.valueOf(day))));
                    format=format.replace("MM",civilMonthNames[month-1]);
                    format=format.replace("YY",convertN.getFormattedNumber(Long.parseLong(String.valueOf(year))));
                    format=Character.toUpperCase(format.charAt(0))+format.substring(1);

                }
                else
                {
		format=format.replace("WW",civilDayNames[dow]);
		format=format.replace("DD",String.valueOf(day));
		format=format.replace("MM",civilMonthNames[month-1]);
		format=format.replace("YY",String.valueOf(year));
		format=Character.toUpperCase(format.charAt(0))+format.substring(1);
                }
                }
		return format;

		//return new Date(y - 1900, m - 1, d); // return the date object
	}
	
	/**
	 * A method for obtaining the day of week from a JDate object
	 * @return integer with the day of the week of the JDate object, where Sunday = 0, Monday = 1, etc.
	 */
	public int getDayOfWeek()
	{
		int temp = (int)(mnJday % 7) + 1;
		if (temp == 7)
		{
			temp = 0;
		}

		return temp;
	}

	public int getDoy()
	{
		long jbar = mnJday + 32083;
		// repeat above steps until m
		int da = mod(jbar, 1461);

		// this is up to 1461 days since the start of a Julian cycle on March 1, leap year
		// if da is 1461, we are at February 29 (doy 366)
		// otherwise, we mod by 365 to figure out where we are in the year, remembering to add 59 to get to Jan 1
		return da == 1461 ? 366 : mod(da + 59, 365) - 1; // Jan 1 is doy 0
	}

	/**
	 * A method to check if one JDate object is equal to another JDate object.
	 * @param jDate a JDate object
	 * @return For n.equals(m), true iff n.getJulianDay() == m.getJulianDay()<p>false otherwise
	 */
	public boolean equals(JDate jDate)
	{
		return (mnJday == jDate.getJulianDay());
	}

	/**
	 * A method to compare two JDate objects.
	 * @param jDate a JDate object
	 * @return For n.compareTo(m), 0 iff n == m<p>
	 * a number less than 0 iff n < m<p>
	 * a number greater than 0 iff m > n
	 */
	@Override
	public int compareTo(JDate jDate)
	{	
		int temp;
		try
		{
			temp = (int)(mnJday - jDate.getJulianDay());
		}
		catch (ClassCastException cce)
		{
			throw (new ClassCastException(cce.toString()));
		}
		return temp;
	}

	/**
	 * A method to obtain a String from a JDate object.
	 * @param dayInfo a LinkedHashMap<String, Object> with localization
	 * @return A String with the String value of a date
	 */
	public String toString(LinkedHashMap<String, Object> dayInfo) {
		analyse.setDayInfo(dayInfo);
		phrases = new LanguagePack(dayInfo);
		dayNames = phrases.obtainValues(phrases.getPhrases().get("2"));
		civilMonthNames = phrases.obtainValues(phrases.getPhrases().get("4"));
		monthNames = phrases.obtainValues(phrases.getPhrases().get("3"));
		format = phrases.getPhrases().get("DateFormat");
		int dow = getDayOfWeek();
		int year = getYear();
		int month = getMonth();
		int day = getDay();

		if (analyse.getDayInfo().get(Constants.IDEOGRAPHIC) == null) {
			format = format.replace("WW", dayNames[dow]);
			format = format.replace("DD", String.valueOf(day));
			format = format.replace("MM", monthNames[month - 1]);
			format = format.replace("YY", String.valueOf(year));
			format = Character.toUpperCase(format.charAt(0)) + format.substring(1);
		} else {
			if (analyse.getDayInfo().get(Constants.IDEOGRAPHIC).equals("1")) {
				RuleBasedNumber convertN = new RuleBasedNumber(dayInfo);
				format = format.replace("WW", dayNames[dow]);
				format = format.replace("DD", convertN.getFormattedNumber(Long.parseLong(String.valueOf(day))));
				format = format.replace("MM", monthNames[month - 1]);
				format = format.replace("YY", convertN.getFormattedNumber(Long.parseLong(String.valueOf(year))));
				format = Character.toUpperCase(format.charAt(0)) + format.substring(1);

			} else {
				format = format.replace("WW", dayNames[dow]);
				format = format.replace("DD", String.valueOf(day));
				format = format.replace("MM", monthNames[month - 1]);
				format = format.replace("YY", String.valueOf(year));
				format = Character.toUpperCase(format.charAt(0)) + format.substring(1);
			}
		}

		return format;
	}

	/**
	 * A cloning method.
	 * @return A clone.
	 */
	public Object clone()
	{
		return new JDate(mnJday);
	}

	/**
	 * A method to add a specified number of days to a JDate object.
	 * @param n an integer with the number of days to be added
	 */
	public synchronized void addDays(int n)
	{
		mnJday += n;
	}

	/**
	 * A method to subtract a specified number of days to a JDate object.
	 * @param n an integer with the number of days to be subtracted
	 */
	public synchronized void subtractDays(int n)
	{
		mnJday -= n;
	}

	/**
	 * A method to add a specified number of months to a JDate object.
	 */
	protected synchronized void addMonths()
	{
		mnJday += (this.getYear() % 4) == 0 ? daysInMonthLeap[this.getMonth() - 1] : daysInMonth[this.getMonth() - 1];
	}

	/**
	 * A method to subtract a specified number of months to a JDate object.
	 */
	protected synchronized void subtractMonths()
	{
		int month = this.getMonth() - 1; // ADJUSTED TO BE THE ARRAY INDEX (STARTS WITH 0)
		month--; // PREVIOUS MONTH

		if (month < 0)
		{
			month += 11;
		}

		mnJday -= ((this.getYear() % 4) == 0) ? daysInMonthLeap[month] : daysInMonth[month];
	}


	/**
	 * A method to find the difference between two JDate objects
	 * @param former a JDate object
	 * @param latter another JDate object
	 * @return A long with the number of days between the two objects, non-first-inclusive. I.e. saturday - sunday = 6
	 */
	public static long difference(JDate former, JDate latter)
	{
		return former.getJulianDay() - latter.getJulianDay();
	}

	/**
	 * A method to return the maximum number of days in a month
	 * @param month an integer with the year
	 * @param year an integer with the year
	 * @return The maximum number of days in that month, given that year
	 */
	protected static int getMaxDaysInMonth(int month, int year) throws IllegalArgumentException
	{
		if (month < 1 || month > 12)
		{
			throw (new IllegalArgumentException(errors[7]));
		}

		return (year % 4 == 0) ? daysInMonthLeap[month - 1] : daysInMonth[month - 1];
	}

	/**
	 * A method to return a String of the JDate
	 * @return Julian date expressed according to ISO 8601
	 */
	public String toString() {
		final String month = String.format("%02d", getMonth());
		final String day = String.format("%02d", getDay());
		return getYear() + "-" +  month + "-" + day;
	}

}
