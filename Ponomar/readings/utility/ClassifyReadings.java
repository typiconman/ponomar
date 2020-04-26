package Ponomar.readings.utility;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import Ponomar.parsing.DocHandler;
import Ponomar.utility.OrderedHashtable;
import Ponomar.utility.StringOp;

public class ClassifyReadings implements DocHandler {

	/**
	 * 
	 */
	private final String configFileName = "ponomar.config";
	private final String triodionFileName = "xml/triodion/";
	private final String pentecostarionFileName = "xml/pentecostarion/";
	protected OrderedHashtable Information2;
	public Vector dailyV = new Vector();
	public Vector dailyR = new Vector();
	public Vector dailyT = new Vector();
	public Vector menaionV = new Vector();
	public Vector menaionR = new Vector();
	public Vector menaionT = new Vector();
	public Vector suppressedV = new Vector();
	public Vector suppressedR = new Vector();
	public Vector suppressedT = new Vector();
	protected StringOp ParameterValues = new StringOp();

	public ClassifyReadings() {
		super();
	}

	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startElement(String elem, Hashtable table) {
	    // THE TAG COULD CONTAIN A COMMAND Cmd
	    // THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
	    // TODAY'S INFORMATION IN dayInfo.
	    if (table.get("Cmd") != null) {
	        // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
	
	        if (ParameterValues.evalbool(table.get("Cmd").toString()) == false) {
	            return;
	        }
	    }
	
	    if (elem.equals("COMMAND")) {
	        //THIS WILL STORE ALL THE POSSIBLE COMMANDS FOR A GIVEN SITUATION AND ALLOW THE RESULTS TO BE DETEMINED.
	        String name = (String) table.get("Name");
	        String value = (String) table.get("Value");
	        //IF THE GIVEN name OCCURS IN THE information HASHTABLE THAN AUGMENT ITS VALUES.
	        //System.out.println("==============================\nTesting Information\n++++++++++++++++++++");
	        if (Information2.containsKey(name)) {
	            Vector previous = (Vector) Information2.get(name);
	            previous.add(value);
	            Information2.put(name, previous);
	        } else {
	            Vector vect = new Vector();
	            vect.add(value);
	            Information2.put(name, vect);
	        }
	
	    }
	    //ALL WE CARE ABOUT ARE THE SCRIPTURE READINGS
	}

	protected void LeapReadings() {
	    //SKIPS THE READINGS IF THERE ARE ANY BREAKS!
	    int doy = Integer.parseInt(ParameterValues.getDayInfo().get("doy").toString());
	    int dow = Integer.parseInt(ParameterValues.getDayInfo().get("dow").toString());
	    int nday = Integer.parseInt(ParameterValues.getDayInfo().get("nday").toString());
	    int ndayF = Integer.parseInt(ParameterValues.getDayInfo().get("ndayF").toString());
	    int ndayP = Integer.parseInt(ParameterValues.getDayInfo().get("ndayP").toString());
	
	    //IN ALL CASES ONLY THE PENTECOSTARION READINGS ARE EFFECTED!
	    Vector empty = new Vector();
	    //USING THE NEWER VERSION OF STORED VALUES
	    //EACH OF THE STORED COMMANDS ARE EVALUATED IF ANY ARE TRUE THEN THE READINGS ARE SKIPPED IF THERE ARE ANY FURTHER READINGS ON THAT DAY.
	    int available = menaionV.size();
	
	    if (available > 0) {
	        Vector vect = (Vector) Information2.get("Suppress");
	        if (vect != null) {
	            for (Enumeration e2 = vect.elements(); e2.hasMoreElements();) {
	                String Command = (String) e2.nextElement();
	                if (ParameterValues.evalbool(Command)) {
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
	
	    return;
	}

	@Override
	public void endElement(String tag) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void text(String str) throws Exception {
		// TODO Auto-generated method stub
		
	}

}