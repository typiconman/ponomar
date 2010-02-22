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
public class ThirdHour implements DocHandler, ActionListener, ItemListener, PropertyChangeListener
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	//THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	//TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	//DURING THE COURSE OF A SINGLE WEEK.
	private final static String configFileName = "ponomar.config";   //CONFIGURATIONS FILE
	private final static String octoecheosFileName   = "Ponomar/xml/Services/Octoecheos/";   // THE LOCATION OF THE BASIC SERVICE RULES
	private final static String ServicesFileName = "Ponomar/xml/Services/"; // THE LOCATION FOR ANY EXTRA INFORMATION
	private static OrderedHashtable PrimesTK;
	private static String FileNameIn="Ponomar/xml/Services/PRIMES1/";
	private static String FileNameOut=FileNameIn+"Primes.html";
	private static String text;
	private static boolean read=false;
	private static String Type;
	private String Troparion1;
	private String Kontakion1;
	private String Kontakion2;
	private String Troparion2;
	private final static String triodionFileName   = "Ponomar/xml/triodion/";   // TRIODION FILE
	private final static String pentecostarionFileName = "Ponomar/xml/pentecostarion/"; // PENTECOSTARION FILE
	private String filename;
	private int lineNumber;
	private LanguagePack Text=new LanguagePack();
	private String[] PrimesNames=Text.obtainValues((String)Text.Phrases.get("Terce"));
	private String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private String LentenK;				//ANY REQUIRED KATHISMA REFERENCED USING "LENTENK = "17"" WOULD BE THE 17th KATHISMA.
	private JFrame frames;
	private String[] FileNames=Text.obtainValues((String)Text.Phrases.get("File"));
	private String[] HelpNames=Text.obtainValues((String)Text.Phrases.get("Help")); 
	String newline = "\n";
	private String strOut;
	private JDate today;
	private Helpers helper;
	private PrimeSelector SelectorP=new PrimeSelector();
	private PrintableTextPane output;
        private String DisplayFont =new String(); //ALLOWS A CUSTOM FONT AND SIZE TO BE SPECIFIED FOR A GIVEN BIBLE READING: REQUIRED FOR OLD CHURCH SLAVONIC AT PRESENT
	private String DisplaySize="12";  //UNTIL A COMPLETE UNICODE FONT IS AVAILIBLE.
	private Font DefaultFont=new Font("",Font.BOLD,12);		//CREATE THE DEFAULT FONT
	private Font CurrentFont=DefaultFont;
	
	
	public ThirdHour(int Weekday)
	{
		helper=new Helpers();
		StringOp.dayInfo = new Hashtable();
		StringOp.dayInfo.put("dow", Weekday);		//DETERMINE THE DAY OF THE WEEK.
		
		try
		{
			FileReader frf = new FileReader(ServicesFileName + "TrialPrimes.xml");
			//Primes Primes1 = new Primes();
			QDParser.parse(this, frf);

		}
		catch (Exception Primes)
		{
			Primes.printStackTrace();
			return; 			//IF THERE ARE NO FILES FOR PRIMES THEN QUIT THE PROGRAMME.
		}

		//THE TONE/WEEKDAY FILES HAVE BEEN PARSED
		//NOW PARSE THE EASTER CYCLE FILE TO CHECK FOR ANY ISSUES ABOUT ADDITIONS (BETTER VERSION MAY BE OBTAINED LATER)
		//FINALLY PARSE THE MENOLOGION FILE

		//CREATE THE SERVICE
		try
		{
			createPrimes();
			BufferedReader in = new BufferedReader(new FileReader(FileNameOut));
			strOut = new String();
			String str = new String();
			strOut="<Font Size=\"12\">This is a preliminary attempt at creating the Third Hour service.</Font>\n\n";
			while ((str = in.readLine()) != null)
        		{
            			strOut += str;
        		}

                        PrimesWindow(strOut);
		}
		catch (IOException j)
		{
		}
	}	
	public ThirdHour(JDate date)
	{
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
		
		//StringOp.dayInfo = new Hashtable();
		//StringOp.dayInfo.put("dow", Weekday);		//DETERMINE THE DAY OF THE WEEK.
		
		
		//CREATING THE SERVICE	
		today=date;
		helper=new Helpers();
		try
		{
			String strOut=createPrimes();
			if(strOut.equals("No Service Today"))
			{
				Object[] options = {LanguageNames[3]};
				JOptionPane.showOptionDialog(null, PrimesNames[0],(String)Text.Phrases.get("0") + ": "+ PrimesNames[1], JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			}
			else
			{
				//strOut=strOut+"<p><Font Color='red'>Disclaimer: This is a preliminary attempt at creating the Primes service.</Font></p>";
				int LangCode=Integer.parseInt(StringOp.dayInfo.get("LS").toString());
                                if (LangCode==2 || LangCode==3 ){
                                    //strOut="<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><p><font face=\"Hirmos Ponomar\" size=\"5\">"+strOut+"</font></p>";
                                    //System.out.println("Added Font");
                                   }

                                PrimesWindow(strOut);
			}
		}
		catch (IOException j)
		{
		}
		
		 	
	}
	private void PrimesWindow(String textOut)
	{
		frames=new JFrame((String)Text.Phrases.get("0") + ": "+ PrimesNames[1]);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		textOut=textOut.replaceAll("</br>", "<BR>");
		textOut=textOut.replaceAll("<br>","<BR>");
		strOut=textOut;
		//System.out.println(textOut);
		JPanel contentPane=new JPanel(new BorderLayout());
		contentPane.setOpaque(true);
		output=new PrintableTextPane();
		output.setEditable(false);
		output.setSize(800,700);
		output.setContentType("text/html; charset=UTF-8");
		output.setText(textOut);
		output.setCaretPosition(0);
                JScrollPane scrollPane = new JScrollPane(output);
		JMenuBar MenuBar=new JMenuBar();
		MenuFiles demo=new MenuFiles();
		PrimeSelector trial=new PrimeSelector();
		MenuBar.add(demo.createFileMenu(this));
		MenuBar.add(trial.createPrimeMenu());
		MenuBar.add(demo.createHelpMenu(this));
		frames.setJMenuBar(MenuBar);
		trial.addPropertyChangeListener(this);
		
		contentPane.add(scrollPane,BorderLayout.CENTER);
		frames.setContentPane(contentPane);
		frames.pack();
		frames.setSize(800,700);
		frames.setVisible(true);
                
                

		//scrollPane.top();
	}
	private String createPrimes() throws IOException
	{
		//OBTAIN THE DEFAULTS FOR THE SERVICE (WHAT WAS LAST USED!)
		StringOp.dayInfo.put("PS",SelectorP.getWhoValue());
		int TypeP=SelectorP.getTypeValue();
		Service ReadPrime=new Service();
		//FIRST READ THE TONE FILES:
				int Weekday=Integer.parseInt(StringOp.dayInfo.get("dow").toString());
				//System.out.println(Weekday);
				int Tone=Integer.parseInt(StringOp.dayInfo.get("Tone").toString());
				if(Tone==8)
				{
					Tone=0;
				}
				//System.out.println(Tone);
				if(Tone != -1)
				{
				String FileName=octoecheosFileName + "Tone " +Tone;
				if (Weekday==1)
				{
					FileName=FileName+"/Monday.xml";
				}
				else if(Weekday==2)
				{
					FileName=FileName+"/Tuesday.xml";
				}
				else if(Weekday==3)
				{
					FileName=FileName+"/Wednesday.xml";
				}
				else if(Weekday==4)
				{
					FileName=FileName+"/Thursday.xml";
				}
				else if(Weekday==5)
				{
					FileName=FileName+"/Friday.xml";
				}
				else if(Weekday==6)
				{
					FileName=FileName+"/Saturday.xml";
				}
				else
				{
					FileName=FileName+"/Sunday.xml";
				}
				
				

				try
				{
					BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(FileName), "UTF8"));
					QDParser.parse(this, frf);

				}
				catch (Exception Primes)
				{
					Primes.printStackTrace();
				}
				}
				
		//READ THE PENTECOSTARION!
		
		//Integer.parseInt(dayInfo.get(expression).toString())
		int nday=Integer.parseInt(StringOp.dayInfo.get("nday").toString());
	
		if (nday >= -70 && nday < 0)
		{
			filename = triodionFileName;
			lineNumber = Math.abs(nday);
		}
		else if (nday < -70)
		{
			// WE HAVE NOT YET REACHED THE LENTEN TRIODION
			filename = pentecostarionFileName;
			lineNumber = Integer.parseInt(StringOp.dayInfo.get("ndayP").toString()) + 1;
		}
		else
		{
			// WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
			filename = pentecostarionFileName;
			lineNumber = nday + 1;
		}

		filename += lineNumber >= 10 ? lineNumber + ".xml" : "0" + lineNumber + ".xml"; // CLEANED UP
		// READ THE PENTECOSTARION / TRIODION INFORMATION
		//IF THERE ARE SPECIAL TROPARION1's FROM THIS FILE THEY CAN OVERRIDE THE SET PIECES
		
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//CHECK WHAT TYPE OF SERVICE WE ARE DEALING WITH
		//POTENTIAL STREAMLINING OF THE SERVICE: ALL THE RULES HAVE NOW BEEN OBTAINED EXCEPT FOR ANY OVERRIDES
		ServiceInfo ServicePrimes=new ServiceInfo("TERCE");
		OrderedHashtable PrimesTrial = ServicePrimes.ServiceRules();
		
		Type=PrimesTrial.get("Type").toString();
		LentenK=(String) PrimesTrial.get("LENTENK");
				
		String PrimesAdd1=new String();
				
		if (Type.equals("None"))
		{
			//THERE ARE NO SERVICES TODAY, THAT IS, THE ROYAL HOURS ARE SERVED INSTEAD
			return "No Service Today";
		}
		else if(Type.equals("Paschal"))
		{
                    
                    return ReadPrime.startService(ServicesFileName+"PaschalHours.xml");
		}
		
		//I WOULD THEN NEED TO READ THE MENOLOGION, BUT I WILL NOT DO SO RIGHT NOW.
		//DETERMINE THE ORDERING OF THE TROPARIA AND KONTAKIA IF THERE ARE 2 OR MORE
				
		String strOut= new String();
		StringOp.dayInfo.put("PFlag1",TypeP);
		StringOp.dayInfo.put("PFlag2",0);
		//NOTE PFlag2 == 3 for Holy Week Services!
		if(Type.equals("Lenten"))
	       {
	       		StringOp.dayInfo.put("PFlag2",1);
	       		
	       		if(LentenK != null)
	       		{
	       			StringOp.dayInfo.put("PFlag2",2);
	       			//CREATE THE KATHISMA PART
	       			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ServicesFileName+"Var/PKath3.xml"),"UTF8"));
	    			String Data="<SERVICES>\r\n<LANGUAGE>\r\n<GET File=\"Kathisma"+LentenK+"\" Null=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    			out.write(Data);
	    			out.close();
	       		}
	       	}
	       	else
	       	{
	       		//CREATE THE FIRST TROPAR (BEFORE THE Glory...) PART, IF ANY
			//CREATE THE SECOND TROPAR (NORMAL)
			//APPROPRIATE TROPAR STILL NEEDS TO BE DETERMINED!!
			if(Troparion1 != null)
	    		{
	    		    	if(Troparion2 != null)
	    		    	{
	    		    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ServicesFileName+"Var/PTrop31.xml"),"UTF8"));
	    				String Data="<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"TROPARION/"+Troparion1+"\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    				out.write(Data);
	    				out.close();
	    				
	    				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ServicesFileName+"Var/PTrop32.xml"),"UTF8"));
	    				Data="<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"TROPARION/"+Troparion2+"\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    				out.write(Data);
	    				out.close();
					
	    		    	}
    	     			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ServicesFileName+"Var/PTrop32.xml"),"UTF8"));
	    			String Data="<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"TROPARION/"+Troparion1+"\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    			out.write(Data);
	    			out.close();
    	     		}
    	     			
	       	}
	       	
	       	//GET AND CREATE THE APPRORIATE KONTAKION
	       	//APROPRIATE KONTAKION MUST STILL BE CREATED!
	       	if (Kontakion1 != null)
		{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ServicesFileName+"Var/PKont3.xml"),"UTF8"));
	    		String Data="<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"KONTAKION/"+Kontakion1+"\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    		out.write(Data);
	    		out.close();
		}
	    	 //Else we are dealing with a Lenten service that does not have any variable parts.
		
		strOut=ReadPrime.startService(ServicesFileName + "ThirdHour.xml")+"</p>";
	
	   
	     return strOut;	     	     
	}
	public void startDocument()
	{

	}

	public void endDocument()
	{

	}

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
		if(elem.equals("LANGUAGE") || elem.equals("TONE"))
		{
			read=true;
		}
		if(elem.equals("TEXT") && read)
		{
			text+=(String)table.get("Value");
			
		}
		if (elem.equals("PRIMES") && read)
		{
			//WE ARE DEALING WITH THE INFORMATION FOR PRIMES (THERE COULD BE INFORMATION FOR OTHER SERVICES)
			//THE VARIABLE COMPONETS IN THIS SERVICE ARE GIVEN BELOW
			String value=(String)table.get("Type");
			if(value != null)
			{
				Type=(String)table.get("Type");
			}
			value=(String)table.get("TROPARION1");
			if(value != null)
			{
				Troparion1=(String)table.get("TROPARION1");
			}
			value=(String)table.get("KONTAKION1");
			if(value != null)
			{
				Kontakion1=(String)table.get("KONTAKION1");
			}
			value=(String)table.get("KONTAKION2");
			if(value != null)
			{
				Kontakion1=(String)table.get("KONTAKION2");
			}
			value=(String)table.get("TROPARION2");
			if(value != null)
			{
				Troparion1=(String)table.get("TROPARION2");
			}
				
			value=(String)table.get("LENTENK");
			if(value != null)
			{
				LentenK=(String)table.get("LENTENK");
				//System.out.println(LentenK);
			}
			
		}
		//OTHER LITURGICAL SERVICES WOULD FOLLOW HERE

	}

	public void endElement(String elem)
	{
		if(elem.equals("LANGUAGE") || elem.equals("TONE"))
		{
			read=false;
		}
	}

	public void text(String text)
	{

	}

	private boolean eval(String expression) throws IllegalArgumentException
	{
		return false;
	}
	

	public static void main(String[] argz)
	{
		
		new Primes(3);	//CREATE THE SERVICE FOR WEDNESDAY FOR TONE 1.
	}
	
	public String readText(String filename)
	{
		try
		{
       			 text= new String();
       			 BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
       			 QDParser.parse(this,fr);
       			 if(text.length()==0)
       			 {
       			 	text=null;
       			 }       			 			
       			      			       		
	    	}
	     	catch (Exception e)
	     	{
	     		//SERIOUS PROBLEM MISSING A PART OF THE SERVICE!
	     		System.out.println(filename);
	     		e.printStackTrace();
	     		return null;
	     	}
	     	
	     	return text;
	}

	 public void actionPerformed(ActionEvent e)
  {
        JMenuItem source = (JMenuItem)(e.getSource());
        String name = source.getText();
       if (name.equals(HelpNames[2]))
        {
        	 new About();
        }
         if (name.equals(HelpNames[0]))
        {
        	 //LAUNCH THE HELP FILE
        	 
        }
        if(name.equals(FileNames[1]))
        {
        	//SAVE THE CURRENT WINDOW
        	helper.SaveHTMLFile(PrimesNames[1]+" "+today, strOut);
        	       	
       	}
        if(name.equals(FileNames[4]))
        {
        	//CLOSE THE PRIMES FRAME
        	if(helper.closeFrame(LanguageNames[7]))
        	{
        		frames.dispose();
        	}
        }
        if(name.equals(FileNames[6]))
        {
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
    
    protected String getClassName(Object o)
    {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");
        return classString.substring(dotIndex+1);
    }

    public void itemStateChanged(ItemEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Item event detected."
                   + newline
                   + "    Event source: " + source.getText()
                   + " (an instance of " + getClassName(source) + ")"
                   + newline
                   + "    New state: "
                   + ((e.getStateChange() == ItemEvent.SELECTED) ?
                     "selected":"unselected");
        System.out.println(s);
        //output.append(s + newline);
        //output.setCaretPosition(output.getDocument().getLength());
    }
    
	public void propertyChange(PropertyChangeEvent e)
	{
		//THERE IS NOTHING HERE TO DO??
		try
		{
			output.setText(createPrimes());
			output.setCaretPosition(0);
		}
		catch (Exception e1)
		{
		
		}
		
	}

	
}

