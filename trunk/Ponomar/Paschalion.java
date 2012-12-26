package Ponomar;

import java.util.*;

/***************************************************************************
 Paschalion.java - A CLASS FOR WORKING WITH THE PASCHALION OF THE ORTHODOX CHURCH
 PURPOSE: The purpose of this class is to provide an interface for various 
 dates of feasts, fasts, and floating observances as well as lunar tables.

 Paschalion.java is part of the Ponomar program.
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
****************************************************************************/

final class Paschalion
{
	// THE LENGTH OF A LUNAR MONTH
	private final static double lunarMonth = 29.52916667;
	private final static double lengthOfRem = 0.016932411; // SEE COMMENTS IN getLunarPhaseString()

	// THE FOUNDATION IS THE "AGE OF THE MOON" (NUMBER OF DAYS SINCE NEW MOON)
	// ON 1 MARCH, JULIAN CALENDAR, FOR A PARTICULAR LUNAR YEAR
	// THERE ARE 19 YEARS IN THE METONIC CYCLE
	// IN THIS CALCULATION, IT IS HOURS SINCE MIDNIGHT
	private final static double foundation[] = new double[]
	{14.042016807, 25.462184874, 6.084033613, 17.966386555, 28.336134454, 9.210084034, 20.504201681, 1.420168067, 12.294117647, 23.168067227, 4.546218487, 15.042016807, 26.294117647, 7.630252101, 18.546218487, 29.420168067, 11.756302521, 26.210084034, 3.042016807};

	// A mod OPERATOR
	private static int mod(int divisor, int modulo)
	{
		int temp = divisor % modulo;
	
		if (temp == 0)
		{
			temp = modulo;
		}

		return temp;
	}

	// Another mod OPERATOR
	// THE POINT OF THIS IS TO FIND THE REMAINDER FROM DIVISION OF A DECIMAL
	// BY ANOTHER DECIMAL, MUCH IN THE SPIRIT OF MOD
	private static double mod(double divisor, double modulo)
	{
		double quotient = Math.floor(divisor / modulo);

		double remainder = divisor - quotient * modulo;

		return remainder;
	}

	// A METHOD TO OBTAIN THE DATE OF THE JULIAN PASCHA
	// USES THE GAUSSIAN FORMULAE TO OBTAIN PASCHA
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: A JDate WITH THE DATE OF PASCHA FOR THAT YEAR (JULIAN CALENDAR)
	// THROWS: IllegalArgumentException IF year < 33
	protected static JDate getPascha(int year) throws IllegalArgumentException
	{
		if (year < 33)
		{
			throw (new IllegalArgumentException("Invalid year"));
		}
		
		int a = year % 4;
		int b = year % 7;
		int c = year % 19;
		int d = (19 * c + 15) % 30;
		int e = (2 * a + 4 * b - d + 34) % 7;
		int f = (int)Math.floor((d + e + 114) / 31); //Month of pascha e.g. march=3
		int g = ((d + e + 114) % 31) + 1; //Day of pascha in the month
		// Create a JDate object
		return new JDate(f, g, year);
	}

	// A METHOD TO OBTAIN THE DATE OF JULIAN PENTECOST
	// USES THE ABOVE ALGORITHM AND ADDS 49 DAYS
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: A JDate WITH THE DATE OF PENTECOST (JULIAN CALENDAR)
	// THROWS: IllegalArgumentException IF year < 33
	protected static JDate getPentecost(int year) throws IllegalArgumentException
	{
		if (year < 33)
		{
			throw (new IllegalArgumentException("Invalid year"));
		}

		JDate date = getPascha(year);

		date.addDays(49);
		return date;
	}

	// A METHOD TO OBTAIN THE DATE JULIAN LENT STARTS (48 DAYS BEFORE PASCHA)
	// USES THE ABOVE ALOGORITHM AND SUBTRACTS 48 DAYS
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: A JDate WITH THE DATE LENT STARTS (DATE OF CLEAN MONDAY)
	// THROWS: IllegalArgumentException IF year < 33
	protected static JDate getLentStart(int year) throws IllegalArgumentException
	{
		if (year < 33)
		{
			throw(new IllegalArgumentException("Invalid year"));
		}

		JDate date = getPascha(year);

		date.subtractDays(48);
		return date;
	}

