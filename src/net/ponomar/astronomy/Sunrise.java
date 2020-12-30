package net.ponomar.astronomy;

import java.util.LinkedHashMap;

import net.ponomar.calendar.JDate;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.utility.Constants;
 
 
import net.ponomar.utility.RuleBasedNumber;
import net.ponomar.utility.StringOp;

/***********************************************************
 Sunrise : a class for performing calculations of sunrise and sunset

 PURPOSE: the purpose of this class is to calculate sunrise and sunset
 for any lattitude and longitude for any day.
 METHODOLOGY: Call getSunriseSunset, see comments below for more information

 A lot of the code has been moved to Astronomy. Check there for more information.

 (C) 2006 ALEKSANDR ANDREEV.

 Here is the copyright information provided by Paul Schlyer:

 Written as DAYLEN.C, 1989-08-16

 Modified to SUNRISET.C, 1992-12-01

 (c) Paul Schlyter, 1989, 1992

 Released to the public domain by Paul Schlyter, December 1992

 Permission is hereby granted, free of charge, to any person obtaining a
 copy of this software and associated documentation files (the "Software"),
 to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense,
 and/or sell copies of the Software, and to permit persons to whom the
 Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE. 
***********************************************************/

public final class Sunrise
{
        private final LanguagePack phrases;//=new LanguagePack();
        private final StringOp analyse=new StringOp();
        public Sunrise(LinkedHashMap<String, Object> dayInfo){
            analyse.setDayInfo(dayInfo);
            phrases=new LanguagePack(dayInfo);
        }

	// GATEWAY TO SUNRISE SUNSET INTERFACE
	// COMPUTES THE SUNRISE AND SUNSET TIMES
	// PARAMETERS:: SEE BELOW
	// RETURNS:: A double[] array WITH FIRST ENTRY SUNRISE AND SECOND ENTRY SUNSET FOR THAT DAY
	// THE ARRAY SUNRISE AND SUNSET ARE INDICATED IN hh.decimal LOCAL TIME (AS SPECIFIED BY tzone and isDST)
	// WHERE hh IS THE HOUR OF THE SUNRISE / SUNSET
	// AND THE decimal IS THE FRACTION AFTER THE TOP THE HOUR
	// TO GET THE MINUTE, TAKE decimal * 60 AND ROUND TO NEAREST INTEGER
	/** DETAILED INSTRUCTIONS FOR PARAMETERS AND CONSTANTS:
		date IS A JDATE OBJECT WITH A DATE ON THE JULIAN CALENDAR

		lon IS A double WITH THE LONGITUDE WITH RESPECT TO GREENWICH, ENGLAND
		Eastern longitude is entered as a positive number
		Western longitude is entered as a negative number

		lat IS A double WITH THE LATITUDE WITH RESPECT TO THE EQUATOR
		Northern latitude is entered as a positive number
		Southern latitude is entered as a negative number

		tzone IS AN int WITH THE LOCAL TIME ZONE OFFSET (IN HOURS) FROM GMT,
		NOT ACCOUNTING DAYLIGHT SAVINGS TIME
		TIME ZONE AHEAD OF GMT IS POSITIVE
		TIME ZONE BEHIND GMT IS NEGATIVE

		isDST IS A boolean OPTIONAL PARAMETER
		IF isDST IS true, LOCATION IS IN DAYLIGHT SAVINGS TIME (DST)
		AND LOCAL TIME IS GMT + tzone + 1
		IF isDST IS false, LOCATION IS NOT IN DST, AND LOCAL TIME IS GMT + tzone
		IF NOT SPECIFIED, isDST IS ASSUMED TO BE false

		alt IS A double OPTIONAL PARAMETER INDICATING THE DESIRED 
		ALTITUDE OF THE SUN AT "SUNSET".
		IF NOT SPECIFIED, alt IS ASSUMED TO BE -0.833, SINCE THIS IS A STANDARD IN MOST LOCATIONS

		OTHER VALUES FOR alt INCLUDING AVAILABLE CONSTANTS:
		0 degrees - Center of Sun's disk touches a mathematical horizon
		-0.25 degrees - Sun's upper limb touches a mathematical horizon
		-0.583 degrees - Center of Sun's disk touches the horizon; atmospheric refraction accounted for
		-0.833 degrees, DEFAULT - Sun's upper limb touches the horizon; atmospheric refraction accounted for
		-6 degrees, CIVIL - Civil twilight (one can no longer read outside without artificial illumination)
		-12 degrees, NAUTICAL - Nautical twilight (navigation using a sea horizon no longer possible)
		-15 degrees, AMATEUR - Amateur astronomical twilight (the sky is dark enough for most astronomical observations)
		-18 degrees, ASTRONOMICAL - Astronomical twilight (the sky is completely dark)

	*****************************************************************************/
	protected static double[] getSunriseSunset(JDate date, double lon, double lat, int tzone)
	{
		return getSunriseSunset(date, lon, lat, tzone, false, -0.833);
	}

