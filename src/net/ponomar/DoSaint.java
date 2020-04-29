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
import net.ponomar.parsing.DocHandler;
import net.ponomar.parsing.QDParser;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 
 
import net.ponomar.utility.StringOp;

/***********************************************************************
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
 ***********************************************************************/

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
    private LinkedHashMap podobni;
    
    
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

    public DoSaint(Commemoration saintInfo, LinkedHashMap dayInfo) {
        //Get the Podobni
        analyse.setDayInfo(dayInfo);
        text = new LanguagePack(dayInfo);
        //PrimesNames = Text.obtainValues((String) Text.Phrases.get("Primes"));
    languageNames = text.obtainValues((String) text.getPhrases().get(Constants.LANGUAGE_MENU));

    fileNames = text.obtainValues((String) text.getPhrases().get("File"));
    helpNames = text.obtainValues((String) text.getPhrases().get("Help"));
    helper=new Helpers(analyse.getDayInfo());

        podobni = new LinkedHashMap();
        try {
            BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(analyse.getDayInfo().get("LS").toString(),PODOBNI)), StandardCharsets.UTF_8));
            QDParser.parse(this, frf);
        } catch (Exception primes) {
            primes.printStackTrace();
        }
        refresh(saintInfo);
    }

    public void refresh(Commemoration saintInfo) {
        
        saintInfo2=saintInfo;
        createWindow();

  }

    private void createWindow()//(String textOut)
    {
        //Order the desired Text
        name=saintInfo2.getName();
        name2=saintInfo2.getGrammar("Short");
        life=saintInfo2.getLife();
        copyright=saintInfo2.getLifeCopyright();
        LinkedHashMap troparInfo=(LinkedHashMap)saintInfo2.getService("/LITURGY/TROPARION","1");
         if (troparInfo !=null){
        tropar=troparInfo.get("text").toString();
        troparT=troparInfo.get("Tone").toString();
        if (troparInfo.get("Podoben")!=null){
        troparP=troparInfo.get("Podoben").toString();
        }}
        else{
            tropar=null;
        }

         LinkedHashMap troparInfo2=(LinkedHashMap)saintInfo2.getService("/LITURGY/TROPARION","2");
        if (troparInfo2 !=null){
        tropar2=troparInfo2.get("text").toString();
        troparT2=troparInfo2.get("Tone").toString();
        if (troparInfo2.get("Podoben")!=null){
        troparP2=troparInfo2.get("Podoben").toString();
        }}
        else{
            tropar2=null;
        }
         

        LinkedHashMap kontakionInfo=(LinkedHashMap)saintInfo2.getService("/LITURGY/KONTAKION","1");
        if (kontakionInfo !=null){
        kondak=kontakionInfo.get("text").toString();
        kondakT=kontakionInfo.get("Tone").toString();
         if (kontakionInfo.get("Podoben")!=null){
        kondakP=kontakionInfo.get("Podoben").toString();
         }}
         else{
            kondak=null;
         }

        LinkedHashMap kontakionInfo2=(LinkedHashMap)saintInfo2.getService("/LITURGY/KONTAKION","2");
        if (kontakionInfo2 !=null){
        kondak2=kontakionInfo2.get("text").toString();
        kondakT2=kontakionInfo2.get("Tone").toString();
         if (kontakionInfo2.get("Podoben")!=null){
        kondakP2=kontakionInfo2.get("Podoben").toString();
         }}
         else{
            kondak2=null;
         }


        text = new LanguagePack(analyse.getDayInfo());
        String[] toneNumbers = text.obtainValues((String) text.getPhrases().get("Tones"));
        String[] mainNames = text.obtainValues((String) text.getPhrases().get("Main"));
        String[] saintInfo = text.obtainValues((String) text.getPhrases().get("LivesW"));
        String textOut = "";
        
        if (name.equals("")) {
            textOut = saintInfo[6];
        } else {
            String displayFontM = (String) text.getPhrases().get(Constants.FONT_FACE_M);
            String displaySizeM = (String) text.getPhrases().get(Constants.FONT_SIZE_M);
            Font value1 = (Font) UIManager.get("Menu.font");
            if (displaySizeM == null || displaySizeM.equals("")) {
                displaySizeM = Integer.toString(value1.getSize());
            }
            if (displayFontM == null || displayFontM.equals("")) {
                displayFontM = value1.getFontName();
            }
            displaySizeM = Integer.toString(Math.max(Integer.parseInt(displaySizeM), value1.getSize()));

            textOut = "<body style=\"font-family:" + displayFontM + ";font-size:" + displaySizeM + ";\"><h1 style=\"text-align: center;\">" + name + "</h1>";
            if (life != null && !life.equals("")) {
                textOut += "<h2 style=\"text-align: center;\">" + saintInfo[0] + "</h2>" + "<p style=\"text-align: center;\"><small>" + copyright + "</small></p>" + "<p>" + life + "</p>";
            }

            //Get the language settings
            String displayFont = (String) text.getPhrases().get("FontFaceL");
            String displaySize = (String) text.getPhrases().get("FontSizeL");
            
            if (displaySize == null || displaySize.equals("")) {
                displaySize = Integer.toString(value1.getSize());
            }
            if (displayFont == null || displayFont.equals("")) {
                displayFont = value1.getFontName();
            }
            displaySize = Integer.toString(Math.max(Integer.parseInt(displaySize), value1.getSize())); //If the default user's font size is larger than the required there is not need to change it.
           
            if (tropar != null && !tropar.isEmpty() && !tropar.equals("\n \n")) {
                textOut += "<h2 style=\"text-align: center;\">" + saintInfo[1] + "</h2>";
                String toneFormat = "";
                try
                {
                 int tone = -1;
                    tone = Integer.parseInt(troparT);


                

                if (tone != -1) {
                    toneFormat = saintInfo[4];
                    toneFormat = toneFormat.replace("TT", toneNumbers[tone]);
                }
                }
                catch (Exception e)
                {
                    toneFormat=troparT;
                }
                if (troparP != null && !troparP.isEmpty()) {
                    textOut += "<p style=\"text-align: center;\">" + toneFormat + saintInfo[5] + saintInfo[2] + podobni.get(troparT + troparP) + "</p>";
                } else {
                    textOut += "<p style=\"text-align: center;\">" + toneFormat + "</p>";
                }
                textOut += "<p style=\"font-family:" + displayFont + ";font-size:" + displaySize + "\">" + tropar + "</p>";
            }

            if (tropar2 != null && !tropar2.isEmpty() && !tropar2.equals("\n \n")) {
                textOut += "<h2 style=\"text-align: center;\">" + saintInfo[1] + "</h2>";
                String toneFormat = "";
                try
                {
                 int tone = -1;
                    tone = Integer.parseInt(troparT2);




                if (tone != -1) {
                    toneFormat = saintInfo[4];
                    toneFormat = toneFormat.replace("TT", toneNumbers[tone]);
                }
                }
                catch (Exception e)
                {
                    toneFormat=troparT2;
                }
                if (troparP2 != null && !troparP2.isEmpty()) {
                    textOut += "<p style=\"text-align: center;\">" + toneFormat + saintInfo[5] + saintInfo[2] + podobni.get(troparT2 + troparP2) + "</p>";
                } else {
                    textOut += "<p style=\"text-align: center;\">" + toneFormat + "</p>";
                }
                textOut += "<p style=\"font-family:" + displayFont + ";font-size:" + displaySize + "\">" + tropar2 + "</p>";
            }

            if (kondak != null && !kondak.isEmpty() && !kondak.equals("\n \n")) {
                textOut += "<h2 style=\"text-align: center;\">" + saintInfo[3] + "</h2>";
                
                String toneFormat = "";

                try
                {
                    int tone = Integer.parseInt(kondakT);
                if (tone != -1) {
                    toneFormat = saintInfo[4];
                    toneFormat = toneFormat.replace("TT", toneNumbers[tone]);
                }
                }
                catch (Exception e)
                {
                    toneFormat=kondakT;
                }
                
                if (kondakP != null && !kondakP.isEmpty()) {
                    textOut += "<p style=\"text-align: center;\">" + toneFormat + saintInfo[5] + saintInfo[2] + podobni.get(kondakT + kondakP) + "</p>";
                } else {
                    textOut += "<p style=\"text-align: center;\">" + toneFormat + "</p>";
                }
                textOut += "<p style=\"font-family:" + displayFont + ";font-size:" + displaySize + "\">" + kondak + "</p>";
            }

            if (kondak2 != null && !kondak2.isEmpty() && !kondak2.equals("\n \n")) {
                textOut += "<h2 style=\"text-align: center;\">" + saintInfo[3] + "</h2>";

                String toneFormat = "";

                try
                {
                    int tone = Integer.parseInt(kondakT2);
                if (tone != -1) {
                    toneFormat = saintInfo[4];
                    toneFormat = toneFormat.replace("TT", toneNumbers[tone]);
                }
                }
                catch (Exception e)
                {
                    toneFormat=kondakT2;
                }

                if (kondakP2 != null && !kondakP2.isEmpty()) {
                    textOut += "<p style=\"text-align: center;\">" + toneFormat + saintInfo[5] + saintInfo[2] + podobni.get(kondakT2 + kondakP2) + "</p>";
                } else {
                    textOut += "<p style=\"text-align: center;\">" + toneFormat + "</p>";
                }
                textOut += "<p style=\"font-family:" + displayFont + ";font-size:" + displaySize + "\">" + kondak2 + "</p>";
            }
        }
        //Other information can go here!
        //String textOut=header+image+rest;
        
        frames = new JFrame((String) text.getPhrases().get("0") + (String) text.getPhrases().get(Constants.COLON) + name2);
        
        //frames.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel left=new JPanel();
        JPanel right=new JPanel();
        left.setLayout(new BorderLayout());
        right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));

        textOut = textOut.replace("</br>", "<BR>");
        textOut = textOut.replace("<br>", "<BR>");
        strOut = textOut;
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);
        output = new PrintableTextPane();
        output.addHyperlinkListener(this);
        //output=new JEditorPane();
        output.setEditable(false);
        output.setSize(800, 700);
        output.setContentType(Constants.CONTENT_TYPE);
        //output.setText(header);
        output.setText(textOut);
        output.setCaretPosition(0);
        contentPane.add(output);

        JScrollPane scrollPane = new JScrollPane(output);
        JMenuBar menuBarElement = new JMenuBar();
        MenuFiles demo = new MenuFiles(analyse.getDayInfo());
        //PrimeSelector trial=new PrimeSelector();
        menuBarElement.add(demo.createFileMenu(this));
        //MenuBar.add(trial.createPrimeMenu());
        menuBarElement.add(demo.createHelpMenu(this));
        frames.setJMenuBar(menuBarElement);
        //trial.addPropertyChangeListener(this);

        contentPane.add(scrollPane, BorderLayout.CENTER);
        right.add(contentPane);

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setLeftComponent(left);
        splitter.setRightComponent(right);

        frames.add(splitter);


        //frames.setContentPane(contentPane);
        

        LinkedHashMap iconsM=(LinkedHashMap)saintInfo2.getDisplayIcons();
        Vector imageList=(Vector)iconsM.get("Images");
                Vector namesList=(Vector)iconsM.get("Names");
                String[] iconImages=new String[imageList.size()];
                String[] iconNames=new String[namesList.size()];

                iconImages=(String[])imageList.toArray(new String[imageList.size()]);
                iconNames=(String[])namesList.toArray(new String[namesList.size()]);
        System.out.println("Icon Length is: " + iconNames.length);
        IconDisplay icons=new IconDisplay(iconImages,iconNames,analyse.getDayInfo());
        left.add(new JPanel(),BorderLayout.NORTH);
        left.add(icons,BorderLayout.CENTER);
        left.add(new JPanel(),BorderLayout.SOUTH);
