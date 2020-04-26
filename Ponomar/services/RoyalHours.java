package Ponomar.services;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.filechooser.FileFilter;

import Ponomar.About;
import Ponomar.MenuFiles;
import Ponomar.calendar.JDate;
import Ponomar.internationalization.LanguagePack;
import Ponomar.panels.PrimeSelector;
import Ponomar.panels.PrintableTextPane;
import Ponomar.parsing.DocHandler;
import Ponomar.parsing.QDParser;
import Ponomar.parsing.Service;
import Ponomar.utility.Helpers;
import Ponomar.utility.OrderedHashtable;
import Ponomar.utility.StringOp;

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
public class RoyalHours extends LitService
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	//THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	//TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	//DURING THE COURSE OF A SINGLE WEEK.
	//private final static String octoecheosFileName   = "xml/Services/Octoecheos/";   // THE LOCATION OF THE BASIC SERVICE RULES
	//private static OrderedHashtable PrimesTK;
	//private static String FileNameIn="xml/Services/PRIMES1/";
	//private static String FileNameOut=FileNameIn+"Primes.html";
	//private String Troparion1;
	//private String Kontakion1;
	//private String Kontakion2;
	//private String Troparion2;
	//private final static String triodionFileName   = "xml/triodion/";   // TRIODION FILE
	//private final static String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
	//private String LentenK;				//ANY REQUIRED KATHISMA REFERENCED USING "LENTENK = "17"" WOULD BE THE 17th KATHISMA.
	//private PrimeSelector SelectorP=new PrimeSelector();


	public RoyalHours(JDate date, OrderedHashtable dayInfo)
	{
            analyse.setDayInfo(dayInfo);
            langText=new LanguagePack(dayInfo);
            primesNames=langText.obtainValues((String)langText.getPhrases().get("RoyalHours"));
	languageNames=langText.obtainValues((String)langText.getPhrases().get("LanguageMenu"));
        fileNames=langText.obtainValues((String)langText.getPhrases().get("File"));
	helpNames=langText.obtainValues((String)langText.getPhrases().get("Help"));
		today=date;
		helper=new Helpers(analyse.getDayInfo());
                analyse.getDayInfo().put("PS",1);

		try
		{
			String strOut=createHours();
			if(strOut.equals("Royal Hours are not served today."))
			{
				Object[] options = {languageNames[3]};
				JOptionPane.showOptionDialog(null, primesNames[0],(String)langText.getPhrases().get("0") + (String)langText.getPhrases().get("Colon")+ primesNames[1], JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			}
			else
			{
				//strOut=strOut+"<p><Font Color='red'>Disclaimer: This is a preliminary attempt at creating the Primes service.</Font></p>";
				//int LangCode=Integer.parseInt(Analyse.getDayInfo().get("LS").toString());
                                //if (LangCode==2 || LangCode==3 ){
                                    //strOut="<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><p><font face=\"Ponomar Unicode TT\" size=\"5\">"+strOut+"</font></p>";
                                    //System.out.println("Added Font");
                                  // }

                               serviceWindow(strOut);
			}
		}
		catch (IOException j)
		{
		}

	}

	@Override
	protected void constructMenu(JFrame frame) {
		JMenuBar menuBar=new JMenuBar();
		MenuFiles demo=new MenuFiles(analyse.getDayInfo());
		menuBar.add(demo.createFileMenu(this));
		menuBar.add(demo.createHelpMenu(this));
		frame.setJMenuBar(menuBar);
	}
	
	
	protected String createHours() throws IOException
	{
		//OBTAIN THE DEFAULTS FOR THE SERVICE (WHAT WAS LAST USED!)
		//Analyse.getDayInfo().put("PS",SelectorP.getWhoValue());
                //MUST ADD APPROPRIATE SELECTOR OF TYPE OF SERVICE
		//int TypeP=SelectorP.getTypeValue();
                
		Service ReadHours=new Service(analyse.getDayInfo());

		int Eday=Integer.parseInt(analyse.getDayInfo().get("nday").toString());
                int day=Integer.parseInt(analyse.getDayInfo().get("doy").toString());
                int dow=Integer.parseInt(analyse.getDayInfo().get("dow").toString());
                

                if (!((Eday == -2) || (day == 4 && (dow != 6 && dow != 0)) || (day == 2 && dow == 5) || (day == 3 && dow == 5) || (day == 357 && ((dow != 6) && dow != 0)) || (day == 356 && dow == 5) || (day == 355 && dow == 5))){
                    return "Royal Hours are not served today.";
                }
		//BASED ON THE DATE DETERMINE THE CORRECT FLAGS
                analyse.getDayInfo().put("PFlag",0); //FOR EVE OF NATIVITY!
                if ((Eday == -2)){
                    analyse.getDayInfo().put("PFlag",2); //FOR GOOD FRIDAY

                }
                if ((day == 4 && (dow != 6 && dow != 0)) || (day == 2 && dow == 5) || (day == 3 && dow == 5)){
                    analyse.getDayInfo().put("PFlag",1);
                }


		String strOut= new String();
		//IT IS TO BE DECIDED WHETHER IT IS DESIRED TO SET THE TROPARIA PROPERLY!

		strOut=ReadHours.startService(SERVICES_FILENAME + "RoyalHours.xml")+"</p>";

                

	     return strOut;
	}

	public void startElement(String elem, Hashtable table)
	{

		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo.
		if (table.get("Cmd") != null)
		{
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

			if (analyse.evalbool(table.get("Cmd").toString()) == false)
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
			text+=(String)table.get("Value");

		}
		if (elem.equals("PRIMES") && read)
		{
			

		}
		//OTHER LITURGICAL SERVICES WOULD FOLLOW HERE

	}

        public static void main(String[] argz)
	{
		//DEBUG MODE
		System.out.println("RoyalHours.java running in Debug mode");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY!!");
                
               OrderedHashtable dayInfo = new OrderedHashtable();

                dayInfo.put("dow",3);
                dayInfo.put("doy",357);
                dayInfo.put("nday",-256);
                dayInfo.put("LS",0); //ENGLISH
                dayInfo.put("PS",1);

                JDate todays=new JDate(12,24,2009);

		new RoyalHours(todays,dayInfo);
	}


}

