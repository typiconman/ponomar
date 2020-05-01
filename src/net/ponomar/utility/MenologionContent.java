package net.ponomar.utility;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.ArrayList;

import net.ponomar.Bible;
import net.ponomar.ConfigurationFiles;
import net.ponomar.astronomy.Astronomy;
import net.ponomar.astronomy.Sunrise;
import net.ponomar.calendar.JDate;
import net.ponomar.calendar.PCalendar;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.readings.DivineLiturgy;
import net.ponomar.readings.Matins;

public final class MenologionContent {

	private StringOp analyse;
	private LanguagePack phrases;
	private JDate today;
	private String rSep = "";
	private String colon = "";

	public MenologionContent(StringOp analyse, LanguagePack phrases, JDate today) {
		this.analyse = analyse;
		this.phrases = phrases;
		this.today = today;
	}

	private static final String READINGS_KEY = Constants.READINGS;

	public String processReadings(Bible shortForm, LinkedHashMap combinedReadings) {
		StringBuilder content = new StringBuilder();
		boolean firstTime = true;
		for (Enumeration<String> e = Collections.enumeration(combinedReadings.keySet()); e.hasMoreElements();) {
			// Temporary solution
			String element1 = e.nextElement();
			LinkedHashMap temp = (LinkedHashMap) combinedReadings.get(element1);
			ArrayList<LinkedHashMap> readings = (ArrayList<LinkedHashMap>) temp.get(READINGS_KEY);
			ArrayList<String> rank = (ArrayList<String>) temp.get("Rank");
			ArrayList<String> tag = (ArrayList<String>) temp.get("Tag");
			if (element1.equals("LITURGY")) {
				if (firstTime) {
					firstTime = false;
				} else {
					content.append(rSep);
				}
				// Special case and consider it differently
				content.append(iterateEpistleGospel(readings, rank, tag));

                /*for (int j=0; j<Readings.size();j++){
                if (j!=0){
                content.append(rSep);
                }
                String BibleText=epistle.get(j).toString();

                output+=ShortForm.getHyperlink(BibleText);

                if (Readings.size()>1){
                output+= Tag.get(j).toString();
                }
                }*/

                /*for (int j=0; j<Readings.size();j++){
                if (j!=0){
                content.append(rSep);
                }
                String BibleText=gospel.get(j).toString();
                content.append(ShortForm.getHyperlink(BibleText));

                if (Readings.size()>1){
                content.append(Tag.get(j).toString());

                }
                }*/
				continue;

			}
			if (element1.equals("MATINS")) {
				if (firstTime) {
					firstTime = false;
				} else {
					content.append(rSep);
				}

				content.append(putMatinsReadings(readings, rank, tag, new LinkedHashMap<>(), "matins"));
				// output+=RSep;

				continue;

			}
			if (firstTime) {
				firstTime = false;
			} else {
				content.append(rSep);
			}
			String type1 = phrases.getPhrases().get(element1.toLowerCase());
			content.append("<B>").append(type1).append("</B>").append(colon);
			content.append(iterateOverReadings(shortForm, readings, tag, rSep));

			// content.append(rSep);
		}
		return content.toString();
	}

	public String iterateEpistleGospel(ArrayList<LinkedHashMap> readings, ArrayList<String> rank, ArrayList<String> tag) {

		String epistleGospelOutput = "";
		ArrayList<String> epistle = new ArrayList<>();

		ArrayList<String> gospel = new ArrayList<>();

		for (LinkedHashMap liturgy : readings) {
			LinkedHashMap stepE = (LinkedHashMap) liturgy.get("apostol");
			LinkedHashMap stepG = (LinkedHashMap) liturgy.get("gospel");

			if (stepE != null) {
				epistle.add(stepE.get(Constants.READING).toString());
			} else {
				epistle.add("");
			}
			if (stepG != null) {
				gospel.add(stepG.get(Constants.READING).toString());
			} else {
				gospel.add("");
			}
		}
		LinkedHashMap<String, ArrayList<String>> readingsA = new LinkedHashMap<>();

		if (!epistle.get(0).equals("")) {
			epistleGospelOutput += putEpistleGospelReadings(rank, tag, epistle, readingsA, "apostol");
			epistleGospelOutput += rSep;
		}
		if (!gospel.get(0).equals("")) {
			epistleGospelOutput += putEpistleGospelReadings(rank, tag, gospel, readingsA, "gospel");
		}
		return epistleGospelOutput;
	}

