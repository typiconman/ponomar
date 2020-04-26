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

import Ponomar.astronomy.Astronomy;
import Ponomar.astronomy.Paschalion;
import Ponomar.astronomy.Sunrise;
import Ponomar.calendar.JCalendar;
import Ponomar.calendar.JDate;
import Ponomar.calendar.PCalendar;
import Ponomar.internationalization.LanguagePack;
import Ponomar.internationalization.LanguageSelector;
import Ponomar.panels.GospelSelector;
import Ponomar.panels.IconDisplay;
import Ponomar.panels.PrintableTextPane;
import Ponomar.parsing.Commemoration1;
import Ponomar.parsing.Day;
import Ponomar.parsing.Fasting;
import Ponomar.readings.DivineLiturgy1;
import Ponomar.readings.Matins;
import Ponomar.services.NinthHour;
import Ponomar.services.Primes;
import Ponomar.services.RoyalHours;
import Ponomar.services.SixthHour;
import Ponomar.services.ThirdHour;
import Ponomar.utility.Helpers;
import Ponomar.utility.OrderedHashtable;
import Ponomar.utility.RuleBasedNumber;
import Ponomar.utility.StringOp;

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
public class Main extends JFrame implements PropertyChangeListener, HyperlinkListener, ActionListener {
    // First, some relevant constants

    private final static String configFileName = "ponomar.config"; // CONFIGURATIONS FILE
    //private final static String generalFileName="Ponomar/xml/";
    private final static String triodionFileName = "xml/triodion/";   // TRIODION FILE
    private final static String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
    private static String newline = "\n";
    // Elements of the interface
    JDate today; 		// "TODAY" (I.E. THE DATE WE'RE WORKING WITH
    private JCalendar calendar; 	// THE CALENDAR OBJECT
    private PrintableTextPane text; 	// MAIN TEXT AREA FOR OUTPUT
    private JDate pascha; 		// THIS YEAR'S PASCHA
    private JDate pentecost; 	// THIS YEAR'S PENTECOST
    private Stack fastInfo;		// CONTAINS A VECTOR OF THE FASTING INFORMATION FOR TODAY, WHICH IS LATER PASSED TO CONVOLVE()
    private OrderedHashtable readings;	// CONTAINS TODAY'S SCRIPTURE READING
    private String output;  	// TODAY'S CALENDAR OUTPUT
    private Boolean inited = false; // PREVENTS MULTIPLE READING OF XML FILES ON LAUNCH
    private Bible bible;
    private GospelSelector GospelLocation;		//THE GOSPEL SELECTOR OBJECT
    private String GLocation;					//STORES THE PATH (FOLDER) TO THE APPROPRIATE GOSPEL READING LOCATION FILES
    private LanguageSelector LanguageLocation;
    //private String LLocation;
    //MY ATTEMPT AT SORTING THE READINGS FOR THE LITURGY 2008/05/19 n.s. YURI SHARDT
    private OrderedHashtable PentecostarionS;		//CONTAINS THE PENTECOSTARION READINGS (SEQUENTIAL (rjadovoje) READINGS!)
    private OrderedHashtable MenalogionS;		//CONTAINS THE MENALOGION READINGS, EXCLUDING ANY FLOATERS
    private OrderedHashtable FloaterS;			//CONTAINS THE FLOATER READINGS.
    private OrderedHashtable[] ReadScriptures;
    private JMenuBar MenuBar;
    private MenuFiles demo;
    private LanguagePack Phrases;
    private static String[] toneNumbers;
    private static String[] Errors;
    private static String[] MainNames;
    private static boolean read = false;		//DETERMINES WHICH LANGUAGE WILL BE READ
    private String[] SaintNames;
    private String OptionsNames;
    private String[] FileNames;
    private String[] ServiceNames;
    private String[] BibleName;
    private String[] HelpNames;
    //Get the Correct Fonts
    private String DisplayFont = new String(); //ALLOWS A CUSTOM FONT AND SIZE TO BE SPECIFIED FOR A GIVEN BIBLE READING: REQUIRED FOR OLD CHURCH SLAVONIC AT PRESENT
    private String DisplaySize = "12";  //UNTIL A COMPLETE UNICODE FONT IS AVAILIBLE.
    private Font DefaultFont = new Font("", Font.BOLD, 12);		//CREATE THE DEFAULT FONT
    private Font CurrentFont = DefaultFont;
    private String RSep = new String();
    private String CSep = new String();
    private String Colon = new String();
    private String Ideographic = new String();
    private DoSaint1 SaintLink;
    private IconDisplay displayIcon;
    private Vector IconImages;
    private Vector IconNames;
    private String OrderBox;
    private StringOp Analyse = new StringOp();
    //private GospelSelector Selector;
    Helpers findLanguage;

