package net.ponomar.readings;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.ponomar.astronomy.Paschalion;
import net.ponomar.calendar.JDate;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.parsing.Day;
import net.ponomar.parsing.DocHandler;
import net.ponomar.readings.utility.ReadingUtility;
import net.ponomar.utility.Helpers;
import net.ponomar.utility.OrderedHashtable;
import net.ponomar.utility.StringOp;

public abstract class Reading implements DocHandler {

	protected static final String CONFIG_FILENAME = "ponomar.config";
	protected static final String TRIODION_FILENAME = "xml/triodion/";
	protected static final String PENTECOSTARION_FILENAME = "xml/pentecostarion/";
	private static OrderedHashtable readings;
	private static OrderedHashtable pentecostarionS;
	private static OrderedHashtable menalogionS;
	private static OrderedHashtable floaterS;
	protected static OrderedHashtable information;
	private static String gLocation;
	protected static LanguagePack phrases;
	protected static String[] transferredDays;
	protected static String[] error;
	private static Helpers findLanguage;
	private static Vector dailyV = new Vector();
	private static Vector dailyR = new Vector();
	private static Vector dailyT = new Vector();
	private static Vector menaion2V = new Vector();
	private static Vector menaion2R = new Vector();
	private static Vector menaion2T = new Vector();
	private static Vector menaionV = new Vector();
	private static Vector menaionR = new Vector();
	private static Vector menaionT = new Vector();
	private static Vector suppressedV = new Vector();
	private static Vector suppressedR = new Vector();
	private static Vector suppressedT = new Vector();
	protected static OrderedHashtable tomorrowRead = new OrderedHashtable();
	protected static OrderedHashtable yesterdayRead = new OrderedHashtable();
	private static StringOp information3 = new StringOp();

	public Reading() {
		super();
	}
	
	@Override
	public void startDocument() throws Exception {
	}

	@Override
	public void endDocument() throws Exception {		
	}

	@Override
	public void endElement(String tag) throws Exception {
		
	}
	
    protected OrderedHashtable getReadings(JDate today, String readingType) {
        String filename = "";
        int lineNumber = 0;

        int nday = (int) JDate.difference(today, Paschalion.getPascha(today.getYear()));

        //I COPIED THIS FROM THE Main.java FILE BY ALEKS WITH MY MODIFICATIONS (Y.S.)
        //FROM HERE UNTIL
        if (nday >= -70 && nday < 0) {
            filename = TRIODION_FILENAME;
            lineNumber = Math.abs(nday);
        } else if (nday < -70) {
            // WE HAVE NOT YET REACHED THE LENTEN TRIODION
            filename = PENTECOSTARION_FILENAME;
            JDate lastPascha = Paschalion.getPascha(today.getYear() - 1);
            lineNumber = (int) JDate.difference(today, lastPascha) + 1;
        } else {
            // WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
            filename = PENTECOSTARION_FILENAME;
            lineNumber = nday + 1;
        }

        filename += lineNumber >= 10 ? lineNumber + "" : "0" + lineNumber + ""; // CLEANED UP
        // READ THE PENTECOSTARION / TRIODION INFORMATION
        Day checkingP = new Day(filename,getInformation3().getDayInfo());


        //ADDED 2008/05/19 n.s. Y.S.
        //COPYING SOME READINGS FILES



        // GET THE MENAION DATA
        int m = today.getMonth();
        int d = today.getDay();

        filename = "";
        filename += m < 10 ? "xml/0" + m : "xml/" + m;  // CLEANED UP
        filename += d < 10 ? "/0" + d : "/" + d; // CLEANED UP
        filename += "";

        Day checkingM = new Day(filename,getInformation3().getDayInfo());
        getInformation3().getDayInfo().put("dRank",Math.max(checkingP.getDayRank(), checkingM.getDayRank()));

        OrderedHashtable[] paschalReadings = checkingP.getReadings();
        OrderedHashtable[] menaionReadings = checkingM.getReadings();
        OrderedHashtable combinedReadings = new OrderedHashtable();


        ReadingUtility.processMenaionPaschalReadings(menaionReadings, combinedReadings);
        ReadingUtility.processMenaionPaschalReadings(paschalReadings, combinedReadings);


        OrderedHashtable temp = (OrderedHashtable) combinedReadings.get("LITURGY");
        //System.out.println("temp values (423)" + temp);
        Vector Readings = (Vector) temp.get("Readings");
        Vector Rank = (Vector) temp.get("Rank");
        Vector Tag = (Vector) temp.get("Tag");
        //Special case and consider it differently


        Vector type = new Vector();


        for (int j = 0; j < Readings.size(); j++) {
            OrderedHashtable liturgy = (OrderedHashtable) Readings.get(j);
            OrderedHashtable stepE = (OrderedHashtable) liturgy.get(readingType);
            if (stepE != null)
            {

            type.add(stepE.get("Reading").toString());
            }
            else
            {
                //type.add("");
            }


        }


        //output += RSep;
        OrderedHashtable Final2 = new OrderedHashtable();
        Final2.put("Readings", type);
        Final2.put("Rank", Rank);
        Final2.put("Tag", Tag);



        return Final2;
    }



	@Override
	public void text(String str) throws Exception {
	
	}

	public static Helpers getFindLanguage() {
		return findLanguage;
	}

	public static void setFindLanguage(Helpers findLanguage) {
		Reading.findLanguage = findLanguage;
	}

	public static StringOp getInformation3() {
		return information3;
	}

	public static void setInformation3(StringOp information3) {
		Reading.information3 = information3;
	}
	
	

}