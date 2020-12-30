package net.ponomar.services;

import javax.swing.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import net.ponomar.calendar.JDate;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.panels.PrimeSelector;
import net.ponomar.parsing.Day;
import net.ponomar.parsing.Service;
import net.ponomar.parsing.ServiceInfo;
import net.ponomar.parsing.utility.QDParser;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 

/***********************************************************************
THIS MODULE CREATES THE TEXT FOR THE ORTHODOX SERVICE OF THE FIRST HOUR (PRIME)
THIS MODULE IS STILL IN THE DEVELOPMENT PHASE.

(C) 2007, 2008 YURI SHARDT. ALL RIGHTS RESERVED.
Updated some parts to make it compatible with the changes in Ponomar, especially the language issues!

PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE CODE
PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES THEREOF.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 ***********************************************************************/
public class SixthHour extends LitService {
    //SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
    //THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
    //TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
    //DURING THE COURSE OF A SINGLE WEEK.

	private static final String SERVICE_LANGUAGE = "<SERVICE>\r\n<LANGUAGE>\r\n";
	private static final String LANGUAGE_SERVICE = "\r\n</LANGUAGE>\r\n</SERVICE>";
	private static final String GETID_TYPE_T_ID = "\r\n<GETID Type=\"T\" Id=\"";
	private static String fileNameIn = Constants.SERVICES_PATH + "PRIMES1/";
    private static String fileNameOut = fileNameIn + "Primes.html";
    private PrimeSelector selectorP;// = new PrimeSelector();
    private String reading6th = "";

    

