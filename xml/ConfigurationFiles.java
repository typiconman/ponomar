package Ponomar;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/***************************************************************
ConfigurationFiles.java :: MODULE THAT READS AND UPDATES THE CONFIGURATION
FILES.

ConfigurationFiles.java is part of the Ponomar project.
Copyright 2008 Yuri Shardt
version 1.0: August 2008
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

public class ConfigurationFiles implements DocHandler
{
	protected static OrderedHashtable Defaults;		//STORES THE START-UP VALUES FOR PONOMAR
	
	//THIS ALLOWS THE PONOMAR CONFIGURAITON FILE TO BE MAINTAINED, UPDATED, AND READ.
	public ConfigurationFiles()
	{
	
	}
	
	public static void ReadFile()
	{
		
		try
		{
			//FileReader frf = new FileReader("Ponomar/xml/ponomar.config");
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream("Ponomar/xml/ponomar.config"), "UTF8"));
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
	
	public static void WriteFile()
	{
		String output;
				
		try
		{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Ponomar/xml/ponomar.config"),"UTF8"));
			//BufferedWriter out = new BufferedWriter(new FileWriter("Ponomar/xml/ponomar.config"));//,"UT8");
			out.write("<CONFIGURATION>");
			out.newLine();
			output="<DEFAULT ";
			for(Enumeration e=Defaults.keys(); e.hasMoreElements();)
			{
				String key = (String) e.nextElement();
				String value=(String) Defaults.get(key);
				output+=key + " = \"" + value + "\" ";
			}
			output+=" />";
			out.write(output);
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

	public void startElement(String elem, Hashtable table)
	{
		
		if (elem.equals("DEFAULT"))
		{
			for(Enumeration e=table.keys(); e.hasMoreElements();)
			{
				
				String entry=(String) e.nextElement();
				String value = (String) table.get(entry);
				
				if(value != null && entry != null)
				{
					Defaults.put(entry,value);
				}
			}
			
		}		
		return;
	}
	
	public void endElement(String elem) { }

	public void text(String text) { }
}