package net.ponomar.readings;

import net.ponomar.astronomy.Paschalion;
import net.ponomar.calendar.JDate;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.parsing.utility.QDParser;
import net.ponomar.readings.utility.ClassifyDivineLiturgy;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
import net.ponomar.utility.StringOp;

import java.util.*;
import java.io.*;

/***************************************************************
DivineLiturgy.java :: MODULE THAT TAKES THE GIVEN DIVIN LITURGY (GOSPEL AND EPISTLE) READINGS FOR THE DAY,
THAT IS, PENTECOSTARION, MENELOGION, AND FLOATERS AND RETURNS
THE APPROPRIATE SET OF READINGS FOR THE DAY AND THEIR ORDER
ASSUMING THAT THE "LUCAN JUMP" IS BEING USED. THIS FUNCITON MUST BE
PREFORMED SEPARATELY FOR EACH TYPE OF READING: EPISTLE AND GOSPEL.
THIS PROGRAMME HAS BEEN GENERALISED TO ALLOW ANY SET OF RULES TO BE USED.

Further work will convert this into the programme that will allow the creation of the text for the Divine Liturgy.

DivineLiturgy1.java is part of the Ponomar project.
Copyright 2008, 2012, 2015 Yuri Shardt
version 1.0: May 2008
 * version 2.0: June 2012, further updates and corrections to the new format.
 * version 2.5: 2015, updates and corrections


PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE CODE
PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES THEREOF.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 *************************************************************/
public class DivineLiturgy extends Reading {

	private static StringOp analyse=new StringOp();

    public DivineLiturgy(LinkedHashMap<String, Object> dayInfo) {
        getAnalyse().setDayInfo(dayInfo);
        getInformation3().setDayInfo(dayInfo);
          phrases = new LanguagePack(dayInfo);
    transferredDays = phrases.obtainValues(phrases.getPhrases().get("DayReading"));
     error = phrases.obtainValues(phrases.getPhrases().get("Errors"));
     setFindLanguage(new Helpers(getAnalyse().getDayInfo()));
    }

//THESE ARE THE SAME FUNCTION AS IN MAIN, BUT TRIMMED FOR THE CURRENT NEEDS


