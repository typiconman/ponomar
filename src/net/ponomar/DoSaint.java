package net.ponomar;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.swing.event.*;

import java.awt.event.*;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.panels.IconDisplay;
import net.ponomar.panels.PrintableTextPane;
import net.ponomar.parsing.Commemoration;
import net.ponomar.parsing.utility.DocHandler;
import net.ponomar.parsing.utility.QDParser;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 
 
import net.ponomar.utility.StringOp;

/*
(C) 2007, 2008, 2012 YURI SHARDT. ALL RIGHTS RESERVED.
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
*/

/**
 * 
 * This module creates the window to display the saint information.
 * 
 * @author Yuri Shardt
 * 
 */
public class DoSaint implements DocHandler, ActionListener, ItemListener, PropertyChangeListener, HyperlinkListener {
    //SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
    //THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
    //TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
    //DURING THE COURSE OF A SINGLE WEEK.

    private static final String KONTAKION = "KONTAKION";
	private static final String TROPARION = "TROPARION";
	private static final String PARAGRAPH_TAG_CLOSE = "</p>";
	private static final String H2_TAG_CLOSE = "</h2>";
	private static final String H2_TAG_CENTERED = "<h2 style=\"text-align: center;\">";
	private static final String PARAGRAPH_TAG_CENTERED = "<p style=\"text-align: center;\">";
	private static final String DOUBLE_NEWLINE = "\n \n";
	private static final String PODOBEN = "Podoben";
	private static final String PODOBNI = Constants.COMMANDS + "Podobni.xml";
	private String life = "";
    private String tropar = "";
    private String troparT = ""; //Tone
    private String troparP = "";  //Podoben melody
    private String kondak = "";
    private String kondakT = "";  //Tone
    private String kondakP = "";  //Podoben melody
    private String textR = "";
    //There may be a second tropar and kontak
    private String tropar2 = "";
    private String troparT2 = ""; //Tone
    private String troparP2 = "";  //Podoben melody
    private String kondak2 = "";
    private String kondakT2 = "";  //Tone
    private String kondakP2 = "";  //Podoben melody
    
    private String repose=""; //date of repose (not yet standardised)

    private String name = "";
    private String copyright = ""; //Any additional information about the life.
    private LanguagePack text; //= new LanguagePack();
    private LinkedHashMap<String, String> podobniMap;
    
    
    //private static String fileNameIn = Constants.SERVICES_PATH + "PRIMES1/";
    private static boolean read = false;
    private static String type;
    
    
    private String[] languageNames;// = Text.obtainValues((String) Text.Phrases.get("LanguageMenu"));
    
    private JFrame frames;
    private String[] fileNames;// = Text.obtainValues((String) Text.Phrases.get("File"));
    private String[] helpNames;// = Text.obtainValues((String) Text.Phrases.get("Help"));
    static final String NEWLINE = "\n";
    private String strOut;
    
    private Helpers helper;// = new Helpers();
    //private PrimeSelector SelectorP=new PrimeSelector();
    private PrintableTextPane output;
    //private JEditorPane output;
    
    private Commemoration saintInfo2;
    private Bible bible;
    private String name2;
    private StringOp analyse=new StringOp();

	public DoSaint(Commemoration saintInfo, LinkedHashMap<String, Object> dayInfo) {
		// Get the Podobni
		analyse.setDayInfo(dayInfo);
		text = new LanguagePack(dayInfo);
		// PrimesNames = Text.obtainValues((String) Text.Phrases.get("Primes"));
		languageNames = text.obtainValues(text.getPhrases().get(Constants.LANGUAGE_MENU));

		fileNames = text.obtainValues(text.getPhrases().get("File"));
		helpNames = text.obtainValues(text.getPhrases().get("Help"));
		helper = new Helpers(analyse.getDayInfo());

		podobniMap = new LinkedHashMap<>();
		try {
			BufferedReader frf = new BufferedReader(new InputStreamReader(
					new FileInputStream(helper.langFileFind(analyse.getDayInfo().get("LS").toString(), PODOBNI)),
					StandardCharsets.UTF_8));
			QDParser.parse(this, frf);
		} catch (Exception primes) {
			primes.printStackTrace();
		}
		refresh(saintInfo);
	}

