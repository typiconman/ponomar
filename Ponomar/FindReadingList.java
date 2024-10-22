package Ponomar;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.lang.Math;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.MutableAttributeSet;

/***********************************************************************
Main.java :: MAIN MODULE FOR THE PONOMAR PROGRAM.
THIS MODULE CONSTITUTES THE PRIMARY PONOMAR GUI AND CENTRE OF THE PROGRAM.
TO START THE PROGRAM, INVOKE main(String[]) OF THIS CLASS.
OUTPUTS RELEVANT INFORMATION FOR EACH DAY, WITH LINKS TO DETAILED INFO.

Main.java is part of the Ponomar program.
Copyright 2006, 2007, 2008, 2009, 2010, 2012 Aleksandr Andreev and Yuri Shardt.
Corresponding e-mail aleksandr.andreev@gmail.com

Ponomar is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

While Ponomar is distributed in the hope that it will be useful,
it comes with ABSOLUTELY NO WARRANTY, without even the implied warranties of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for details.
 ***********************************************************************/
public class FindReadingList {
    // First, some relevant constants

    private final static String configFileName = "ponomar.config"; // CONFIGURATIONS FILE
    //private final static String generalFileName="Ponomar/xml/";
    private final static String triodionFileName = "xml/triodion/";   // TRIODION FILE
    private final static String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
    private static String newline = "\n";
    // Elements of the interface
    JDate2 today; 		// "TODAY" (I.E. THE DATE WE'RE WORKING WITH
    private JDate2 pascha; 		// THIS YEAR'S PASCHA
    private JDate2 pentecost; 	// THIS YEAR'S PENTECOST
    private Stack fastInfo;		// CONTAINS A VECTOR OF THE FASTING INFORMATION FOR TODAY, WHICH IS LATER PASSED TO CONVOLVE()
    private OrderedHashtable readings;	// CONTAINS TODAY'S SCRIPTURE READING
    private String output;  	// TODAY'S CALENDAR OUTPUT
    private Boolean inited = false; // PREVENTS MULTIPLE READING OF XML FILES ON LAUNCH
    private GospelSelector GospelLocation;		//THE GOSPEL SELECTOR OBJECT
    private String GLocation;					//STORES THE PATH (FOLDER) TO THE APPROPRIATE GOSPEL READING LOCATION FILES
    private LanguageSelector LanguageLocation;
    //private String LLocation;
    //MY ATTEMPT AT SORTING THE READINGS FOR THE LITURGY 2008/05/19 n.s. YURI SHARDT
    private OrderedHashtable PentecostarionS;		//CONTAINS THE PENTECOSTARION READINGS (SEQUENTIAL (rjadovoje) READINGS!)
    private OrderedHashtable MenalogionS;		//CONTAINS THE MENALOGION READINGS, EXCLUDING ANY FLOATERS
    private OrderedHashtable FloaterS;			//CONTAINS THE FLOATER READINGS.
    private OrderedHashtable[] ReadScriptures;
    private LanguagePack Phrases;
    private static boolean read = false;		//DETERMINES WHICH LANGUAGE WILL BE READ
    private String RSep = new String();
    private String CSep = new String();
    private String Colon = new String();
    private String Ideographic = new String();
    private StringOp Analyse = new StringOp();
    private int ReligiousCal=0;
    //private GospelSelector Selector;
    Helpers findLanguage;

