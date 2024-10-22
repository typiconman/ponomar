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

(C) 2007, 2008, 2024 YURI SHARDT. ALL RIGHTS RESERVED.
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
public class Primes implements DocHandler, ActionListener, ItemListener, PropertyChangeListener
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	//THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	//TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	//DURING THE COURSE OF A SINGLE WEEK.
	private final static String configFileName = "ponomar.config";   //CONFIGURATIONS FILE
	private final static String octoecheosFileName   = "xml/Services/Octoecheos/";   // THE LOCATION OF THE BASIC SERVICE RULES
	private final static String ServicesFileName = "xml/Services/"; // THE LOCATION FOR ANY EXTRA INFORMATION
	private static OrderedHashtable PrimesTK;
	private static String FileNameIn="xml/Services/PRIMES1/";
	private static String FileNameOut=FileNameIn+"Primes.html";
	private static String text;
	private static boolean read=false;
	private static String Type;
	private String Troparion1;
	private String Kontakion1;
	private String Kontakion2;
	private String Troparion2;
	private final static String triodionFileName   = "xml/triodion/";   // TRIODION FILE
	private final static String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
	private String filename;
	private int lineNumber;
	private LanguagePack Text;//=new LanguagePack();
	private String[] PrimesNames;//=Text.obtainValues((String)Text.Phrases.get("Primes"));
	private String[] LanguageNames;//=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private String LentenK;				//ANY REQUIRED KATHISMA REFERENCED USING "LENTENK = "17"" WOULD BE THE 17th KATHISMA.
	private JFrame frames;
	private String[] FileNames;//=Text.obtainValues((String)Text.Phrases.get("File"));
	private String[] HelpNames;//=Text.obtainValues((String)Text.Phrases.get("Help"));
	String newline = "\n";
	private String strOut;
	private JDate2 today;
	private Helpers helper;
	private PrimeSelector SelectorP;//=new PrimeSelector();
	private PrintableTextPane output;
        private String DisplayFont =new String(); //ALLOWS A CUSTOM FONT AND SIZE TO BE SPECIFIED FOR A GIVEN BIBLE READING: REQUIRED FOR OLD CHURCH SLAVONIC AT PRESENT
	private String DisplaySize="12";  //UNTIL A COMPLETE UNICODE FONT IS AVAILIBLE.
	private Font DefaultFont=new Font("",Font.BOLD,12);		//CREATE THE DEFAULT FONT
	private Font CurrentFont=DefaultFont;
        private StringOp Analyse=new StringOp();
        private String VersionControl="";
        private Day menologion;
	
	
		
	public Primes(JDate2 date, OrderedHashtable dayInfo)
	{
            Analyse.dayInfo=dayInfo;
            Text=new LanguagePack(dayInfo);
            PrimesNames=Text.obtainValues((String)Text.Phrases.get("Primes"));
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
		today=date;
		helper=new Helpers(Analyse.dayInfo);
		try
		{
			String strOut=createPrimes();
			if(strOut.equals("No Service Today"))
			{
				Object[] options = {LanguageNames[3]};
				JOptionPane.showOptionDialog(null, PrimesNames[0],(String)Text.Phrases.get("0") + Text.Phrases.get("Colon")+ PrimesNames[1], JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			}
			else
			{
				//strOut=strOut+"<p><Font Color='red'>Disclaimer: This is a preliminary attempt at creating the Primes service.</Font></p>";
				//int LangCode=Integer.parseInt(Analyse.dayInfo.get("LS").toString());
                                //if (LangCode==2 || LangCode==3 ){
                                    //strOut="<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><p><font face=\"Ponomar Unicode TT\" size=\"5\">"+strOut+"</font></p>";
                                    //System.out.println("Added Font");
                                  // }

                                PrimesWindow(strOut);
			}
		}
		catch (IOException j)
		{
		}
		
		 	
	}
	private void PrimesWindow(String textOut)
	{
		frames=new JFrame((String)Text.Phrases.get("0") + (String)Text.Phrases.get("Colon")+ PrimesNames[1]);
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
		MenuFiles demo=new MenuFiles(Analyse.dayInfo);
		PrimeSelector trial=new PrimeSelector(Analyse.dayInfo);
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
                
                Helpers orient=new Helpers(Analyse.dayInfo);
                orient.applyOrientation(frames,(ComponentOrientation)Analyse.dayInfo.get("Orient"));

		//scrollPane.top();
	}
	private String createPrimes() throws IOException
	{
		//OBTAIN THE DEFAULTS FOR THE SERVICE (WHAT WAS LAST USED!)
		Analyse.dayInfo.put("PS",SelectorP.getWhoValue());
		int TypeP=SelectorP.getTypeValue();
		Service ReadPrime=new Service(Analyse.dayInfo);
		//FIRST READ THE TONE FILES:
				int Weekday=Integer.parseInt(Analyse.dayInfo.get("dow").toString());
				//System.out.println(Weekday);
				int Tone=Integer.parseInt(Analyse.dayInfo.get("Tone").toString());
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
					BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(Analyse.dayInfo.get("LS").toString(),FileName)), "UTF8"));
					QDParser.parse(this, frf);

				}
				catch (Exception Primes)
				{
					Primes.printStackTrace();
				}
				}
				
		//READ THE PENTECOSTARION!
		
		//Integer.parseInt(dayInfo.get(expression).toString())
		int nday=Integer.parseInt(Analyse.dayInfo.get("nday").toString());
	
		if (nday >= -70 && nday < 0)
		{
			filename = triodionFileName;
			lineNumber = Math.abs(nday);
		}
		else if (nday < -70)
		{
			// WE HAVE NOT YET REACHED THE LENTEN TRIODION
			filename = pentecostarionFileName;
			lineNumber = Integer.parseInt(Analyse.dayInfo.get("ndayP").toString()) + 1;
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
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(Analyse.dayInfo.get("LS").toString(),filename)), "UTF8"));
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
               
        String filenameS = Analyse.dayInfo.get("SolarPath").toString();
        menologion = new Day(filenameS, Analyse.dayInfo);
                
                
		//CHECK WHAT TYPE OF SERVICE WE ARE DEALING WITH
		//POTENTIAL STREAMLINING OF THE SERVICE: ALL THE RULES HAVE NOW BEEN OBTAINED EXCEPT FOR ANY OVERRIDES
		ServiceInfo ServicePrimes=new ServiceInfo("PRIME",Analyse.dayInfo);
		OrderedHashtable PrimesTrial = ServicePrimes.ServiceRules();
                
               /* ServiceInfo ServicePrimesM=new ServiceInfo("PRIME",Analyse.dayInfo.get("SolarPath").toString()+".xml",Analyse.dayInfo);
		OrderedHashtable PrimesTrialM = ServicePrimesM.ServiceRules();
                System.out.println("Information Found: "+PrimesTrialM);
                System.out.println("Override Type: "+PrimesTrialM.get("Type").toString());
                System.out.println("Override Troparion: "+PrimesTrialM.get("Troparion").toString());
                */
              /*  System.out.println(PrimesTrial.get("PickT"));
                System.out.println(PrimesTrial.get("Troparion"));
                System.out.println(Troparion1);
		*/
		Type=PrimesTrial.get("Type").toString();
		LentenK=(String) PrimesTrial.get("LENTENK");
                VersionControl=Type+".";
				
		String PrimesAdd1=new String();
				
		if (Type.equals("None"))
		{
			//THERE ARE NO SERVICES TODAY, THAT IS, THE ROYAL HOURS ARE SERVED INSTEAD
			return "No Service Today";
		}
		else if(Type.equals("Paschal"))
		{
                    
                    return ReadPrime.startService(ServicesFileName+"PaschalHours.xml")+"\r\n"+VersionControl;
		}
		
		//I WOULD THEN NEED TO READ THE MENOLOGION, BUT I WILL NOT DO SO RIGHT NOW.
		//DETERMINE THE ORDERING OF THE TROPARIA AND KONTAKIA IF THERE ARE 2 OR MORE
				
		String strOut= new String();
		Analyse.dayInfo.put("PFlag1",TypeP);
		Analyse.dayInfo.put("PFlag2",0);
		//NOTE PFlag2 == 3 for Holy Week Services!
		if(Type.equals("Lenten"))
	       {
	       		Analyse.dayInfo.put("PFlag2",1);
	       		
	       		if(LentenK != null)
	       		{
	       			Analyse.dayInfo.put("PFlag2",2);
	       			//CREATE THE KATHISMA PART
	       			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/"+Analyse.dayInfo.get("LS").toString()+ServicesFileName+"Var/PKath.xml"),"UTF8"));
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
                    String options=PrimesTrial.get("Troparion").toString();
                    String amount=PrimesTrial.get("PickT").toString();
                    OrderedHashtable Troparia=troparia(options, amount);
                    int amountI=Integer.parseInt(amount);
                    int amountA=Troparia.size(); //How many troparia did we actually find, we may find fewer than allowed.
                                      
                    
                    //When properly done, this will give me the maximum allowed. But what if there are two allowed, but not available!?!
			if (Math.min(amountA, amountI)==2){
                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/"+Analyse.dayInfo.get("LS").toString()+ServicesFileName+"Var/PTrop1.xml"),"UTF8"));
                            GetID DataID=(GetID) Troparia.get(1);
                            DataID.Header="1";
                            DataID.ToneA="1";
                            DataID.RedFirst="1";
                            DataID.NewLine="1";
                            DataID.Who="R";
                            String Data="<SERVICES>\r\n<LANGUAGE>\r\n"+DataID.getHTML()+"\r\n</LANGUAGE>\r\n</SERVICES>";
                            out.write(Data);
                            out.close();
                            VersionControl+="T{"+DataID.GetFullID();
                            DataID=(GetID) Troparia.get(2);
                            DataID.Header="1";
                            DataID.ToneA="1";
                            DataID.RedFirst="1";
                            DataID.NewLine="1";
                            DataID.Who="R";
                            Data="<SERVICES>\r\n<LANGUAGE>\r\n"+DataID.getHTML()+"\r\n</LANGUAGE>\r\n</SERVICES>";
	    				
                            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/"+Analyse.dayInfo.get("LS").toString()+ServicesFileName+"Var/PTrop2.xml"),"UTF8"));
                            out.write(Data);
                            out.close();
                            VersionControl+=","+DataID.GetFullID()+"}.";
                        }else if (Math.min(amountA, amountI)==1){
                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/"+Analyse.dayInfo.get("LS").toString()+ServicesFileName+"Var/PTrop2.xml"),"UTF8"));
	    			GetID DataID=(GetID) Troparia.get(1);
                               // System.out.println(DataID);
                            DataID.Header="1";
                            DataID.ToneA="1";
                            DataID.RedFirst="1";
                            DataID.NewLine="1";
                            DataID.Who="R";
                            DataID.Times="1";
                            VersionControl+="T{"+DataID.GetFullID()+"}.";
                            String Data="<SERVICES>\r\n<LANGUAGE>\r\n"+DataID.getHTML()+"\r\n</LANGUAGE>\r\n</SERVICES>";
                            out.write(Data);
	    			out.close();
                              //  System.out.println("TESTING COMPLETED");
                            //Clearing the other Troparion file.
                            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/"+Analyse.dayInfo.get("LS").toString()+ServicesFileName+"Var/PTrop1.xml"),"UTF8"));
                            out.write("<SERVICE/>");
                            out.close();
                        }else {
                            //We are in big trouble: no troparia were found, but we need at least one!
                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/"+Analyse.dayInfo.get("LS").toString()+ServicesFileName+"Var/PTrop2.xml"),"UTF8"));
	    			GetID DataID=(GetID) Troparia.get(1);
                               // System.out.println(DataID);
                            String Data="<SERVICES>\r\n<LANGUAGE>\r\n<TEXT Value=\"NO RELEVANT TROPARIA FOUND.\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
                            out.write(Data);
           			out.close();
                                System.out.println("Error reading troparia: None found!");
                            
                        }
                        /* OLD VERSION
                        if(Troparion1 != null)
	    		{
	    		    	if(Troparion2 != null)
	    		    	{
	    		    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/"+Analyse.dayInfo.get("LS").toString()+ServicesFileName+"Var/PTrop1.xml"),"UTF8"));
	    				String Data="<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"TROPARION/"+Troparion1+"\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    				out.write(Data);
	    				out.close();
	    				
	    				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/"+Analyse.dayInfo.get("LS").toString()+ServicesFileName+"Var/PTrop2.xml"),"UTF8"));
	    				Data="<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"TROPARION/"+Troparion2+"\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    				out.write(Data);
	    				out.close();
					
	    		    	}
    	     			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/"+Analyse.dayInfo.get("LS").toString()+ServicesFileName+"Var/PTrop2.xml"),"UTF8"));
	    			String Data="<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"TROPARION/"+Troparion1+"\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    			out.write(Data);
	    			out.close();
    	     		}*/
                        
			if(Type.equals("HolyWeek"))
			{
				Analyse.dayInfo.put("PFlag2",3);
			}
    	     			
	       	}
	       	
	       	//GET AND CREATE THE APPRORIATE KONTAKION
	       	//APROPRIATE KONTAKION MUST STILL BE CREATED!
               if (Type.equals("Lenten")){
                 if (Kontakion1 != null)
		{
			
                        //Old Version, still only works for the Lenten Part
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/"+Analyse.dayInfo.get("LS").toString()+ServicesFileName+"Var/PKont1.xml"),"UTF8"));
	    		String Data="<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"KONTAKION/"+Kontakion1+"\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    		out.write(Data);
	    		out.close();
               }
               }else
               {
                String optionsK=PrimesTrial.get("Kontakion").toString();
                    String amountK=PrimesTrial.get("PickK").toString();
                    OrderedHashtable Troparia=contacia(optionsK, amountK);
                    int amountIK=Integer.parseInt(amountK);
	       //	if (Kontakion1 != null)
		{
			GetID DataID=(GetID) Troparia.get(1);
                               // System.out.println(DataID);
                            DataID.Header="1";
                            DataID.ToneA="1";
                            DataID.RedFirst="1";
                            DataID.NewLine="1";
                            DataID.Who="R";
                            DataID.Times="1";
                            VersionControl+="K{"+DataID.GetFullID()+"}";
                            
                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/"+Analyse.dayInfo.get("LS").toString()+ServicesFileName+"Var/PKont1.xml"),"UTF8"));
	    		    String Data="<SERVICES>\r\n<LANGUAGE>\r\n"+DataID.getHTML()+"\r\n</LANGUAGE>\r\n</SERVICES>";
                            out.write(Data);
	    			out.close();
                        /*Old Version, still only works for the Lenten Part
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/languages/"+Analyse.dayInfo.get("LS").toString()+ServicesFileName+"Var/PKont1.xml"),"UTF8"));
	    		String Data="<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"KONTAKION/"+Kontakion1+"\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    		out.write(Data);
	    		out.close();
            */
                        //VersionControl+="K{"+DataID.Id+"}";
		}
               }
	    	 		
		//System.out.println("Primes Case A: ");
		strOut=ReadPrime.startService(ServicesFileName + "Prime.xml")+"<footer><p>"+VersionControl+"</p>";
                System.out.println("Type of Primes for Selected Day: "+VersionControl);
	
	   
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
			
			if (Analyse.evalbool(table.get("Cmd").toString()) == false)
			{
				
				return;
			}
		}
		//if(elem.equals("LANGUAGE") || elem.equals("TONE"))
		//{
			read=true;
		//}
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
		
		//new Primes(3);	//CREATE THE SERVICE FOR WEDNESDAY FOR TONE 1.
	}
	
	public String readText(String filename)
	{
		try
		{
       			 text= new String();
       			 BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(Analyse.dayInfo.get("LS").toString(),filename)), "UTF8"));
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
        	 Helpers orient=new Helpers(Analyse.dayInfo);
                orient.applyOrientation(new About(Analyse.dayInfo),(ComponentOrientation)Analyse.dayInfo.get("Orient"));
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
                    strOut=createPrimes();
                    output.setText(strOut);
			output.setCaretPosition(0);
		}
		catch (Exception e1)
		{
		
		}
		
	}
        public OrderedHashtable troparia(String options, String number)
	{
        //DETERMINE WHICH TROPARIA CAN BE USED TODAY GIVEN THE RULES
            OrderedHashtable Troparia=new OrderedHashtable();
            //FIRST PARSE THE options AND THEN DETERMINE HOW MANY
            String[] cases=options.split(",");
            int countT=1;
            for (int counterT = 0; counterT<cases.length; counterT++){
                //WE NOW NEED TO PARSE THE POTENTIAL TROPARIA
                //THE FIRST LETTER TELLS US HOW SPECIFIC THE REQUIREMENT IS
                String firstLetter=cases[counterT].substring(0,1);
                //System.out.println("First Letter is: "+firstLetter);
                if (firstLetter.equals("S")){
                    //WE ARE DEALING WITH A VERY SPECIFIC REQUIREMENT
                    String[] specific=cases[counterT].split("_");
                    String CID=specific[1]; //THE SECOND ELEMENT IS ALWAYS THE REQUIRED FILE
                    String TType="1";
                    if (specific.length>2){
                        //A SPECIFIC TYPE HAS BEEN REQUIRED
                        TType=specific[2];
                    }
                    GetID tropar=new GetID(CID,"/LITURGY/TROPARION/"+TType);
                    Troparia.put(countT,tropar);
                    countT=countT+1;
                   // System.out.println("The specific troparion is "+CID);
                }else if (firstLetter.equals("T")){
                    //WE NEED THE FILE CORRESPONDING TO THE TONE (I will ignore right now the weekday issue)
                    int tone=Integer.parseInt(Analyse.dayInfo.get("Tone").toString());
                    int dow=Integer.parseInt(Analyse.dayInfo.get("dow").toString());
                    int FileName=0;
                    if (dow==0){
                    FileName=9700+tone;
                    if (tone == 8){
                        FileName=9700;
                    }
                    }else{
                     FileName=9710+dow;   
                    }
                    GetID tropar=new GetID(Integer.toString(FileName),"/LITURGY/TROPARION/1");
                    Troparia.put(countT,tropar);
                    countT=countT+1;
                    
                   // System.out.println("The specific troparion based on today’s tone is "+FileName);
                   // System.out.println("The specific call is "+tropar.getHTML());
                }else if (firstLetter.equals("M")){
                    //WE NEED THE FILE CORRESPONDING TO THE HIGHEST (OR IF TIED, FIRST) RANKED HOLIDAY
                    
                    int dRankM=Integer.parseInt(Analyse.dayInfo.get("dRankM").toString());
                   //Day menologion=(Day) Analyse.dayInfo.get("SolarCycle");
                    Vector commemorations=menologion.getCommemorations();
                    for (int i = 0; i < commemorations.size(); i++) {
                        Commemoration1 CurrentC = (Commemoration1) commemorations.get(i);
                        if (CurrentC.getRank() == dRankM){
                            //We have found a commemoration with a given day rank. We will now search all such commemorations until we find a troparion!
                            String node="/LITURGY/TROPARION";
                            
                        if (CurrentC.getService(node,"1") !=null){
                            //I should search over other numbers, but I will assume that if "1" is present, then it will be taken!
                            //We are in luck, there is a troparion. Add it!
                            OrderedHashtable testing=CurrentC.getService(node,"1");
                            //System.out.println(testing);
                            if (testing.get("text").toString().length()>1){
                            String FileName=CurrentC.getCId();
                            //System.out.println("The troparion found is for "+FileName);
                            GetID tropar=new GetID(FileName,"/LITURGY/TROPARION/1");
                            Troparia.put(countT,tropar);
                            countT=countT+1;
                            }
                            
                        }
                        }
                    }
                    /*
                    Still needs to be worked out.
                    */              
                    //System.out.println("The Menologion cannot at present be asked.");
                    //countT=countT+1;
                }
                //System.out.println("counterT"+counterT);
            }
            //System.out.println("We stored " +countT+" troparia.");
            return Troparia;
	}
        public OrderedHashtable contacia(String options, String number)
	{
        //DETERMINE WHICH TROPARIA CAN BE USED TODAY GIVEN THE RULES
            OrderedHashtable Troparia=new OrderedHashtable();
            //FIRST PARSE THE options AND THEN DETERMINE HOW MANY
            String[] cases=options.split(",");
            int countT=1;
            for (int counterT = 0; counterT<cases.length; counterT++){
                //WE NOW NEED TO PARSE THE POTENTIAL TROPARIA
                //THE FIRST LETTER TELLS US HOW SPECIFIC THE REQUIREMENT IS
                String firstLetter=cases[counterT].substring(0,1);
                //System.out.println("First Letter is: "+firstLetter);
                if (firstLetter.equals("S")){
                    //WE ARE DEALING WITH A VERY SPECIFIC REQUIREMENT
                    String[] specific=cases[counterT].split("_");
                    String CID=specific[1]; //THE SECOND ELEMENT IS ALWAYS THE REQUIRED FILE
                    String TType="1";
                    if (specific.length>2){
                        //A SPECIFIC TYPE HAS BEEN REQUIRED
                        TType=specific[2];
                    }
                    GetID tropar=new GetID(CID,"/LITURGY/KONTAKION/"+TType);
                    Troparia.put(countT,tropar);
                    countT=countT+1;
                    //System.out.println("The specific troparion is "+CID);
                }else if (firstLetter.equals("T")){
                    //WE NEED THE FILE CORRESPONDING TO THE TONE (I will ignore right now the weekday issue)
                    int tone=Integer.parseInt(Analyse.dayInfo.get("Tone").toString());
                    int dow=Integer.parseInt(Analyse.dayInfo.get("dow").toString());
                    int FileName=0;
                    if (dow==0){
                    FileName=9700+tone;
                    if (tone == 8){
                        FileName=9700;
                    }
                    }else{
                     FileName=9710+dow;   
                    }
                    GetID tropar=new GetID(Integer.toString(FileName),"/LITURGY/KONTAKION/1");
                    Troparia.put(countT,tropar);
                    countT=countT+1;              
                    System.out.println("The specific contacion based on today’s tone is "+FileName);
                }else if (firstLetter.equals("M")){
                    //WE NEED THE FILE CORRESPONDING TO THE HIGHEST (OR IF TIED, FIRST) RANKED HOLIDAY
                    
                    int dRankM=Integer.parseInt(Analyse.dayInfo.get("dRankM").toString());
                   //Day menologion=(Day) Analyse.dayInfo.get("SolarCycle");
                    Vector commemorations=menologion.getCommemorations();
                    for (int i = 0; i < commemorations.size(); i++) {
                        Commemoration1 CurrentC = (Commemoration1) commemorations.get(i);
                        if (CurrentC.getRank() == dRankM){
                            //We have found a commemoration with a given day rank. We will now search all such commemorations until we find a troparion!
                            String node="/LITURGY/KONTAKION";
                            
                        if (CurrentC.getService(node,"1") !=null){
                            //I should search over other numbers, but I will assume that if "1" is present, then it will be taken!
                            //We are in luck, there is a troparion. Add it!
                            OrderedHashtable testing=CurrentC.getService(node,"1");
                            //System.out.println(testing);
                            if (testing.get("text").toString().length()>1){
                            String FileName=CurrentC.getCId();
                            //System.out.println("The troparion found is for "+FileName);
                            GetID tropar=new GetID(FileName,"/LITURGY/KONTAKION/1");
                            Troparia.put(countT,tropar);
                            countT=countT+1;
                            }
                            
                        }
                        }
                }
                }
            }
            //System.out.println("We stored " +countT+" troparia.");
            return Troparia;
	}


	
}

