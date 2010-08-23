package Ponomar;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;

/***************************************************************
LanguagePack.java :: MODULE THAT DETERMINES THE LANGUAGE SPECIFIC OUTPUTS

LanguagePack.java is part of the Ponomar project.
Copyright 2008 Yuri Shardt
version 1.0: August 2008
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

class LanguagePack implements DocHandler
{
	OrderedHashtable Phrases;		//STORES ALL THE REQUIRED PHRASES FOR THE INTERFACE IN THE CURRENT INTERFACE LANGUAGE.
	private boolean readPhrases=false;		//DETERMINE WHETHER TO READ OR NOT TO READ THE GIVEN PHRASES	(THIS MUST BE ADDED TO ALL THE READERS).
	
	public LanguagePack()
	{
		Phrases =new OrderedHashtable();
		ReadPhrases();
		
	}
        public LanguagePack(String path)
	{
		Phrases =new OrderedHashtable();
		ReadPhrases(path);

	}
	private void ReadPhrases()
	{
            Helpers getFile=new Helpers();
            ReadPhrases(getFile.langFileFind(StringOp.dayInfo.get("LS").toString(),"xml/Commands/LanguagePacks.xml"));
        }
        private void ReadPhrases(String langPath)
	{
            String filename=langPath;
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
                        System.out.println(e.toString());
                        for(int i=0;i<e.getStackTrace().length;i++)
                        {
                            System.out.println(e.getStackTrace()[i].toString());
                        }
                        System.out.println("------------------");
		}
	
	}
	public String[] obtainValues(String in)
	{
		//THIS FUNCTION TAKES A STTRING SEPARATED BY '\,' AND RETURNS A STRING ARRAY.
		String[] rough=in.split("/,");
		//System.out.println(rough[0] + " " +rough[1]);
		return rough;
	}
	
	public void startDocument() { }

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
			readPhrases=true;
		//}
		if (elem.equals("PHRASE") && readPhrases)
		{
                    
                    String Key=table.get("Key").toString();
			String Value=table.get("Value").toString();
			Phrases.put(Key,Value);
			//System.out.println("The current language is " + Language + ". The phrases are " +Phrases);
		}			
	}

	public void endElement(String elem)
	{
		if(elem.equals("LANGUAGE"))
		{
			readPhrases=false;
		}	
	 }

	public void text(String text) { }
}