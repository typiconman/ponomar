package net.ponomar.services;

import javax.swing.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import net.ponomar.calendar.JDate;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.panels.PrimeSelector;
import net.ponomar.parsing.Service;
import net.ponomar.parsing.ServiceInfo;
import net.ponomar.parsing.utility.QDParser;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 

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
public class ThirdHour extends LitService
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	//THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	//TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	//DURING THE COURSE OF A SINGLE WEEK.
	private static String fileNameIn=Constants.SERVICES_PATH + "PRIMES1/";
	private static String fileNameOut=fileNameIn+"Primes.html";
	private PrimeSelector selectorP;//=new PrimeSelector();	
	
	public ThirdHour(JDate date, LinkedHashMap<String, Object> dayInfo)
	{
            analyse.setDayInfo(dayInfo);
            langText=new LanguagePack(dayInfo);
            primesNames=langText.obtainValues(langText.getPhrases().get("Terce"));
	languageNames=langText.obtainValues(langText.getPhrases().get(Constants.LANGUAGE_MENU));
        fileNames=langText.obtainValues(langText.getPhrases().get("File"));
	helpNames=langText.obtainValues(langText.getPhrases().get("Help"));
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
				JOptionPane.showOptionDialog(null, primesNames[0], langText.getPhrases().get("0") + langText.getPhrases().get(Constants.COLON) + primesNames[1], JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			}
			else
			{
				//strOut=strOut+"<p><Font Color='red'>Disclaimer: This is a preliminary attempt at creating the Primes service.</Font></p>";
				//int LangCode=Integer.parseInt(Analyse.getDayInfo().get("LS").toString());
                                //if (LangCode==2 || LangCode==3 ){
                                    //strOut="<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><p><font face=\"Ponomar Unicode TT\" size=\"5\">"+strOut+"</font></p>";
                                    //System.out.println("Added Font");
                                   //}

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
		int typeP=selectorP.getTypeValue();
		Service readPrime=new Service(analyse.getDayInfo());
		//FIRST READ THE TONE FILES:
				int weekday=Integer.parseInt(analyse.getDayInfo().get("dow").toString());
				//System.out.println(Weekday);
				int tone=Integer.parseInt(analyse.getDayInfo().get("Tone").toString());
				if(tone==8)
				{
					tone=0;
				}
				//System.out.println(Tone);
				if(tone != -1)
				{
				String fileName=OCTOECHEOS_FILENAME + "Tone " +tone;
				if (weekday==1)
				{
					fileName=fileName+"/Monday.xml";
				}
				else if(weekday==2)
				{
					fileName=fileName+"/Tuesday.xml";
				}
				else if(weekday==3)
				{
					fileName=fileName+"/Wednesday.xml";
				}
				else if(weekday==4)
				{
					fileName=fileName+"/Thursday.xml";
				}
				else if(weekday==5)
				{
					fileName=fileName+"/Friday.xml";
				}
				else if(weekday==6)
				{
					fileName=fileName+"/Saturday.xml";
				}
				else
				{
					fileName=fileName+"/Sunday.xml";
				}
				
				

				try
				{
					BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(analyse.getDayInfo().get("LS").toString(),fileName)), StandardCharsets.UTF_8));
					QDParser.parse(this, frf);

				}
				catch (Exception primes)
				{
					primes.printStackTrace();
				}
				}
				
		//READ THE PENTECOSTARION!
		
		//Integer.parseInt(dayInfo.get(expression).toString())
		int nday=Integer.parseInt(analyse.getDayInfo().get("nday").toString());
	
		if (nday >= -70 && nday < 0)
		{
			filename = Constants.TRIODION_PATH;
			lineNumber = Math.abs(nday);
		}
		else if (nday < -70)
		{
			// WE HAVE NOT YET REACHED THE LENTEN TRIODION
			filename = Constants.PENTECOSTARION_PATH;
			lineNumber = Integer.parseInt(analyse.getDayInfo().get(Constants.NDAY_P).toString()) + 1;
		}
		else
		{
			// WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
			filename = Constants.PENTECOSTARION_PATH;
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
		ServiceInfo servicePrimes=new ServiceInfo("TERCE",analyse.getDayInfo());
		LinkedHashMap primesTrial = servicePrimes.serviceRules();
		
		type=primesTrial.get("Type").toString();
		lentenKat=(String) primesTrial.get(LENTENK);
				
		String primesAdd1="";
				
		if (type.equals("None"))
		{
			//THERE ARE NO SERVICES TODAY, THAT IS, THE ROYAL HOURS ARE SERVED INSTEAD
			return "No Service Today";
		}
		else if(type.equals("Paschal"))
		{
                    
                    return readPrime.startService(Constants.SERVICES_PATH+"PaschalHours.xml");
		}
		
		//I WOULD THEN NEED TO READ THE MENOLOGION, BUT I WILL NOT DO SO RIGHT NOW.
		//DETERMINE THE ORDERING OF THE TROPARIA AND KONTAKIA IF THERE ARE 2 OR MORE
				
		String strOut= "";
		analyse.getDayInfo().put(Constants.P_FLAG_1,typeP);
		analyse.getDayInfo().put(Constants.P_FLAG_2,0);
		//NOTE PFlag2 == 3 for Holy Week Services!
		if(type.equals("Lenten"))
	       {
	       		analyse.getDayInfo().put(Constants.P_FLAG_2,1);
	       		
	       		if(lentenKat != null)
	       		{
	       			analyse.getDayInfo().put(Constants.P_FLAG_2,2);
	       			//CREATE THE KATHISMA PART
	       			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString()+Constants.SERVICES_PATH+"Var/PKath3.xml"),StandardCharsets.UTF_8));
	    			String data="<SERVICES>\r\n<LANGUAGE>\r\n<GET File=\"Kathisma"+lentenKat+"\" Null=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    			out.write(data);
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
	    		    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString()+Constants.SERVICES_PATH+"Var/PTrop31.xml"),StandardCharsets.UTF_8));
	    				String data=TROPARION_OUTPUT_START + troparion1 + TROPARION_OUTPUT_END;
	    				out.write(data);
	    				out.close();
	    				
	    				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString()+Constants.SERVICES_PATH+"Var/PTrop32.xml"),StandardCharsets.UTF_8));
	    				data=TROPARION_OUTPUT_START + troparion2 + TROPARION_OUTPUT_END;
	    				out.write(data);
	    				out.close();
					
	    		    	}
    	     			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString()+Constants.SERVICES_PATH+"Var/PTrop32.xml"),StandardCharsets.UTF_8));
	    			String data=TROPARION_OUTPUT_START + troparion1 + TROPARION_OUTPUT_END;
	    			out.write(data);
	    			out.close();
    	     		}
    	     			
	       	}
	       	
	       	//GET AND CREATE THE APPRORIATE KONTAKION
	       	//APROPRIATE KONTAKION MUST STILL BE CREATED!
	       	if (kontakion1 != null)
		{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.LANGUAGES_PATH + "/" + analyse.getDayInfo().get("LS").toString()+Constants.SERVICES_PATH+"Var/PKont3.xml"),StandardCharsets.UTF_8));
	    		String data="<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"KONTAKION/"+kontakion1+"\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	    		out.write(data);
	    		out.close();
		}
	    	 //Else we are dealing with a Lenten service that does not have any variable parts.
		
		strOut=readPrime.startService(Constants.SERVICES_PATH + "ThirdHour.xml")+"</p>";
	
	   
	     return strOut;	     	     
	}
	
	public void startElement(String elem, HashMap<String, String> table)
	{
		
		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo. 
		if (table.get("Cmd") != null)
		{
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
			
			if (!analyse.evalbool(table.get("Cmd")))
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
			text+=table.get(Constants.VALUE);
			
		}
		if (elem.equals("TERCE") && read)
		{
			//WE ARE DEALING WITH THE INFORMATION FOR TERCE (THERE COULD BE INFORMATION FOR OTHER SERVICES)
			//THE VARIABLE COMPONETS IN THIS SERVICE ARE GIVEN BELOW
			String value=table.get("Type");
			if(value != null)
			{
				type=table.get("Type");
			}
			value=table.get(Constants.TROPARION_1);
			if(value != null)
			{
				troparion1=table.get(Constants.TROPARION_1);
			}
			value=table.get(Constants.KONTAKION_1);
			if(value != null)
			{
				kontakion1=table.get(Constants.KONTAKION_1);
			}
			value=table.get(Constants.KONTAKION_2);
			if(value != null)
			{
				kontakion1=table.get(Constants.KONTAKION_2);
			}
			value=table.get(Constants.TROPARION_2);
			if(value != null)
			{
				troparion1=table.get(Constants.TROPARION_2);
			}
				
			value=table.get(LENTENK);
			if(value != null)
			{
				lentenKat=table.get(LENTENK);
				//System.out.println(LentenK);
			}
			
		}
		//OTHER LITURGICAL SERVICES WOULD FOLLOW HERE

	}

	public static void main(String[] argz)
	{
		
		//new Primes(3);	//CREATE THE SERVICE FOR WEDNESDAY FOR TONE 1.
	}
    	
}

