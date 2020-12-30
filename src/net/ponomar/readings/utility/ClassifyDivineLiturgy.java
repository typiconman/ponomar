package net.ponomar.readings.utility;

import java.io.FileReader;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Collections;

import net.ponomar.parsing.utility.DocHandler;
import net.ponomar.parsing.utility.QDParser;
import net.ponomar.readings.DivineLiturgy;
import net.ponomar.readings.Reading;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;

public class ClassifyDivineLiturgy extends ClassifyReadings implements DocHandler {

		public ClassifyDivineLiturgy() {
        }

        public ClassifyDivineLiturgy(LinkedHashMap<String, ArrayList<String>> readingsInA) {
			//StringOp Testing = new StringOp();
            parameterValues.setDayInfo(DivineLiturgy.getAnalyse().getDayInfo());
            //System.out.println("In ParameterValues, we have LS = " + ParameterValues.getDayInfo().get("LS")+" while in Analyse, we have "+Analyse.getDayInfo().get("LS"));
            classify(readingsInA);
        }

       public ClassifyDivineLiturgy(LinkedHashMap<String, ArrayList<String>> readingsInA, LinkedHashMap<String, Object> dayInfo) {
		parameterValues.setDayInfo(dayInfo);
            classify(readingsInA);

        }
        private void classify(LinkedHashMap<String, ArrayList<String>> readingsIn)
        {
            //Initialise Information.
            information2=new LinkedHashMap<>();
            Reading.setFindLanguage(new Helpers(parameterValues.getDayInfo()));
            //System.out.println(findLanguage.langFileFind(ParameterValues.getDayInfo().get("LS").toString(), Constants.DIVINE_LITURGY));
            try {
                FileReader frf = new FileReader(Reading.getFindLanguage().langFileFind(parameterValues.getDayInfo().get("LS").toString(), Constants.DIVINE_LITURGY));
                //System.out.println(findLanguage.langFileFind(ParameterValues.getDayInfo().get("LS").toString(), Constants.DIVINE_LITURGY));
                //DivineLiturgy a1 = new classifyReadin();
                QDParser.parse(this, frf);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ArrayList<String> paschalV = readingsIn.get(Constants.READINGS);
            ArrayList paschalR = readingsIn.get("Rank");
            ArrayList<String> paschalT = readingsIn.get("Tag");

            dailyV = new ArrayList<String>();
            dailyR = new ArrayList();
            dailyT = new ArrayList<String>();

            
            if (paschalV == null){
                return;
            }
           

            for (int k = 0; k < paschalV.size(); k++) {
                
                if ((Integer) paschalR.get(k) == -2) {
                    //THIS IS A DAILY READING THAT CAN BE SKIPPED, EXCEPT MAYBE ON SUNDAYS.
                    dailyV.add(paschalV.get(k));
                    dailyR.add(paschalR.get(k));
                    dailyT.add(paschalT.get(k));
                } else {
                    menaionV.add(paschalV.get(k));
                    menaionR.add(paschalR.get(k));
                    menaionT.add(paschalT.get(k));
                }

            }           
            
            suppress();
            //LeapReadings();            


        }

        private void suppress() {
            //THIS FUNCTION CONSIDERS WHAT HOLIDAYS ARE CURRENTLY OCCURING AND RETURNS THE READINGS FOR THE DAY, WHERE SUPPRESSED CONTAINS THE READINGS THAT WERE SUPPRESSED.
            int doy = Integer.parseInt(parameterValues.getDayInfo().get("doy").toString());
            int dow = Integer.parseInt(parameterValues.getDayInfo().get("dow").toString());
            int nday = Integer.parseInt(parameterValues.getDayInfo().get("nday").toString());
            int ndayF = Integer.parseInt(parameterValues.getDayInfo().get(Constants.NDAY_F).toString());
            int ndayP = Integer.parseInt(parameterValues.getDayInfo().get(Constants.NDAY_P).toString());
            leapReadings();		//THIS ALLOWS APPROPRIATE SKIPPING OF READINGS OVER THE NATIVITY SEASON!

            /******************************************************
            FOR ALL HOLIDAYS OF THE FIRST CLASS, THAT IS, OF THE LORD, THEN ONLY THE MENALOGION
            READINGS ARE TAKEN. THE PENTECOSTARION READINGS CAN BE TRANSFERRED.
            THE FOLLOWING FESTIVALS ARE CONSIDERED:
            1. EXALTATION: SEPTEMBER 14th: DOY == 256
            2. CHRISTMAS: DECEMBER 25th: DOY == 358
            3. THEOPHANY: JANUARY 6th: DOY == 5
            4. TRANSFIGURATION: AUGUST 6th: DOY == 217
             ******************************************************/
   /*         if (doy == 256 || doy == 358 || doy == 5 || doy == 217) {
                for (int k = 0; k < dailyV.size(); k++) {
                    suppressedV.add(dailyV.get(k));
                    suppressedR.add(dailyR.get(k));
                    suppressedT.add(dailyT.get(k));
                }
                dailyV.clear();
                dailyR.clear();
                dailyT.clear();                
                return;				//There is no need for any other readings to be considered!
            }
*/
            /********************************
            FOR ALL HOLIDAY OF THE SECOND CLASS, THAT IS, OF THE MOTHER OF GOD, THEN ONLY THE MENALOGION
            READINGS ARE TAKEN, IF IT FALLS DURING MONDAY TO SATURDAY, OTHERWISE THE READINGS
            ARE COMBINED WITH THE SEQUENTIAL READINGS.
            THE FOLLOWING FESTIVALS ARE CONSIDERED:
            1. ANNUNCIATION: MARCH 25th: DOY == 83 (ALTHOUGH THE RULES ARE ACTUALLY MORE INVOLVED, HENCE SKIPPED)
            2. PRESENTATION OF THE LORD: FEBRUARY 2nd: DOY == 32, BUT NOT IF NDAY == -48 (FIRST DAY OF LENT).
            3. NATIVITY OF THE MOTHER OF GOD: SEPTEMBER 8th: DOY == 250
            4. DORMITION OF THE MOTHER OF GOD: AUGUST 15th: DOY == 226
            5. ENTRY OF THE MOTHER OF GOD INTO THE TEMPLE: NOVEMBER 21st: 324
             **************************************************************************************/
 /*           if ((doy == 32 && nday != -48) || doy == 250 || doy == 226 || doy == 324) {
                if (dow != 0) {
                    for (int k = 0; k < dailyV.size(); k++) {
                        suppressedV.add(dailyV.get(k));
                        suppressedR.add(dailyR.get(k));
                        suppressedT.add(dailyT.get(k));
                    }
                    dailyV.clear();
                    dailyR.clear();
                    dailyT.clear();
                    return;					//There is no need for any other readings to be considered!
                } else {
                    //ALL THE READINGS ARE COMBINED IN SOME FASHION, HOWEVER SOME COULD POTENTIAL BE REDUCED DUE TO REPEATS

                    return;					//CHECK WHETHER IS TRUE
                }
            }


            if (dow != 0) {*/
                ArrayList<String> list = information2.get("Class3Transfers");
                if (list != null) {
                    for (Enumeration<String> e2 = Collections.enumeration(list); e2.hasMoreElements();) {
                        String command = e2.nextElement();
                        if (parameterValues.evalbool(command)) {
                            //THE CURRENT COMMAND WAS TRUE AND THE SEQUENTITIAL READING IS TO BE SUPPRESSED/TRANSFERRED
                            for (int k = 0; k < dailyV.size(); k++) {
                                suppressedV.add(dailyV.get(k));
                                suppressedR.add(dailyR.get(k));
                                suppressedT.add(dailyT.get(k));
                            }
                            dailyV.clear();
                            dailyR.clear();
                            dailyT.clear();
                            return;
                        }
                    }
                }
                return;					//There is no need for any other readings to be considered!
            //}

            //AT THIS POINT, THE PENTECOSTARION READINGS MAY BE REDUCED DUE TO REPEATS

            //return;
        }
    }