package net.ponomar.parsing;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;

import net.ponomar.internationalization.LanguagePack;
import net.ponomar.parsing.utility.DocHandler;
import net.ponomar.parsing.utility.QDParser;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 
 
import net.ponomar.utility.StringOp;

/***********************************************************************
THIS MODULE READS XML FILES THAT CONTAIN THAT ARE OF THE <COMMEMORATION> TYPE 
AND STORES THE INFORMATION IN A MANNER USUABLE BY OTHER COMPONENTS
OF THE PROGRAMME.

(C) 2009, 2015 YURI SHARDT. ALL RIGHTS RESERVED.
* 
* 2015 Changes: Updates and simplifications due to changes in the overall standard.

PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE CODE
PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES THEREOF.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 ***********************************************************************/
public class Commemoration implements DocHandler {

    private static final String AUTHOR = "Author";
	private static final String LIVES = "/xml/lives/";
	private static boolean read = false;
    //private LanguagePack Text=new LanguagePack();
    //private String[] ServiceNames=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
    //private String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
    private LinkedHashMap<String, Object> information;
    private LinkedHashMap<String, Object> readings;
    private LinkedHashMap<String, Object> grammar;
    private LinkedHashMap<String, String> variable;
    private String textR;
    private boolean readRH = false;
    private LinkedHashMap royalHours;
    private String elemRH;
    private LinkedHashMap<String, Object> value;
    private LinkedHashMap<String, LinkedHashMap<String, Object>> serviceInfo;
    private String location1;
    private boolean readService = false;
    private LanguagePack text;// = new LanguagePack();
    private String[] commNames;// = Text.obtainValues((String) Text.Phrases.get("Commemoration"));
    private String errorName;//=(String)Text.Phrases.get("Commemoration3");
    private Helpers helper;
    private boolean combine = false;
    private boolean skipElement = false;
    private boolean presentPropers=false;
    private StringOp analyse=new StringOp();

	public Commemoration(String sId, String cId, LinkedHashMap<String, Object> dayInfo) {
		analyse.setDayInfo(dayInfo);
		text = new LanguagePack(dayInfo);
		commNames = text.obtainValues(text.getPhrases().get("Commemoration"));
		errorName = text.getPhrases().get("Commemoration3");
		information = new LinkedHashMap<>();
		readings = new LinkedHashMap<>();
		royalHours = new LinkedHashMap();
		grammar = new LinkedHashMap<>();
		information.put("SID", sId);
		information.put("CID", cId);
		helper = new Helpers(analyse.getDayInfo());
		serviceInfo = new LinkedHashMap<>();
		readCommemoration(sId, cId);
	}

    public Commemoration() {
        information = new LinkedHashMap<>();
        readings = new LinkedHashMap<>();
        helper = new Helpers(analyse.getDayInfo());
    }

