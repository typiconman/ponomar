package Ponomar;

//Copyright 2012 Yuri Shardt
//This creates the astronomy package for the Ponomar project. In due course it will combine
//the results from sunrise and add additional information, such as moon phases, moon rise, etc
//Sunrise will then only deal with the display issues.
//Astronomical functions taken from the Ponomar/Sunrise.java and credit should be also given to appropriate people there.

class Astronomy
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
	public static double[] sunpos(double d)
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
        public double lunarlong(long day){
            //Computes the apparant lunar longitude based on the method in Meeus, pp. 337-343
            //day must be in the Gregorian calendar
            //Formula does not work far into the future or past due to issues with dynamical time
            //System.out.println("day = " +daysSinceJan0(day));
            double days=daysSinceJan0(day);
            double T=days/36525;
            //System.out.println(T);
            double Lprime=revolution(218.3164477+481267.88123421*T-0.0015786*T*T+T*T*T/538841-T*T*T*T/65194000);
            double D=revolution(297.8501921+445267.1114034*T-0.0018819*T*T+T*T*T/545868-T*T*T*T/113065000);
            double Mprime=revolution(134.9633964+477198.8675055*T+0.0087414*T*T-T*T*T/69699-T*T*T*T/14712000);
            double E = 1-0.002516*T-0.0000074*T*T;
            //Only the first four additive terms considered
            double additive=6288774*sind(Mprime)+1274027*E*sind(2*D-Mprime)+658314*sind(2*D)+213618*sind(2*Mprime);
            //Action of Venus, Jupiter, or flattening ignored.
            double longitude=revolution(Lprime+additive/1000000);

            //System.out.println("Lunar Long=" + longitude);


            return longitude;

        }
        public double solarlong(long day){

            double[] results=sunpos(day);

            double days=daysSinceJan0(day);
            double T=days/36525;

            double L0=revolution(280.46646+36000.76983*T+0.0003032*T*T);
            double M=revolution(357.52911+35999.05029*T-0.0001537*T*T);
            double e=0.016708634-0.000042037*T-0.0000001267*T*T;
            double C=(1.914602-0.004817*T-0.000014*T*T)*sind(M)+(0.019993-0.000101*T)*sind(2*M)+0.000289*sind(3*M);
            double L=revolution(L0+C);
            L=revolution(L-0.00569-0.00478*sind(revolution(125.04-1934.136*T)));
            //System.out.println("Solar long = " + results[1] + "+ Meeus's Method; "+L);
            return L;
        }
        public double lunarage(long day){
           //System.out.println("Lunar Age = " + revolution(lunarlong(day)-solarlong(day)));
            return revolution(lunarlong(day)-solarlong(day));

        }
        public String lunarphase(long day, OrderedHashtable dayInfo){
            /*Determine the lunar phase given various for the given JDE day;
             * Assumes that the given day is midnight local time.
             * */
            double todaysAge=lunarage(day);
            double tomorrowsAge=lunarage(day+1);
            String phase="Error";
         LanguagePack Text = new LanguagePack(dayInfo);
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
        public static void main(String[] argz)
	{
            OrderedHashtable dayInfo=new OrderedHashtable();
            dayInfo.put("LS", "en/");
            Astronomy test = new Astronomy();
                
                for(int k=0;k<30;k++){
                System.out.println("phase = " + test.lunarphase((long)1444534.0+k,dayInfo));
                }
	}

}


