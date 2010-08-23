package Ponomar;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;

/***************************************************************
RULEBASEDNUMBER.java :: MODULE THAT CONVERTS A DECIMAL NUMBER TO AN IDEOGRAPHIC NUMBER, THAT IS,
                        145 -> CXLV

RuleBasedNumber.java is part of the Ponomar project.
Copyright 2010 Yuri Shardt
version 1.0: May 2010
yuri (dot) shardt (at) gmail.com

 PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE CODE
 PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES THEREOF.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
**************************************************************/

class RuleBasedNumber implements DocHandler
{
    //{'a',N} means that a will be repeated for each time that the number exceeds by N (less the base amount)
    //$ Represents the rest of the number that is to be converted.
    //# Represents the remainder of a number that is to be converted based on a part, that is,
    //# for 10,000 and a base of 1,000 would represent 10 converted into the local representation.
    //For Roman Numberals
    //private String[] DF={"I{'I',1}","IV","V{'I',1}","IX","X{'X',10}$","XL$","L{'X',10}$","XC$","C{'C',100}$","CD$","D{'C',100}$","CM$","M{'M',1000}$"};
    //private long[] BaN={1,4,5,9,10,40,50,90,100,400,500,900,1000};
    //For Chinese Numberals
    private String[] DF={"\u4E00","\u4E8C","\u4E09","\u56DB","\u4E94","\u516D","\u4E03","\u516B","\u4E5D","#\u5341$","#\u767E['\u3007(0)','$ < 10']['\u4E00(1)','$ < 20 && $ >= 10']$","#\u5343$","#\u842C$","#\u5104$"};
    private long[] BaN={1,2,3,4,5,6,7,8,9,10,100,1000,10000,100000000};
    private boolean Cz=false; //Convert zero to something special!
    private String zero="zero"; //What to do if zero is called for
    private long UB=4999; //The largest feasible number! Latin
    //private double UB=1000000;
    private String[] IM={}; //Ignorable marks that can be disposed.
    /*The following marks are permitted: 1) anything before or after the three octothorps
     *  2) The three octothorps are delimiters:
     *      a) between the first two octothorps all characters that are to be placed after a character and counting from the start of the number
     *      b) between the last two octothorps all characters that are to be placed before a character and counting from the end of a number
     *      c) #{'character','location'}## The curly brackets represent a character that is to be always placed. If there are fewer characters then location, then the character will go either at the end (as in the example) or at the start of the number
     *              'location' can be a relative location that includes any of
     *              length(A) to represent the length of the string
     *              N the decimal equivalent of the number
     *      d) #['character','location','condition']## The square brackets represent the conditional format the will only be placed if the 'condition' is true.
     *              same as for location marks can be placed in condition.
     * 3) Square brackets enclose conditional formatting in the format ['character','command']. Command will be evaluated using StringOp and the following are acceptable variables:
     *          a) length(A) or length($) the length of the completed number string (A) or the remaining part ($) or the partial part (#).
     *          b) N will represent the original number
     *  For example, ".#['\u0483','length(A) / 2 - 0.5','length(A) % 2 == 1']['\u0483','length(A) / 2 - 1','length(A) % 2 == 0 $$ N >= 20']['\u0483','length(A) / 2 + 1','length(A) % 2 == 0 $$ N < 20']##."
            will display a titlo (\u0483) over the middle letter or if there is no middle letter over the right-hand letter if N < 20; otherwise over the left-hand letter; dots are placed before and after the number
     *              e.g. (Cyrillic numbers): .BI\u0483. (=12) or .PB\u0483I. (=112).
     *  Note that all the formatting applies in the order in which the character are stored in Unicode (left-to-right), the display is irrelevant here.
     */
    //private String fformat=".#{'!','2'}['$@','2','N > 10']#['<','2','N > 1000']{'>','7'}#."; //Any marks that need to be added on top of the final number
    private String fformat="###";
    private OrderedHashtable Phrases;		//STORES ALL THE REQUIRED PHRASES FOR THE INTERFACE IN THE CURRENT INTERFACE LANGUAGE.
    private boolean readRules=false;