    public void readCommemoration(String sId, String cId) //throws IOException
    {
         String fileName="";
        try {
            combine = true;
            String language = analyse.getDayInfo().get("LS").toString();
            String[] pathS = language.split("/");
            int path = pathS.length;
            StringBuilder pathF = new StringBuilder();
           
            for (int i = -1; i < path; i++) {
                if (i == -1) {
                    pathF = new StringBuilder();
                } else {
                    pathF.append("/").append(pathS[i]);
                }
                //System.out.println("pathF=" + pathF);


                fileName = Constants.LANGUAGES_PATH + pathF + LIVES + cId + ".xml";
                File f = new File(fileName);             

                if (f.exists()) {

                    BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));
                    QDParser.parse(this, frf);                    
                } else {
                    //The given file does not exist, do nothing, it is not a calamity!
                }
            }
            combine = true;
            //BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(StringOp.dayInfo.get("LS").toString(), LIVES + CId + ".xml")), "UTF8"));
            //QDParser.parse(this, frf);
        } catch (Exception e) {
            System.out.println("In file name, "+fileName+" an error occurred of type: ");
            e.printStackTrace();
        }
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String elem, HashMap<String, String> table) {

        // THE TAG COULD CONTAIN A COMMAND Cmd
        // THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
        // TODAY'S INFORMATION IN dayInfo.
        // IT WOULD BE VERY RARE IN THIS CASE
        skipElement = false;
        if (table.get("Cmd") != null) {
            // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
            if (!analyse.evalbool(table.get("Cmd"))) {


                skipElement = true;


                return;
            }
        }
        //if(elem.equals("LANGUAGE"))
        //{
        read = true;
        //System.out.println(table.get("Cmd").toString());
        //      return;
        //}
        if (elem.equals("SERVICE") && read) {
            readService = true;
            if (serviceInfo == null) {
                serviceInfo = new LinkedHashMap<>();               
            }

            location1 = "";

            if (table.get("Type") != null) {
                information.put("Rank", Integer.parseInt(table.get("Type")));
            }
            return;
        }
        if (readService && read) {
            location1 += "/" + elem;
            //elemRH=elem;
            value = new LinkedHashMap<>();
            table.forEach((k,v) -> value.put(k, v));
            return;
        }

        if (readRH && read) {
            elemRH = elem;
            value = new LinkedHashMap<>();
            table.forEach((k,v) -> value.put(k, v));

        }
        if (elem.equals("SCRIPTURE") && read) {
            String type = table.get("Type");
            String reading = table.get(Constants.READING);
            if (readings.containsKey(type)) {

                // ADD THIS READING TO OTHERS OF THE SAME TYPE
                ArrayList<String> vect = (ArrayList<String>) readings.get(type);
                vect.add(reading);
                readings.put(type, vect);
            } else {
                // CREATE A NEW TYPE WITH A COLLECTION INCLUDING THIS READING
                ArrayList<String> vect = new ArrayList<>();
                vect.add(reading);
                readings.put(type, vect);
            }
            information.put("Scripture", readings);
            //Information.put("presentPropers",true);
        }
        if (elem.equals("GRAMMAR") && read) {
            //THIS SHOULD ONLY BE READ ONCE PER LANGUAGE AND PASS!
            if (grammar == null) {
                grammar = new LinkedHashMap<>();
            }
            table.forEach((k,v) -> grammar.put(k, v));
            information.put("Grammar", grammar);
        }
        //if (elem.equals("SERVICE") && read) {

        //Information.put("Cycle",table.get("Cycle").toString());
        // }
        if (elem.equals("ICON") && read) {
            information.put("Icon", table.get("Id"));
        }
        if (elem.equals("TROPARION") && read) {
            variable = new LinkedHashMap<>();
            variable.put("Tone", table.get("Tone"));
            if (table.get(AUTHOR) != null){
            variable.put(AUTHOR, table.get(AUTHOR));
            }
            //Information.put("presentPropers",true);
        }
        if (elem.equals("KONTAKION") && read) {
            variable = new LinkedHashMap<>();
            variable.put("Tone", table.get("Tone"));
            if (table.get(AUTHOR) != null){
            variable.put(AUTHOR, table.get(AUTHOR));
            //Information.put("presentPropers",true);
            }
        }
        if (elem.equals("NAME") && read) {
            //grammar=new OrderedHashtable();
            //System.out.println("Hello World: This is Name testing!");
            table.forEach((k,v) -> grammar.put(k, v));
            information.put(Constants.GRAMMAR, grammar);
            //Information.put("Nominative", table.get("Nominative").toString());
            //Information.put("Short", table.get("Short").toString());
            //Information.put("ShortFor", table.get("ShortF").toString());

        }
        if (elem.equals("LIFE") && read) {
            if (table.get("Id")!=null){
            information.put("LifeID",table.get("Id"));
        }
            if (table.get(Constants.COPYRIGHT)!=null){
            information.put("LifeCopyright",table.get(Constants.COPYRIGHT));
            }
         }

    }

    public void endElement(String elem) {
        if (skipElement) {
            skipElement = false;
            return;
        }
        if (elem.equals(Constants.LANGUAGE)) {
            read = false;
        }
        if (elem.equals("ROYALHOURS")) {
            readRH = false;
        }
        if (elem.equals("SERVICE") && read) {
            //Information.put("presentPropers",true);
            readService = false;

            //System.out.println(ServiceInfo);
            //if(ServiceInfo.containsKey("ROYALHOURS/VERSE")){
            //System.out.println(ServiceInfo.get("ROYALHOURS/VERSE"));
            //}
        }
        if (readService && read) {
            //System.out.println("Services");
            //System.out.println(textR);
            //System.out.println(Location1);
            //System.out.println(value.get("Type"));

            /*if(elem.equals("VERSE")){
            System.out.println(textR);
            }*/
            //System.out.println("In endElement, I saw the following elements: "+elem);
            if (textR != null) {
                value.put("text", textR);                
            }
            if (serviceInfo.containsKey(location1)) {
                LinkedHashMap<String, Object> stuff = serviceInfo.get(location1);
                stuff.put((String) value.get("Type"), value);
                serviceInfo.put(location1, stuff);
                /*if(elem.equals("VERSE")){
                System.out.println(value.get("Type"));
                System.out.println(stuff.get(value.get("Type")));
                System.out.println(ServiceInfo.get(Location1));
                }*/
                //Location1=Location1.substring(0,Location1.lastIndexOf("/"));
            } else {
                // CREATE A NEW ORDEREDHASHTABLE TO STORE THE DATA
                LinkedHashMap<String, Object> stuff = new LinkedHashMap<>();

                if (!(value.get("Type") == null)) {
                    //There are instances of this info
                    stuff.put((String) value.get("Type"), value);

                } else {
                    //There are no other instances of this info
                    if (elemRH == null || value == null) {
                        //System.out.println("A null set of values was encountered. Why? At point elemRH = "+elemRH+" and value = "+value+" and location = "+Location1);
                        if (location1.lastIndexOf('/') == -1) {
                            location1 = "";
                            return;

                        }
                        //System.out.println("****\n" + Location1 + "\n****\n");
                        location1 = location1.substring(0, location1.lastIndexOf('/'));
                        return;
                    }
                    stuff.put(elemRH, value);
                    serviceInfo.put(location1, value);
                }
                serviceInfo.put(location1, stuff);
            }
            value = new LinkedHashMap<>();
            location1 = location1.substring(0, location1.lastIndexOf('/'));             

            //return;
        }

        if (elem.equals("LIFE") && read) {
            information.put("LIFE", textR);
        }
        /*if(elem.equals("NAME") && read)
        {
        Information.put("Name",textR);
        }*/
    }

    public void text(String text) {
        textR = text;        
    }

	public String getGrammar(String value) {
		if (Integer.parseInt(information.get("CID").toString()) != -1) {
			grammar = (LinkedHashMap<String, Object>) information.get(Constants.GRAMMAR);

			if (value.equals("")) {
				// System.out.println( Information.get("Name").toString());
				return grammar.get(Constants.NOMINATIVE).toString();
			}
			try {
				return grammar.get(value).toString();
			} catch (Exception e) {
				if (grammar != null) {
					if (grammar.get(Constants.NOMINATIVE) != null) {
						return grammar.get(Constants.NOMINATIVE).toString();
					} else {
						return errorName;
					}
				} else {
					return errorName;
				}
			}
		} else {
			return information.get("Name").toString();
		}
	}

	public int getRank() {

		if (!information.containsKey("Rank")) {
			String cId = information.get("CID").toString();
			int cIdN = Integer.parseInt(cId);
			// System.out.println("CID: "+CID+"; length: "+CID.length());
			if ((cIdN >= 9000 && cIdN < 9900) && cId.length() == 4) {
				information.put("Rank", "-2");
				return -2;
			}
			return 0;
		}
		// System.out.println(Information.get("Rank").toString());
		int rank = Integer.parseInt(information.get("Rank").toString());
		String cId = information.get("CID").toString();
		int cIdN = Integer.parseInt(cId);
		if ((cIdN >= 9000 && cIdN < 9900) && cId.length() == 4) {
			if (rank < 2) {
				information.put("Rank", "-2");
				return -2;
			}
		}
		return rank;
	}

    public String getSId() {
        if (!information.containsKey("SID")) {
            return "";
        }
        return information.get("SID").toString();
    }

    public String getCId() {
        if (!information.containsKey("CID")) {
            return "";
        }
        return information.get("CID").toString();
    }

    public String getName() {
        return getGrammar(Constants.NOMINATIVE);
        /*if (!Information.containsKey("Nominative")){
        return "";
        }
        return Information.get("Nominative").toString();*/
    }

    public String getIcon() {
        return information.get("Icon").toString();
    }

	public LinkedHashMap<String, ArrayList<String>> getDisplayIcons() {

		// Ordered List of the Icons
		ArrayList<String> iconImages = new ArrayList<>();
		ArrayList<String> iconNames = new ArrayList<>();

		String cId = information.get("CID").toString();
		String nameF = getGrammar("Short");
		String[] iconSearch = text.obtainValues(text.getPhrases().get("IconSearch"));

		File fileNew = new File(helper.langFileFind(analyse.getDayInfo().get("LS").toString(),
				Constants.ICONS_RESOURCE_PATH + cId + "/0.jpg"));
		int countSearch = 0;
		String languageString = analyse.getDayInfo().get("LS").toString();

		while (!(fileNew.exists()) && countSearch < iconSearch.length) {
			languageString = iconSearch[countSearch];
			fileNew = new File(
					helper.langFileFind(iconSearch[countSearch], Constants.ICONS_RESOURCE_PATH + cId + "/0.jpg"));
			countSearch += 1;
		}

		// The above code will add the Greek Icons and this will allow me to do what I
		// wish to do!!!

		int counterI = 0;

		// System.out.println(fileNew.getAbsolutePath());
		while (fileNew.exists()) {

			iconImages.add(fileNew.toString());
			iconNames.add(nameF);
			counterI += 1;
			fileNew = new File(
					helper.langFileFind(languageString, Constants.ICONS_RESOURCE_PATH + cId + "/" + counterI + ".jpg"));
		}
		File file = new File(Constants.ICONS_LOCATION + cId + ".jpg");
		if (file.exists()) {
			iconImages.add(file.toString());
			iconNames.add(nameF);
		}

		LinkedHashMap<String, ArrayList<String>> finalI = new LinkedHashMap<>();
		finalI.put("Images", iconImages);
		finalI.put("Names", iconNames);
		return finalI;
	}

    public String getID() {
        return information.get("ID").toString();
    }

    public String getCycle() {
        return information.get("Cycle").toString();
    }

    public LinkedHashMap<String, Object> getReadings() {
        //return (OrderedHashtable) Information.get("Scripture");
        //This is a list of all possible cases:
        //1stHour,3rdHour,6thHour,9thHour,apostol,gospel,VESPERS,MATINS
        readings = new LinkedHashMap<>();
        LinkedHashMap<String, Object> readingsT = getServiceNode("/VESPERS/SCRIPTURE");
        if (readingsT != null) {
            readings.put("VESPERS", readingsT);            
        }
        readingsT = getServiceNode("/PRIMES/SCRIPTURE");
        if (readingsT != null) {
            readings.put("1st hour", readingsT);
        }
        readingsT = getServiceNode("/TERCE/SCRIPTURE");
        if (readingsT != null) {
            readings.put("3rd hour", readingsT);
        }
        readingsT = getServiceNode("/SEXTE/SCRIPTURE");
        if (readingsT != null) {
            readings.put("6th hour", readingsT);
        }
        readingsT = getServiceNode("/NONE/SCRIPTURE");
        if (readingsT != null) {
            readings.put("9th hour", readingsT);
        }
        readingsT = getServiceNode("/MATINS/SCRIPTURE");
        if (readingsT != null) {
            readings.put("MATINS", readingsT);
        }
        readingsT = getServiceNode("/LITURGY/SCRIPTURE");
        if (readingsT != null) {
            readings.put("LITURGY", readingsT);
        }


        return readings;
    }

    public LinkedHashMap<String, Object> getServiceNode(String node) {
        if (serviceInfo != null) {
            if (serviceInfo.containsKey(node)) {
                return serviceInfo.get(node);
            }
        }
        //System.out.println(CommNames[2] + Node);
        return null;

    }

    public LinkedHashMap<String, String> getService(String node, String type) {
        //System.out.println(ServiceInfo);
        //System.out.println("\n\n");
        //System.out.println(Node+"/"+Type);
        //System.out.println(ServiceInfo.get(Node));
        if (serviceInfo.containsKey(node)) {
        	LinkedHashMap<String, Object> stuff = serviceInfo.get(node);

            if (stuff.containsKey(type)) {
                return (LinkedHashMap<String, String>) stuff.get(type);
            } else {
                System.out.println(commNames[0] + node + commNames[1] + type);
                return null;
            }
        } else {
            System.out.println(commNames[2] + node);
            return null;
        }
    }

	public LinkedHashMap getRH(String node, String type) {

		if (royalHours.containsKey(node)) {

			LinkedHashMap stuff = (LinkedHashMap) royalHours.get(node);
			// System.out.println(stuff);
			if (stuff.containsKey(type)) {
				return (LinkedHashMap) stuff.get(type);
			} else {
				System.out.println(commNames[3]);
				return new LinkedHashMap();
			}
		} else {
			System.out.println(commNames[3]);
			return null;
		}

	}
	
    public boolean checkLife(){
        //Checks whether the given commemoration has an associated life or not

        return information.get("LIFE") != null;
    }
    public boolean checkIcon(){
        //Checks whether the given commemoration has any icons assoicated with it
    	LinkedHashMap<String, ArrayList<String>> checkIcon=getDisplayIcons();
        return checkIcon.size() > 0;
    }
    public boolean checkPropers(){
        //Checks whether there are any associated propers for the given commemoration that could be display.
        //At present only cares about the tropar and kondak.
        /*System.out.println(Information.get("CID"));
        OrderedHashtable check1=getService("/LITURGY/TROPARION","1");
        System.out.println(check1);
        if (check1 != null){
            System.out.println("This is CCID: "+ Information.get("CID")+" and check1 form: "+check1);
            return true;
        }*/
        /*if (Information.get("presentPropers")!=null){
        boolean check= Boolean.parseBoolean(Information.get("presentPropers").toString());
        if (check){
            return true;
        }
        }*/
        return false;

    }

    public String getLife(){
        //Checks whether the given commemoration has an associated life or not

        if (information.get("LIFE")!= null){
            return information.get("LIFE").toString();
        }
        else
        {
            return null;
        }
    }
    public String getLifeCopyright(){
        //Checks whether the given commemoration has an associated life or not

        if (information.get("LifeCopyright")!= null){
            return information.get("LifeCopyright").toString();
        }
        else
        {
            return null;
        }
    }


    public static void main(String[] argz) {
    	LinkedHashMap<String, Object> dayInfo = new LinkedHashMap<>();
        dayInfo.put("LS", "en/");
        dayInfo.put("dow", "1");
        //StringOp.dayInfo.put("")
        //Commemoration Paramony = new Commemoration("P_3174");    //Paramony of Christmas
        System.out.println("THIS IS RUNNING ON DEBUG MODE, USING THE FILE FOR the Paramony of Christmas");
        //OrderedHashtable stuff=Paramony.getRH("Idiomel","11");
        //System.out.println(Paramony.getService("/ROYALHOURS/IDIOMEL","13"));
        //System.out.println(Paramony .ServiceInfo());
        //System.out.println(Paramony.getRH("IDIOMEL","11"));
        //System.out.println(Paramony);
        Commemoration paramony = new Commemoration("0", "9001",dayInfo); //Forefeast of Christmas
        //System.out.println(Paramony.getService("/MATINS/KONTAKION","1"));
        System.out.println(paramony.getRank());
        //System.out.println(Paramony.Information.get("LIFE"));
        System.out.println(paramony.getGrammar(Constants.NOMINATIVE));
        System.out.println("Rank = "+paramony.getRank());
        System.out.println(paramony.getService("/LITURGY/TROPARION", "1"));
        System.out.println(paramony.getService("/LITURGY/KONTAKION", "1"));
        System.out.println(paramony.getService("/VESPERS/SCRIPTURE", "3"));
        System.out.println(paramony.getReadings().get("VESPERS"));
    }
}