	public String putMatinsReadings(ArrayList<LinkedHashMap> readings, ArrayList<String> rank, ArrayList<String> tag,
									LinkedHashMap<String, ArrayList<String>> readingsA, String key) {
		readingsA.put(READINGS_KEY, MenologionContent.processMatins(readings));
		readingsA.put("Rank", rank);
		readingsA.put("Tag", tag);
		Matins trial1 = new Matins(analyse.getDayInfo());
		String type1 = phrases.getPhrases().get(key);
		return "<B>" + type1 + "</B>" + colon + trial1.readings(readingsA, today);
	}

	private String putEpistleGospelReadings(ArrayList<String> rank, ArrayList<String> tag, ArrayList<String> reading,
											LinkedHashMap<String, ArrayList<String>> readingsA, String key) {
		readingsA.put(READINGS_KEY, reading);
		readingsA.put("Rank", rank);
		readingsA.put("Tag", tag);
		DivineLiturgy trial1 = new DivineLiturgy(analyse.getDayInfo());
		String type1 = phrases.getPhrases().get(key);
		return "<B>" + type1 + "</B>" + colon + trial1.readings(readingsA, key, today);
	}

	public static ArrayList<String> processMatins(ArrayList<LinkedHashMap> readings) {
		ArrayList<String> matins2 = new ArrayList<>();

		for (LinkedHashMap matins : readings) {
			// System.out.println("In Main1, we have "+Readings.get(j));
			LinkedHashMap stepE = (LinkedHashMap) matins.get("matins");
			if (stepE == null) {
				stepE = (LinkedHashMap) matins.get("1");
			}
			// OrderedHashtable stepE=(OrderedHashtable)matins.get("matins");
			// System.out.println("In Main1, we have "+matins2);
			// System.out.println(stepE);

			if (stepE != null) {
				matins2.add(stepE.get(Constants.READING).toString());
			} else {
				matins2.add("");
			}
		}
		return matins2;
	}

	public static String iterateOverReadings(Bible shortForm, ArrayList<LinkedHashMap> readings, ArrayList<String> tag,
			String rSep) {
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < readings.size(); i++) {
			LinkedHashMap reading = readings.get(i);
			if (i != 0) {
				output.append(rSep);
			}
			boolean first = true;

			for (Enumeration<String> e2 = Collections.enumeration(reading.keySet()); e2.hasMoreElements();) {
				if (first) {
					first = false;
				} else {
					output.append(rSep);
				}
				String element2 = e2.nextElement();
				LinkedHashMap stuff = (LinkedHashMap) reading.get(element2);
				String bibleText = stuff.get(Constants.READING).toString();
				output.append(shortForm.getHyperlink(bibleText));
			}
			if (readings.size() > 1) {
				output.append(tag.get(i));

			}
		}
		return output.toString();
	}

	public static String getAstronomicalData(StringOp analyse, JDate today, String sunriseText, String sunsetText,
			String lunarPhaseText) {
		Sunrise sunrise = new Sunrise(analyse.getDayInfo());
		String[] sunriseSunset = sunrise.getSunriseSunsetString(today,
				ConfigurationFiles.getDefaults().get("Longitude"),
				ConfigurationFiles.getDefaults().get("Latitude"),
				ConfigurationFiles.getDefaults().get("TimeZone"));
		String astronomicalData = "<BR>" + sunriseText + sunriseSunset[0];
		astronomicalData += "<BR>" + sunsetText + sunriseSunset[1];
		astronomicalData += Constants.DOUBLE_LINEBREAK; // <B>"+MainNames[3]+"</B>"+Colon+
														// Paschalion.getLunarPhaseString(today) +"<BR><BR>";
		// getting rid of the lunar phase until we program a paschalion ...
		// adding the civil Lunar phase by request of Mitrophan
		Astronomy sky = new Astronomy();

		astronomicalData += lunarPhaseText + sky.lunarphase(today.getJulianDay(), analyse.getDayInfo());
		astronomicalData += Constants.DOUBLE_LINEBREAK;
		return astronomicalData;
	}

	public static String getFormat(String amc, StringOp analyse, JDate today, String am) {
		String format = "";
		if (amc.equals("1")) {
			PCalendar checking = new PCalendar(today, PCalendar.JULIAN, analyse.getDayInfo());
			format = am;
			if (analyse.getDayInfo().get(Constants.IDEOGRAPHIC).equals("1")) {
				RuleBasedNumber convertN = new RuleBasedNumber(analyse.getDayInfo());

				format = format.replace("^YYAM",
						convertN.getFormattedNumber(Long.parseLong(Integer.toString(checking.getAM()))));

			} else {
				format = format.replace("^YYAM", Integer.toString(checking.getAM()));
			}
		}
		return format;
	}

	public void setrSep(String rSep) {
		this.rSep = rSep;
	}

	public String getColon() {
		return colon;
	}

	public void setColon(String colon) {
		this.colon = colon;
	}
}
