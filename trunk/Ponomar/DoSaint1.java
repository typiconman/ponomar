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
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;

/***********************************************************************
THIS MODULE CREATES THE WINDOW TO DISPLAY THE SAINT INFORMATION.

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
public class DoSaint1 implements DocHandler, ActionListener, ItemListener, PropertyChangeListener, HyperlinkListener {
    //SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
    //THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
    //TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
    //DURING THE COURSE OF A SINGLE WEEK.

    private final static String configFileName = "ponomar.config";   //CONFIGURATIONS FILE
    private final static String lifeFileName = "xml/lives/";   // THE LOCATION OF THE Lives File
    private final static String iconFileName = "images/icons/"; // THE LOCATION OF THE ICON
    private final static String serviceFileName = "xml/Services/menaion/";
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

    private String name = "";
    private String copyright = ""; //Any additional information about the life.
    private LanguagePack Text; //= new LanguagePack();
    private OrderedHashtable Podobni;
    
    private static OrderedHashtable PrimesTK;
    private static String FileNameIn = "xml/Services/PRIMES1/";
    private static String FileNameOut = FileNameIn + "Primes.html";
    private static String text;
    private static boolean read = false;
    private static String Type;
    private final static String triodionFileName = "xml/triodion/";   // TRIODION FILE
    private final static String pentecostarionFileName = "Pxml/pentecostarion/"; // PENTECOSTARION FILE
    private String filename;
    private int lineNumber;
    private String[] PrimesNames;// = Text.obtainValues((String) Text.Phrases.get("Primes"));
    private String[] LanguageNames;// = Text.obtainValues((String) Text.Phrases.get("LanguageMenu"));
    private String LentenK;				//ANY REQUIRED KATHISMA REFERENCED USING "LENTENK = "17"" WOULD BE THE 17th KATHISMA.
    private JFrame frames;
    private String[] FileNames;// = Text.obtainValues((String) Text.Phrases.get("File"));
    private String[] HelpNames;// = Text.obtainValues((String) Text.Phrases.get("Help"));
    String newline = "\n";
    private String strOut;
    private JDate today;
    private Helpers helper;// = new Helpers();
    //private PrimeSelector SelectorP=new PrimeSelector();
    private PrintableTextPane output;
    //private JEditorPane output;
    private String DisplayFont = new String(); //ALLOWS A CUSTOM FONT AND SIZE TO BE SPECIFIED FOR A GIVEN BIBLE READING: REQUIRED FOR OLD CHURCH SLAVONIC AT PRESENT
    private String DisplaySize = "12";  //UNTIL A COMPLETE UNICODE FONT IS AVAILIBLE.
    private Font DefaultFont = new Font("", Font.BOLD, 12);		//CREATE THE DEFAULT FONT
    private Font CurrentFont = DefaultFont;
    private Commemoration1 SaintInfo2;
    private Bible bible;
    private String name2;
    private StringOp Analyse=new StringOp();

    public DoSaint1(Commemoration1 SaintInfo, OrderedHashtable dayInfo) {
        //Get the Podobni
        Analyse.dayInfo=dayInfo;
        Text = new LanguagePack(dayInfo);
        PrimesNames = Text.obtainValues((String) Text.Phrases.get("Primes"));
    LanguageNames = Text.obtainValues((String) Text.Phrases.get("LanguageMenu"));

    FileNames = Text.obtainValues((String) Text.Phrases.get("File"));
    HelpNames = Text.obtainValues((String) Text.Phrases.get("Help"));
    helper=new Helpers(Analyse.dayInfo);

        Podobni = new OrderedHashtable();
        try {
            BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(Analyse.dayInfo.get("LS").toString(),"xml/Commands/Podobni.xml")), "UTF8"));
            QDParser.parse(this, frf);
        } catch (Exception Primes) {
            Primes.printStackTrace();
        }
        refresh(SaintInfo);
    }

    public void refresh(Commemoration1 SaintInfo) {
        //Get Life, Tropar, Icon, and Liturgical Information!
        //Get Life and TroparA
        /*if (SaintId.equals(""))
        {
            SaintId="-100";
        }
        name="";
        try {
            BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(StringOp.dayInfo.get("LS").toString(),lifeFileName + SaintId + ".xml")), "UTF8"));
            QDParser.parse(this, frf);
        } catch (Exception Primes) {
            //Primes.printStackTrace();
            }*/
        SaintInfo2=SaintInfo;
        CreateWindow();

        //PrimesWindow("<h1 style=\"text-align: center;\">"+name+"</h1>"+life+"</p><h2>Troparion</h2> <small>"+troparT+"</small><p>"+tropar+"</p><h2>Kontakion</h2><small>"+kondakT+"/"+kondakP+"</small><p>"+kondak+"</p>");