	// A METHOD TO OBTAIN THE DATE APOSTLES' FAST STARTS
	// USES THE ABOVE ALGORITHM AND ADDS 57 DAYS
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURS: A JDate WITH THE DATE APOSTLES' FAST STARTS (MONDAY AFTER SUNDAY OF ALL SAINTS)
	// THROWS: IllegalArgumentException IF year < 33
	protected static JDate getApostlesFastStart(int year) throws IllegalArgumentException
	{
		if (year < 33)
		{
			throw(new IllegalArgumentException("Invalid year"));
		}

		JDate date = getPascha(year);

		date.addDays(57);
		return date;
	}
	
	// A METHOD TO OBTAIN THE LENGTH OF APOSTLES' FAST
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: AN int WITH THE LENGTH OF APOSTLES' FAST
	// THROWS: IllegalArgumentException IF year < 33
	protected static int getApostlesFastLength(int year) throws IllegalArgumentException
	{
		if (year < 33)
		{
			throw(new IllegalArgumentException("Invalid year"));
		}

		JDate start = getApostlesFastStart(year);		

		JDate end = new JDate(6, 29, year);
		return (int)JDate.difference(end, start);
	}

	// METHODS FOR DEALING WITH THE VISUAL PASCHALION
	// FOR ADVANCED USERS ONLY
	// A METHOD TO OBTAIN THE kluch granits (key of boundaries)
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: AN int REPRESENTING ONE OF THE LETTERS OF THE KEY, WHERE Az = 1
	// THROWS: ditto
	protected static int getKeyOfBoundaries(int year) throws IllegalArgumentException
	{
		if (year < 33)
		{
			throw(new IllegalArgumentException("Invalid year"));
		}

		JDate date = getPascha(year);

		int f = date.getMonth();
		int g = date.getDay();

		if (f == 3)
		{
			// PASCHA IS IN MARCH
			g -= 21;
		}
		else
		{
			// PASCHA IS IN APRIL
			g += 10;
		}

		return g;
	}

	// A METHOD TO OBTAIN THE INDICTION
	// PARAMETERS: ditto
	// RETURNS: AN INTEGER WITH THE INDICTION
	// THROWS: ditto
	protected static int getIndiction(int year) throws IllegalArgumentException
	{
		if (year < 33)
		{
			throw(new IllegalArgumentException("Invalid year"));
		}

		return mod(year - 312, 15);
	}

	// A METHOD TO OBTAIN THE SOLAR CYCLE
	// PARAMETERS: ditto
	// RETURNS: AN int WITH THE SOLAR CYCLE FOR THIS YEAR
	// THROWS: ditto
	protected static int getSolarCycle(int year) throws IllegalArgumentException
	{
 		if (year < 33)
		{
			throw(new IllegalArgumentException("Invalid year"));
		}

		return mod(year + 5508, 28);
	}

	// A METHOD FOR OBTAINING THE LUNAR CYCLE
	// PARAMETERS: ditto
	// RETURNS: AN int WITH THE LUNAR CYCLE FOR THIS YEAR
	// THROWS: ditto
	protected static int getLunarCycle(int year) throws IllegalArgumentException
	{
 		if (year < 33)
		{
			throw(new IllegalArgumentException("Invalid year"));
		}

		int temp = (year + 1) % 19 - 3;
		if (temp <= 0)
		{
			temp += 19;
		}

		return temp;
	}

	// METHODS FOR PERFORMING CALCULATIONS WITH PHASES OF THE MOON
	// ALL LUNAR PHASE INFO IS COMPUTED **NOT ASTRONOMICALLY** BUT ON THE METONIC CYCLE
	// EXPERIMENTAL ------------ NOT FOR FURTHER USE AS OF THIS VERSION ----------------
	// NO GUARANTEES OF ANY KIND ARE MADE ABOUT THE RESULTS OF THESE (EXPERIMENTAL) ALGORITHMS
	
	// A METHOD TO OBTAIN THE PHASE OF THE MOON FOR SOME DATE
	// PARAMETERS: A JDate object WITH THE DATE DESIRED
	// RETURNS: A double WITH THE PHASE OF THE MOON FOR THAT DAY 
	// WHERE NEW MOON = 0, FULL MOON = 0.5
	// THROWS: IllegalArgumentException IF THE YEAR IS < 33 AD
	protected static double getLunarPhase(JDate date) throws IllegalArgumentException
	{
		int year  = date.getYear();
		int cycle;
		try
		{
			cycle = getLunarCycle(year);
		} catch(IllegalArgumentException iae) {
			throw (iae);
		}

		// FIND THE DIFFERENCE (IN DAYS) BETWEEN NOW AND MAR 1
		long diff  = JDate.difference(date, new JDate(3, 1, year));
		if (diff < 0)
		{
			// WE ARE BEFORE MAR 1, SO USE THE PREVIOUS YEAR
			diff = JDate.difference(date, new JDate(3, 1, year - 1));
		}

		// TAKE THE DIFFERENCE MODULO THE LENGTH OF A LUNAR MONTH
		// TO FIND THE REMAINDER OF THE MOON VIS-A-VIS MAR 1
		double remainder = mod((double)diff, lunarMonth);
		// ADD THIS REMAINDER TO THE AGE OF THE MOON ON MAR 1
		remainder += foundation[cycle - 1];
		while (remainder >= lunarMonth)
		{
			remainder -= lunarMonth;
		}

		// SCALE THE AGE OF THE MOON TO A MORE MANAGEABLE QUANTITY
		return (remainder / lunarMonth);
	}

