package Ponomar;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.filechooser.FileFilter;

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
public class SixthHour implements DocHandler, ActionListener, ItemListener, PropertyChangeListener {
    //SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
    //THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
    //TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
    //DURING THE COURSE OF A SINGLE WEEK.

    private final static String configFileName = "ponomar.config";   //CONFIGURATIONS FILE
    private final static String octoecheosFileName = "xml/Services/Octoecheos/";   // THE LOCATION OF THE BASIC SERVICE RULES
    private final static String ServicesFileName = "xml/Services/"; // THE LOCATION FOR ANY EXTRA INFORMATION
    private static OrderedHashtable PrimesTK;
    private static String FileNameIn = "xml/Services/PRIMES1/";
    private static String FileNameOut = FileNameIn + "Primes.html";
    private static String text;
    private static boolean read = false;
    private static String Type;
    private String Troparion1;
    private String Kontakion1;
    private String Kontakion2;
    private String Troparion2;
    private final static String triodionFileName = "xml/triodion/";   // TRIODION FILE
    private final static String pentecostarionFileName = "/xml/pentecostarion/"; // PENTECOSTARION FILE
    private String filename;
    private int lineNumber;
    private LanguagePack Text;// = new LanguagePack();
    private String[] PrimesNames;// = Text.obtainValues((String) Text.Phrases.get("Sexte"));
    private String[] LanguageNames;// = Text.obtainValues((String) Text.Phrases.get("LanguageMenu"));
    private String LentenK;				//ANY REQUIRED KATHISMA REFERENCED USING "LENTENK = "17"" WOULD BE THE 17th KATHISMA.
    private JFrame frames;
    private String[] FileNames;// = Text.obtainValues((String) Text.Phrases.get("File"));
    private String[] HelpNames;// = Text.obtainValues((String) Text.Phrases.get("Help"));
    String newline = "\n";
    private String strOut;
    private JDate today;
    private Helpers helper;
    private PrimeSelector SelectorP;// = new PrimeSelector();
    private PrintableTextPane output;
    private String DisplayFont = new String(); //ALLOWS A CUSTOM FONT AND SIZE TO BE SPECIFIED FOR A GIVEN BIBLE READING: REQUIRED FOR OLD CHURCH SLAVONIC AT PRESENT
    private String DisplaySize = "12";  //UNTIL A COMPLETE UNICODE FONT IS AVAILIBLE.
    private Font DefaultFont = new Font("", Font.BOLD, 12);		//CREATE THE DEFAULT FONT
    private Font CurrentFont = DefaultFont;
    private String Reading6th = new String();
    private StringOp Analyse=new StringOp();

    

    public SixthHour(JDate date, OrderedHashtable dayInfo) {
        Analyse.dayInfo=dayInfo;
            Text=new LanguagePack(dayInfo);
            PrimesNames=Text.obtainValues((String)Text.Phrases.get("Sexte"));
	LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
        FileNames=Text.obtainValues((String)Text.Phrases.get("File"));
	HelpNames=Text.obtainValues((String)Text.Phrases.get("Help"));
        SelectorP=new PrimeSelector(dayInfo);
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

        //Analyse.dayInfo = new Hashtable();
        //Analyse.dayInfo.put("dow", Weekday);		//DETERMINE THE DAY OF THE WEEK.


        //CREATING THE SERVICE
        today = date;
        helper = new Helpers(Analyse.dayInfo);
        Reading6th = new String();
        try {
            String strOut = createPrimes();
            if (strOut.equals("No Service Today")) {
                Object[] options = {LanguageNames[3]};
                JOptionPane.showOptionDialog(null, PrimesNames[0], (String) Text.Phrases.get("0") + (String) Text.Phrases.get("Colon") + PrimesNames[1], JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            } else {
                //strOut=strOut+"<p><Font Color='red'>Disclaimer: This is a preliminary attempt at creating the Primes service.</Font></p>";
                //int LangCode=Integer.parseInt(Analyse.dayInfo.get("LS").toString());
                //if (LangCode==2 || LangCode==3 ){
                //strOut="<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><p><font face=\"Ponomar Unicode TT\" size=\"5\">"+strOut+"</font></p>";
                //System.out.println("Added Font");
                //  }

                PrimesWindow(strOut);
            }
        } catch (IOException j) {
        }


    }

    private void PrimesWindow(String textOut) {
        frames = new JFrame((String) Text.Phrases.get("0") + (String) Text.Phrases.get("Colon") + PrimesNames[1]);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textOut = textOut.replaceAll("</br>", "<BR>");
        textOut = textOut.replaceAll("<br>", "<BR>");
        strOut = textOut;
        //System.out.println(textOut);
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);
        output = new PrintableTextPane();
        output.setEditable(false);
        output.setSize(800, 700);
        output.setContentType("text/html; charset=UTF-8");
        output.setText(textOut);
        output.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(output);
        JMenuBar MenuBar = new JMenuBar();
        MenuFiles demo = new MenuFiles(Analyse.dayInfo);
        PrimeSelector trial = new PrimeSelector(Analyse.dayInfo);
        MenuBar.add(demo.createFileMenu(this));
        MenuBar.add(trial.createPrimeMenu());
        MenuBar.add(demo.createHelpMenu(this));
        frames.setJMenuBar(MenuBar);
        trial.addPropertyChangeListener(this);

        contentPane.add(scrollPane, BorderLayout.CENTER);
        frames.setContentPane(contentPane);
        frames.pack();
        frames.setSize(800, 700);
        frames.setVisible(true);
        Helpers orient = new Helpers(Analyse.dayInfo);
        orient.applyOrientation(frames, (ComponentOrientation) Analyse.dayInfo.get("Orient"));


        //scrollPane.top();
    }

