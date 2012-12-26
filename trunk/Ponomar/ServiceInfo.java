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
THIS MODULE READS THE ServiceRules.XML FILE TO DETERMINE THE CORRECT
ORDER FOR PRIMES ON A GIVEN DAY

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

public class ServiceInfo implements DocHandler
{
	private final static String FileName   = "xml/Commands/ServiceRules.xml";
	private static boolean readPeriod=false;
	private static boolean readLanguage=false;
	private static OrderedHashtable Information;
	
	private LanguagePack Phrases;//=new LanguagePack();
	//GET THE APPROPRIATE FASTING LINES
	//private String[] ServiceNames=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
	//private String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private static OrderedHashtable Service;
	private String Type;
        private Helpers findLanguage;
	private StringOp Analyse=new StringOp();
	public ServiceInfo(String Info, OrderedHashtable dayInfo)
	{
		Type = Info;
                findLanguage=new Helpers(Analyse.dayInfo);
                Analyse.dayInfo=dayInfo;
                Phrases=new LanguagePack(dayInfo);
	}
	
	public OrderedHashtable ServiceRules() //throws IOException
	{
		Service=new OrderedHashtable();;
		Information=new OrderedHashtable();
		//THIS IS A KLUTZ THAT WILL BE REMOVED ONCE THERE IS A PROPER ABILITY TO RANK THE DAY
		//RANK 1 HOLIDAYS
		/*StringOp.dayInfo.put("dRank",1);	//ANY RANK LESS THAN 4 WILL DO
		try
		{
			FileReader frf = new FileReader("Ponomar/xml/Commands/DivineLiturgy.xml");
			QDParser.parse(this, frf);
			int doy=Integer.parseInt(StringOp.dayInfo.get("doy").toString());
			int nday=Integer.parseInt(StringOp.dayInfo.get("nday").toString());
			if(doy == 256 || doy == 358 || doy == 5 ||  nday == 40 || nday == 50 || doy == 217)
			{
				//THESE ARE CLASS 1 HOLIDAYS
				StringOp.dayInfo.put("dRank",8);
			}
			if ((doy == 32 && nday != -48) || doy == 250 || doy == 226 || doy == 324 || doy == 83)
			{
				//THESE ARE CLASS 2 HOLIDAYS
				StringOp.dayInfo.put("dRank",7);
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
				StringOp.dayInfo.put("dRank",4);
			}
						
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		//FINISHED THE KLUTZ
		//ACTUAL PROGRAMME
                 */
                //The above is unnecessary since the days can now be ranked properly.
		//System.out.print("Today's rank is "+StringOp.dayInfo.get("dRank")+"\n");
		try
		{
			BufferedReader frf1 = new BufferedReader(new InputStreamReader(new FileInputStream(findLanguage.langFileFind(Analyse.dayInfo.get("LS").toString(),FileName)), "UTF8"));
			QDParser.parse(this, frf1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;			//THERE WAS AN ERROR IN PROCESSING THE FILES
		}
		//NOW IT IS NECESSARY TO CONVERT THE COMPUTER SPEAK TO HUMAN SPEAK
		return Service;
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
		//if(elem.equals("LANGUAGE"))
		//{
			readLanguage=true;
		//}
		if(elem.equals("PERIOD"))
		{
			readPeriod=true;
		}
		if(elem.equals(Type) && readPeriod && readLanguage)
		{
			//A POTENTIAL ORDER RULE HAS BEEN ENCOUNTERED.
			Enumeration listed = table.keys();
			while (listed.hasMoreElements())
			{
				String nextEle=listed.nextElement().toString();
				
				if(nextEle == null)
				{
					continue;
				}
				Service.put(nextEle,table.get(nextEle).toString());
				
			}
		}
		//THE FOLLOWING SECTION SHOULD BE REMOVED ONCE THERE IS A PROPER RANKING OF DAYS
		if (elem.equals("COMMAND"))
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

	}

	public void endElement(String elem)
	{
		if(elem.equals("LANGUAGE"))
		{
			readLanguage=false;
		}
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