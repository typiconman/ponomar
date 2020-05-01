package net.ponomar.parsing;

import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import net.ponomar.parsing.utility.DocHandler;
import net.ponomar.parsing.utility.QDParser;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 
import net.ponomar.utility.StringOp;


/***********************************************************************
THIS MODULE CREATES THE TEXT FOR THE ORTHODOX USUAL BEGINNING OF A SERVICE
THIS MODULE IS STILL IN THE DEVELOPMENT PHASE.

(C) 2008 YURI SHARDT. ALL RIGHTS RESERVED.
Updated some parts to make it compatible with the changes in Ponomar, especially the language issues!

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
public class ReadText implements DocHandler
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	//THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	//TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	//DURING THE COURSE OF A SINGLE WEEK.
	private static String text;
	private static boolean read=false;
	private static String header;
        private Helpers findLanguage;
        private StringOp analyse=new StringOp();
        //There is no assumption about the provenance of the text, it must still be determined
		
		
	
	public ReadText(LinkedHashMap<String, Object> dayInfo)
	{
            findLanguage=new Helpers(analyse.getDayInfo());
            analyse.setDayInfo(dayInfo);
	}
	
				
	public void startDocument()
	{

	}

	public void endDocument()
	{

	}

	public void startElement(String elem, HashMap<String, String> table)
	{
		
		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo. 
		if (table.get("Cmd") != null)
		{
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
			
			if (!analyse.evalbool(table.get("Cmd")))
			{
				return;
			}
		}
		//if(elem.equals("LANGUAGE"))
		//{
			read=true;
		//}
		if(elem.equals("HEADER") && read)
		{
			header=table.get(Constants.VALUE);
		}
		if(elem.equals("TEXT") && read)
		{
			text+=table.get(Constants.VALUE);
			
		}	
		

	}

	public void endElement(String elem)
	{
		if(elem.equals(Constants.LANGUAGE) || elem.equals("TONE"))
		{
			read=false;
		}
	}

	public void text(String text)
	{

	}


	public static void main(String[] argz)
	{
		
		//new UsualBeginning(3);	
	}
	
	public String readText(String filename)
	{
		try
		{
       			 text= "";
       			 BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(findLanguage.langFileFind(analyse.getDayInfo().get("LS").toString(),filename)), StandardCharsets.UTF_8));
       			 QDParser.parse(this,fr);
       			 if(text.length()==0)
       			 {
       			 	text=null;
       			 }       			 			
       			      			       		
	    	}
	     	catch (Exception e)
	     	{
	     		//SERIOUS PROBLEM MISSING A PART OF THE SERVICE!
	     		System.out.println(filename);
	     		e.printStackTrace();
	     		return null;
	     	}
	     	
	     	return text;
	}
	public String readHeader(String filename)
	{
		try
		{
       			 header= "";
       			 BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(findLanguage.langFileFind(analyse.getDayInfo().get("LS").toString(),filename)), StandardCharsets.UTF_8));
       			 QDParser.parse(this,fr);
       			 if(header.length()==0)
       			 {
       			 	header=null;
       			 }       			 			
       			      			       		
	    	}
	     	catch (Exception e)
	     	{
	     		//SERIOUS PROBLEM MISSING A PART OF THE SERVICE!
	     		System.out.println(filename);
	     		e.printStackTrace();
	     		return null;
	     	}
	     	
	     	return header;
	}
	
}