	// CONVERT ABOVE TO A STRING WITH THE PHASE OF THE MOON
	protected static String getLunarPhaseString(JDate date, OrderedHashtable dayInfo) throws IllegalArgumentException
	{
		double raw;

		try
		{
			raw = getLunarPhase(date);
		}
		catch(IllegalArgumentException e)
		{
			throw (new IllegalArgumentException(e.toString()));
		}

		String ret = "";
		// LET ME EXPLAIN THIS WITH AN EXAMPLE. THE PHASE TODAY IS "FULL MOON"
		// IFF THE TIME OF FULL MOON OCCURS +/- ONE-HALF LUNAR DAY (lengthOfRem)
		// FROM THE TIME WE ARE CONSIDERING, SINCE THE TIME WE ARE CONSIDERING IS NOON
		// ALL OTHER PHASES ARE ANALAGOUS
		LanguagePack Text=new LanguagePack(dayInfo);
		String[] Phases=Text.obtainValues((String)Text.Phrases.get("Phases"));
		if (raw < lengthOfRem || raw > 1 - lengthOfRem)
		{
			ret =Phases[0];
		}
		else if (raw < 0.25 - lengthOfRem)
		{
			ret =Phases[1];
		}
		else if (raw >= 0.25 - lengthOfRem && raw <= 0.25 + lengthOfRem)
		{
			ret = Phases[2];
		}
		else if (raw < 0.5 - lengthOfRem)
		{
			ret = Phases[3];
		}
		else if (raw >= 0.5 - lengthOfRem && raw <= 0.5 + lengthOfRem)
		{
			ret = Phases[4];
		}
		else if (raw < 0.75 - lengthOfRem)
		{
			ret =Phases[5];
		}
		else if (raw >= 0.75 - lengthOfRem && raw <= 0.75 + lengthOfRem)
		{
			ret = Phases[6];
		}
		else if (raw >= 0.75 + lengthOfRem)
		{
			ret = Phases[7];
		}

		return ret;
	}

	// A METHOD TO OBTAIN THE DATE OF THE NEXT NEW MOON
	// PARAMETERS: A JDate WITH THE DATE DESIRED
	// RETURNS: A JDate WITH THE DATE OF THE NEXT NEW MOON, ROUNDED DOWN
	// THROWS: IllegalArgumentException IF YEAR < 33
	protected static JDate getNextNewMoon(JDate date) throws IllegalArgumentException
	{
		// GET THE LUNAR PHASE FOR THIS DATE
		double phase;
		try
		{
			phase = getLunarPhase(date);
		} catch (IllegalArgumentException iae) {
			throw (iae);
		}

		// OBTAIN THE AGE OF THE MOON (AGAIN)
		phase *= lunarMonth;

		// CALCULATE HOW MANY DAYS REMAIN UNTIL PHASE == LUNARMONTH
		int diff = (int)Math.floor(lunarMonth - phase);

		// ADD THAT MANY DAYS TO THE CURRENT DATE
		date.addDays(diff);
		return date;
	}

	// A METHOD TO OBTAIN THE DATE OF THE NEXT FULL MOON
	// PARAMETERS: A JDate WITH THE DATE DESIRED
	// RETURNS: A JDate WITH THE DATE OF THE NEXT FULL MOON, ROUNDED DOWN
	// THROWS: IllegalArgumentException IF YEAR < 33
	protected static JDate getNextFullMoon(JDate date) throws IllegalArgumentException
	{
		// GET THE LUNAR PHASE FOR THIS DATE
		double phase;
		try
		{
			phase = getLunarPhase(date);
		} catch (IllegalArgumentException iae) {
			throw (iae);
		}

		// OBTAIN THE AGE OF THE MOON
		phase *= lunarMonth;

		// FIND OUT HOW MANY REMAIN UNTIL PHASE == LUNARMONTH / 2
		int diff = (int)Math.floor(lunarMonth / 2 - phase);
		if (diff < 0)
		{
			diff = (int)Math.floor(lunarMonth / 2 - phase + lunarMonth);
		}

		// ADD THIS MANY DAYS TO THE CURRENT DATE
		date.addDays(diff);
		return date;
	}