    public String readings(LinkedHashMap<String, ArrayList<String>> readingsIn, String readingType, JDate today) {
        /********************************************************
        SINCE I HAVE CORRECTED THE SCRIPTURE READINGS IN THE MAIN FILE, I CAN NOW PRECEDE WITH A BETTER VERSION OF THIS PROGRAMME!
         ********************************************************/
        //PROCESS THE READINGS INTO THE DESIRED FORMS:
        ClassifyDivineLiturgy orderedReadings = new ClassifyDivineLiturgy(readingsIn);
       /* Information3.getDayInfo().put("doy","12");
        Information3.getDayInfo().put("dow","1");
        Information3.getDayInfo().put("nday","2");
        System.out.println("Testing the new StringOp formulation is " + Information3.evalbool("doy == 12"));*/

        information = new LinkedHashMap<>();
        int doy = Integer.parseInt(getAnalyse().getDayInfo().get("doy").toString());
        int dow = Integer.parseInt(getAnalyse().getDayInfo().get("dow").toString());
        int nday = Integer.parseInt(getAnalyse().getDayInfo().get("nday").toString());


        //DETERMINE THE GOVERNING PARAMETERS FOR COMPILING THE READINGS
        try {
            FileReader frf = new FileReader(getFindLanguage().langFileFind(getAnalyse().getDayInfo().get("LS").toString(), Constants.DIVINE_LITURGY));
            DivineLiturgy a1 = new DivineLiturgy(getAnalyse().getDayInfo());
            QDParser.parse(a1, frf);
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*NOTE: SINCE THE 33rd SUNDAY AFTER PENTECOST DOES NOT HAVE ANY ASSOCIATED READINGS IN THE PENTECOSTARION,
        THIS CAN LEAD TO DIFFICULTIES IN DOING CERTAIN THINGS! THUS, THE FOLLOWING CORRECTIONS.
         */
        if ((doy >= 4 && doy <= 10) && (dow == 0) && readingType.equals("apostol")) {
            //IF THERE IS AN APOSTOL ON THIS DAY, THEN THERE MAY BE ISSUES WITH ITS PRESENCE.
            //NOTE: NOTHING IS CURRENTLY DONE ABOUT THIS!           
        }

        //CHECK WHETHER OR NOT IT IS DESIRED TO TRANSFER THE SKIPPED SEQUENTIAL READINGS
        ArrayList<String> transfer = information.get("Transfer");
        boolean transfer1 = getAnalyse().evalbool(transfer.get(0));
        ClassifyDivineLiturgy tomorrows = new ClassifyDivineLiturgy();
        ClassifyDivineLiturgy yesterdays = new ClassifyDivineLiturgy();
        if (transfer1) {
            /*NOW CONSIDER ANY SUPPRESSED READINGS:
            //THE FOLLOWING SHOULD BE NOTED:
            1. READINGS ARE NEVER TRANSFERRED TO A SUNDAY
            2. TUESDAY CAN HAVE 2 SETS OF READINGS TRANSFERRED TO IT: MONDAY'S AND WEDNESDAY'S
             */
            //NOTE 2: NO READINGS ARE TRANSFERRED DURING LENT, THAT IS, -48 <= nday <=0.
            ArrayList<String> transferRule = information.get("TransferRulesB");
            boolean transfer2 =getAnalyse().evalbool(transferRule.get(0));
            if (transfer2) //St. NICHOLAS'S DAY HAS A SPECIAL SET OF RULES
            {
                //IT IS OBLIGATORY TO CHECK THE NEXT DAY IF ANY READINGS ARE TRANSFERRED!
                //THE SAME PROCEDURE AS IN Main.java WILL BE FOLLOWED!


                //Rewriting the transferring rules based on the changes in the file format for the ranking and the like (Y.S. 20120610 n.s.)

                
                StringOp transfers=new StringOp();
                transfers.setDayInfo((LinkedHashMap<String, Object>) getAnalyse().getDayInfo().clone());//findLanguage.deepCopy((Hashtable)StringOp.dayInfo.clone());
                getInformation3().setDayInfo((LinkedHashMap<String, Object>) getAnalyse().getDayInfo().clone());
                String dRankOld=getAnalyse().getDayInfo().get(Constants.D_RANK).toString();
                today.addDays(1);
                // PUT THE RELEVANT DATA IN THE HASH FOR TOMORROW
                //System.out.println("Case I: Testing the StringOp files: In StringOp, doy = "+StringOp.dayInfo.get("dRank").toString()+" In Information3, doy = "+Information3.getDayInfo().get("dRank").toString()+" In Transfers, doy = "+Transfers.dayInfo.get("dRank"));
                getInformation3().getDayInfo().put("dow", today.getDayOfWeek());
                getInformation3().getDayInfo().put("doy", today.getDoy());
                getInformation3().getDayInfo().put(Constants.D_RANK,"0");
                //System.out.println("Case II: Testing the StringOp files: In StringOp, doy = "+StringOp.dayInfo.get("doy").toString()+" In Information3, doy = "+Information3.getDayInfo().get("doy").toString()+" In Transfers, doy = "+Transfers.dayInfo.get("doy"));
                nday = (int) JDate.difference(today, Paschalion.getPascha(today.getYear()));
                int ndayP = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
                //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
                int ndayF = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));
                getInformation3().getDayInfo().put("nday", nday);
                getInformation3().getDayInfo().put(Constants.NDAY_P, ndayP);
                getInformation3().getDayInfo().put(Constants.NDAY_F, ndayF);

                getReadings(today, readingType);
                tomorrowRead = getReadings(today, readingType);
                tomorrows = new ClassifyDivineLiturgy(tomorrowRead, (LinkedHashMap<String, Object>) getInformation3().getDayInfo().clone());
                //System.out.println("Case III: Testing the StringOp files: In StringOp, doy = "+StringOp.dayInfo.get("dRank").toString()+" In Information3, doy = "+Information3.getDayInfo().get("dRank").toString()+" In Transfers, doy = "+Transfers.dayInfo.get("dRank"));


                today.subtractDays(1);
                /*Analyse.getDayInfo().put("dow", today.getDayOfWeek());
                Analyse.getDayInfo().put("doy", today.getDoy());
                nday = (int) JDate.difference(today, Paschalion.getPascha(today.getYear()));
                ndayP = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
                //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
                ndayF = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));
                Analyse.getDayInfo().put("nday", nday);
                Analyse.getDayInfo().put("ndayP", ndayP);
                Analyse.getDayInfo().put("ndayF", ndayF);
                Analyse.getDayInfo().put("dRank",dRankOld);*/
            }
            //NOW WE NEED TO CHECK YESTERDAY'S READINGS, BUT THIS WILL ONLY OCCUR ON A TUESDAY OR DEC. 6th
            transferRule = information.get("TransferRulesF");
            transfer2 = getAnalyse().evalbool(transferRule.get(0));

            if (transfer2) //IF IT IS A SATURDAY, THEN THE READINGS WILL BE SKIPPED, ???
            {
                //IT IS OBLIGATORY TO CHECK THE NEXT DAY IF ANY READINGS ARE TRANSFERRED!
                //THE SAME PROCEDURE AS IN Main.java WILL BE FOLLOWED!


                StringOp transfers=new StringOp();
                transfers.getDayInfo().putAll((LinkedHashMap<String, Object>) getAnalyse().getDayInfo().clone());
                getInformation3().setDayInfo((LinkedHashMap<String, Object>) getAnalyse().getDayInfo().clone());
                String dRankOld=getAnalyse().getDayInfo().get(Constants.D_RANK).toString();
                today.subtractDays(1);


                // PUT THE RELEVANT DATA IN THE HASH FOR TOMORROW
                getInformation3().getDayInfo().put("dow", today.getDayOfWeek());
                getInformation3().getDayInfo().put("doy", today.getDoy());
                getInformation3().getDayInfo().put(Constants.D_RANK,"0");
                nday = (int) JDate.difference(today, Paschalion.getPascha(today.getYear()));
                int ndayP = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
                //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
                int ndayF = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));
                getInformation3().getDayInfo().put("nday", nday);
                getInformation3().getDayInfo().put(Constants.NDAY_P, ndayP);
                getInformation3().getDayInfo().put(Constants.NDAY_F, ndayF);