    public RuleBasedNumber()
	{
    	//Do nothing right now; later load the required rules.
            initialise();
    }
    private void initialise()
    {
		String filename="xml/Commands/RuleBasedNumbers.xml";
                Helpers findLanguage=new Helpers();
                filename=findLanguage.langFileFind(StringOp.dayInfo.get("LS").toString(),filename);
		try
		{
			//ALLOWS MULTILINGUAL SUPPORT, WHICH IS A MUST IN OUR CASE.
			BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
			//FileReader fr = new FileReader(filename);
			QDParser.parse(this, fr);
		}
		catch (Exception e)
		{
			//THIS STATEMENT CANNOT BE MULTILINGUAL!
			System.out.println("Unable to find " + filename);
		}


	}

	public void startDocument() {
            readRules=false;
        }

	public void endDocument() { }

	public void startElement(String elem, Hashtable table)
	{
		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo.
		String Language = new String();

		if (table.get("Cmd") != null)
		{
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
			if (StringOp.evalbool(table.get("Cmd").toString()) == false)
			{
				return;
			}
		}
		//if(elem.equals("LANGUAGE"))
		//{
			readRules=true;
		//}
		if (elem.equals("PHRASE") && readRules)
		{
			String Key=table.get("Key").toString();
			String Value=table.get("Value").toString();
			if (Key.equals("DF"))
                        {
                           DF=obtainValues(Value);
                           
                        }
                        
                        if (Key.equals("BaN"))
                        {
                            String[] tempvalue=obtainValues(Value);
                            BaN=new long[tempvalue.length];
                            for(int i=0;i<tempvalue.length;i++)
                            {
                                BaN[i]=Long.parseLong(tempvalue[i]);
                                //System.out.println(BaN[i]);
                            }
                            
                        }
                        if (Key.equals("Cz"))
                        {
                            Cz=StringOp.evalbool(Value);                           

                        }
                        if (Key.equals("UB"))
                        {
                            UB=Long.parseLong(Value);
                        }
                        if (Key.equals("fformat"))
                        {
                            fformat=Value;
                        }
                        if (Key.equals("IM"))
                        {
                            IM=obtainValues(Value);
                        }
                        if (Key.equals("zero"))
                        {
                            zero=Value;
                        }
		}
	}

	public void endElement(String elem)
	{
		if(elem.equals("LANGUAGE"))
		{
			readRules=false;
		}
	 }