	// A METHOD FOR FIGURING OUT THE FAST DAYS FOR A PARTICULAR YEAR
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: AN array WITH THE FASTING REGULATIONS FOR EVERY DAY OF THAT YEAR
	// 	0 - FAST FREE
	//	1 - FAST DAY
	//	2 - CHEESEFARE
	// ONLY FOR USE WITH THE CALENDAR CONTROL; FOR SPECIFIC DAYS, MORE CONVOLUTED CALCULATIONS ARE MADE
	// THROWS: ditto
	protected static int[] getFasts(int year) throws IllegalArgumentException
	{
		if (year < 33)
		{
			throw( new IllegalArgumentException("Invalid year"));
		}

		// A HASHTABLE WITH MANDATORY FAST DAYS IN THE YEAR
		Hashtable mustFast = new Hashtable();

		mustFast.put(new JDate(1, 5, year).getJulianDay(), "Eve of Theophany");
		mustFast.put(new JDate(8, 29, year).getJulianDay(), "Beheading");
		mustFast.put(new JDate(9, 14, year).getJulianDay(), "Exaltation");

		// A HASHTABLE WITH MANDATORY FAST-FREE DAYS
		Hashtable cantFast = new Hashtable();

		cantFast.put(new JDate(1, 6, year).getJulianDay(), "Theophany");

		// PASCHA
		JDate pascha = getPascha(year);

		// OTHER FASTING REGULATIONS
		JDate SVIATKI_START  = new JDate(12, 25, year);
		JDate SVIATKI_END    = new JDate(1, 4, year);
		JDate PUB_PHAR_START = new JDate(pascha.getJulianDay() - 70);
		JDate PUB_PHAR_END   = new JDate(pascha.getJulianDay() - 63);
		JDate CHEESE_START   = new JDate(pascha.getJulianDay() - 55);
		JDate CHEESE_END     = new JDate(pascha.getJulianDay() - 49);
		JDate LENT_START     = new JDate(pascha.getJulianDay() - 48);
		JDate LENT_END       = new JDate(pascha.getJulianDay() - 1);
		JDate BRIGHT_START   = new JDate(pascha.getJulianDay());
		JDate BRIGHT_END     = new JDate(pascha.getJulianDay() + 6);
		JDate PENT_START     = new JDate(pascha.getJulianDay() + 49);
		JDate PENT_END       = new JDate(pascha.getJulianDay() + 56);
		JDate APOSTLES_START = new JDate(pascha.getJulianDay() + 57);
		JDate APOSTLES_END   = new JDate(6, 28, year);
		JDate DORM_START     = new JDate(8, 1, year);
		JDate DORM_END       = new JDate(8, 14, year);
		JDate ADVENT_START   = new JDate(11, 15, year);
		JDate ADVENT_END     = new JDate(12, 24, year);

		JDate dummy = new JDate(1, 1, year);
		int numdays = (year % 4 == 0) ? 366 : 365;

		int[] retval = new int[numdays];
		int i = 0;

		do
		{
			// figure out if this day is a fast day
			int fast = 0;

			if (mustFast.containsKey(dummy.getJulianDay()))
			{
				// mandatory fast
				fast = 1;
			}
			else if (cantFast.containsKey(dummy.getJulianDay()))
			{
				// not a fast day
			}
			else if (dummy.compareTo(SVIATKI_START) >= 0)
			{
				// not a fast day
			}
			else if (dummy.compareTo(SVIATKI_END) <= 0)
			{
				// not a fast day
			}
			else if (dummy.compareTo(PUB_PHAR_START) >= 0 && dummy.compareTo(PUB_PHAR_END) <= 0)
			{
				// not a fast day
			}
			else if (dummy.compareTo(CHEESE_START) >= 0 && dummy.compareTo(CHEESE_END) <= 0)
			{
				// CHEESEFARE WEEK
				fast = 2;
			}
			else if (dummy.compareTo(LENT_START) >= 0 && dummy.compareTo(LENT_END) <= 0)
			{
				// LENT
				fast = 1;
			}
			else if (dummy.compareTo(BRIGHT_START) >= 0 && dummy.compareTo(BRIGHT_END) <= 0)
			{
				// BRIGHT WEEK
				fast = 0;
			}
			else if (dummy.compareTo(PENT_START) >= 0 && dummy.compareTo(PENT_END) <= 0)
			{
				// PENTECOST WEEK
				fast = 0;
			}
			else if (dummy.compareTo(APOSTLES_START) >= 0 && dummy.compareTo(APOSTLES_END) <= 0)
			{
				// APOSTLES' FAST
				fast = 1;
			}
			else if (dummy.compareTo(DORM_START) >= 0 && dummy.compareTo(DORM_END) <= 0)
			{
				// DORMITION FAST
				fast = 1;
			}
			else if (dummy.compareTo(ADVENT_START) >= 0 && dummy.compareTo(ADVENT_END) <= 0)
			{
				// ADVENT
				fast = 1;
			}
			else
			{
				// IS THIS A WEDNESDAY OR FRIDAY?
				fast = (dummy.getDayOfWeek() == 3 || dummy.getDayOfWeek() == 5) ? 1 : 0;
			}

			retval[i] = fast;
			i++;
			dummy.addDays(1);
		}  while(i < numdays);

		return retval;

	}

