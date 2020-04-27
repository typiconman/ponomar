package net.ponomar;

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

import net.ponomar.astronomy.Astronomy;
import net.ponomar.astronomy.Paschalion;
import net.ponomar.astronomy.Sunrise;
import net.ponomar.calendar.JCalendar;
import net.ponomar.calendar.JDate;
import net.ponomar.calendar.PCalendar;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.internationalization.LanguageSelector;
import net.ponomar.panels.GospelSelector;
import net.ponomar.panels.IconDisplay;
import net.ponomar.panels.PrintableTextPane;
import net.ponomar.parsing.Commemoration;
import net.ponomar.parsing.Day;
import net.ponomar.parsing.Fasting;
import net.ponomar.readings.DivineLiturgy;
import net.ponomar.readings.Matins;
import net.ponomar.readings.utility.ReadingUtility;
import net.ponomar.services.NinthHour;
import net.ponomar.services.Primes;
import net.ponomar.services.RoyalHours;
import net.ponomar.services.SixthHour;
import net.ponomar.services.ThirdHour;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
import net.ponomar.utility.OrderedHashtable;
import net.ponomar.utility.RuleBasedNumber;
import net.ponomar.utility.StringOp;

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

    private static final String READINGS_KEY = "Readings";
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
    private GospelSelector gospelLocation;		//THE GOSPEL SELECTOR OBJECT
    private String gLocation;					//STORES THE PATH (FOLDER) TO THE APPROPRIATE GOSPEL READING LOCATION FILES
    private LanguageSelector languageLocation;
    //private String LLocation;
    //MY ATTEMPT AT SORTING THE READINGS FOR THE LITURGY 2008/05/19 n.s. YURI SHARDT
    private OrderedHashtable pentecostarionS;		//CONTAINS THE PENTECOSTARION READINGS (SEQUENTIAL (rjadovoje) READINGS!)
    private OrderedHashtable menalogionS;		//CONTAINS THE MENALOGION READINGS, EXCLUDING ANY FLOATERS
    private OrderedHashtable floaterS;			//CONTAINS THE FLOATER READINGS.
    private OrderedHashtable[] readScriptures;
    private JMenuBar menuBarElement;
    private MenuFiles demo;
    private LanguagePack phrases;
    private static String[] toneNumbers;
    private static String[] errors;
    private static String[] mainNames;
    private static boolean read = false;		//DETERMINES WHICH LANGUAGE WILL BE READ
    private String[] saintNames;
    private String optionsNames;
    private String[] fileNames;
    private String[] serviceNames;
    private String[] bibleName;
    private String[] helpNames;
    //Get the Correct Fonts
    private String displayFont = ""; //ALLOWS A CUSTOM FONT AND SIZE TO BE SPECIFIED FOR A GIVEN BIBLE READING: REQUIRED FOR OLD CHURCH SLAVONIC AT PRESENT
    private String displaySize = "12";  //UNTIL A COMPLETE UNICODE FONT IS AVAILIBLE.
    private Font defaultFont = new Font("", Font.BOLD, 12);		//CREATE THE DEFAULT FONT
    private Font currentFont = defaultFont;
    private String rSep = "";
    private String cSep = "";
    private String colon = "";
    private String ideographic = "";
    private DoSaint saintLink;
    private IconDisplay displayIcon;
    private Vector iconImages;
    private Vector iconNames;
    private String orderBox;
    private StringOp analyse = new StringOp();
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
        languageLocation = new LanguageSelector(analyse.getDayInfo());

        analyse.getDayInfo().put("LS", languageLocation.getLValue());
        phrases = new LanguagePack(analyse.getDayInfo());
        //Changing language storage format
        findLanguage = new Helpers(analyse.getDayInfo());

        toneNumbers = phrases.obtainValues((String) phrases.getPhrases().get("Tones"));
        saintNames = phrases.obtainValues((String) phrases.getPhrases().get("SMenu"));
        optionsNames = (String) phrases.getPhrases().get("Options");
        fileNames = phrases.obtainValues((String) phrases.getPhrases().get("File"));
        serviceNames = phrases.obtainValues((String) phrases.getPhrases().get("Services"));
        bibleName = phrases.obtainValues((String) phrases.getPhrases().get("Bible"));
        helpNames = phrases.obtainValues((String) phrases.getPhrases().get("Help"));

        errors = phrases.obtainValues((String) phrases.getPhrases().get("Errors"));
        mainNames = phrases.obtainValues((String) phrases.getPhrases().get("Main"));
        displayFont = (String) phrases.getPhrases().get("FontFaceM");
        displaySize = (String) phrases.getPhrases().get("FontSizeM");
        orderBox = (String) phrases.getPhrases().get("OrderBox");

        Font value1 = (Font) UIManager.get("Menu.font");
        if (displaySize == null || displaySize.equals("")) {
            displaySize = Integer.toString(value1.getSize());
        }
        if (displayFont == null || displayFont.equals("")) {
            displayFont = value1.getFontName();
        }
        displaySize = Integer.toString(Math.max(Integer.parseInt(displaySize), value1.getSize())); //If the default user's font size is larger than the required there is not need to change it.
        //The specified fonts sizes are the mininum required.
        currentFont = new Font(displayFont, Font.PLAIN, Integer.parseInt(displaySize));
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
                Font newFont = new Font(currentFont.getFontName(), keyF.getStyle(), currentFont.getSize());
                UIManager.put(key, newFont);
                //System.out.println(key);
            }
        }

        //System.out.println(this.getFont());
        setTitle((String) phrases.getPhrases().get("0"));
        rSep = (String) phrases.getPhrases().get("ReadSep");
        cSep = (String) phrases.getPhrases().get("CommSep");
        colon = (String) phrases.getPhrases().get("Colon");
        analyse.getDayInfo().put("FontFaceM", displayFont);
        analyse.getDayInfo().put("FontSizeM", displaySize);
        analyse.getDayInfo().put("ReadSep", rSep);
        analyse.getDayInfo().put("Colon", colon);
        ideographic = (String) phrases.getPhrases().get("Ideographic");
        analyse.getDayInfo().put("Ideographic", ideographic);
        gospelLocation = new GospelSelector(analyse.getDayInfo());

        //ADD A MENU BAR Y.S. 2008/08/11 n.s.
        demo = new MenuFiles(analyse.getDayInfo().clone());
        menuBarElement = new JMenuBar();
        menuBarElement.add(demo.createFileMenu(this));
        menuBarElement.add(demo.createOptionsMenu(this,this));
        menuBarElement.add(demo.createSaintsMenu(this));
        menuBarElement.add(demo.createServicesMenu(this));
        menuBarElement.add(demo.createBibleMenu(this));
        menuBarElement.add(demo.createHelpMenu(this));
        menuBarElement.setFont(currentFont);
        //MenuBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        setJMenuBar(menuBarElement);

        JPanel left = new JPanel(new GridLayout(3, 0));
        calendar = new JCalendar(analyse.getDayInfo());
        //System.out.println(calendar);
        calendar.addPropertyChangeListener(this);
        left.setLayout(new BorderLayout());
        left.add(calendar, BorderLayout.NORTH);
        displayIcon = new IconDisplay(new String[0], new String[0], analyse.getDayInfo());
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

        Locale place = new Locale(phrases.getPhrases().get("Language").toString(), phrases.getPhrases().get("Country").toString());
        Helpers orient = new Helpers(analyse.getDayInfo());
        analyse.getDayInfo().put("Locale", place);
        analyse.getDayInfo().put("Orient", ComponentOrientation.getOrientation(place));
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
        if (value1.getSize() < Integer.parseInt(displaySize)) {
            Dimension defaultScreen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

            //System.out.println(screen);
            int newSize = Integer.parseInt(displaySize);
            int maxW = 95 * defaultScreen.width / 100;
            int maxH = 95 * defaultScreen.height / 100;
            screen.width = java.lang.Math.min(screen.width * newSize / value1.getSize(), maxW);
            screen.height = java.lang.Math.min(screen.height * newSize / value1.getSize(), maxH);
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
        Helpers helper = new Helpers(analyse.getDayInfo());
        JMenuItem source = (JMenuItem) (e.getSource());
        String name = source.getText();

        if (name.equals(helpNames[2])) {
            //new About();
            Helpers orient = new Helpers(analyse.getDayInfo());
            orient.applyOrientation(new About(analyse.getDayInfo()), (ComponentOrientation) analyse.getDayInfo().get("Orient"));
        }
        if (name.equals(helpNames[0])) {
            //HELP FILES
        }
        if (name.equals(fileNames[1])) {
            //SAVE THE CURRENT WINDOW
            helper.saveHTMLFile(mainNames[5] + " " + today + ".html", "<html><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"><title>" + (String) phrases.getPhrases().get("0") + colon + today + "</title>" + output);
        }
        if (name.equals(fileNames[4])) {
            if (helper.closeFrame(mainNames[6])) {
                System.exit(0);
            }
        }
        if (name.equals(serviceNames[1])) {
            //DIVINE LITURGY
        }
        if (name.equals(serviceNames[2])) {
            //VESPERS
        }
        if (name.equals(serviceNames[3])) {
            //COMPLINE
        }
        if (name.equals(serviceNames[4])) {
            //MATINS
        }
        if (name.equals(serviceNames[5])) {
            //Create the primes service
            new Primes(today, analyse.getDayInfo());
        }
        if (name.equals(serviceNames[6])) {
            new ThirdHour(today, analyse.getDayInfo());
        }
        if (name.equals(serviceNames[7])) {
            new SixthHour(today, analyse.getDayInfo());
            //SEXT
        }
        if (name.equals(serviceNames[8])) {
            new NinthHour(today, analyse.getDayInfo());
            //NONE
        }
        if (name.equals(serviceNames[9])) {
            //ROYAL HOURS
            new RoyalHours(today, analyse.getDayInfo());
        }
        if (name.equals(serviceNames[10])) {
            //ALL-NIGHT VIGIL
        }
        if (name.equals(serviceNames[11])) {
            //MIDNIGHT OFFICE
        }
        if (name.equals(serviceNames[12])) {
            //TYPICA
        }
        if (name.equals(bibleName[0])) {
            //Launch the Bible Reader
            Helpers orient = new Helpers(analyse.getDayInfo());
            orient.applyOrientation(new Bible("Gen", "1:1-31", analyse.getDayInfo()), (ComponentOrientation) analyse.getDayInfo().get("Orient"));
        }
        if (name.equals(fileNames[6])) {
            helper.sendHTMLToPrinter(text);
        }
        if (name.equals( phrases.getPhrases().get("OptionMenu"))){
            Options optionsN=new Options(analyse.getDayInfo());
            optionsN.addPropertyChangeListener("CalendarChange",this); //nifty way of only listening to what I want to hear!
            optionsN.createDefaultWindow();

        }

        String s = "Action event detected." + Constants.NEWLINE + "    Event source: " + source.getText() + " (an instance of " + getClassName(source) + ")";
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
                    bible = new Bible(parts[1], parts[2], analyse.getDayInfo());
                    Helpers orient = new Helpers(analyse.getDayInfo());
                    orient.applyOrientation(bible, (ComponentOrientation) analyse.getDayInfo().get("Orient"));
                }
            } else {
                parts = cmd.split("\\?");
                if (parts[0].indexOf("goDoSaint") != -1) {

                    String[] parts2 = parts[1].split("=");
                    //System.out.println(parts2[1]);
                    String[] parts3 = parts2[1].split(",");

                    Commemoration trial1 = new Commemoration(parts3[parts3.length - 2], parts3[parts3.length - 1], analyse.getDayInfo());
                    if (saintLink == null) {
                        System.out.println(parts3[parts3.length - 1]);

                        saintLink = new DoSaint(trial1, analyse.getDayInfo());
                    } else {
                        saintLink.refresh(trial1);
                    }

                }
            }
        }
    }

    private void write() {
     
        output = "<body style=\"font-family:" + displayFont + ";font-size:" + displaySize + "pt\">";
     
        String AMC = (String) phrases.getPhrases().get("AMC");
        String AML = (String) phrases.getPhrases().get("AML");
        String format = getFormat(AMC);
        //System.out.println("AML = " + AML.equals("B"));
        if (AML.equals("B")) {
            output += "<B>" + format + today.toString(analyse.getDayInfo()) + "</B><BR>";
        } else {
            output += "<B>" + today.toString(analyse.getDayInfo()) + format + "</B><BR>";
        }

        output += mainNames[0] + colon + today.getGregorianDateS(analyse.getDayInfo()) + "<BR>";
        String filename = "";
        int lineNumber = 0;
        int dow = today.getDayOfWeek();
        int doy = today.getDoy();
        int nday = (int) JDate.difference(today, this.pascha);
        int ndayP = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
        //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
        int ndayF = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));

        //Clearing the holders for the icons and names
        iconImages = new Vector();
        iconNames = new Vector();

        // PUT THE RELEVANT DATA IN THE HASH
        analyse.getDayInfo().put("dow", dow);	// THE DAY'S DAY OF WEEK
        analyse.getDayInfo().put("doy", doy);	// THE DAY'S DOY (see JDate.java for specification)
        //System.out.println(doy);
        analyse.getDayInfo().put("nday", nday);	// THE NUMBER OF DAYS BEFORE (-) OR AFTER (+) THIS YEAR'S PASCHA
        analyse.getDayInfo().put("ndayP", ndayP);	// THE NUMBER OF DAYS AFTER LAST YEAR'S PASCHA
        //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
        analyse.getDayInfo().put("ndayF", ndayF);	// THE NUMBER OF DAYS TO NEXT YEAR'S PASCHA (CAN BE +ve or -ve).
        //ADDING THE TYPE OF GOSPEL READINGS TO BE FOLLOWED
        analyse.getDayInfo().put("GS", GospelSelector.getGValue());
        
        //INTERFACE LANGUAGE
        analyse.getDayInfo().put("LS", languageLocation.getLValue());
        analyse.getDayInfo().put("Year", today.getYear());
        analyse.getDayInfo().put("dRank", 0); //The default rank for a day is 0. Y.S. 2010/02/01 n.s.
        analyse.getDayInfo().put("Ideographic", ideographic);

        readings = new OrderedHashtable();
        fastInfo = new Stack();
        //MY ATTEMPT AT SORTING THE READINGS FOR THE LITURGY 2008/05/24 n.s. YURI SHARDT
		/*ReadScriptures = new OrderedHashtable[3];		//CONTAINS A SORTED ARRAY OF ALL THE READINGS
        ReadScriptures[0] = new OrderedHashtable();		//STORES THE PENTECOSTARION READINGS (SEQUENTIAL (rjadovoje) READINGS!)
        ReadScriptures[1] = new OrderedHashtable();		//CONTAINS THE MENALOGION READINGS, EXCLUDING ANY FLOATERS
        ReadScriptures[2] = new OrderedHashtable();		//CONTAINS THE FLOATER READINGS.
         */
        //TESTING THE LANGUAGE PACKS
        String rough = (String) phrases.getPhrases().get("1");
        String[] final1 = rough.split(",");
        //System.out.println(output);

        output += getAstronomicalData();

        if (nday >= -70 && nday < 0) {
            filename = Constants.TRIODION_PATH;
            lineNumber = Math.abs(nday);
        } else if (nday < -70) {
            // WE HAVE NOT YET REACHED THE LENTEN TRIODION
            filename = Constants.PENTECOSTARION_PATH;
            JDate lastPascha = Paschalion.getPascha(today.getYear() - 1);
            lineNumber = (int) JDate.difference(today, lastPascha) + 1;
        } else {
            // WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
            filename = Constants.PENTECOSTARION_PATH;
            lineNumber = nday + 1;
        }

        filename += lineNumber >= 10 ? lineNumber : "0" + lineNumber; // CLEANED UP
        //System.out.println("++++++++++++++++++++++\n"+filename+"\n+++++++++++++++++++\n");
        //System.out.println("File name in Main: " + Analyse.getDayInfo().get("LS").toString());
        Day paschalCycle = new Day(filename, analyse.getDayInfo());

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
        Day solarCycle = new Day(filename, analyse.getDayInfo());
        analyse.getDayInfo().put("dRank", Math.max(solarCycle.getDayRank(), paschalCycle.getDayRank()));
        output += paschalCycle.getCommsHyper() + cSep;
        output += solarCycle.getCommsHyper();
        analyse.getDayInfo().put("Tone", paschalCycle.getTone());


        String collection = "";
        output += "<BR><BR>";
        OrderedHashtable[] paschalReadings = paschalCycle.getReadings();
        //System.out.println("Length of Ordinary Readings="+PaschalReadings.length);

        OrderedHashtable[] menaionReadings = solarCycle.getReadings();
        Bible shortForm = new Bible(analyse.getDayInfo());
        //System.out.println("First Paschal Reading is :"+PaschalReadings[0].get(READINGS));
        //System.out.println("First Menologion Reading is :"+MenaionReadings[0].get(READINGS));
        OrderedHashtable combinedReadings = new OrderedHashtable();
        //for(int j=0;j<7;j++){
        ReadingUtility.processMenaionPaschalReadings(menaionReadings, combinedReadings);
        ReadingUtility.processMenaionPaschalReadings(paschalReadings, combinedReadings);
        //}
        boolean firstTime = true;
        for (Enumeration e = combinedReadings.enumerateKeys(); e.hasMoreElements();) {
            //Temporary solution
            String element1 = e.nextElement().toString();
            OrderedHashtable temp = (OrderedHashtable) combinedReadings.get(element1);
            Vector readings = (Vector) temp.get(READINGS_KEY);
            Vector rank = (Vector) temp.get("Rank");
            Vector tag = (Vector) temp.get("Tag");
            if (element1.equals("LITURGY")) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    output += rSep;
                }
                //Special case and consider it differently
                output += iterateEpistleGospel(readings, rank, tag);

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
                    output += rSep;
                }

                output+=putMatinsReadings(readings, rank, tag, new OrderedHashtable(), "matins");
                //output+=RSep;


                continue;

            }
            if (firstTime) {
                firstTime = false;
            } else {
                output += rSep;
            }
            String type1 = (String) phrases.getPhrases().get(element1.toLowerCase());
            output += "<B>" + type1 + "</B>" + colon;
            output += iterateOverReadings(shortForm, readings, tag);
            //output += RSep;
        }



        displayIcons(paschalCycle, solarCycle);

        //THIS IS NOW REPLACED BY THE NEW PROGRAMME, THAT SIMPLIFIES THE DETERMINATION OF THE FAST.
        String[] fastNames = phrases.obtainValues((String) phrases.getPhrases().get("Fasts"));
        Fasting getfast = new Fasting(analyse.getDayInfo());
        output += "<BR><BR>" + getfast.fastRules() + "<BR><BR>";
        //output+="</FONT>";
        output += "</body>";

        text.setContentType("text/html; charset=UTF-8");
        text.setFont(currentFont);
        text.setText(output);
        text.setCaretPosition(0);



    }

	private String iterateEpistleGospel(Vector readings, Vector rank, Vector tag) {
		
		String epistleGospelOutput = "";
		Vector epistle = new Vector();

		Vector gospel = new Vector();

		for (int j = 0; j < readings.size(); j++) {
		    OrderedHashtable liturgy = (OrderedHashtable) readings.get(j);
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
			epistleGospelOutput += putEpistleGospelReadings(rank, tag, epistle, readingsA, "apostol");
		    epistleGospelOutput += rSep;
		}
		if (!gospel.get(0).equals("")) {
			epistleGospelOutput += putEpistleGospelReadings(rank, tag, gospel, readingsA, "gospel");
		}
		return epistleGospelOutput;
	}

	private String iterateOverReadings(Bible shortForm, Vector readings, Vector tag) {
		String readingOutput = "";
		for (int i = 0; i < readings.size(); i++) {
		    OrderedHashtable reading = (OrderedHashtable) readings.get(i);
		    String name = "";
		    if (i != 0) {
		        readingOutput += rSep;
		    }
		    //System.out.println(Reading);
		    //System.out.println(Tag.get(i));
		    boolean first = true;

		    for (Enumeration e2 = reading.enumerateKeys(); e2.hasMoreElements();) {
		        if (first) {
		            first = false;
		        } else {
		            readingOutput += rSep;
		        }
		        String element2 = e2.nextElement().toString();
		        OrderedHashtable stuff = (OrderedHashtable) reading.get(element2);
		        String bibleText = stuff.get("Reading").toString();
		        readingOutput += shortForm.getHyperlink(bibleText);
		    }
		    if (readings.size() > 1) {
		        readingOutput += tag.get(i).toString();

		    }

		}
		return readingOutput;
	}

	private void displayIcons(Day paschalCycle, Day solarCycle) {
		OrderedHashtable iconsP = (OrderedHashtable) paschalCycle.getIcon();
        OrderedHashtable iconsM = (OrderedHashtable) solarCycle.getIcon();
        //String[] ss = (String[])v.toArray(new String[v.size()]);
        Vector imageList = (Vector) iconsM.get("Images");
        Vector namesList = (Vector) iconsM.get("Names");
        String[] iconImages = new String[imageList.size()];
        String[] iconNames = new String[namesList.size()];

        iconImages = (String[]) imageList.toArray(new String[imageList.size()]);
        iconNames = (String[]) namesList.toArray(new String[namesList.size()]);

        displayIcon.updateImagesFiled(iconImages, iconNames);
	}

	private String getAstronomicalData() {
        Sunrise sunrise = new Sunrise(analyse.getDayInfo());
        String[] sunriseSunset = sunrise.getSunriseSunsetString(today, (String) ConfigurationFiles.getDefaults().get("Longitude"), (String) ConfigurationFiles.getDefaults().get("Latitude"), (String) ConfigurationFiles.getDefaults().get("TimeZone"));
        String astronomicalData = "<BR>" + mainNames[1] + sunriseSunset[0];
        astronomicalData += "<BR>" + mainNames[2] + sunriseSunset[1];
        astronomicalData += "<BR><BR>"; //<B>"+MainNames[3]+"</B>"+Colon+ Paschalion.getLunarPhaseString(today) +"<BR><BR>";
        // getting rid of the lunar phase until we program a paschalion ...
        //adding the civil Lunar phase by request of Mitrophan
        Astronomy sky = new Astronomy();

        astronomicalData += mainNames[3] + sky.lunarphase(today.getJulianDay(), analyse.getDayInfo());
        astronomicalData += "<BR><BR>";
        return astronomicalData;
	}



	private String putMatinsReadings(Vector readings, Vector rank, Vector tag, OrderedHashtable readingsA, String key) {
		readingsA.put(READINGS_KEY, processMatins(readings));
		readingsA.put("Rank", rank);
		readingsA.put("Tag", tag);
		Matins trial1 = new Matins(analyse.getDayInfo());
		String type1 = (String) phrases.getPhrases().get(key);
		return "<B>" + type1 + "</B>" + colon + trial1.Readings(readingsA, today);
	}

	private String putEpistleGospelReadings(Vector rank, Vector tag, Vector reading, OrderedHashtable readingsA, String key) {
		readingsA.put(READINGS_KEY, reading);
		readingsA.put("Rank", rank);
		readingsA.put("Tag", tag);
		DivineLiturgy trial1 = new DivineLiturgy(analyse.getDayInfo());
		String type1 = (String) phrases.getPhrases().get(key);
		return "<B>" + type1 + "</B>" + colon + trial1.Readings(readingsA, key, today);
	}

	private Vector<String> processMatins(Vector<OrderedHashtable> readings) {
		Vector<String> matins2 = new Vector<>();

		for (int j = 0; j < readings.size(); j++) {
		    OrderedHashtable matins = readings.get(j);
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
		return matins2;
	}

	private String getFormat(String amc) {
		String format = "";
        if (amc.equals("1")) {
            PCalendar checking = new PCalendar(today, PCalendar.JULIAN, analyse.getDayInfo());
            format = (String) phrases.getPhrases().get("AM");
            if (analyse.getDayInfo().get("Ideographic").equals("1"))
                {
                    RuleBasedNumber convertN=new RuleBasedNumber(analyse.getDayInfo());
                    
                    format = format.replace("^YYAM", convertN.getFormattedNumber(Long.parseLong(Integer.toString((int) checking.getAM()))));

                }
                else
                {
		format = format.replace("^YYAM", Integer.toString((int) checking.getAM()));
                }
        }
		return format;
	}

    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf('.');
        return classString.substring(dotIndex + 1);
    }

    public static void main(String[] argz) {
        new Main();
    }
}