    // CONSTRUCTOR
    public Main() {
        //super("Ponomar");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        //WE NEED THIS HANDY STORER OF VALUES NOW.
        //StringOp.dayInfo = new OrderedHashtable();
        //DETERMINE THE DEFAULTS
        ConfigurationFiles.setDefaults(new OrderedHashtable());
        ConfigurationFiles.ReadFile();
        LanguageLocation = new LanguageSelector(Analyse.getDayInfo());

        Analyse.getDayInfo().put("LS", LanguageLocation.getLValue());
        Phrases = new LanguagePack(Analyse.getDayInfo());
        //Changing language storage format
        findLanguage = new Helpers(Analyse.getDayInfo());

        toneNumbers = Phrases.obtainValues((String) Phrases.getPhrases().get("Tones"));
        SaintNames = Phrases.obtainValues((String) Phrases.getPhrases().get("SMenu"));
        OptionsNames = (String) Phrases.getPhrases().get("Options");
        FileNames = Phrases.obtainValues((String) Phrases.getPhrases().get("File"));
        ServiceNames = Phrases.obtainValues((String) Phrases.getPhrases().get("Services"));
        BibleName = Phrases.obtainValues((String) Phrases.getPhrases().get("Bible"));
        HelpNames = Phrases.obtainValues((String) Phrases.getPhrases().get("Help"));

        Errors = Phrases.obtainValues((String) Phrases.getPhrases().get("Errors"));
        MainNames = Phrases.obtainValues((String) Phrases.getPhrases().get("Main"));
        DisplayFont = (String) Phrases.getPhrases().get("FontFaceM");
        DisplaySize = (String) Phrases.getPhrases().get("FontSizeM");
        OrderBox = (String) Phrases.getPhrases().get("OrderBox");

        Font value1 = (Font) UIManager.get("Menu.font");
        if (DisplaySize == null || DisplaySize.equals("")) {
            DisplaySize = Integer.toString(value1.getSize());
        }
        if (DisplayFont == null || DisplayFont.equals("")) {
            DisplayFont = value1.getFontName();
        }
        DisplaySize = Integer.toString(Math.max(Integer.parseInt(DisplaySize), value1.getSize())); //If the default user's font size is larger than the required there is not need to change it.
        //The specified fonts sizes are the mininum required.
        CurrentFont = new Font(DisplayFont, Font.PLAIN, Integer.parseInt(DisplaySize));
        //System.out.println(this.getFont());
        //System.out.println("Pause");
        //setDefaultLookAndFeelDecorated( true );
        //UIManager.put("Frame.font",CurrentFont);
        //this.setFont(CurrentFont);
        //This is a nifty way to set the default font for displaying everything in a programme. I (Y.S.) will
        //later work to implement it properly. At present, there seem to be some technical issues with obtaining
        //everything properly.
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                Font keyF = (Font) value;
                String[] splitkey = key.toString().replace(".", ":").split(":");
                //This prevents the font from being changed for those things that are to remain in the Latin alphabet!
                if (splitkey.length > 1) {
                    if (splitkey[splitkey.length - 1].equals("acceleratorFont")) {
                        continue;
                    }
                    /*if (splitkey[0].equals("Button"))
                    {
                    continue;
                    }*/
                }
                if (key.toString().equals("MenuItem.acceleratorFont")) {
                    continue;
                }
                Font NewFont = new Font(CurrentFont.getFontName(), keyF.getStyle(), CurrentFont.getSize());
                UIManager.put(key, NewFont);
                //System.out.println(key);
            }
        }

        //System.out.println(this.getFont());
        setTitle((String) Phrases.getPhrases().get("0"));
        RSep = (String) Phrases.getPhrases().get("ReadSep");
        CSep = (String) Phrases.getPhrases().get("CommSep");
        Colon = (String) Phrases.getPhrases().get("Colon");
        Analyse.getDayInfo().put("FontFaceM", DisplayFont);
        Analyse.getDayInfo().put("FontSizeM", DisplaySize);
        Analyse.getDayInfo().put("ReadSep", RSep);
        Analyse.getDayInfo().put("Colon", Colon);
        Ideographic = (String) Phrases.getPhrases().get("Ideographic");
        Analyse.getDayInfo().put("Ideographic", Ideographic);
        GospelLocation = new GospelSelector(Analyse.getDayInfo());

        //ADD A MENU BAR Y.S. 2008/08/11 n.s.
        demo = new MenuFiles(Analyse.getDayInfo().clone());
        MenuBar = new JMenuBar();
        MenuBar.add(demo.createFileMenu(this));
        MenuBar.add(demo.createOptionsMenu(this,this));
        MenuBar.add(demo.createSaintsMenu(this));
        MenuBar.add(demo.createServicesMenu(this));
        MenuBar.add(demo.createBibleMenu(this));
        MenuBar.add(demo.createHelpMenu(this));
        MenuBar.setFont(CurrentFont);
        //MenuBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        setJMenuBar(MenuBar);

        JPanel left = new JPanel(new GridLayout(3, 0));
        calendar = new JCalendar(Analyse.getDayInfo());
        //System.out.println(calendar);
        calendar.addPropertyChangeListener(this);
        left.setLayout(new BorderLayout());
        left.add(calendar, BorderLayout.NORTH);
        displayIcon = new IconDisplay(new String[0], new String[0], Analyse.getDayInfo());
        left.add(displayIcon, BorderLayout.CENTER);



        JPanel right = new JPanel();
        text = new PrintableTextPane();
        text.setEditable(false);
        text.addHyperlinkListener(this);
        right.setLayout(new BorderLayout());
        right.add(text, BorderLayout.CENTER);
        right.setSize(200, 400);
        JScrollPane scrollPane3 = new JScrollPane(text);
        right.add(scrollPane3);

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setLeftComponent(left);
        splitter.setRightComponent(right);

        today = new JDate(calendar.getMonth(), calendar.getDay(), calendar.getYear());
                
        setContentPane(splitter);

        Locale place = new Locale(Phrases.getPhrases().get("Language").toString(), Phrases.getPhrases().get("Country").toString());
        Helpers orient = new Helpers(Analyse.getDayInfo());
        Analyse.getDayInfo().put("Locale", place);
        Analyse.getDayInfo().put("Orient", ComponentOrientation.getOrientation(place));
        orient.applyOrientation(this, ComponentOrientation.getOrientation(place));
        this.validate();

        pack();
        setSize(700, 500);
        setVisible(true);

        pascha = Paschalion.getPascha(today.getYear());
        pentecost = Paschalion.getPentecost(today.getYear());


        inited = true;
        Dimension screen = this.getSize();
        //Default screen size issues for East Asian languages!
        if (value1.getSize() < Integer.parseInt(DisplaySize)) {
            Dimension defaultScreen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

            //System.out.println(screen);
            int newSize = Integer.parseInt(DisplaySize);
            int MaxW = 95 * defaultScreen.width / 100;
            int MaxH = 95 * defaultScreen.height / 100;
            screen.width = java.lang.Math.min(screen.width * newSize / value1.getSize(), MaxW);
            screen.height = java.lang.Math.min(screen.height * newSize / value1.getSize(), MaxH);
            this.setSize(screen);
            //System.out.println(screen);
        }

        write();
    }

    public void propertyChange(PropertyChangeEvent e) {
        
        if (inited == true) {
            // FIND OUT THE OLD YEAR
            int year = today.getYear();
            today = new JDate(calendar.getMonth(), calendar.getDay(), calendar.getYear());
            if (year != today.getYear()) {
                pascha = Paschalion.getPascha(today.getYear());
                pentecost = Paschalion.getPentecost(today.getYear());
                /*StringOp.dayInfo.clear();
                StringOp.dayInfo.put("FontFaceM",DisplayFont);
                StringOp.dayInfo.put("FontSizeM",DisplaySize);
                StringOp.dayInfo.put("ReadSep",RSep);
                StringOp.dayInfo.put("Colon",Colon);
                Ideographic=(String)Phrases.Phrases.get("Ideographic");
                StringOp.dayInfo.put("Ideographic",Ideographic);
                 */
            }

            write();
        }
    }

    public void actionPerformed(ActionEvent e) {
        Helpers helper = new Helpers(Analyse.getDayInfo());
        JMenuItem source = (JMenuItem) (e.getSource());
        String name = source.getText();
        if (name.equals(HelpNames[2])) {
            //new About();
            Helpers orient = new Helpers(Analyse.getDayInfo());
            orient.applyOrientation(new About(Analyse.getDayInfo()), (ComponentOrientation) Analyse.getDayInfo().get("Orient"));
        }
        if (name.equals(HelpNames[0])) {
            //HELP FILES
        }
        if (name.equals(FileNames[1])) {
            //SAVE THE CURRENT WINDOW
            helper.saveHTMLFile(MainNames[5] + " " + today + ".html", "<html><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"><title>" + (String) Phrases.getPhrases().get("0") + Colon + today + "</title>" + output);
        }
        if (name.equals(FileNames[4])) {
            if (helper.closeFrame(MainNames[6])) {
                System.exit(0);
            }
        }
        if (name.equals(ServiceNames[1])) {
            //DIVINE LITURGY
        }
        if (name.equals(ServiceNames[2])) {
            //VESPERS
        }
        if (name.equals(ServiceNames[3])) {
            //COMPLINE
        }
        if (name.equals(ServiceNames[4])) {
            //MATINS
        }
        if (name.equals(ServiceNames[5])) {
            //Create the primes service
            new Primes(today, Analyse.getDayInfo());
        }
        if (name.equals(ServiceNames[6])) {
            new ThirdHour(today, Analyse.getDayInfo());
        }
        if (name.equals(ServiceNames[7])) {
            new SixthHour(today, Analyse.getDayInfo());
            //SEXT
        }
        if (name.equals(ServiceNames[8])) {
            new NinthHour(today, Analyse.getDayInfo());
            //NONE
        }
        if (name.equals(ServiceNames[9])) {
            //ROYAL HOURS
            new RoyalHours(today, Analyse.getDayInfo());
        }
        if (name.equals(ServiceNames[10])) {
            //ALL-NIGHT VIGIL
        }
        if (name.equals(ServiceNames[11])) {
            //MIDNIGHT OFFICE
        }
        if (name.equals(ServiceNames[12])) {
            //TYPICA
        }
        if (name.equals(BibleName[0])) {
            //Launch the Bible Reader
            Helpers orient = new Helpers(Analyse.getDayInfo());
            orient.applyOrientation(new Bible("Gen", "1:1-31", Analyse.getDayInfo()), (ComponentOrientation) Analyse.getDayInfo().get("Orient"));
        }
        if (name.equals(FileNames[6])) {
            helper.sendHTMLToPrinter(text);
        }
        if (name.equals( Phrases.getPhrases().get("OptionMenu"))){
            Options optionsN=new Options(Analyse.getDayInfo());
            optionsN.addPropertyChangeListener("CalendarChange",this); //nifty way of only listening to what I want to hear!
            optionsN.createDefaultWindow();

        }

        String s = "Action event detected." + newline + "    Event source: " + source.getText() + " (an instance of " + getClassName(source) + ")";
        System.out.println(s);
        //output.append(s + newline);
        //output.setCaretPosition(output.getDocument().getLength());
    }


    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType().toString() == "ACTIVATED") {
            String cmd = e.getDescription();
            String[] parts = cmd.split("#");
            if (parts[0].indexOf("reading") != -1) {
                try {
                    bible.update(parts[1], parts[2]);
                    bible.show();
                } catch (NullPointerException npe) {
                    bible = new Bible(parts[1], parts[2], Analyse.getDayInfo());
                    Helpers orient = new Helpers(Analyse.getDayInfo());
                    orient.applyOrientation(bible, (ComponentOrientation) Analyse.getDayInfo().get("Orient"));
                }
            } else {
                parts = cmd.split("\\?");
                if (parts[0].indexOf("goDoSaint") != -1) {

                    String[] parts2 = parts[1].split("=");
                    //System.out.println(parts2[1]);
                    String[] parts3 = parts2[1].split(",");

                    Commemoration1 trial1 = new Commemoration1(parts3[parts3.length - 2], parts3[parts3.length - 1], Analyse.getDayInfo());
                    if (SaintLink == null) {
                        System.out.println(parts3[parts3.length - 1]);

                        SaintLink = new DoSaint1(trial1, Analyse.getDayInfo());
                    } else {
                        SaintLink.refresh(trial1);
                    }

                }
            }
        }
    }

   
    

    private void write() {
     
         output = "<body style=\"font-family:" + DisplayFont + ";font-size:" + DisplaySize + "pt\">";
     


        String AMC = (String) Phrases.getPhrases().get("AMC");
        String AML = (String) Phrases.getPhrases().get("AML");
        String Format = "";
        if (AMC.equals("1")) {
            PCalendar checking = new PCalendar(today, PCalendar.julian, Analyse.getDayInfo());
            Format = (String) Phrases.getPhrases().get("AM");
            if (Analyse.getDayInfo().get("Ideographic").equals("1"))
                {
                    RuleBasedNumber convertN=new RuleBasedNumber(Analyse.getDayInfo());
                    
                    Format = Format.replace("^YYAM", convertN.getFormattedNumber(Long.parseLong(Integer.toString((int) checking.getAM()))));

                }
                else
                {
		Format = Format.replace("^YYAM", Integer.toString((int) checking.getAM()));
                }
        }
        //System.out.println("AML = " + AML.equals("B"));
        if (AML.equals("B")) {
            output += "<B>" + Format + today.toString(Analyse.getDayInfo()) + "</B><BR>";
        } else {
            output += "<B>" + today.toString(Analyse.getDayInfo()) + Format + "</B><BR>";
        }

        output += MainNames[0] + Colon + (String) today.getGregorianDateS(Analyse.getDayInfo()) + "<BR>";
        String filename = "";
        int lineNumber = 0;
        int dow = today.getDayOfWeek();
        int doy = today.getDoy();
        int nday = (int) JDate.difference(today, this.pascha);
        int ndayP = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
        //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
        int ndayF = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));

        //Clearing the holders for the icons and names
        IconImages = new Vector();
        IconNames = new Vector();

        // PUT THE RELEVANT DATA IN THE HASH
        Analyse.getDayInfo().put("dow", dow);	// THE DAY'S DAY OF WEEK
        Analyse.getDayInfo().put("doy", doy);	// THE DAY'S DOY (see JDate.java for specification)
        //System.out.println(doy);
        Analyse.getDayInfo().put("nday", nday);	// THE NUMBER OF DAYS BEFORE (-) OR AFTER (+) THIS YEAR'S PASCHA
        Analyse.getDayInfo().put("ndayP", ndayP);	// THE NUMBER OF DAYS AFTER LAST YEAR'S PASCHA
        //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
        Analyse.getDayInfo().put("ndayF", ndayF);	// THE NUMBER OF DAYS TO NEXT YEAR'S PASCHA (CAN BE +ve or -ve).
        //ADDING THE TYPE OF GOSPEL READINGS TO BE FOLLOWED
        Analyse.getDayInfo().put("GS", GospelSelector.getGValue());
        
        //INTERFACE LANGUAGE
        Analyse.getDayInfo().put("LS", LanguageLocation.getLValue());
        Analyse.getDayInfo().put("Year", today.getYear());
        Analyse.getDayInfo().put("dRank", 0); //The default rank for a day is 0. Y.S. 2010/02/01 n.s.
        Analyse.getDayInfo().put("Ideographic", Ideographic);

        readings = new OrderedHashtable();
        fastInfo = new Stack();
        //MY ATTEMPT AT SORTING THE READINGS FOR THE LITURGY 2008/05/24 n.s. YURI SHARDT
		/*ReadScriptures = new OrderedHashtable[3];		//CONTAINS A SORTED ARRAY OF ALL THE READINGS
        ReadScriptures[0] = new OrderedHashtable();		//STORES THE PENTECOSTARION READINGS (SEQUENTIAL (rjadovoje) READINGS!)
        ReadScriptures[1] = new OrderedHashtable();		//CONTAINS THE MENALOGION READINGS, EXCLUDING ANY FLOATERS
        ReadScriptures[2] = new OrderedHashtable();		//CONTAINS THE FLOATER READINGS.
         */
        //TESTING THE LANGUAGE PACKS
        String rough = (String) Phrases.getPhrases().get("1");
        String[] final1 = rough.split(",");
        //System.out.println(output);



        // GET THE DAY'S ASTRONOMICAL DATA
        Sunrise sunrise = new Sunrise(Analyse.getDayInfo());
        String[] sunriseSunset = sunrise.getSunriseSunsetString(today, (String) ConfigurationFiles.getDefaults().get("Longitude"), (String) ConfigurationFiles.getDefaults().get("Latitude"), (String) ConfigurationFiles.getDefaults().get("TimeZone"));
        output += "<BR>" + MainNames[1] + sunriseSunset[0];
        output += "<BR>" + MainNames[2] + sunriseSunset[1];
        output += "<BR><BR>"; //<B>"+MainNames[3]+"</B>"+Colon+ Paschalion.getLunarPhaseString(today) +"<BR><BR>";
        // getting rid of the lunar phase until we program a paschalion ...
        //adding the civil Lunar phase by request of Mitrophan
        Astronomy sky = new Astronomy();

        output += MainNames[3] + sky.lunarphase(today.getJulianDay(), Analyse.getDayInfo());
        output += "<BR><BR>";

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

        filename += lineNumber >= 10 ? lineNumber : "0" + lineNumber; // CLEANED UP
        //System.out.println("++++++++++++++++++++++\n"+filename+"\n+++++++++++++++++++\n");
        //System.out.println("File name in Main: " + Analyse.getDayInfo().get("LS").toString());
        Day PaschalCycle = new Day(filename, Analyse.getDayInfo());

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
        Day SolarCycle = new Day(filename, Analyse.getDayInfo());
        Analyse.getDayInfo().put("dRank", Math.max(SolarCycle.getDayRank(), PaschalCycle.getDayRank()));
        output += PaschalCycle.getCommsHyper() + CSep;
        output += SolarCycle.getCommsHyper();
        Analyse.getDayInfo().put("Tone", PaschalCycle.getTone());


        String collection = "";
        output += "<BR><BR>";
        OrderedHashtable[] PaschalReadings = PaschalCycle.getReadings();
        //System.out.println("Length of Ordinary Readings="+PaschalReadings.length);

        OrderedHashtable[] MenaionReadings = SolarCycle.getReadings();
        Bible ShortForm = new Bible(Analyse.getDayInfo());
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
                    DivineLiturgy1 trial1 = new DivineLiturgy1(Analyse.getDayInfo());
                    String type1 = (String) Phrases.getPhrases().get("apostol");
                    output += "<B>" + type1 + "</B>" + Colon;
                    //System.out.println(readingsA);
                    output += trial1.Readings(readingsA, "apostol", today);
                    output += RSep;
                }
                if (!gospel.get(0).equals("")) {
                    readingsA.put("Readings", gospel);
                    readingsA.put("Rank", Rank);
                    readingsA.put("Tag", Tag);
                    String type1 = (String) Phrases.getPhrases().get("gospel");
                    DivineLiturgy1 trial1 = new DivineLiturgy1(Analyse.getDayInfo());
                    output += "<B>" + type1 + "</B>" + Colon;
                    output += trial1.Readings(readingsA, "gospel", today);
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
            if (element1.equals("MATINS")) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    output += RSep;
                }
                Vector matins2 = new Vector();

                for (int j = 0; j < Readings.size(); j++) {
                    OrderedHashtable matins = (OrderedHashtable) Readings.get(j);
                    //System.out.println("In Main1, we have "+Readings.get(j));
                    OrderedHashtable stepE = (OrderedHashtable) matins.get("matins");
                    if (stepE == null) {
                        stepE = (OrderedHashtable) matins.get("1");
                    }
                    //OrderedHashtable stepE=(OrderedHashtable)matins.get("matins");
                    //System.out.println("In Main1, we have "+matins2);
                    //System.out.println(stepE);

                    if (stepE != null) {
                        matins2.add(stepE.get("Reading").toString());
                    } else {
                        matins2.add("");
                    }
                }

                OrderedHashtable readingsA = new OrderedHashtable();





                readingsA.put("Readings", matins2);
                readingsA.put("Rank", Rank);
                readingsA.put("Tag", Tag);
                Matins trial1 = new Matins(Analyse.getDayInfo());
                String type1 = (String) Phrases.getPhrases().get("matins");
                output += "<B>" + type1 + "</B>" + Colon;
                //System.out.println(readingsA);
                output += trial1.Readings(readingsA, today);
                //output+=RSep;


                continue;

            }
            if (firstTime) {
                firstTime = false;
            } else {
                output += RSep;
            }
            String type1 = (String) Phrases.getPhrases().get(element1.toLowerCase());
            output += "<B>" + type1 + "</B>" + Colon;
            for (int i = 0; i < Readings.size(); i++) {
                OrderedHashtable Reading = (OrderedHashtable) Readings.get(i);
                String Name = "";
                if (i != 0) {
                    output += RSep;
                }
                //System.out.println(Reading);
                //System.out.println(Tag.get(i));
                boolean first = true;

                for (Enumeration e2 = Reading.enumerateKeys(); e2.hasMoreElements();) {
                    if (first) {
                        first = false;
                    } else {
                        output += RSep;
                    }
                    String element2 = e2.nextElement().toString();
                    OrderedHashtable stuff = (OrderedHashtable) Reading.get(element2);
                    String BibleText = stuff.get("Reading").toString();
                    output += ShortForm.getHyperlink(BibleText);
                }
                if (Readings.size() > 1) {
                    output += Tag.get(i).toString();

                }

            }//output += RSep;
        }



        OrderedHashtable iconsP = (OrderedHashtable) PaschalCycle.getIcon();
        OrderedHashtable iconsM = (OrderedHashtable) SolarCycle.getIcon();
        //String[] ss = (String[])v.toArray(new String[v.size()]);
        Vector ImageList = (Vector) iconsM.get("Images");
        Vector NamesList = (Vector) iconsM.get("Names");
        String[] iconImages = new String[ImageList.size()];
        String[] iconNames = new String[NamesList.size()];

        iconImages = (String[]) ImageList.toArray(new String[ImageList.size()]);
        iconNames = (String[]) NamesList.toArray(new String[NamesList.size()]);



        {
            displayIcon.updateImagesFiled(iconImages, iconNames);

        }

        //THIS IS NOW REPLACED BY THE NEW PROGRAMME, THAT SIMPLIFIES THE DETERMINATION OF THE FAST.
        String[] FastNames = Phrases.obtainValues((String) Phrases.getPhrases().get("Fasts"));
        Fasting getfast = new Fasting(Analyse.getDayInfo());
        output += "<BR><BR>" + getfast.FastRules() + "<BR><BR>";
        //output+="</FONT>";
        output += "</body>";

        text.setContentType("text/html; charset=UTF-8");
        text.setFont(CurrentFont);
        text.setText(output);
        text.setCaretPosition(0);



    }

    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");
        return classString.substring(dotIndex + 1);
    }

    public static void main(String[] argz) {
        new Main();
    }
}
