package net.ponomar;

import net.ponomar.parsing.utility.DocHandler;
import net.ponomar.parsing.utility.QDParser;
import net.ponomar.utility.Constants;
 
 

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import java.util.LinkedHashMap;

/***************************************************************
ConfigurationFiles.java is part of the Ponomar project.
Copyright 2008, 2013 Yuri Shardt
version 1.0: August 2008
version 2.0: January 2013
yuri.shardt (at) gmail.com

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

/**
 * 
 * Module that reads and updates the configuration files.
 * 
 * @author Yuri Shardt
 * @version 2.0: January 2013
 * 
 */
public class ConfigurationFiles implements DocHandler
{
	private static LinkedHashMap<String, String> defaults;		//STORES THE START-UP VALUES FOR PONOMAR
	
	//THIS ALLOWS THE PONOMAR CONFIGURAITON FILE TO BE MAINTAINED, UPDATED, AND READ.
	public ConfigurationFiles()
	{
	
	}
	
	public static void readFile()
	{
		
		try
		{
			//FileReader frf = new FileReader("Constants.CONFIG_FILE)");
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.CONFIG_FILE), StandardCharsets.UTF_8));
			//OutputStreamWriter out = new OutputStreamWriter(new ByteArrayOutputStream());
			//System.out.println(out.getEncoding());

			ConfigurationFiles a1 =new ConfigurationFiles();
			QDParser.parse(a1, frf);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//return Defaults;
	}
	
	public static void writeFile()
	{
		StringBuilder output;
				
		try
		{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.CONFIG_FILE), StandardCharsets.UTF_8));
			//BufferedWriter out = new BufferedWriter(new FileWriter("Constants.CONFIG_FILE)"));//,"UT8");
			out.write("<CONFIGURATION>");
			out.newLine();
			output = new StringBuilder("<DEFAULT ");
			getDefaults().forEach((k,v) -> output.append(k).append(" = \"").append(v).append("\" "));

			output.append(" />");
			out.write(output.toString());
			out.newLine();
			out.write("</CONFIGURATION>");
			out.close();
		}
		catch(IOException e)
		{
			//CANNOT BE MULTILINGUAL
			System.out.println("There was a problem:" + e);
		}
	}
	
	
	public void startDocument() { }

	public void endDocument() { }

	public void startElement(String elem, HashMap<String, String> table)
	{
		
		if (elem.equals("DEFAULT"))
		{
            table.forEach((k,v) -> checkAndPutDefaults(k, v));
		}		
	}

	private void checkAndPutDefaults(String entry, String value) {
		if(value != null && entry != null)
		{
			getDefaults().put(entry,value);
		}
	}
	
	public void endElement(String elem) { }

	public void text(String text) { }

	public static LinkedHashMap<String, String> getDefaults() {
		return defaults;
	}

	public static void setDefaults(LinkedHashMap<String, String> newDefaults) {
		defaults = newDefaults;
	}
}