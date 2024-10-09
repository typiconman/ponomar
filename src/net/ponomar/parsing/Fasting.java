package net.ponomar.parsing;

import net.ponomar.internationalization.LanguagePack;
import net.ponomar.parsing.utility.DocHandler;
import net.ponomar.parsing.utility.QDParser;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 
 
import net.ponomar.utility.StringOp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import java.util.LinkedHashMap;
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
	private static final String FASTING_FILE   = Constants.COMMANDS + "Fasting.xml";
	private static boolean readPeriod=false;
	private static boolean readLanguage=false;
	private static LinkedHashMap<String, Object> information;
	
	private LanguagePack phrases;
	//GET THE APPROPRIATE FASTING LINES
	//private String[] ServiceNames=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
	//private String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private static String fast;
	private static Helpers helper;
        private static StringOp analyse=new StringOp();
        public Fasting(LinkedHashMap<String, Object> dayInfo){
            analyse.setDayInfo(dayInfo);
            phrases=new LanguagePack(dayInfo);
        }
	public String fastRules() //throws IOException
	{
            
            fast="";
		helper=new Helpers(analyse.getDayInfo());
		information=new LinkedHashMap<>();
		//THIS IS A KLUTZ THAT WILL BE REMOVED ONCE THERE IS A PROPER ABILITY TO RANK THE DAY
		//RANK 1 HOLIDAYS
                //I have removed the klutz, as dRank has been properly implemented! Y.S 2010/02/02 n.s.
		//StringOp.dayInfo.put("dRank",1);	//ANY RANK LESS THAN 4 WILL DO
		/*try
		{
			FileReader frf = new FileReader(Constants.DIVINE_LITURGY);
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
			ArrayList vect = (ArrayList) Information.get("Class3Transfers");
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
			BufferedReader frf1 = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(analyse.getDayInfo().get("LS").toString(),FASTING_FILE)), StandardCharsets.UTF_8));
			QDParser.parse(this, frf1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;			//THERE WAS AN ERROR IN PROCESSING THE FILES
		}
		//NOW IT IS NECESSARY TO CONVERT THE COMPUTER SPEAK TO HUMAN SPEAK
		return convert(fast);
	}
	public String convert(String fast)
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
		
		String[] fastNames= phrases.obtainValues(phrases.getPhrases().get("Fasts"));
				
		if(fast.equals("0000000"))
		{
			return fastNames[4]+ fastNames[1]+fastNames[6]+ fastNames[7];
		}
		if(fast.equals("0000001"))
		{
			return  fastNames[4]+fastNames[1]+fastNames[6]+ fastNames[8];
		}
		if(fast.equals("0000011"))
		{
			return  fastNames[4]+fastNames[1]+fastNames[6]+ fastNames[9];
		}
		if(fast.equals("0000111"))
		{
			return  fastNames[4]+fastNames[2];
		}
		if(fast.equals("0001111"))
		{
			return fastNames[4]+fastNames[10];
		}
		if(fast.equals("0011111"))
		{
			return  fastNames[4]+fastNames[3];
		}
		if(fast.equals("0111111"))
		{
			return fastNames[4]+fastNames[11];
		}
		if(fast.equals("1111111"))
		{
			return  fastNames[4]+fastNames[0];
		}
		if(fast.equals("0000010"))
		{
			return fastNames[4]+fastNames[12];
		}
		//NONE OF THE PREDEFINED SEQUENCES WERE ENCOUNTERED.
		//PARSE IT ELEMENT BY ELEMENT!
		String[] item = new String[] {fastNames[13],fastNames[14],fastNames[15],fastNames[16],fastNames[17],fastNames[18],fastNames[19]};
		String[] permitted=new String[7];
		String[] forbidden=new String[7];
		int permit=0;
		int forbid=0;
		for(int i=0;i < 7;i++)
		{
			if(fast.substring(i,i+1).equals("1"))
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
		StringBuilder output= new StringBuilder(fastNames[27] + " ");
		for(int i=0;i < permit;i++)
		{
			output.append(permitted[i]);
			if(i<permit-1)
			{
				output.append(", ");
			}
			if(i==permit-2)
			{
				output.append(" ").append(fastNames[25]).append(" ");
			}
		}
		if(permit==1)
		{
			output.append(" ").append(fastNames[20]).append(" ").append(fastNames[23]);
		}
		else if(permit==2)
		{
			//FOR THOSES SLAVIC LANGUAGES WITH THE DUAL
			output.append(" ").append(fastNames[21]).append(" ").append(fastNames[23]);
		}
		else
		{
			output.append(" ").append(fastNames[22]).append(" ").append(fastNames[23]);
		}
		output.append(fastNames[26]).append(" ");
		for(int i=0;i < forbid;i++)
		{
			output.append(forbidden[i]);
			if(i<forbid-1)
			{
				output.append(", ");
			}
			if(i==forbid-2)
			{
				output.append(" ").append(fastNames[25]).append(" ");
			}
		}
		if(forbid==1)
		{
			output.append(output.append(" ").append(fastNames[20]).append(" ").append(fastNames[24]));
		}
		else if(forbid==2)
		{
			//FOR THOSES SLAVIC LANGUAGES WITH THE DUAL
			output.append(" ").append(fastNames[21]).append(" ").append(fastNames[24]);
		}
		else
		{
			output.append(" ").append(fastNames[22]).append(" ").append(fastNames[24]);
		}
		return fastNames[4]+" " +output;
	}
				
	public void startDocument()
	{

	}

	public void endDocument()
	{

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
			fast=table.get("Case");
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
			ArrayList previous = (ArrayList)Information.get(name);
			previous.add(value);
			Information.put(name,previous);
		}
		else
		{
			ArrayList vect = new ArrayList();
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