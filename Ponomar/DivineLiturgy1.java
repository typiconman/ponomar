package Ponomar;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;

/***************************************************************
DivineLiturgy1.java :: MODULE THAT TAKES THE GIVEN DIVIN LITURGY (GOSPEL AND EPISTLE) READINGS FOR THE DAY,
THAT IS, PENTECOSTARION, MENELOGION, AND FLOATERS AND RETURNS
THE APPROPRIATE SET OF READINGS FOR THE DAY AND THEIR ORDER
ASSUMING THAT THE "LUCAN JUMP" IS BEING USED. THIS FUNCITON MUST BE
PREFORMED SEPARATELY FOR EACH TYPE OF READING: EPISTLE AND GOSPEL.
THIS PROGRAMME HAS BEEN GENERALISED TO ALLOW ANY SET OF RULES TO BE USED.

Further work will convert this into the programme that will allow the creation of the text for the Divine Liturgy.

DivineLiturgy1.java is part of the Ponomar project.
Copyright 2008, 2012, 2015 Yuri Shardt
version 1.0: May 2008
 * version 2.0: June 2012, further updates and corrections to the new format.
 * version 2.5: 2015, updates and corrections


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
public class DivineLiturgy1 implements DocHandler {

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
    private static LanguagePack Phrases;// = new LanguagePack();
    private static String[] TransferredDays;// = Phrases.obtainValues((String) Phrases.Phrases.get("DayReading"));
    private static String[] Error;// = Phrases.obtainValues((String) Phrases.Phrases.get("Errors"));
    private static Helpers findLanguage;// = new Helpers();
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
    private static StringOp Analyse=new StringOp();

    public DivineLiturgy1(OrderedHashtable dayInfo) {
        Analyse.dayInfo=dayInfo;
          Phrases = new LanguagePack(dayInfo);
    TransferredDays = Phrases.obtainValues((String) Phrases.Phrases.get("DayReading"));
     Error = Phrases.obtainValues((String) Phrases.Phrases.get("Errors"));
     findLanguage=new Helpers(Analyse.dayInfo);
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

            if (Analyse.evalbool(table.get("Cmd").toString()) == false) {
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

    public String Readings(OrderedHashtable readingsIn, String ReadingType, JDate2 today) {
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
        int doy = Integer.parseInt(Analyse.dayInfo.get("doy").toString());
        int dow = Integer.parseInt(Analyse.dayInfo.get("dow").toString());
        int nday = Integer.parseInt(Analyse.dayInfo.get("nday").toString());


        //DETERMINE THE GOVERNING PARAMETERS FOR COMPILING THE READINGS
        try {
            FileReader frf = new FileReader(findLanguage.langFileFind(Analyse.dayInfo.get("LS").toString(), "xml/Commands/DivineLiturgy.xml"));
            DivineLiturgy1 a1 = new DivineLiturgy1(Analyse.dayInfo);
            QDParser.parse(a1, frf);
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*NOTE: SINCE THE 33rd SUNDAY AFTER PENTECOST DOES NOT HAVE ANY ASSOCIATED READINGS IN THE PENTECOSTARION,
        THIS CAN LEAD TO DIFFICULTIES IN DOING CERTAIN THINGS! THUS, THE FOLLOWING CORRECTIONS.
         */
        if ((doy >= 4 && doy <= 10) && (dow == 0) && ReadingType.equals("apostol")) {
            //IF THERE IS AN APOSTOL ON THIS DAY, THEN THERE MAY BE ISSUES WITH ITS PRESENCE.
            //NOTE: NOTHING IS CURRENTLY DONE ABOUT THIS!           
        }

        //CHECK WHETHER OR NOT IT IS DESIRED TO TRANSFER THE SKIPPED SEQUENTIAL READINGS
        Vector transfer = (Vector) Information.get("Transfer");
        boolean transfer1 = Analyse.evalbool((String) transfer.get(0));
        classifyReadings tomorrows = new classifyReadings();
        classifyReadings yesterdays = new classifyReadings();
        if (transfer1) {
            /*NOW CONSIDER ANY SUPPRESSED READINGS:
            //THE FOLLOWING SHOULD BE NOTED:
            1. READINGS ARE NEVER TRANSFERRED TO A SUNDAY
            2. TUESDAY CAN HAVE 2 SETS OF READINGS TRANSFERRED TO IT: MONDAY'S AND WEDNESDAY'S
             */
            //NOTE 2: NO READINGS ARE TRANSFERRED DURING LENT, THAT IS, -48 <= nday <=0.
            Vector transferRule = (Vector) Information.get("TransferRulesB");
            boolean transfer2 =Analyse.evalbool((String) transferRule.get(0));
            if (transfer2) //St. NICHOLAS'S DAY HAS A SPECIAL SET OF RULES
            {
                //IT IS OBLIGATORY TO CHECK THE NEXT DAY IF ANY READINGS ARE TRANSFERRED!
                //THE SAME PROCEDURE AS IN Main.java WILL BE FOLLOWED!


                //Rewriting the transferring rules based on the changes in the file format for the ranking and the like (Y.S. 20120610 n.s.)

                
                StringOp Transfers=new StringOp();
                Transfers.dayInfo = Analyse.dayInfo.clone();//findLanguage.deepCopy((Hashtable)StringOp.dayInfo.clone());
                Information3.dayInfo=Analyse.dayInfo.clone();
                String dRankOld=Analyse.dayInfo.get("dRank").toString();
                today.addDays(1);
                // PUT THE RELEVANT DATA IN THE HASH FOR TOMORROW
                //System.out.println("Case I: Testing the StringOp files: In StringOp, doy = "+StringOp.dayInfo.get("dRank").toString()+" In Information3, doy = "+Information3.dayInfo.get("dRank").toString()+" In Transfers, doy = "+Transfers.dayInfo.get("dRank"));
                Information3.dayInfo.put("dow", today.getDayOfWeek());
                Information3.dayInfo.put("doy", today.getDoy());
                Information3.dayInfo.put("dRank","0");
                //System.out.println("Case II: Testing the StringOp files: In StringOp, doy = "+StringOp.dayInfo.get("doy").toString()+" In Information3, doy = "+Information3.dayInfo.get("doy").toString()+" In Transfers, doy = "+Transfers.dayInfo.get("doy"));
                nday = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear(),today.getCalendar2()));
                int ndayP = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() - 1,today.getCalendar2()));
                //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
                int ndayF = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() + 1,today.getCalendar2()));
                Information3.dayInfo.put("nday", nday);
                Information3.dayInfo.put("ndayP", ndayP);
                Information3.dayInfo.put("ndayF", ndayF);

                getReadings(today, ReadingType);
                tomorrowRead = getReadings(today, ReadingType);
                tomorrows = new classifyReadings(tomorrowRead, Information3.dayInfo.clone());
                //System.out.println("Case III: Testing the StringOp files: In StringOp, doy = "+StringOp.dayInfo.get("dRank").toString()+" In Information3, doy = "+Information3.dayInfo.get("dRank").toString()+" In Transfers, doy = "+Transfers.dayInfo.get("dRank"));


                today.subtractDays(1);
                /*Analyse.dayInfo.put("dow", today.getDayOfWeek());
                Analyse.dayInfo.put("doy", today.getDoy());
                nday = (int) JDate.difference(today, Paschalion.getPascha(today.getYear()));
                ndayP = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
                //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
                ndayF = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));
                Analyse.dayInfo.put("nday", nday);
                Analyse.dayInfo.put("ndayP", ndayP);
                Analyse.dayInfo.put("ndayF", ndayF);
                Analyse.dayInfo.put("dRank",dRankOld);*/
            }
            //NOW WE NEED TO CHECK YESTERDAY'S READINGS, BUT THIS WILL ONLY OCCUR ON A TUESDAY OR DEC. 6th
            transferRule = (Vector) Information.get("TransferRulesF");
            transfer2 = Analyse.evalbool((String) transferRule.get(0));

            if (transfer2) //IF IT IS A SATURDAY, THEN THE READINGS WILL BE SKIPPED, ???
            {
                //IT IS OBLIGATORY TO CHECK THE NEXT DAY IF ANY READINGS ARE TRANSFERRED!
                //THE SAME PROCEDURE AS IN Main.java WILL BE FOLLOWED!


                StringOp Transfers=new StringOp();
                Transfers.dayInfo.putAll(Analyse.dayInfo.clone());
                Information3.dayInfo=Analyse.dayInfo.clone();
                String dRankOld=Analyse.dayInfo.get("dRank").toString();
                today.subtractDays(1);


                // PUT THE RELEVANT DATA IN THE HASH FOR TOMORROW
                Information3.dayInfo.put("dow", today.getDayOfWeek());
                Information3.dayInfo.put("doy", today.getDoy());
                Information3.dayInfo.put("dRank","0");
                nday = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear(),today.getCalendar2()));
                int ndayP = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() - 1,today.getCalendar2()));
                //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
                int ndayF = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear() + 1,today.getCalendar2()));
                Information3.dayInfo.put("nday", nday);
                Information3.dayInfo.put("ndayP", ndayP);
                Information3.dayInfo.put("ndayF", ndayF);

                yesterdayRead = getReadings(today, ReadingType);
                yesterdays = new classifyReadings(yesterdayRead, Information3.dayInfo.clone());



                today.addDays(1);
                /*Analyse.dayInfo.put("dow", today.getDayOfWeek());
                Analyse.dayInfo.put("doy", today.getDoy());
                nday = (int) JDate.difference(today, Paschalion.getPascha(today.getYear()));
                ndayP = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
                //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
                ndayF = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));
                Analyse.dayInfo.put("nday", nday);
                Analyse.dayInfo.put("ndayP", ndayP);
                Analyse.dayInfo.put("ndayF", ndayF);
                Analyse.dayInfo.put("dRank",dRankOld);*/
            }
        }


        Vector dailyVf = new Vector();
        Vector dailyRf = new Vector();
        Vector dailyTf = new Vector();        
        for (int i=0;i<yesterdays.suppressedV.size();i++){
            dailyVf.add(yesterdays.suppressedV.get(i));
            dailyRf.add(yesterdays.suppressedR.get(i));
            dailyTf.add((dow - 1 + 7) % 7);

        }
        for (int i=0;i<orderedReadings.dailyV.size();i++){
            dailyVf.add(orderedReadings.dailyV.get(i));
            dailyRf.add(orderedReadings.dailyR.get(i));
            dailyTf.add(dow);

        }
        for (int i=0;i<tomorrows.suppressedV.size();i++){
            dailyVf.add(tomorrows.suppressedV.get(i));
            dailyRf.add(tomorrows.suppressedR.get(i));
            dailyTf.add((dow + 1) % 7);

        }        
        

        
        //System.out.println("Testing some math: " + (0 - 1 + 7) % 7);
        Vector menaionV = new Vector();
        Vector menaionR = new Vector();
        Vector menaionT = new Vector();

        for (int i=0;i<orderedReadings.menaionV.size();i++){
            menaionV.add(orderedReadings.menaionV.get(i));
            menaionR.add(orderedReadings.menaionR.get(i));
            menaionT.add(orderedReadings.menaionT.get(i));

        }

        


        //THE GENERAL FORMAT IS: FLOATERS, PENTECOSTARION, MENALOGION, EXCEPT ON SATURDAYS WHERE IT IS FLOATERS, MENALOGION, PENTECOSTARION

        if (dow == 6) {
            //ON SATURDAYS, THE READINGS FROM THE MENALOGION TAKE PRECEDENCE.
            for (int i=0;i<dailyVf.size();i++){
            menaionV.add(dailyVf.get(i));
            menaionR.add(dailyRf.get(i));
            menaionT.add(dailyTf.get(i));
            
            return format(menaionV, menaionR, menaionT);
            }
        }
        for (int i=0;i<menaionV.size();i++){
            dailyVf.add(menaionV.get(i));
            dailyRf.add(menaionR.get(i));
            dailyTf.add(menaionT.get(i));

        }
         //System.out.println("---Testing Main Programme-----");

        //System.out.println(menaionV);
        //System.out.println(dailyVf);
        
        return format(dailyVf, dailyRf, dailyTf);
    }

    private OrderedHashtable getReadings(JDate2 today, String readingType) {
        String filename = "";
        int lineNumber = 0;

        int nday = (int) JDate2.difference(today, Paschalion.getPascha(today.getYear(),today.getCalendar2()));

        //I COPIED THIS FROM THE Main.java FILE BY ALEKS WITH MY MODIFICATIONS (Y.S.)
        //FROM HERE UNTIL
        if (nday >= -70 && nday < 0) {
            filename = triodionFileName;
            lineNumber = Math.abs(nday);
        } else if (nday < -70) {
            // WE HAVE NOT YET REACHED THE LENTEN TRIODION
            filename = pentecostarionFileName;
            JDate2 lastPascha = Paschalion.getPascha(today.getYear() - 1,today.getCalendar2());
            lineNumber = (int) JDate2.difference(today, lastPascha) + 1;
        } else {
            // WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
            filename = pentecostarionFileName;
            lineNumber = nday + 1;
        }

        filename += lineNumber >= 10 ? lineNumber + "" : "0" + lineNumber + ""; // CLEANED UP
        // READ THE PENTECOSTARION / TRIODION INFORMATION
        Day checkingP = new Day(filename,Information3.dayInfo);


        //ADDED 2008/05/19 n.s. Y.S.
        //COPYING SOME READINGS FILES



        // GET THE MENAION DATA
        int m = today.getMonth();
        int d = today.getDay();

        filename = "";
        filename += m < 10 ? "xml/0" + m : "xml/" + m;  // CLEANED UP
        filename += d < 10 ? "/0" + d : "/" + d; // CLEANED UP
        filename += "";
        
        Day checkingM = new Day(filename,Information3.dayInfo);
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
                output += Analyse.dayInfo.get("ReadSep") + " ";
            }
            output += b;
        }
        if (c.length() > 0) {
            if (output.length() > 0) {
                output += Analyse.dayInfo.get("ReadSep") + " ";
            }
            output += c;

        }

        //TECHNICALLY, IF THERE ARE 3 OR MORE READINGS, THEN SOME SHOULD BE TAKEN "FROM THE BEGINNING" (nod zachalo).
        return output;
    }

    public String format(Vector vectV, Vector vectR, Vector vectT) {
        String output = "";
        //AT THIS POINT, THE PENTECOSTARION READINGS WILL BE FORMATED SO THAT THEY ARE SEQUENTIAL BY THE WEEK,
        //ESPECIALLY IF THERE ARE ANY RETRACTIONS OR THE LIKE.
        /*try {
        //The Readings should be sorted based on the order of values in Type, but only if it is numeric, that is, it is the Pentecostarion data
        if (vectV.size() > 1) {
        //THIS IS NOT THE MOST EFFECTIVE TECHNIQUE, BUT THEN THERE WILL ONLY EVER TRULY BE 2 READINGS TO MOVE!
        int secondDay = Integer.parseInt((String) vectT.get(1));
        int firstDay = Integer.parseInt((String) vectT.get(0));
        //SOLVES AN ORDERING ISSUE WITH SUNDAY BEING ORIGINALLY PLACED BEFORE SATURDAY,
        //WHEN IT SHOULD HAVE BEEN AFTER
        //ADDED 2008/08/04 n.s. by Y.S.
        if (secondDay == 0) {
        secondDay = 7;
        }
        if (firstDay == 0) {
        firstDay = 7;
        }

        if (Integer.parseInt((String) vectT.get(0)) > secondDay) {

        Object a = vectV.set(0, vectV.get(1));
        vectV.set(1, a);
        a = Type.set(0, Type.get(1));
        Type.set(1, a);
        }

        }
        } catch (Exception e) {
        }*/
        Bible ShortForm = new Bible(Analyse.dayInfo);
        try {
            Enumeration e3 = vectV.elements();
            for (int k = 0; k < vectV.size(); k++) {
                String reading = (String) vectV.get(k);
                output += ShortForm.getHyperlinkLoc(reading);

                if ((Integer) vectR.get(k) == -2 ) {
                    if (vectV.size()>1){
                    int tag = (Integer) vectT.get(k);
                    output += " (" + Week(vectT.get(k).toString()) + ")";
                    }
                } else {
                    output += vectT.get(k);
                }

                if (k < vectV.size() - 1) {
                    output += Analyse.dayInfo.get("ReadSep");		//IF THERE ARE MORE READINGS OF THE SAME TYPE APPEND A SEMICOLON!
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
            ParameterValues.dayInfo=Analyse.dayInfo;
            //System.out.println("In ParameterValues, we have LS = " + ParameterValues.dayInfo.get("LS")+" while in Analyse, we have "+Analyse.dayInfo.get("LS"));
            classify(readingsInA);
        }

       public classifyReadings(OrderedHashtable readingsInA, OrderedHashtable dayInfo) {
           ParameterValues.dayInfo=dayInfo;
            classify(readingsInA);

        }
        private void classify(OrderedHashtable readingsIn)
        {
            //Initialise Information.
            Information2=new OrderedHashtable();
            findLanguage=new Helpers(ParameterValues.dayInfo);
            //System.out.println(findLanguage.langFileFind(ParameterValues.dayInfo.get("LS").toString(), "xml/Commands/DivineLiturgy.xml"));
            try {
                FileReader frf = new FileReader(findLanguage.langFileFind(ParameterValues.dayInfo.get("LS").toString(), "xml/Commands/DivineLiturgy.xml"));
                //System.out.println(findLanguage.langFileFind(ParameterValues.dayInfo.get("LS").toString(), "xml/Commands/DivineLiturgy.xml"));
                //DivineLiturgy a1 = new classifyReadin();
                QDParser.parse(this, frf);
            } catch (Exception e) {
                e.printStackTrace();
            }

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
            LeapReadings();		//THIS ALLOWS APPROPRIATE SKIPPING OF READINGS OVER THE NATIVITY SEASON!

            /******************************************************
            FOR ALL HOLIDAYS OF THE FIRST CLASS, THAT IS, OF THE LORD, THEN ONLY THE MENALOGION
            READINGS ARE TAKEN. THE PENTECOSTARION READINGS CAN BE TRANSFERRED.
            THE FOLLOWING FESTIVALS ARE CONSIDERED:
            1. EXALTATION: SEPTEMBER 14th: DOY == 256
            2. CHRISTMAS: DECEMBER 25th: DOY == 358
            3. THEOPHANY: JANUARY 6th: DOY == 5
            4. TRANSFIGURATION: AUGUST 6th: DOY == 217
             ******************************************************/
   /*         if (doy == 256 || doy == 358 || doy == 5 || doy == 217) {
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
*/
            /********************************
            FOR ALL HOLIDAY OF THE SECOND CLASS, THAT IS, OF THE MOTHER OF GOD, THEN ONLY THE MENALOGION
            READINGS ARE TAKEN, IF IT FALLS DURING MONDAY TO SATURDAY, OTHERWISE THE READINGS
            ARE COMBINED WITH THE SEQUENTIAL READINGS.
            THE FOLLOWING FESTIVALS ARE CONSIDERED:
            1. ANNUNCIATION: MARCH 25th: DOY == 83 (ALTHOUGH THE RULES ARE ACTUALLY MORE INVOLVED, HENCE SKIPPED)
            2. PRESENTATION OF THE LORD: FEBRUARY 2nd: DOY == 32, BUT NOT IF NDAY == -48 (FIRST DAY OF LENT).
            3. NATIVITY OF THE MOTHER OF GOD: SEPTEMBER 8th: DOY == 250
            4. DORMITION OF THE MOTHER OF GOD: AUGUST 15th: DOY == 226
            5. ENTRY OF THE MOTHER OF GOD INTO THE TEMPLE: NOVEMBER 21st: 324
             **************************************************************************************/
 /*           if ((doy == 32 && nday != -48) || doy == 250 || doy == 226 || doy == 324) {
                if (dow != 0) {
                    for (int k = 0; k < dailyV.size(); k++) {
                        suppressedV.add(dailyV.get(k));
                        suppressedR.add(dailyR.get(k));
                        suppressedT.add(dailyT.get(k));
                    }
                    dailyV.clear();
                    dailyR.clear();
                    dailyT.clear();
                    return;					//There is no need for any other readings to be considered!
                } else {
                    //ALL THE READINGS ARE COMBINED IN SOME FASHION, HOWEVER SOME COULD POTENTIAL BE REDUCED DUE TO REPEATS

                    return;					//CHECK WHETHER IS TRUE
                }
            }


            if (dow != 0) {*/
                Vector vect = (Vector) Information2.get("Class3Transfers");
                if (vect != null) {
                    for (Enumeration e2 = vect.elements(); e2.hasMoreElements();) {
                        String Command = (String) e2.nextElement();
                        if (ParameterValues.evalbool(Command)) {
                            //THE CURRENT COMMAND WAS TRUE AND THE SEQUENTITIAL READING IS TO BE SUPPRESSED/TRANSFERRED
                            for (int k = 0; k < dailyV.size(); k++) {
                                suppressedV.add(dailyV.get(k));
                                suppressedR.add(dailyR.get(k));
                                suppressedT.add(dailyT.get(k));
                            }
                            dailyV.clear();
                            dailyR.clear();
                            dailyT.clear();
                            return;
                        }
                    }
                }
                return;					//There is no need for any other readings to be considered!
            //}

            //AT THIS POINT, THE PENTECOSTARION READINGS MAY BE REDUCED DUE TO REPEATS

            //return;
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