                yesterdayRead = getReadings(today, readingType);
                yesterdays = new ClassifyDivineLiturgy(yesterdayRead, (LinkedHashMap<String, Object>) getInformation3().getDayInfo().clone());



                today.addDays(1);
                /*Analyse.getDayInfo().put("dow", today.getDayOfWeek());
                Analyse.getDayInfo().put("doy", today.getDoy());
                nday = (int) JDate.difference(today, Paschalion.getPascha(today.getYear()));
                ndayP = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
                //REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
                ndayF = (int) JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));
                Analyse.getDayInfo().put("nday", nday);
                Analyse.getDayInfo().put("ndayP", ndayP);
                Analyse.getDayInfo().put("ndayF", ndayF);
                Analyse.getDayInfo().put("dRank",dRankOld);*/
            }
        }


        ArrayList<Object> dailyVf = new ArrayList<>();
        ArrayList<Object> dailyRf = new ArrayList<>();
        ArrayList dailyTf = new ArrayList();        
        for (int i=0;i<yesterdays.suppressedV.size();i++){
            dailyVf.add(yesterdays.suppressedV.get(i));
            dailyRf.add(yesterdays.suppressedR.get(i));
            dailyTf.add((dow - 1 + 7) % 7);

        }
        for (int i=0;i<orderedReadings.dailyV.size();i++){
            dailyVf.add(orderedReadings.dailyV.get(i));
            dailyRf.add(orderedReadings.dailyR.get(i));
            dailyTf.add(dow);

        }
        for (int i=0;i<tomorrows.suppressedV.size();i++){
            dailyVf.add(tomorrows.suppressedV.get(i));
            dailyRf.add(tomorrows.suppressedR.get(i));
            dailyTf.add((dow + 1) % 7);

        }        
        

        
        //System.out.println("Testing some math: " + (0 - 1 + 7) % 7);
        ArrayList<Object> menaionV = new ArrayList<>();
        ArrayList<Object> menaionR = new ArrayList<>();
        ArrayList menaionT = new ArrayList();

        for (int i=0;i<orderedReadings.menaionV.size();i++){
            menaionV.add(orderedReadings.menaionV.get(i));
            menaionR.add(orderedReadings.menaionR.get(i));
            menaionT.add(orderedReadings.menaionT.get(i));

        }

        


        //THE GENERAL FORMAT IS: FLOATERS, PENTECOSTARION, MENALOGION, EXCEPT ON SATURDAYS WHERE IT IS FLOATERS, MENALOGION, PENTECOSTARION

        if (dow == 6) {
            //ON SATURDAYS, THE READINGS FROM THE MENALOGION TAKE PRECEDENCE.
            for (int i=0;i<dailyVf.size();i++){
            menaionV.add(dailyVf.get(i));
            menaionR.add(dailyRf.get(i));
            menaionT.add(dailyTf.get(i));
            
            return format(menaionV, menaionR, menaionT);
            }
        }
        for (int i=0;i<menaionV.size();i++){
            dailyVf.add(menaionV.get(i));
            dailyRf.add(menaionR.get(i));
            dailyTf.add(menaionT.get(i));

        }
         //System.out.println("---Testing Main Programme-----");

        //System.out.println(menaionV);
        //System.out.println(dailyVf);
        
        return format(dailyVf, dailyRf, dailyTf);
    }

	public static StringOp getAnalyse() {

		return analyse;
	}

	public static void setAnalyse(StringOp newAnalyse) {
		analyse = newAnalyse;

	}

}