//        contentPane.add(icons);
  //      textOut+=icons;
    //    output.setText(textOut);

        Helpers orient = new Helpers(analyse.getDayInfo());

        orient.applyOrientation(frames,(ComponentOrientation)analyse.getDayInfo().get(Constants.ORIENT));
        frames.pack();
        frames.setSize(800, 700);
        frames.setVisible(true);
        //scrollPane.top();
    }

    public void hyperlinkUpdate(HyperlinkEvent e)
	{
            if (e.getEventType().toString().equals("ACTIVATED"))
		{
			String cmd = e.getDescription();
			String[] parts = cmd.split("=");
			if (parts[0].contains("bible"))
			{
			String[] parts2=parts[1].split("#");
                            try
				{
					bible.update(parts2[0], parts2[1]);
					bible.show();
				} catch (NullPointerException npe) {
					bible = new Bible(parts2[0], parts2[1],analyse.getDayInfo());
                                        Helpers orient=new Helpers(analyse.getDayInfo());
                orient.applyOrientation(bible,(ComponentOrientation)analyse.getDayInfo().get(Constants.ORIENT));
				}
			}
                        else
                        {
                           //Deal with a Commemoration Link, but this should be done differently and later.
                            /*if (parts[0].indexOf("goDoSaint") != -1)
                            {

                                String[] parts2=parts[1].split("=");
                                //System.out.println(parts2[1]);
                                String[] parts3=parts2[1].split(",");

                                Commemoration1 trial1=new Commemoration1(parts3[parts3.length-2],parts3[parts3.length-1]);
                                if (SaintLink == null){
                                    System.out.println(parts3[parts3.length-1]);

                                    SaintLink=new DoSaint1(trial1);
                                }
                                else
                                {
                                    SaintLink.refresh(trial1);
                                }

                            }*/
                        }
		}
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

    public void startElement(String elem, Hashtable table) {

        // THE TAG COULD CONTAIN A COMMAND Cmd
        // THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
        // TODAY'S INFORMATION IN dayInfo.
        // IT WOULD BE VERY RARE IN THIS CASE
        if (table.get("Cmd") != null) {
            // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

            if (analyse.evalbool(table.get("Cmd").toString()) == false) {

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
                copyright = table.get(Constants.COPYRIGHT).toString();
            }
            if (table.get("Repose") != null) {
                repose = table.get(Constants.COPYRIGHT).toString();
            }
            return;
        }
        if (elem.equals("NAME") && read) {
            name = table.get(Constants.NOMINATIVE).toString();
            if (!repose.isEmpty()){
                name=name+"(\u2020 "+repose+")";
            }
        }
        if (elem.equals("TROPARION") && read) {
            troparT = table.get("Tone").toString();
            if (table.get("Podoben") != null) {
                troparP = table.get("Podoben").toString();
            }
        }
        if (elem.equals("KONTAKION") && read) {
            kondakT = table.get("Tone").toString();
            if (table.get("Podoben") != null) {
                kondakP = table.get("Podoben").toString();
            }
        }
        if (elem.equals("PODOBEN") && read) {
            String tone = table.get("Tone").toString();
            String caseP = table.get("Case").toString();
            String intro = table.get("Intro").toString();
            podobni.put(tone + caseP, intro);
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
        if (elem.equals("TROPARION") && read) {
            tropar = textR;

            //System.out.println(ServiceInfo);
            //if(ServiceInfo.containsKey("ROYALHOURS/VERSE")){
            //System.out.println(ServiceInfo.get("ROYALHOURS/VERSE"));
            //}
        }
        if (elem.equals("KONTAKION") && read) {
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
   

    public static void main(String[] argz) {


        LinkedHashMap dayInfo = new LinkedHashMap();
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
            System.out.println(strOut);
            helper.saveHTMLFile(name, strOut);


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
        //THERE IS NOTHING HERE TO DO??
        try {
            //strOut=createPrimes();
            //output.setText(strOut);
            output.setCaretPosition(0);
        } catch (Exception e1) {
        }

    }
}

