package net.ponomar.utility;

import java.util.Enumeration;
import java.util.Vector;

import net.ponomar.Bible;
import net.ponomar.ConfigurationFiles;
import net.ponomar.Main;
import net.ponomar.astronomy.Astronomy;
import net.ponomar.astronomy.Sunrise;
import net.ponomar.calendar.JDate;
import net.ponomar.calendar.PCalendar;

public final class MenologionContent {

	private MenologionContent() {

	}

	public static Vector<String> processMatins(Vector<OrderedHashtable> readings) {
		Vector<String> matins2 = new Vector<>();

		for (int j = 0; j < readings.size(); j++) {
			OrderedHashtable matins = readings.get(j);
			// System.out.println("In Main1, we have "+Readings.get(j));
			OrderedHashtable stepE = (OrderedHashtable) matins.get("matins");
			if (stepE == null) {
				stepE = (OrderedHashtable) matins.get("1");
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

	public static String iterateOverReadings(Bible shortForm, Vector<OrderedHashtable> readings, Vector<String> tag,
			String rSep) {
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < readings.size(); i++) {
			OrderedHashtable reading = readings.get(i);
			if (i != 0) {
				output.append(rSep);
			}
			boolean first = true;

			for (Enumeration<String> e2 = reading.enumerateKeys(); e2.hasMoreElements();) {
				if (first) {
					first = false;
				} else {
					output.append(rSep);
				}
				String element2 = e2.nextElement();
				OrderedHashtable stuff = (OrderedHashtable) reading.get(element2);
				String bibleText = stuff.get(Constants.READING).toString();
				output.append(shortForm.getHyperlink(bibleText));
			}
			if (readings.size() > 1) {
				output.append(tag.get(i));

			}
		}
		return output.toString();
	}

	public static String getAstronomicalData(StringOp analyse, JDate today) {
	    Sunrise sunrise = new Sunrise(analyse.getDayInfo());
	    String[] sunriseSunset = sunrise.getSunriseSunsetString(today, (String) ConfigurationFiles.getDefaults().get("Longitude"), (String) ConfigurationFiles.getDefaults().get("Latitude"), (String) ConfigurationFiles.getDefaults().get("TimeZone"));
	    String astronomicalData = "<BR>" + Main.mainNames[1] + sunriseSunset[0];
	    astronomicalData += "<BR>" + Main.mainNames[2] + sunriseSunset[1];
	    astronomicalData += Constants.DOUBLE_LINEBREAK; //<B>"+MainNames[3]+"</B>"+Colon+ Paschalion.getLunarPhaseString(today) +"<BR><BR>";
	    // getting rid of the lunar phase until we program a paschalion ...
	    //adding the civil Lunar phase by request of Mitrophan
	    Astronomy sky = new Astronomy();
	
	    astronomicalData += Main.mainNames[3] + sky.lunarphase(today.getJulianDay(), analyse.getDayInfo());
	    astronomicalData += Constants.DOUBLE_LINEBREAK;
	    return astronomicalData;
	}

	public static String getFormat(String amc, StringOp analyse, JDate today, String am) {
		String format = "";
	    if (amc.equals("1")) {
	        PCalendar checking = new PCalendar(today, PCalendar.JULIAN, analyse.getDayInfo());
	        format = am;
	        if (analyse.getDayInfo().get("Ideographic").equals("1"))
	            {
	                RuleBasedNumber convertN=new RuleBasedNumber(analyse.getDayInfo());
	                
	                format = format.replace("^YYAM", convertN.getFormattedNumber(Long.parseLong(Integer.toString((int) checking.getAM()))));
	
	            }
	            else
	            {
		format = format.replace("^YYAM", Integer.toString((int) checking.getAM()));
	            }
	    }
		return format;
	}
}
