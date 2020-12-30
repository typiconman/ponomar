package net.ponomar;

import javax.swing.*;
import javax.swing.event.*;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import net.ponomar.astronomy.Paschalion;
import net.ponomar.calendar.JCalendar;
import net.ponomar.calendar.JDate;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.internationalization.LanguageSelector;
import net.ponomar.panels.GospelSelector;
import net.ponomar.panels.IconDisplay;
import net.ponomar.panels.PrintableTextPane;
import net.ponomar.parsing.Commemoration;
import net.ponomar.parsing.Day;
import net.ponomar.parsing.Fasting;
import net.ponomar.readings.utility.ReadingUtility;
import net.ponomar.services.NinthHour;
import net.ponomar.services.Primes;
import net.ponomar.services.RoyalHours;
import net.ponomar.services.SixthHour;
import net.ponomar.services.ThirdHour;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
import net.ponomar.utility.MenologionContent;
 
 
import net.ponomar.utility.StringOp;

/* * Copyright 2006, 2007, 2008, 2009, 2010, 2012 Aleksandr Andreev and Yuri
 * Shardt.
 * 
 * Ponomar is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * While Ponomar is distributed in the hope that it will be useful, it comes
 * with ABSOLUTELY NO WARRANTY, without even the implied warranties of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for details.
 */

/**
 * 
 * Main module for the Ponomar program. This module constitutes the primary
 * Ponomar gui and centre of the program. To start the program, invoke
 * main(string[]) of this class. Outputs relevant information for each day, with
 * links to detailed info.
 * <p>
 * 
 * @author Aleksandr Andreev (aleksandr.andreev@gmail.com) and Yuri Shardt
 * 
 */
public class Main extends JFrame implements PropertyChangeListener, HyperlinkListener, ActionListener {
	// Elements of the interface
	JDate today; 		// "TODAY" (I.E. THE DATE WE'RE WORKING WITH
	private JCalendar calendar; 	// THE CALENDAR OBJECT
	private PrintableTextPane text; 	// MAIN TEXT AREA FOR OUTPUT
	private JDate pascha; 		// THIS YEAR'S PASCHA
	private JDate pentecost; 	// THIS YEAR'S PENTECOST
	//private Stack fastInfo;		// CONTAINS A VECTOR OF THE FASTING INFORMATION FOR TODAY, WHICH IS LATER PASSED TO CONVOLVE()
	private String output;  	// TODAY'S CALENDAR OUTPUT
	private boolean inited = false; // PREVENTS MULTIPLE READING OF XML FILES ON LAUNCH
	private Bible bible;
	private LanguageSelector languageLocation;
	//private String LLocation;
	//MY ATTEMPT AT SORTING THE READINGS FOR THE LITURGY 2008/05/19 n.s. YURI SHARDT
	private JMenuBar menuBarElement;
	private MenuFiles menuFiles;
	private LanguagePack phrases;
	private String[] mainNames;
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
	private StringOp analyse = new StringOp();
	//private GospelSelector Selector;
	//Helpers findLanguage;

	// CONSTRUCTOR
	public Main() {
		loadPhrases();
		Font fontValue = configureFonts();
		configureWindow();
		adjustResolutionEastAsianLanguages(fontValue);
		today = new JDate(calendar.getMonth(), calendar.getDay(), calendar.getYear());
		pascha = Paschalion.getPascha(today.getYear());
		pentecost = Paschalion.getPentecost(today.getYear());
		write();
	}

	protected void configureWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setTitle(phrases.getPhrases().get("0"));
		rSep = phrases.getPhrases().get("ReadSep");
		cSep = phrases.getPhrases().get("CommSep");
		colon = phrases.getPhrases().get(Constants.COLON);
		analyse.getDayInfo().put(Constants.FONT_FACE_M, displayFont);
		analyse.getDayInfo().put(Constants.FONT_SIZE_M, displaySize);
		analyse.getDayInfo().put("ReadSep", rSep);
		analyse.getDayInfo().put(Constants.COLON, colon);
		ideographic = phrases.getPhrases().get(Constants.IDEOGRAPHIC);
		analyse.getDayInfo().put(Constants.IDEOGRAPHIC, ideographic);
		//gospelLocation = new GospelSelector(analyse.getDayInfo());

		addMenuBar();

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

		setContentPane(splitter);

		Locale place = new Locale(phrases.getPhrases().get("Language"), phrases.getPhrases().get("Country"));
		Helpers orient = new Helpers(analyse.getDayInfo());
		analyse.getDayInfo().put("Locale", place);
		analyse.getDayInfo().put(Constants.ORIENT, ComponentOrientation.getOrientation(place));
		orient.applyOrientation(this, ComponentOrientation.getOrientation(place));
		this.validate();

		pack();
		setSize(700, 500);
		setVisible(true);

