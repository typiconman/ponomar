package net.ponomar.readings;

import java.util.Enumeration;
import java.util.HashMap;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Collections;

import net.ponomar.Bible;
import net.ponomar.astronomy.Paschalion;
import net.ponomar.calendar.JDate;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.parsing.Day;
import net.ponomar.parsing.utility.DocHandler;
import net.ponomar.readings.utility.ReadingUtility;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;

import net.ponomar.utility.StringOp;

public abstract class Reading implements DocHandler {

	// private static LinkedHashMap readings;
//	private static LinkedHashMap pentecostarionS;
//	private static LinkedHashMap menalogionS;
//	private static LinkedHashMap floaterS;
	protected static LinkedHashMap<String, ArrayList<String>> information;
//	private static String gLocation;
	protected static LanguagePack phrases;
	protected static String[] transferredDays;
	protected static String[] error;
	private static Helpers findLanguage;
//	private static ArrayList dailyV = new ArrayList();
//	private static ArrayList dailyR = new ArrayList();
//	private static ArrayList dailyT = new ArrayList();
//	private static ArrayList menaion2V = new ArrayList();
//	private static ArrayList menaion2R = new ArrayList();
//	private static ArrayList menaion2T = new ArrayList();
//	private static ArrayList menaionV = new ArrayList();
//	private static ArrayList menaionR = new ArrayList();
//	private static ArrayList menaionT = new ArrayList();
//	private static ArrayList suppressedV = new ArrayList();
//	private static ArrayList suppressedR = new ArrayList();
//	private static ArrayList suppressedT = new ArrayList();
	protected static LinkedHashMap<String, ArrayList<String>> tomorrowRead = new LinkedHashMap<>();
	protected static LinkedHashMap<String, ArrayList<String>> yesterdayRead = new LinkedHashMap<>();
	private static StringOp information3 = new StringOp();

	public Reading() {
		super();
	}

	@Override
	public void startDocument() throws Exception {
	}

	@Override
	public void endDocument() throws Exception {
	}

	@Override
	public void endElement(String tag) throws Exception {

	}

	protected LinkedHashMap<String, ArrayList<String>> getReadings(JDate today, String readingType) {
		String filename = "";
		int lineNumber = 0;

		int nday = (int) JDate.difference(today, Paschalion.getPascha(today.getYear()));

		// I COPIED THIS FROM THE Main.java FILE BY ALEKS WITH MY MODIFICATIONS (Y.S.)
		// FROM HERE UNTIL
		if (nday >= -70 && nday < 0) {
			filename = Constants.TRIODION_PATH;
			lineNumber = Math.abs(nday);
		} else if (nday < -70) {
			// WE HAVE NOT YET REACHED THE LENTEN TRIODION
			filename = Constants.PENTECOSTARION_PATH;
			JDate lastPascha = Paschalion.getPascha(today.getYear() - 1);
			lineNumber = (int) JDate.difference(today, lastPascha) + 1;
		} else {
			// WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
			filename = Constants.PENTECOSTARION_PATH;
			lineNumber = nday + 1;
		}

		filename += lineNumber >= 10 ? lineNumber + "" : "0" + lineNumber + ""; // CLEANED UP
		// READ THE PENTECOSTARION / TRIODION INFORMATION
		Day checkingP = new Day(filename, getInformation3().getDayInfo());

		// ADDED 2008/05/19 n.s. Y.S.
		// COPYING SOME READINGS FILES

		// GET THE MENAION DATA
		int m = today.getMonth();
		int d = today.getDay();

		filename = "";
		filename += m < 10 ? "xml/0" + m : "xml/" + m; // CLEANED UP
		filename += d < 10 ? "/0" + d : "/" + d; // CLEANED UP
		filename += "";

		Day checkingM = new Day(filename, getInformation3().getDayInfo());
		getInformation3().getDayInfo().put(Constants.D_RANK, Math.max(checkingP.getDayRank(), checkingM.getDayRank()));

		LinkedHashMap[] paschalReadings = checkingP.getReadings();
		LinkedHashMap[] menaionReadings = checkingM.getReadings();
		LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> combinedReadings = new LinkedHashMap<>();

		ReadingUtility.processMenaionPaschalReadings(menaionReadings, combinedReadings);
		ReadingUtility.processMenaionPaschalReadings(paschalReadings, combinedReadings);

		LinkedHashMap<String, ArrayList<String>> temp = combinedReadings.get("LITURGY");
		// System.out.println("temp values (423)" + temp);
		ArrayList<String> readings = temp.get(Constants.READINGS);
		// TODO: Properly go through the entire sequence of code to replace the
		// ArrayList with a new class
		ArrayList<String> rank = temp.get("Rank");
		ArrayList<String> tag = temp.get("Tag");
		// Special case and consider it differently

		ArrayList<String> type = new ArrayList<>();

		for (Object reading : readings) {
			LinkedHashMap liturgy = (LinkedHashMap) reading;
			LinkedHashMap stepE = (LinkedHashMap) liturgy.get(readingType);
			if (stepE != null) {

				type.add(stepE.get(Constants.READING).toString());
			} else {
				// type.add("");
			}

		}

		// output += RSep;
		LinkedHashMap<String, ArrayList<String>> final2 = new LinkedHashMap<>();
		final2.put(Constants.READINGS, type);
		final2.put("Rank", rank);
		final2.put("Tag", tag);

		return final2;
	}

