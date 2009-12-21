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
THIS MODULE READ XML FILES THAT CONTAIN A SET OF <CREATE> TAGS THAT SET THE RULES FOR THE CREATION OF
A SERVICE

(C) 2008 YURI SHARDT. ALL RIGHTS RESERVED.
TO START THE READING OF THE SERVICE FILES, CALL startService(FileName)
TO CONTINUE READING, THE SAME SERVICE, BUT WITH POSSIBLY DIFFERENT FILES, CALL readService(FileName)
TO END THE SERVICE READER CALL, closeService();

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

public class Service implements DocHandler
{
	private final static String CommonPrayersFileName   = "Ponomar/xml/Services/CommonPrayers/";   // THE LOCATION OF THE BASIC SERVICE RULES
	private final static String ServiceFileName="Ponomar/xml/Services/";
	public static String Service1;
	private static String text;
	private static boolean read=false;
	private String filename;
	private int lineNumber;
	private LanguagePack Text=new LanguagePack();
	private String[] ServiceNames=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
	//private String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private String Who;
	private String What;
	private String RedFirst;
	private String NewLine;
	private String Times;
	private int Header;
	private String CommandB;
	private String WhoLast="";
	private String Command;
	private int count=-1;
	private String[] OldText=new String[10];
	private String[] parsedBible;
		
