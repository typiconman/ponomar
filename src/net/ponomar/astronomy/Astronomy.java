package net.ponomar.astronomy;

import java.util.LinkedHashMap;

import net.ponomar.calendar.JDate;
import net.ponomar.internationalization.LanguagePack;
 
 
 

/***********************************************************
 * 
 * Copyright 2012 Yuri Shardt This creates the astronomy package for the Ponomar
 * project. In due course it will combine the results from sunrise and add
 * additional information, such as moon phases, moon rise, etc Sunrise will then
 * only deal with the display issues. Astronomical functions taken from
 * Sunrise.java and credit should be also given to appropriate people
 * there.
 * 
 * THANK YOU TO Ron Hill, Robert Creager, Joshua Hoblitt, Chris Phillips, Brian
 * D Foy, Paul Schlyer of Stockholm, Sweden, and others who worked on
 * Astro::Sunrise PERL MODULE
 * 
 * CODE CONVERTED FROM PERL TO JAVA BY ALEKSANDR ANDREEV AS PART OF THE PONOMAR
 * PROJECT CERTAIN FEATURES OF PERL CODE OMITTED FOR EASE AND BREVITY (C) 2006
 * ALEKSANDR ANDREEV.
 * 
 *  (C) 2006 ALEKSANDR ANDREEV.
 *   Here is the copyright information provided by Paul Schlyer:
 * 
 * Here is the copyright information provided by Paul Schlyer:
 * 
 * Written as DAYLEN.C, 1989-08-16
 * 
 * Modified to SUNRISET.C, 1992-12-01
 * 
 * (c) Paul Schlyter, 1989, 1992
 * 
 * Released to the public domain by Paul Schlyter, December 1992
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 ***********************************************************/

public class Astronomy
{
	
	// CONSTANTS
	private static final double INV360 = (1.0 / 360.0);
	private static final double RADEG  = (180.0 / Math.PI);
	private static final double DEGRAD = (Math.PI / 180.0);
	private static final boolean UPPER_LIMB = true;
	protected static final double DEFAULT      = -0.833;
	protected static final double CIVIL        = -6.0;
	protected static final double NAUTICAL     = -12.0;
	protected static final double AMATEUR      = -15.0;
	protected static final double ASTRONOMICAL = -18.0;
        

	// MATHEMATICAL FUNCTIONS
	// OVERRIDDEN TRIGONOMETRIC FUNCTIONS, used to work with degrees instead of radians
	private static double sind(double n)
	{
		return Math.sin( n * DEGRAD );
	}

	static double cosd(double n)
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
	static double revolution(double angle)
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
	static long daysSinceJan0(long jday)
	{
		return jday - 2451544;
	}

	/******************** ASTRONOMICAL FUNCTIONS ************************/
	// computeGreenwichMeanSiderialTime:: computes the Greenwich Mean Siderial Time for any date
	// PARAMETERS:: A double REPRESENTING A DATE AND LOCATION WITH RESPECT TO GREENWICH, ENGLAND
	// RETURNS:: A double WITH THE GMST
	private static double computeGreenwichMeanSiderialTime(double d)
	{
		return revolution( ( 180.0 + 356.0470 + 282.9404 ) + ( 0.9856002585 + 4.70935E-5 ) * d );
	}

	// computeSunRightAscensionDeclination:: COMPUTES THE SUN'S RIGHT ASCENSION AND DECLINATION
	// PARAMETERS:: A double REPRESENTING A DATE AND LOCATION WITH RESPECT TO GREENWICH, ENGLAND
	// RETURNS:: A double[] WITH THE RIGHT ASCENSION and DECLINATION
	private static double[] computeSunRightAscensionDeclination(double d)
	{
		// Compute Sun's ecliptical coordinates
		double[] rlon = computeSunPosition(d);
		double r = rlon[0];
		double lon = rlon[1];

		// Compute ecliptic rectangular coordinates (z=0)
		double x = r * cosd(lon);
		double y = r * sind(lon);

		// Compute obliquity of ecliptic (inclination of Earth's axis)
		double obliquity = 23.4393 - 3.563E-7 * d;

		// Convert to equatorial rectangular coordinates - x is unchanged
    		double z = y * sind(obliquity);
		y *= cosd(obliquity);

		// Convert to spherical coordinates
		double rightAscension  = atan2d( y, x );
		double declination = atan2d( z, Math.sqrt( x * x + y * y ) );

		return new double[]{rightAscension, declination};
	}

