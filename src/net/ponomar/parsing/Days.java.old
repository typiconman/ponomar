package Ponomar.parsing;

import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import Ponomar.utility.OrderedHashtable;
import Ponomar.utility.StringOp;
/***********************************************************************
THIS MODULE READS XML FILES THAT CONTAIN THE <DAY> TYPE 
AND STORES THE INFORMATION IN A MANNER USUABLE BY OTHER COMPONENTS
OF PONOMAR.

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

public class Days implements DocHandler
{
	private static final String LOCATION  = "Ponomar/xml/";   // THE LOCATION OF THE DAY FILES
	private static boolean read=false;
	private String filename;
	private int lineNumber;
	//private LanguagePack Text=new LanguagePack();
	//private String[] ServiceNames=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
	//private String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private OrderedHashtable commemorations;
	private Vector<Commemoration> commemorationList;
	private String[] rank;
	private String[] icons;
	private int number;
        private StringOp analyse=new StringOp();
	//private OrderedHashtable readings;
	//private OrderedHashtable grammar;
	//private OrderedHashtable variable;
	//private String textR;
	
	protected Days()
	{
		//THIS OPENS A NEW INSTANCE OF THE CLASS AND RESETS EVERYTHING TO DEFAULT VALUES

		reset();			
	}
	protected Days(String fileName,OrderedHashtable dayInfo)
	{
		//THIS READS A GIVEN FILE AND RESETS EVERYTHING
		reset();
		//THE FILENAME MUST CONTAIN THE APPROPRIATE FOLDER AND EXTENSION (.xml)
                analyse.setDayInfo(dayInfo);
		readDay(fileName);		
	}
	
	public void readDay(String fileName) //throws IOException
	{
		//reset Rank, which can potentially have been changed
		resetVars();
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(LOCATION+fileName), StandardCharsets.UTF_8));
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			e.printStackTrace();			
		}		
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
		// IT WOULD BE VERY RARE IN THIS CASE
		if (table.get("Cmd") != null)
		{
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
			
			if (analyse.evalbool(table.get("Cmd").toString()) == false)
			{
				return;
			}
		}
		if(elem.equals("LANGUAGE"))
		{
			read=true;
		}
		
		if(elem.equals("SAINT") && read)
		{
			//THERE ARE 2 POSSIBILITIES: EITHER THERE IS A NAME GIVEN (OLD FORMAT) OR AN ID IS SPECIFIED (NEW FORMAT)
			String name=table.get("Name").toString();
			if(table.get("Id")==null)
			{
				return;
			}
			String idAll=table.get("Id").toString();
			String[] id=idAll.split(",");
			number++;
			if(name.equals(""))
			{
				//THERE IS ONLY A LIST OF POSSIBLE ID'S THAT NEED TO BE READ
				for(int i=0;i<id.length;i++)
				{
					Commemoration saint=new Commemoration(id[i],analyse.getDayInfo());
					commemorationList.add(saint);
				}
			}
			else
			{
				//THERE IS A NAME ASSIGNED FOR THE GIVEN DAY
				if(id[0].equals(""))
				{
					//System.out.println("Hello World : "+name);
					Commemoration saint= new Commemoration(name,new OrderedHashtable(),new OrderedHashtable());
					commemorationList.add(saint);
				}
				else
				{
					//AN ID NUMBER IS SPECIFIED, SKIP THE NAME AND READ THE ACTUAL ID FILE
					//Commemoration Saint=new Commemoration();
					//CommemorationList.add(Saint.Commemoration(Id));
					Commemoration saint=new Commemoration(id[0],analyse.getDayInfo());
					commemorationList.add(saint);
				}
			}
		}
	}

	public void endElement(String elem)
	{
		if(elem.equals("LANGUAGE"))
		{
			read=false;
		}		
	}

	public void text(String text)
	{		
	}
	
	public void reset()
	{
		//RESETS THE FILE TO A BLANK FILE
		commemorations=new OrderedHashtable();
		commemorationList=new Vector<>();
		resetVars();
	}
	private void resetVars()
	{
		rank=new String[3];
		rank[0]=null;
		rank[1]=null;
		rank[2]=null;
		icons=new String[2];
		number=0;
	}
	public String[] getRank()
	{
		//DETERMINES THE RANK FOR A GIVEN DAY
		//THE FOLLOWING IS RETURNED: THE FIRST ENTRY CONTAINS THE RANK FOR THE DAY BETWEEN 0 (EASTER) TO 8 (SIMPLE SERVICE)
		//THE SECOND ENTRY CONTAINS ANY COMMENTS ABOUT PRE- (B)/POST-(A) FEASTS, WHICH WILL BE RETURNED AS FeastRank_B/A.
		//IF THERE IS NO PRE-/POSTFEAST, THEN THAT ENTRY IS NULL.
		//THE THIRD ENTRY LISTS ALL OTHER SUBSIDIARY FEASTS THAT OCCUR WITH RANK GREATER THAN 3, SINCE THERE MAY BE A NEED TO KNOW THIS 
		if(rank[0]==null)
		{
			rank=new String[3];
			rank[0]="10";
			rank[1]=null;
			rank[2]=null;
			for(int i=0;i<commemorationList.size();i++)
			{
				Commemoration saint=commemorationList.get(i);
				String rankSaint=saint.getRank();
				int k=rankSaint.indexOf('_');
				if(k == -1)
				{
					//THIS IS NOT A SPECIAL SEASON
					int rankDay=Integer.parseInt(rankSaint);
					if(rankDay<Integer.parseInt(rank[0]) && rankDay != -1)
					{
						rank[0]=rankSaint;
					}
					if(Integer.parseInt(rankSaint)>=2)
					{
						if(rank[2]==null)
						{
							rank[2]=rankSaint;
						}
						else
						{
							rank[2]+=","+rankSaint;
						}
					}
				}
				else
				{
					//THIS IS A SPECIAL SEASON
					rank[1]=rankSaint;
				}			
			}
			if(rank[0].equals("10"))
			{
				rank[0]="-1";
			}
		}
		return rank;
		
	}
	
	public void order()
	{
		//THIS WILL ORDER THE COMMEMORATIONS LISTED BASED ON THEIR RANK
		//HOW TO IMPLEMENT THIS QUICKLY AND EFFICIENTLY IS A QUESTION
	}
	
	public String[] getIcons()
	{
		//THIS WILL TAKE AN ORDERED LIST AND CONVERT ALL THE ICONS (SINCE A SINGLE COMMEMORATION CAN HAVE MORE THAN ONE ICON) INTO A LIST OF ICONS
		if(icons[0]==null)
		{
			for(int i=0;i<commemorationList.size();i++)
			{
				Commemoration saint=commemorationList.get(i);
				String iconTest=saint.getIcon();
				String name=saint.getGrammar("");		//THE <NAME> TAG IS DESIRED
				if(iconTest != null)
				{
					//DETERMINE IF THERE ARE MULTIPLE ICONS FOR THE GIVEN DAY
					String[] splits=iconTest.split(",");
					String nameList=name;
					if(splits.length>1)
					{
						//THERE ARE MORE THAN ONE ICONS FOR A GIVEN DAY
						for(i=2;i<splits.length;i++)
						{
							nameList+="%"+name;
						}
					}
					if(icons[0]==null)
					{
						icons[0]=iconTest;
						icons[1]=nameList;
					}
					else
					{
						icons[0]+="%"+iconTest;
						icons[1]+="%"+nameList;
					}
				}
			}
		}
		return icons;
	}
	public String getGrammar(int value, String case1)
	{
		Commemoration saint=commemorationList.get(value);
		return saint.getGrammar(case1);
	}
	public int getNumber()
	{
		//returns the number of commemorations on a given day
		return number;
	}
	public String getID(int value)
	{
		Commemoration saint=commemorationList.get(value);
		return saint.getID();
	}
	public String getCycle(int value)
	{
		Commemoration saint=commemorationList.get(value);
		return saint.getCycle();
	}
	
}