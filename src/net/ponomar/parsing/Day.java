package net.ponomar.parsing;

import net.ponomar.internationalization.LanguagePack;
import net.ponomar.parsing.utility.DocHandler;
import net.ponomar.parsing.utility.QDParser;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 
 
import net.ponomar.utility.StringOp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import java.util.LinkedHashMap;
import java.util.ArrayList;

/***********************************************************************
THIS MODULE READS XML FILES THAT CONTAIN THAT ARE OF THE <DAY> TYPE
AND STORES THE INFORMATION IN A MANNER USUABLE BY OTHER COMPONENTS
OF THE PROGRAMME.

(C) 2010, 2012 YURI SHARDT. ALL RIGHTS RESERVED.

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
public class Day implements DocHandler {

	private static boolean read = false;
    private String filename;
    private int lineNumber;
    //private LanguagePack Text=new LanguagePack();
    //private String[] ServiceNames=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
    //private String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
    private LinkedHashMap<String, String> information;
    private LinkedHashMap readings;
    private LinkedHashMap grammar;
    private LinkedHashMap variable;
    private String textR;
    private boolean readRH = false;
    private LinkedHashMap royalHours;
    private String elemRH;
    private LinkedHashMap value;
    private LinkedHashMap serviceInfo;
    private String location1;
    private boolean readService = false;
    private LanguagePack text;// = new LanguagePack();
    private String[] commNames;// = Text.obtainValues((String) Text.Phrases.get("Commemoration"));
    private Helpers helper;
    private int counter = 0;
    private ArrayList<Commemoration> orderedCommemorations;
    private int dayRank = -100;
    private int tone = -1;
    private String[] mainNames;//=Text.obtainValues((String)Text.Phrases.get("Main"));
    private String[] toneNumbers;//= Text.obtainValues((String)Text.Phrases.get("Tones"));
    private String forComm;//=(String)Text.Phrases.get("Commemoration2");
    private static StringOp parameterValues = new StringOp();

	public Day(String fileName, LinkedHashMap<String, Object> dayInfo) {
		information = new LinkedHashMap<>();
		readings = new LinkedHashMap();
		royalHours = new LinkedHashMap();
		parameterValues.setDayInfo(dayInfo);
		helper = new Helpers(parameterValues.getDayInfo());
		text = new LanguagePack(parameterValues.getDayInfo());
		commNames = text.obtainValues(text.getPhrases().get("Commemoration"));
		mainNames = text.obtainValues(text.getPhrases().get("Main"));
		toneNumbers = text.obtainValues(text.getPhrases().get("Tones"));
		forComm = text.getPhrases().get("Commemoration2");
		counter = 0;
		orderedCommemorations = new ArrayList<>();
		dayRank = -100;
		information.put("ID", fileName);
		readDay(fileName);

	}
    protected Day(String fileName)
    {
       /*ParameterValues.getDayInfo()=StringOp.dayInfo;
        Information = new OrderedHashtable();
        readings = new OrderedHashtable();
        RoyalHours = new OrderedHashtable();
        Information.put("ID", FileName);
        helper = new Helpers();

        counter = 0;
        OrderedCommemorations = new ArrayList();
        dayRank = -100;
        readDay(FileName);*/
    }

    protected Day() {
        counter = 0;
        orderedCommemorations = new ArrayList<>();
        dayRank = -100;

        information = new LinkedHashMap<>();
        readings = new LinkedHashMap();
        helper = new Helpers(parameterValues.getDayInfo());
        System.out.println("NOTE USING WRONG DAY INPUT FORMAT!!!!");
    }

    public void readDay(String fileName) //throws IOException
    {
        //fileName = fileName;
        //System.out.println(ParameterValues.getDayInfo().get("LS"));
        //String test=parameterValues.getDayInfo().get("LS").toString();
        //System.out.println("In Day, we have the path as " + test);
        
        try {
            //BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(ParameterValues.getDayInfo().get("LS").toString(),FileName+".xml")), StandardCharsets.UTF_8));
            BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind((String)parameterValues.getDayInfo().get("LS"), fileName + ".xml")), StandardCharsets.UTF_8));
            //System.out.println("===============\n"+helper.langFileFind(ParameterValues.getDayInfo().get("LS").toString(), FileName + ".xml"));
            QDParser.parse(this, frf);
        } catch (Exception e) {
            System.out.println("In file name, "+helper.langFileFind((String)parameterValues.getDayInfo().get("LS"), fileName + ".xml")+" an error occurred of type: ");
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
        if (table.get("Cmd") != null) {
            // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

            if (!parameterValues.evalbool(table.get("Cmd"))) {

                return;
            }
        }
        //if(elem.equals("LANGUAGE"))
        //{
        read = true;
        //System.out.println(table.get("Cmd").toString());
        //      return;
        //}
        if (elem.equals("SAINT") && read) {
            //System.out.println(table);
             String sId="1";
            if (table.get("SId")!=null){
                sId = table.get("SId");
            }
             

            String cId = table.get("CId");
            if (table.get("Tone")!=null){
               tone=(int) Math.floor(parameterValues.eval(table.get("Tone")));
            }
            Commemoration dayA = new Commemoration(sId, cId,parameterValues.getDayInfo());

            orderedCommemorations.add(dayA);

        }

    }

    public void endElement(String elem) {
    }

    public void text(String text) {
    }

    public int getDayRank() {
        if (dayRank == -100) {


            for (Commemoration currentC : orderedCommemorations) {
                dayRank = Math.max(currentC.getRank(), dayRank);
            }
        }
        return dayRank;
    }
    public int getTone(){
        
        if(tone==0)
				{
					tone=8;
				}
        return tone;
    }

    public String getCommsHyper() {
        //Returns a hyperlinked listing of all the commemorations for a given day.
        String cSep= text.getPhrases().get("CommSep");
        StringBuilder output = new StringBuilder();
        for (Commemoration cCom : orderedCommemorations) {
            String sId = cCom.getSId();
            String cId = cCom.getCId();
            String nameF = cCom.getName();

            if (output.length() > 0 && nameF.length() > 0) {
                output.append(cSep);
            }

            //System.out.println(NameF);
            if (cCom.checkLife() || cCom.checkPropers()) {
                output.append("<A Href='goDoSaint?id=").append(sId).append(",").append(cId).append("'>");
            }
            int rank = cCom.getRank();
            String rank0Format = text.getPhrases().get("Rank0");
            String rank1Format = text.getPhrases().get("Rank1");
            String rank2Format = text.getPhrases().get("Rank2");
            String rank3Format = text.getPhrases().get("Rank3");
            String rank4Format = text.getPhrases().get("Rank4");
            String rank5Format = text.getPhrases().get("Rank5");
            String rank6Format = text.getPhrases().get("Rank6");

            switch (rank) {
                case 8:
                case 7:
                case 6:
                    //output += "<FONT Color='red'><Font face='Ponomar Unicode TT' size='+1'>\uA698</Font><B>\u00A0" + table.get("Name") + "</B></FONT>";//A698
                    //output += "<FONT Color='red'><Font face='Ponomar Unicode TT' size='+1'>\uD83D\uDD40</Font><B>\u00A0" + NameF + "</B></FONT>";
                    output.append(rank6Format.replace("^NF", nameF));
                    //output += "</body><body style=\"font-family:Ponomar Unicode TT;font-size:"+Integer.parseInt(DisplaySize)+2+"pt;color:red\">\uA698</body><body style=\"font-family:"+DisplayFont+";font-size:"+DisplaySize+"pt;color:red;font-style:bold\">\u00A0" + table.get("Name") + "</body><body style=\"font-family:"+DisplayFont+";font-size:"+DisplaySize+"pt\">";
                    //output += "<style style=\"font-family:Ponomar Unicode TT;font-size:"+Integer.parseInt(DisplaySize)+2+"pt;color:red\">\uA698</style>\u00A0<style style=\"color:red\">" + table.get("Name") + "</style>";
                    //output+="<B><rank style=\"font-face:Ponomar Unicode TT;size=18;color:red\">\uA698</rank><B>\u00A0"+table.get("Name");
                    //output += "<div style=\"font-face:Ponomar Unicode TT; font-size:18pt; color:red\">\uA698\u00A0</div><Font color='red'><B>" + table.get("Name") + "</B></Font>";
                    break;
                case 5:
                    //output += "<FONT Color='red'><Font face='Ponomar Unicode TT' size='+1'>\uD83D\uDD41</Font>\u00A0" + NameF + "</FONT>";
                    output.append(rank5Format.replace("^NF", nameF));
                    break;
                case 4:
                    //output += "<Font Color='red' face='Ponomar Unicode TT' size='+1'>\uD83D\uDD42</Font><B>\u00A0" + NameF + "</B>";
                    output.append(rank4Format.replace("^NF", nameF));
                    break;

                case 3:
                    //output += "<Font Color='red' face='Ponomar Unicode TT' size='+1'>\uD83D\uDD43</Font><I>\u00A0" + NameF + "</I>";
                    output.append(rank3Format.replace("^NF", nameF));
                    break;
                case 2:
                    //output += "<Font face='Ponomar Unicode TT' size='+1'>\uD83D\uDD43</Font><I>\u00A0" + NameF + "</I>";
                    output.append(rank2Format.replace("^NF", nameF));
                    break;
                case 1:
                    output.append(rank1Format.replace("^NF", nameF));
                    break;
                default:
                    //output += NameF;
                    output.append(rank0Format.replace("^NF", nameF));
                    //Note: \u00A0 is a nonbreaking space.
            }
            if (cCom.checkLife()) {
                output.append("</A>");
            }
            if (tone != -1) {
                int cIdn = Integer.parseInt(cId);
                //System.out.println(cIdn);
                if (cIdn >= 9000 && cIdn < 9900) {
                    if (tone == 0) {
                        tone = 8;
                    }

                    String toneFormat =  mainNames[4];
                    toneFormat = cSep + toneFormat.replace("TT", toneNumbers[tone]);
                    output.append(toneFormat);

                }
            }
        }
        return output.toString();
    }

    public LinkedHashMap<String, ArrayList<String>> getIcon() {
        //Ordered List of the Icons
        ArrayList<String> iconImages = new ArrayList<>();
        ArrayList<String> iconNames=new ArrayList<>();

        for (Commemoration cCom : orderedCommemorations) {
            String sId = cCom.getSId();
            String cId = cCom.getCId();
            String nameF = cCom.getGrammar("Short");
            String[] iconSearch = text.obtainValues(text.getPhrases().get("IconSearch"));

            File fileNew = new File(helper.langFileFind(parameterValues.getDayInfo().get("LS").toString(), Constants.ICONS_RESOURCE_PATH + cId + "/0.jpg"));
            int countSearch = 0;
            String languageString = parameterValues.getDayInfo().get("LS").toString();

            while (!(fileNew.exists()) && countSearch < iconSearch.length) {
                languageString = iconSearch[countSearch];
                fileNew = new File(helper.langFileFind(iconSearch[countSearch], Constants.ICONS_RESOURCE_PATH + cId + "/0.jpg"));
                countSearch += 1;
            }

            //The above code will add the Greek Icons and this will allow me to do what I wish to do!!!

            int counterI = 0;

            //System.out.println(fileNew.getAbsolutePath());
            while (fileNew.exists()) {

                iconImages.add(fileNew.toString());
                iconNames.add(nameF);
                counterI += 1;
                fileNew = new File(helper.langFileFind(languageString, Constants.ICONS_RESOURCE_PATH + cId + "/" + counterI + ".jpg"));
            }
            File file = new File(Constants.ICONS_LOCATION + cId + ".jpg");
            if (file.exists()) {
                iconImages.add(file.toString());
                iconNames.add(nameF);
            }
        }
        LinkedHashMap<String, ArrayList<String>> finalI = new LinkedHashMap<>();
        finalI.put("Images",iconImages);
        finalI.put("Names",iconNames);
        return finalI;
    }

    public LinkedHashMap getService(String node, String type) {
        //System.out.println(ServiceInfo);
        //System.out.println("\n\n");
        //System.out.println(Node+"/"+Type);
        if (serviceInfo.containsKey(node)) {
        	LinkedHashMap stuff = (LinkedHashMap) serviceInfo.get(node);

            if (stuff.containsKey(type)) {
                return (LinkedHashMap) stuff.get(type);
            } else {
                System.out.println(commNames[0] + node + commNames[1] + type);
                return new LinkedHashMap();
            }
        } else {
            System.out.println(commNames[2] + node);
            return new LinkedHashMap();
        }
    }
    public LinkedHashMap[] getReadings(){
    	LinkedHashMap[] rInformation = new LinkedHashMap[orderedCommemorations.size()];
        ArrayList<Integer> count= new ArrayList<>();


        for (int i = 0; i < orderedCommemorations.size(); i++) {
                Commemoration currentC = orderedCommemorations.get(i);
                int sizeR=currentC.getReadings().size();
                if (currentC.getReadings() != null || sizeR>0){
                    //There are readings to consider for today.
                    //ReadingsA[i]= new OrderedHashtable();
                    //ReadingsA[i]=CurrentC.getReadings();
                    count.add(i);
                    int ranked=currentC.getRank();
                    //System.out.println("For "+CurrentC.getCId().toString()+" rank is "+Ranked);
                    rInformation[i]=new LinkedHashMap();

                    //System.out.println(CurrentC.getGrammar("Short")+" "+CurrentC.getReadings());
                    //forComm=forComm.replace("^CC", CurrentC.getGrammar("Short"));
                    //Temporary grammar processor
                    int getN=forComm.indexOf("%getN");
                    int forwardbracket=forComm.indexOf('(',getN);
                    int backbracket=forComm.indexOf(')');
                    String info=forComm.substring(forwardbracket+1,backbracket);
                    String[] splits=info.split(",");
                    String forCommF=forComm.substring(0,getN)+currentC.getGrammar(splits[1])+forComm.substring(backbracket+1);
                   


                    rInformation[i].put("Rank",ranked);
                    rInformation[i].put("Name",forCommF);
                    rInformation[i].put(Constants.READINGS,currentC.getReadings());
                //dayRank = Math.max(CurrentC.getRank(), dayRank);
            }
        }
        if (!count.isEmpty()){
        	LinkedHashMap[] readingsArray = new LinkedHashMap[count.size()];
            //int count2=0;
            for (int i = 0; i < count.size(); i++) {
                readingsArray[i]=new LinkedHashMap();
                //Readings[i].put("Readings",ReadingsA[Integer.parseInt(count.get(i).toString())]);
                readingsArray[i].put(Constants.READINGS,rInformation[Integer.parseInt(count.get(i).toString())]);
                //count2+=1;
            }

            return readingsArray;
        }
        else
        {
            return new LinkedHashMap[0];
        }
        
    }

    public LinkedHashMap getRH(String node, String type) {

        if (royalHours.containsKey(node)) {


        	LinkedHashMap stuff = (LinkedHashMap) royalHours.get(node);
            //System.out.println(stuff);
            if (stuff.containsKey(type)) {
                return (LinkedHashMap) stuff.get(type);
            } else {
                System.out.println(commNames[3]);
                return new LinkedHashMap();
            }
        } else {
            System.out.println(commNames[3]);
            return new LinkedHashMap();
        }


    }

    public static void main(String[] argz) {
    	parameterValues.setDayInfo(new LinkedHashMap<>());
        parameterValues.getDayInfo().put("LS", "cu/ru/");
        parameterValues.getDayInfo().put("dow", "5");
        //Commemoration paramony = new Commemoration("P_3174");    //Paramony of Christmas
        System.out.println("THIS IS RUNNING ON DEBUG MODE, USING THE FILE FOR the Paramony of Christmas");
        //OrderedHashtable stuff=Paramony.getRH("Idiomel","11");
        //System.out.println(paramony.getService("/ROYALHOURS/IDIOMEL","13"));
        //System.out.println(paramony .ServiceInfo());
        //System.out.println(paramony.getRH("IDIOMEL","11"));
        //System.out.println(paramony);
        Day paramony = new Day("xml/pentecostarion/06"); //Forefeast of Christmas

        System.out.println(paramony.getCommsHyper());
        System.out.println(paramony.getDayRank());
        LinkedHashMap[] testing=paramony.getReadings();
        System.out.println(testing[0].get(Constants.READINGS));
        System.out.println(testing[1].get(Constants.READINGS));
        //System.out.println(Testing[0].get("Information"));
    }
}