	protected static double[] getSunriseSunset(JDate date, double lon, double lat, int tzone, boolean isDST)
	{
		return getSunriseSunset(date, lon, lat, tzone, isDST, -0.833);
	}

	protected static double[] getSunriseSunset(JDate date, double lon, double lat, int tzone, boolean isDST, double alt)
	{
		double d = (double)Astronomy.daysSinceJan0(date.getJulianDay()) + 0.5 - lon / 360.0;
		double[] hours = Astronomy.computeSunriseSunset(d, lon, lat, alt);
		
		// CONVERT FROM UT TO THE LOCAL TIME
		if (isDST)
		{
			tzone += 1;
		}
	
		hours[0] += tzone;
		hours[1] += tzone;

		if (hours[0] > 24)
		{
			hours[0] -= 24;
		}
		if (hours[1] > 24)
		{
			hours[1] -= 24;
		}
		if (hours[0] < 0)
		{
			hours[0] += 24;
		}
		if (hours[1] < 0)
		{
			hours[1] += 24;
		}

		return hours;
	}

	public String[] getSunriseSunsetString(JDate date, String lon, String lat, String tzone)
	{
		return getSunriseSunsetString(date, Double.parseDouble(lon), Double.parseDouble(lat), Integer.parseInt(tzone));
	}
			

	protected String[] getSunriseSunsetString(JDate date, double lon, double lat, int tzone)
	{
		double[] raw = getSunriseSunset(date, lon, lat, tzone, false, -0.833);
		String[] out = new String[2];
                

		// NOW, TAKE THE RAW INPUT AND PARSE IT TO HOURS / MINUTES
		for (int i = 0; i < 2; i++)
		{
			String format= phrases.getPhrases().get("TimeF");
                        int hour = (int)Math.floor(raw[i]);
			int minute = (int)Math.floor((raw[i] - hour) * 60);
                        /* original version
			if (hour < 10)
			{
				out[i] = "0" + hour;
			}
			else
			{
				out[i] = String.valueOf(hour);
			}
			if (minute < 10)
			{
				out[i] += ":0" + minute;
			}
			else
			{
				out[i] += ":" + minute;
			}*/
			// Changed to this by Y.S. to internationalise it.
			if (analyse.getDayInfo().get(Constants.IDEOGRAPHIC).equals("1")) {
				RuleBasedNumber convertN = new RuleBasedNumber((LinkedHashMap<String, Object>) analyse.getDayInfo().clone());
				format = format.replace("HH", convertN.getFormattedNumber(hour));
				format = format.replace("MM", convertN.getFormattedNumber(minute));

			} else {
				format = format.replace("HH",
						String.format("%0" + phrases.getPhrases().get("PadH") + "d", hour));// Integer.toString(hour));
				// Format=Format.replace("MM", Integer.toString(minute));
				format = format.replace("MM",
						String.format("%0" + phrases.getPhrases().get("PadM") + "d", minute));
			}
			out[i] = format;
		}

		return out;
	}
}