    public SixthHour(JDate date, LinkedHashMap<String, Object> dayInfo) {
        analyse.setDayInfo(dayInfo);
            langText=new LanguagePack(dayInfo);
            primesNames=langText.obtainValues(langText.getPhrases().get("Sexte"));
	languageNames=langText.obtainValues(langText.getPhrases().get(Constants.LANGUAGE_MENU));
        fileNames=langText.obtainValues(langText.getPhrases().get("File"));
	helpNames=langText.obtainValues(langText.getPhrases().get("Help"));
        selectorP=new PrimeSelector(dayInfo);
        /*THIS IS THE PLAN FOR CREATING THE SERVICE
        1) DETERMINE ON THE BASIS OF THE PENTECOSTARION (EASTER CYCLE) THE APPROPRIATE TONE AND ANY EASTER RELATED CHANGES TO THE SERVICE
        2) LOAD THE INFORMATION FOR THE TONE, WEEKDAY, AND ANY CHANGES
        3) DETERMINE WHETHER THE MENAION REQUIRES ANY CHANGES TO THE ORDER OF THE TROPAR AND KONTAKION
        4) IMPLEMENT THE CHANGES DETERMINED IN 3)
        5) WRITE THE SERVICE AS TEXT (EVANTUALLY AS HTML OR PDF FILES).
         */
        /*GENERAL ENTRY: IN PENTECOSTARION: <PRIMES TONE="1" PRIMES1="Normal" (or "Easter") [TROPARION1=" " KONTAKION1=" "] /> PARTS IN [] ARE OPTIONAL
        IN MENOLOGION: <PRIMES TROPARION1=" " (or TROPARION2=" ") KONTAKION1=" " (or KONTAKION2=" ") />
        FOR FLOATERS: <PRIMES TROPARION1=" " KONTAKION1=" " />
        ALL VALUES WITHIN QUOTATION MARKS (EXCEPT THE TONE) REFERS TO THE FILENAME FOR THE APPROPRIATE VALUES.
         */
        //FOR THE TIME BEING IT WILL BE ASSUMED THAT THE TONE AND WEEKDAY HAVE BEEN DETERMINED EXTERNALLY.
        //GIVEN THE WEEKDAY AND TONE READ THE APPROPRIATE FILES

        //Analyse.getDayInfo() = new Hashtable();
        //Analyse.getDayInfo().put("dow", Weekday);		//DETERMINE THE DAY OF THE WEEK.


        //CREATING THE SERVICE
        today = date;
        helper = new Helpers(analyse.getDayInfo());
        reading6th = "";
        try {
            String strOut = createHours();
            if (strOut.equals("No Service Today")) {
                Object[] options = {languageNames[3]};
                JOptionPane.showOptionDialog(null, primesNames[0], langText.getPhrases().get("0") + langText.getPhrases().get(Constants.COLON) + primesNames[1], JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            } else {
                //strOut=strOut+"<p><Font Color='red'>Disclaimer: This is a preliminary attempt at creating the Primes service.</Font></p>";
                //int LangCode=Integer.parseInt(Analyse.getDayInfo().get("LS").toString());
                //if (LangCode==2 || LangCode==3 ){
                //strOut="<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><p><font face=\"Ponomar Unicode TT\" size=\"5\">"+strOut+"</font></p>";
                //System.out.println("Added Font");
                //  }

                serviceWindow(strOut);
            }
        } catch (IOException j) {
        }


    }

    protected String createHours() throws IOException {
        //OBTAIN THE DEFAULTS FOR THE SERVICE (WHAT WAS LAST USED!)
        analyse.getDayInfo().put("PS", selectorP.getWhoValue());
        int typeP = selectorP.getTypeValue();
        Service readPrime=new Service(analyse.getDayInfo());
        //FIRST READ THE TONE FILES:
        int weekday = Integer.parseInt(analyse.getDayInfo().get("dow").toString());
        //System.out.println(Weekday);
        int tone = Integer.parseInt(analyse.getDayInfo().get("Tone").toString());
        if (tone == 8) {
            tone = 0;
        }
        //System.out.println(Tone);
        if (tone != -1) {
            String fileName = OCTOECHEOS_FILENAME + "Tone " + tone;
            if (weekday == 1) {
                fileName = fileName + "/Monday.xml";
            } else if (weekday == 2) {
                fileName = fileName + "/Tuesday.xml";
            } else if (weekday == 3) {
                fileName = fileName + "/Wednesday.xml";
            } else if (weekday == 4) {
                fileName = fileName + "/Thursday.xml";
            } else if (weekday == 5) {
                fileName = fileName + "/Friday.xml";
            } else if (weekday == 6) {
                fileName = fileName + "/Saturday.xml";
            } else {
                fileName = fileName + "/Sunday.xml";
            }



            try {
                BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(analyse.getDayInfo().get("LS").toString(), fileName)), StandardCharsets.UTF_8));
                QDParser.parse(this, frf);

            } catch (Exception primes) {
                primes.printStackTrace();
            }
        }

        //READ THE PENTECOSTARION!

        //Integer.parseInt(dayInfo.get(expression).toString())
        int nday = Integer.parseInt(analyse.getDayInfo().get("nday").toString());

        if (nday >= -70 && nday < 0) {
            filename = Constants.TRIODION_PATH;
            lineNumber = Math.abs(nday);
        } else if (nday < -70) {
            // WE HAVE NOT YET REACHED THE LENTEN TRIODION
            filename = Constants.PENTECOSTARION_PATH;
            lineNumber = Integer.parseInt(analyse.getDayInfo().get(Constants.NDAY_P).toString()) + 1;
        } else {
            // WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
            filename = Constants.PENTECOSTARION_PATH;
            lineNumber = nday + 1;
        }

        filename += lineNumber >= 10 ? lineNumber : "0" + lineNumber; // CLEANED UP
        // READ THE PENTECOSTARION / TRIODION INFORMATION
        //IF THERE ARE SPECIAL TROPARION1's FROM THIS FILE THEY CAN OVERRIDE THE SET PIECES

        Day readings = new Day(filename, analyse.getDayInfo());
        try {
        	LinkedHashMap[] lessons = readings.getReadings();
        	LinkedHashMap readingContent = (LinkedHashMap) lessons[0].get(Constants.READINGS);
        	LinkedHashMap lesson = (LinkedHashMap) readingContent.get(Constants.READINGS);
        	LinkedHashMap reading = (LinkedHashMap) lesson.get("6th hour");
            //System.out.println("Reading == " +reading);
        	LinkedHashMap lesson2 = (LinkedHashMap) reading.get("1");
            //System.out.println("Lesson 2 == "+lesson2);
            reading6th = lesson2.get(Constants.READING).toString();
        } catch (Exception e)  {           //There are no appointed readings
            reading6th = "";
        }
        //System.out.println("Reading 6th = " + Reading6th);
                /*try
        {
        BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(Analyse.getDayInfo().get("LS").toString(),filename)), StandardCharsets.UTF_8));
        QDParser.parse(this, frf);
        }
        catch (Exception e)
        {
        e.printStackTrace();
        }*/

        //CHECK WHAT TYPE OF SERVICE WE ARE DEALING WITH
        //POTENTIAL STREAMLINING OF THE SERVICE: ALL THE RULES HAVE NOW BEEN OBTAINED EXCEPT FOR ANY OVERRIDES
        ServiceInfo servicePrimes = new ServiceInfo("SEXTE",analyse.getDayInfo());
        LinkedHashMap primesTrial = servicePrimes.serviceRules();

        type = primesTrial.get("Type").toString();
        lentenKat = (String) primesTrial.get(LENTENK);

        String primesAdd1 = "";

        if (type.equals("None")) {
            //THERE ARE NO SERVICES TODAY, THAT IS, THE ROYAL HOURS ARE SERVED INSTEAD
            return "No Service Today";
        } else if (type.equals("Paschal")) {

            return readPrime.startService(Constants.SERVICES_PATH + "PaschalHours.xml");
        }

        //I WOULD THEN NEED TO READ THE MENOLOGION, BUT I WILL NOT DO SO RIGHT NOW.
        //DETERMINE THE ORDERING OF THE TROPARIA AND KONTAKIA IF THERE ARE 2 OR MORE

        String strOut = "";
        analyse.getDayInfo().put(Constants.P_FLAG_1, typeP);
        analyse.getDayInfo().put(Constants.P_FLAG_2, 0);
        analyse.getDayInfo().put("PFlag3", 0);
        //NOTE PFlag2 == 3 for Holy Week Services!

        if (type.equals("Lenten")) {
            analyse.getDayInfo().put(Constants.P_FLAG_2, 1);

            if (lentenKat != null) {
                analyse.getDayInfo().put(Constants.P_FLAG_2, 2);
                //CREATE THE KATHISMA PART
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/PKath6.xml"), StandardCharsets.UTF_8));
                String data = "<SERVICES>\r\n<LANGUAGE>\r\n<GET File=\"Kathisma" + lentenKat + "\" Null=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
                out.write(data);
                out.close();
            }
            //System.out.println("Hello Lent b");
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/TP6R.xml"), StandardCharsets.UTF_8));
            //System.out.println(Reading6th);
            String data = SERVICE_LANGUAGE;
            BufferedWriter out1a = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/TP6C.xml"), StandardCharsets.UTF_8));
            String data1a = SERVICE_LANGUAGE;
            BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/PROK61R.xml"), StandardCharsets.UTF_8));
            String data1 = SERVICE_LANGUAGE;
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/PROK61C.xml"), StandardCharsets.UTF_8));
            String data2 = SERVICE_LANGUAGE;
            BufferedWriter out3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/STYX61R.xml"), StandardCharsets.UTF_8));
            String data3 = SERVICE_LANGUAGE;
            BufferedWriter out4 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/STYX61C.xml"), StandardCharsets.UTF_8));
            String data4 = SERVICE_LANGUAGE;
            BufferedWriter out5 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/PROK61a.xml"), StandardCharsets.UTF_8));
            String data5 = SERVICE_LANGUAGE;
            BufferedWriter out6 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/PROK61b.xml"), StandardCharsets.UTF_8));
            String data6 = SERVICE_LANGUAGE;
            BufferedWriter out7 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/Intro6.xml"), StandardCharsets.UTF_8));
            String data7 = SERVICE_LANGUAGE;
            BufferedWriter out8 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/Reading6.xml"), StandardCharsets.UTF_8));
            String data8 = SERVICE_LANGUAGE;
            BufferedWriter out9 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/PROK62R.xml"), StandardCharsets.UTF_8));
            String data9 = SERVICE_LANGUAGE;
            BufferedWriter out10 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/PROK62C.xml"), StandardCharsets.UTF_8));
            String data10 = SERVICE_LANGUAGE;
            BufferedWriter out11 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/STYX62R.xml"), StandardCharsets.UTF_8));
            String data11 = SERVICE_LANGUAGE;
            BufferedWriter out12 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/STYX62C.xml"), StandardCharsets.UTF_8));
            String data12 = SERVICE_LANGUAGE;
            BufferedWriter out13 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/PROK62a.xml"), StandardCharsets.UTF_8));
            String data13 = SERVICE_LANGUAGE;
            BufferedWriter out14 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/PROK62b.xml"), StandardCharsets.UTF_8));
            String data14 = SERVICE_LANGUAGE;
            if (reading6th != null) {
                if (reading6th.length() > 0) {
                    //System.out.println(Reading6th);
                    analyse.getDayInfo().put("PFlag3", 1);
                    String nday1 = String.valueOf(-nday);
                    if (-nday < 10) {
                        nday1 = "0" + nday1;
                    }

                    data = data + GETID_TYPE_T_ID + nday1 + "\" Header=\"1\" What=\"/SEXTE/TROPARION/1\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\" />";
                    data1a = data1a + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/TROPARION/1\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
                    data1 = data1 + GETID_TYPE_T_ID + nday1 + "\" Header=\"1\" What=\"/SEXTE/PROKEIMENON/1a\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
                    data1 = data1 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/PROKEIMENON/1b\" Who=\"R\"/>";
                    data2 = data2 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/PROKEIMENON/1a\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
                    data2 = data2 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/PROKEIMENON/1b\" Who=\"C\"/>";
                    data3 = data3 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/STICHOS/1\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
                    data4 = data4 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/STICHOS/1\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
                    data5 = data5 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/PROKEIMENON/1a\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
                    data6 = data6 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/PROKEIMENON/1b\" Who=\"C\" NewLine=\"1\"/>";
                    data7 = data7 + "\r\n<BIBLE getReading=\"" + reading6th + "\" Who=\"SR\" NewLine=\"1\"/>";
                    data8 = data8 + "\r\n<BIBLE Verses=\"" + reading6th + "\" Who=\"SR\" RedFirst=\"1\" Header=\"1\" NewLine=\"1\" />";
                    data9 = data9 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/PROKEIMENON/2a\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\" Header=\"1\"/>";
                    data9 = data9 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/PROKEIMENON/2b\" Who=\"R\" />";
                    data10 = data10 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/PROKEIMENON/2a\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
                    data10 = data10 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/PROKEIMENON/2b\" Who=\"C\" />";
                    data11 = data11 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/STICHOS/2\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
                    data12 = data12 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/STICHOS/2\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
                    data13 = data13 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/PROKEIMENON/2a\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
                    data14 = data14 + GETID_TYPE_T_ID + nday1 + "\" What=\"/SEXTE/PROKEIMENON/2b\" Who=\"C\" NewLine=\"1\"/>";

                }
            }
            data = data + LANGUAGE_SERVICE;
            data1a = data1a + LANGUAGE_SERVICE;
            data1 = data1 + LANGUAGE_SERVICE;
            data2 = data2 + LANGUAGE_SERVICE;
            data3 = data3 + LANGUAGE_SERVICE;
            data4 = data4 + LANGUAGE_SERVICE;
            data5 = data5 + LANGUAGE_SERVICE;
            data6 = data6 + LANGUAGE_SERVICE;
            data7 = data7 + LANGUAGE_SERVICE;
            data8 = data8 + LANGUAGE_SERVICE;
            data9 = data9 + LANGUAGE_SERVICE;
            data10 = data10 + LANGUAGE_SERVICE;
            data11 = data11 + LANGUAGE_SERVICE;
            data12 = data12 + LANGUAGE_SERVICE;
            data13 = data13 + LANGUAGE_SERVICE;
            data14 = data14 + LANGUAGE_SERVICE;
            out.write(data);
            out1a.write(data1a);
            out1.write(data1);
            out2.write(data2);
            out3.write(data3);
            out4.write(data4);
            out5.write(data5);
            out6.write(data6);
            out7.write(data7);
            out8.write(data8);
            out9.write(data9);
            out10.write(data10);
            out11.write(data11);
            out12.write(data12);
            out13.write(data13);
            out14.write(data14);
            out.close();
            out1a.close();
            out1.close();
            out2.close();
            out3.close();
            out4.close();
            out5.close();
            out6.close();
            out7.close();
            out8.close();
            out9.close();
            out10.close();
            out11.close();
            out12.close();
            out13.close();
            out14.close();
        } else {
            //CREATE THE FIRST TROPAR (BEFORE THE Glory...) PART, IF ANY
            //CREATE THE SECOND TROPAR (NORMAL)
            //APPROPRIATE TROPAR STILL NEEDS TO BE DETERMINED!!
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/PTrop61.xml"), StandardCharsets.UTF_8));
            String data = "<SERVICE>\r\n<LANGUAGE>";
            String data2 = "<SERVICE>\r\n<LANGUAGE>";
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/PTrop62.xml"), StandardCharsets.UTF_8));
            if (troparion1 != null) {
                System.out.println("The first Troparion is " + troparion1 + " Troparion2 is " + troparion2);
                if (troparion2 != null) {
                    //BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ServicesFileName+"Var/PTrop61.xml"),StandardCharsets.UTF_8));
                    data = data + "\r\n<CREATE Who=\"\" What=\"TROPARION/" + troparion1 + "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n";


                    //Dim out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ServicesFileName+"Var/PTrop62.xml"),StandardCharsets.UTF_8));
                    data2 = data2 + "\r\n<CREATE Who=\"\" What=\"TROPARION/" + troparion2 + "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n";


                } else {
                    //BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ServicesFileName+"Var/PTrop62.xml"),StandardCharsets.UTF_8));
                    data2 = data2 + "\r\n<CREATE Who=\"\" What=\"TROPARION/" + troparion1 + "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
                    //out.write(Data);
                    //out.close();
                }
            }
            data = data + "</SERVICE>\r\n</LANGUAGE>";
            data2 = data2 + "</SERVICE>\r\n</LANGUAGE>";
            out.write(data);
            out.close();
            out2.write(data2);
            out2.close();

        }

        //GET AND CREATE THE APPRORIATE KONTAKION
        //APROPRIATE KONTAKION MUST STILL BE CREATED!
       // System.out.println(Kontakion1);
        if (kontakion1 != null) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString() + Constants.SERVICES_PATH + "Var/PKont6.xml"), StandardCharsets.UTF_8));
            String data = "<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"SR\" What=\"KONTAKION/" + kontakion1 + "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
            out.write(data);
            out.close();
        }
        //Else we are dealing with a Lenten service that does not have any variable parts.
        //System.out.println("Sixth Hour: "+Analyse.getDayInfo().get("PFlag3"));

        strOut = readPrime.startService(Constants.SERVICES_PATH + "SixthHour.xml") + "</p>";


        return strOut;
    }

    public void startElement(String elem, HashMap<String, String> table) {

        // THE TAG COULD CONTAIN A COMMAND Cmd
        // THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
        // TODAY'S INFORMATION IN dayInfo.
        if (table.get("Cmd") != null) {
            // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

            if (!analyse.evalbool(table.get("Cmd"))) {

                return;
            }
        }
        //if(elem.equals("LANGUAGE") || elem.equals("TONE"))
        //{
        read = true;
        //}
        if (elem.equals("TEXT") && read) {
            text += table.get(Constants.VALUE);

        }
        if (elem.equals("SCRIPTURE") && read) {
            String type = table.get("Type");
            String reading = table.get(Constants.READING);
            //System.out.println("The readings that were found were "+type+" "+reading);
            if (type.equals("6th hour")) {
                reading6th = reading;
            }
        }
        if (elem.equals("SEXTE") && read) {
            //WE ARE DEALING WITH THE INFORMATION FOR PRIMES (THERE COULD BE INFORMATION FOR OTHER SERVICES)
            //THE VARIABLE COMPONETS IN THIS SERVICE ARE GIVEN BELOW
            String value = table.get("Type");
            if (value != null) {
                type = table.get("Type");
            }
            value = table.get(Constants.TROPARION_1);
            if (value != null) {
                troparion1 = table.get(Constants.TROPARION_1);
            }
            value = table.get(Constants.KONTAKION_1);
            if (value != null) {
                kontakion1 = table.get(Constants.KONTAKION_1);
            }
            value = table.get(Constants.KONTAKION_2);
            if (value != null) {
                kontakion1 = table.get(Constants.KONTAKION_2);
            }
            value = table.get(Constants.TROPARION_2);
            if (value != null) {
                troparion1 = table.get(Constants.TROPARION_2);
            }

            value = table.get(LENTENK);
            if (value != null) {
                lentenKat = table.get(LENTENK);
                //System.out.println(LentenK);
            }

        }
        //OTHER LITURGICAL SERVICES WOULD FOLLOW HERE

    }

    public static void main(String[] argz) {

        //new SixthHour(3);	//CREATE THE SERVICE FOR WEDNESDAY FOR TONE 1.
    }
}
