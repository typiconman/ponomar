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
        private StringOp Analyse=new StringOp();

        protected PCalendar(JDate date1, String calendar,OrderedHashtable dayInfo){
            date=date1;
            type=calendar;
            Analyse.dayInfo=dayInfo;
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
            if (type.equals(gregorian) && (y > 1582 || y == 1582 && m > 10 || y == 1582 && m == 10 && d > 14)){
                //Note if the user specifies the Gregorian calendar before its existence, it will not be honoured.
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
            PCalendar cutoff=new PCalendar(new JDate(9,1,date.getYear()),julian,Analyse.dayInfo.clone());
            double year=date.getYear();
            double AM=5508- Math.floor(difference / 365) + year;
            if (JulianDay()>=cutoff.JulianDay()){
                AM=AM+1;
            }
            return (int)AM;
        }
        public double getDiff(){
            PCalendar test=new PCalendar(new JDate(date.getMonth(),date.getDay(),date.getYear()),julian,Analyse.dayInfo.clone());
            PCalendar test2=new PCalendar(new JDate(date.getMonth(),date.getDay(),date.getYear()),gregorian,Analyse.dayInfo.clone());
            return (test.JulianDay()-test2.JulianDay());
        }

        public Object clone()
	{
		return new PCalendar(date, type,Analyse.dayInfo.clone());
	}
        //RETURNS THE YEAR, MONTH, DAY IN THE JULIAN CALENDAR
        private int[] getJulianDate(){
            //We wish to obtain the Julian calendar parameters
            Double JD=getJulianDay()+0.5;
            int A=integerPart(JD);

            int B=A + 1524;
            int C=integerPart((B-122.1) / 365.25);
            int D = integerPart((365.25*C));
            int E = integerPart ((B-D)/30.6001);

            int[] values=new int[3];
            values[2]=B-D-integerPart(30.6001*E);
            if (E<14){
                values[1]=E-1;
            }else{
                values[1]=E-13;
            }
            if (values[1]>2){
                values[0]=C-4716;
            }else{
                values[0]=C-4715;
            }

            return values;
        }
        //RETURNS THE YEAR, MONTH, DAY IN THE GREGORIAN CALENDAR
        private int[] getGregorianDate(){
            //We wish to obtain the Gregorian calendar parameters
            Double JD=getJulianDay()+0.5;
            int Z=integerPart(JD);
            int A=Z;
            //Again we will not honour Gregorian dates outside of the implmentation period!
            if (Z>2299161){
            int alpha =integerPart((Z-1867216.25)/36524.25);
            A=Z+1+alpha-integerPart(alpha/4);
            }

            int B=A + 1524;
            int C=integerPart((B-122.1) / 365.25);
            int D = integerPart((365.25*C));
            int E = integerPart ((B-D)/30.6001);
            
            int[] values=new int[3];
            values[2]=B-D-integerPart(30.6001*E);
            if (E<14){
                values[1]=E-1;
            }else{
                values[1]=E-13;
            }
            if (values[1]>2){
                values[0]=C-4716;
            }else{
                values[0]=C-4715;
            }

            return values;
        }
        private int integerPart(double number){
            return (int) number - (int) (number % 1);
        }
        //RETURNS THE JULIAN CALENDAR YEAR
        public int getYearJ(){

            return getJulianDate()[0];
        }
        public int getMonthJ(){

            return getJulianDate()[1];
        }
        public int getDayJ(){

            return getJulianDate()[2];
        }

        //RETURNS THE GREGORIAN CALENDAR YEAR (if < 1582/10/15, returns Julian Calendar Year)
        public int getYearG(){

            return getGregorianDate()[0];
        }
        public int getMonthG(){

            return getGregorianDate()[1];
        }
        public int getDayG(){

            return getGregorianDate()[2];
        }


        public static void main(String[] argz)
	{
            OrderedHashtable dayInfo=new OrderedHashtable();
            dayInfo.put("LS", "en/");
            PCalendar test=new PCalendar(new JDate(10,4,1957),gregorian,dayInfo);
            System.out.println(test.JulianDay());
            test=new PCalendar(new JDate(1,27,333),julian,dayInfo);
            System.out.println(test.JulianDay());
            test=new PCalendar(new JDate(1,14,2012),julian,dayInfo);
            PCalendar test2=new PCalendar(new JDate(1,14,2012),gregorian,dayInfo);
            System.out.println(test.JulianDay()-test2.JulianDay());

            System.out.println("Anno Mundi: "+test.getAM());
            test=new PCalendar(new JDate(9,14,2012),julian,dayInfo);
            System.out.println("Anno Mundi: "+test.getAM());
            test=new PCalendar(new JDate(9,14,458973),gregorian,dayInfo);
            System.out.println("Anno Mundi: "+test.getAM());
	}

}