//"<img id=\"137\" src=\"file://C:\\Users/Yuri/Ponomar Main/Subversion Version/ponomar/"+iconFileName+"137.jpg\" width=\"238\" height=\"300\" alt=\"\" />","<p>"
    }

    private void CreateWindow()//(String textOut)
    {
        //Order the desired Text
        name=SaintInfo2.getName();
        name2=SaintInfo2.getGrammar("Short");
        life=SaintInfo2.getLife();
        copyright=SaintInfo2.getLifeCopyright();
        OrderedHashtable troparInfo=(OrderedHashtable)SaintInfo2.getService("/LITURGY/TROPARION","1");
         if (troparInfo !=null){
        tropar=troparInfo.get("text").toString();
        troparT=troparInfo.get("Tone").toString();
        if (troparInfo.get("Podoben")!=null){
        troparP=troparInfo.get("Podoben").toString();
        }}
        else{
            tropar=null;
        }

        OrderedHashtable troparInfo2=(OrderedHashtable)SaintInfo2.getService("/LITURGY/TROPARION","2");
        if (troparInfo2 !=null){
        tropar2=troparInfo2.get("text").toString();
        troparT2=troparInfo2.get("Tone").toString();
        if (troparInfo2.get("Podoben")!=null){
        troparP2=troparInfo2.get("Podoben").toString();
        }}
        else{
            tropar2=null;
        }
         

        OrderedHashtable kontakionInfo=(OrderedHashtable)SaintInfo2.getService("/LITURGY/KONTAKION","1");
        if (kontakionInfo !=null){
        kondak=kontakionInfo.get("text").toString();
        kondakT=kontakionInfo.get("Tone").toString();
         if (kontakionInfo.get("Podoben")!=null){
        kondakP=kontakionInfo.get("Podoben").toString();
         }}
         else{
            kondak=null;
         }

        OrderedHashtable kontakionInfo2=(OrderedHashtable)SaintInfo2.getService("/LITURGY/KONTAKION","2");
        if (kontakionInfo2 !=null){
        kondak2=kontakionInfo2.get("text").toString();
        kondakT2=kontakionInfo2.get("Tone").toString();
         if (kontakionInfo2.get("Podoben")!=null){
        kondakP2=kontakionInfo2.get("Podoben").toString();
         }}
         else{
            kondak2=null;
         }


        Text = new LanguagePack(Analyse.dayInfo);
        String[] toneNumbers = Text.obtainValues((String) Text.Phrases.get("Tones"));
        String[] MainNames = Text.obtainValues((String) Text.Phrases.get("Main"));
        String[] SaintInfo = Text.obtainValues((String) Text.Phrases.get("LivesW"));
        String textOut = "";
        //System.out.println(SaintInfo[1]);
        if (name.equals("")) {
            textOut = SaintInfo[6];
        } else {
            String DisplayFontM = (String) Text.Phrases.get("FontFaceM");
            String DisplaySizeM = (String) Text.Phrases.get("FontSizeM");
            Font value1 = (Font) UIManager.get("Menu.font");
            if (DisplaySizeM == null || DisplaySizeM.equals("")) {
                DisplaySizeM = Integer.toString(value1.getSize());
            }
            if (DisplayFontM == null || DisplayFontM.equals("")) {
                DisplayFontM = value1.getFontName();
            }
            DisplaySizeM = Integer.toString(Math.max(Integer.parseInt(DisplaySizeM), value1.getSize()));

            textOut = "<body style=\"font-family:" + DisplayFontM + ";font-size:" + DisplaySizeM + ";\"><h1 style=\"text-align: center;\">" + name + "</h1>";
            if (life != null && !life.equals("")) {
                textOut += "<h2 style=\"text-align: center;\">" + SaintInfo[0] + "</h2>" + "<p style=\"text-align: center;\"><small>" + copyright + "</small></p>" + "<p>" + life + "</p>";
            }

            //Get the language settings
            String DisplayFont = (String) Text.Phrases.get("FontFaceL");
            String DisplaySize = (String) Text.Phrases.get("FontSizeL");
            
            if (DisplaySize == null || DisplaySize.equals("")) {
                DisplaySize = Integer.toString(value1.getSize());
            }
            if (DisplayFont == null || DisplayFont.equals("")) {
                DisplayFont = value1.getFontName();
            }
            DisplaySize = Integer.toString(Math.max(Integer.parseInt(DisplaySize), value1.getSize())); //If the default user's font size is larger than the required there is not need to change it.
            //The specified fonts sizes are the mininum required.
            //    Style+="body {font-family:"+DisplayFont+";font-size:"+DisplaySize+"}\n";
            //System.out.println("["+tropar+"]"+tropar.length());
            //System.out.println(name + ": " + tropar + "; " + tropar.length()+"; "+tropar.equals("\n \n"));
            if (tropar != null && tropar != "" && !tropar.equals("\n \n")) {
                textOut += "<h2 style=\"text-align: center;\">" + SaintInfo[1] + "</h2>";
                String ToneFormat = new String();
                try
                {
                 int tone = -1;
                    tone = Integer.parseInt(troparT);


                

                if (tone != -1) {
                    ToneFormat = SaintInfo[4];
                    ToneFormat = ToneFormat.replace("TT", toneNumbers[tone]);
                }
                }
                catch (Exception e)
                {
                    ToneFormat=troparT;
                }
                if (troparP != null && troparP != "") {
                    textOut += "<p style=\"text-align: center;\">" + ToneFormat + SaintInfo[5] + SaintInfo[2] + Podobni.get(troparT + troparP) + "</p>";
                } else {
                    textOut += "<p style=\"text-align: center;\">" + ToneFormat + "</p>";
                }
                textOut += "<p style=\"font-family:" + DisplayFont + ";font-size:" + DisplaySize + "\">" + tropar + "</p>";
            }

            if (tropar2 != null && tropar2 != "" && !tropar2.equals("\n \n")) {
                textOut += "<h2 style=\"text-align: center;\">" + SaintInfo[1] + "</h2>";
                String ToneFormat = new String();
                try
                {
                 int tone = -1;
                    tone = Integer.parseInt(troparT2);




                if (tone != -1) {
                    ToneFormat = SaintInfo[4];
                    ToneFormat = ToneFormat.replace("TT", toneNumbers[tone]);
                }
                }
                catch (Exception e)
                {
                    ToneFormat=troparT2;
                }
                if (troparP2 != null && troparP2 != "") {
                    textOut += "<p style=\"text-align: center;\">" + ToneFormat + SaintInfo[5] + SaintInfo[2] + Podobni.get(troparT2 + troparP2) + "</p>";
                } else {
                    textOut += "<p style=\"text-align: center;\">" + ToneFormat + "</p>";
                }
                textOut += "<p style=\"font-family:" + DisplayFont + ";font-size:" + DisplaySize + "\">" + tropar2 + "</p>";
            }

            if (kondak != null && kondak != "" && !kondak.equals("\n \n")) {
                textOut += "<h2 style=\"text-align: center;\">" + SaintInfo[3] + "</h2>";
                
                String ToneFormat = new String();

                try
                {
                    int tone = Integer.parseInt(kondakT);
                if (tone != -1) {
                    ToneFormat = SaintInfo[4];
                    ToneFormat = ToneFormat.replace("TT", toneNumbers[tone]);
                }
                }
                catch (Exception e)
                {
                    ToneFormat=kondakT;
                }
                
                if (kondakP != null && kondakP != "") {
                    textOut += "<p style=\"text-align: center;\">" + ToneFormat + SaintInfo[5] + SaintInfo[2] + Podobni.get(kondakT + kondakP) + "</p>";
                } else {
                    textOut += "<p style=\"text-align: center;\">" + ToneFormat + "</p>";
                }
                textOut += "<p style=\"font-family:" + DisplayFont + ";font-size:" + DisplaySize + "\">" + kondak + "</p>";
            }

            if (kondak2 != null && kondak2 != "" && !kondak2.equals("\n \n")) {
                textOut += "<h2 style=\"text-align: center;\">" + SaintInfo[3] + "</h2>";

                String ToneFormat = new String();

                try
                {
                    int tone = Integer.parseInt(kondakT2);
                if (tone != -1) {
                    ToneFormat = SaintInfo[4];
                    ToneFormat = ToneFormat.replace("TT", toneNumbers[tone]);
                }
                }
                catch (Exception e)
                {
                    ToneFormat=kondakT2;
                }

                if (kondakP2 != null && kondakP2 != "") {
                    textOut += "<p style=\"text-align: center;\">" + ToneFormat + SaintInfo[5] + SaintInfo[2] + Podobni.get(kondakT2 + kondakP2) + "</p>";
                } else {
                    textOut += "<p style=\"text-align: center;\">" + ToneFormat + "</p>";
                }
                textOut += "<p style=\"font-family:" + DisplayFont + ";font-size:" + DisplaySize + "\">" + kondak2 + "</p>";
            }
        }
        //Other information can go here!
        //String textOut=header+image+rest;
        
        frames = new JFrame((String) Text.Phrases.get("0") + (String) Text.Phrases.get("Colon") + name2);
        
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textOut = textOut.replaceAll("</br>", "<BR>");
        textOut = textOut.replaceAll("<br>", "<BR>");
        strOut = textOut;
        //System.out.println(textOut);
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);
        output = new PrintableTextPane();
        output.addHyperlinkListener(this);
        //output=new JEditorPane();
        output.setEditable(false);
        output.setSize(800, 700);
        output.setContentType("text/html; charset=UTF-8");
        //output.setText(header);
        output.setText(textOut);
        output.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(output);
        JMenuBar MenuBar = new JMenuBar();
        MenuFiles demo = new MenuFiles(Analyse.dayInfo);
        //PrimeSelector trial=new PrimeSelector();
        MenuBar.add(demo.createFileMenu(this));
        //MenuBar.add(trial.createPrimeMenu());
        MenuBar.add(demo.createHelpMenu(this));
        frames.setJMenuBar(MenuBar);
        //trial.addPropertyChangeListener(this);

        contentPane.add(scrollPane, BorderLayout.CENTER);
        frames.setContentPane(contentPane);
        frames.pack();
        frames.setSize(800, 700);
        frames.setVisible(true);

        /*OrderedHashtable iconsM=(OrderedHashtable)SaintInfo2.getDisplayIcons();
        Vector ImageList=(Vector)iconsM.get("Images");
                Vector NamesList=(Vector)iconsM.get("Names");
                String[] iconImages=new String[ImageList.size()];
                String[] iconNames=new String[NamesList.size()];

                iconImages=(String[])ImageList.toArray(new String[ImageList.size()]);
                iconNames=(String[])NamesList.toArray(new String[NamesList.size()]);
        System.out.println("Icon Length is: " + iconNames.length);
        IconDisplay icons=new IconDisplay(iconImages,iconNames);
        contentPane.add(icons);
        textOut+=icons;
        output.setText(textOut);*/

        Helpers orient = new Helpers(Analyse.dayInfo);

        orient.applyOrientation(frames,(ComponentOrientation)Analyse.dayInfo.get("Orient"));

        //scrollPane.top();
    }

    public void hyperlinkUpdate(HyperlinkEvent e)
	{
            if (e.getEventType().toString() == "ACTIVATED")
		{
			String cmd = e.getDescription();
			String[] parts = cmd.split("=");
			if (parts[0].indexOf("bible") != -1)
			{
			String[] parts2=parts[1].split("#");
                            try
				{
					bible.update(parts2[0], parts2[1]);
					bible.show();
				} catch (NullPointerException npe) {
					bible = new Bible(parts2[0], parts2[1],Analyse.dayInfo);
                                        Helpers orient=new Helpers(Analyse.dayInfo);
                orient.applyOrientation(bible,(ComponentOrientation)Analyse.dayInfo.get("Orient"));
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

            if (Analyse.evalbool(table.get("Cmd").toString()) == false) {

                return;
            }
        }
        //if (elem.equals("LANGUAGE")) {
            read = true;
            //System.out.println(table.get("Cmd").toString());
          //  return;
        //}
        if (elem.equals("LIFE") && read) {
            if (table.get("Copyright") != null) {
                copyright = table.get("Copyright").toString();
            }
            return;
        }
        if (elem.equals("NAME") && read) {
            name = table.get("Nominative").toString();
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
            Podobni.put(tone + caseP, intro);
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
        if (elem.equals("LANGUAGE")) {
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


        OrderedHashtable dayInfo = new OrderedHashtable();
        dayInfo.put("LS", "0");
        /*setTitle((String)Phrases.Phrases.get("0"));
        RSep=(String)Phrases.Phrases.get("ReadSep");
        CSep=(String)Phrases.Phrases.get("CommSep");
        Colon=(String)Phrases.Phrases.get("Colon");*/
        dayInfo.put("FontFaceM", "TimesNewRoman");
        dayInfo.put("FontSizeM", "14");
        dayInfo.put("FontFaceM", "TimesNewRoman");
        dayInfo.put("FontSizeM", "14");
        /*StringOp.dayInfo.put("ReadSep",RSep);
        dayInfo.put("Colon",Colon);
        Ideographic=(String)Phrases.Phrases.get("Ideographic");*/
        dayInfo.put("Ideographic", "0");

        //DoSaint1 test = new DoSaint1("134",dayInfo); //Forefeast of Christmas
        //System.out.println(Paramony.getService("/KONTAKION","1"));
        return;

    }

    public String readText(String filename) {
        try {
            text = new String();
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(Analyse.dayInfo.get("LS").toString(),filename)), "UTF8"));
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
        String name1 = source.getText();
        if (name1.equals(HelpNames[2])) {
            Helpers orient = new Helpers(Analyse.dayInfo);
            orient.applyOrientation(new About(Analyse.dayInfo), (ComponentOrientation) Analyse.dayInfo.get("Orient"));
        }
        if (name1.equals(HelpNames[0])) {
            //LAUNCH THE HELP FILE
        }
        if (name1.equals(FileNames[1])) {
            //SAVE THE CURRENT WINDOW
            System.out.println(strOut);
            helper.SaveHTMLFile(name, strOut);


        }
        if (name1.equals(FileNames[4])) {
            //CLOSE THE PRIMES FRAME
            if (helper.closeFrame(LanguageNames[7])) {
                frames.dispose();
            }
        }
        if (name1.equals(FileNames[6])) {
            //PRINT THE FILE
            helper.sendHTMLToPrinter(output);
        }

    }

    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");
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