	// A METHOD TO OBTAIN MAJOR FEAST DAYS FOR A PARTICULAR YEAR
	// PARAMETERS: AN int WITH THE YEAR DESIRED
	// RETURNS: A Hashtable OBJECT WITH THE FEASTS FOR THAT YEAR
	//	FIRST ENTRY: THE julian date of a feast
	//	SECOND ENTRY: A STRING DESCRIBING THAT FEAST
	// THROWS: ditto
	protected static Hashtable getFeasts(int year, OrderedHashtable dayInfo) throws IllegalArgumentException
	{
		if (year < 33)
		{
			throw( new IllegalArgumentException("Invalid year"));
		}

		Hashtable feasts = new Hashtable();
		LanguagePack Text=new LanguagePack(dayInfo);
		String[] FeastNames=Text.obtainValues((String)Text.Phrases.get("Feasts"));
		// ADD ALL THE FIXED FEASTS TO OUR HASHTABLE
		feasts.put(new JDate(1, 1, year).getJulianDay(), FeastNames[0]);
		feasts.put(new JDate(1, 6, year).getJulianDay(), FeastNames[1]);
		feasts.put(new JDate(6, 24, year).getJulianDay(),FeastNames[2]);
		feasts.put(new JDate(6, 29, year).getJulianDay(),FeastNames[3]);
		feasts.put(new JDate(8, 6, year).getJulianDay(), FeastNames[4]);
		feasts.put(new JDate(8, 15, year).getJulianDay(), FeastNames[5]);
		feasts.put(new JDate(8, 29, year).getJulianDay(),FeastNames[6]);
		feasts.put(new JDate(9, 8, year).getJulianDay(), FeastNames[7]);
		feasts.put(new JDate(9, 14, year).getJulianDay(), FeastNames[8]);
		feasts.put(new JDate(10, 1, year).getJulianDay(),FeastNames[9]);
		feasts.put(new JDate(11, 21, year).getJulianDay(), FeastNames[10]);
		feasts.put(new JDate(12, 25, year).getJulianDay(), FeastNames[11]);

		// NOW ADD THE MOVEABLE FEASTS TO OUR HASHTABLE
		JDate pascha = getPascha(year);
		// DOUBLE CHECK THAT PASCHA IS NOT ON ANNUNCIATION:
		if (pascha.equals(new JDate(3, 25, year)))
		{
			feasts.put(pascha.getJulianDay(),FeastNames[12]);
		}
		else
		{
			feasts.put(pascha.getJulianDay(), FeastNames[13]);
			feasts.put(new JDate(3, 25, year).getJulianDay(), FeastNames[14]);
		}

		feasts.put(new JDate(pascha.getJulianDay() + 49).getJulianDay(), FeastNames[15]);
		feasts.put(new JDate(pascha.getJulianDay() + 39).getJulianDay(), FeastNames[16]);
		feasts.put(new JDate(pascha.getJulianDay() - 7).getJulianDay(), FeastNames[17]);

		// CHECK THAT MEETING OF THE LORD DOES NOT OCCUR ON THE FIRST MONDAY OF LENT
		JDate meeting = new JDate(2, 2, year);

		if (JDate.difference(pascha, meeting) == 48)
		{
			// MEETING OF THE LORD TRANSFERRED TO FORGIVENESS SUNDAY
			meeting.subtractDays(1);
		}

		feasts.put(meeting.getJulianDay(), FeastNames[18]);

		return feasts;
	}
}
