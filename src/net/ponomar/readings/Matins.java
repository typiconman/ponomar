package net.ponomar.readings;

import net.ponomar.calendar.JDate;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.readings.utility.ClassifyMatins;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 
 

import java.util.*;

/***************************************************************
Matins.java :: MODULE THAT TAKES THE GIVEN MATINS READINGS FOR THE DAY,
THAT IS, PENTECOSTARION, MENELOGION, AND FLOATERS AND RETURNS
THE APPROPRIATE SET OF READINGS FOR THE DAY AND THEIR ORDER. 

Further work will convert this into the programme that will allow the creation of the text for Matins.

Matins.java is part of the Ponomar project.
Copyright 2012 Yuri Shardt
version 1.0: July 2012
yuri.shardt (at) gmail.com

PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE CODE
PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES THEREOF.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 **************************************************************/
public class Matins extends Reading {

    public Matins(LinkedHashMap<String, Object> dayInfo) {
        getInformation3().setDayInfo(dayInfo);
        phrases = new LanguagePack(dayInfo);
        transferredDays = phrases.obtainValues(phrases.getPhrases().get("DayReading"));
        error = phrases.obtainValues(phrases.getPhrases().get("Errors"));
        setFindLanguage(new Helpers(getInformation3().getDayInfo()));
    }

//THESE ARE THE SAME FUNCTION AS IN MAIN, BUT TRIMMED FOR THE CURRENT NEEDS




    public String readings(LinkedHashMap<String, ArrayList<String>> readingsIn, JDate today) {
        /********************************************************
        SINCE I HAVE CORRECTED THE SCRIPTURE READINGS IN THE MAIN FILE, I CAN NOW PRECEDE WITH A BETTER VERSION OF THIS PROGRAMME!
         ********************************************************/
        //PROCESS THE READINGS INTO THE DESIRED FORMS:
        ClassifyMatins orderedReadings = new ClassifyMatins(readingsIn);
       /* Information3.getDayInfo().put("doy","12");
        Information3.getDayInfo().put("dow","1");
        Information3.getDayInfo().put("nday","2");
        System.out.println("Testing the new StringOp formulation is " + Information3.evalbool("doy == 12"));*/

        information = new LinkedHashMap<>();
        int doy = Integer.parseInt(getInformation3().getDayInfo().get("doy").toString());
        int dow = Integer.parseInt(getInformation3().getDayInfo().get("dow").toString());
        int nday = Integer.parseInt(getInformation3().getDayInfo().get("nday").toString());
        int dRank=Integer.parseInt(getInformation3().getDayInfo().get(Constants.D_RANK).toString());


        //DETERMINE THE GOVERNING PARAMETERS FOR COMPILING THE READINGS
        /*try {
            FileReader frf = new FileReader(findLanguage.langFileFind(StringOp.dayInfo.get("LS").toString(), Constants.COMMANDS + "Matins.xml"));
            Matins a1 = new Matins();
            QDParser.parse(a1, frf);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        //For the time being I will hard code the rules, as it is simple one. Suppress Sequential readings on Sunday if dRank > 6; otherwise suppress the menaion readings.
        
		ArrayList<Object> dailyVf = new ArrayList<>();
		ArrayList<Object> dailyRf = new ArrayList<>();
		ArrayList dailyTf = new ArrayList();

		for (int i = 0; i < orderedReadings.dailyV.size(); i++) {
			dailyVf.add(orderedReadings.dailyV.get(i));
			dailyRf.add(orderedReadings.dailyR.get(i));
			dailyTf.add(dow);

		}
		for (int i = 0; i < orderedReadings.menaionV.size(); i++) {
			dailyVf.add(orderedReadings.menaionV.get(i));
			dailyRf.add(orderedReadings.menaionR.get(i));
			dailyTf.add(orderedReadings.menaionT.get(i));
		}

		return format(dailyVf, dailyRf, dailyTf);
    }

}