	public void refresh(Commemoration saintInfo) {

		saintInfo2 = saintInfo;
		createWindow();

	}

	private void createWindow()// (String textOut)
	{
		// Order the desired Text
		name = saintInfo2.getName();
		name2 = saintInfo2.getGrammar("Short");
		life = saintInfo2.getLife();
		copyright = saintInfo2.getLifeCopyright();

		fillHymns();

		text = new LanguagePack(analyse.getDayInfo());
		String[] toneNumbers = text.obtainValues(text.getPhrases().get("Tones"));
		String[] mainNames = text.obtainValues(text.getPhrases().get("Main"));
		String[] saintInfo = text.obtainValues(text.getPhrases().get("LivesW"));
		String textOut = generateContent(toneNumbers, saintInfo);
		// Other information can go here!
		// String textOut=header+image+rest;

		frames = new JFrame(text.getPhrases().get("0") + text.getPhrases().get(Constants.COLON) + name2);

		// frames.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel left = new JPanel();
		JPanel right = new JPanel();
		left.setLayout(new BorderLayout());
		right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));

		textOut = textOut.replace("</br>", "<BR>");
		textOut = textOut.replace("<br>", "<BR>");
		strOut = textOut;
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setOpaque(true);
		setPrintableTextPane(textOut, contentPane);

		JScrollPane scrollPane = new JScrollPane(output);
		setMenuBar();

		contentPane.add(scrollPane, BorderLayout.CENTER);
		right.add(contentPane);

		JSplitPane splitter = generateSplitter(left, right);

		frames.add(splitter);

		// frames.setContentPane(contentPane);

		LinkedHashMap<String, ArrayList<String>> iconsM = saintInfo2.getDisplayIcons();
		ArrayList<String> imageList = iconsM.get("Images");
		ArrayList<String> namesList = iconsM.get("Names");
		String[] iconImages = new String[imageList.size()];
		String[] iconNames = new String[namesList.size()];

		iconImages = imageList.toArray(new String[0]);
		iconNames = namesList.toArray(new String[0]);
		System.out.println("Icon Length is: " + iconNames.length);
		IconDisplay icons = new IconDisplay(iconImages, iconNames, analyse.getDayInfo());
		left.add(new JPanel(), BorderLayout.NORTH);
		left.add(icons, BorderLayout.CENTER);
		left.add(new JPanel(), BorderLayout.SOUTH);
		// contentPane.add(icons);
		// textOut+=icons;
		// output.setText(textOut);

		Helpers orient = new Helpers(analyse.getDayInfo());

		orient.applyOrientation(frames, (ComponentOrientation) analyse.getDayInfo().get(Constants.ORIENT));
		frames.pack();
		frames.setSize(800, 700);
		frames.setVisible(true);
		// scrollPane.top();
	}

	private JSplitPane generateSplitter(JPanel left, JPanel right) {
		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitter.setLeftComponent(left);
		splitter.setRightComponent(right);
		return splitter;
	}

	private void setMenuBar() {
		JMenuBar menuBarElement = new JMenuBar();
		MenuFiles demo = new MenuFiles(analyse.getDayInfo());
		// PrimeSelector trial=new PrimeSelector();
		menuBarElement.add(demo.createFileMenu(this));
		// MenuBar.add(trial.createPrimeMenu());
		menuBarElement.add(demo.createHelpMenu(this));
		frames.setJMenuBar(menuBarElement);
		// trial.addPropertyChangeListener(this);
	}

	private void setPrintableTextPane(String textOut, JPanel contentPane) {
		output = new PrintableTextPane();
		output.addHyperlinkListener(this);
		// output=new JEditorPane();
		output.setEditable(false);
		output.setSize(800, 700);
		output.setContentType(Constants.CONTENT_TYPE);
		// output.setText(header);
		output.setText(textOut);
		output.setCaretPosition(0);
		contentPane.add(output);
	}

	private void fillHymns() {
		String[] troparTriple = fillHymn(TROPARION, "1");
		tropar = troparTriple[0];
		if (tropar != null) {
			troparT = troparTriple[1];
			troparP = troparTriple[2];
		}

		String[] tropar2Triple = fillHymn(TROPARION, "2");
		tropar2 = tropar2Triple[0];
		if (tropar2 != null) {
			troparT2 = tropar2Triple[1];
			troparP2 = tropar2Triple[2];
		}

		String[] kondakTriple = fillHymn(KONTAKION, "1");
		kondak = kondakTriple[0];
		if (kondak != null) {
			kondakT = kondakTriple[1];
			kondakP = kondakTriple[2];
		}

		String[] kondak2Triple = fillHymn(KONTAKION, "2");
		kondak2 = kondak2Triple[0];
		if (kondak2 != null) {
			kondakT2 = kondak2Triple[1];
			kondakP2 = kondak2Triple[2];
		}
	}

	protected String[] fillHymn(String hymnConstant, String hymnNumber) {
		LinkedHashMap<String, String> kontakionInfo = saintInfo2.getService("/LITURGY/" + hymnConstant, hymnNumber);
		String[] hymnInfo = new String[3];
		if (kontakionInfo != null) {
			hymnInfo[0] = kontakionInfo.get("text");
			hymnInfo[1] = kontakionInfo.get("Tone");
			String podobenMelody = "";
			if (kontakionInfo.get(PODOBEN) != null) {
				podobenMelody = kontakionInfo.get(PODOBEN);
			} 
			hymnInfo[2] = podobenMelody;
			return hymnInfo;
		} else {
			hymnInfo[0] = null;
			return hymnInfo;
		}
	}

	private String generateContent(String[] toneNumbers, String[] saintInfo) {
		String textOut = "";

		if (name.equals("")) {
			textOut = saintInfo[6];
		} else {
			Font value1 = (Font) UIManager.get("Menu.font");

			String displaySizeM = getDisplaySize(value1, Constants.FONT_SIZE_M);
			String displayFontM = getDisplayFont(value1, Constants.FONT_FACE_M);
			displaySizeM = Integer.toString(Math.max(Integer.parseInt(displaySizeM), value1.getSize()));

			textOut = "<body style=\"font-family:" + displayFontM + ";font-size:" + displaySizeM
					+ ";\"><h1 style=\"text-align: center;\">" + name + "</h1>";
			if (life != null && !life.equals("")) {
				textOut += H2_TAG_CENTERED + saintInfo[0] + H2_TAG_CLOSE
						+ PARAGRAPH_TAG_CENTERED + "<small>" + copyright + "</small>"+PARAGRAPH_TAG_CLOSE + "<p>" + life
						+ PARAGRAPH_TAG_CLOSE;
			}

			// Get the language settings
			String displaySize = getDisplaySize(value1, "FontSizeL");
			String displayFont = getDisplayFont(value1, "FontFaceL");
			// If the default user's font size is larger than the required there is not need to change it.
			displaySize = Integer.toString(Math.max(Integer.parseInt(displaySize), value1.getSize())); 

			if (checkHymn(tropar)) {
				textOut += generateHymn(toneNumbers, saintInfo, displaySize, displayFont, troparP, troparT, tropar, saintInfo[1]);
			}

			if (checkHymn(tropar2)) {
				textOut += generateHymn(toneNumbers, saintInfo, displaySize, displayFont, troparP2, troparT2, tropar2, saintInfo[1]);
			}

			if (checkHymn(kondak)) {
				textOut += generateHymn(toneNumbers, saintInfo, displaySize, displayFont, kondakP, kondakT, kondak, saintInfo[3]);
			}

			if (checkHymn(kondak2)) {
				textOut += generateHymn(toneNumbers, saintInfo, displaySize, displayFont, kondakP2, kondakT2, kondak2, saintInfo[3]);
			}
		}
		return textOut;
	}

	protected boolean checkHymn(String hymn) {
		return hymn != null && !hymn.isEmpty() && !hymn.equals(DOUBLE_NEWLINE);
	}

	private String generateHymn(String[] toneNumbers, String[] saintInfo, String displaySize, String displayFont,
			String podobenMelody, String hymnTone, String hymn, String typeOfHymn) {
		String hymnOutput = H2_TAG_CENTERED + typeOfHymn + H2_TAG_CLOSE;

		String toneFormat = "";

		try {
			int tone = -1;
			tone = Integer.parseInt(hymnTone);
			if (tone != -1) {
				toneFormat = saintInfo[4];
				toneFormat = toneFormat.replace("TT", toneNumbers[tone]);
			}
		} catch (Exception e) {
			toneFormat = hymnTone;
		}

		if (podobenMelody != null && !podobenMelody.isEmpty()) {
			hymnOutput += PARAGRAPH_TAG_CENTERED + toneFormat + saintInfo[5] + saintInfo[2]
					+ podobniMap.get(hymnTone + podobenMelody) + PARAGRAPH_TAG_CLOSE;
		} else {
			hymnOutput += PARAGRAPH_TAG_CENTERED + toneFormat + PARAGRAPH_TAG_CLOSE;
		}
		hymnOutput += "<p style=\"font-family:" + displayFont + ";font-size:" + displaySize + "\">" + hymn
				+ PARAGRAPH_TAG_CLOSE;
		return hymnOutput;
	}


	private String getDisplayFont(Font value1, String lookUp) {
		String displayFont = text.getPhrases().get(lookUp);

		if (displayFont == null || displayFont.equals("")) {
			displayFont = value1.getFontName();
		}
		return displayFont;
	}

	private String getDisplaySize(Font value1, String lookUp) {
		String displaySize = text.getPhrases().get(lookUp);
		if (displaySize == null || displaySize.equals("")) {
			displaySize = Integer.toString(value1.getSize());
		}
		return displaySize;
	}

	@Override

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			activateHyperlink(e);
		}
	}

	private void activateHyperlink(HyperlinkEvent e) {
		String description = e.getDescription();
		System.out.println(description);
		if (description.contains("bible")) {
			String[] bibleParts = description.split("=")[1].split("#");
			activateBibleHyperlink(bibleParts[0], bibleParts[1]);
		} else if (description.contains("saint")) {
			activateSaintHyperlink(description);
		}
	}

	private void activateBibleHyperlink(String book, String chapterAndVerse) {
		try {
			bible.update(book, chapterAndVerse);
			bible.setVisible(true);
		} catch (NullPointerException npe) {
			bible = new Bible(book, chapterAndVerse, analyse.getDayInfo());
			Helpers orient = new Helpers(analyse.getDayInfo());
			orient.applyOrientation(bible, (ComponentOrientation) analyse.getDayInfo().get(Constants.ORIENT));
		}
	}
	
	private void activateSaintHyperlink(String description) {
		System.out.println("Unimplemented hyperlink for: " + description);
		// Deal with a Commemoration Link, but this should be done differently and
		// later.
		/*
		 * if (parts[0].indexOf("goDoSaint") != -1) {
		 * 
		 * String[] parts2=parts[1].split("="); //System.out.println(parts2[1]);
		 * String[] parts3=parts2[1].split(",");
		 * 
		 * Commemoration1 trial1=new
		 * Commemoration1(parts3[parts3.length-2],parts3[parts3.length-1]); if
		 * (SaintLink == null){ System.out.println(parts3[parts3.length-1]);
		 * 
		 * SaintLink=new DoSaint1(trial1); } else { SaintLink.refresh(trial1); }
		 * 
		 * }
		 */
	}

    public void startDocument() {
        read = false;
        life = "";
        tropar = "";
        troparT = ""; //Tone
        troparP = "";  //Podoben melody
        kondak = "";
        kondakT = "";  //Tone
        kondakP = "";  //Podoben melody
        textR = "";
        name = "";
        copyright = "";
        repose="";

    }

    public void endDocument() {
    }

    public void startElement(String elem, HashMap<String, String> table) {

        // THE TAG COULD CONTAIN A COMMAND Cmd
        // THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
        // TODAY'S INFORMATION IN dayInfo.
        // IT WOULD BE VERY RARE IN THIS CASE
        if (table.get("Cmd") != null) {
            // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

            if (!analyse.evalbool(table.get("Cmd"))) {

                return;
            }
        }
        //if (elem.equals("LANGUAGE")) {
            read = true;
            //System.out.println(table.get("Cmd").toString());
          //  return;
        //}
        if (elem.equals("LIFE") && read) {
            if (table.get(Constants.COPYRIGHT) != null) {
                copyright = table.get(Constants.COPYRIGHT);
            }
            if (table.get("Repose") != null) {
                repose = table.get(Constants.COPYRIGHT);
            }
            return;
        }
        if (elem.equals("NAME") && read) {
            name = table.get(Constants.NOMINATIVE);
            if (!repose.isEmpty()){
                name=name+"(\u2020 "+repose+")";
            }
        }
        if (elem.equals(TROPARION) && read) {
            troparT = table.get("Tone");
            if (table.get(PODOBEN) != null) {
                troparP = table.get(PODOBEN);
            }
        }
        if (elem.equals(KONTAKION) && read) {
            kondakT = table.get("Tone");
            if (table.get(PODOBEN) != null) {
                kondakP = table.get(PODOBEN);
            }
        }
        if (elem.equals("PODOBEN") && read) {
            String tone = table.get("Tone");
            String caseP = table.get("Case");
            String intro = table.get("Intro");
            podobniMap.put(tone + caseP, intro);
        }
        /*if(readService && read){
        Location1+="/"+elem;
        //elemRH=elem;
        value=new OrderedHashtable();
        for(Enumeration e = table.keys(); e.hasMoreElements();)
        {
        String type = (String)e.nextElement();
        value.put(type,table.get(type));
        }
        return;
        }*/
        //if(elem.equals("ROYALHOURS") && read){
        //  readRH=true;
        //RoyalHours=new OrderedHashtable();
        //}

    }

    public void endElement(String elem) {
        if (elem.equals(Constants.LANGUAGE)) {
            read = false;
        }
        if (elem.equals("LIFE") && read) {
            life = textR;

            //System.out.println(ServiceInfo);
            //if(ServiceInfo.containsKey("ROYALHOURS/VERSE")){
            //System.out.println(ServiceInfo.get("ROYALHOURS/VERSE"));
            //}
        }
        if (elem.equals(TROPARION) && read) {
            tropar = textR;

            //System.out.println(ServiceInfo);
            //if(ServiceInfo.containsKey("ROYALHOURS/VERSE")){
            //System.out.println(ServiceInfo.get("ROYALHOURS/VERSE"));
            //}
        }
        if (elem.equals(KONTAKION) && read) {
            kondak = textR;

            //System.out.println(ServiceInfo);
            //if(ServiceInfo.containsKey("ROYALHOURS/VERSE")){
            //System.out.println(ServiceInfo.get("ROYALHOURS/VERSE"));
            //}
        }
        //if(readService && read){
                    /*if(elem.equals("VERSE")){
        System.out.println(textR);
        }*/
        //System.out.println("In endElement, I saw the following elements: "+elem);
                  /*  if (textR!=null){
        value.put("text",textR);
        }
        if(ServiceInfo.containsKey(Location1)){
        OrderedHashtable stuff=(OrderedHashtable)ServiceInfo.get(Location1);
        stuff.put(value.get("Type"),value);
        ServiceInfo.put(Location1,stuff);
         */    /*if(elem.equals("VERSE")){
        System.out.println(value.get("Type"));
        System.out.println(stuff.get(value.get("Type")));
        System.out.println(ServiceInfo.get(Location1));
        }/*/
        //Location1=Location1.substring(0,Location1.lastIndexOf("/"));
                    /*}
        else{
        // CREATE A NEW ORDEREDHASHTABLE TO STORE THE DATA
        OrderedHashtable stuff=new OrderedHashtable();

        if(!(value.get("Type") == null)){
        //There are instances of this info
        stuff.put(value.get("Type"),value);

        }
        else{
        //There are no other instances of this info
        if (elemRH==null || value==null){
        //System.out.println("A null set of values was encountered. Why? At point elemRH = "+elemRH+" and value = "+value+" and location = "+Location1);
        Location1=Location1.substring(0,Location1.lastIndexOf("/"));
        return;
        }
        stuff.put(elemRH,value);
        ServiceInfo.put(Location1,value);
        }
        ServiceInfo.put(Location1,stuff);
        }
        value=new OrderedHashtable();
        Location1=Location1.substring(0,Location1.lastIndexOf("/"));
         */
        //return;
        //}

        /*if(elem.equals("TROPARION") && read)
        {
        variable.put("Troparion",textR);
        Information.put("Troparion",variable);
        }
        if(elem.equals("KONTAKION") && read)
        {
        variable.put("Kontakion",textR);
        Information.put("Kontakion",variable);
        }*/
        /*if(elem.equals("NAME") && read)
        {
        name=textR;
        }*/
    }

	public void text(String text) {
		textR = text;
		if (text.equals("\n\n")) {
			textR = "";
		}
	}

	private boolean eval(String expression) throws IllegalArgumentException {
		return false;
	}

    /*public String readText(String filename) {
        try {
            String text = "";
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(Analyse.getDayInfo().get("LS").toString(),filename)), "UTF8"));
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
    }*/

    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem) (e.getSource());
        String name1 = source.getText();
        if (name1.equals(helpNames[2])) {
            Helpers orient = new Helpers(analyse.getDayInfo());
            orient.applyOrientation(new About(analyse.getDayInfo()), (ComponentOrientation) analyse.getDayInfo().get(Constants.ORIENT));
        }
        if (name1.equals(helpNames[0])) {
            //LAUNCH THE HELP FILE
        }
        if (name1.equals(fileNames[1])) {
            //SAVE THE CURRENT WINDOW
            String strippedName = name.replace("<sup>", "").replace("</sup>", "");
            helper.saveHTMLFile(strippedName, strOut);
        }
        if (name1.equals(fileNames[4])) {
            //CLOSE THE PRIMES FRAME
            if (helper.closeFrame(languageNames[7])) {
                frames.dispose();
            }
        }
        if (name1.equals(fileNames[6])) {
            //PRINT THE FILE
            helper.sendHTMLToPrinter(output);
        }

    }

    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf('.');
        return classString.substring(dotIndex + 1);
    }

    public void itemStateChanged(ItemEvent e) {
        JMenuItem source = (JMenuItem) (e.getSource());
        /*String s = "Item event detected."
        + newline
        + "    Event source: " + source.getText()
        + " (an instance of " + getClassName(source) + ")"
        + newline
        + "    New state: "
        + ((e.getStateChange() == ItemEvent.SELECTED) ?
        "selected":"unselected");
        System.out.println(s);*/
        //output.append(s + newline);
        //output.setCaretPosition(output.getDocument().getLength());
    }

	public void propertyChange(PropertyChangeEvent e) {
		// THERE IS NOTHING HERE TO DO??
		try {
			// strOut=createPrimes();
			// output.setText(strOut);
			output.setCaretPosition(0);
		} catch (Exception e1) {
		}
	}
    
    public static void main(String[] argz) {
        LinkedHashMap<String, String> dayInfo = new LinkedHashMap<>();
        dayInfo.put("LS", "0");
        /*setTitle((String)Phrases.Phrases.get("0"));
        RSep=(String)Phrases.Phrases.get("ReadSep");
        CSep=(String)Phrases.Phrases.get("CommSep");
        Colon=(String)Phrases.Phrases.get("Colon");*/
        dayInfo.put(Constants.FONT_FACE_M, "TimesNewRoman");
        dayInfo.put(Constants.FONT_SIZE_M, "14");
        /*StringOp.dayInfo.put("ReadSep",RSep);
        dayInfo.put("Colon",Colon);
        Ideographic=(String)Phrases.Phrases.get("Ideographic");*/
        dayInfo.put(Constants.IDEOGRAPHIC, "0");

        //DoSaint1 test = new DoSaint1("134",dayInfo); //Forefeast of Christmas
        //System.out.println(Paramony.getService("/KONTAKION","1"));
    }
}

