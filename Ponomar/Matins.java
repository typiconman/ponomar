package Ponomar;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;

/***************************************************************
Matins.java :: MODULE THAT TAKES THE GIVEN MATINS READINGS FOR THE DAY,
THAT IS, PENTECOSTARION, MENELOGION, AND FLOATERS AND RETURNS
THE APPROPRIATE SET OF READINGS FOR THE DAY AND THEIR ORDER. 

Further work will convert this into the programme that will allow the creation of the text for Matins.

Matins.java is part of the Ponomar project.
Copyright 2012 Yuri Shardt
version 1.0: July 2012
yuri.shardt (at) gmail.com

PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE CODE
PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES THEREOF.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 **************************************************************/
public class Matins implements DocHandler {

    private final static String configFileName = "ponomar.config"; // CONFIGURATIONS FILE
    //private final static String generalFileName="Ponomar/xml/";
    private final static String triodionFileName = "xml/triodion/";   // TRIODION FILE
    private final static String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
    private static OrderedHashtable readings;	// CONTAINS TODAY'S SCRIPTURE READING
    private static OrderedHashtable PentecostarionS;		//CONTAINS THE PENTECOSTARION READINGS (SEQUENTIAL (rjadovoje) READINGS!)
    private static OrderedHashtable MenalogionS;		//CONTAINS THE MENALOGION READINGS, EXCLUDING ANY FLOATERS
    private static OrderedHashtable FloaterS;
    private static OrderedHashtable Information;		//CONTAINS COMMANDS ABOUT HOW TO CARRY OUT THE ORDERING OF THE READINGS
    private static String Glocation;
    private static LanguagePack Phrases = new LanguagePack();
    private static String[] TransferredDays = Phrases.obtainValues((String) Phrases.Phrases.get("DayReading"));
    private static String[] Error = Phrases.obtainValues((String) Phrases.Phrases.get("Errors"));
    private static Helpers findLanguage = new Helpers();
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
    private static OrderedHashtable tomorrowRead = new OrderedHashtable();
    private static OrderedHashtable yesterdayRead = new OrderedHashtable();
    private static StringOp Information3  = new StringOp();

