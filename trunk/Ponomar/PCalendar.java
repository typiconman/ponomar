package Ponomar;

//Copyright 2012 Yuri Shardt
//This creates a calendar objec that will store the relate information about manipulating and dealing with different calendars and standards.
//It uses the JDate object to store the Julian Date

class PCalendar implements Cloneable
{
	
	// CONSTANTS
	private double difference; //Stores the associated difference in days between Julian and Gregorian calendars
        private String type; //Stores which calendar I am dealing with. At present only Gregorian and Julian.
        private JDate date; //Stores the date associated with the given calendar.
        public final static String julian="julian"; //Fixes the spelling of the Julian option
        public final static String gregorian="gregorian"; //Fixes the spelling of the Gregorian option

        protected PCalendar(JDate date1, String calendar){
            date=date1;
            type=calendar;
        }
        //Computes the Julian Day given the calendar type
        private double JulianDay(){
            int y=date.getYear();
            int m=date.getMonth();
            int d=date.getDay();
            if (m<3){
                m=m+12;
                y=y-1;
            }
            double A=Math.floor(y/100);
            double B=0;
            if (type.equals(gregorian)){
                B=2-A+Math.floor(A/4);
            }

            return Math.floor(365.25*(y+4716))+Math.floor(30.6001*(m+1))+d+B-1524.5;
        }
        public double getJulianDay(){
            return JulianDay();
        }

        public int getAM(){
            //returns the corresponding anno mundi given the date.
            difference=0;
            if (type.equals(gregorian)){
                difference=getDiff();
            }
            PCalendar cutoff=new PCalendar(new JDate(9,1,date.getYear()),julian);
            double year=date.getYear();
            double AM=5508- Math.floor(difference / 365) + year;
            if (JulianDay()>cutoff.JulianDay()){
                AM=AM+1;
            }
            return (int)AM;
        }
        public double getDiff(){
            PCalendar test=new PCalendar(new JDate(date.getMonth(),date.getDay(),date.getYear()),julian);
            PCalendar test2=new PCalendar(new JDate(date.getMonth(),date.getDay(),date.getYear()),gregorian);
            return (test.JulianDay()-test2.JulianDay());
        }

        public Object clone()
	{
		return new PCalendar(date, type);
	}

        public static void main(String[] argz)
	{
            StringOp.dayInfo=new OrderedHashtable();
            StringOp.dayInfo.put("LS", "en/");
            PCalendar test=new PCalendar(new JDate(10,4,1957),gregorian);
            System.out.println(test.JulianDay());
            test=new PCalendar(new JDate(1,27,333),julian);
            System.out.println(test.JulianDay());
            test=new PCalendar(new JDate(1,14,2012),julian);
            PCalendar test2=new PCalendar(new JDate(1,14,2012),gregorian);
            System.out.println(test.JulianDay()-test2.JulianDay());

            System.out.println("Anno Mundi: "+test.getAM());
            test=new PCalendar(new JDate(9,14,2012),julian);
            System.out.println("Anno Mundi: "+test.getAM());
            test=new PCalendar(new JDate(9,14,458973),gregorian);
            System.out.println("Anno Mundi: "+test.getAM());
	}

}