	// computeSunPosition:: Computes the Sun's ecliptic longitude and distance
	// at an instant given in d, number of days since 2000 Jan 0.0. gregorian
	// PARAMETERS:: ditto
	// RETURNS:: ecliptic longitude and distance True_solar_longitude, Solar_distance
	public static double[] computeSunPosition(double d)
	{
		// Mean anomaly of the Sun
		// Mean longitude of perihelion
		// Note: Sun's mean longitude = M + w
		// Eccentricity of Earth's orbit
		// Eccentric anomaly
		// x, y coordinates in orbit
		// True anomaly

		final double meanAnomalyOfSun = revolution( 356.0470 + 0.9856002585 * d );
		final double meanLongitudeOfPerihelion = 282.9404 + 4.70935E-5 * d;
		double eccentricityOfEarthOrbit  = 0.016709 - 1.151E-9 * d;

		// Compute true longitude and radius vector
		double eccentricAnomaly = meanAnomalyOfSun + eccentricityOfEarthOrbit * RADEG * sind(meanAnomalyOfSun) * ( 1.0 + eccentricityOfEarthOrbit * cosd(meanAnomalyOfSun) );
		double x = cosd(eccentricAnomaly) - eccentricityOfEarthOrbit;
		double y = Math.sqrt( 1.0 - eccentricityOfEarthOrbit * eccentricityOfEarthOrbit ) * sind(eccentricAnomaly);

		double solarDistance = Math.sqrt( x * x + y * y );
		double trueAnomaly = atan2d( y, x ); 

		double trueSolarLongitude = trueAnomaly + meanLongitudeOfPerihelion;

		if ( trueSolarLongitude >= 360.0 )
		{
			trueSolarLongitude -= 360.0;    // Make it 0..360 degrees
		}

		return new double[]{solarDistance, trueSolarLongitude};
	}

