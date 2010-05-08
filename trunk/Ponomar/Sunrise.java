package Ponomar;

/***********************************************************
 Sunrise : a class for performing calculations of sunrise and sunset

 PURPOSE: the purpose of this class is to calculate sunrise and sunset
 for any lattitude and longitude for any day.
 METHODOLOGY: Call getSunriseSunset, see comments below for more information

 THANK YOU TO Ron Hill, Robert Creager, Joshua Hoblitt, Chris Phillips, Brian D Foy
 Paul Schlyer of Stockholm, Sweden, and others who worked on Astro::Sunrise PERL MODULE

 CODE CONVERTED FROM PERL TO JAVA BY ALEKSANDR ANDREEV AS PART OF THE PONOMAR PROJECT
 CERTAIN FEATURES OF PERL CODE OMITTED FOR EASE AND BREVITY
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

final class Sunrise
{
	// CONSTANTS
	private static double INV360 = (1.0 / 360.0);
	private static double RADEG  = (180.0 / Math.PI);
	private static double DEGRAD = (Math.PI / 180.0);
	private static boolean upper_limb = true;
	protected final static double DEFAULT      = -0.833;
	protected final static double CIVIL        = -6.0;
	protected final static double NAUTICAL     = -12.0;
	protected final static double AMATEUR      = -15.0;
	protected final static double ASTRONOMICAL = -18.0;
        private static LanguagePack Phrases=new LanguagePack();

	// MATHEMATICAL FUNCTIONS
	// OVERRIDDEN TRIGONOMETRIC FUNCTIONS, used to work with degrees instead of radians
	private static double sind(double n)
	{
		return Math.sin( n * DEGRAD );
	}

	private static double cosd(double n)
	{
		return Math.cos( n * DEGRAD );
	}

	private static double tand(double n)
	{
		return Math.tan( n * DEGRAD );
	}

	private static double atand(double n)
	{
		return ( RADEG * Math.atan(n) );
	}

	private static double asind(double n)
	{
		return ( RADEG * Math.asin(n) );
	}

	private static double acosd(double n)
	{
		return ( RADEG * Math.acos(n) );
	}

	private static double atan2d(double n1, double n2)
	{
		return ( RADEG * Math.atan2(n1, n2) );
	}

	// REDUCES AN ANGLE TO WITHIN ONE REVOULTION (360 DEGREES)
	private static double revolution(double angle)
	{
		return ( angle - 360.0 * Math.floor( angle * INV360 ) );
	}

	// REDUCES A GIVEN ANGLE TO BETWEEN +180 DEG AND -180 DEG
	private static double rev180(double x)
	{
		return ( x - 360.0 * Math.floor( x * INV360 + 0.5 ) );
	}

	// THE NUMBER OF DAYS SINCE (OR, BEFORE)
	// JANUARY 0, 2000 ****GREGORIAN****, FOR TECHNICAL REASONS
	// WHICH, IN ANY CASE, IS DAY 2451544
	private static long daysSinceJan0(long jday)
	{
		return jday - 2451544;
	}

	/******************** ASTRONOMICAL FUNCTIONS ************************/
	// GMST0:: computes the Greenwich Mean Siderial Time for any date
	// PARAMETERS:: A double REPRSENTING A DATE AND LOCATION WITH RESPECT TO GREENWICH, ENGLAND
	// RETURNS:: A double WITH THE GMST
	private static double GMST0(double d)
	{
		double sidtime0 = revolution( ( 180.0 + 356.0470 + 282.9404 ) + ( 0.9856002585 + 4.70935E-5 ) * d );
		return sidtime0;
	}

	// sun_ra_dec:: COMPUTES THE SUN'S RIGHT ASCENSION AND DECLINATION
	// PARAMETERS:: A double REPRESENTING A DATE AND LOCATION WITH RESPECT TO GREENWICH, ENGLAND
	// RETURNS:: A double[] WITH THE RIGHT ASCENSION and DECLINATION
	private static double[] sun_ra_dec(double d)
	{
		// Compute Sun's ecliptical coordinates 
		double[] rlon = sunpos(d);
		double r = rlon[0];
		double lon = rlon[1];

		// Compute ecliptic rectangular coordinates (z=0) 
		double x = r * cosd(lon);
		double y = r * sind(lon);

		// Compute obliquity of ecliptic (inclination of Earth's axis) 
		double obl_ecl = 23.4393 - 3.563E-7 * d;

		// Convert to equatorial rectangular coordinates - x is unchanged 
    		double z = y * sind(obl_ecl);
		y *= cosd(obl_ecl);

		// Convert to spherical coordinates 
		double RA  = atan2d( y, x );
		double dec = atan2d( z, Math.sqrt( x * x + y * y ) );

		double[] RA_dec = {RA, dec};
		return RA_dec;
	}

	// sunpos:: Computes the Sun's ecliptic longitude and distance
	// at an instant given in d, number of days since 2000 Jan 0.0. gregorian
	// PARAMETERS:: ditto
	// RETURNS:: ecliptic longitude and distance True_solar_longitude, Solar_distance
	private static double[] sunpos(double d)
	{
		// Mean anomaly of the Sun 
		// Mean longitude of perihelion 
		// Note: Sun's mean longitude = M + w 
		// Eccentricity of Earth's orbit 
		// Eccentric anomaly 
		// x, y coordinates in orbit 
		// True anomaly 

		double Mean_anomaly_of_sun = revolution( 356.0470 + 0.9856002585 * d );
		double Mean_longitude_of_perihelion = 282.9404 + 4.70935E-5 * d;
		double Eccentricity_of_Earth_orbit  = 0.016709 - 1.151E-9 * d;

		// Compute true longitude and radius vector 
		double Eccentric_anomaly = Mean_anomaly_of_sun + Eccentricity_of_Earth_orbit * RADEG * sind(Mean_anomaly_of_sun) * ( 1.0 + Eccentricity_of_Earth_orbit * cosd(Mean_anomaly_of_sun) );
		double x = cosd(Eccentric_anomaly) - Eccentricity_of_Earth_orbit;
		double y = Math.sqrt( 1.0 - Eccentricity_of_Earth_orbit * Eccentricity_of_Earth_orbit ) * sind(Eccentric_anomaly);

		double Solar_distance = Math.sqrt( x * x + y * y );    // Solar distance
		double True_anomaly = atan2d( y, x );               // True anomaly

		double True_solar_longitude = True_anomaly + Mean_longitude_of_perihelion;    // True solar longitude

		if ( True_solar_longitude >= 360.0 )
		{
			True_solar_longitude -= 360.0;    // Make it 0..360 degrees
		}

		double[] retval = {Solar_distance, True_solar_longitude};
		return retval;
	}

	// INTERNAL METHOD TO COMPUTE SUNRISE AND SUNSET
	// PARAMETERS: A double WITH THE NUMBER OF DAYS SINCE JAN 200 0.0.0, GREGORIAN
	// A DOUBLE WITH THE LONGITUDE WITH RESPECT TO GREENWICH, ENGLAND,
	// A DOUBLE WITH THE LATITUDE WITH RESPECT TO THE EQUATOR
	// A DOUBLE WITH THE DESIRED ALTITUDE OF SUNSET (OBSERVED, CIVIL, NAUTICAL, ETC.)
	private static double[] sun_rise_set(double d, double lon, double lat, double altit)
	{
		double sidtime = revolution( GMST0(d) + 180.0 + lon );

		double sRAsdec[] = sun_ra_dec(d);
		double tsouth  = 12.0 - rev180( sidtime - sRAsdec[0] ) / 15.0;
		double sradius = 0.2666 / sRAsdec[0];

		if (upper_limb)
		{
			altit -= sradius;
		}

		// Compute the diurnal arc that the Sun traverses to reach 
		// the specified altitude altit: 

		double cost = ( sind(altit) - sind(lat) * sind(sRAsdec[1]) ) / ( cosd(lat) * cosd(sRAsdec[1]) );
		double t;
		if ( cost >= 1.0 )
		{
		        // "Sun never rises!!\n";
			t = 0.0;    // Sun always below altit
		}
		else if ( cost <= -1.0 )
		{
			// "Sun never sets!!\n";
			t = 12.0;    // Sun always above altit
		}
		else
		{
			t = acosd(cost) / 15.0;    // The diurnal arc, hours
		}

		// Store rise and set times - in hours UT 

		double hour_rise_ut = tsouth - t;
		double hour_set_ut  = tsouth + t;
		double[] retval = {hour_rise_ut, hour_set_ut};
		return retval;
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
		double d = (double)daysSinceJan0(date.getJulianDay()) + 0.5 - lon / 360.0;
		double[] hours = sun_rise_set(d, lon, lat, alt);
		
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

	protected static String[] getSunriseSunsetString(JDate date, String lon, String lat, String tzone)
	{
		return getSunriseSunsetString(date, (double)Double.parseDouble(lon), (double)Double.parseDouble(lat), (int)Integer.parseInt(tzone));
	}
			

	protected static String[] getSunriseSunsetString(JDate date, double lon, double lat, int tzone)
	{
		double[] raw = getSunriseSunset(date, lon, lat, tzone, false, -0.833);
		String[] out = new String[2];
                

		// NOW, TAKE THE RAW INPUT AND PARSE IT TO HOURS / MINUTES
		for (int i = 0; i < 2; i++)
		{
			String Format=(String)Phrases.Phrases.get("TimeF");
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
                        //Changed to this by Y.S. to interationalised it.
                        if (StringOp.dayInfo.get("Ideographic").equals("1"))
                        {
                            RuleBasedNumber convertN=new RuleBasedNumber();
                            Format=Format.replace("HH", convertN.getFormattedNumber(hour));
                               Format=Format.replace("MM", convertN.getFormattedNumber(minute));

                        }
                        else
                        {
                        Format=Format.replace("HH", Integer.toString(hour));
                        Format=Format.replace("MM", Integer.toString(minute));
                        }
                        out[i]=Format;
		}

		return out;
	}
}