	public String startService(String FileName)
	{
		WhoLast="";
		count=-1;
		//Service1="";
		return readService(FileName);
	}
	public String readService(String FileName) //throws IOException
	{
		Service1="";
		
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(FileName), "UTF8"));
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;			//THERE WAS AN ERROR IN PROCESSING THE FILES
		}
		return Service1;
	}
	public String closeService()
	{
		//THIS CLOSES THE SERVICE TEXT APPROPRIATELY
		WhoLast="";
		count=-1;
		return "</p>";
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
		if(elem.equals("LANGUAGE"))
		{
			read=true;
		}
		if(elem.equals("GET") && read)
		{
			//WE NEED TO GET ANOTHER SERVICE OR PART THEREOF.
			read=false;
			String GetFile=table.get("File").toString();
			count++;
			OldText[count]=Service1;
			readService(ServiceFileName+GetFile+".xml");
			int NullCheck=0;
			if(table.get("Null")!=null)
			{
				NullCheck=Integer.parseInt(table.get("Null").toString());
			}
			
			if(NullCheck == 0 || (NullCheck == 1 && Service1 != null))
			{
				Service1=OldText[count]+Service1;
			}
			else
			{
				Service1=OldText[count];
			}
			count--;
			read=true;
		}
		if(elem.equals("TITLE") && read)
		{
			//WE ARE DEALING WITH THE TITLE OF THE SERVICE. IT CAN HAVE 3 PARTS: THE TITLE ITSELF, THE SOURCE FOR 
			//SERVICE, AND SOME ADDITIONAL COMMENTS.
			String Title=table.get("Header").toString();
			ReadText textGet1=new ReadText();	
			String text4=textGet1.readText(ServiceFileName+"Text/"+Title+".xml");
			if(text4 != null)
			{
				Service1+="<title>Ponomar: "+text4+"</title>";
			}
			else
			{
				Service1+="<Font color=\"red\"><B>"+ServiceNames[0]+"</B><BR>";
			}
			Title=table.get("Value").toString();
			text4=textGet1.readText(ServiceFileName+"Text/"+Title+".xml");
			if(text4 != null)
			{
				Service1+="<Font color=\"red\"><center><B><h1> "+text4+"</h1></B></center><BR> ";
			}
			else
			{
				Service1+="<Font color=\"red\"><B>"+ServiceNames[0]+"</B><BR>";
			}
						
			if(table.get("Source") != null)
			{
				String Source=table.get("Source").toString();
				text4=textGet1.readText(ServiceFileName+"Text/"+Source+".xml");
				Service1+="<I><small>"+text4+"</small></I><BR>";
			}
			
			if(table.get("Comment") != null)
			{
				String Comment=table.get("Comment").toString();
				text4=textGet1.readText(ServiceFileName+"Text/"+Comment+".xml");
				if(text4 != null)
				{
					Service1+="<I><small>"+text4+"</small></I><BR>";
				}
			}
			Service1+="</Font>";
		}
		if(elem.equals("TEXT") && read)
		{
			//HERE IS IT IS ASSUMED THAT THE TEXT AND THE HEADER HAVE BEEN CREATED PROGRAMMATICALLY AND HAVE BEEN ASSIGNED FIXED VALUES
			//APPROPRIATE TO THE GIVEN LANGUAGE. THUS, BOTH what AND HeaderText ARE ASSUMED TO CONTAIN TEXT
			What=table.get("What").toString();
			String HeaderText="";
			if(table.get("Header") != null)
			{
				HeaderText=table.get("Header").toString();
				Header=1;				
			}
			readIncidentals(table);
			Service1+=Implement(Header,HeaderText,What)+"\n";	
		}
		if(elem.equals("BIBLE") && read)
		{
			String What2=new String();
			if(table.get("Verses") !=null)
			{
				//ALLOWING THE BIBLE TO BE READ Y.S. 2008/12/11 n.s.
				//THE FORMAT FOR A BIBLE STATEMENT IS Bible="Book_Chapter:VerseStart-VerseEnd" or ="Book_Chapter:Verse,Chapter:Verse" or ="Book_Chapter"
				//THE BOOK COULD BE OF THE FORM II_NAME_Chapter:VerseStart-VerseEnd,Verse,Verse,Chapter:Verse
				String Reading1=table.get("Verses").toString();
				int k=Reading1.lastIndexOf("_");
				Bible reader=new Bible();
				parsedBible=reader.getText(Reading1.substring(0,k),Reading1.substring(k+1),false);
				What2=parsedBible[0].substring(1);											
			}
			if(table.get("getReading") != null)
			{
				//THIS ALLOWS THE READING HEADER FOR THE GIVEN SELECTION TO BE OBTAINED, THAT IS, "A reading from the Book of...."
				Bible reader=new Bible();
				What2=reader.getIntro(table.get("getReading").toString());
			}
			int Stars2=-1;	 		//THIS VARIABLE CONSIDERS WHAT TO DO WITH ANY POSSIBLE 2 STARS IN THE TEXT "**"
			int StarsBible=parsedBible[1].toString().indexOf("**");	//IF THERE ARE NO ** TO BE FOUND IN THE TEXT THEN THERE IS NO NEED TO CONTINUE!
			if((table.get("2Stars") != null) && StarsBible != -1)
			{
				Stars2=Integer.parseInt(table.get("2Stars").toString());
				if(Stars2==1)
				{
					//USE THE 2 STARS DATA AS AN ADDITIONAL HEADER
					parsedBible[2]=parsedBible[1].substring(3)+"<BR>"+parsedBible[2];	
					
				}
				if(Stars2==2)
				{
					int k=What2.indexOf("**");
					String[] splitString=parsedBible[1].split("<BR>");
					int a1=splitString[0].indexOf("\"");
					int a2=splitString[0].substring(a1+1).indexOf("\"");
					String textNew=new String();
					if(a1 != -1)
					{
						textNew=parsedBible[1].substring(a1+1,a2+a1+1).replace("...",""); 		//3 separate dots
						textNew=textNew.replace(" ",""); 								//The 3 dots combined as a single symbol
					}
										
					What2=textNew+" "+What2.substring(k+2);
				}				
			}
			//REMOVE THE 2 STARS FROM THE ORIGINAL READING
			int Stars=What2.indexOf("**");

			while(Stars != -1)
			{
				//System.out.println(What2);
				if(Stars==0)
				{
					What2=What2.substring(3);
				}
				else
				{
					What2=What2.substring(0,Stars-1)+What2.substring(Stars+2);
				}
				Stars=What2.indexOf("**");
			}
			
			if(table.get("Header") != null)
			{
				if(table.get("Header").toString().equals("1"))
				{
					Header= Integer.parseInt(table.get("Header").toString());								
				}
				else
				{
					Header=0;
				}
			}
			else
			{
				Header=0;
			}
			readIncidentals(table);			
			Service1+=Implement(Header,parsedBible[2],What2)+"\n";		
			read=true;
		}
		if (elem.equals("CREATE") && read)
		{
			String What2="";
			if(table.get("What") != null)
			{
				What=table.get("What").toString();
			}
			else
			{
				What=null;
			}
			String text2="";
			if(table.get("Header") != null)
			{
				if(table.get("Header").toString().equals("1"))
				{
					Header= Integer.parseInt(table.get("Header").toString());
										
				}
				else
				{
					Header=0;
				}
			}
			else
			{
				Header=0;
			}
			readIncidentals(table);
			//System.out.println(What);
			ReadText textGet=new ReadText();
			if(What != null)
			{
				What2 = textGet.readText(CommonPrayersFileName+What+".xml");
				if (What2 == null)
				{
					Service1+= "<BR><Font color=\"red\"/>" + ServiceNames[1] +" "+CommonPrayersFileName+What+".xml</FONT><BR>";
					return;
				}
			}
				
			Service1+=Implement(Header,textGet.readHeader(CommonPrayersFileName+What+".xml"),What2)+"\n";		
			read=true;
		}
		

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
	private void readIncidentals(Hashtable table)
	{
			//THIS READS THE COMMON LABELS FOR CREATE, BIBLE, AND TEXT TAGS.
			Who = table.get("Who").toString();
			if(table.get("CommandB") != null)
			{
				CommandB= table.get("CommandB").toString();	
			}
			else
			{
				CommandB=null;
			}
			if(table.get("Command") != null)
			{
				Command= table.get("Command").toString();	
			}
			else
			{
				Command=null;
			}
				if( table.get("RedFirst") != null)
			{
				RedFirst = table.get("RedFirst").toString();				
			}
			else
			{
				RedFirst=null;
			}
			if( table.get("NewLine") != null)
			{
				NewLine =  table.get("NewLine").toString();
			}
			else
			{
				NewLine=null;
			}
			if(table.get("Times") != null)
			{
				Times= table.get("Times").toString();	
			}
			else
			{
				Times=null;
			}
			
	}
	private String Implement(int Header,String text4, String What)
	{
		String text2=What;
		ReadText textGet=new ReadText();
		
		if(Command != null)
		{
			String text3=textGet.readText(ServiceFileName+"Command/"+Command+".xml");
			
			if(text3 != null)
			{
				text2=text2+" <I><Font color=\"red\"> "+text3+"</Font></I> ";
			}
			else
			{
				text2=text2+" <I><Font color=\"red\">" +ServiceNames[2]+"</Font></I>";
			}
		}
		if(RedFirst != null)
		{
			if(RedFirst.equals("1"))
			{
				text2="<B><FONT color=\"red\">"+text2.charAt(0)+"</FONT></B>"+text2.substring(1);
			}
		}
		if(Times != null)
		{
			int Time= Integer.parseInt(Times);
			String textR=text2;
			for(int i=1;i < Time;i++)
			{
				text2+="<BR>"+textR;
			}
			
		}
		if(CommandB != null)
		{
			String text3=textGet.readText(ServiceFileName+"Command/"+CommandB+".xml");
				
			if(text3 != null)
			{
				text2=" <I><Font color=\"red\"> "+text3+"</Font></I> "+text2;
			}
			else
			{
				text2=" <I><Font color=\"red\">"+ServiceNames[2] + "</Font></I>"+text2;
			}
		}
			
		if(Header != 0)
		{
			if(text4 != null)
			{
				text4="<BR><BR><center><B><Font color=\"red\"> "+text4+"</Font></B></center><BR> ";
			}
			else
			{
				text4="<BR><B><Font color=\"red\">" +ServiceNames[3]+" </Font></B><BR>";
			}
			WhoLast="";
							
		}
		else
		{
			text4="";
		}
		if(!Who.equals(WhoLast) || WhoLast.equals(""))
		{
		//THERE HAS BEEN A CHANGE IN WHO IS READING THE SERVICE READER TO PRIEST OR SOMETHING SIMILAR
		//THERE IS A NEED TO AFFIX THE NEW READER.
		//THIS WILL ENTAIL BY DEFAULT A NEW LINE
			if(!Who.equals(""))
			{
				String textWho = textGet.readText(CommonPrayersFileName+Who+".xml");
				text2="<p style=\"margin-left:.5in;text-indent:-.5in\"><B><FONT color=\"red\">"+textWho+"</FONT></B> "+text2;
				if(!WhoLast.equals(""))
				{
					text2="</p>"+text4+text2;	
				}
				else
				{
					text2=text4+text2;
				}
			}
			else
			{
				text2="</p>"+text4+text2;
			}
							
			WhoLast=Who;	
		}
		else
		{
		//THE READER IS THE SAME. CHECK IF A NEW LINE IS DESIRED
			
				
				if(NewLine != null)
				{
					if(NewLine.equals("1"))
					{
						text2="<BR><BR>"+text4+text2;
					}
					else
					{
						text2=text4+text2;
					}
				
				
				}
				else
				{
					text2=text4+text2;
				}
			
			
		}
		return text2;
	}

	private boolean eval(String expression) throws IllegalArgumentException
	{
		return false;
	}
	
}
