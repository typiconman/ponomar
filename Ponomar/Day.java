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
THIS MODULE READS XML FILES THAT CONTAIN THAT ARE OF THE <DAY> TYPE
AND STORES THE INFORMATION IN A MANNER USUABLE BY OTHER COMPONENTS
OF THE PROGRAMME.

(C) 2010 YURI SHARDT. ALL RIGHTS RESERVED.

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
    private LanguagePack Text = new LanguagePack();
    private String[] CommNames = Text.obtainValues((String) Text.Phrases.get("Commemoration"));
    private Helpers helper;
    private int counter = 0;
    private Vector OrderedCommemorations;
    private int dayRank = -100;
    private int Tone = -1;
    private String[] MainNames=Text.obtainValues((String)Text.Phrases.get("Main"));
    private String[] toneNumbers= Text.obtainValues((String)Text.Phrases.get("Tones"));
    private String forComm=(String)Text.Phrases.get("Commemoration2");

    protected Day(String FileName) {
        Information = new OrderedHashtable();
        readings = new OrderedHashtable();
        RoyalHours = new OrderedHashtable();
        Information.put("ID", FileName);
        helper = new Helpers();

        counter = 0;
        OrderedCommemorations = new Vector();
        dayRank = -100;
        readDay(FileName);


    }

    protected Day() {
        counter = 0;
        OrderedCommemorations = new Vector();
        dayRank = -100;

        Information = new OrderedHashtable();
        readings = new OrderedHashtable();
        helper = new Helpers();
    }

    public void readDay(String FileName) //throws IOException
    {
        FileName = FileName;
        try {
            //BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(StringOp.dayInfo.get("LS").toString(),FileName+".xml")), "UTF8"));
            BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(StringOp.dayInfo.get("LS").toString(), FileName + ".xml")), "UTF8"));
            //System.out.println("===============\n"+helper.langFileFind(StringOp.dayInfo.get("LS").toString(), FileName + ".xml"));
            QDParser.parse(this, frf);
        } catch (Exception e) {
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

            if (StringOp.evalbool(table.get("Cmd").toString()) == false) {

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
             String Sid="1";
            if (table.get("SId")!=null){
                Sid = table.get("SId").toString();
            }
             

            String Cid = table.get("CId").toString();
            if (table.get("Tone")!=null){
                Tone=Integer.parseInt(table.get("Tone").toString());
            }
            Commemoration1 DayA = new Commemoration1(Sid, Cid);

            OrderedCommemorations.addElement(DayA);

        }

    }

    public void endElement(String elem) {
    }

    public void text(String text) {
    }

    public int getDayRank() {
        if (dayRank == -100) {


            for (int i = 0; i < OrderedCommemorations.size(); i++) {
                Commemoration1 CurrentC = (Commemoration1) OrderedCommemorations.get(i);
                dayRank = Math.max(CurrentC.getRank(), dayRank);
            }
        }
        return dayRank;
    }
    public int getTone(){
        if(Tone==0)
				{
					Tone=8;
				}
        return Tone;
    }

    public String getCommsHyper() {
        //Returns a hyperlinked listing of all the commemorations for a given day.
        String CSep=(String)Text.Phrases.get("CommSep");
        String output = "";
        for (int i = 0; i < OrderedCommemorations.size(); i++) {
            if (output.length()>0){
                output+=CSep;
            }
            Commemoration1 CCom = (Commemoration1) OrderedCommemorations.get(i);
            
            String Sid = CCom.getSId();
            String Cid = CCom.getCId();
            String NameF = CCom.getName();
            //System.out.println(NameF);
            if (CCom.checkLife()){
                output += "<A Href='goDoSaint?id=" + Sid + "," + Cid + "'>";
            }
            int Rank = CCom.getRank();
            switch (Rank) {
                case 8:
                case 7:
                case 6:
                    //output += "<FONT Color='red'><Font face='Hirmos Ponomar' size='+1'>\uA698</Font><B>\u00A0" + table.get("Name") + "</B></FONT>";//A698
                    output += "<FONT Color='red'><Font face='Hirmos Ponomar' size='+1'>\uD83D\uDD40</Font><B>\u00A0" + NameF + "</B></FONT>";
                    //output += "</body><body style=\"font-family:Hirmos Ponomar;font-size:"+Integer.parseInt(DisplaySize)+2+"pt;color:red\">\uA698</body><body style=\"font-family:"+DisplayFont+";font-size:"+DisplaySize+"pt;color:red;font-style:bold\">\u00A0" + table.get("Name") + "</body><body style=\"font-family:"+DisplayFont+";font-size:"+DisplaySize+"pt\">";
                    //output += "<style style=\"font-family:Hirmos Ponomar;font-size:"+Integer.parseInt(DisplaySize)+2+"pt;color:red\">\uA698</style>\u00A0<style style=\"color:red\">" + table.get("Name") + "</style>";
                    //output+="<B><rank style=\"font-face:Hirmos Ponomar;size=18;color:red\">\uA698</rank><B>\u00A0"+table.get("Name");
                    //output += "<div style=\"font-face:Hirmos Ponomar; font-size:18pt; color:red\">\uA698\u00A0</div><Font color='red'><B>" + table.get("Name") + "</B></Font>";
                    break;
                case 5:
                    output += "<FONT Color='red'><Font face='Hirmos Ponomar' size='+1'>\uD83D\uDD41</Font>\u00A0" + NameF + "</FONT>";
                    break;
                case 4:
                    output += "<Font Color='red' face='Hirmos Ponomar' size='+1'>\uD83D\uDD42</Font><B>\u00A0" + NameF + "</B>";
                    break;

                case 3:
                    output += "<Font Color='red' face='Hirmos Ponomar' size='+1'>\uD83D\uDD43</Font><I>\u00A0" + NameF + "</I>";
                    break;
                case 2:
                    output += "<Font face='Hirmos Ponomar' size='+1'>\uD83D\uDD43</Font><I>\u00A0" + NameF + "</I>";
                    break;
                default:
                    output += NameF;
                //Note: \u00A0 is a nonbreaking space.
                }
             if (CCom.checkLife()){
            output += "</A>";
             }
            if (Tone != -1){
                int Cidn=Integer.parseInt(Cid);
                //System.out.println(Cidn);
                if (Cidn>=9000 && Cidn<9900){
                if(Tone==0)
				{
					Tone=8;
				}



                                String ToneFormat = new String();
                                ToneFormat=MainNames[4];
                                ToneFormat=CSep+ToneFormat.replace("TT",toneNumbers[Tone]);
                                output+=ToneFormat;
                
            }
            }
        }
        return output;
    }

    public OrderedHashtable getIcon() {
        //Ordered List of the Icons
        Vector IconImages = new Vector();
        Vector IconNames=new Vector();

        for (int i = 0; i < OrderedCommemorations.size(); i++) {
            Commemoration1 CCom = (Commemoration1) OrderedCommemorations.get(i);
            String Sid = CCom.getSId();
            String Cid = CCom.getCId();
            String NameF = CCom.getName();
            
            File fileNew=new File(helper.langFileFind(StringOp.dayInfo.get("LS").toString(), "/icons/"+ Cid + "/0.jpg"));

            int counterI=0;
            
            //System.out.println(fileNew.getAbsolutePath());
            while (fileNew.exists()){
            
                IconImages.add(fileNew.toString());
                IconNames.add(NameF);
                counterI+=1;
                fileNew=new File(helper.langFileFind(StringOp.dayInfo.get("LS").toString(), "/icons/"+ Cid + "/"+counterI+".jpg"));
            }
        File file = new File("Ponomar/images/icons/" + Cid + ".jpg");
        if (file.exists()) {
            IconImages.add(file.toString());
            IconNames.add(NameF);
        }
        }
        OrderedHashtable finalI = new OrderedHashtable();
        finalI.put("Images",IconImages);
        finalI.put("Names",IconNames);
        return finalI;
    }

    public OrderedHashtable getService(String Node, String Type) {
        //System.out.println(ServiceInfo);
        //System.out.println("\n\n");
        //System.out.println(Node+"/"+Type);
        if (ServiceInfo.containsKey(Node)) {
            OrderedHashtable stuff = (OrderedHashtable) ServiceInfo.get(Node);

            if (stuff.containsKey(Type)) {
                OrderedHashtable stuff1 = (OrderedHashtable) stuff.get(Type);

                return stuff1;
            } else {
                System.out.println(CommNames[0] + Node + CommNames[1] + Type);
                return new OrderedHashtable();
            }
        } else {
            System.out.println(CommNames[2] + Node);
            return new OrderedHashtable();
        }
    }
    public OrderedHashtable[] getReadings(){
        OrderedHashtable ReadingsA[] =new OrderedHashtable[OrderedCommemorations.size()];
        OrderedHashtable RInformation[] = new OrderedHashtable[OrderedCommemorations.size()];
        Vector count= new Vector();


        for (int i = 0; i < OrderedCommemorations.size(); i++) {
                Commemoration1 CurrentC = (Commemoration1) OrderedCommemorations.get(i);
                int sizeR=CurrentC.getReadings().size();
                if (CurrentC.getReadings() != null || sizeR>0){
                    //There are readings to consider for today.
                    //ReadingsA[i]= new OrderedHashtable();
                    //ReadingsA[i]=CurrentC.getReadings();
                    count.add(i);
                    int Ranked=(int) CurrentC.getRank();
                    //System.out.println("For "+CurrentC.getCId().toString()+" rank is "+Ranked);
                    RInformation[i]=new OrderedHashtable();

                    //System.out.println(CurrentC.getGrammar("Short")+" "+CurrentC.getReadings());
                    //forComm=forComm.replace("^CC", CurrentC.getGrammar("Short"));
                    //Temporary grammar processor
                    int getN=forComm.indexOf("%getN");
                    int forwardbracket=forComm.indexOf("(",getN);
                    int backbracket=forComm.indexOf(")");
                    String info=forComm.substring(forwardbracket+1,backbracket);
                    String[] splits=info.split(",");
                    String forCommF=forComm.substring(0,getN)+CurrentC.getGrammar(splits[1])+forComm.substring(backbracket+1);
                   


                    RInformation[i].put("Rank",Ranked);
                    RInformation[i].put("Name",forCommF);
                    RInformation[i].put("Readings",CurrentC.getReadings());
                //dayRank = Math.max(CurrentC.getRank(), dayRank);
            }
        }
        if (count.size()>0){
            OrderedHashtable Readings[] = new OrderedHashtable[count.size()];
            //int count2=0;
            for (int i = 0; i < count.size(); i++) {
                Readings[i]=new OrderedHashtable();
                //Readings[i].put("Readings",ReadingsA[Integer.parseInt(count.get(i).toString())]);
                Readings[i].put("Readings",RInformation[Integer.parseInt(count.get(i).toString())]);
                //count2+=1;
            }

            return Readings;
        }
        else
        {
            return new OrderedHashtable[0];
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
            return new OrderedHashtable();
        }


    }

    public static void main(String[] argz) {
        StringOp.dayInfo = new OrderedHashtable();
        StringOp.dayInfo.put("LS", "cu/ru/");
        StringOp.dayInfo.put("dow", "5");
        //Commemoration Paramony = new Commemoration("P_3174");    //Paramony of Christmas
        System.out.println("THIS IS RUNNING ON DEBUG MODE, USING THE FILE FOR the Paramony of Christmas");
        //OrderedHashtable stuff=Paramony.getRH("Idiomel","11");
        //System.out.println(Paramony.getService("/ROYALHOURS/IDIOMEL","13"));
        //System.out.println(Paramony .ServiceInfo());
        //System.out.println(Paramony.getRH("IDIOMEL","11"));
        //System.out.println(Paramony);
        Day Paramony = new Day("xml/pentecostarion/06"); //Forefeast of Christmas

        System.out.println(Paramony.getCommsHyper());
        System.out.println(Paramony.getDayRank());
        OrderedHashtable[] Testing=Paramony.getReadings();
        System.out.println(Testing[0].get("Readings"));
        System.out.println(Testing[1].get("Readings"));
        //System.out.println(Testing[0].get("Information"));
    }
}