	// INTERNAL METHOD TO COMPUTE SUNRISE AND SUNSET
	// PARAMETERS: A double WITH THE NUMBER OF DAYS SINCE JAN 200 0.0.0, GREGORIAN
	// A DOUBLE WITH THE LONGITUDE WITH RESPECT TO GREENWICH, ENGLAND,
	// A DOUBLE WITH THE LATITUDE WITH RESPECT TO THE EQUATOR
	// A DOUBLE WITH THE DESIRED ALTITUDE OF SUNSET (OBSERVED, CIVIL, NAUTICAL, ETC.)
	protected static double[] computeSunriseSunset(double d, double lon, double lat, double altit)
	{
		double sidtime = revolution( computeGreenwichMeanSiderialTime(d) + 180.0 + lon );

		double[] sRAsdec = computeSunRightAscensionDeclination(d);
		double tsouth  = 12.0 - rev180( sidtime - sRAsdec[0] ) / 15.0;
		double sradius = 0.2666 / sRAsdec[0];

		if (UPPER_LIMB)
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

		double sunriseHourUT = tsouth - t;
		double sunsetHourUT  = tsouth + t;
		return new double[]{sunriseHourUT, sunsetHourUT};
	}
        public double lunarlong(long day){
            //Computes the apparant lunar longitude based on the method in Meeus, pp. 337-343
            //day must be in the Gregorian calendar
            //Formula does not work far into the future or past due to issues with dynamical time
            //System.out.println("day = " +daysSinceJan0(day));
            double days=daysSinceJan0(day);
            double t=days/36525;
            //System.out.println(T);
            double lPrime=revolution(218.3164477+481267.88123421*t-0.0015786*t*t+t*t*t/538841-t*t*t*t/65194000);
            double d=revolution(297.8501921+445267.1114034*t-0.0018819*t*t+t*t*t/545868-t*t*t*t/113065000);
            double mPrime=revolution(134.9633964+477198.8675055*t+0.0087414*t*t-t*t*t/69699-t*t*t*t/14712000);
            double e = 1-0.002516*t-0.0000074*t*t;
            //Only the first four additive terms considered
            double additive=6288774*sind(mPrime)+1274027*e*sind(2*d-mPrime)+658314*sind(2*d)+213618*sind(2*mPrime);
            //Action of Venus, Jupiter, or flattening ignored.

			return revolution(lPrime+additive/1000000);

        }
        public double solarlong(long day){

            double[] results=computeSunPosition(day);

            double days=daysSinceJan0(day);
            double t=days/36525;

            double l0=revolution(280.46646+36000.76983*t+0.0003032*t*t);
            double m=revolution(357.52911+35999.05029*t-0.0001537*t*t);
            double e=0.016708634-0.000042037*t-0.0000001267*t*t;
            double c=(1.914602-0.004817*t-0.000014*t*t)*sind(m)+(0.019993-0.000101*t)*sind(2*m)+0.000289*sind(3*m);
            double l=revolution(l0+c);
            l=revolution(l-0.00569-0.00478*sind(revolution(125.04-1934.136*t)));
            //System.out.println("Solar long = " + results[1] + "+ Meeus's Method; "+L);
            return l;
        }
        public double lunarage(long day){
           //System.out.println("Lunar Age = " + revolution(lunarlong(day)-solarlong(day)));
            return revolution(lunarlong(day)-solarlong(day));

        }
        public String lunarphase(long day, LinkedHashMap<String, Object> dayInfo){
            /*Determine the lunar phase given various for the given JDE day;
             * Assumes that the given day is midnight local time.
             * */
            double todaysAge=lunarage(day);
            double tomorrowsAge=lunarage(day+1);
            String phase="Error";
         LanguagePack text = new LanguagePack(dayInfo);
         String[] phaseNames = text.obtainValues(text.getPhrases().get("Phases"));
            //Key="Phases" Value="New Moon/,Waxing Crescent/,First Quarter/,Waxing Gibbous/,Full Moon/,Waning Gibbous/,Third Quarter/,Waning Crescent" Comment="Lunar Phases" />
            if (todaysAge>tomorrowsAge){
                //This can only signify a new moon
                phase=phaseNames[0];
            }
            else if (todaysAge>0 && tomorrowsAge<90){
                //New Moon was yesterday
                phase=phaseNames[1];
            //System.out.println(todaysAge+" "+tomorrowsAge);
            }
            else if (todaysAge <= 90 && tomorrowsAge > 90){
                    //First Quarter occurred today
                    phase=phaseNames[2];}
            else if (todaysAge > 90 && tomorrowsAge < 180){
                //Between first quarter and full moon
                phase=phaseNames[3];}
            else if(todaysAge <=180 && tomorrowsAge > 180){
                //Full moon
                phase=phaseNames[4];}
            else if (todaysAge > 180 && tomorrowsAge<275){
                //Between Full moon and last quarter
                phase=phaseNames[5];}
            else if (todaysAge<=275 && tomorrowsAge>275){
                //Last Quarter
                phase=phaseNames[6];
            }
            else if (todaysAge>275 && tomorrowsAge>todaysAge){
                //Between Last Quarter and new moon
                phase=phaseNames[7];
            //System.out.println(todaysAge+" "+tomorrowsAge);
            }
            



             return phase;
        }
        public String lunarphaseJulian(JDate day){
            /*Determine the lunar phase for the Julian calendar;
             * Assumes that the given day is midnight local time.
             * */
            String phase="Error";
            /*int Golden=(int) year%30+1;
            int epact=11*(Golden-1)%30;
            //The epact gives the lunar age on March 22. Therefore to determine the
            //age on other days, requires us to determine the number of days to March 22
            //and then divide by an appropriate factor 29.5 and take the integer component.

            int difference=(day-JDate(day.getYear(),3,22));
            int epactV=epact+difference;
            double todaysAge=epactV-29.5*Math.floor(epactV/29.5); //Not strictly correct, but will do right now.
            double tomorrowsAge=35;

            
         LanguagePack Text = new LanguagePack();
         String[] PhaseNames = Text.obtainValues((String) Text.Phrases.get("Phases"));
            //Key="Phases" Value="New Moon/,Waxing Crescent/,First Quarter/,Waxing Gibbous/,Full Moon/,Waning Gibbous/,Third Quarter/,Waning Crescent" Comment="Lunar Phases" />
            if (todaysAge>tomorrowsAge){
                //This can only signify a new moon
                phase=PhaseNames[0];
            }
            else if (todaysAge>0 && tomorrowsAge<90){
                //New Moon was yesterday
                phase=PhaseNames[1];
            //System.out.println(todaysAge+" "+tomorrowsAge);
            }
            else if (todaysAge <= 90 && tomorrowsAge > 90){
                    //First Quarter occurred today
                    phase=PhaseNames[2];}
            else if (todaysAge > 90 && tomorrowsAge < 180){
                //Between first quarter and full moon
                phase=PhaseNames[3];}
            else if(todaysAge <=180 && tomorrowsAge > 180){
                //Full moon
                phase=PhaseNames[4];}
            else if (todaysAge > 180 && tomorrowsAge<275){
                //Between Full moon and last quarter
                phase=PhaseNames[5];}
            else if (todaysAge<=275 && tomorrowsAge>275){
                //Last Quarter
                phase=PhaseNames[6];
            }
            else if (todaysAge>275 && tomorrowsAge>todaysAge){
                //Between Last Quarter and new moon
                phase=PhaseNames[7];
            //System.out.println(todaysAge+" "+tomorrowsAge);
            }*/

             return phase;
        }

	public static void main(String[] argz) {
		LinkedHashMap<String, Object> dayInfo = new LinkedHashMap<>();
		dayInfo.put("LS", "en/");
		Astronomy test = new Astronomy();

		for (int k = 0; k < 30; k++) {
			System.out.println("phase = " + test.lunarphase((long) 1444534.0 + k, dayInfo));
		}
	}

}


