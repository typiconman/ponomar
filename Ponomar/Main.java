package Ponomar;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/***********************************************************************
 Main.java :: MAIN MODULE FOR THE PONOMAR PROGRAM.
 THIS MODULE CONSTITUTES THE PRIMARY PONOMAR GUI AND CENTRE OF THE PROGRAM.
 TO START THE PROGRAM, INVOKE main(String[]) OF THIS CLASS.
 OUTPUTS RELEVANT INFORMATION FOR EACH DAY, WITH LINKS TO DETAILED INFO.

 Main.java is part of the Ponomar program.
 Copyright 2006, 2007, 2008 Aleksandr Andreev and Yuri Shardt.
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
	private final static String triodionFileName   = "Ponomar/xml/triodion/";   // TRIODION FILE
	private final static String pentecostarionFileName = "Ponomar/xml/pentecostarion/"; // PENTECOSTARION FILE
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
	
	//private GospelSelector Selector;

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
		toneNumbers= Phrases.obtainValues((String)Phrases.Phrases.get("Tones"));
		SaintNames=Phrases.obtainValues((String)Phrases.Phrases.get("SMenu"));
		OptionsNames=(String)Phrases.Phrases.get("Options");
		FileNames=Phrases.obtainValues((String)Phrases.Phrases.get("File")); 
		ServiceNames=Phrases.obtainValues((String)Phrases.Phrases.get("Services"));
		BibleName=Phrases.obtainValues((String)Phrases.Phrases.get("Bible"));
		HelpNames=Phrases.obtainValues((String)Phrases.Phrases.get("Help"));
		setTitle((String)Phrases.Phrases.get("0"));
		Errors=Phrases.obtainValues((String)Phrases.Phrases.get("Errors"));
		MainNames=Phrases.obtainValues((String)Phrases.Phrases.get("Main"));
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
		
		setJMenuBar(MenuBar);
			   
	        JPanel left = new JPanel(new GridLayout(3,0));
		calendar = new JCalendar();
		calendar.addPropertyChangeListener(this);
		left.setLayout(new BorderLayout());
		left.add(calendar, BorderLayout.NORTH);
			
		
									
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
		pack();
		setSize(700, 500);
		setVisible(true);
		
		pascha = Paschalion.getPascha(today.getYear());
		pentecost = Paschalion.getPentecost(today.getYear());
		
		
		inited = true;
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
				StringOp.dayInfo.clear();
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
        	 new About();
        }
        if (name.equals(HelpNames[0]))
        {
        	 //HELP FILES
        }
        if(name.equals(FileNames[1]))
        {
        	//SAVE THE CURRENT WINDOW
       		helper.SaveHTMLFile(MainNames[5]+ " "+today+".html", "<title>"+(String)Phrases.Phrases.get("0")+" : " + today+"</title>"+output);
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
        	//TERCE
        }
        if (name.equals(ServiceNames[7]))
        {
        	//SEXT
        }
        if (name.equals(ServiceNames[8]))
        {
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
        	new Bible("Gen","1:1-31");
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
		if(elem.equals("LANGUAGE"))
		{
			read=true;
		}
		if (elem.equals("SAINT") && read == true)
		{
			try
			{
				int floatnum = Integer.parseInt((String)table.get("Name"));
				// Floater: READ FROM AN XML FILE FOR THE PARTICULAR FLOATER
				try
				{
					BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream("Ponomar/xml/float/" + floatnum + ".xml"), "UTF8"));
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
			}

			switch (Integer.parseInt((String)table.get("Type")))
			{
				case 3:
					output += "<I>" + table.get("Name") + "</I>";
					break;
				case 2: 
					output += "<B><FONT Color='red'>" + table.get("Name") + "</FONT></B>";
					break;
				case 1:
					output += "<B>" + table.get("Name") + "</B>";
					break;
				default:
					output += table.get("Name");
			}

			output += id.length() != 0 ? "</A>; " : "; ";


			if (table.get("Tone") != null) {
				int tone = (int)Math.floor(StringOp.eval((String)table.get("Tone")));
				if(tone==0)
				{
					tone=8;
				}
				output += tone != -1 ? MainNames[4] +": " + toneNumbers[tone] + "; " : "";
				StringOp.dayInfo.put("Tone",tone);
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
		output = "<B>" + today.toString() + "</B><BR>";
		output +=MainNames[0] +": " + (String)today.getGregorianDateS() + "<BR>";
		String filename = "";
		int lineNumber = 0;
		int dow = today.getDayOfWeek();
		int doy = today.getDoy();
		int nday = (int)JDate.difference(today, this.pascha);
		int ndayP = (int)JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
		//REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
		int ndayF = (int)JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));
		

		// PUT THE RELEVANT DATA IN THE HASH
		StringOp.dayInfo.put("dow", dow);	// THE DAY'S DAY OF WEEK
		StringOp.dayInfo.put("doy", doy);	// THE DAY'S DOY (see JDate.java for specification)
		System.out.println(doy);
		StringOp.dayInfo.put("nday", nday);	// THE NUMBER OF DAYS BEFORE (-) OR AFTER (+) THIS YEAR'S PASCHA
		StringOp.dayInfo.put("ndayP", ndayP);	// THE NUMBER OF DAYS AFTER LAST YEAR'S PASCHA
		//REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
		StringOp.dayInfo.put("ndayF", ndayF);	// THE NUMBER OF DAYS TO NEXT YEAR'S PASCHA (CAN BE +ve or -ve).
		//ADDING THE TYPE OF GOSPEL READINGS TO BE FOLLOWED
		StringOp.dayInfo.put("GS",GospelSelector.getGValue());
		//INTERFACE LANGUAGE
		StringOp.dayInfo.put("LS",LanguageLocation.getLValue());
		StringOp.dayInfo.put("Year",today.getYear());
			
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
		
		
		// GET THE DAY'S ASTRONOMICAL DATA
		String[] sunriseSunset = Sunrise.getSunriseSunsetString(today, (String)ConfigurationFiles.Defaults.get("Longitude"), (String)ConfigurationFiles.Defaults.get("Latitude"), (String)ConfigurationFiles.Defaults.get("TimeZone"));
		output += "<BR><B>"+MainNames[1]+"</B>: " + sunriseSunset[0];
		output += "<BR><B>"+MainNames[2]+"</B>: " + sunriseSunset[1];
		output += "<BR><BR>"; //<B>"+MainNames[3]+"</B>: " + Paschalion.getLunarPhaseString(today) +"<BR><BR>";
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
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
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
		filename += m < 10 ? "Ponomar/xml/0" + m : "Ponomar/xml/" + m;  // CLEANED UP
		filename += d < 10 ? "/0" + d : "/" + d; // CLEANED UP
		filename += ".xml";

		// PARSE THE MENAION XML FILE
		try
		{
			BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
			//FileReader fr = new FileReader(filename);
			QDParser.parse(this, fr);
		}
		catch (Exception e)
		{
			System.out.println(Errors[2] + " " + today.toString() + ": " + e.toString());
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
			output += "<b>" + type1 + ":</b> ";
			Bible ShortForm=new Bible();
			if(type.equals("gospel") || type.equals("apostol"))
			{
				//FOR THESE 2 TYPES OF READINGS WE HAVE A SPECIAL READING PROCEDURE
				output += DivineLiturgy.Readings((Vector)  ReadScriptures[0].get(type),(Vector)  ReadScriptures[1].get(type), (Vector)  ReadScriptures[2].get(type),type,today);
				if (i<readingType.size()-1)
				{
					output += ";";
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
							output += "; ";
						}						
					}
					
				}
				if(ReadScriptures[2].containsKey(type))
				{
					if(Type1Flag)
					{
						output += "; ";
					}
					Vector vect = (Vector) ReadScriptures[2].get(type);
					Type1Flag=true;					;
					for (Enumeration e2=vect.elements();e2.hasMoreElements();)
					{
						String reading = (String) e2.nextElement();
						output+=ShortForm.getHyperlink(reading);
						if (e2.hasMoreElements())
						{
							output += "; ";
						}
					}
					
				}
				if(ReadScriptures[1].containsKey(type))
				{
					if(Type1Flag || Type2Flag)
					{
						output += "; ";
					}
					Vector vect = (Vector) ReadScriptures[1].get(type);
					for (Enumeration e2=vect.elements();e2.hasMoreElements();)
					{
						String reading = (String) e2.nextElement();
						output+=ShortForm.getHyperlink(reading);
						if (e2.hasMoreElements())
						{
							output += "; ";
						}
					}
					
				}							
			}
			output += " ";
			
		}
		
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

		text.setContentType("text/html; charset=UTF-8");
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