    // CONSTRUCTOR
    public FindReadingList(int year, int GS) {
        //super("Ponomar");

        ConfigurationFiles.Defaults = new OrderedHashtable();
        ConfigurationFiles.ReadFile();
	//DisplayCal=Integer.parseInt(ConfigurationFiles.Defaults.get("DisplayCalendar").toString());
	ReligiousCal=GS;//Integer.parseInt(ConfigurationFiles.Defaults.get("ReligiousCalendar").toString());
        LanguageLocation = new LanguageSelector(Analyse.dayInfo);

        Analyse.dayInfo.put("LS", LanguageLocation.getLValue());
        Phrases = new LanguagePack(Analyse.dayInfo);
        //Changing language storage format
        findLanguage = new Helpers(Analyse.dayInfo);

       
        RSep = " ";
        CSep = (String) Phrases.Phrases.get("CommSep");
        Colon = (String) Phrases.Phrases.get("Colon");
        Analyse.dayInfo.put("ReadSep", RSep);
        Analyse.dayInfo.put("Colon", Colon);
        Ideographic = (String) Phrases.Phrases.get("Ideographic");
        Analyse.dayInfo.put("Ideographic", Ideographic);
        //GospelLocation = new GospelSelector(Analyse.dayInfo);
        
        pascha = Paschalion.getPascha(year,ReligiousCal);
       pascha.addDays(134);
        JDate2 start=pascha;

        today = new JDate2(start.getMonth(), start.getDay(), start.getYear(),ReligiousCal);
        int nday = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear(),ReligiousCal));
        int ndayP = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() - 1,ReligiousCal));
        String outputE="Year: "+today.getYear();
        outputE+="\n";
        String outputG=outputE;
                        
        while (nday >= 134 || nday < -70)
        {
        outputE+=nday+" M"+today.getMonth()+"."+today.getDay()+" ";
        outputG+=nday+" M"+today.getMonth()+"."+today.getDay()+" ";
        int dow = today.getDayOfWeek();
        int doy = today.getDoy();
        nday = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear(),ReligiousCal));
        ndayP = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() - 1,ReligiousCal));
        //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
        int ndayF = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() + 1,ReligiousCal));
        
        Analyse.dayInfo.put("dow", dow);	// THE DAY'S DAY OF WEEK
        Analyse.dayInfo.put("doy", doy);	// THE DAY'S DOY (see JDate.java for specification)
        Analyse.dayInfo.put("nday", nday);	// THE NUMBER OF DAYS BEFORE (-) OR AFTER (+) THIS YEAR'S PASCHA
        Analyse.dayInfo.put("ndayP", ndayP);	// THE NUMBER OF DAYS AFTER LAST YEAR'S PASCHA
        Analyse.dayInfo.put("ndayF", ndayF);	// THE NUMBER OF DAYS TO NEXT YEAR'S PASCHA (CAN BE +ve or -ve).
        Analyse.dayInfo.put("GS", 1);
        
        //INTERFACE LANGUAGE
        Analyse.dayInfo.put("LS", LanguageLocation.getLValue());
        Analyse.dayInfo.put("Year", today.getYear());
        Analyse.dayInfo.put("dRank", 0); //The default rank for a day is 0. Y.S. 2010/02/01 n.s.
        Analyse.dayInfo.put("Ideographic", Ideographic);
        Analyse.dayInfo.put("isLeapYear",today.isLeapYear(today.getYear()) ? 1 : 0);

        readings = new OrderedHashtable();
        //MY ATTEMPT AT SORTING THE READINGS FOR THE LITURGY 2008/05/24 n.s. YURI SHARDT
		/*ReadScriptures = new OrderedHashtable[3];		//CONTAINS A SORTED ARRAY OF ALL THE READINGS
        ReadScriptures[0] = new OrderedHashtable();		//STORES THE PENTECOSTARION READINGS (SEQUENTIAL (rjadovoje) READINGS!)
        ReadScriptures[1] = new OrderedHashtable();		//CONTAINS THE MENALOGION READINGS, EXCLUDING ANY FLOATERS
        ReadScriptures[2] = new OrderedHashtable();		//CONTAINS THE FLOATER READINGS.
         */
        //TESTING THE LANGUAGE PACKS
        String rough = (String) Phrases.Phrases.get("1");
        String[] final1 = rough.split(",");
        //System.out.println(output);
        String filename = "";
        int lineNumber = 0;


        
        if (nday >= -70 && nday < 0) {
            filename = triodionFileName;
            lineNumber = Math.abs(nday);
        } else if (nday < -70) {
            // WE HAVE NOT YET REACHED THE LENTEN TRIODION
            filename = pentecostarionFileName;
            JDate2 lastPascha = Paschalion.getPascha(today.getYear() - 1,ReligiousCal);
            lineNumber = (int) JDate2.difference(today, lastPascha) + 1;
        } else {
            // WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
            filename = pentecostarionFileName;
            lineNumber = nday + 1;
        }

        filename += lineNumber >= 10 ? lineNumber : "0" + lineNumber; // CLEANED UP
        //System.out.println("++++++++++++++++++++++\n"+filename+"\n+++++++++++++++++++\n");
        //System.out.println("File name in Main: " + Analyse.dayInfo.get("LS").toString());
        Day PaschalCycle = new Day(filename, Analyse.dayInfo);

        // READ THE PENTECOSTARION / TRIODION INFORMATION

        /*
        for (Enumeration e = readings.enumerateKeys(); e.hasMoreElements(); )
        {
        String type = (String)e.nextElement();
        Vector vect = (Vector)readings.get(type);

        ReadScriptures[0].put(type, vect);
        }


        readings.clear();*/

        // GET THE MENAION DATA, THESE MAY BE INDEPENDENT OF THE GOSPEL READING IMPLEMENTATION, BUT WILL NOT BE SO IMPLEMENTED
        int m = today.getMonth();
        int d = today.getDay();

        filename = "xml/";
        filename += m < 10 ? "0" + m : "" + m;  // CLEANED UP
        filename += d < 10 ? "/0" + d : "/" + d; // CLEANED UP
        //filename += ".xml";
        Day SolarCycle = new Day(filename, Analyse.dayInfo);
        Analyse.dayInfo.put("dRank", Math.max(SolarCycle.getDayRank(), PaschalCycle.getDayRank()));
        output="";


            
        OrderedHashtable[] PaschalReadings = PaschalCycle.getReadings();
        //System.out.println("Length of Ordinary Readings="+PaschalReadings.length);

        OrderedHashtable[] MenaionReadings = SolarCycle.getReadings();
        Bible ShortForm = new Bible(Analyse.dayInfo);
        //System.out.println("First Paschal Reading is :"+PaschalReadings[0].get("Readings"));
        //System.out.println("First Menologion Reading is :"+MenaionReadings[0].get("Readings"));
        OrderedHashtable CombinedReadings = new OrderedHashtable();
        //for(int j=0;j<7;j++){
        for (int k = 0; k < MenaionReadings.length; k++) {
            OrderedHashtable Reading = (OrderedHashtable) MenaionReadings[k].get("Readings");
            OrderedHashtable Readings = (OrderedHashtable) Reading.get("Readings");
            for (Enumeration e = Readings.enumerateKeys(); e.hasMoreElements();) {
                String element1 = e.nextElement().toString();
                if (CombinedReadings.get(element1) != null) {
                    //Type of Reading already exists combine them
                    OrderedHashtable temp = (OrderedHashtable) CombinedReadings.get(element1);
                    Vector Readings2 = (Vector) temp.get("Readings");
                    Vector Rank = (Vector) temp.get("Rank");
                    Vector Tag = (Vector) temp.get("Tag");
                    Readings2.add(Readings.get(element1));
                    Rank.add(Reading.get("Rank"));
                    Tag.add(Reading.get("Name"));
                    temp.put("Readings", Readings2);
                    temp.put("Rank", Rank);
                    temp.put("Tag", Tag);
                    CombinedReadings.put(element1, temp);
                } else {
                    //Reading does not exist
                    Vector Readings2 = new Vector();
                    Vector Rank = new Vector();
                    Vector Tag = new Vector();
                    Readings2.add(Readings.get(element1));
                    Rank.add(Reading.get("Rank"));
                    Tag.add(Reading.get("Name"));
                    OrderedHashtable temp = new OrderedHashtable();
                    temp.put("Readings", Readings2);
                    temp.put("Rank", Rank);
                    temp.put("Tag", Tag);
                    CombinedReadings.put(element1, temp);
                }
            }
        }
        for (int k = 0; k < PaschalReadings.length; k++) {
            OrderedHashtable Reading = (OrderedHashtable) PaschalReadings[k].get("Readings");
            OrderedHashtable Readings = (OrderedHashtable) Reading.get("Readings");
            for (Enumeration e = Readings.enumerateKeys(); e.hasMoreElements();) {
                String element1 = e.nextElement().toString();
                if (CombinedReadings.get(element1) != null) {
                    //Type of Reading already exists combine them
                    OrderedHashtable temp = (OrderedHashtable) CombinedReadings.get(element1);
                    Vector Readings2 = (Vector) temp.get("Readings");
                    Vector Rank = (Vector) temp.get("Rank");
                    Vector Tag = (Vector) temp.get("Tag");
                    Readings2.add(Readings.get(element1));
                    Rank.add(Reading.get("Rank"));
                    Tag.add(Reading.get("Name"));
                    temp.put("Readings", Readings2);
                    temp.put("Rank", Rank);
                    temp.put("Tag", Tag);
                    CombinedReadings.put(element1, temp);
                } else {
                    //Reading does not exist
                    Vector Readings2 = new Vector();
                    Vector Rank = new Vector();
                    Vector Tag = new Vector();
                    Readings2.add(Readings.get(element1));
                    Rank.add(Reading.get("Rank"));

                    Tag.add(Reading.get("Name"));
                    OrderedHashtable temp = new OrderedHashtable();
                    temp.put("Readings", Readings2);
                    temp.put("Rank", Rank);
                    temp.put("Tag", Tag);
                    CombinedReadings.put(element1, temp);
                }
            }
        }
        //}
        boolean firstTime = true;
        for (Enumeration e = CombinedReadings.enumerateKeys(); e.hasMoreElements();) {
            //Temperary solution
            String element1 = e.nextElement().toString();
            OrderedHashtable temp = (OrderedHashtable) CombinedReadings.get(element1);
            Vector Readings = (Vector) temp.get("Readings");
            Vector Rank = (Vector) temp.get("Rank");
            Vector Tag = (Vector) temp.get("Tag");
            if (element1.equals("LITURGY")) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    output += RSep;
                }
                //Special case and consider it differently
                Vector epistle = new Vector();

                Vector gospel = new Vector();


                for (int j = 0; j < Readings.size(); j++) {
                    OrderedHashtable liturgy = (OrderedHashtable) Readings.get(j);
                    OrderedHashtable stepE = (OrderedHashtable) liturgy.get("apostol");
                    OrderedHashtable stepG = (OrderedHashtable) liturgy.get("gospel");

                    if (stepE != null) {
                        epistle.add(stepE.get("Reading").toString());
                    } else {
                        epistle.add("");
                    }
                    if (stepG != null) {
                        gospel.add(stepG.get("Reading").toString());
                    } else {
                        gospel.add("");
                    }


                }
                OrderedHashtable readingsA = new OrderedHashtable();

                if (!epistle.get(0).equals("")) {
                    readingsA.put("Readings", epistle);
                    readingsA.put("Rank", Rank);
                    readingsA.put("Tag", Tag);
                    //System.out.println(Tag);
                    //System.out.println("Hello World");
                    DivineLiturgy1 trial1 = new DivineLiturgy1(Analyse.dayInfo);
                    String type1 = (String) Phrases.Phrases.get("apostol");
                    outputE += trial1.Readings(readingsA, "apostol", today);
                    outputE += " \n";
                }
                if (!gospel.get(0).equals("")) {
                    readingsA.put("Readings", gospel);
                    readingsA.put("Rank", Rank);
                    readingsA.put("Tag", Tag);
                    String type1 = (String) Phrases.Phrases.get("gospel");
                    DivineLiturgy1 trial1 = new DivineLiturgy1(Analyse.dayInfo);
                    outputG += trial1.Readings(readingsA, "gospel", today)+" \n";
                }


                /*for (int j=0; j<Readings.size();j++){
                if (j!=0){
                output+=RSep;
                }
                String BibleText=epistle.get(j).toString();

                output+=ShortForm.getHyperlink(BibleText);

                if (Readings.size()>1){
                output+= Tag.get(j).toString();
                }
                }*/


                /*for (int j=0; j<Readings.size();j++){
                if (j!=0){
                output+=RSep;
                }
                String BibleText=gospel.get(j).toString();
                output+=ShortForm.getHyperlink(BibleText);

                if (Readings.size()>1){
                output+= Tag.get(j).toString();
                }
                }*/
                continue;

            }
        

            }//output += RSep;
        today.addDays(1);
        }

      
