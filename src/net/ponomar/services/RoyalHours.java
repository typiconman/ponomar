package net.ponomar.services;

import javax.swing.*;
import java.util.*;
import java.io.*;
import net.ponomar.MenuFiles;
import net.ponomar.calendar.JDate;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.parsing.Service;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 

/***********************************************************************
 * THIS MODULE CREATES THE TEXT FOR THE ORTHODOX SERVICE OF THE FIRST HOUR
 * (PRIME) THIS MODULE IS STILL IN THE DEVELOPMENT PHASE.
 * 
 * (C) 2007, 2008 YURI SHARDT. ALL RIGHTS RESERVED. Updated some parts to make
 * it compatible with the changes in Ponomar, especially the language issues!
 * 
 * PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE
 * CODE PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES
 * THEREOF.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ***********************************************************************/
public class RoyalHours extends LitService {
	// SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	// THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	// TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	// DURING THE COURSE OF A SINGLE WEEK.

	private static final String P_FLAG = "PFlag";

	public RoyalHours(JDate date, LinkedHashMap<String, Object> dayInfo) {
		analyse.setDayInfo(dayInfo);
		langText = new LanguagePack(dayInfo);
		primesNames = langText.obtainValues(langText.getPhrases().get("RoyalHours"));
		languageNames = langText.obtainValues(langText.getPhrases().get(Constants.LANGUAGE_MENU));
		fileNames = langText.obtainValues(langText.getPhrases().get("File"));
		helpNames = langText.obtainValues(langText.getPhrases().get("Help"));
		today = date;
		helper = new Helpers(analyse.getDayInfo());
		analyse.getDayInfo().put("PS", 1);

		try {
			String strOut = createHours();
			if (strOut.equals("Royal Hours are not served today.")) {
				Object[] options = { languageNames[3] };
				JOptionPane.showOptionDialog(null, primesNames[0],
						langText.getPhrases().get("0") + langText.getPhrases().get(Constants.COLON)
								+ primesNames[1],
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			} else {
				serviceWindow(strOut);
			}
		} catch (IOException j) {
			j.printStackTrace();
		}

	}

	@Override
	protected void constructMenu(JFrame frame) {
		JMenuBar menuBar = new JMenuBar();
		MenuFiles demo = new MenuFiles(analyse.getDayInfo());
		menuBar.add(demo.createFileMenu(this));
		menuBar.add(demo.createHelpMenu(this));
		frame.setJMenuBar(menuBar);
	}

	protected String createHours() throws IOException {
		// OBTAIN THE DEFAULTS FOR THE SERVICE (WHAT WAS LAST USED!)
		// Analyse.getDayInfo().put("PS",SelectorP.getWhoValue());
		// MUST ADD APPROPRIATE SELECTOR OF TYPE OF SERVICE
		// int TypeP=SelectorP.getTypeValue();

		Service readHours = new Service(analyse.getDayInfo());

		int eDay = Integer.parseInt(analyse.getDayInfo().get("nday").toString());
		int day = Integer.parseInt(analyse.getDayInfo().get("doy").toString());
		int dow = Integer.parseInt(analyse.getDayInfo().get("dow").toString());

		if (!((eDay == -2) || (day == 4 && (dow != 6 && dow != 0)) || (day == 2 && dow == 5) || (day == 3 && dow == 5)
				|| (day == 357 && ((dow != 6) && dow != 0)) || (day == 356 && dow == 5) || (day == 355 && dow == 5))) {
			return "Royal Hours are not served today.";
		}
		// BASED ON THE DATE DETERMINE THE CORRECT FLAGS
		analyse.getDayInfo().put(P_FLAG, 0); // FOR EVE OF NATIVITY!
		if ((eDay == -2)) {
			analyse.getDayInfo().put(P_FLAG, 2); // FOR GOOD FRIDAY

		}
		if ((day == 4 && (dow != 6 && dow != 0)) || (day == 2 && dow == 5) || (day == 3 && dow == 5)) {
			analyse.getDayInfo().put(P_FLAG, 1);
		}

		String strOut = "";
		// IT IS TO BE DECIDED WHETHER IT IS DESIRED TO SET THE TROPARIA PROPERLY!

		strOut = readHours.startService(Constants.SERVICES_PATH + "RoyalHours.xml") + "</p>";

		return strOut;
	}

	public void startElement(String elem, HashMap<String, String> table) {

		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo.
		if (table.get("Cmd") != null) {
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

			if (!analyse.evalbool(table.get("Cmd"))) {

				return;
			}
		}
		// if(elem.equals("LANGUAGE") || elem.equals("TONE"))
		// {
		read = true;
		// }
		if (elem.equals("TEXT") && read) {
			text += table.get(Constants.VALUE);

		}
		if (elem.equals("PRIMES") && read) {

		}
		// OTHER LITURGICAL SERVICES WOULD FOLLOW HERE

	}

	public static void main(String[] argz) {
		// DEBUG MODE
		System.out.println("RoyalHours.java running in Debug mode");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY!!");

		/*
		 * LinkedHashMap dayInfo = new LinkedHashMap();
		 * 
		 * dayInfo.put("dow", 3); dayInfo.put("doy", 357); dayInfo.put("nday", -256);
		 * dayInfo.put("LS", 0); // ENGLISH dayInfo.put("PS", 1);
		 */

		JDate todays = new JDate(12, 24, 2009);

		//new RoyalHours(todays, dayInfo);
	}

}
