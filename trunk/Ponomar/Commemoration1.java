package Ponomar;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;

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
public class Commemoration1 implements DocHandler {

    private final static String Location = "xml/Services/menaion/";   // THE LOCATION OF THE BASIC SERVICE RULES
    private final static String LocationT = "xml/triodion/";
    private final static String LocationP = "xml/pentecostarion/";
    private static boolean read = false;
    private String filename;
    private int lineNumber;
    //private LanguagePack Text=new LanguagePack();
    //private String[] ServiceNames=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
    //private String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
    private OrderedHashtable Information;
    private OrderedHashtable readings;
    private OrderedHashtable grammar;
    private OrderedHashtable variable;
    private String textR;
    private boolean readRH = false;
    private OrderedHashtable RoyalHours;
    private String elemRH;
    private OrderedHashtable value;
    private OrderedHashtable ServiceInfo;
    private String Location1;
    private boolean readService = false;
    private LanguagePack Text;// = new LanguagePack();
    private String[] CommNames;// = Text.obtainValues((String) Text.Phrases.get("Commemoration"));
    private String errorName;//=(String)Text.Phrases.get("Commemoration3");
    private Helpers helper;
    private boolean combine = false;
    private boolean skipElement = false;
    private boolean presentPropers=false;
    private StringOp Analyse=new StringOp();

    protected Commemoration1(String SId, String CId,OrderedHashtable dayInfo) {
        Analyse.dayInfo=dayInfo;
       Text = new LanguagePack(dayInfo);
    CommNames = Text.obtainValues((String) Text.Phrases.get("Commemoration"));
    errorName=(String)Text.Phrases.get("Commemoration3");
        Information = new OrderedHashtable();
        readings = new OrderedHashtable();
        RoyalHours = new OrderedHashtable();
        grammar = new OrderedHashtable();
        Information.put("SID", SId);
        Information.put("CID", CId);
        helper = new Helpers(Analyse.dayInfo);
        ServiceInfo = new OrderedHashtable();
        readCommemoration(SId, CId);

    }

    protected Commemoration1() {
        Information = new OrderedHashtable();
        readings = new OrderedHashtable();
        helper = new Helpers(Analyse.dayInfo);
    }