	public void startElement(String elem, HashMap<String, String> table) {
		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo.
		if (table.get("Cmd") != null) {
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

			if (!getInformation3().evalbool(table.get("Cmd"))) {
				return;
			}
		}

		if (elem.equals(Constants.COMMAND)) {
			// THIS WILL STORE ALL THE POSSIBLE COMMANDS FOR A GIVEN SITUATION AND ALLOW THE
			// RESULTS TO BE DETEMINED.
			String name = table.get("Name");
			String value = table.get(Constants.VALUE);
			// IF THE GIVEN name OCCURS IN THE information HASHTABLE THAN AUGMENT ITS
			// VALUES.
			if (information.containsKey(name)) {
				ArrayList<String> previous = information.get(name);
				previous.add(value);
				information.put(name, previous);
			} else {
				ArrayList<String> vect = new ArrayList<>();
				vect.add(value);
				information.put(name, vect);
			}

		}
		// ALL WE CARE ABOUT ARE THE SCRIPTURE READINGS
	}

	@Override
	public void text(String str) throws Exception {

	}

	public static Helpers getFindLanguage() {
		return findLanguage;
	}

	public static void setFindLanguage(Helpers findLanguage) {
		Reading.findLanguage = findLanguage;
	}

	public static StringOp getInformation3() {
		return information3;
	}

	public static void setInformation3(StringOp information3) {
		Reading.information3 = information3;
	}

	protected String week(String dow) {
		// CONVERTS THE DOW STRING INTO A NAME. THIS SHOULD BE IN THE ACCUSATIVE CASE
		try {
			return transferredDays[Integer.parseInt(dow)];
		} catch (Exception a) {
			return dow; // A DAY OF THE WEEK WAS NOT SENT
		}
	}

	public String format(ArrayList listV, ArrayList listR, ArrayList<Integer> listT) {
		StringBuilder output = new StringBuilder();
		// AT THIS POINT, THE PENTECOSTARION READINGS WILL BE FORMATED SO THAT THEY ARE
		// SEQUENTIAL BY THE WEEK,
		// ESPECIALLY IF THERE ARE ANY RETRACTIONS OR THE LIKE.

		Bible shortForm = new Bible(getInformation3().getDayInfo());
		try {
			for (int k = 0; k < listV.size(); k++) {
				String reading = (String) listV.get(k);
				output.append(shortForm.getHyperlink(reading));

				if ((Integer) listR.get(k) == -2) {
					if (listV.size() > 1) {
						output.append(" (").append(week(listT.get(k).toString())).append(")");
					}
				} else {
					output.append(listT.get(k));
				}

				if (k < listV.size() - 1) {
					output.append(getInformation3().getDayInfo().get("ReadSep")); // IF THERE ARE MORE READINGS OF THE
																					// SAME TYPE APPEND A SEMICOLON!
				}
			}
		} catch (Exception a) {

			System.out.println(a.toString());
			StackTraceElement[] trial = a.getStackTrace();
			System.out.println(trial[0].toString());

		}
		return output.toString();
	}

	protected String display(String a, String b, String c) {
		// THIS FUNCTION TAKES THE POSSIBLE 3 READINGS AND COMBINES THEM AS APPROPRIATE,
		// SO THAT NO SPACES OR OTHER UNDESIRED STUFF IS DISPLAYED!
		String output = "";
		if (a.length() > 0) {
			output += a;
		}
		if (b.length() > 0) {
			if (output.length() > 0) {
				output += getInformation3().getDayInfo().get("ReadSep") + " ";
			}
			output += b;
		}
		if (c.length() > 0) {
			if (output.length() > 0) {
				output += getInformation3().getDayInfo().get("ReadSep") + " ";
			}
			output += c;

		}

		// TECHNICALLY, IF THERE ARE 3 OR MORE READINGS, THEN SOME SHOULD BE TAKEN "FROM
		// THE BEGINNING" (nod zachalo).
		return output;
	}

}