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
import javax.swing.text.MutableAttributeSet;


/***********************************************************************
 Main.java :: MAIN MODULE FOR THE PONOMAR PROGRAM.
 THIS MODULE CONSTITUTES THE PRIMARY PONOMAR GUI AND CENTRE OF THE PROGRAM.
 TO START THE PROGRAM, INVOKE main(String[]) OF THIS CLASS.
 OUTPUTS RELEVANT INFORMATION FOR EACH DAY, WITH LINKS TO DETAILED INFO.

 Main.java is part of the Ponomar program.
 Copyright 2006, 2007, 2008, 2009, 2010 Aleksandr Andreev and Yuri Shardt.
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

public class Main extends JFrame implements PropertyChangeListener, DocHandler, HyperlinkListener, ActionListener
{
	// First, some relevant constants
	private final static String configFileName = "ponomar.config"; // CONFIGURATIONS FILE
	//private final static String generalFileName="Ponomar/xml/";
	private final static String triodionFileName   = "xml/triodion/";   // TRIODION FILE
	private final static String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
	private static String newline="\n";

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
	private static boolean read=false;		//DETERMINES WHICH LANGUAGE WILL BE READ
	private String[] SaintNames;
	private String OptionsNames;
	private String[] FileNames; 
	private String[] ServiceNames;
	private String[] BibleName;
	private String[] HelpNames;
        //Get the Correct Fonts
	private String DisplayFont =new String(); //ALLOWS A CUSTOM FONT AND SIZE TO BE SPECIFIED FOR A GIVEN BIBLE READING: REQUIRED FOR OLD CHURCH SLAVONIC AT PRESENT
	private String DisplaySize="12";  //UNTIL A COMPLETE UNICODE FONT IS AVAILIBLE.
	private Font DefaultFont=new Font("",Font.BOLD,12);		//CREATE THE DEFAULT FONT
	private Font CurrentFont=DefaultFont;
        private String RSep=new String();
        private String CSep=new String();
        private String Colon=new String();
        private String Ideographic=new String();
        private DoSaint SaintLink;
        private IconDisplay displayIcon;
        private Vector IconImages;
        private Vector IconNames;
	//private GospelSelector Selector;
        Helpers findLanguage;

	// CONSTRUCTOR
	public Main()
	{
		//super("Ponomar");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		

                //WE NEED THIS HANDY STORER OF VALUES NOW.
		StringOp.dayInfo = new Hashtable();
		//DETERMINE THE DEFAULTS
		ConfigurationFiles.Defaults = new OrderedHashtable();
		ConfigurationFiles.ReadFile();
		LanguageLocation = new LanguageSelector();
                              
                StringOp.dayInfo.put("LS",LanguageLocation.getLValue());
		Phrases = new LanguagePack();
               //Changing language storage format
                findLanguage=new Helpers();

                toneNumbers= Phrases.obtainValues((String)Phrases.Phrases.get("Tones"));
		SaintNames=Phrases.obtainValues((String)Phrases.Phrases.get("SMenu"));
		OptionsNames=(String)Phrases.Phrases.get("Options");
		FileNames=Phrases.obtainValues((String)Phrases.Phrases.get("File")); 
		ServiceNames=Phrases.obtainValues((String)Phrases.Phrases.get("Services"));
		BibleName=Phrases.obtainValues((String)Phrases.Phrases.get("Bible"));
		HelpNames=Phrases.obtainValues((String)Phrases.Phrases.get("Help"));
		
                Errors=Phrases.obtainValues((String)Phrases.Phrases.get("Errors"));
		MainNames=Phrases.obtainValues((String)Phrases.Phrases.get("Main"));
                DisplayFont=(String)Phrases.Phrases.get("FontFaceM");
                DisplaySize=(String)Phrases.Phrases.get("FontSizeM");
                
                Font value1 = (Font)UIManager.get ("Menu.font");
                if (DisplaySize == null || DisplaySize.equals(""))
                {
                    DisplaySize=Integer.toString(value1.getSize());
                }
                if (DisplayFont == null || DisplayFont.equals(""))
                {
                    DisplayFont=value1.getFontName();
                }
                DisplaySize=Integer.toString(Math.max(Integer.parseInt(DisplaySize), value1.getSize())); //If the default user's font size is larger than the required there is not need to change it.
                //The specified fonts sizes are the mininum required.
                CurrentFont=new Font(DisplayFont,Font.PLAIN,Integer.parseInt(DisplaySize));
                //System.out.println(this.getFont());
                //System.out.println("Pause");
                //setDefaultLookAndFeelDecorated( true );
                //UIManager.put("Frame.font",CurrentFont);
                //this.setFont(CurrentFont);
                //This is a nifty way to set the default font for displaying everything in a programme. I (Y.S.) will
                //later work to implement it properly. At present, there seem to be some technical issues with obtaining
                //everything properly.
               java.util.Enumeration keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements())
    {
      Object key = keys.nextElement();
      Object value = UIManager.get (key);
      if (value instanceof javax.swing.plaf.FontUIResource)
      {
          Font keyF=(Font)value;
          String[] splitkey=key.toString().replace(".",":").split(":");
          //This prevents the font from being changed for those things that are to remain in the Latin alphabet!
          if (splitkey.length>1)
          {
            if (splitkey[splitkey.length-1].equals("acceleratorFont"))
            {
                continue;
            }
            /*if (splitkey[0].equals("Button"))
            {
                continue;
            }*/
          }
          if(key.toString().equals("MenuItem.acceleratorFont"))
          {
            continue;
          }
          Font NewFont=new Font(CurrentFont.getFontName(), keyF.getStyle(),CurrentFont.getSize());
        UIManager.put (key, NewFont);    
        //System.out.println(key);
      }
        }
        
                //System.out.println(this.getFont());
                setTitle((String)Phrases.Phrases.get("0"));
                RSep=(String)Phrases.Phrases.get("ReadSep");
                CSep=(String)Phrases.Phrases.get("CommSep");
                Colon=(String)Phrases.Phrases.get("Colon");
                StringOp.dayInfo.put("FontFaceM",DisplayFont);
                StringOp.dayInfo.put("FontSizeM",DisplaySize);
                StringOp.dayInfo.put("ReadSep",RSep);
                StringOp.dayInfo.put("Colon",Colon);
                Ideographic=(String)Phrases.Phrases.get("Ideographic");
                StringOp.dayInfo.put("Ideographic",Ideographic);
		GospelLocation = new GospelSelector();
		
		//ADD A MENU BAR Y.S. 2008/08/11 n.s.
		demo = new MenuFiles();
		MenuBar=new JMenuBar();
		MenuBar.add(demo.createFileMenu(this));
		MenuBar.add(demo.createOptionsMenu(this));
		MenuBar.add(demo.createSaintsMenu(this));
		MenuBar.add(demo.createServicesMenu(this));
		MenuBar.add(demo.createBibleMenu(this));
		MenuBar.add(demo.createHelpMenu(this));
		MenuBar.setFont(CurrentFont);
                //MenuBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		setJMenuBar(MenuBar);
			   
	        JPanel left = new JPanel(new GridLayout(3,0));
		calendar = new JCalendar();
		calendar.addPropertyChangeListener(this);
		left.setLayout(new BorderLayout());
		left.add(calendar, BorderLayout.NORTH);
                displayIcon=new IconDisplay(new String[0],new String[0]);
                left.add(displayIcon,BorderLayout.CENTER);
			
		
									
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

                               Locale place=new Locale(Phrases.Phrases.get("Language").toString(),Phrases.Phrases.get("Country").toString());
        Helpers orient=new Helpers();
        StringOp.dayInfo.put("Locale",place);
                StringOp.dayInfo.put("Orient",ComponentOrientation.getOrientation(place));
                orient.applyOrientation(this,ComponentOrientation.getOrientation(place));
                this.validate();
                
		pack();
		setSize(700, 500);
		setVisible(true);
		
		pascha = Paschalion.getPascha(today.getYear());
		pentecost = Paschalion.getPentecost(today.getYear());
		
		
		inited = true;
                Dimension screen=this.getSize();
                //Default screen size issues for East Asian languages!
                if (value1.getSize()<Integer.parseInt(DisplaySize))
                {
                    Dimension defaultScreen= java . awt . Toolkit . getDefaultToolkit ().getScreenSize();

                    //System.out.println(screen);
                    int newSize=Integer.parseInt(DisplaySize);
                    int MaxW=95*defaultScreen.width/100;
                    int MaxH=95*defaultScreen.height/100;
                    screen.width=java.lang.Math.min(screen.width*newSize/value1.getSize(),MaxW);
                    screen.height=java.lang.Math.min(screen.height*newSize/value1.getSize(),MaxH);
                    this.setSize(screen);
                    //System.out.println(screen);
                }
        
		write();
	}

	public void propertyChange(PropertyChangeEvent e)
	{
		if (inited == true)
		{
			// FIND OUT THE OLD YEAR
			int year = today.getYear();
			today = new JDate(calendar.getMonth(), calendar.getDay(), calendar.getYear());
			if (year != today.getYear())
			{
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
	public void actionPerformed(ActionEvent e)
  	{
        Helpers helper=new Helpers();
        JMenuItem source = (JMenuItem)(e.getSource());
        String name = source.getText();
       if (name.equals(HelpNames[2]))
        {
        	 //new About();
                 Helpers orient=new Helpers();
                orient.applyOrientation(new About(),(ComponentOrientation)StringOp.dayInfo.get("Orient"));
        }
        if (name.equals(HelpNames[0]))
        {
        	 //HELP FILES
        }
        if(name.equals(FileNames[1]))
        {
        	//SAVE THE CURRENT WINDOW
       		helper.SaveHTMLFile(MainNames[5]+ " "+today+".html", "<html><title>"+(String)Phrases.Phrases.get("0")+Colon + today+"</title>"+output);
       	}
        if(name.equals(FileNames[4]))
        {
        	if(helper.closeFrame(MainNames[6]))
        	{
        		System.exit(0);
        	}
        }
        if (name.equals(ServiceNames[1]))
        {
        	//DIVINE LITURGY
        }
        if (name.equals(ServiceNames[2]))
        {
        	//VESPERS
        }
        if (name.equals(ServiceNames[3]))
        {
        	//COMPLINE
        }
        if (name.equals(ServiceNames[4]))
        {
        	//MATINS
        }
        if (name.equals(ServiceNames[5]))
        {
        	//Create the primes service
        	new Primes(today);
        }
        if (name.equals(ServiceNames[6]))
        {
        	new ThirdHour(today);
        }
        if (name.equals(ServiceNames[7]))
        {
            new SixthHour(today);
            //SEXT
        }
        if (name.equals(ServiceNames[8]))
        {
            new NinthHour(today);
            //NONE
        }
        if (name.equals(ServiceNames[9]))
        {
        	//ROYAL HOURS
            new RoyalHours(today);
        }
        if (name.equals(ServiceNames[10]))
        {
        	//ALL-NIGHT VIGIL
        }
        if (name.equals(ServiceNames[11]))
        {
        	//MIDNIGHT OFFICE
        }
        if (name.equals(ServiceNames[12]))
        {
        	//TYPICA
        }
          if (name.equals(BibleName[0]))
        {
        	//Launch the Bible Reader
               Helpers orient=new Helpers();
        orient.applyOrientation(new Bible("Gen","1:1-31"),(ComponentOrientation)StringOp.dayInfo.get("Orient"));
        }
        if(name.equals(FileNames[6]))
        {
        	helper.sendHTMLToPrinter(text);
        }
         
        String s = "Action event detected."
                   + newline
                   + "    Event source: " + source.getText()
                   + " (an instance of " + getClassName(source) + ")";
        System.out.println(s);
        //output.append(s + newline);
        //output.setCaretPosition(output.getDocument().getLength());
    }
	
		
	
	public void startDocument() { }

	public void endDocument() { }

	public void startElement(String elem, Hashtable table)
	{
		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo. 
		
		if (table.get("Cmd") != null)
		{
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
			if (StringOp.evalbool(table.get("Cmd").toString()) == false) 
			{
				return;
			}
		}
		//if(elem.equals("LANGUAGE"))
		//{
			read=true;
		//}
		if (elem.equals("SAINT") && read == true)
		{
			try
			{
				int floatnum = Integer.parseInt((String)table.get("Name"));
				// Floater: READ FROM AN XML FILE FOR THE PARTICULAR FLOATER
				try
				{
					BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(findLanguage.langFileFind(StringOp.dayInfo.get("LS").toString(),"xml/float/" + floatnum + ".xml")), "UTF8"));
					read=false;
					QDParser.parse(this, frf);
					read=true;
				}
				catch(Exception e)
				{
					System.out.println(Errors[1] + floatnum);
					read=true;
				}
				finally
				{
					//COPY THE READINGS FROM THE FLOATER TO THE UNIQUE IDENTIFIER (2008/05/24 n.s. Y.S.)
					read=true;
					for (Enumeration e = readings.enumerateKeys(); e.hasMoreElements(); )
					{
						String type = (String)e.nextElement();
						
						if(ReadScriptures[2].containsKey(type))
						{
							String vect = readings.get(type).toString();
							Vector vect2 = (Vector)ReadScriptures[2].get(type);
							vect2.add(vect.substring(1,vect.length()-1).trim());
							ReadScriptures[2].put(type, vect2);
						}
						else
						{
							Vector vect = (Vector)readings.get(type);
							ReadScriptures[2].put(type,vect);
						}
					}
					readings.clear();
					return;
					
				}
			}
			catch (NumberFormatException nfe)
			{
				// Not a floater, just continue (bad use of exceptions on my part)
			}

			String id = (String)table.get("Id");
			if (id.length() != 0)
			{
				output += "<A Href='goDoSaint?id=" + table.get("Id") + "'>";
                                //if there is an id, then check if an Icon exists
                                //System.out.println("Ponomar/images/icons/"+id+".jpg");
                               File file=new File("Ponomar/images/icons/"+id+".jpg");
                               if (file.exists())
                               {
                                   IconImages.add(id);
                                   IconNames.add(table.get("Name").toString());
                               }
			}
                        //Adding a programme to automatically determine the rank of a given feast.
                        //It is assumed that the highest Type == Rank found in all the files that are being parsed is equal 
                        //to the Rank for the day.
                        //Y.S. 2010/02/01 n.s.
                        int Rank=0;
                        if (table.get("Type") != null){
                            //System.out.println("A!"+table.get("Type").toString()+"!B");
                            if (table.get("Type").toString().length() > 0){
                         Rank = Integer.parseInt((String)table.get("Type"));
                        StringOp.dayInfo.put("dRank",Math.max(Integer.parseInt(StringOp.dayInfo.get("dRank").toString()),Rank));
                            }
                        }
                        //System.out.println(StringOp.dayInfo.get("dRank")+"\nFound Rank: "+Rank );

			switch (Rank)
			{
				case 8:
                            case 7:
                            case 6:
					//output += "<FONT Color='red'><Font face='Hirmos Ponomar' size='+1'>\uA698</Font><B>\u00A0" + table.get("Name") + "</B></FONT>";//A698
                                	output += "<FONT Color='red'><Font face='Hirmos Ponomar' size='+1'>\uD83D\uDD40</Font><B>\u00A0" + table.get("Name") + "</B></FONT>";
                                //output += "</body><body style=\"font-family:Hirmos Ponomar;font-size:"+Integer.parseInt(DisplaySize)+2+"pt;color:red\">\uA698</body><body style=\"font-family:"+DisplayFont+";font-size:"+DisplaySize+"pt;color:red;font-style:bold\">\u00A0" + table.get("Name") + "</body><body style=\"font-family:"+DisplayFont+";font-size:"+DisplaySize+"pt\">";
                               //output += "<style style=\"font-family:Hirmos Ponomar;font-size:"+Integer.parseInt(DisplaySize)+2+"pt;color:red\">\uA698</style>\u00A0<style style=\"color:red\">" + table.get("Name") + "</style>";
                               //output+="<B><rank style=\"font-face:Hirmos Ponomar;size=18;color:red\">\uA698</rank><B>\u00A0"+table.get("Name");
                                //output += "<div style=\"font-face:Hirmos Ponomar; font-size:18pt; color:red\">\uA698\u00A0</div><Font color='red'><B>" + table.get("Name") + "</B></Font>";
					break;
				case 5:
                                    output += "<FONT Color='red'><Font face='Hirmos Ponomar' size='+1'>\uD83D\uDD41</Font>\u00A0" + table.get("Name") + "</FONT>";
					break;
                            case 4:
				output += "<Font Color='red' face='Hirmos Ponomar' size='+1'>\uD83D\uDD42</Font><B>\u00A0" + table.get("Name") + "</B>";
					break;

				case 3:
					output += "<Font Color='red' face='Hirmos Ponomar' size='+1'>\uD83D\uDD43</Font><I>\u00A0" + table.get("Name") + "</I>";
					break;
                            case 2:
                                output += "<Font face='Hirmos Ponomar' size='+1'>\uD83D\uDD43</Font><I>\u00A0" + table.get("Name") + "</I>";
					break;
				default:
					output += table.get("Name");
                                        //Note: \u00A0 is a nonbreaking space.
			}

			output += id.length() != 0 ? "</A>"+CSep:CSep;


			if (table.get("Tone") != null) {
				int tone = (int)Math.floor(StringOp.eval((String)table.get("Tone")));

				if(tone==0)
				{
					tone=8;
				}
                                StringOp.dayInfo.put("Tone",tone);
				//output += tone != -1 ? MainNames[4] +": " + toneNumbers[tone] + CSep+" " : "";
                                if (tone!=-1)
                                {
                                String ToneFormat = new String();
                                ToneFormat=MainNames[4];
                                ToneFormat=ToneFormat.replace("TT",toneNumbers[tone]);
                                output+=ToneFormat;
                                }
				
			}
		}
		else if (elem.equals("SCRIPTURE") && read == true)
		{
			String type = (String)table.get("Type");
			String reading = (String)table.get("Reading");
			if (readings.containsKey(type))
			{
				
				// ADD THIS READING TO OTHERS OF THE SAME TYPE
				Vector vect = (Vector)readings.get(type);
				vect.add(reading);
				readings.put(type, vect);
			}
			else
			{
				// CREATE A NEW TYPE WITH A COLLECTION INCLUDING THIS READING
				Vector vect = new Vector();
				vect.add(reading);
				readings.put(type, vect);
			} 
		}
		else if (elem.equals("FAST") && read == true)
		{
			fastInfo.push(Integer.parseInt(table.get("Num").toString()));
		}
	}

	public void endElement(String elem) 
	{
		if(elem.equals("LANGUAGE"))
		{
			read=false;
		}
	
	 }

	public void text(String text) { }
	
	public void hyperlinkUpdate(HyperlinkEvent e)
	{
            if (e.getEventType().toString() == "ACTIVATED")
		{
			String cmd = e.getDescription();
			String[] parts = cmd.split("#");
			if (parts[0].indexOf("reading") != -1)
			{
				try
				{
					bible.update(parts[1], parts[2]);
					bible.show();
				} catch (NullPointerException npe) {
					bible = new Bible(parts[1], parts[2]);
                                        Helpers orient=new Helpers();
                orient.applyOrientation(bible,(ComponentOrientation)StringOp.dayInfo.get("Orient"));
				}
			}
                        else
                        {
                            parts = cmd.split("\\?");
                            if (parts[0].indexOf("goDoSaint") != -1)
                            {
                                
                                String[] parts2=parts[1].split("=");
                                System.out.println(parts2[1]);
                                if (SaintLink == null){
                                    SaintLink=new DoSaint(parts2[1]);
                                }
                                else
                                {
                                    SaintLink.refresh(parts2[1]);
                                }
                                
                            }
                        }
		}
	}

	// RETURNS THE "CONVOLUTION" OF TWO INTEGERS
	// THIS ISN'T ACTUALLY A "CONVOLUTION" IN THE SENSE OF FUNCTIONAL ANALYSIS; I JUST CAN'T FIND A BETTER TERM FOR THIS OPERATION
	// WHAT WE'RE DOING HERE IS A SORT OF STRANGE WAY OF MULTIPLYING NUMBERS (WITHOUT OPERATOR OVERLOADING)
	private int convolve(int m, int n)
	{
		return (m > 0) && (n > 0) ? -1 * m * n : m * n;
		/** (more detailed explanation: zero convolve anything is zero - i.e. when at least one input is fast-free, the whole day is fast-free
			a negative convolve a negative is a positive - i.e. when both inputs are not fast requirements, the whole day is fast-free
			a positive convolve a positive is a negative - i.e. when both inputs are fast days, the whole days is a fast day
			a positive convolve a negative is a negative - i.e. when one input is a fast day, the whole day is a fast day **/
	}

	private void write()
	{
		//if(!DisplayFont.equals(""))
		{
			//output="<FONT face=\""+DisplayFont+"\" size=\"5\">";//+DisplaySize+"pt\">";
			//text.setText("<B>" + (String)books.get(curbook) + " " + formatPassage(curpassage) + "</B><BR>" +Stuff[0]);
			//text.setFont(CurrentFont);
                    output="<body style=\"font-family:"+DisplayFont+";font-size:"+DisplaySize+"pt\">";
		}
                //else
                //{
                  //  output="";
                //}
                
                output += "<B>" + today.toString() + "</B><BR>";
		output +=MainNames[0] +Colon + (String)today.getGregorianDateS() + "<BR>";
		String filename = "";
		int lineNumber = 0;
		int dow = today.getDayOfWeek();
		int doy = today.getDoy();
		int nday = (int)JDate.difference(today, this.pascha);
		int ndayP = (int)JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
		//REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
		int ndayF = (int)JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));

                //Clearing the holders for the icons and names
                IconImages=new Vector();
                IconNames= new Vector();

		// PUT THE RELEVANT DATA IN THE HASH
		StringOp.dayInfo.put("dow", dow);	// THE DAY'S DAY OF WEEK
		StringOp.dayInfo.put("doy", doy);	// THE DAY'S DOY (see JDate.java for specification)
		//System.out.println(doy);
		StringOp.dayInfo.put("nday", nday);	// THE NUMBER OF DAYS BEFORE (-) OR AFTER (+) THIS YEAR'S PASCHA
		StringOp.dayInfo.put("ndayP", ndayP);	// THE NUMBER OF DAYS AFTER LAST YEAR'S PASCHA
		//REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
		StringOp.dayInfo.put("ndayF", ndayF);	// THE NUMBER OF DAYS TO NEXT YEAR'S PASCHA (CAN BE +ve or -ve).
		//ADDING THE TYPE OF GOSPEL READINGS TO BE FOLLOWED
		StringOp.dayInfo.put("GS",GospelSelector.getGValue());
		//INTERFACE LANGUAGE
		StringOp.dayInfo.put("LS",LanguageLocation.getLValue());
		StringOp.dayInfo.put("Year",today.getYear());
                StringOp.dayInfo.put("dRank",0); //The default rank for a day is 0. Y.S. 2010/02/01 n.s.
                StringOp.dayInfo.put("Ideographic",Ideographic);
			
		readings = new OrderedHashtable();
		fastInfo = new Stack();
		//MY ATTEMPT AT SORTING THE READINGS FOR THE LITURGY 2008/05/24 n.s. YURI SHARDT
		ReadScriptures = new OrderedHashtable[3];		//CONTAINS A SORTED ARRAY OF ALL THE READINGS
		ReadScriptures[0] = new OrderedHashtable();		//STORES THE PENTECOSTARION READINGS (SEQUENTIAL (rjadovoje) READINGS!)
		ReadScriptures[1] = new OrderedHashtable();		//CONTAINS THE MENALOGION READINGS, EXCLUDING ANY FLOATERS
		ReadScriptures[2] = new OrderedHashtable();		//CONTAINS THE FLOATER READINGS.
		
		//TESTING THE LANGUAGE PACKS
		String rough=(String)Phrases.Phrases.get("1");
		String[] final1=rough.split(",");
		//System.out.println(output);
		


		// GET THE DAY'S ASTRONOMICAL DATA
		String[] sunriseSunset = Sunrise.getSunriseSunsetString(today, (String)ConfigurationFiles.Defaults.get("Longitude"), (String)ConfigurationFiles.Defaults.get("Latitude"), (String)ConfigurationFiles.Defaults.get("TimeZone"));
		output += "<BR><B>"+MainNames[1]+"</B>"+Colon + sunriseSunset[0];
		output += "<BR><B>"+MainNames[2]+"</B>"+Colon + sunriseSunset[1];
		output += "<BR><BR>"; //<B>"+MainNames[3]+"</B>"+Colon+ Paschalion.getLunarPhaseString(today) +"<BR><BR>";
		// getting rid of the lunar phase until we program a paschalion ...
		if (nday >= -70 && nday < 0)
		{
			filename = triodionFileName;
			lineNumber = Math.abs(nday);
		}
		else if (nday < -70)
		{
			// WE HAVE NOT YET REACHED THE LENTEN TRIODION
			filename = pentecostarionFileName;
			JDate lastPascha = Paschalion.getPascha(today.getYear() - 1);
			lineNumber = (int)JDate.difference(today, lastPascha) + 1;
		}
		else
		{
			// WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
			filename = pentecostarionFileName;
			lineNumber = nday + 1;
		}

		filename += lineNumber >= 10 ? lineNumber + ".xml" : "0" + lineNumber + ".xml"; // CLEANED UP
		// READ THE PENTECOSTARION / TRIODION INFORMATION
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(findLanguage.langFileFind(StringOp.dayInfo.get("LS").toString(),filename)), "UTF8"));
			//FileReader frf = new FileReader(filename);
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//ADDED 2008/05/19 n.s. Y.S.
		//COPYING SOME READINGS FILES
		
		
		for (Enumeration e = readings.enumerateKeys(); e.hasMoreElements(); )
		{
			String type = (String)e.nextElement();
			Vector vect = (Vector)readings.get(type);
			
			ReadScriptures[0].put(type, vect);
		}
		
		
		readings.clear();
		
		// GET THE MENAION DATA, THESE MAY BE INDEPENDENT OF THE GOSPEL READING IMPLEMENTATION, BUT WILL NOT BE SO IMPLEMENTED
		int m = today.getMonth();
		int d = today.getDay();

		filename = "";
		filename += m < 10 ? "xml/0" + m : "xml/" + m;  // CLEANED UP
		filename += d < 10 ? "/0" + d : "/" + d; // CLEANED UP
		filename += ".xml";

		// PARSE THE MENAION XML FILE
		try
		{
			BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(findLanguage.langFileFind(StringOp.dayInfo.get("LS").toString(),filename)), "UTF8"));
			//FileReader fr = new FileReader(filename);
			QDParser.parse(this, fr);
		}
		catch (Exception e)
		{
			System.out.println(Errors[2] + " " + today.toString() + Colon + e.toString());
		}
		//ADDED 2008/05/19 n.s. Y.S.
		for (Enumeration e = readings.enumerateKeys(); e.hasMoreElements(); )
		{
			String type = (String)e.nextElement();
			Vector vect = (Vector)readings.get(type);
			
			ReadScriptures[1].put(type, vect);
		}
		
			
		String collection="";
		output += "<BR><BR>";
		// OUTPUT THE SCRIPTURE READINGS BY PARSING THE SCRIPTURE HASHTABLE
		//MODIFIED BY Y.S. 2008/05/25 n.s. TO ACCOUNT FOR CHANGES IN THE SAVING OF THE READINGS!
		//THIS IS A COMPACT AND QUICK METHOD TO DO THIS THAT DOES NOT ASSUME THE EXISTENCE OF ANY GIVEN READING.
		//OFFICIALLY, EACH READING WOULD HAVE ITS OWN VERSION THAT DETERMINES THE APPROPRAITE READINGS FOR THE DAY,
		//THIS HAS ALREADY BEEN IMPLEMENTED FOR THE DIVINE LITURGY, THAT IS, THE EPISTLE AND GOSPEL READINGS.
		//OTHER PROGRAMMES MAY SOON FOLLOW.
		//MADE FURTHER CORRECTIONS AND IMPROVEMENTS 2008/12/11 n.s. 
		
		Vector readingType = new Vector();
		for(int i=0;i <=2;i++)
		{
			//WE NEED TO DETERMINE WHICH READINGS ARE PRESENT TODAY
			for(Enumeration e = ReadScriptures[i].enumerateKeys(); e.hasMoreElements();)
			{
				String type = (String)e.nextElement();
				boolean present = false;
				for(Enumeration e4 = readingType.elements();e4.hasMoreElements();)
				{
					String value =(String) e4.nextElement();
					if(value.equals(type))
					{
						present = true;
						break;			//ONCE THE ELEMENT IS FOUND WHY BOTHER SEARCHING LONGER FOR IT
					}
				}
				if(!present)
				{
					//IF WE HAVE NOT YET ADDED THIS TYPE OF READING TO THE VECTOR CONTAINING THE TYPES OF READINGS FOR TODAY, ADD IT
					readingType.add(type);					
				}
			}
		}
		
			
			
		for(int i=0;i<readingType.size();i++)
		{
			String type=(String)readingType.get(i);
			String type1 =(String) Phrases.Phrases.get(type);			//(String)e.nextElement();
			output += "<b>"+ type1 + Colon+"</b>";
			Bible ShortForm=new Bible();
			if(type.equals("gospel") || type.equals("apostol"))
			{
				//FOR THESE 2 TYPES OF READINGS WE HAVE A SPECIAL READING PROCEDURE
				output += DivineLiturgy.Readings((Vector)  ReadScriptures[0].get(type),(Vector)  ReadScriptures[1].get(type), (Vector)  ReadScriptures[2].get(type),type,today);
				if (i<readingType.size()-1)
				{
					output += RSep;
				}
			}
			else
			{
				boolean Type1Flag=false;
				boolean Type2Flag=false;
				
				if(ReadScriptures[0].containsKey(type))
				{
			
					Vector vect = (Vector) ReadScriptures[0].get(type);
					Type1Flag=true;
				
					for (Enumeration e2=vect.elements();e2.hasMoreElements();)
					{
						String reading = (String) e2.nextElement();
						output+=ShortForm.getHyperlink(reading);
						if (e2.hasMoreElements())
						{
							output += RSep;
						}						
					}
					
				}
				if(ReadScriptures[2].containsKey(type))
				{
					if(Type1Flag)
					{
						output += RSep;
					}
					Vector vect = (Vector) ReadScriptures[2].get(type);
					Type1Flag=true;					;
					for (Enumeration e2=vect.elements();e2.hasMoreElements();)
					{
						String reading = (String) e2.nextElement();
						output+=ShortForm.getHyperlink(reading);
						if (e2.hasMoreElements())
						{
							output += RSep;
						}
					}
					
				}
				if(ReadScriptures[1].containsKey(type))
				{
					if(Type1Flag || Type2Flag)
					{
						output += RSep;
					}
					Vector vect = (Vector) ReadScriptures[1].get(type);
					for (Enumeration e2=vect.elements();e2.hasMoreElements();)
					{
						String reading = (String) e2.nextElement();
						output+=ShortForm.getHyperlink(reading);
						if (e2.hasMoreElements())
						{
							output += RSep;
						}
					}
					
				}
                                output += RSep;
			}
			
			
		}
                
                String[] iconImages=new String[IconImages.size()];
                String[] iconNames=new String[IconImages.size()];
                iconImages=(String [])IconImages.toArray(new String[IconImages.size()]);
                iconNames=(String [])IconNames.toArray(new String[IconImages.size()]);
                //System.out.println(iconNames.length);
                //System.out.println(iconImages);
                //if (iconNames.length>0)
                {
                    //System.out.println(iconImages[0]+"   "+iconNames[0]+"   ");
                    displayIcon.updateImages(iconImages,iconNames);
                    
                }
                //this.pack();
		// OUTPUT THE FASTING REGULATIONS FOR THIS Day
		/*int f = Integer.parseInt(fastInfo.pop().toString());
		for (Enumeration e = fastInfo.elements(); e.hasMoreElements(); )
		{
			f = convolve(Integer.parseInt(fastInfo.pop().toString()), f);
		}
		String[] FastNames= Phrases.obtainValues((String)Phrases.Phrases.get("Fasts"));
		
		String fast = f >= 0  ? FastNames[0] :
			      f == -1 ? FastNames[1] :
			      f == -2 ? FastNames[2] :
			      f <= -3 ? FastNames[3] : FastNames[5];
		output += "<BR><BR>"+FastNames[4]+" " + fast+"<BR><BR>";*/
		//THIS IS NOW REPLACED BY THE NEW PROGRAMME, THAT SIMPLIFIES THE DETERMINATION OF THE FAST.
		String[] FastNames= Phrases.obtainValues((String)Phrases.Phrases.get("Fasts"));
		Fasting getfast=new Fasting();
		output+="<BR><BR>"+ getfast.FastRules() +"<BR><BR>";
                //output+="</FONT>";
                output+="</body>";
                
		text.setContentType("text/html; charset=UTF-8");
                text.setFont(CurrentFont);
                //text.applyComponentOrientation(ComponentOrientation.getOrientation(new Locale("zh","HK")));
                //System.out.println("Hello Tests");
                //System.out.println(ComponentOrientation.getOrientation(new Locale("ar","CN")).isHorizontal());
                //System.out.println(ComponentOrientation.getOrientation(new Locale("ar","CN")).isLeftToRight());
                //System.out.println(ComponentOrientation.getOrientation(new Locale("zh","CN")).isHorizontal());
                //System.out.println(ComponentOrientation.getOrientation(new Locale("zh_Hant","")).isHorizontal());
		text.setText(output);
		text.setCaretPosition(0);       
		
		
	}
	 protected String getClassName(Object o)
    	{
        	String classString = o.getClass().getName();
        	int dotIndex = classString.lastIndexOf(".");
        	return classString.substring(dotIndex+1);
    	}

	public static void main(String[] argz)
	{
		new Main();
	}
}