//Closed the while loop above
        String epistle="Epistle."+year+"."+ReligiousCal+".csv";
        String gospel="Gospel."+year+"."+ReligiousCal+".csv";
        
        try
        {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/Regression/"+epistle),"UTF8"));
            out.write(outputE);
            out.close();
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/Regression/"+gospel),"UTF8"));
            out.write(outputG);
            out.close();
        }
        catch(IOException e)
		{
			//CANNOT BE MULTILINGUAL
			System.out.println("There was a problem:" + e);
		}



    }


    public static void main(String[] argz) {
        int yearsJulian[]=new int[]{2487, 2288, 2209,2053,2211,2128,2056,2214,2131,2132,2486,2134,2135,2136,2149,2139,2152,2153,2507,2157,2063,2150,2151,2154,2155,2509,2253,2159,2160,2077,2078,2527,2176,
                    2177, 2083, 2254, 2087, 2525, 2100, 2174, 2175, 2004, 2179, 2001, 2192, 2193, 2099, 2196, 2197, 2103, 2094, 2117, 2023, 2205, 2279, 2282, 2021, 2202, 2203, 2025, 2122, 2123, 2118, 2383,
                    2222, 2060, 2084, 2535, 2091, 2456, 2095, 2457, 2024, 2027, 2028, 2031, 2035, 2064, 2085, 2104, 2105, 2351, 2142, 2143, 2230, 2407, 2162, 2250, 2294, 2223, 2242, 2243, 2247, 2347, 2021,
                    2453, 2199, 2302, 2303, 2322, 2271, 2283, 2287, 2291, 2488, 2267, 2275, 2307, 2338, 2295, 2315, 2331, 2335, 2439, 2423, 2443, 2467, 2427, 2447, 2491, 2511, 2515, 2163, 2089, 2065, 2119, 2095};
        int yearsGregorian[] = new int[]{1999, 2000,2001,2002,2003,2004,2005,2006,2007,2008,2009,2010,2011,2012,2014,2015,2017,2018,2019,2021,2022,2023,2026,2027,2030,2031,2032,2034,2035,2036,2037,2038,2039,2041,2042,2043,2046,2047,2048,2049,2050,2051,2054,2055,2056,2057,2058,2059,2061,2062,2063,2065,2066,2067,2070,2071,2074,2075,2076,2078,2079,2080,2082,2083,2089,2091,2092,2095,2098,2099,2112,2115,2119,2120,2123,2132,2136,2139,2140,2143,2147,2244,2265,2284,2285,2319,2390,2391,2478,2487,2494,2498,2599,2691,2699,2863,2867,2883,2887,2890,2894,2971,2982,2990,2991,2999,3134,3263,3275,3279,3283,3290,3783,3791,3891,4074,4183,4271,4287,4291,4463,4819,4839,4863,5279,5671,5783,6395,6483,7504,7599,8587,8739,16567,22267,23255,30095,31083,35795,35947};
        
      //  for (int j=0;j<yearsJulian.length;j++)
        //{
          //  System.out.println("Years Julian: "+yearsJulian[j]+"\n");
            //new FindReadingList(yearsJulian[j],0);
        //}
        
       for (int j=0;j<yearsGregorian.length;j++)
       {
            System.out.println("Years Gregorian: "+yearsGregorian[j]+"\n");
            new FindReadingList(yearsGregorian[j],1);
       }
    }
}
