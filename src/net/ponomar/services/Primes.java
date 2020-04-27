package net.ponomar.services;

import javax.swing.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import net.ponomar.calendar.JDate;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.panels.PrimeSelector;
import net.ponomar.parsing.QDParser;
import net.ponomar.parsing.Service;
import net.ponomar.parsing.ServiceInfo;
import net.ponomar.utility.Helpers;
import net.ponomar.utility.OrderedHashtable;

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
public class Primes extends LitService
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	//THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	//TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	//DURING THE COURSE OF A SINGLE WEEK.
	private static String fileNameIn="xml/Services/PRIMES1/";
	private static String fileNameOut=fileNameIn+"Primes.html";
	private PrimeSelector selectorP;//=new PrimeSelector();
			
	public Primes(JDate date, OrderedHashtable dayInfo)
	{
            analyse.setDayInfo(dayInfo);
            langText=new LanguagePack(dayInfo);
            primesNames=langText.obtainValues((String)langText.getPhrases().get("Primes"));
	languageNames=langText.obtainValues((String)langText.getPhrases().get("LanguageMenu"));
        fileNames=langText.obtainValues((String)langText.getPhrases().get("File"));
	helpNames=langText.obtainValues((String)langText.getPhrases().get("Help"));
        selectorP=new PrimeSelector(dayInfo);
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
		
		//Analyse.getDayInfo() = new Hashtable();
		//Analyse.getDayInfo().put("dow", Weekday);		//DETERMINE THE DAY OF THE WEEK.
		
		
		//CREATING THE SERVICE	
		today=date;
		helper=new Helpers(analyse.getDayInfo());
		try
		{
			String strOut=createHours();
			if(strOut.equals("No Service Today"))
			{
				Object[] options = {languageNames[3]};
				JOptionPane.showOptionDialog(null, primesNames[0],(String)langText.getPhrases().get("0") + langText.getPhrases().get("Colon")+ primesNames[1], JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			}
			else
			{
				//strOut=strOut+"<p><Font Color='red'>Disclaimer: This is a preliminary attempt at creating the Primes service.</Font></p>";
				//int LangCode=Integer.parseInt(Analyse.getDayInfo().get("LS").toString());
                                //if (LangCode==2 || LangCode==3 ){
                                    //strOut="<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><p><font face=\"Ponomar Unicode TT\" size=\"5\">"+strOut+"</font></p>";
                                    //System.out.println("Added Font");
                                  // }

                                serviceWindow(strOut);
			}
		}
		catch (IOException j)
		{
		}
		
		 	
	}

	protected String createHours() throws IOException
	{
		//OBTAIN THE DEFAULTS FOR THE SERVICE (WHAT WAS LAST USED!)
		analyse.getDayInfo().put("PS",selectorP.getWhoValue());
		int TypeP=selectorP.getTypeValue();
		Service ReadPrime=new Service(analyse.getDayInfo());
		//FIRST READ THE TONE FILES:
				int Weekday=Integer.parseInt(analyse.getDayInfo().get("dow").toString());
				//System.out.println(Weekday);
				int Tone=Integer.parseInt(analyse.getDayInfo().get("Tone").toString());
				if(Tone==8)
				{
					Tone=0;
				}
				//System.out.println(Tone);
				if(Tone != -1)
				{
				String FileName=OCTOECHEOS_FILENAME + "Tone " +Tone;
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
					BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(analyse.getDayInfo().get("LS").toString(),FileName)), StandardCharsets.UTF_8));
					QDParser.parse(this, frf);

				}
				catch (Exception Primes)
				{
					Primes.printStackTrace();
				}
				}
				
		//READ THE PENTECOSTARION!
		
		//Integer.parseInt(dayInfo.get(expression).toString())
		int nday=Integer.parseInt(analyse.getDayInfo().get("nday").toString());
	
		if (nday >= -70 && nday < 0)
		{
			filename = TRIODION_FILENAME;
			lineNumber = Math.abs(nday);
		}
		else if (nday < -70)
		{
			// WE HAVE NOT YET REACHED THE LENTEN TRIODION
			filename = PENTECOSTARION_FILENAME;
			lineNumber = Integer.parseInt(analyse.getDayInfo().get("ndayP").toString()) + 1;
		}
		else
		{
			// WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
			filename = PENTECOSTARION_FILENAME;
			lineNumber = nday + 1;
		}

		filename += lineNumber >= 10 ? lineNumber + ".xml" : "0" + lineNumber + ".xml"; // CLEANED UP
		// READ THE PENTECOSTARION / TRIODION INFORMATION
		//IF THERE ARE SPECIAL TROPARION1's FROM THIS FILE THEY CAN OVERRIDE THE SET PIECES
		
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(analyse.getDayInfo().get("LS").toString(),filename)), StandardCharsets.UTF_8));
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//CHECK WHAT TYPE OF SERVICE WE ARE DEALING WITH
		//POTENTIAL STREAMLINING OF THE SERVICE: ALL THE RULES HAVE NOW BEEN OBTAINED EXCEPT FOR ANY OVERRIDES
		ServiceInfo ServicePrimes=new ServiceInfo("PRIME",analyse.getDayInfo());
		OrderedHashtable PrimesTrial = ServicePrimes.serviceRules();
		
		type=PrimesTrial.get("Type").toString();
		lentenKat=(String) PrimesTrial.get(LENTENK);
				
		String PrimesAdd1="";
				
		if (type.equals("None"))
		{
			//THERE ARE NO SERVICES TODAY, THAT IS, THE ROYAL HOURS ARE SERVED INSTEAD
			return "No Service Today";
		}
		else if(type.equals("Paschal"))
		{
                    
                    return ReadPrime.startService(SERVICES_FILENAME+"PaschalHours.xml");
		}
		
		//I WOULD THEN NEED TO READ THE MENOLOGION, BUT I WILL NOT DO SO RIGHT NOW.
		//DETERMINE THE ORDERING OF THE TROPARIA AND KONTAKIA IF THERE ARE 2 OR MORE
				
		String strOut= "";
		analyse.getDayInfo().put("PFlag1",TypeP);
		analyse.getDayInfo().put("PFlag2",0);
		//NOTE PFlag2 == 3 for Holy Week Services!
		if(type.equals("Lenten"))
	       {
	       		analyse.getDayInfo().put("PFlag2",1);
	       		
	       		if(lentenKat != null)
	       		{
	       			analyse.getDayInfo().put("PFlag2",2);
	       			//CREATE THE KATHISMA PART
	       			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(PONOMAR_LANGUAGES+analyse.getDayInfo().get("LS").toString()+SERVICES_FILENAME+"Var/PKath.xml"),StandardCharsets.UTF_8));
	    			String Data="<SERVICES>\r\n<LANGUAGE>\r\n<GET File=\"Kathisma"+lentenKat+"\" Null=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    			out.write(Data);
	    			out.close();
	       		}
	       	}
	       	else
	       	{
	       		//CREATE THE FIRST TROPAR (BEFORE THE Glory...) PART, IF ANY
			//CREATE THE SECOND TROPAR (NORMAL)
			//APPROPRIATE TROPAR STILL NEEDS TO BE DETERMINED!!
			if(troparion1 != null)
	    		{
	    		    	if(troparion2 != null)
	    		    	{
	    		    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(PONOMAR_LANGUAGES+analyse.getDayInfo().get("LS").toString()+SERVICES_FILENAME+"Var/PTrop1.xml"),StandardCharsets.UTF_8));
	    				String Data = TROPARION_OUTPUT_START + troparion1 + TROPARION_OUTPUT_END;
	    				out.write(Data);
	    				out.close();
	    				
	    				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(PONOMAR_LANGUAGES+analyse.getDayInfo().get("LS").toString()+SERVICES_FILENAME+"Var/PTrop2.xml"),StandardCharsets.UTF_8));
	    				Data = TROPARION_OUTPUT_START + troparion2 + TROPARION_OUTPUT_END;
	    				out.write(Data);
	    				out.close();
					
	    		    	}
    	     			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(PONOMAR_LANGUAGES+analyse.getDayInfo().get("LS").toString()+SERVICES_FILENAME+"Var/PTrop2.xml"),StandardCharsets.UTF_8));
	    			String Data = TROPARION_OUTPUT_START + troparion1 + TROPARION_OUTPUT_END;
	    			out.write(Data);
	    			out.close();
    	     		}
    	     			
	       	}
	       	
	       	//GET AND CREATE THE APPRORIATE KONTAKION
	       	//APROPRIATE KONTAKION MUST STILL BE CREATED!
	       	if (kontakion1 != null)
		{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(PONOMAR_LANGUAGES+analyse.getDayInfo().get("LS").toString()+SERVICES_FILENAME+"Var/PKont1.xml"),StandardCharsets.UTF_8));
	    		String Data="<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"KONTAKION/"+kontakion1+"\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    		out.write(Data);
	    		out.close();
		}
	    	 		
		//System.out.println("Primes Case A: ");
		strOut=ReadPrime.startService(SERVICES_FILENAME + "Prime.xml")+"</p>";
	
	   
	     return strOut;	     	     
	}

	public void startElement(String elem, Hashtable table)
	{
		
		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo. 
		if (table.get("Cmd") != null)
		{
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
			
			if (analyse.evalbool(table.get("Cmd").toString()) == false)
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
				type=(String)table.get("Type");
			}
			value=(String)table.get("TROPARION1");
			if(value != null)
			{
				troparion1=(String)table.get("TROPARION1");
			}
			value=(String)table.get("KONTAKION1");
			if(value != null)
			{
				kontakion1=(String)table.get("KONTAKION1");
			}
			value=(String)table.get("KONTAKION2");
			if(value != null)
			{
				kontakion1=(String)table.get("KONTAKION2");
			}
			value=(String)table.get("TROPARION2");
			if(value != null)
			{
				troparion1=(String)table.get("TROPARION2");
			}
				
			value=(String)table.get(LENTENK);
			if(value != null)
			{
				lentenKat=(String)table.get(LENTENK);
				//System.out.println(LentenK);
			}
			
		}
		//OTHER LITURGICAL SERVICES WOULD FOLLOW HERE

	}

	public static void main(String[] argz)
	{
		
		//new Primes(3);	//CREATE THE SERVICE FOR WEDNESDAY FOR TONE 1.
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e)
	{
		//THERE IS NOTHING HERE TO DO??
		try
		{
                    strOut=createHours();
                    output.setText(strOut);
			output.setCaretPosition(0);
		}
		catch (Exception e1)
		{
		
		}
		
	}

	
}

