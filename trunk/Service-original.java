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
	private boolean BibleFlag=false;
	
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
		BibleFlag=false;
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
			text+=(String)table.get("Value");
			
		}
		
		if (elem.equals("CREATE") && read)
		{
			Who = table.get("Who").toString();
			if(table.get("What") != null)
			{
				What=table.get("What").toString();
			}
			else
			{
				What=null;
			}
			String text2="";
			boolean Flag1=false;
			if(table.get("Bible") !=null)
			{
				//ALLOWING THE BIBLE TO BE READ Y.S. 2008/12/11 n.s.
				//THE FORMAT FOR A BIBLE STATEMENT IS Bible="Book_Chapter:VerseStart-VerseEnd" or ="Book_Chapter:Verse,Chapter:Verse" or ="Book_Chapter"
				//THE BOOK COULD BE OF THE FORM II_NAME_Chapter:VerseStart-VerseEnd,Verse,Verse,Chapter:Verse
				String Reading1=table.get("Bible").toString();
				int k=Reading1.lastIndexOf("_");
				Bible reader=new Bible();
				parsedBible=reader.getText(Reading1.substring(0,k),Reading1.substring(k+1),false);
				text2=parsedBible[0].substring(1);
				BibleFlag=true;
				Flag1=true;
				//What=call the Bible reader with the desired passage and return it as what text.
			}
			if(table.get("getReading") != null)
			{
				//THIS ALLOWS THE READING HEADER FOR THE GIVEN SELECTION TO BE OBTAINED, THAT IS, "A reading from the Book of...."
				Bible reader=new Bible();
				text2=reader.getIntro(table.get("getReading").toString());
				Flag1=true;
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
			if(table.get("Header") != null)
			{
				if(table.get("Header").toString().equals("1"))
				{
					//ALLOWING THE BIBLE TO BE READ Y.S. 2008/12/11 n.s.
					if(!BibleFlag)
					{
						Header= Integer.parseInt(table.get("Header").toString());
					}					
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
			//System.out.println(What);
			ReadText textGet=new ReadText();	
			
			if(!Flag1)
			{
				text2 = textGet.readText(CommonPrayersFileName+What+".xml");
			
				if (text2 == null)
				{
					Service1+="<BR><Font color=\"red\"/>" + ServiceNames[1] +" "+CommonPrayersFileName+What+".xml</FONT><BR>";
					return;
				}
			}
						
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
			
			String text4=new String();
			text4="";
			if(Header != 0 || BibleFlag)
			{
				//ALLOWING THE BIBLE TO BE READ Y.S. 2008/12/11 n.s.
				if(!BibleFlag)
				{
					text4=textGet.readHeader(CommonPrayersFileName+What+".xml");
				}
				else
				{
					text4=parsedBible[2];
				}
				
				if(text4 != null)
				{
					text4="<BR><BR><BR><center><B><Font color=\"red\"> "+text4+"</Font></B></center><BR> ";
				}
				else
				{
					text4="<BR><B><Font color=\"red\">" +ServiceNames[3]+" </Font></B><BR>";
				}
				WhoLast="";
							
			}
			if(!Who.equals(WhoLast) || WhoLast.equals(""))
			{
			//THERE HAS BEEN A CHANGE IN WHO IS READING THE SERVICE READER TO PRIEST OR SOMETHING SIMILAR
			//THERE IS A NEED TO AFFIX THE NEW READER.
			//THIS WILL ENTAIL BY DEFAULT A NEW LINE
				if(!Who.equals(""))
				{
					String textWho = textGet.readText(CommonPrayersFileName+Who+".xml");
					//text2="<BR><dt><B><FONT color=\"red\">"+textWho+"</FONT></B> "+text2;	
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
			Service1+=text2+"\n";
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

	private boolean eval(String expression) throws IllegalArgumentException
	{
		return false;
	}
	
}