package net.ponomar.parsing;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import net.ponomar.Bible;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.parsing.utility.DocHandler;
import net.ponomar.parsing.utility.QDParser;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 
import net.ponomar.utility.StringOp;
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
	private static final String HEADER = "Header";
	private static final String TIMES = Constants.COMMANDS + "Times.xml";
	private static final String COMMONPRAYERS_FILENAME = Constants.SERVICES_PATH + "CommonPrayers/";   // THE LOCATION OF THE BASIC SERVICE RULES
	public static String service1;
	//private static String text;
	private static boolean read=false;
	//private String filename;
	//private int lineNumber;
	private LanguagePack text;//=new LanguagePack();
	private String[] serviceNames;//=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
	//private String[] languageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private String who;
	private String what;
	private String redFirst;
	private String newLine;
	private String timesContent;
	private int headerInteger;
	private String commandB;
	private String whoLast="";
	private String command;
	private int count=-1;
	private String[] oldText=new String[10];
	private String[] parsedBible;
	private String textTimes;
        private String style;
        private String header1;
        private Helpers findLanguage;
        private StringOp analyse=new StringOp();
	//private Font CurrentFont=new Font((String)StringOp.dayInfo.get("FontFaceM"),Font.PLAIN,Integer.parseInt((String)StringOp.dayInfo.get("FontSizeM")));
        public Service (LinkedHashMap<String, Object> dayInfo){
            analyse.setDayInfo(dayInfo);
                text=new LanguagePack(dayInfo);
                serviceNames=text.obtainValues(text.getPhrases().get("ServiceRead"));
        }

	public String startService(String fileName) {
		findLanguage = new Helpers(analyse.getDayInfo());

		whoLast = "";
		count = -1;
		// Service1="";
		header1 = "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">\n<head>\n";
		// <meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">\n";
		style = "<style type=\"text/css\">\nrubric {color:red;font-weight:bold}\np {margin-left:.5in;text-indent:-.5in}\nh1 {color:red;font-weight:bold;text-align:center}\ncomment {color:red;font-size:50%;font-style:italic}\ncommand {color:red;font-style:italic}\nh2 {color:red;font-size:110%;text-align:center}\n";

		/*
		 * int LangCode=Integer.parseInt(StringOp.dayInfo.get("LS").toString()); if
		 * (LangCode==2 || LangCode==3 ){
		 * Style=Style+"body {font-family:\"Ponomar Unicode TT\";font-size:18pt}\n";
		 * //System.out.println("Added Font"); } else{
		 * Style=Style+"body {font-size:12pt}\n"; }
		 */
		String displayFont = text.getPhrases().get("FontFaceL");
		String displaySize = text.getPhrases().get("FontSizeL");

		Font value1 = (Font) UIManager.get("Menu.font");
		if (displaySize == null || displaySize.equals("")) {
			displaySize = Integer.toString(value1.getSize());
		}
		if (displayFont == null || displayFont.equals("")) {
			displayFont = value1.getFontName();
		}
		// If the default user's font size is larger than the required there is not need to change it.
		displaySize = Integer.toString(Math.max(Integer.parseInt(displaySize), value1.getSize())); 
		// The specified fonts sizes are the mininum required.
		style += "body {font-family:" + displayFont + ";font-size:" + displaySize + "}\n</head>";
		return readService(fileName);
	}

	public String readService(String fileName)	{
		service1 = "";
		//System.out.println("In the body, we have that " + analyse.getDayInfo().get("PFlag3"));

		try {
			BufferedReader frf = new BufferedReader(new InputStreamReader(
					new FileInputStream(findLanguage.langFileFind(analyse.getDayInfo().get("LS").toString(), fileName)),
					StandardCharsets.UTF_8));
			QDParser.parse(this, frf);
		} catch (Exception e) {
			e.printStackTrace();
			// return "";
			// THERE WAS AN ERROR IN PROCESSING THE FILES
		}

		return "<html>\n" + header1 + style + "</style>\n</head>\n<body>" + service1 + "</body></html>";
	}

	public String closeService() {
		// THIS CLOSES THE SERVICE TEXT APPROPRIATELY
		whoLast = "";
		count = -1;
		return "</p>";
	}

	public void startDocument() {

	}

	public void endDocument() {

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
			read=true;
		//}
		if(elem.equals("GET") && read)
		{
			//WE NEED TO GET ANOTHER SERVICE OR PART THEREOF.
			read=false;
			String getFile=table.get("File");
			count++;
			oldText[count]=service1;
			readService(Constants.SERVICES_PATH+getFile+".xml");
			int nullCheck=0;
			if(table.get("Null")!=null)
			{
				nullCheck=Integer.parseInt(table.get("Null"));
			}
			
			if(nullCheck == 0 || (nullCheck == 1 && service1 != null))
			{
				//Service1=OldText[count]+"<font face=\"Ponomar Unicode TT\" size=\"5\">"+Service1+"</font>";
                                service1=oldText[count]+service1;
			}
			else
			{
				service1=oldText[count];
			}
			count--;
			read=true;
		}
		if(elem.equals("TITLE") && read)
		{
			//WE ARE DEALING WITH THE TITLE OF THE SERVICE. IT CAN HAVE 3 PARTS: THE TITLE ITSELF, THE SOURCE FOR 
			//SERVICE, AND SOME ADDITIONAL COMMENTS.
			String title=table.get(HEADER);
			ReadText textGet1=new ReadText((LinkedHashMap<String, Object>) analyse.getDayInfo().clone());
                        whoLast="";
			String text4=textGet1.readText(Constants.SERVICES_PATH+"Text/"+title+".xml");
                        String ponomar=text.getPhrases().get("0");
                        String colon=text.getPhrases().get(Constants.COLON);
			if(text4 != null)
			{
				header1=header1+"<title>"+ponomar+colon+text4+"</title>";
                                
			}
			else
			{
				header1=header1+serviceNames[0];
			}
			title=table.get(Constants.VALUE);
			text4=textGet1.readText(Constants.SERVICES_PATH+"Text/"+title+".xml");
			if(text4 != null)
			{
				service1+="<h1> "+text4+"</h1>\n";
			}
			else
			{
				service1+="<h1>"+serviceNames[0]+"</h1>\n";
			}
						
			if(table.get("Source") != null)
			{
				String source=table.get("Source");
				text4=textGet1.readText(Constants.SERVICES_PATH+"Text/"+source+".xml");
				service1+="<Font color=\"red\"><I><small>"+text4+"</small></I><BR>";
			}
			
			if(table.get("Comment") != null)
			{
				String comment=table.get("Comment");
				text4=textGet1.readText(Constants.SERVICES_PATH+"Text/"+comment+".xml");
				if(text4 != null)
				{
					service1+="<I><small>"+text4+"</small></I><BR>";
				}
			}
			service1+="</Font>";
		}
                if(elem.equals("SUBTITLE") && read)
		{
			//WE ARE DEALING WITH THE TITLE OF THE SERVICE. IT CAN HAVE 3 PARTS: THE TITLE ITSELF, THE SOURCE FOR
			//SERVICE, AND SOME ADDITIONAL COMMENTS.
			//String Subtitle=table.get("Header").toString();
			ReadText textGet1=new ReadText((LinkedHashMap<String, Object>) analyse.getDayInfo().clone());
                        whoLast="";

			String subtitle=table.get(Constants.VALUE);
			String text4=textGet1.readText(Constants.SERVICES_PATH+"Text/"+subtitle+".xml");
			if(text4 != null)
			{
				service1+="<h2> "+text4+"</h2>\n";
			}
			else
			{
				service1+="<h2>"+serviceNames[0]+"</h2>\n";
			}

			if(table.get("Comment") != null)
			{
				String comment=table.get("Comment");
				text4=textGet1.readText(Constants.SERVICES_PATH+"Text/"+comment+".xml");
				if(text4 != null)
				{
					service1+="<I><small>"+text4+"</small></I><BR>";
				}
			}
			service1+="</Font>";
		}
		if(elem.equals("TEXT") && read)
		{
			//HERE IS IT IS ASSUMED THAT THE TEXT AND THE HEADER HAVE BEEN CREATED PROGRAMMATICALLY AND HAVE BEEN ASSIGNED FIXED VALUES
			//APPROPRIATE TO THE GIVEN LANGUAGE. THUS, BOTH what AND HeaderText ARE ASSUMED TO CONTAIN TEXT
			what=table.get("What");
			String headerText="";
			if(table.get(HEADER) != null)
			{
				headerText=table.get(HEADER);
				headerInteger=1;				
			}
			readIncidentals(table);
			service1+=implement(headerInteger,headerText,what)+"\n";	
		}
		if(elem.equals("BIBLE") && read)
		{
			String what2="";
			if(table.get("Verses") !=null)
			{
				//ALLOWING THE BIBLE TO BE READ Y.S. 2008/12/11 n.s.
				//THE FORMAT FOR A BIBLE STATEMENT IS Bible="Book_Chapter:VerseStart-VerseEnd" or ="Book_Chapter:Verse,Chapter:Verse" or ="Book_Chapter"
				//THE BOOK COULD BE OF THE FORM II_NAME_Chapter:VerseStart-VerseEnd,Verse,Verse,Chapter:Verse
				String reading1=table.get("Verses");
				int k=reading1.lastIndexOf('_');
				Bible reader=new Bible(analyse.getDayInfo());
				parsedBible=reader.getText(reading1.substring(0,k),reading1.substring(k+1),false);
				what2=parsedBible[0].substring(0);
			}
			if(table.get("getReading") != null)
			{
				//THIS ALLOWS THE READING HEADER FOR THE GIVEN SELECTION TO BE OBTAINED, THAT IS, "A reading from the Book of...."
				Bible reader=new Bible(analyse.getDayInfo());
				what2=reader.getIntro(table.get("getReading"));
			}
			int stars2=-1;	 		//THIS VARIABLE CONSIDERS WHAT TO DO WITH ANY POSSIBLE 2 STARS IN THE TEXT "**"
			int starsBible=parsedBible[1].indexOf("**");	//IF THERE ARE NO ** TO BE FOUND IN THE TEXT THEN THERE IS NO NEED TO CONTINUE!
			if((table.get("2Stars") != null) && starsBible != -1)
			{
				stars2=Integer.parseInt(table.get("2Stars"));
				if(stars2==1)
				{
					//USE THE 2 STARS DATA AS AN ADDITIONAL HEADER
					parsedBible[2]=parsedBible[1].substring(3)+"<BR>"+parsedBible[2];	
					
				}
				if(stars2==2)
				{
					int k=what2.indexOf("**");
					String[] splitString=parsedBible[1].split("<BR>");
					int a1=splitString[0].indexOf('\"');
					int a2=splitString[0].substring(a1+1).indexOf('\"');
					String textNew="";
					if(a1 != -1)
					{
						textNew=parsedBible[1].substring(a1+1,a2+a1+1).replace("...",""); 		//3 separate dots
						textNew=textNew.replace("...",""); 								//The 3 dots combined as a single symbol
					}
										
					what2=textNew+" "+what2.substring(k+2);
				}				
			}
			//REMOVE THE 2 STARS FROM THE ORIGINAL READING
			int stars=what2.indexOf("**");

			while(stars != -1)
			{
				//System.out.println(What2);
				if(stars==0)
				{
					what2=what2.substring(3);
				}
				else
				{
					what2=what2.substring(0,stars-1)+what2.substring(stars+2);
				}
				stars=what2.indexOf("**");
			}
			
			if(table.get(HEADER) != null)
			{
				if(table.get(HEADER).equals("1"))
				{
					headerInteger= Integer.parseInt(table.get(HEADER));								
				}
				else
				{
					headerInteger=0;
				}
			}
			else
			{
				headerInteger=0;
			}
			readIncidentals(table);			
			service1+=implement(headerInteger,parsedBible[2],what2)+"\n";
			read=true;
		}
                if (elem.equals("GETID") && read){
                    String type = "M";
                           //System.out.println(table.get("Type"));
                    if (table.get("Type")!= null){
                        type=table.get("Type");
                    }
                    String lifeId=table.get("Id");
                    if (type.equals("T"))
                    {
                        lifeId="98"+lifeId;
                    }
                    Commemoration data=new Commemoration("0",lifeId,analyse.getDayInfo());
                    String info = table.get("What");
                    int parsedInfo1=info.lastIndexOf('/');
                   //System.out.println(parsedInfo[0]);
                    //The last 2 such elements are important as they contain the general location of what is desired!!!
                    //System.out.println(Info);
                    //System.out.println(parsedInfo[1]);
                    //GENERALISED THE VERSION TO ANYTHING LOCATED INSIDE THE SERVICE TAGS!!!
                    LinkedHashMap<String, String> royalHours= data.getService(info.substring(0,parsedInfo1),info.substring(parsedInfo1+1));
                    //System.out.println((OrderedHashtable)data.getService("/ROYALHOURS/VERSE","9P"));
                    //System.out.println(RoyalHours);
                    String headerRH="";
                    if(royalHours.get(HEADER)!=null){
                        headerRH=royalHours.get(HEADER);
                    }
                    if(table.get(HEADER)!=null){
                        headerInteger=Integer.parseInt(table.get(HEADER));
                    }
                    else{
                        headerInteger=0;
                    }
                    //System.out.println(RoyalHours);
                    if (royalHours.get("text") == null){
                        service1+="<BR><Font color=\"red\"> "+serviceNames[4] + info+"</Font><BR>";
                        
                    }
                    else{
                        //System.out.println(table);
                        readIncidentals(table);

                        service1+=implement(headerInteger,headerRH,royalHours.get("text").substring(1))+"\n";
                    }
                    read=true;
                }
		if (elem.equals("CREATE") && read)
		{
			String what2="";
			if(table.get("What") != null)
			{
				what=table.get("What");
			}
			else
			{
				what=null;
			}
			//String text2="";
			if(table.get(HEADER) != null)
			{
				if(table.get(HEADER).equals("1"))
				{
					headerInteger= Integer.parseInt(table.get(HEADER));
										
				}
				else
				{
					headerInteger=0;
				}
			}
			else
			{
				headerInteger=0;
			}
			readIncidentals(table);
			//System.out.println(What);
			ReadText textGet=new ReadText((LinkedHashMap<String, Object>) analyse.getDayInfo().clone());
			if(what != null)
			{
				what2 = textGet.readText(COMMONPRAYERS_FILENAME+what+".xml");
				if (what2 == null)
				{
					service1+= "<BR><Font color=\"red\"/>" + serviceNames[1] +" "+COMMONPRAYERS_FILENAME+what+".xml</FONT><BR>";
					return;
				}
			}
				
			service1+=implement(headerInteger,textGet.readHeader(COMMONPRAYERS_FILENAME+what+".xml"),what2)+"\n";		
			read=true;
		}
		if (elem.equals("TIMES") && read)
		{
			textTimes=table.get(Constants.VALUE);
		}
		

	}

	public void endElement(String elem)
	{
		if(elem.equals(Constants.LANGUAGE) || elem.equals("TONE"))
		{
			read=false;
		}
	}

	public void text(String text)
	{

	}
	private void readIncidentals(HashMap<String, String> table)
	{
			//THIS READS THE COMMON LABELS FOR CREATE, BIBLE, AND TEXT TAGS.
			
                        who = table.get("Who");
			if(table.get("CommandB") != null)
			{
				commandB= table.get("CommandB");	
			}
			else
			{
				commandB=null;
			}
			if(table.get("Command") != null)
			{
				command= table.get("Command");	
			}
			else
			{
				command=null;
			}
				if( table.get("RedFirst") != null)
			{
				redFirst = table.get("RedFirst");				
			}
			else
			{
				redFirst=null;
			}
			if( table.get("NewLine") != null)
			{
				newLine =  table.get("NewLine");
			}
			else
			{
				newLine=null;
			}
			if(table.get("Times") != null)
			{
				timesContent= table.get("Times");	
			}
			else
			{
				timesContent=null;
			}
			
	}
	private String implement(int header,String text4, String what)
	{
		String text2=what;
		ReadText textGet=new ReadText((LinkedHashMap<String, Object>) analyse.getDayInfo().clone());
		
		/* Original place of this when there the times is used as it was originally.
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
		if(redFirst != null)
		{
			if(redFirst.equals("1"))
			{
                            //ADDED 2009/10/20 n.s Yuri Shardt
                            //TAKING THE FIRST LETTER IS INSUFFICIENT FOR PROPERLY PROCESSING THE RED, AS ANY DIACRITICS MUST ALSO BE IN READ,
                            //FOR CHURCH SLAVONIC AND OTHER LANGUAGES, THIS MARKS DO NOT CREATE THEIR OWN SEPARATE LETTERS AS IN A+grave != A`
                            //AS ONE CHARACTER, BUT AS TWO CHARACTERS.
                            //LISTING ALL DIACRITIC MARKS THAT I WISH TO RECOGNISE: COMMON, SLAVIC, AND GREEK
                           String[] list={"\u0300","\u0301","\u0302","\u0303","\u0304","\u0305","\u0306","\u0307","\u0308","\u0309","\u030a","\u030b","\u030c","\u030d","\u030e","\u030f",
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
                            StringBuilder redNow= new StringBuilder();
                            int countRed=1;
                            redNow = new StringBuilder(text2.substring(0, 1));
                            
                            boolean stopRed=false;
                            while (!stopRed){
                                stopRed=true;
								for (String s : list) {
									//System.out.println(redNow +" a");
									//System.out.println(text2.substring(countRed,countRed+1));
									if (text2.substring(countRed, countRed + 1).equals(s)) {
										stopRed = false;
										redNow.append(text2.substring(countRed, countRed + 1));
										countRed = countRed + 1;
										break;
									}
								}
                            }
                            
                            text2="<B><FONT color=\"red\">"+redNow+"</FONT></B>"+text2.substring(countRed);
			}
		}
		String textRepeat="";
		if(timesContent != null)
		{
			/*int Time= Integer.parseInt(Times);
			String textR=text2;
			for(int i=1;i < Time;i++)
			{
				text2+="<BR>"+textR;
			}*/
			//THIS IS THE ORIGINAL  VERSION OF TIMES. CHANGED TO A BETTER VERSION. 2009/05/18 Y.S.
			textTimes="";
			analyse.getDayInfo().put("Times",Integer.parseInt(timesContent));
			
			try
			{
				BufferedReader frf1 = new BufferedReader(new InputStreamReader(new FileInputStream(findLanguage.langFileFind(analyse.getDayInfo().get("LS").toString(), TIMES)), StandardCharsets.UTF_8));
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
				String lhs = "";
				String rhs = "";
				if (splitting > 1)
				{
					lhs = textTimes.substring(0, splitting).trim();
				}
				if (splitting + 2 < textTimes.length())
				{
					rhs = textTimes.substring(splitting + 2);
				}
				textRepeat = lhs + timesContent + rhs;
	
				
			}
			else
			{
				textRepeat= textTimes;
			}
				
			
		}
		String textCommand="";
		if(command != null)
		{
			String text3=textGet.readText(Constants.SERVICES_PATH+"Command/"+command+".xml");
			
			if(text3 != null)
			{
				textCommand=text3;
			}
			else
			{
				textCommand=serviceNames[2];
			}
			//PROVIDING A PROPER COMBINATION OF THE 2 EVENTS: REPEAT AND COMMAND
                        //CORRECTED A FORMATING ERROR IN THE FOLLOWING LINES MISSING A CLOSING ANGLE BRACKET FOR </I>
                        //YURI SHARDT 2009/10/31 n.s.
                        
			if (timesContent != null)
			{
				String textR=textGet.readText(Constants.SERVICES_PATH+"Command/AfterEach.xml");
				if (textR !=null)
				{
					text2=text2+" <I><Font color=\"red\">("+textRepeat+textR+" "+textCommand+")</Font></I>";
                                        
				}
				else
				{
					text2=text2+" <I><Font color=\"red\">("+textRepeat+serviceNames[2]+" "+textCommand+")</Font></I>";
                                        
				}
			}
			else
			{
				text2=text2+" <I><Font color=\"red\">("+textCommand+")</Font></I>";
                                
			}
		}
		else
		{
			if(timesContent != null)
			{
				text2=text2+" <I><Font color=\"red\">("+textRepeat+")</Font></I>";
			}
		}
		
		if(commandB != null)
		{
			String text3=textGet.readText(Constants.SERVICES_PATH+"Command/"+commandB+".xml");
				
			if(text3 != null)
			{
				text2=" <I><Font color=\"red\"> "+text3+"</Font></I> "+text2;
			}
			else
			{
				text2=" <I><Font color=\"red\">"+serviceNames[2] + "</Font></I>"+text2;
			}
		}
			
		if(header != 0)
		{
			if(text4 != null)
			{
				text4="<h2>"+text4+"</h2> ";
			}
			else
			{
				text4="<BR><B><Font color=\"red\">" +serviceNames[3]+" </Font></B><BR>";
			}
                        
			if (!whoLast.equals("")){
                            text4="</p>"+text4;
                        }
                        whoLast="";
							
		}
		else
		{
			text4="";
		}
		if(!who.equals(whoLast) || whoLast.equals(""))
		{
		//THERE HAS BEEN A CHANGE IN WHO IS READING THE SERVICE READER TO PRIEST OR SOMETHING SIMILAR
		//THERE IS A NEED TO AFFIX THE NEW READER.
		//THIS WILL ENTAIL BY DEFAULT A NEW LINE
                    if(!who.equals(""))
			{
				String textWho = textGet.readText(COMMONPRAYERS_FILENAME+who+".xml");
                                text2="<p><B><FONT color=\"red\">"+textWho+"</FONT></B>"+text2;
				if(!whoLast.equals(""))
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
                            if(newLine != null)
				{
					if(newLine.equals("1"))
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
							
			whoLast=who;	
		}
		else
		{
		//THE READER IS THE SAME. CHECK IF A NEW LINE IS DESIRED
			
				
				if(newLine != null)
				{
					if(newLine.equals("1"))
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
