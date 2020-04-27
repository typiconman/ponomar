package net.ponomar.readings.utility;

import java.util.Vector;

import net.ponomar.readings.Matins;
import net.ponomar.utility.OrderedHashtable;
import net.ponomar.utility.StringOp;

public class ClassifyMatins extends ClassifyReadings {

    public ClassifyMatins() {
    }

    public ClassifyMatins(OrderedHashtable readingsInA) {
		StringOp Testing = new StringOp();
        ParameterValues.setDayInfo(Matins.getInformation3().getDayInfo());
        classify(readingsInA);
    }

    public ClassifyMatins(Matins matins, OrderedHashtable readingsInA, StringOp ParameterValues) {
		classify(readingsInA);

    }
    private void classify(OrderedHashtable readingsIn)
    {
        //Initialise Information.
        Information2=new OrderedHashtable();
        /*try {
            FileReader frf = new FileReader(findLanguage.langFileFind(ParameterValues.dayInfo.get("LS").toString(), "xml/Commands/Matins.xml"));
            //System.out.println(findLanguage.langFileFind(ParameterValues.dayInfo.get("LS").toString(), "xml/Commands/DivineLiturgy.xml"));
            //DivineLiturgy a1 = new classifyReadin();
            QDParser.parse(this, frf);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        Vector paschalV = (Vector) readingsIn.get("Readings");
        Vector paschalR = (Vector) readingsIn.get("Rank");
        Vector paschalT = (Vector) readingsIn.get("Tag");

        dailyV = new Vector();
        dailyR = new Vector();
        dailyT = new Vector();


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
        

        Suppress();
        //LeapReadings();


    }

    private void Suppress() {
        //THIS FUNCTION CONSIDERS WHAT HOLIDAYS ARE CURRENTLY OCCURING AND RETURNS THE READINGS FOR THE DAY, WHERE SUPPRESSED CONTAINS THE READINGS THAT WERE SUPPRESSED.
        int doy = Integer.parseInt(ParameterValues.getDayInfo().get("doy").toString());
        int dow = Integer.parseInt(ParameterValues.getDayInfo().get("dow").toString());
        int nday = Integer.parseInt(ParameterValues.getDayInfo().get("nday").toString());
        int ndayF = Integer.parseInt(ParameterValues.getDayInfo().get("ndayF").toString());
        int ndayP = Integer.parseInt(ParameterValues.getDayInfo().get("ndayP").toString());
        int dRank = Integer.parseInt(ParameterValues.getDayInfo().get("dRank").toString());
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
            for (int k = 0; k < menaionV.size(); k++) {
                suppressedV.add(menaionV.get(k));
                suppressedR.add(menaionV.get(k));
                suppressedT.add(menaionV.get(k));
            }
            menaionV.clear();
            menaionV.clear();
            menaionV.clear();
            return;
        }

        return;
    }
}