    public void readCommemoration(String SId, String CId) //throws IOException
    {
         String FileName="";
        try {
            combine = true;
            String language = Analyse.dayInfo.get("LS").toString();
            String[] pathS = language.split("/");
            int path = pathS.length;
            String pathF = "";
           
            for (int i = -1; i < path; i++) {
                if (i == -1) {
                    pathF = "";
                } else {
                    pathF += "/" + pathS[i];
                }
                //System.out.println("pathF=" + pathF);


                FileName = "Ponomar/languages/" + pathF + "/xml/lives/" + CId + ".xml";
                File f = new File(FileName);             

                if (f.exists()) {

                    BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(FileName), "UTF8"));
                    QDParser.parse(this, frf);                    
                } else {
                    //The given file does not exist, do nothing, it is not a calamity!
                }
            }
            combine = true;
            //BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(StringOp.dayInfo.get("LS").toString(), "xml/lives/" + CId + ".xml")), "UTF8"));
            //QDParser.parse(this, frf);
        } catch (Exception e) {
            System.out.println("In file name, "+FileName+" an error occurred of type: ");
            e.printStackTrace();
        }
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String elem, Hashtable table) {

        // THE TAG COULD CONTAIN A COMMAND Cmd
        // THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
        // TODAY'S INFORMATION IN dayInfo.
        // IT WOULD BE VERY RARE IN THIS CASE
        skipElement = false;
        if (table.get("Cmd") != null) {
            // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
            if (Analyse.evalbool(table.get("Cmd").toString()) == false) {


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
            if (ServiceInfo == null) {
                ServiceInfo = new OrderedHashtable();               
            }

            Location1 = new String();

            if (table.get("Type") != null) {
                Information.put("Rank", Integer.parseInt(table.get("Type").toString()));
            }
            return;
        }
        if (readService && read) {
            Location1 += "/" + elem;
            //elemRH=elem;
            value = new OrderedHashtable();
            for (Enumeration e = table.keys(); e.hasMoreElements();) {
                String type = (String) e.nextElement();
                value.put(type, table.get(type));
            }            
            return;
        }

        if (readRH && read) {
            elemRH = elem;
            value = new OrderedHashtable();
            for (Enumeration e = table.keys(); e.hasMoreElements();) {
                String type = (String) e.nextElement();
                value.put(type, table.get(type));
            }
        }
        if (elem.equals("SCRIPTURE") && read) {
            String type = (String) table.get("Type");
            String reading = (String) table.get("Reading");
            if (readings.containsKey(type)) {

                // ADD THIS READING TO OTHERS OF THE SAME TYPE
                Vector vect = (Vector) readings.get(type);
                vect.add(reading);
                readings.put(type, vect);
            } else {
                // CREATE A NEW TYPE WITH A COLLECTION INCLUDING THIS READING
                Vector vect = new Vector();
                vect.add(reading);
                readings.put(type, vect);
            }
            Information.put("Scripture", readings);
            //Information.put("presentPropers",true);
        }
        if (elem.equals("GRAMMAR") && read) {
            //THIS SHOULD ONLY BE READ ONCE PER LANGUAGE AND PASS!
            if (grammar == null) {
                grammar = new OrderedHashtable();
            }
            for (Enumeration e = table.keys(); e.hasMoreElements();) {
                String type = (String) e.nextElement();
                grammar.put(type, table.get(type));
            }
            Information.put("Grammar", grammar);
        }
        //if (elem.equals("SERVICE") && read) {

        //Information.put("Cycle",table.get("Cycle").toString());
        // }
        if (elem.equals("ICON") && read) {
            Information.put("Icon", table.get("Id").toString());
        }
        if (elem.equals("TROPARION") && read) {
            variable = new OrderedHashtable();
            variable.put("Tone", table.get("Tone").toString());
            if (table.get("Author") != null){
            variable.put("Author", table.get("Author").toString());
            }
            //Information.put("presentPropers",true);
        }
        if (elem.equals("KONTAKION") && read) {
            variable = new OrderedHashtable();
            variable.put("Tone", table.get("Tone").toString());
            if (table.get("Author") != null){
            variable.put("Author", table.get("Author").toString());
            //Information.put("presentPropers",true);
            }
        }
        if (elem.equals("NAME") && read) {
            //grammar=new OrderedHashtable();
            //System.out.println("Hello World: This is Name testing!");
            for (Enumeration e = table.keys(); e.hasMoreElements();) {
                String type = (String) e.nextElement();
                grammar.put(type, table.get(type));
            }
            Information.put("grammar", grammar);
            //Information.put("Nominative", table.get("Nominative").toString());
            //Information.put("Short", table.get("Short").toString());
            //Information.put("ShortFor", table.get("ShortF").toString());

        }
        if (elem.equals("LIFE") && read) {
            if (table.get("Id")!=null){
            Information.put("LifeID",table.get("Id"));
        }
            if (table.get("Copyright")!=null){
            Information.put("LifeCopyright",table.get("Copyright"));
            }
         }

    }

    public void endElement(String elem) {
        if (skipElement) {
            skipElement = false;
            return;
        }
        if (elem.equals("LANGUAGE")) {
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
            if (ServiceInfo.containsKey(Location1)) {
                OrderedHashtable stuff = (OrderedHashtable) ServiceInfo.get(Location1);
                stuff.put(value.get("Type"), value);
                ServiceInfo.put(Location1, stuff);
                /*if(elem.equals("VERSE")){
                System.out.println(value.get("Type"));
                System.out.println(stuff.get(value.get("Type")));
                System.out.println(ServiceInfo.get(Location1));
                }*/
                //Location1=Location1.substring(0,Location1.lastIndexOf("/"));
            } else {
                // CREATE A NEW ORDEREDHASHTABLE TO STORE THE DATA
                OrderedHashtable stuff = new OrderedHashtable();

                if (!(value.get("Type") == null)) {
                    //There are instances of this info
                    stuff.put(value.get("Type"), value);

                } else {
                    //There are no other instances of this info
                    if (elemRH == null || value == null) {
                        //System.out.println("A null set of values was encountered. Why? At point elemRH = "+elemRH+" and value = "+value+" and location = "+Location1);
                        if (Location1.lastIndexOf("/") == -1) {
                            Location1 = "";
                            return;

                        }
                        //System.out.println("****\n" + Location1 + "\n****\n");
                        Location1 = Location1.substring(0, Location1.lastIndexOf("/"));
                        return;
                    }
                    stuff.put(elemRH, value);
                    ServiceInfo.put(Location1, value);
                }
                ServiceInfo.put(Location1, stuff);
            }
            value = new OrderedHashtable();
            Location1 = Location1.substring(0, Location1.lastIndexOf("/"));             

            //return;
        }

        if (elem.equals("LIFE") && read) {
            Information.put("LIFE", textR);
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
        if (Integer.parseInt(Information.get("CID").toString()) != -1) {
            grammar = (OrderedHashtable) Information.get("grammar");

            if (value.equals("")) {
                //System.out.println( Information.get("Name").toString());
                return grammar.get("Nominative").toString();
            }
            try {
                return grammar.get(value).toString();
            } catch (Exception e) {
                if (grammar != null) {
                    if (grammar.get("Nominative") != null) {
                        return grammar.get("Nominative").toString();
                    } else {
                        return errorName;
                    }
                } else {
                    return errorName;
                }
            }
        } else {
            return Information.get("Name").toString();
        }
    }

    public int getRank() {

        if (!Information.containsKey("Rank")) {
            String CID = Information.get("CID").toString();
            int Cidn=Integer.parseInt(CID);
            //System.out.println("CID: "+CID+"; length: "+CID.length());
            if ((Cidn>=9000 && Cidn<9900) && CID.length()==4){
                Information.put("Rank","-2");
                return -2;
            }
            return 0;
        }
        //System.out.println(Information.get("Rank").toString());
        int Rank = Integer.parseInt(Information.get("Rank").toString());
        String CID = Information.get("CID").toString();
        int Cidn=Integer.parseInt(CID);
            if ((Cidn>=9000 && Cidn<9900) && CID.length()==4){
                if (Rank<2){
                Information.put("Rank","-2");
                return -2;
                }
            }
        return Rank;
    }

    public String getSId() {
        if (!Information.containsKey("SID")) {
            return "";
        }
        return Information.get("SID").toString();
    }

    public String getCId() {
        if (!Information.containsKey("CID")) {
            return "";
        }
        return Information.get("CID").toString();
    }

    public String getName() {
        return getGrammar("Nominative");
        /*if (!Information.containsKey("Nominative")){
        return "";
        }
        return Information.get("Nominative").toString();*/
    }

    public String getIcon() {
        return Information.get("Icon").toString();
    }
    public OrderedHashtable getDisplayIcons(){

        //Ordered List of the Icons
        Vector IconImages = new Vector();
        Vector IconNames=new Vector();




            String Cid = Information.get("CID").toString();
            String NameF = getGrammar("Short");
            String[] IconSearch=Text.obtainValues((String)Text.Phrases.get("IconSearch"));

            File fileNew=new File(helper.langFileFind(Analyse.dayInfo.get("LS").toString(), "/icons/"+ Cid + "/0.jpg"));
            int countSearch=0;
            String LanguageString=Analyse.dayInfo.get("LS").toString();

            while (!(fileNew.exists()) && countSearch<IconSearch.length){
                LanguageString=IconSearch[countSearch];
                fileNew=new File(helper.langFileFind(IconSearch[countSearch], "/icons/"+ Cid + "/0.jpg"));
                countSearch+=1;
            }

            //The above code will add the Greek Icons and this will allow me to do what I wish to do!!!

            int counterI=0;

            //System.out.println(fileNew.getAbsolutePath());
            while (fileNew.exists()){

                IconImages.add(fileNew.toString());
                IconNames.add(NameF);
                counterI+=1;
                fileNew=new File(helper.langFileFind(LanguageString, "/icons/"+ Cid + "/"+counterI+".jpg"));
            }
        File file = new File("Ponomar/images/icons/" + Cid + ".jpg");
        if (file.exists()) {
            IconImages.add(file.toString());
            IconNames.add(NameF);
        }

        OrderedHashtable finalI = new OrderedHashtable();
        finalI.put("Images",IconImages);
        finalI.put("Names",IconNames);
        return finalI;
    }

    public String getID() {
        return Information.get("ID").toString();
    }

    public String getCycle() {
        return Information.get("Cycle").toString();
    }

    public OrderedHashtable getReadings() {
        //return (OrderedHashtable) Information.get("Scripture");
        //This is a list of all possible cases:
        //1stHour,3rdHour,6thHour,9thHour,apostol,gospel,VESPERS,MATINS
        readings = new OrderedHashtable();
        OrderedHashtable readingsT = getServiceNode("/VESPERS/SCRIPTURE");
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

    public OrderedHashtable getServiceNode(String Node) {
        if (ServiceInfo != null) {
            if (ServiceInfo.containsKey(Node)) {
                OrderedHashtable stuff = (OrderedHashtable) ServiceInfo.get(Node);


                return stuff;

            }
        }
        //System.out.println(CommNames[2] + Node);
        return null;

    }

    public OrderedHashtable getService(String Node, String Type) {
        //System.out.println(ServiceInfo);
        //System.out.println("\n\n");
        //System.out.println(Node+"/"+Type);
        //System.out.println(ServiceInfo.get(Node));
        if (ServiceInfo.containsKey(Node)) {
            OrderedHashtable stuff = (OrderedHashtable) ServiceInfo.get(Node);

            if (stuff.containsKey(Type)) {
                OrderedHashtable stuff1 = (OrderedHashtable) stuff.get(Type);

                return stuff1;
            } else {
                System.out.println(CommNames[0] + Node + CommNames[1] + Type);
                return null;
            }
        } else {
            System.out.println(CommNames[2] + Node);
            return null;
        }
    }

    public OrderedHashtable getRH(String Node, String Type) {

        if (RoyalHours.containsKey(Node)) {


            OrderedHashtable stuff = (OrderedHashtable) RoyalHours.get(Node);
            //System.out.println(stuff);
            if (stuff.containsKey(Type)) {
                OrderedHashtable stuff1 = (OrderedHashtable) stuff.get(Type);
                return stuff1;
            } else {
                System.out.println(CommNames[3]);
                return new OrderedHashtable();
            }
        } else {
            System.out.println(CommNames[3]);
            return null;
        }


    }
    public boolean checkLife(){
        //Checks whether the given commemoration has an associated life or not
        
        if (Information.get("LIFE")!= null){
            return true;
        }
        return false;
    }
    public boolean checkIcon(){
        //Checks whether the given commemoration has any icons assoicated with it
        OrderedHashtable checkIcon=getDisplayIcons();
        if (checkIcon.size()>0){
            return true;
        }
        return false;
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

        if (Information.get("LIFE")!= null){
            return Information.get("LIFE").toString();
        }
        else
        {
            return null;
        }
    }
    public String getLifeCopyright(){
        //Checks whether the given commemoration has an associated life or not

        if (Information.get("LifeCopyright")!= null){
            return Information.get("LifeCopyright").toString();
        }
        else
        {
            return null;
        }
    }


    public static void main(String[] argz) {
        OrderedHashtable dayInfo = new OrderedHashtable();
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
        Commemoration1 Paramony = new Commemoration1("0", "9001",dayInfo); //Forefeast of Christmas
        //System.out.println(Paramony.getService("/MATINS/KONTAKION","1"));
        System.out.println(Paramony.getRank());
        //System.out.println(Paramony.Information.get("LIFE"));
        System.out.println(Paramony.getGrammar("Nominative"));
        System.out.println("Rank = "+Paramony.getRank());
        System.out.println(Paramony.getService("/LITURGY/TROPARION", "1"));
        System.out.println(Paramony.getService("/LITURGY/KONTAKION", "1"));
        System.out.println(Paramony.getService("/VESPERS/SCRIPTURE", "3"));
        System.out.println(Paramony.getReadings().get("VESPERS"));
    }
}