		inited = true;
	}

	/**
	 * Add a menu bar.
	 * <p>
	 * Y.S. 2008/08/11 n.s.
	 */
	protected void addMenuBar() {
		menuFiles = new MenuFiles((LinkedHashMap<String, Object>) analyse.getDayInfo().clone());
		menuBarElement = new JMenuBar();
		menuBarElement.add(menuFiles.createFileMenu(this));
		menuBarElement.add(menuFiles.createOptionsMenu(this, this));
		menuBarElement.add(menuFiles.createSaintsMenu(this));
		menuBarElement.add(menuFiles.createServicesMenu(this));
		menuBarElement.add(menuFiles.createBibleMenu(this));
		menuBarElement.add(menuFiles.createHelpMenu(this));
		menuBarElement.setFont(currentFont);
		setJMenuBar(menuBarElement);
	}

	protected void adjustResolutionEastAsianLanguages(Font fontValue) {
		Dimension screen = this.getSize();
		// Default screen size issues for East Asian languages!
		if (fontValue.getSize() < Integer.parseInt(displaySize)) {
			Dimension defaultScreen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			int newSize = Integer.parseInt(displaySize);
			int maxW = 95 * defaultScreen.width / 100;
			int maxH = 95 * defaultScreen.height / 100;
			screen.width = java.lang.Math.min(screen.width * newSize / fontValue.getSize(), maxW);
			screen.height = java.lang.Math.min(screen.height * newSize / fontValue.getSize(), maxH);
			this.setSize(screen);
		}
	}

	protected Font configureFonts() {
		//Load font settings
		displayFont = phrases.getPhrases().get(Constants.FONT_FACE_M);
		displaySize = phrases.getPhrases().get(Constants.FONT_SIZE_M);

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
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				Font keyF = (Font) value;
				String[] splitkey = key.toString().replace(".", ":").split(":");
				//This prevents the font from being changed for those things that are to remain in the Latin alphabet!
				if (checkForAcceleratorFontSplitKey(splitkey) || checkForAcceleratorFontKey(key)) {
						continue;
				}
				Font newFont = new Font(currentFont.getFontName(), keyF.getStyle(), currentFont.getSize());
				UIManager.put(key, newFont);
			}
		}
		return value1;
	}

	protected boolean checkForAcceleratorFontKey(Object key) {
		return key.toString().equals("MenuItem.acceleratorFont");
	}

	protected boolean checkForAcceleratorFontSplitKey(String[] splitkey) {
		return splitkey.length > 1 && splitkey[splitkey.length - 1].equals("acceleratorFont");
	}

	protected void loadPhrases() {

		//WE NEED THIS HANDY STORER OF VALUES NOW.
		//StringOp.dayInfo = new OrderedHashtable();
		//DETERMINE THE DEFAULTS
		ConfigurationFiles.setDefaults(new LinkedHashMap<>());
		ConfigurationFiles.readFile();
		languageLocation = new LanguageSelector(analyse.getDayInfo());

		analyse.getDayInfo().put("LS", languageLocation.getLValue());
		phrases = new LanguagePack(analyse.getDayInfo());
		//Changing language storage format
		//findLanguage = new Helpers(analyse.getDayInfo());

		fileNames = phrases.obtainValues(phrases.getPhrases().get("File"));
		serviceNames = phrases.obtainValues(phrases.getPhrases().get("Services"));
		bibleName = phrases.obtainValues(phrases.getPhrases().get("Bible"));
		helpNames = phrases.obtainValues(phrases.getPhrases().get("Help"));
		mainNames = phrases.obtainValues(phrases.getPhrases().get("Main"));
	}

	public void propertyChange(PropertyChangeEvent e) {

		if (inited) {
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
			// new About();
			Helpers orient = new Helpers(analyse.getDayInfo());
			orient.applyOrientation(new About(analyse.getDayInfo()),
					(ComponentOrientation) analyse.getDayInfo().get(Constants.ORIENT));
		}
		if (name.equals(helpNames[0])) {
			// HELP FILES
		}
		if (name.equals(fileNames[1])) {
			// SAVE THE CURRENT WINDOW
			helper.saveHTMLFile(mainNames[5] + today + ".html",
					"<html><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"><title>"
							+ phrases.getPhrases().get("0") + colon + today + "</title>" + output);
		}
		if (name.equals(fileNames[4])) {
			if (helper.closeFrame(mainNames[6])) {
				System.exit(0);
			}
		}
		if (name.equals(serviceNames[1])) {
			// DIVINE LITURGY
		}
		if (name.equals(serviceNames[2])) {
			// VESPERS
		}
		if (name.equals(serviceNames[3])) {
			// COMPLINE
		}
		if (name.equals(serviceNames[4])) {
			// MATINS
		}
		if (name.equals(serviceNames[5])) {
			// Create the primes service
			new Primes(today, analyse.getDayInfo());
		}
		if (name.equals(serviceNames[6])) {
			new ThirdHour(today, analyse.getDayInfo());
		}
		if (name.equals(serviceNames[7])) {
			new SixthHour(today, analyse.getDayInfo());
			// SEXT
		}
		if (name.equals(serviceNames[8])) {
			new NinthHour(today, analyse.getDayInfo());
			// NONE
		}
		if (name.equals(serviceNames[9])) {
			// ROYAL HOURS
			new RoyalHours(today, analyse.getDayInfo());
		}
		if (name.equals(serviceNames[10])) {
			// ALL-NIGHT VIGIL
		}
		if (name.equals(serviceNames[11])) {
			// MIDNIGHT OFFICE
		}
		if (name.equals(serviceNames[12])) {
			// TYPICA
		}
		if (name.equals(bibleName[0])) {
			// Launch the Bible Reader
			Helpers orient = new Helpers(analyse.getDayInfo());
			orient.applyOrientation(new Bible("Gen", "1:1-31", analyse.getDayInfo()),
					(ComponentOrientation) analyse.getDayInfo().get(Constants.ORIENT));
		}
		if (name.equals(fileNames[6])) {
			helper.sendHTMLToPrinter(text);
		}
		if (name.equals(phrases.getPhrases().get("OptionMenu"))) {
			Options optionsN = new Options(analyse.getDayInfo());
			optionsN.addPropertyChangeListener("CalendarChange", this); // nifty way of only listening to what I want to hear!
			optionsN.createDefaultWindow();

		}
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			activateHyperlink(e);
		}
	}

	protected void activateHyperlink(HyperlinkEvent e) {
		String description = e.getDescription();
		if (description.contains("reading")) {
			String[] bibleParts = description.split("#");
			activateBibleHyperlink(bibleParts[1], bibleParts[2]);
		} else if (description.contains("goDoSaint")) {
			String queryString = description.split("\\?")[1];
			String value = queryString.split("=")[1];
			String[] ids = value.split(",");
			activateSaintHyperlink(ids[0], ids[1]);
		}
	}

	protected void activateSaintHyperlink(String sId, String cId) {
		Commemoration commemoration = new Commemoration(sId, cId, analyse.getDayInfo());
		if (saintLink == null) {
			saintLink = new DoSaint(commemoration, analyse.getDayInfo());
		} else {
			saintLink.refresh(commemoration);
		}
	}

	protected void activateBibleHyperlink(String book, String chapterAndVerse) {
		try {
			bible.update(book, chapterAndVerse);
			bible.setVisible(true);
		} catch (NullPointerException npe) {
			bible = new Bible(book, chapterAndVerse, analyse.getDayInfo());
			Helpers orient = new Helpers(analyse.getDayInfo());
			orient.applyOrientation(bible, (ComponentOrientation) analyse.getDayInfo().get(Constants.ORIENT));
		}
	}

	private void write() {
		text.setContentType(Constants.CONTENT_TYPE);
		text.setFont(currentFont);
		output = generateContent();
		text.setText(output);
		text.setCaretPosition(0);
	}

	protected String generateContent() {
		String content = "<body style=\"font-family:" + displayFont + ";font-size:" + displaySize + "pt\">";

		String amc = phrases.getPhrases().get("AMC");
		String aml = phrases.getPhrases().get("AML");
		String format = MenologionContent.getFormat(amc, analyse, today, phrases.getPhrases().get("AM"));
		//System.out.println("AML = " + AML.equals("B"));
		if (aml.equals("B")) {
			content += "<B>" + format + today.toString(analyse.getDayInfo()) + "</B><BR>";
		} else {
			content += "<B>" + today.toString(analyse.getDayInfo()) + format + "</B><BR>";
		}

		content += mainNames[0] + colon + today.getGregorianDateS(analyse.getDayInfo()) + "<BR>";
		String filename = "";
		int lineNumber;
		int nday = updateDayInfoAndReturnPaschaDifference();

		//readings = new OrderedHashtable();
		//fastInfo = new Stack();
		//MY ATTEMPT AT SORTING THE READINGS FOR THE LITURGY 2008/05/24 n.s. YURI SHARDT
		/*ReadScriptures = new OrderedHashtable[3];		//CONTAINS A SORTED ARRAY OF ALL THE READINGS
        ReadScriptures[0] = new OrderedHashtable();		//STORES THE PENTECOSTARION READINGS (SEQUENTIAL (rjadovoje) READINGS!)
        ReadScriptures[1] = new OrderedHashtable();		//CONTAINS THE MENALOGION READINGS, EXCLUDING ANY FLOATERS
        ReadScriptures[2] = new OrderedHashtable();		//CONTAINS THE FLOATER READINGS.
		 */

		content += MenologionContent.getAstronomicalData(analyse, today, mainNames[1], mainNames[2], mainNames[3]);

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
        ArrayList vect = (ArrayList)readings.get(type);

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
		analyse.getDayInfo().put(Constants.D_RANK, Math.max(solarCycle.getDayRank(), paschalCycle.getDayRank()));
		content += paschalCycle.getCommsHyper() + cSep;
		content += solarCycle.getCommsHyper();
		analyse.getDayInfo().put("Tone", paschalCycle.getTone());


		content += Constants.DOUBLE_LINEBREAK;
		LinkedHashMap[] paschalReadings = paschalCycle.getReadings();
		//System.out.println("Length of Ordinary Readings="+PaschalReadings.length);

		LinkedHashMap[] menaionReadings = solarCycle.getReadings();
		Bible shortForm = new Bible(analyse.getDayInfo());
		//System.out.println("First Paschal Reading is :"+PaschalReadings[0].get(READINGS));
		//System.out.println("First Menologion Reading is :"+MenaionReadings[0].get(READINGS));
		LinkedHashMap combinedReadings = new LinkedHashMap();
		//for(int j=0;j<7;j++){
		ReadingUtility.processMenaionPaschalReadings(menaionReadings, combinedReadings);
		ReadingUtility.processMenaionPaschalReadings(paschalReadings, combinedReadings);
		//}

		MenologionContent contentHelper = new MenologionContent(analyse, phrases, today);
		contentHelper.setrSep(rSep);
		contentHelper.setColon(colon);

		content += contentHelper.processReadings(shortForm, combinedReadings);

		//THIS IS NOW REPLACED BY THE NEW PROGRAMME, THAT SIMPLIFIES THE DETERMINATION OF THE FAST.
		//String[] fastNames = phrases.obtainValues((String) phrases.getPhrases().get("Fasts"));
		Fasting getfast = new Fasting(analyse.getDayInfo());
		content += Constants.DOUBLE_LINEBREAK + getfast.fastRules() + Constants.DOUBLE_LINEBREAK;
		//output+="</FONT>";

		//TODO: Find a way to separate this, because it is GUI
		displayIcons(paschalCycle, solarCycle);
		content += "</body>";
		return content;
	}

	protected int updateDayInfoAndReturnPaschaDifference() {
		int dayOfWeek = today.getDayOfWeek();
		int doy = today.getDoy();
		int daysBeforeOrAfterPascha = (int) JDate.difference(today, this.pascha);
		int daysAfterLastYearsPascha = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
		//REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
		int daysBeforeNextYearsPascha = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));

		// PUT THE RELEVANT DATA IN THE HASH
		analyse.getDayInfo().put("dow", dayOfWeek);	// THE DAY'S DAY OF WEEK
		analyse.getDayInfo().put("doy", doy);	// THE DAY'S DOY (see JDate.java for specification)
		//System.out.println(doy);
		analyse.getDayInfo().put("nday", daysBeforeOrAfterPascha);	// THE NUMBER OF DAYS BEFORE (-) OR AFTER (+) THIS YEAR'S PASCHA
		analyse.getDayInfo().put(Constants.NDAY_P, daysAfterLastYearsPascha);	// THE NUMBER OF DAYS AFTER LAST YEAR'S PASCHA
		//REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
		analyse.getDayInfo().put(Constants.NDAY_F, daysBeforeNextYearsPascha);	// THE NUMBER OF DAYS TO NEXT YEAR'S PASCHA (CAN BE +ve or -ve).
		//ADDING THE TYPE OF GOSPEL READINGS TO BE FOLLOWED
		analyse.getDayInfo().put("GS", GospelSelector.getGValue());

		//INTERFACE LANGUAGE
		analyse.getDayInfo().put("LS", languageLocation.getLValue());
		analyse.getDayInfo().put("Year", today.getYear());
		analyse.getDayInfo().put(Constants.D_RANK, 0); //The default rank for a day is 0. Y.S. 2010/02/01 n.s.
		analyse.getDayInfo().put(Constants.IDEOGRAPHIC, ideographic);
		return daysBeforeOrAfterPascha;
	}

	private void displayIcons(Day paschalCycle, Day solarCycle) {
		//OrderedHashtable iconsP = (OrderedHashtable) paschalCycle.getIcon();
		LinkedHashMap<String, ArrayList<String>> iconsM = solarCycle.getIcon();

		ArrayList<String> imageList = iconsM.get("Images");
		ArrayList<String> namesList = iconsM.get("Names");

		String[] iconImages = imageList.toArray(new String[0]);
		String[] iconNames = namesList.toArray(new String[0]);

		displayIcon.updateImagesFiled(iconImages, iconNames);
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