    public Matins() {
    }

//THESE ARE THE SAME FUNCTION AS IN MAIN, BUT TRIMMED FOR THE CURRENT NEEDS
    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String elem, Hashtable table) {
        // THE TAG COULD CONTAIN A COMMAND Cmd
        // THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
        // TODAY'S INFORMATION IN dayInfo.
        if (table.get("Cmd") != null) {
            // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

            if (StringOp.evalbool(table.get("Cmd").toString()) == false) {
                return;
            }
        }

        if (elem.equals("COMMAND")) {
            //THIS WILL STORE ALL THE POSSIBLE COMMANDS FOR A GIVEN SITUATION AND ALLOW THE RESULTS TO BE DETEMINED.
            String name = (String) table.get("Name");
            String value = (String) table.get("Value");
            //IF THE GIVEN name OCCURS IN THE information HASHTABLE THAN AUGMENT ITS VALUES.
            if (Information.containsKey(name)) {
                Vector previous = (Vector) Information.get(name);
                previous.add(value);
                Information.put(name, previous);
            } else {
                Vector vect = new Vector();
                vect.add(value);
                Information.put(name, vect);
            }

        }
        //ALL WE CARE ABOUT ARE THE SCRIPTURE READINGS
    }

    public void endElement(String elem) {
    }

    public void text(String text) {
    }

    public String Readings(OrderedHashtable readingsIn, JDate today) {
        /********************************************************
        SINCE I HAVE CORRECTED THE SCRIPTURE READINGS IN THE MAIN FILE, I CAN NOW PRECEDE WITH A BETTER VERSION OF THIS PROGRAMME!
         ********************************************************/
        //PROCESS THE READINGS INTO THE DESIRED FORMS:
        classifyReadings orderedReadings = new classifyReadings(readingsIn);
       /* Information3.dayInfo.put("doy","12");
        Information3.dayInfo.put("dow","1");
        Information3.dayInfo.put("nday","2");
        System.out.println("Testing the new StringOp formulation is " + Information3.evalbool("doy == 12"));*/

        Information = new OrderedHashtable();
        int doy = Integer.parseInt(StringOp.dayInfo.get("doy").toString());
        int dow = Integer.parseInt(StringOp.dayInfo.get("dow").toString());
        int nday = Integer.parseInt(StringOp.dayInfo.get("nday").toString());
        int dRank=Integer.parseInt(StringOp.dayInfo.get("dRank").toString());


        //DETERMINE THE GOVERNING PARAMETERS FOR COMPILING THE READINGS
        /*try {
            FileReader frf = new FileReader(findLanguage.langFileFind(StringOp.dayInfo.get("LS").toString(), "xml/Commands/Matins.xml"));
            Matins a1 = new Matins();
            QDParser.parse(a1, frf);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        //For the time being I will hard code the rules, as it is simple one. Suppress Sequential readings on Sunday if dRank > 6; otherwise suppress the menaion readings.
        


        Vector dailyVf = new Vector();
        Vector dailyRf = new Vector();
        Vector dailyTf = new Vector();
        
        for (int i=0;i<orderedReadings.dailyV.size();i++){
            dailyVf.add(orderedReadings.dailyV.get(i));
            dailyRf.add(orderedReadings.dailyR.get(i));
            dailyTf.add(dow);

        }
        for (int i=0;i<orderedReadings.menaionV.size();i++){
            dailyVf.add(orderedReadings.menaionV.get(i));
            dailyRf.add(orderedReadings.menaionR.get(i));
            dailyTf.add(orderedReadings.menaionT.get(i));
        }        

        return format(dailyVf, dailyRf, dailyTf);
    }

    private OrderedHashtable getReadings(JDate today, String readingType) {
        String filename = "";
        int lineNumber = 0;

        int nday = (int) JDate.difference(today, Paschalion.getPascha(today.getYear()));

        //I COPIED THIS FROM THE Main.java FILE BY ALEKS WITH MY MODIFICATIONS (Y.S.)
        //FROM HERE UNTIL
        if (nday >= -70 && nday < 0) {
            filename = triodionFileName;
            lineNumber = Math.abs(nday);
        } else if (nday < -70) {
            // WE HAVE NOT YET REACHED THE LENTEN TRIODION
            filename = pentecostarionFileName;
            JDate lastPascha = Paschalion.getPascha(today.getYear() - 1);
            lineNumber = (int) JDate.difference(today, lastPascha) + 1;
        } else {
            // WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
            filename = pentecostarionFileName;
            lineNumber = nday + 1;
        }

        filename += lineNumber >= 10 ? lineNumber + "" : "0" + lineNumber + ""; // CLEANED UP
        // READ THE PENTECOSTARION / TRIODION INFORMATION
        Day checkingP = new Day(filename,Information3);


        //ADDED 2008/05/19 n.s. Y.S.
        //COPYING SOME READINGS FILES



        // GET THE MENAION DATA
        int m = today.getMonth();
        int d = today.getDay();

        filename = "";
        filename += m < 10 ? "xml/0" + m : "xml/" + m;  // CLEANED UP
        filename += d < 10 ? "/0" + d : "/" + d; // CLEANED UP
        filename += "";

        Day checkingM = new Day(filename,Information3);
        Information3.dayInfo.put("dRank",Math.max(checkingP.getDayRank(), checkingM.getDayRank()));

        OrderedHashtable[] PaschalReadings = checkingP.getReadings();
        OrderedHashtable[] MenaionReadings = checkingM.getReadings();
        OrderedHashtable CombinedReadings = new OrderedHashtable();


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


        OrderedHashtable temp = (OrderedHashtable) CombinedReadings.get("LITURGY");
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

    protected String Display(String a, String b, String c) {
        //THIS FUNCTION TAKES THE POSSIBLE 3 READINGS AND COMBINES THEM AS APPROPRIATE, SO THAT NO SPACES OR OTHER UNDESIRED STUFF IS DISPLAYED!
        String output = "";
        if (a.length() > 0) {
            output += a;
        }
        if (b.length() > 0) {
            if (output.length() > 0) {
                output += StringOp.dayInfo.get("ReadSep") + " ";
            }
            output += b;
        }
        if (c.length() > 0) {
            if (output.length() > 0) {
                output += StringOp.dayInfo.get("ReadSep") + " ";
            }
            output += c;

        }

        //TECHNICALLY, IF THERE ARE 3 OR MORE READINGS, THEN SOME SHOULD BE TAKEN "FROM THE BEGINNING" (nod zachalo).
        return output;
    }

    public String format(Vector vectV, Vector vectR, Vector vectT) {
        String output = "";
        
        Bible ShortForm = new Bible();
        try {
            Enumeration e3 = vectV.elements();
            for (int k = 0; k < vectV.size(); k++) {
                String reading = (String) vectV.get(k);
                output += ShortForm.getHyperlink(reading);

                if ((Integer) vectR.get(k) == -2 ) {
                    if (vectV.size()>1){
                    int tag = (Integer) vectT.get(k);
                    output += " (" + Week(vectT.get(k).toString()) + ")";
                    }
                } else {
                    output += vectT.get(k);
                }

                if (k < vectV.size() - 1) {
                    output += StringOp.dayInfo.get("ReadSep");		//IF THERE ARE MORE READINGS OF THE SAME TYPE APPEND A SEMICOLON!
                }
            }
        } catch (Exception a) {

            System.out.println(a.toString());
            StackTraceElement[] trial=a.getStackTrace();
            System.out.println(trial[0].toString());

        }
        return output;
    }

    private String Week(String dow) {
        //CONVERTS THE DOW STRING INTO A NAME. THIS SHOULD BE IN THE ACCUSATIVE CASE
        try {
            return TransferredDays[Integer.parseInt(dow)];
        } catch (Exception a) {
            return dow;		//A DAY OF THE WEEK WAS NOT SENT
        }
    }

    public static void main(String[] argz) {
    }

    class classifyReadings implements DocHandler {

        private final String configFileName = "ponomar.config"; // CONFIGURATIONS FILE
        //private final static String generalFileName="Ponomar/xml/";
        private final String triodionFileName = "xml/triodion/";   // TRIODION FILE
        private final String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
        private OrderedHashtable Information2;		//CONTAINS COMMANDS ABOUT HOW TO CARRY OUT THE ORDERING OF THE READINGS
        public Vector dailyV = new Vector();
        public Vector dailyR = new Vector();
        public Vector dailyT = new Vector();
        public Vector menaionV = new Vector();
        public Vector menaionR = new Vector();
        public Vector menaionT = new Vector();
        public Vector suppressedV = new Vector();
        public Vector suppressedR = new Vector();
        public Vector suppressedT = new Vector();
        private StringOp ParameterValues=new StringOp();

        public classifyReadings() {
        }

        public classifyReadings(OrderedHashtable readingsInA) {
            StringOp Testing = new StringOp();
            ParameterValues.dayInfo=StringOp.dayInfo;
            classify(readingsInA);
        }

        public classifyReadings(OrderedHashtable readingsInA, StringOp ParameterValues) {
            classify(readingsInA);

        }
        private void classify(OrderedHashtable readingsIn)
        {
            //Initialise Information.
            Information2=new OrderedHashtable();
            /*try {
                FileReader frf = new FileReader(findLanguage.langFileFind(ParameterValues.dayInfo.get("LS").toString(), "xml/Commands/Matins.xml"));
                //System.out.println(findLanguage.langFileFind(ParameterValues.dayInfo.get("LS").toString(), "xml/Commands/DivineLiturgy.xml"));
                //DivineLiturgy a1 = new classifyReadin();
                QDParser.parse(this, frf);
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            Vector paschalV = (Vector) readingsIn.get("Readings");
            Vector paschalR = (Vector) readingsIn.get("Rank");
            Vector paschalT = (Vector) readingsIn.get("Tag");

            dailyV = new Vector();
            dailyR = new Vector();
            dailyT = new Vector();


            if (paschalV == null){
                return;
            }


            for (int k = 0; k < paschalV.size(); k++) {

                if ((Integer) paschalR.get(k) == -2) {
                    //THIS IS A DAILY READING THAT CAN BE SKIPPED, EXCEPT MAYBE ON SUNDAYS.
                    dailyV.add(paschalV.get(k));
                    dailyR.add(paschalR.get(k));
                    dailyT.add(paschalT.get(k));
                } else {
                    menaionV.add(paschalV.get(k));
                    menaionR.add(paschalR.get(k));
                    menaionT.add(paschalT.get(k));
                }

            }
            

            Suppress();
            //LeapReadings();


        }

        public void startDocument() {
        }

        public void endDocument() {
        }

        public void startElement(String elem, Hashtable table) {
            // THE TAG COULD CONTAIN A COMMAND Cmd
            // THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
            // TODAY'S INFORMATION IN dayInfo.
            if (table.get("Cmd") != null) {
                // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

                if (ParameterValues.evalbool(table.get("Cmd").toString()) == false) {
                    return;
                }
            }

            if (elem.equals("COMMAND")) {
                //THIS WILL STORE ALL THE POSSIBLE COMMANDS FOR A GIVEN SITUATION AND ALLOW THE RESULTS TO BE DETEMINED.
                String name = (String) table.get("Name");
                String value = (String) table.get("Value");
                //IF THE GIVEN name OCCURS IN THE information HASHTABLE THAN AUGMENT ITS VALUES.
                //System.out.println("==============================\nTesting Information\n++++++++++++++++++++");
                if (Information2.containsKey(name)) {
                    Vector previous = (Vector) Information2.get(name);
                    previous.add(value);
                    Information2.put(name, previous);
                } else {
                    Vector vect = new Vector();
                    vect.add(value);
                    Information2.put(name, vect);
                }

            }
            //ALL WE CARE ABOUT ARE THE SCRIPTURE READINGS
        }

        public void endElement(String elem) {
        }

        public void text(String text) {
        }

        private void Suppress() {
            //THIS FUNCTION CONSIDERS WHAT HOLIDAYS ARE CURRENTLY OCCURING AND RETURNS THE READINGS FOR THE DAY, WHERE SUPPRESSED CONTAINS THE READINGS THAT WERE SUPPRESSED.
            int doy = Integer.parseInt(ParameterValues.dayInfo.get("doy").toString());
            int dow = Integer.parseInt(ParameterValues.dayInfo.get("dow").toString());
            int nday = Integer.parseInt(ParameterValues.dayInfo.get("nday").toString());
            int ndayF = Integer.parseInt(ParameterValues.dayInfo.get("ndayF").toString());
            int ndayP = Integer.parseInt(ParameterValues.dayInfo.get("ndayP").toString());
            int dRank = Integer.parseInt(ParameterValues.dayInfo.get("dRank").toString());
            //LeapReadings();		//THIS ALLOWS APPROPRIATE SKIPPING OF READINGS OVER THE NATIVITY SEASON!

          
            if (dow == 0 && dRank > 6 && (nday < -49 || nday > 0)) {
                for (int k = 0; k < dailyV.size(); k++) {
                    suppressedV.add(dailyV.get(k));
                    suppressedR.add(dailyR.get(k));
                    suppressedT.add(dailyT.get(k));
                }
                dailyV.clear();
                dailyR.clear();
                dailyT.clear();
                
                return;				//There is no need for any other readings to be considered!
            }

            if (dow == 0 && dRank <= 6){
                for (int k = 0; k < menaionV.size(); k++) {
                    suppressedV.add(menaionV.get(k));
                    suppressedR.add(menaionV.get(k));
                    suppressedT.add(menaionV.get(k));
                }
                menaionV.clear();
                menaionV.clear();
                menaionV.clear();
                return;
            }

            return;
        }

        protected void LeapReadings() {
            //SKIPS THE READINGS IF THERE ARE ANY BREAKS!
            int doy = Integer.parseInt(ParameterValues.dayInfo.get("doy").toString());
            int dow = Integer.parseInt(ParameterValues.dayInfo.get("dow").toString());
            int nday = Integer.parseInt(ParameterValues.dayInfo.get("nday").toString());
            int ndayF = Integer.parseInt(ParameterValues.dayInfo.get("ndayF").toString());
            int ndayP = Integer.parseInt(ParameterValues.dayInfo.get("ndayP").toString());

            //IN ALL CASES ONLY THE PENTECOSTARION READINGS ARE EFFECTED!
            Vector empty = new Vector();
            //USING THE NEWER VERSION OF STORED VALUES
            //EACH OF THE STORED COMMANDS ARE EVALUATED IF ANY ARE TRUE THEN THE READINGS ARE SKIPPED IF THERE ARE ANY FURTHER READINGS ON THAT DAY.
            int available = menaionV.size();

            if (available > 0) {
                Vector vect = (Vector) Information2.get("Suppress");
                if (vect != null) {
                    for (Enumeration e2 = vect.elements(); e2.hasMoreElements();) {
                        String Command = (String) e2.nextElement();
                        if (ParameterValues.evalbool(Command)) {
                            //THE CURRENT COMMAND WAS TRUE AND THE SEQUENTITIAL READING IS TO BE SKIPPED
                            dailyV.clear();
                            dailyR.clear();
                            dailyT.clear();
                            suppressedV.clear();
                            suppressedR.clear();
                            suppressedT.clear();
                            return;
                        }

                    }
                }
            }

            return;
        }
    }
}
