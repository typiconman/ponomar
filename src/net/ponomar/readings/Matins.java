package net.ponomar.readings;

import net.ponomar.Bible;
import net.ponomar.calendar.JDate;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.readings.utility.ClassifyMatins;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 
 

import java.util.*;

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
public class Matins extends Reading {

    public Matins(LinkedHashMap<String, Object> dayInfo) {
        getInformation3().setDayInfo(dayInfo);
        phrases = new LanguagePack(dayInfo);
        transferredDays = phrases.obtainValues(phrases.getPhrases().get("DayReading"));
        error = phrases.obtainValues(phrases.getPhrases().get("Errors"));
        setFindLanguage(new Helpers(getInformation3().getDayInfo()));
    }

//THESE ARE THE SAME FUNCTION AS IN MAIN, BUT TRIMMED FOR THE CURRENT NEEDS

    public void startElement(String elem, Hashtable table) {
        // THE TAG COULD CONTAIN A COMMAND Cmd
        // THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
        // TODAY'S INFORMATION IN dayInfo.
        if (table.get("Cmd") != null) {
            // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

            if (getInformation3().evalbool(table.get("Cmd").toString()) == false) {
                return;
            }
        }

        if (elem.equals(Constants.COMMAND)) {
            //THIS WILL STORE ALL THE POSSIBLE COMMANDS FOR A GIVEN SITUATION AND ALLOW THE RESULTS TO BE DETEMINED.
            String name = (String) table.get("Name");
            String value = (String) table.get(Constants.VALUE);
            //IF THE GIVEN name OCCURS IN THE information HASHTABLE THAN AUGMENT ITS VALUES.
            if (information.containsKey(name)) {
                Vector<String> previous = (Vector<String>) information.get(name);
                previous.add(value);
                information.put(name, previous);
            } else {
                Vector<String> vect = new Vector<String>();
                vect.add(value);
                information.put(name, vect);
            }

        }
        //ALL WE CARE ABOUT ARE THE SCRIPTURE READINGS
    }


    public String Readings(LinkedHashMap<String, Vector<String>> readingsIn, JDate today) {
        /********************************************************
        SINCE I HAVE CORRECTED THE SCRIPTURE READINGS IN THE MAIN FILE, I CAN NOW PRECEDE WITH A BETTER VERSION OF THIS PROGRAMME!
         ********************************************************/
        //PROCESS THE READINGS INTO THE DESIRED FORMS:
        ClassifyMatins orderedReadings = new ClassifyMatins(readingsIn);
       /* Information3.getDayInfo().put("doy","12");
        Information3.getDayInfo().put("dow","1");
        Information3.getDayInfo().put("nday","2");
        System.out.println("Testing the new StringOp formulation is " + Information3.evalbool("doy == 12"));*/

        information = new LinkedHashMap<String, Vector<String>>();
        int doy = Integer.parseInt(getInformation3().getDayInfo().get("doy").toString());
        int dow = Integer.parseInt(getInformation3().getDayInfo().get("dow").toString());
        int nday = Integer.parseInt(getInformation3().getDayInfo().get("nday").toString());
        int dRank=Integer.parseInt(getInformation3().getDayInfo().get(Constants.D_RANK).toString());


        //DETERMINE THE GOVERNING PARAMETERS FOR COMPILING THE READINGS
        /*try {
            FileReader frf = new FileReader(findLanguage.langFileFind(StringOp.dayInfo.get("LS").toString(), Constants.COMMANDS + "Matins.xml"));
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



    protected String Display(String a, String b, String c) {
        //THIS FUNCTION TAKES THE POSSIBLE 3 READINGS AND COMBINES THEM AS APPROPRIATE, SO THAT NO SPACES OR OTHER UNDESIRED STUFF IS DISPLAYED!
        String output = "";
        if (a.length() > 0) {
            output += a;
        }
        if (b.length() > 0) {
            if (output.length() > 0) {
                output += getInformation3().getDayInfo().get("ReadSep") + " ";
            }
            output += b;
        }
        if (c.length() > 0) {
            if (output.length() > 0) {
                output += getInformation3().getDayInfo().get("ReadSep") + " ";
            }
            output += c;

        }

        //TECHNICALLY, IF THERE ARE 3 OR MORE READINGS, THEN SOME SHOULD BE TAKEN "FROM THE BEGINNING" (nod zachalo).
        return output;
    }

    public String format(Vector vectV, Vector vectR, Vector<Integer> vectT) {
        StringBuilder output = new StringBuilder();
        
        Bible ShortForm = new Bible(getInformation3().getDayInfo());
        try {
            Enumeration e3 = vectV.elements();
            for (int k = 0; k < vectV.size(); k++) {
                String reading = (String) vectV.get(k);
                output.append(ShortForm.getHyperlink(reading));

                if ((Integer) vectR.get(k) == -2 ) {
                    if (vectV.size()>1){
                    int tag = vectT.get(k);
                    output.append(" (").append(Week(vectT.get(k).toString())).append(")");
                    }
                } else {
                    output.append(vectT.get(k));
                }

                if (k < vectV.size() - 1) {
                    output.append(getInformation3().getDayInfo().get("ReadSep"));		//IF THERE ARE MORE READINGS OF THE SAME TYPE APPEND A SEMICOLON!
                }
            }
        } catch (Exception a) {

            System.out.println(a.toString());
            StackTraceElement[] trial=a.getStackTrace();
            System.out.println(trial[0].toString());

        }
        return output.toString();
    }

    private String Week(String dow) {
        //CONVERTS THE DOW STRING INTO A NAME. THIS SHOULD BE IN THE ACCUSATIVE CASE
        try {
            return transferredDays[Integer.parseInt(dow)];
        } catch (Exception a) {
            return dow;		//A DAY OF THE WEEK WAS NOT SENT
        }
    }

    public static void main(String[] argz) {
    }
}