        public String getFormattedNumber(double Number)
{
   if (Number>UB)
        {
            return Double.toString(Number);
        }
   if (Number==0)
   {
       return zero;
   }
   String FormattedNumber=FormatNumber(Number);
   //Perform final formatting
   int firstOcto=fformat.indexOf("#");
   int secondOcto=fformat.indexOf("#",firstOcto+1);
   int thirdOcto=fformat.indexOf("#",secondOcto+1);
   int lengthFN=FormattedNumber.length();
   //Peform formatting over the formatted numbers
   int change=0;
   //System.out.println(lengthFN);
   String firstAdd=fformat.substring(firstOcto+1,secondOcto);
   if (firstAdd != null)
   {
       //There is something to add after the letters
       int curlF=firstAdd.indexOf("{");
       String before=FormattedNumber.substring(0,1);
       String after=FormattedNumber.substring(1);

       while (curlF>-1)
       {
           int curlAF=firstAdd.indexOf("}",curlF);
           String data=firstAdd.substring(curlF+1,curlAF-1);
           String[] curlSplit=data.split("','");
           int dloc=Integer.parseInt(curlSplit[1]);
           if (dloc>FormattedNumber.length()-1)
           {
               dloc=FormattedNumber.length()-1;
           }
           FormattedNumber=FormattedNumber.substring(0,dloc+change)+curlSplit[0].substring(1)+FormattedNumber.substring(dloc+change);
           change+=1;
           curlF=firstAdd.indexOf("{",curlF+1);


       }
       int squareF=firstAdd.indexOf("[");
       while (squareF>-1)
       {
           int squareAF=firstAdd.indexOf("]",squareF);
           String data=firstAdd.substring(squareF+1,squareAF-1);
           String[] squareSplit=data.split("','");


           squareSplit[2]=squareSplit[2].replace("length(A)", Integer.toString(lengthFN));
           squareSplit[1]=squareSplit[1].replace("length(A)", Integer.toString(lengthFN));
           squareSplit[2]=squareSplit[2].replace("N",Double.toString(Number));
           squareSplit[1]=squareSplit[1].replace("N",Double.toString(Number));
           

           if (StringOp.evalbool(squareSplit[2]))
           {
             int dloc=(int) StringOp.eval(squareSplit[1]);
           if (dloc>FormattedNumber.length()-1-change)
           {
               dloc=FormattedNumber.length()-1-change;
           }
           FormattedNumber=FormattedNumber.substring(0,dloc+change)+squareSplit[0].substring(1)+FormattedNumber.substring(dloc+change);
           change+=1;
           }

           squareF=firstAdd.indexOf("[",squareF+1);




       }
       //FormattedNumber=before+added+after;
   }

   firstAdd=fformat.substring(secondOcto+1,thirdOcto);
   if (firstAdd != null)
   {
       //There is something to add after the letters
       int curlF=firstAdd.indexOf("{");
       String before=FormattedNumber.substring(0,1);
       String after=FormattedNumber.substring(1);

       while (curlF>-1)
       {
           int curlAF=firstAdd.indexOf("}",curlF);
           String data=firstAdd.substring(curlF+1,curlAF-1);
           String[] curlSplit=data.split("','");
           int dloc=FormattedNumber.length()-change-Integer.parseInt(curlSplit[1]);
           if (dloc<0)
           {
               dloc=-change;
           }
           FormattedNumber=FormattedNumber.substring(0,dloc+change)+curlSplit[0].substring(1)+FormattedNumber.substring(dloc+change);
           change+=1;
           curlF=firstAdd.indexOf("{",curlF+1);


       }
       int squareF=firstAdd.indexOf("[");
       while (squareF>-1)
       {
           int squareAF=firstAdd.indexOf("]",squareF);
           String data=firstAdd.substring(squareF+1,squareAF-1);
           String[] squareSplit=data.split("','");


           squareSplit[2]=squareSplit[2].replace("length(A)", Integer.toString(lengthFN));
           squareSplit[1]=squareSplit[1].replace("length(A)", Integer.toString(lengthFN));
           squareSplit[2]=squareSplit[2].replace("N",Double.toString(Number));
           squareSplit[1]=squareSplit[1].replace("N",Double.toString(Number));

           if (StringOp.evalbool(squareSplit[2]))
           {
             int dloc=FormattedNumber.length()-change-(int) StringOp.eval(squareSplit[1]);
           if (dloc<0)
           {
               dloc=-change;
           }
           FormattedNumber=FormattedNumber.substring(0,dloc+change)+squareSplit[0].substring(1)+FormattedNumber.substring(dloc+change);
           change+=1;
           }
           squareF=firstAdd.indexOf("[",squareF+1);




       }
       //FormattedNumber=before+added+after;
   }

   //System.out.println(firstOcto+" final "+finalOcto);
   int finalOcto=thirdOcto;
   if (firstOcto==0)
   {
       if (finalOcto==fformat.length()-1)
       {
           return FormattedNumber;
       }
       else
       {
           return FormattedNumber+fformat.substring(finalOcto+1);
       }
   }
   else
   {
       if (finalOcto==fformat.length()-1)
       {
           return fformat.substring(0,firstOcto);
       }
       else
       {
           return fformat.substring(0,firstOcto)+FormattedNumber+fformat.substring(finalOcto+1);
       }
   }

}
    private String FormatNumber(double Number)
    {
        String FNumber="";

        if (Number==0)
        {
            if (!Cz)
            {
                return "";
            }
            else
            {
                return zero;
            }

        }
        int i=-1;
        for(i=DF.length-1;i>=0;i--)
        {
            if (BaN[i]<=Number)
            {
                //i=j;
                break;
            }
        }
        String format=DF[i];
        long base=BaN[i];
        long Pint=(long) Number/base; //The integer part of the number
        long remainder=(long) Number-base;
        //System.out.println("--------------------------");
        //System.out.println("base: "+base);
        //System.out.println("Pint: "+Pint);
        //System.out.println("remainder: "+remainder);
        //System.out.println("Number: "+Number);
        int cbI=format.indexOf("{");
        if (cbI>-1)
        {
            //We have found a case of repeat case
            int cbF=format.lastIndexOf("}");
            //System.out.println(format);
            String cb=format.substring(cbI+1,cbF);
            int quoteI=cb.indexOf("'");
            int quoteF=cb.lastIndexOf("'");
            String repeat=cb.substring(quoteI+1,quoteF);
            //System.out.println(cb);
            //System.out.println(quoteF);
            //System.out.println(cb.substring(quoteF+2));
            int times=Integer.parseInt(cb.substring(quoteF+2));
            long divide=(long) remainder/times;
            FNumber=format.substring(0,cbI);
            remainder=remainder-divide*times;
            for(int i1=1;i1<=divide;i1++)
            {
                FNumber+=repeat;
            }
            //System.out.println(FNumber);
            if (cbF+1<format.length())
            {
                FNumber+=format.substring(cbF+1);
            }
        }
        else
        {
            FNumber=format;
        }
        int octo=format.indexOf("#");
        
        if (octo>=0)
        {
            //There is a need to convert the multiplier as per the rules
            //System.out.println("The octo location is "+octo);
            String multiplier=FormatNumber(Pint);
            
            if (octo==0)
            {
                FNumber=multiplier+FNumber.substring(1);
            }
            else
            {
                if (octo==FNumber.length()-1)
                {
                    FNumber=FNumber.substring(0,octo)+multiplier;
                }
                else
                {
                    FNumber=FNumber.substring(0,octo)+multiplier+FNumber.substring(octo+1);
                    //System.out.println("After modifications, it is "+FNumber);
                }
            }
            remainder=(long) Number-Pint*base;
        }
        //
        int squareF=FNumber.indexOf("[");
       while (squareF>-1)
       {
           int squareAF=FNumber.indexOf("]",squareF);
           String data=FNumber.substring(squareF+1,squareAF-1);
           String[] squareSplit=data.split("','");


           //squareSplit[2]=squareSplit[2].replace("length(A)", Integer.toString(lengthFN));
           //squareSplit[1]=squareSplit[1].replace("length(A)", Integer.toString(lengthFN));
           //squareSplit[2]=squareSplit[2].replace("N",Double.toString(Number));
           squareSplit[1]=squareSplit[1].replace("N",Double.toString(Number));
           squareSplit[1]=squareSplit[1].replace("$",Double.toString(remainder));

           if (StringOp.evalbool(squareSplit[1]))
           {
                     FNumber=FNumber.substring(0,squareF)+squareSplit[0].substring(1)+FNumber.substring(squareAF+1);

           }
           else
           {
               FNumber=FNumber.substring(0,squareF)+FNumber.substring(squareAF+1);
           }
           squareF=FNumber.indexOf("[");




       }
        //
        int amper=FNumber.indexOf("$");
        /*if (remainder==0)
        {
            amper=-1;
        }*/
        if (amper>-1)
        {
            //There is need to recursively solve the situation
            //System.out.println(remainder);
            String amperNumber=FormatNumber(remainder);
            //System.out.println("The $ is given as "+amperNumber);
            //String amperNumber=Integer.toString(remainder);
            if (amperNumber.equals(""))
            {
                //No number needs to be converted remove the symbol
                if (amper==0)
            {
                FNumber=FNumber.substring(1);
            }
            else
            {
                if (amper==FNumber.length()-1)
                {
                    //The ampersand is at the end of a number
                    FNumber=FNumber.substring(0,amper);
                }
                else
                {
                    //The ampersand is in the middle of the number
                    String before=FNumber.substring(0,amper);
                    String after=FNumber.substring(amper+1);
                    FNumber=before+after;
                }
            }
            }else
            {
            if (amper==0)
            {
                FNumber=amperNumber+FNumber.substring(1);
            }
            else
            {
                if (amper==FNumber.length()-1)
                {
                    //The ampersand is at the end of a number
                    FNumber=FNumber.substring(0,amper)+amperNumber;
                }
                else
                {
                    //The ampersand is in the middle of the number
                    String before=FNumber.substring(0,amper);
                    String after=FNumber.substring(amper+1);
                    FNumber=before+amperNumber+after;
                }
            }
            }
        }
        //System.out.println("At present, the number is converted as "+FNumber);
        return FNumber;
    }
       public int ConvertToInteger(String FNumber)
       {
           //At present this is not supported.
           return -1;
       }

	public void text(String text) { }

	private String[] obtainValues(String in)
	{
		//THIS FUNCTION TAKES A STTRING SEPARATED BY '/,' AND RETURNS A STRING ARRAY.
		String[] rough=in.split("/,");
		//System.out.println(rough[0] + " " +rough[1]);
		return rough;
	}

       public static void main(String[] argz) {
        //DEBUG MODE
        System.out.println("RuleBasedNumber.java running in Debug mode");
        System.out.println("This program comes with ABSOLUTELY NO WARRANTY!!");
        StringOp.dayInfo = new Hashtable();
        StringOp.dayInfo.put("LS","6");

        RuleBasedNumber test=new RuleBasedNumber();
        int number=10018;


        String result=test.getFormattedNumber(number);
        System.out.println("The converted form of the number "+number+" in Roman Numerals is "+result+" !");
    }
}