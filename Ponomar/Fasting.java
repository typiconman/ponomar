package Ponomar;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;
/***********************************************************************
THIS MODULE READS THE FASTING.XML FILE TO DETERMINE THE FAST ON A GIVEN DAY

(C) 2009 YURI SHARDT. ALL RIGHTS RESERVED.

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

public class Fasting implements DocHandler
{
	private final static String FileName   = "xml/Commands/Fasting.xml";
	private static boolean readPeriod=false;
	private static boolean readLanguage=false;
	private static OrderedHashtable Information;
	
	private LanguagePack Phrases=new LanguagePack();
	//GET THE APPROPRIATE FASTING LINES
	//private String[] ServiceNames=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
	//private String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private static String Fast;
	private static Helpers helper;
	public String FastRules() //throws IOException
	{
		Fast=new String();
		helper=new Helpers();
		Information=new OrderedHashtable();
		//THIS IS A KLUTZ THAT WILL BE REMOVED ONCE THERE IS A PROPER ABILITY TO RANK THE DAY
		//RANK 1 HOLIDAYS
                //I have removed the klutz, as dRank has been properly implemented! Y.S 2010/02/02 n.s.
		//StringOp.dayInfo.put("dRank",1);	//ANY RANK LESS THAN 4 WILL DO
		/*try
		{
			FileReader frf = new FileReader("Ponomar/xml/Commands/DivineLiturgy.xml");
			QDParser.parse(this, frf);
			int doy=Integer.parseInt(StringOp.dayInfo.get("doy").toString());
			int nday=Integer.parseInt(StringOp.dayInfo.get("nday").toString());
			if(doy == 256 || doy == 358 || doy == 5 ||  nday == 40 || nday == 50 || doy == 217)
			{
				//THESE ARE CLASS 1 HOLIDAYS
				StringOp.dayInfo.put("dRank",6);
			}
			if ((doy == 32 && nday != -48) || doy == 250 || doy == 226 || doy == 324 || doy == 83)
			{
				//THESE ARE CLASS 2 HOLIDAYS
				StringOp.dayInfo.put("dRank",6);
			}
			//THIS WILL NOT CATCH ALL THE RANK  3 OR 4 HOLIDAYS, BUT MOST OF THEM WILL BE SO CAUGHT
			Vector vect = (Vector) Information.get("Class3Transfers");
			if(vect != null)
			{
				for(Enumeration e2=vect.elements();e2.hasMoreElements();)
				{	
					String Command = (String)e2.nextElement();
					if (StringOp.evalbool(Command))
					{
						StringOp.dayInfo.put("dRank",5);	//IT DOES NOT MATTER FOR THE FASTING RULES WHETHER IT IS 3 OR 4 AS THE RANK.
					}
				}
			}
			//ADDING SOME FURTHER DAYS THAT ARE NOT INCLUDED ABOVE
			if(doy == 6)
			{
				StringOp.dayInfo.put("dRank",3);
			}
			if(doy == 127 || doy == 128 || doy == 161 || doy == 169 || doy == 185 || doy == 190 || doy == 199 || doy == 200 || doy == 220 || doy == 227 || doy == 239 || doy == 241 || doy == 242 || doy == 267 || doy == 281 || doy == 282 || doy == 290 || doy == 291 || doy == 295 || doy == 300 || doy == 328 || doy == 342 || doy == 345 || doy == 346 || doy == 350 || doy == 23 || doy == 26 || doy == 16 || doy == 19 || doy == 8 || doy == 10 || doy == 11 || doy == 13 || doy == 24)
			{
				StringOp.dayInfo.put("dRank",5);
			}
						
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		//FINISHED THE KLUTZ
                */
		//ACTUAL PROGRAMME
		//System.out.print("Today's rank is "+StringOp.dayInfo.get("dRank")+"\n");
		try
		{
			BufferedReader frf1 = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(StringOp.dayInfo.get("LS").toString(),FileName)), "UTF8"));
			QDParser.parse(this, frf1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;			//THERE WAS AN ERROR IN PROCESSING THE FILES
		}
		//NOW IT IS NECESSARY TO CONVERT THE COMPUTER SPEAK TO HUMAN SPEAK
		return convert(Fast);
	}
	public String convert(String Fast)
	{
		/*COMMON REGULATIONS
		0000000 : No Food
		0000001 : Strict Fast (Dry Food Only)
		0000011 : Food without oil
		0000111 : Food with oil/Fast: Wine and oil allowed
		0001111 : Caviar is permitted 
		0011111 : Fish is permitted/Fast: Fish, wine, and oil allowed
		0111111 : Meat is excluded
		1111111 : No fast
		0000010 : Wine Permitted
		*/
		
		String[] FastNames= Phrases.obtainValues((String)Phrases.Phrases.get("Fasts"));
				
		if(Fast.equals("0000000"))
		{
			return FastNames[4]+ FastNames[1]+FastNames[6]+ FastNames[7];
		}
		if(Fast.equals("0000001"))
		{
			return  FastNames[4]+FastNames[1]+FastNames[6]+ FastNames[8];
		}
		if(Fast.equals("0000011"))
		{
			return  FastNames[4]+FastNames[1]+FastNames[6]+ FastNames[9];
		}
		if(Fast.equals("0000111"))
		{
			return  FastNames[4]+FastNames[2];
		}
		if(Fast.equals("0001111"))
		{
			return FastNames[4]+FastNames[10];
		}
		if(Fast.equals("0011111"))
		{
			return  FastNames[4]+FastNames[3];
		}
		if(Fast.equals("0111111"))
		{
			return FastNames[4]+FastNames[11];
		}
		if(Fast.equals("1111111"))
		{
			return  FastNames[4]+FastNames[0];
		}
		if(Fast.equals("0000010"))
		{
			return FastNames[4]+FastNames[12];
		}
		//NONE OF THE PREDEFINED SEQUENCES WERE ENCOUNTERED.
		//PARSE IT ELEMENT BY ELEMENT!
		String[] item = new String[] {FastNames[13],FastNames[14],FastNames[15],FastNames[16],FastNames[17],FastNames[18],FastNames[19]};
		String[] permitted=new String[7];
		String[] forbidden=new String[7];
		int permit=0;
		int forbid=0;
		for(int i=0;i < 7;i++)
		{
			if(Fast.substring(i,i+1).equals("1"))
			{
				//THE GIVEN ITEM IS PERMITTED
				permitted[permit]=item[i];
				permit++;
			}
			else
			{
				//THE GIVEN ITEM IS FORBIDDEN
				forbidden[forbid]=item[i];
				forbid++;
			}
		}
		String output=FastNames[27]+" ";
		for(int i=0;i < permit;i++)
		{
			output+=permitted[i];
			if(i<permit-1)
			{
				output+=", ";
			}
			if(i==permit-2)
			{
				output+=" "+FastNames[25]+" ";
			}
		}
		if(permit==1)
		{
			output+=" "+FastNames[20]+" "+FastNames[23]; 
		}
		else if(permit==2)
		{
			//FOR THOSES SLAVIC LANGUAGES WITH THE DUAL
			output+=" "+FastNames[21]+" "+FastNames[23];
		}
		else
		{
			output+=" "+FastNames[22]+" "+FastNames[23];
		}
		output+=FastNames[26]+" ";
		for(int i=0;i < forbid;i++)
		{
			output+=forbidden[i];
			if(i<forbid-1)
			{
				output+=", ";
			}
			if(i==forbid-2)
			{
				output+=" "+FastNames[25]+" ";
			}
		}
		if(forbid==1)
		{
			output+=output+=" "+FastNames[20]+" "+FastNames[24]; 
		}
		else if(forbid==2)
		{
			//FOR THOSES SLAVIC LANGUAGES WITH THE DUAL
			output+=" "+FastNames[21]+" "+FastNames[24];
		}
		else
		{
			output+=" "+FastNames[22]+" "+FastNames[24];
		}
		return FastNames[4]+" " +output;
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
		//if(elem.equals("LANGUAGE"))
		//{
			readLanguage=true;
		//}
		if(elem.equals("PERIOD"))
		{
			readPeriod=true;
		}
		if(elem.equals("RULE") && readPeriod && readLanguage)
		{
			//A POTENTIAL FASTING RULE HAS BEEN ENCOUNTERED THAT APPLIES FOR TODAY.
			Fast=table.get("Case").toString();
		}
		//THE FOLLOWING SECTION SHOULD BE REMOVED ONCE THERE IS A PROPER RANKING OF DAYS
		/*if (elem.equals("COMMAND"))
		{
		//THIS WILL STORE ALL THE POSSIBLE COMMANDS FOR A GIVEN SITUATION AND ALLOW THE RESULTS TO BE DETEMINED.
		String name = table.get("Name").toString();
		String value=table.get("Value").toString();
		//IF THE GIVEN name OCCURS IN THE information HASHTABLE THAN AUGMENT ITS VALUES.
		if (Information.containsKey(name))
		{
			Vector previous = (Vector)Information.get(name);
			previous.add(value);
			Information.put(name,previous);
		}
		else
		{
			Vector vect = new Vector();
			vect.add(value);
			Information.put(name,vect);
		}
		
		}
		//TO HERE REMOVE
                */

	}

	public void endElement(String elem)
	{
		//if(elem.equals("LANGUAGE"))
		//{
		//	readLanguage=false;
		//}
		if(elem.equals("PERIOD"))
		{
			readPeriod=false;
		}
	}

	public void text(String text)
	{

	}

	private boolean eval(String expression) throws IllegalArgumentException
	{
		return false;
	}
	
}