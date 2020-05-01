package net.ponomar.readings.utility;

import java.util.Enumeration;
import java.util.HashMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import net.ponomar.parsing.utility.DocHandler;
import net.ponomar.utility.Constants;
 
 
import net.ponomar.utility.StringOp;

public class ClassifyReadings implements DocHandler {

	/**
	 * 
	 */
	protected LinkedHashMap<String, ArrayList<String>> information2;
	public List dailyV = new ArrayList();
	public List dailyR = new ArrayList();
	public List dailyT = new ArrayList();
	public List menaionV = new ArrayList();
	public List menaionR = new ArrayList();
	public List menaionT = new ArrayList();
	public List suppressedV = new ArrayList();
	public List suppressedR = new ArrayList();
	public List suppressedT = new ArrayList();
	protected StringOp parameterValues = new StringOp();

	public ClassifyReadings() {
		super();
	}

	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startElement(String elem, HashMap<String, String> table) {
	    // THE TAG COULD CONTAIN A COMMAND Cmd
	    // THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
	    // TODAY'S INFORMATION IN dayInfo.
	    if (table.get("Cmd") != null) {
	        // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
	
	        if (!parameterValues.evalbool(table.get("Cmd"))) {
	            return;
	        }
	    }
	
	    if (elem.equals(Constants.COMMAND)) {
	        //THIS WILL STORE ALL THE POSSIBLE COMMANDS FOR A GIVEN SITUATION AND ALLOW THE RESULTS TO BE DETEMINED.
	        String name = table.get("Name");
	        String value = table.get(Constants.VALUE);
	        //IF THE GIVEN name OCCURS IN THE information HASHTABLE THAN AUGMENT ITS VALUES.
	        //System.out.println("==============================\nTesting Information\n++++++++++++++++++++");
	        if (information2.containsKey(name)) {
	            ArrayList<String> previous = information2.get(name);
	            previous.add(value);
	            information2.put(name, previous);
	        } else {
	            ArrayList<String> vect = new ArrayList<>();
	            vect.add(value);
	            information2.put(name, vect);
	        }
	
	    }
	    //ALL WE CARE ABOUT ARE THE SCRIPTURE READINGS
	}

	protected void leapReadings() {
	    //SKIPS THE READINGS IF THERE ARE ANY BREAKS!
	    int doy = Integer.parseInt(parameterValues.getDayInfo().get("doy").toString());
	    int dow = Integer.parseInt(parameterValues.getDayInfo().get("dow").toString());
	    int nday = Integer.parseInt(parameterValues.getDayInfo().get("nday").toString());
	    int ndayF = Integer.parseInt(parameterValues.getDayInfo().get(Constants.NDAY_F).toString());
	    int ndayP = Integer.parseInt(parameterValues.getDayInfo().get(Constants.NDAY_P).toString());
	
	    //IN ALL CASES ONLY THE PENTECOSTARION READINGS ARE EFFECTED!
	    //ArrayList empty = new ArrayList();
	    //USING THE NEWER VERSION OF STORED VALUES
	    //EACH OF THE STORED COMMANDS ARE EVALUATED IF ANY ARE TRUE THEN THE READINGS ARE SKIPPED IF THERE ARE ANY FURTHER READINGS ON THAT DAY.
	    int available = menaionV.size();
	
	    if (available > 0) {
	        ArrayList<String> list = information2.get("Suppress");
	        if (list != null) {
	            for (Enumeration<String> e2 = Collections.enumeration(list); e2.hasMoreElements();) {
	                String command = e2.nextElement();
	                if (parameterValues.evalbool(command)) {
	                    //THE CURRENT COMMAND WAS TRUE AND THE SEQUENTITIAL READING IS TO BE SKIPPED
	                    dailyV.clear();
	                    dailyR.clear();
	                    dailyT.clear();
	                    suppressedV.clear();
	                    suppressedR.clear();
	                    suppressedT.clear();
	                    return;
	                }
	
	            }
	        }
	    }
	
	}

	@Override
	public void endElement(String tag) throws Exception {
		
	}

	@Override
	public void text(String str) throws Exception {
		
	}

	

}