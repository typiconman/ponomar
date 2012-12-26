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
	private final static String Location  = "Ponomar/xml/";   // THE LOCATION OF THE DAY FILES
	private static boolean read=false;
	private String filename;
	private int lineNumber;
	//private LanguagePack Text=new LanguagePack();
	//private String[] ServiceNames=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
	//private String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private OrderedHashtable Commemorations;
	private Vector CommemorationList;
	private String[] Rank;
	private String[] Icons;
	private int Number;
        private StringOp Analyse=new StringOp();
	//private OrderedHashtable readings;
	//private OrderedHashtable grammar;
	//private OrderedHashtable variable;
	//private String textR;
	
	protected Days()
	{
		//THIS OPENS A NEW INSTANCE OF THE CLASS AND RESETS EVERYTHING TO DEFAULT VALUES

		reset();			
	}
	protected Days(String FileName,OrderedHashtable dayInfo)
	{
		//THIS READS A GIVEN FILE AND RESETS EVERYTHING
		reset();
		//THE FILENAME MUST CONTAIN THE APPROPRIATE FOLDER AND EXTENSION (.xml)
                Analyse.dayInfo=dayInfo;
		readDay(FileName);		
	}
	
	public void readDay(String FileName) //throws IOException
	{
		//reset Rank, which can potentially have been changed
		resetVars();
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(Location+FileName), "UTF8"));
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
			
			if (Analyse.evalbool(table.get("Cmd").toString()) == false)
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
			String IdAll=table.get("Id").toString();
			String[] Id=IdAll.split(",");
			Number++;
			if(name.equals(""))
			{
				//THERE IS ONLY A LIST OF POSSIBLE ID'S THAT NEED TO BE READ
				for(int i=0;i<Id.length;i++)
				{
					Commemoration Saint=new Commemoration(Id[i],Analyse.dayInfo);
					CommemorationList.add(Saint);
				}
			}
			else
			{
				//THERE IS A NAME ASSIGNED FOR THE GIVEN DAY
				if(Id[0].equals(""))
				{
					//System.out.println("Hello World : "+name);
					Commemoration Saint= new Commemoration(name,new OrderedHashtable(),new OrderedHashtable());
					CommemorationList.add(Saint);
				}
				else
				{
					//AN ID NUMBER IS SPECIFIED, SKIP THE NAME AND READ THE ACTUAL ID FILE
					//Commemoration Saint=new Commemoration();
					//CommemorationList.add(Saint.Commemoration(Id));
					Commemoration Saint=new Commemoration(Id[0],Analyse.dayInfo);
					CommemorationList.add(Saint);
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
		Commemorations=new OrderedHashtable();
		CommemorationList=new Vector();
		resetVars();
	}
	private void resetVars()
	{
		Rank=new String[3];
		Rank[0]=null;
		Rank[1]=null;
		Rank[2]=null;
		Icons=new String[2];
		Number=0;
	}
	public String[] getRank()
	{
		//DETERMINES THE RANK FOR A GIVEN DAY
		//THE FOLLOWING IS RETURNED: THE FIRST ENTRY CONTAINS THE RANK FOR THE DAY BETWEEN 0 (EASTER) TO 8 (SIMPLE SERVICE)
		//THE SECOND ENTRY CONTAINS ANY COMMENTS ABOUT PRE- (B)/POST-(A) FEASTS, WHICH WILL BE RETURNED AS FeastRank_B/A.
		//IF THERE IS NO PRE-/POSTFEAST, THEN THAT ENTRY IS NULL.
		//THE THIRD ENTRY LISTS ALL OTHER SUBSIDIARY FEASTS THAT OCCUR WITH RANK GREATER THAN 3, SINCE THERE MAY BE A NEED TO KNOW THIS 
		if(Rank[0]==null)
		{
			Rank=new String[3];
			Rank[0]="10";
			Rank[1]=null;
			Rank[2]=null;
			for(int i=0;i<CommemorationList.size();i++)
			{
				Commemoration Saint=(Commemoration)CommemorationList.get(i);
				String RankSaint=Saint.getRank();
				int k=RankSaint.indexOf("_");
				if(k == -1)
				{
					//THIS IS NOT A SPECIAL SEASON
					int RankDay=Integer.parseInt(RankSaint);
					if(RankDay<Integer.parseInt(Rank[0]) && RankDay != -1)
					{
						Rank[0]=RankSaint;
					}
					if(Integer.parseInt(RankSaint)>=2)
					{
						if(Rank[2]==null)
						{
							Rank[2]=RankSaint;
						}
						else
						{
							Rank[2]+=","+RankSaint;
						}
					}
				}
				else
				{
					//THIS IS A SPECIAL SEASON
					Rank[1]=RankSaint;
				}			
			}
			if(Rank[0].equals("10"))
			{
				Rank[0]="-1";
			}
		}
		return Rank;
		
	}
	
	public void order()
	{
		//THIS WILL ORDER THE COMMEMORATINS LISTED BASED ON THEIR RANK
		//HOW TO IMPLEMENT THIS QUICKLY AND EFFICIENTLY IS A QUESTION
	}
	
	public String[] getIcons()
	{
		//THIS WILL TAKE AN ORDERED LIST AND CONVERT ALL THE ICONS (SINCE A SINGLE COMMEMORATION CAN HAVE MORE THAN ONE ICON) INTO A LIST OF ICONS
		if(Icons[0]==null)
		{
			for(int i=0;i<CommemorationList.size();i++)
			{
				Commemoration Saint=(Commemoration)CommemorationList.get(i);
				String IconTest=Saint.getIcon();
				String Name=Saint.getGrammar("");		//THE <NAME> TAG IS DESIRED
				if(IconTest != null)
				{
					//DETERMINE IF THERE ARE MULTIPLE ICONS FOR THE GIVEN DAY
					String[] splits=IconTest.split(",");
					String NameList=Name;
					if(splits.length>1)
					{
						//THERE ARE MORE THAN ONE ICONS FOR A GIVEN DAY
						for(i=2;i<splits.length;i++)
						{
							NameList+="%"+Name;
						}
					}
					if(Icons[0]==null)
					{
						Icons[0]=IconTest;
						Icons[1]=NameList;
					}
					else
					{
						Icons[0]+="%"+IconTest;
						Icons[1]+="%"+NameList;
					}
				}
			}
		}
		return Icons;
	}
	public String getGrammar(int value, String case1)
	{
		Commemoration Saint=(Commemoration)CommemorationList.get(value);
		return Saint.getGrammar(case1);
	}
	public int getNumber()
	{
		//returns the number of commemorations on a given day
		return Number;
	}
	public String getID(int value)
	{
		Commemoration Saint=(Commemoration)CommemorationList.get(value);
		return Saint.getID();
	}
	public String getCycle(int value)
	{
		Commemoration Saint=(Commemoration)CommemorationList.get(value);
		return Saint.getCycle();
	}
	
}