    private String createPrimes() throws IOException {
        //OBTAIN THE DEFAULTS FOR THE SERVICE (WHAT WAS LAST USED!)
        Analyse.dayInfo.put("PS", SelectorP.getWhoValue());
        int TypeP = SelectorP.getTypeValue();
        Service ReadPrime=new Service(Analyse.dayInfo);
        //FIRST READ THE TONE FILES:
        int Weekday = Integer.parseInt(Analyse.dayInfo.get("dow").toString());
        //System.out.println(Weekday);
        int Tone = Integer.parseInt(Analyse.dayInfo.get("Tone").toString());
        if (Tone == 8) {
            Tone = 0;
        }
        //System.out.println(Tone);
        if (Tone != -1) {
            String FileName = octoecheosFileName + "Tone " + Tone;
            if (Weekday == 1) {
                FileName = FileName + "/Monday.xml";
            } else if (Weekday == 2) {
                FileName = FileName + "/Tuesday.xml";
            } else if (Weekday == 3) {
                FileName = FileName + "/Wednesday.xml";
            } else if (Weekday == 4) {
                FileName = FileName + "/Thursday.xml";
            } else if (Weekday == 5) {
                FileName = FileName + "/Friday.xml";
            } else if (Weekday == 6) {
                FileName = FileName + "/Saturday.xml";
            } else {
                FileName = FileName + "/Sunday.xml";
            }



            try {
                BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(Analyse.dayInfo.get("LS").toString(), FileName)), "UTF8"));
                QDParser.parse(this, frf);

            } catch (Exception Primes) {
                Primes.printStackTrace();
            }
        }

        //READ THE PENTECOSTARION!

        //Integer.parseInt(dayInfo.get(expression).toString())
        int nday = Integer.parseInt(Analyse.dayInfo.get("nday").toString());

        if (nday >= -70 && nday < 0) {
            filename = triodionFileName;
            lineNumber = Math.abs(nday);
        } else if (nday < -70) {
            // WE HAVE NOT YET REACHED THE LENTEN TRIODION
            filename = pentecostarionFileName;
            lineNumber = Integer.parseInt(Analyse.dayInfo.get("ndayP").toString()) + 1;
        } else {
            // WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
            filename = pentecostarionFileName;
            lineNumber = nday + 1;
        }

        filename += lineNumber >= 10 ? lineNumber : "0" + lineNumber; // CLEANED UP
        // READ THE PENTECOSTARION / TRIODION INFORMATION
        //IF THERE ARE SPECIAL TROPARION1's FROM THIS FILE THEY CAN OVERRIDE THE SET PIECES

        Day Readings = new Day(filename, Analyse.dayInfo);
        try {
            OrderedHashtable[] lessons = Readings.getReadings();
            OrderedHashtable Reading = (OrderedHashtable) lessons[0].get("Readings");
            OrderedHashtable lesson = (OrderedHashtable) Reading.get("Readings");
            OrderedHashtable reading = (OrderedHashtable) lesson.get("6th hour");
            //System.out.println("Reading == " +reading);
            OrderedHashtable lesson2 = (OrderedHashtable) reading.get("1");
            //System.out.println("Lesson 2 == "+lesson2);
            Reading6th = lesson2.get("Reading").toString();
        } catch (Exception e)  {           //There are no appointed readings
            Reading6th = "";
        }
        //System.out.println("Reading 6th = " + Reading6th);
                /*try
        {
        BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(Analyse.dayInfo.get("LS").toString(),filename)), "UTF8"));
        QDParser.parse(this, frf);
        }
        catch (Exception e)
        {
        e.printStackTrace();
        }*/

        //CHECK WHAT TYPE OF SERVICE WE ARE DEALING WITH
        //POTENTIAL STREAMLINING OF THE SERVICE: ALL THE RULES HAVE NOW BEEN OBTAINED EXCEPT FOR ANY OVERRIDES
        ServiceInfo ServicePrimes = new ServiceInfo("SEXTE",Analyse.dayInfo);
        OrderedHashtable PrimesTrial = ServicePrimes.ServiceRules();

        Type = PrimesTrial.get("Type").toString();
        LentenK = (String) PrimesTrial.get("LENTENK");

        String PrimesAdd1 = new String();

        if (Type.equals("None")) {
            //THERE ARE NO SERVICES TODAY, THAT IS, THE ROYAL HOURS ARE SERVED INSTEAD
            return "No Service Today";
        } else if (Type.equals("Paschal")) {

            return ReadPrime.startService(ServicesFileName + "PaschalHours.xml");
        }

        //I WOULD THEN NEED TO READ THE MENOLOGION, BUT I WILL NOT DO SO RIGHT NOW.
        //DETERMINE THE ORDERING OF THE TROPARIA AND KONTAKIA IF THERE ARE 2 OR MORE

        String strOut = new String();
        Analyse.dayInfo.put("PFlag1", TypeP);
        Analyse.dayInfo.put("PFlag2", 0);
        Analyse.dayInfo.put("PFlag3", 0);
        //NOTE PFlag2 == 3 for Holy Week Services!

        if (Type.equals("Lenten")) {
            Analyse.dayInfo.put("PFlag2", 1);

            if (LentenK != null) {
                Analyse.dayInfo.put("PFlag2", 2);
                //CREATE THE KATHISMA PART
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PKath6.xml"), "UTF8"));
                String Data = "<SERVICES>\r\n<LANGUAGE>\r\n<GET File=\"Kathisma" + LentenK + "\" Null=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
                out.write(Data);
                out.close();
            }
            //System.out.println("Hello Lent b");
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/TP6R.xml"), "UTF8"));
            //System.out.println(Reading6th);
            String Data = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out1a = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/TP6C.xml"), "UTF8"));
            String Data1a = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PROK61R.xml"), "UTF8"));
            String Data1 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PROK61C.xml"), "UTF8"));
            String Data2 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/STYX61R.xml"), "UTF8"));
            String Data3 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out4 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/STYX61C.xml"), "UTF8"));
            String Data4 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out5 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PROK61a.xml"), "UTF8"));
            String Data5 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out6 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PROK61b.xml"), "UTF8"));
            String Data6 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out7 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/Intro6.xml"), "UTF8"));
            String Data7 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out8 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/Reading6.xml"), "UTF8"));
            String Data8 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out9 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PROK62R.xml"), "UTF8"));
            String Data9 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out10 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PROK62C.xml"), "UTF8"));
            String Data10 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out11 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/STYX62R.xml"), "UTF8"));
            String Data11 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out12 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/STYX62C.xml"), "UTF8"));
            String Data12 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out13 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PROK62a.xml"), "UTF8"));
            String Data13 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            BufferedWriter out14 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PROK62b.xml"), "UTF8"));
            String Data14 = "<SERVICE>\r\n<LANGUAGE>\r\n";
            if (Reading6th != null) {
                if (Reading6th.length() > 0) {
                    //System.out.println(Reading6th);
                    Analyse.dayInfo.put("PFlag3", 1);
                    String nday1 = String.valueOf(-nday);
                    if (-nday < 10) {
                        nday1 = "0" + nday1;
                    }

                    Data = Data + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" Header=\"1\" What=\"/SEXTE/TROPARION/1\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\" />";
                    Data1a = Data1a + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/TROPARION/1\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
                    Data1 = Data1 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" Header=\"1\" What=\"/SEXTE/PROKEIMENON/1a\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
                    Data1 = Data1 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/PROKEIMENON/1b\" Who=\"R\"/>";
                    Data2 = Data2 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/PROKEIMENON/1a\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
                    Data2 = Data2 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/PROKEIMENON/1b\" Who=\"C\"/>";
                    Data3 = Data3 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/STICHOS/1\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
                    Data4 = Data4 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/STICHOS/1\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
                    Data5 = Data5 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/PROKEIMENON/1a\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
                    Data6 = Data6 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/PROKEIMENON/1b\" Who=\"C\" NewLine=\"1\"/>";
                    Data7 = Data7 + "\r\n<BIBLE getReading=\"" + Reading6th + "\" Who=\"SR\" NewLine=\"1\"/>";
                    Data8 = Data8 + "\r\n<BIBLE Verses=\"" + Reading6th + "\" Who=\"SR\" RedFirst=\"1\" Header=\"1\" NewLine=\"1\" />";
                    Data9 = Data9 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/PROKEIMENON/2a\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\" Header=\"1\"/>";
                    Data9 = Data9 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/PROKEIMENON/2b\" Who=\"R\" />";
                    Data10 = Data10 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/PROKEIMENON/2a\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
                    Data10 = Data10 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/PROKEIMENON/2b\" Who=\"C\" />";
                    Data11 = Data11 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/STICHOS/2\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
                    Data12 = Data12 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/STICHOS/2\" Who=\"C\" RedFirst=\"1\" NewLine=\"1\"/>";
                    Data13 = Data13 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/PROKEIMENON/2a\" Who=\"R\" RedFirst=\"1\" NewLine=\"1\"/>";
                    Data14 = Data14 + "\r\n<GETID Type=\"T\" Id=\"" + nday1 + "\" What=\"/SEXTE/PROKEIMENON/2b\" Who=\"C\" NewLine=\"1\"/>";

                }
            }
            Data = Data + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data1a = Data1a + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data1 = Data1 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data2 = Data2 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data3 = Data3 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data4 = Data4 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data5 = Data5 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data6 = Data6 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data7 = Data7 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data8 = Data8 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data9 = Data9 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data10 = Data10 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data11 = Data11 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data12 = Data12 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data13 = Data13 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            Data14 = Data14 + "\r\n</LANGUAGE>\r\n</SERVICE>";
            out.write(Data);
            out1a.write(Data1a);
            out1.write(Data1);
            out2.write(Data2);
            out3.write(Data3);
            out4.write(Data4);
            out5.write(Data5);
            out6.write(Data6);
            out7.write(Data7);
            out8.write(Data8);
            out9.write(Data9);
            out10.write(Data10);
            out11.write(Data11);
            out12.write(Data12);
            out13.write(Data13);
            out14.write(Data14);
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
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PTrop61.xml"), "UTF8"));
            String Data = "<SERVICE>\r\n<LANGUAGE>";
            String Data2 = "<SERVICE>\r\n<LANGUAGE>";
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PTrop62.xml"), "UTF8"));
            if (Troparion1 != null) {
                System.out.println("The first Troparion is " + Troparion1 + " Troparion2 is " + Troparion2);
                if (Troparion2 != null) {
                    //BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ServicesFileName+"Var/PTrop61.xml"),"UTF8"));
                    Data = Data + "\r\n<CREATE Who=\"\" What=\"TROPARION/" + Troparion1 + "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n";


                    //Dim out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ServicesFileName+"Var/PTrop62.xml"),"UTF8"));
                    Data2 = Data2 + "\r\n<CREATE Who=\"\" What=\"TROPARION/" + Troparion2 + "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n";


                } else {
                    //BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ServicesFileName+"Var/PTrop62.xml"),"UTF8"));
                    Data2 = Data2 + "\r\n<CREATE Who=\"\" What=\"TROPARION/" + Troparion1 + "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
                    //out.write(Data);
                    //out.close();
                }
            }
            Data = Data + "</SERVICE>\r\n</LANGUAGE>";
            Data2 = Data2 + "</SERVICE>\r\n</LANGUAGE>";
            out.write(Data);
            out.close();
            out2.write(Data2);
            out2.close();

        }

        //GET AND CREATE THE APPRORIATE KONTAKION
        //APROPRIATE KONTAKION MUST STILL BE CREATED!
       // System.out.println(Kontakion1);
        if (Kontakion1 != null) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/" + Analyse.dayInfo.get("LS").toString() + ServicesFileName + "Var/PKont6.xml"), "UTF8"));
            String Data = "<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"SR\" What=\"KONTAKION/" + Kontakion1 + "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
            out.write(Data);
            out.close();
        }
        //Else we are dealing with a Lenten service that does not have any variable parts.
        //System.out.println("Sixth Hour: "+Analyse.dayInfo.get("PFlag3"));

        strOut = ReadPrime.startService(ServicesFileName + "SixthHour.xml") + "</p>";


        return strOut;
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

            if (Analyse.evalbool(table.get("Cmd").toString()) == false) {

                return;
            }
        }
        //if(elem.equals("LANGUAGE") || elem.equals("TONE"))
        //{
        read = true;
        //}
        if (elem.equals("TEXT") && read) {
            text += (String) table.get("Value");

        }
        if (elem.equals("SCRIPTURE") && read) {
            String type = (String) table.get("Type");
            String reading = (String) table.get("Reading");
            //System.out.println("The readings that were found were "+type+" "+reading);
            if (type.equals("6th hour")) {
                Reading6th = reading;
            }
        }
        if (elem.equals("SEXTE") && read) {
            //WE ARE DEALING WITH THE INFORMATION FOR PRIMES (THERE COULD BE INFORMATION FOR OTHER SERVICES)
            //THE VARIABLE COMPONETS IN THIS SERVICE ARE GIVEN BELOW
            String value = (String) table.get("Type");
            if (value != null) {
                Type = (String) table.get("Type");
            }
            value = (String) table.get("TROPARION1");
            if (value != null) {
                Troparion1 = (String) table.get("TROPARION1");
            }
            value = (String) table.get("KONTAKION1");
            if (value != null) {
                Kontakion1 = (String) table.get("KONTAKION1");
            }
            value = (String) table.get("KONTAKION2");
            if (value != null) {
                Kontakion1 = (String) table.get("KONTAKION2");
            }
            value = (String) table.get("TROPARION2");
            if (value != null) {
                Troparion1 = (String) table.get("TROPARION2");
            }

            value = (String) table.get("LENTENK");
            if (value != null) {
                LentenK = (String) table.get("LENTENK");
                //System.out.println(LentenK);
            }

        }
        //OTHER LITURGICAL SERVICES WOULD FOLLOW HERE

    }

    public void endElement(String elem) {
        if (elem.equals("LANGUAGE") || elem.equals("TONE")) {
            read = false;
        }
    }

    public void text(String text) {
    }

    private boolean eval(String expression) throws IllegalArgumentException {
        return false;
    }

    public static void main(String[] argz) {

        //new SixthHour(3);	//CREATE THE SERVICE FOR WEDNESDAY FOR TONE 1.
    }

    public String readText(String filename) {
        try {
            text = new String();
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(Analyse.dayInfo.get("LS").toString(), filename)), "UTF8"));
            QDParser.parse(this, fr);
            if (text.length() == 0) {
                text = null;
            }

        } catch (Exception e) {
            //SERIOUS PROBLEM MISSING A PART OF THE SERVICE!
            System.out.println(filename);
            e.printStackTrace();
            return null;
        }

        return text;
    }

    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem) (e.getSource());
        String name = source.getText();
        if (name.equals(HelpNames[2])) {
            Helpers orient = new Helpers(Analyse.dayInfo);
            orient.applyOrientation(new About(Analyse.dayInfo), (ComponentOrientation) Analyse.dayInfo.get("Orient"));
        }
        if (name.equals(HelpNames[0])) {
            //LAUNCH THE HELP FILE
        }
        if (name.equals(FileNames[1])) {
            //SAVE THE CURRENT WINDOW
            helper.SaveHTMLFile(PrimesNames[1] + " " + today, strOut);

        }
        if (name.equals(FileNames[4])) {
            //CLOSE THE PRIMES FRAME
            if (helper.closeFrame(LanguageNames[7])) {
                frames.dispose();
            }
        }
        if (name.equals(FileNames[6])) {
            //PRINT THE FILE
            helper.sendHTMLToPrinter(output);
        }
        String s = "Action event detected."
                + newline
                + "    Event source: " + source.getText()
                + " (an instance of " + getClassName(source) + ")";
        System.out.println(s);
        //output.append(s + newline);
        //output.setCaretPosition(output.getDocument().getLength());
    }

    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");
        return classString.substring(dotIndex + 1);
    }

    public void itemStateChanged(ItemEvent e) {
        JMenuItem source = (JMenuItem) (e.getSource());
        String s = "Item event detected."
                + newline
                + "    Event source: " + source.getText()
                + " (an instance of " + getClassName(source) + ")"
                + newline
                + "    New state: "
                + ((e.getStateChange() == ItemEvent.SELECTED)
                ? "selected" : "unselected");
        System.out.println(s);
        //output.append(s + newline);
        //output.setCaretPosition(output.getDocument().getLength());
    }

    public void propertyChange(PropertyChangeEvent e) {
        //THERE IS NOTHING HERE TO DO??
        try {
            output.setText(createPrimes());
            output.setCaretPosition(0);
        } catch (Exception e1) {
        }

    }
}
