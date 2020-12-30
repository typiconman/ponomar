package net.ponomar.readings.utility;

import net.ponomar.readings.Matins;
import net.ponomar.readings.Reading;
import net.ponomar.utility.Constants;
 
 
 
import net.ponomar.utility.StringOp;

import java.util.LinkedHashMap;
import java.util.ArrayList;

public class ClassifyMatins extends ClassifyReadings {

    public ClassifyMatins() {
    }

    public ClassifyMatins(LinkedHashMap<String, ArrayList<String>> readingsInA) {
		//StringOp Testing = new StringOp();
        parameterValues.setDayInfo(Reading.getInformation3().getDayInfo());
        classify(readingsInA);
    }

    public ClassifyMatins(Matins matins, LinkedHashMap<String, ArrayList<String>> readingsInA, StringOp parameterValues) {
		classify(readingsInA);

    }
    private void classify(LinkedHashMap<String, ArrayList<String>> readingsIn)
    {
        //Initialise Information.
        information2=new LinkedHashMap<>();
        /*try {
            FileReader frf = new FileReader(findLanguage.langFileFind(ParameterValues.dayInfo.get("LS").toString(), "xml/Commands/Matins.xml"));
            //System.out.println(findLanguage.langFileFind(ParameterValues.dayInfo.get("LS").toString(), Constants.DIVINE_LITURGY));
            //DivineLiturgy a1 = new classifyReadin();
            QDParser.parse(this, frf);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

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
        int dRank = Integer.parseInt(parameterValues.getDayInfo().get(Constants.D_RANK).toString());
        //LeapReadings();		//THIS ALLOWS APPROPRIATE SKIPPING OF READINGS OVER THE NATIVITY SEASON!

      
        if (dow == 0 && dRank > 6 && (nday < -49 || nday > 0)) {
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

        if (dow == 0 && dRank <= 6){
            for (Object o : menaionV) {
                suppressedV.add(o);
                suppressedR.add(o);
                suppressedT.add(o);
            }
            menaionV.clear();
            menaionV.clear();
            menaionV.clear();
            return;
        }

        return;
    }
}