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
*******************************************************/

public class parseConvert implements DocHandler
{
	private final static String Location   = "Ponomar/data/blsl/BLS/";
	private static String text2;
	private static boolean read=false;
	private static int verse;
	private static int chapter;
	private static String output;
	private final static String locationOut="Ponomar/data/parsed/";
	private static BufferedWriter out;
	private String[] b;
		
	public parseConvert()
	{
		//parse(Location+"ge.html",locationOut+"Gen.txt");
		String[] files={"1ch","1co","1jo","1ki","1pe","1ti","2ch","2co","2jo","2ki","2th","2ti","3jo","ac","am","de","dochome","ec","eph","es","ezr","ga","hab","ho","isa","jas","jer","job","1sa","1th","2pe","2sa","col","da","ex","eze","hag","heb","joe","joh","jon","jos","jud","jude","la","le","lu","mal","mic","mr","mt","na","ne","nu","ob","phm","php","pr","ps","re","ro","ru","so","tit","zec","zep"};
		for(int i=3; i<files.length;i++)
		{
			//int i=3;
			prepareFiles(Location+files[i]+".html");
			parse(Location+files[i]+".html",locationOut+files[i]+".text");
		}
		return;
	}
	public void prepareFiles(String FileNameIn)
	{
		String q=new String();
		String renew=new String();
		
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(FileNameIn)));
			q=frf.readLine();
			int counter=0;
			int cont=0;
			while(q != null)
			{
				System.out.println(q);
				//THE FIRST 20 lines must be skipped
				if(counter >= 20)
				{
					String q1=q.substring(0,7);
					String q3=q.substring(0,4);
					if(!q1.equals("<A HREF") && !q3.equals("<TH>") && !q3.equals("<TR>"))
					{
						if(cont<4)
						{
							cont+=1;
						}
						else
						{
						q=q.replaceAll("<DIV CLASS=s><DL>","\r\n<DIV>\r\n");
						q=q.replaceAll("<DT><A","</DD>\r\n<A");
						q=q.replaceAll("</A><DD>","</A>\r\n<DD>");
						q=q.replaceAll("</DL></DIV><H2>","</DD>\r\n</DIV>\r\n<H2>");
						q=q.replaceAll("</H2><DIV","</H2>\r\n<DIV");
						q=q.replaceAll("</DL>","</DD>");
						String q2=q.substring(0,6);
						//System.out.println(q2);
						if(q2.equals("<TABLE"))
						{
							break;
						}
						renew+=q+"\r\n";
						}
					}
					
				}
				counter+=1;
				q=frf.readLine();
			}
			frf.close();	
			BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FileNameIn),"UTF8"));
			out2.write(renew);
			out2.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
					//THERE WAS AN ERROR IN PROCESSING THE FILES
		}
		finally
		{
			try
			{
				out.close();
			}
			catch(Exception e1)
			{
			
			}
		}
	}
	public void parse(String FileNameIn, String FileNameOut) //throws IOException
	{
		output=new String();
		verse=0;
		chapter=0;
			
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(FileNameIn),"UTF8"));
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FileNameOut),"UTF8"));
			QDParser.parse(this, frf);
			
			//out.write(output);
			out.close();
			output="";
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
					//THERE WAS AN ERROR IN PROCESSING THE FILES
		}
		finally
		{
			output="";
			try
			{
				out.close();
			}
			catch(Exception e1)
			{
			
			}
		}
		
		
		
	}
					
	public void startDocument()
	{

	}

	public void endDocument()
	{

	}

	public void startElement(String elem, Hashtable table)
	{
		
		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo. 
		System.out.println(elem);
		if(elem.equals("A"))
		{
			System.out.println(table.get("NAME"));
			if(table.get("NAME") != null)
			{
				read=true;
				b=table.get("NAME").toString().split(":");
				if(b[0].equals("1") && (Integer.parseInt(b[0]) != chapter) )
				{
					//THE FIRST CHAPTER IS BEING READ
					chapter=1;
					try
					{
						out.write("#1\r\n");
					}
					catch(Exception a)
					{
					
					}
					output+="#1\r\n";
				} 
				else if(Integer.parseInt(b[0]) != chapter)
				{
					//A CHAPTER HAS BEEN ENCOUNTERED
					chapter+=1;
					output+="#"+b[0]+"\r\n";
					try
					{
						out.write("#"+b[0]+"\r\n");
					}
					catch(Exception a)
					{
					
					}
				}
				//ADD THE NEW VERSE
				output+=b[1]+"| ";
				try
				{
					out.write(b[1]+"| ");
				}
				catch(Exception a)
				{
				
				}
				//System.out.print(output);
			}
		}
		if(elem.equals("DD") && read)
		{
			//System.out.println(text2);
		
		}
	}

	public void endElement(String elem)
	{
		if(elem.equals("DD") && read)
		{
			output+=text2;
			try
			{
				out.write(text2);
			}
			catch(Exception a)
			{
					
			}
			read=false;
		}
	}

	public void text(String text)
	{
		//System.out.println(text);
		if(read)
		{
			text2=text+"\r\n";
			//FORMAT text2
			text2=text2.replace(":", " :");
			text2=text2.replace(";", " ;");
			text2=text2.replace("?", " ?");
			text2=text2.replace("!", " !");
			
		}
	}

	private boolean eval(String expression) throws IllegalArgumentException
	{
		return false;
	}
	
	public static void main(String[] argz)
	{
		new parseConvert();
	}
	
}