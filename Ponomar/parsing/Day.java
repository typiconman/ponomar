package Ponomar.parsing;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.swing.event.*;

import Ponomar.internationalization.LanguagePack;
import Ponomar.utility.Helpers;
import Ponomar.utility.OrderedHashtable;
import Ponomar.utility.StringOp;

import java.awt.event.*;
import java.beans.*;

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
    private OrderedHashtable information;
    private OrderedHashtable readings;
    private OrderedHashtable grammar;
    private OrderedHashtable variable;
    private String textR;
    private boolean readRH = false;
    private OrderedHashtable royalHours;
    private String elemRH;
    private OrderedHashtable value;
    private OrderedHashtable serviceInfo;
    private String location1;
    private boolean readService = false;
    private LanguagePack text;// = new LanguagePack();
    private String[] commNames;// = Text.obtainValues((String) Text.Phrases.get("Commemoration"));
    private Helpers helper;
    private int counter = 0;
    private Vector<Commemoration> orderedCommemorations;
    private int dayRank = -100;
    private int tone = -1;
    private String[] mainNames;//=Text.obtainValues((String)Text.Phrases.get("Main"));
    private String[] toneNumbers;//= Text.obtainValues((String)Text.Phrases.get("Tones"));
    private String forComm;//=(String)Text.Phrases.get("Commemoration2");
    private static StringOp parameterValues = new StringOp();

    public Day(String fileName, OrderedHashtable dayInfo) {
        information = new OrderedHashtable();
        readings = new OrderedHashtable();
        royalHours = new OrderedHashtable();
        parameterValues.setDayInfo(dayInfo);
        helper = new Helpers(parameterValues.getDayInfo());
        text = new LanguagePack(parameterValues.getDayInfo());
    commNames = text.obtainValues((String) text.getPhrases().get("Commemoration"));
mainNames=text.obtainValues((String)text.getPhrases().get("Main"));
    toneNumbers= text.obtainValues((String)text.getPhrases().get("Tones"));
    forComm=(String)text.getPhrases().get("Commemoration2");
        counter = 0;
        orderedCommemorations = new Vector<>();
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
        OrderedCommemorations = new Vector();
        dayRank = -100;
        readDay(FileName);*/
    }

    protected Day() {
        counter = 0;
        orderedCommemorations = new Vector<>();
        dayRank = -100;

        information = new OrderedHashtable();
        readings = new OrderedHashtable();
        helper = new Helpers(parameterValues.getDayInfo());
        System.out.println("NOTE USING WRONG DAY INPUT FORMAT!!!!");
    }

    public void readDay(String fileName) //throws IOException
    {
        fileName = fileName;
        //System.out.println(ParameterValues.getDayInfo().get("LS"));
        String test=parameterValues.getDayInfo().get("LS").toString();
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

    public void startElement(String elem, Hashtable table) {

        // THE TAG COULD CONTAIN A COMMAND Cmd
        // THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
        // TODAY'S INFORMATION IN dayInfo.
        // IT WOULD BE VERY RARE IN THIS CASE
        if (table.get("Cmd") != null) {
            // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

            if (parameterValues.evalbool(table.get("Cmd").toString()) == false) {

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
                sId = table.get("SId").toString();
            }
             

            String cId = table.get("CId").toString();
            if (table.get("Tone")!=null){
               tone=(int) Math.floor(parameterValues.eval(table.get("Tone").toString()));
            }
            Commemoration dayA = new Commemoration(sId, cId,parameterValues.getDayInfo());

            orderedCommemorations.addElement(dayA);

        }

    }

    public void endElement(String elem) {
    }

    public void text(String text) {
    }

    public int getDayRank() {
        if (dayRank == -100) {


            for (int i = 0; i < orderedCommemorations.size(); i++) {
                Commemoration currentC = orderedCommemorations.get(i);
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
        String cSep=(String)text.getPhrases().get("CommSep");
        String output = "";
        for (int i = 0; i < orderedCommemorations.size(); i++) {
            Commemoration cCom = orderedCommemorations.get(i);

            String sId = cCom.getSId();
            String cId = cCom.getCId();
            String nameF = cCom.getName();
            
            if (output.length()>0 && nameF.length()>0){
                output+=cSep;
            }
            
            //System.out.println(NameF);
            if (cCom.checkLife() || cCom.checkPropers()){
                output += "<A Href='goDoSaint?id=" + sId + "," + cId + "'>";
            }
            int rank = cCom.getRank();
            String rank0Format=(String)text.getPhrases().get("Rank0");
            String rank1Format=(String)text.getPhrases().get("Rank1");
            String rank2Format=(String)text.getPhrases().get("Rank2");
            String rank3Format=(String)text.getPhrases().get("Rank3");
            String rank4Format=(String)text.getPhrases().get("Rank4");
            String rank5Format=(String)text.getPhrases().get("Rank5");
            String rank6Format=(String)text.getPhrases().get("Rank6");

            switch (rank) {
                case 8:
                case 7:
                case 6:
                    //output += "<FONT Color='red'><Font face='Ponomar Unicode TT' size='+1'>\uA698</Font><B>\u00A0" + table.get("Name") + "</B></FONT>";//A698
                    //output += "<FONT Color='red'><Font face='Ponomar Unicode TT' size='+1'>\uD83D\uDD40</Font><B>\u00A0" + NameF + "</B></FONT>";
                    output +=rank6Format.replace("^NF", nameF);
                    //output += "</body><body style=\"font-family:Ponomar Unicode TT;font-size:"+Integer.parseInt(DisplaySize)+2+"pt;color:red\">\uA698</body><body style=\"font-family:"+DisplayFont+";font-size:"+DisplaySize+"pt;color:red;font-style:bold\">\u00A0" + table.get("Name") + "</body><body style=\"font-family:"+DisplayFont+";font-size:"+DisplaySize+"pt\">";
                    //output += "<style style=\"font-family:Ponomar Unicode TT;font-size:"+Integer.parseInt(DisplaySize)+2+"pt;color:red\">\uA698</style>\u00A0<style style=\"color:red\">" + table.get("Name") + "</style>";
                    //output+="<B><rank style=\"font-face:Ponomar Unicode TT;size=18;color:red\">\uA698</rank><B>\u00A0"+table.get("Name");
                    //output += "<div style=\"font-face:Ponomar Unicode TT; font-size:18pt; color:red\">\uA698\u00A0</div><Font color='red'><B>" + table.get("Name") + "</B></Font>";
                    break;
                case 5:
                    //output += "<FONT Color='red'><Font face='Ponomar Unicode TT' size='+1'>\uD83D\uDD41</Font>\u00A0" + NameF + "</FONT>";
                    output+=rank5Format.replace("^NF",nameF);
                    break;
                case 4:
                    //output += "<Font Color='red' face='Ponomar Unicode TT' size='+1'>\uD83D\uDD42</Font><B>\u00A0" + NameF + "</B>";
                    output+=rank4Format.replace("^NF",nameF);
                    break;

                case 3:
                    //output += "<Font Color='red' face='Ponomar Unicode TT' size='+1'>\uD83D\uDD43</Font><I>\u00A0" + NameF + "</I>";
                    output+=rank3Format.replace("^NF",nameF);
                    break;
                case 2:
                    //output += "<Font face='Ponomar Unicode TT' size='+1'>\uD83D\uDD43</Font><I>\u00A0" + NameF + "</I>";
                    output+=rank2Format.replace("^NF",nameF);
                    break;
                case 1:
                    output+=rank1Format.replace("^NF",nameF);
                    break;
                default:
                    //output += NameF;
                    output+=rank0Format.replace("^NF",nameF);
                //Note: \u00A0 is a nonbreaking space.
                }
             if (cCom.checkLife()){
            output += "</A>";
             }
            if (tone != -1){
                int cIdn=Integer.parseInt(cId);
                //System.out.println(cIdn);
                if (cIdn>=9000 && cIdn<9900){
                if(tone==0)
				{
					tone=8;
				}



                                String toneFormat = new String();
                                toneFormat=mainNames[4];
                                toneFormat=cSep+toneFormat.replace("TT",toneNumbers[tone]);
                                output+=toneFormat;
                
            }
            }
        }
        return output;
    }

    public OrderedHashtable getIcon() {
        //Ordered List of the Icons
        Vector iconImages = new Vector();
        Vector iconNames=new Vector();

        for (int i = 0; i < orderedCommemorations.size(); i++) {
            Commemoration cCom = orderedCommemorations.get(i);
            String sId = cCom.getSId();
            String cId = cCom.getCId();
            String nameF = cCom.getGrammar("Short");
            String[] iconSearch=text.obtainValues((String)text.getPhrases().get("IconSearch"));
            
            File fileNew=new File(helper.langFileFind(parameterValues.getDayInfo().get("LS").toString(), "/icons/"+ cId + "/0.jpg"));
            int countSearch=0;
            String languageString=parameterValues.getDayInfo().get("LS").toString();
            
            while (!(fileNew.exists()) && countSearch<iconSearch.length){
                languageString=iconSearch[countSearch];
                fileNew=new File(helper.langFileFind(iconSearch[countSearch], "/icons/"+ cId + "/0.jpg"));
                countSearch+=1;               
            }

            //The above code will add the Greek Icons and this will allow me to do what I wish to do!!!

            int counterI=0;
            
            //System.out.println(fileNew.getAbsolutePath());
            while (fileNew.exists()){
            
                iconImages.add(fileNew.toString());
                iconNames.add(nameF);
                counterI+=1;
                fileNew=new File(helper.langFileFind(languageString, "/icons/"+ cId + "/"+counterI+".jpg"));
            }
        File file = new File("Ponomar/images/icons/" + cId + ".jpg");
        if (file.exists()) {
            iconImages.add(file.toString());
            iconNames.add(nameF);
        }
        }
        OrderedHashtable finalI = new OrderedHashtable();
        finalI.put("Images",iconImages);
        finalI.put("Names",iconNames);
        return finalI;
    }

    public OrderedHashtable getService(String node, String type) {
        //System.out.println(ServiceInfo);
        //System.out.println("\n\n");
        //System.out.println(Node+"/"+Type);
        if (serviceInfo.containsKey(node)) {
            OrderedHashtable stuff = (OrderedHashtable) serviceInfo.get(node);

            if (stuff.containsKey(type)) {
                OrderedHashtable stuff1 = (OrderedHashtable) stuff.get(type);

                return stuff1;
            } else {
                System.out.println(commNames[0] + node + commNames[1] + type);
                return new OrderedHashtable();
            }
        } else {
            System.out.println(commNames[2] + node);
            return new OrderedHashtable();
        }
    }
    public OrderedHashtable[] getReadings(){
        OrderedHashtable[] readingsA = new OrderedHashtable[orderedCommemorations.size()];
        OrderedHashtable[] rInformation = new OrderedHashtable[orderedCommemorations.size()];
        Vector count= new Vector();


        for (int i = 0; i < orderedCommemorations.size(); i++) {
                Commemoration currentC = orderedCommemorations.get(i);
                int sizeR=currentC.getReadings().size();
                if (currentC.getReadings() != null || sizeR>0){
                    //There are readings to consider for today.
                    //ReadingsA[i]= new OrderedHashtable();
                    //ReadingsA[i]=CurrentC.getReadings();
                    count.add(i);
                    int ranked=(int) currentC.getRank();
                    //System.out.println("For "+CurrentC.getCId().toString()+" rank is "+Ranked);
                    rInformation[i]=new OrderedHashtable();

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
                    rInformation[i].put("Readings",currentC.getReadings());
                //dayRank = Math.max(CurrentC.getRank(), dayRank);
            }
        }
        if (!count.isEmpty()){
            OrderedHashtable[] readingsArray = new OrderedHashtable[count.size()];
            //int count2=0;
            for (int i = 0; i < count.size(); i++) {
                readingsArray[i]=new OrderedHashtable();
                //Readings[i].put("Readings",ReadingsA[Integer.parseInt(count.get(i).toString())]);
                readingsArray[i].put("Readings",rInformation[Integer.parseInt(count.get(i).toString())]);
                //count2+=1;
            }

            return readingsArray;
        }
        else
        {
            return new OrderedHashtable[0];
        }
        
    }

    public OrderedHashtable getRH(String node, String type) {

        if (royalHours.containsKey(node)) {


            OrderedHashtable stuff = (OrderedHashtable) royalHours.get(node);
            //System.out.println(stuff);
            if (stuff.containsKey(type)) {
                OrderedHashtable stuff1 = (OrderedHashtable) stuff.get(type);
                return stuff1;
            } else {
                System.out.println(commNames[3]);
                return new OrderedHashtable();
            }
        } else {
            System.out.println(commNames[3]);
            return new OrderedHashtable();
        }


    }

    public static void main(String[] argz) {
    	parameterValues.setDayInfo(new OrderedHashtable());
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
        OrderedHashtable[] testing=paramony.getReadings();
        System.out.println(testing[0].get("Readings"));
        System.out.println(testing[1].get("Readings"));
        //System.out.println(Testing[0].get("Information"));
    }
}
