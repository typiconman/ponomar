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

(C) 2008, 2009, 2011 YURI SHARDT. ALL RIGHTS RESERVED.
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
	private final static String CommonPrayersFileName   = "xml/Services/CommonPrayers/";   // THE LOCATION OF THE BASIC SERVICE RULES
	private final static String ServiceFileName="xml/Services/";
	public static String Service1;
	private static String text;
	private static boolean read=false;
	private String filename;
	private int lineNumber;
	private LanguagePack Text;//=new LanguagePack();
	private String[] ServiceNames;//=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
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
	private String textTimes;
        private String Style;
        private String Header1;
        private Helpers findLanguage;
        private StringOp Analyse=new StringOp();
	//private Font CurrentFont=new Font((String)StringOp.dayInfo.get("FontFaceM"),Font.PLAIN,Integer.parseInt((String)StringOp.dayInfo.get("FontSizeM")));
        public Service (OrderedHashtable dayInfo){
            Analyse.dayInfo=dayInfo;
                Text=new LanguagePack(dayInfo);
                ServiceNames=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
        }
        public String startService(String FileName)
	{
		findLanguage=new Helpers(Analyse.dayInfo);
                
                WhoLast="";
		count=-1;
		//Service1="";
                Header1="<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">\n<head>\n";//<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">\n";
                Style="<style type=\"text/css\">\nrubric {color:red;font-weight:bold}\np {margin-left:.5in;text-indent:-.5in}\nh1 {color:red;font-weight:bold;text-align:center}\ncomment {color:red;font-size:50%;font-style:italic}\ncommand {color:red;font-style:italic}\nh2 {color:red;font-size:110%;text-align:center}\n";

                /*int LangCode=Integer.parseInt(StringOp.dayInfo.get("LS").toString());
                                if (LangCode==2 || LangCode==3 ){
                                    Style=Style+"body {font-family:\"Hirmos Ponomar\";font-size:18pt}\n";
                                    //System.out.println("Added Font");
                                   }
                                else{
                                    Style=Style+"body {font-size:12pt}\n";
                                }*/
                String DisplayFont=(String)Text.Phrases.get("FontFaceL");
                String DisplaySize=(String)Text.Phrases.get("FontSizeL");
                
                Font value1 = (Font)UIManager.get ("Menu.font");
                if (DisplaySize == null || DisplaySize.equals(""))
                {
                    DisplaySize=Integer.toString(value1.getSize());
                }
                if (DisplayFont == null || DisplayFont.equals(""))
                {
                    DisplayFont=value1.getFontName();
                }
                DisplaySize=Integer.toString(Math.max(Integer.parseInt(DisplaySize), value1.getSize())); //If the default user's font size is larger than the required there is not need to change it.
                //The specified fonts sizes are the mininum required.
                Style+="body {font-family:"+DisplayFont+";font-size:"+DisplaySize+"}\n</head>";
		return readService(FileName);
	}
	public String readService(String FileName) //throws IOException
	{
		Service1="";
                System.out.println("In the body, we have that "+Analyse.dayInfo.get("PFlag3"));
               

            	try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(findLanguage.langFileFind(Analyse.dayInfo.get("LS").toString(),FileName)), "UTF8"));
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			//return "";			//THERE WAS AN ERROR IN PROCESSING THE FILES
		}
                
                return "<html>\n"+Header1+Style+"</style>\n</head>\n<body>"+Service1+"</body></html>";
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
			
			if (Analyse.evalbool(table.get("Cmd").toString()) == false)
			{
				return;
			}
		}
		//if(elem.equals("LANGUAGE"))
		//{
			read=true;
		//}
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
				//Service1=OldText[count]+"<font face=\"Hirmos Ponomar\" size=\"5\">"+Service1+"</font>";
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
			ReadText textGet1=new ReadText(Analyse.dayInfo.clone());
                        WhoLast="";
			String text4=textGet1.readText(ServiceFileName+"Text/"+Title+".xml");
                        String Ponomar=Text.Phrases.get("0").toString();
                        String Colon=Text.Phrases.get("Colon").toString();
			if(text4 != null)
			{
				Header1=Header1+"<title>"+Ponomar+Colon+text4+"</title>";
                                
			}
			else
			{
				Header1=Header1+ServiceNames[0];
			}
			Title=table.get("Value").toString();
			text4=textGet1.readText(ServiceFileName+"Text/"+Title+".xml");
			if(text4 != null)
			{
				Service1+="<h1> "+text4+"</h1>\n";
			}
			else
			{
				Service1+="<h1>"+ServiceNames[0]+"</h1>\n";
			}
						
			if(table.get("Source") != null)
			{
				String Source=table.get("Source").toString();
				text4=textGet1.readText(ServiceFileName+"Text/"+Source+".xml");
				Service1+="<Font color=\"red\"><I><small>"+text4+"</small></I><BR>";
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
                if(elem.equals("SUBTITLE") && read)
		{
			//WE ARE DEALING WITH THE TITLE OF THE SERVICE. IT CAN HAVE 3 PARTS: THE TITLE ITSELF, THE SOURCE FOR
			//SERVICE, AND SOME ADDITIONAL COMMENTS.
			//String Subtitle=table.get("Header").toString();
			ReadText textGet1=new ReadText(Analyse.dayInfo.clone());
                        WhoLast="";

			String Subtitle=table.get("Value").toString();
			String text4=textGet1.readText(ServiceFileName+"Text/"+Subtitle+".xml");
			if(text4 != null)
			{
				Service1+="<h2> "+text4+"</h2>\n";
			}
			else
			{
				Service1+="<h2>"+ServiceNames[0]+"</h2>\n";
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
				Bible reader=new Bible(Analyse.dayInfo);
				parsedBible=reader.getText(Reading1.substring(0,k),Reading1.substring(k+1),false);
				What2=parsedBible[0].substring(0);
			}
			if(table.get("getReading") != null)
			{
				//THIS ALLOWS THE READING HEADER FOR THE GIVEN SELECTION TO BE OBTAINED, THAT IS, "A reading from the Book of...."
				Bible reader=new Bible(Analyse.dayInfo);
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
						textNew=textNew.replace("...",""); 								//The 3 dots combined as a single symbol
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
                if (elem.equals("GETID") && read){
                    String Type = "M";
                           //System.out.println(table.get("Type"));
                    if (table.get("Type")!= null){
                        Type=table.get("Type").toString();
                    }
                    String LifeId=table.get("Id").toString();
                    if (Type.equals("T"))
                    {
                        LifeId="98"+LifeId;
                    }
                    Commemoration1 data=new Commemoration1("0",LifeId,Analyse.dayInfo);
                    String Info = table.get("What").toString();
                    int parsedInfo1=Info.lastIndexOf("/");
                   //System.out.println(parsedInfo[0]);
                    //The last 2 such elements are important as they contain the general location of what is desired!!!
                    //System.out.println(Info);
                    //System.out.println(parsedInfo[1]);
                    //GENERALISED THE VERSION TO ANYTHING LOCATED INSIDE THE SERVICE TAGS!!!
                    OrderedHashtable RoyalHours=(OrderedHashtable)data.getService(Info.substring(0,parsedInfo1),Info.substring(parsedInfo1+1));
                    //System.out.println((OrderedHashtable)data.getService("/ROYALHOURS/VERSE","9P"));
                    //System.out.println(RoyalHours);
                    String HeaderRH=new String();
                    if(RoyalHours.get("Header")!=null){
                        HeaderRH=RoyalHours.get("Header").toString();
                    }
                    if(table.get("Header")!=null){
                        Header=Integer.parseInt(table.get("Header").toString());
                    }
                    else{
                        Header=0;
                    }
                    //System.out.println(RoyalHours);
                    if (RoyalHours.get("text") == null){
                        Service1+="<BR><Font color=\"red\"> "+ServiceNames[4] + Info+"</Font><BR>";
                        
                    }
                    else{
                        //System.out.println(table);
                        readIncidentals(table);

                        Service1+=Implement(Header,HeaderRH,RoyalHours.get("text").toString().substring(1))+"\n";
                    }
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
			ReadText textGet=new ReadText(Analyse.dayInfo.clone());
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
		if (elem.equals("TIMES") && read)
		{
			textTimes=table.get("Value").toString();
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
		ReadText textGet=new ReadText(Analyse.dayInfo.clone());
		
		/* Original place of this when there the times is used as it was orignally.
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
		*/
		if(RedFirst != null)
		{
			if(RedFirst.equals("1"))
			{
                            //ADDED 2009/10/20 n.s Yuri Shardt
                            //TAKING THE FIRST LETTER IS INSUFFICIENT FOR PROPERLY PROCESSING THE RED, AS ANY DIACRITICS MUST ALSO BE IN READ,
                            //FOR CHURCH SLAVONIC AND OTHER LANGUAGES, THIS MARKS DO NOT CREATE THEIR OWN SEPARATE LETTERS AS IN A+grave != A`
                            //AS ONE CHARACTER, BUT AS TWO CHARACTERS.
                            //LISTING ALL DIACRITIC MARKS THAT I WISH TO RECOGNISE: COMMON, SLAVIC, AND GREEK
                           String[] List={"\u0300","\u0301","\u0302","\u0303","\u0304","\u0305","\u0306","\u0307","\u0308","\u0309","\u030a","\u030b","\u030c","\u030d","\u030e","\u030f",
                                            "\u0310","\u0311","\u0312","\u0313","\u0314","\u0315","\u0316","\u0317","\u0318","\u0319","\u031a","\u031b","\u031c","\u031d","\u031e","\u031f",
                                            "\u0320","\u0321","\u0322","\u0323","\u0324","\u0325","\u0326","\u0327","\u0328","\u0329","\u032a","\u032b","\u032c","\u032d","\u032e","\u032f",
                                            "\u0330","\u0331","\u0332","\u0333","\u0334","\u0335","\u0336","\u0337","\u0338","\u0339","\u033a","\u033b","\u033c","\u033d","\u033e","\u033f",
                                            "\u0384","\u0385","\u037A", //Greek Diacritics I
                                            "\u0483","\u0484","\u0485","\u0486","\u0487","\u0488","\u0489",     //Cyrillic Diacritics I
                                            "\u1fbd","\u1fbe","\u1fbf","\u1fc0","\u1fc1","\u1fcd","\u1fce","\u1fcf","\u1fdd","\u1fde","\u1fdf","\u1fed","\u1fee","\u1fef","\u1ffd","\u1ffe",         //Greek Diacritics II
                                            "\u2de0","\u2de1","\u2de2","\u2de3","\u2de4","\u2de5","\u2de6","\u2de7","\u2de8","\u2de9","\u2dea","\u2deb","\u2dec","\u2ded","\u2dee","\u2def",        //Cyrillic Combining Letters I
                                            "\u2df0","\u2df1","\u2df2","\u2df3","\u2df4","\u2df5","\u2df6","\u2df7","\u2df8","\u2df9","\u2dfa","\u2dfb","\u2dfc","\u2dfd","\u2dfe","\u2dff",        //Cyrillic Combining Letters II
                                            "\ua66f",
                                            "\ua670","\ua671","\ua672","\ua673","\ua67c","\ua67d"};         //Some more Cyrilic Diacritics
                            //Continuing to add letters to the red part until none from the above list are found.
                            String redNow=new String();
                            int countRed=1;
                            redNow=text2.substring(0,1);
                            
                            boolean stopRed=false;
                            while (!stopRed){
                                stopRed=true;
                                for(int i=0;i<List.length;i++){
                                    //System.out.println(redNow +" a");
                                    //System.out.println(text2.substring(countRed,countRed+1));
                                    if(text2.substring(countRed,countRed+1).equals(List[i])){
                                        stopRed=false;
                                        redNow=redNow+text2.substring(countRed,countRed+1);
                                        countRed=countRed+1;
                                        break;
                                    }
                                }                               
                            }
                            
                            text2="<B><FONT color=\"red\">"+redNow+"</FONT></B>"+text2.substring(countRed);
			}
		}
		String textRepeat=new String();
		if(Times != null)
		{
			/*int Time= Integer.parseInt(Times);
			String textR=text2;
			for(int i=1;i < Time;i++)
			{
				text2+="<BR>"+textR;
			}*/
			//THIS IS THE ORIGINAL  VERSION OF TIMES. CHANGED TO A BETTER VERSION. 2009/05/18 Y.S.
			textTimes=new String();
			Analyse.dayInfo.put("Times",Integer.parseInt(Times));
			
			try
			{
				String FileName="xml/Commands/Times.xml";
				BufferedReader frf1 = new BufferedReader(new InputStreamReader(new FileInputStream(findLanguage.langFileFind(Analyse.dayInfo.get("LS").toString(),FileName)), "UTF8"));
				QDParser.parse(this, frf1);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				//THERE WAS AN ERROR IN PROCESSING THE FILES
			}
			int splitting=textTimes.indexOf("^#");
			if (splitting != -1)
			{
				//REPLACE ^# BY THE ACTUAL NUMBER
				String LHS = new String();
				String RHS = new String();
				if (splitting > 1)
				{
					LHS = textTimes.substring(0, splitting).trim();
				}
				if (splitting + 2 < textTimes.length())
				{
					RHS = textTimes.substring(splitting + 2);
				}
				textRepeat = LHS + Times + RHS;
	
				
			}
			else
			{
				textRepeat= textTimes;
			}
				
			
		}
		String textCommand=new String();
		if(Command != null)
		{
			String text3=textGet.readText(ServiceFileName+"Command/"+Command+".xml");
			
			if(text3 != null)
			{
				textCommand=text3;
			}
			else
			{
				textCommand=ServiceNames[2];
			}
			//PROVIDING A PROPER COMBINATION OF THE 2 EVENTS: REPEAT AND COMMAND
                        //CORRECTED A FORMATING ERROR IN THE FOLLOWING LINES MISSING A CLOSING ANGLE BRACKET FOR </I>
                        //YURI SHARDT 2009/10/31 n.s.
                        
			if (Times != null)
			{
				String textR=textGet.readText(ServiceFileName+"Command/AfterEach.xml");
				if (textR !=null)
				{
					text2=text2+" <I><Font color=\"red\">("+textRepeat+textR+" "+textCommand+")</Font></I>";
                                        
				}
				else
				{
					text2=text2+" <I><Font color=\"red\">("+textRepeat+ServiceNames[2]+" "+textCommand+")</Font></I>";
                                        
				}
			}
			else
			{
				text2=text2+" <I><Font color=\"red\">("+textCommand+")</Font></I>";
                                
			}
		}
		else
		{
			if(Times != null)
			{
				text2=text2+" <I><Font color=\"red\">("+textRepeat+")</Font></I>";
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
				text4="<h2>"+text4+"</h2> ";
			}
			else
			{
				text4="<BR><B><Font color=\"red\">" +ServiceNames[3]+" </Font></B><BR>";
			}
                        
			if (!WhoLast.equals("")){
                            text4="</p>"+text4;
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
                                text2="<p><B><FONT color=\"red\">"+textWho+"</FONT></B>"+text2;
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
                            if(NewLine != null)
				{
					if(NewLine.equals("1"))
					{
						text2="</p><BR>"+text4+text2;
					}
					else
					{
						text2="</p>"+text4+text2;
					}


				}
				else
				{
					text2="</p>"+text4+text2;
				}
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
						text2="<BR>"+text4+text2;
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
