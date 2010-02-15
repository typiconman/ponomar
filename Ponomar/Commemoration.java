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

(C) 2009 YURI SHARDT. ALL RIGHTS RESERVED.

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

public class Commemoration implements DocHandler
{
	private final static String Location  = "Ponomar/xml/Services/menaion/";   // THE LOCATION OF THE BASIC SERVICE RULES
	private static boolean read=false;
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
        private boolean readRH=false;
        private OrderedHashtable RoyalHours;
        private String elemRH;
        private OrderedHashtable value;
        private OrderedHashtable ServiceInfo;
        private String Location1;
        private boolean readService=false;
        private LanguagePack Text=new LanguagePack();
	private String[] CommNames=Text.obtainValues((String)Text.Phrases.get("Commemoration"));
	
	protected Commemoration(String FileName)
	{
		Information=new OrderedHashtable();
		readings=new OrderedHashtable();
                RoyalHours=new OrderedHashtable();
		readCommemoration(FileName);                
	}
	protected Commemoration()
	{
		Information=new OrderedHashtable();
		readings=new OrderedHashtable();				
	}
	protected Commemoration(String Name, OrderedHashtable grammar, OrderedHashtable readings)
	{
		//THIS WILL CREATE A QUASI-COMMEMORATION FILE ONLY GIVEN THE NAME OF THE COMMEMORATION!
		Information=new OrderedHashtable();
		Information.put("Name",Name);
		Information.put("Rank",-1);
		Information.put("Cycle",-1);
		Information.put("Grammar",grammar);
		Information.put("Scripture",readings);
		Information.put("ID","-1");
	}
	
	
	public void readCommemoration(String FileName) //throws IOException
	{
		Information.put("ID",FileName);
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(Location+FileName+".xml"), "UTF8"));
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			e.printStackTrace();			
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
		// IT WOULD BE VERY RARE IN THIS CASE
		if (table.get("Cmd") != null)
		{
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
			
			if (StringOp.evalbool(table.get("Cmd").toString()) == false) 
			{

                            return;
			}
		}
		if(elem.equals("LANGUAGE"))
		{
			read=true;
                        //System.out.println(table.get("Cmd").toString());
                        return;
		}
                if(elem.equals("SERVICE") && read){
                    readService=true;
                    ServiceInfo=new OrderedHashtable();
                    Location1=new String();
                    return;
                }
                if(readService && read){
                    Location1+="/"+elem;
                    //elemRH=elem;
                    value=new OrderedHashtable();
                    for(Enumeration e = table.keys(); e.hasMoreElements();)
			{
				String type = (String)e.nextElement();
				value.put(type,table.get(type));
			}
                    return;
                }
		//if(elem.equals("ROYALHOURS") && read){
                  //  readRH=true;
                    //RoyalHours=new OrderedHashtable();
                //}
                if(readRH && read){
                    elemRH=elem;
                    value=new OrderedHashtable();
                    for(Enumeration e = table.keys(); e.hasMoreElements();)
			{
				String type = (String)e.nextElement();
				value.put(type,table.get(type));				
			}                    
                }
                if (elem.equals("SCRIPTURE") && read)
		{
			String type = (String)table.get("Type");
			String reading = (String)table.get("Reading");
			if (readings.containsKey(type))
			{
				
				// ADD THIS READING TO OTHERS OF THE SAME TYPE
				Vector vect = (Vector)readings.get(type);
				vect.add(reading);
				readings.put(type, vect);
			}
			else
			{
				// CREATE A NEW TYPE WITH A COLLECTION INCLUDING THIS READING
				Vector vect = new Vector();
				vect.add(reading);
				readings.put(type, vect);
			}
			Information.put("Scripture",readings); 
		}
		if (elem.equals("GRAMMAR") && read)
		{
			//THIS SHOULD ONLY BE READ ONCE PER LANGUAGE AND PASS!
			grammar=new OrderedHashtable();
			for(Enumeration e = table.keys(); e.hasMoreElements();)
			{
				String type = (String)e.nextElement();
				grammar.put(type,table.get(type));				
			}
			Information.put("Grammar",grammar);			
		}
		if(elem.equals("CHURCH") && read)
		{
			Information.put("Rank",table.get("Rank").toString());
			Information.put("Cycle",table.get("Cycle").toString());
		}
		if(elem.equals("ICON") && read)
		{
			Information.put("Icon",table.get("Id").toString());
		}
		if(elem.equals("TROPARION") && read)
		{
			variable=new OrderedHashtable();
			variable.put("Tone",table.get("Tone").toString());
			variable.put("Author",table.get("Author").toString());
		}
		if(elem.equals("KONTAKION") && read)
		{
			variable=new OrderedHashtable();
			variable.put("Tone",table.get("Tone").toString());
			variable.put("Author",table.get("Author").toString());
		}
		if(elem.equals("NAME") && read)
		{
		
		}		
	}

	public void endElement(String elem)
	{
		if(elem.equals("LANGUAGE"))
		{
			read=false;
		}
                if(elem.equals("ROYALHOURS")){
                    readRH=false;
                }
                if(elem.equals("SERVICE") && read){
                    readService=false;
                    //System.out.println(ServiceInfo);
                    //if(ServiceInfo.containsKey("ROYALHOURS/VERSE")){
                        //System.out.println(ServiceInfo.get("ROYALHOURS/VERSE"));
                    //}
                }
                if(readService && read){
                    /*if(elem.equals("VERSE")){
                        System.out.println(textR);
                    }*/
                    //System.out.println("In endElement, I saw the following elements: "+elem);
                    if (textR!=null){
                        value.put("text",textR);
                    }
                    if(ServiceInfo.containsKey(Location1)){
                        OrderedHashtable stuff=(OrderedHashtable)ServiceInfo.get(Location1);
                        stuff.put(value.get("Type"),value);
                        ServiceInfo.put(Location1,stuff);
                        /*if(elem.equals("VERSE")){
                            System.out.println(value.get("Type"));
                            System.out.println(stuff.get(value.get("Type")));
                            System.out.println(ServiceInfo.get(Location1));
                        }*/
                        //Location1=Location1.substring(0,Location1.lastIndexOf("/"));
                    }
                    else{
                        // CREATE A NEW ORDEREDHASHTABLE TO STORE THE DATA
                                OrderedHashtable stuff=new OrderedHashtable();

                                if(!(value.get("Type") == null)){
                                    //There are instances of this info
                                    stuff.put(value.get("Type"),value);

                                }
                                else{
                                    //There are no other instances of this info
                                    if (elemRH==null || value==null){
                                        //System.out.println("A null set of values was encountered. Why? At point elemRH = "+elemRH+" and value = "+value+" and location = "+Location1);
                                        Location1=Location1.substring(0,Location1.lastIndexOf("/"));
                                        return;
                                    }
                                    stuff.put(elemRH,value);
                                    ServiceInfo.put(Location1,value);
                                }
                                ServiceInfo.put(Location1,stuff);
                    }
                    value=new OrderedHashtable();
                    Location1=Location1.substring(0,Location1.lastIndexOf("/"));
                    
                     //return;
                }
                
		/*if(elem.equals("TROPARION") && read)
		{
			variable.put("Troparion",textR);
			Information.put("Troparion",variable);
		}
		if(elem.equals("KONTAKION") && read)
		{
			variable.put("Kontakion",textR);
			Information.put("Kontakion",variable);
		}*/
		if(elem.equals("NAME") && read)
		{
			Information.put("Name",textR);
		}
	}

	public void text(String text)
	{
		textR=text;
	}
	public String getGrammar(String value)
	{
		if(Integer.parseInt(Information.get("ID").toString()) != -1)
		{
			grammar=(OrderedHashtable)Information.get(grammar);
		
			if(value.equals(""))
			{
				//System.out.println( Information.get("Name").toString());
				return Information.get("Name").toString();
			}
			try
			{
				return grammar.get(value).toString();
			}
			catch (Exception e)
			{
				return Information.get("Name").toString();
			}
		}
		else
		{
			return Information.get("Name").toString();
		}
	}
	public String getRank()
	{
		return Information.get("Rank").toString();
	}
	
	public String getIcon()
	{
		return Information.get("Icon").toString();
	}
	public String getID()
	{
		return Information.get("ID").toString();
	}
	public String getCycle()
	{
		return Information.get("Cycle").toString();
	}
        public OrderedHashtable getService(String Node, String Type){
            //System.out.println(ServiceInfo);
            //System.out.println("\n\n");
            //System.out.println(Node+"/"+Type);
            if (ServiceInfo.containsKey(Node)){
                OrderedHashtable stuff=(OrderedHashtable)ServiceInfo.get(Node);
                
                if (stuff.containsKey(Type)){
                    OrderedHashtable stuff1=(OrderedHashtable)stuff.get(Type);
                    
                    return stuff1;
                }
                else{
                    System.out.println(CommNames[0] + Node + CommNames[1] +Type);
                    return new OrderedHashtable();
                }
            }
            else{
                System.out.println(CommNames[2] +Node);
                return new OrderedHashtable();
            }
        }
        public OrderedHashtable getRH(String Node, String Type){
            
            if (RoyalHours.containsKey(Node)){


            OrderedHashtable stuff = (OrderedHashtable)RoyalHours.get(Node);
            //System.out.println(stuff);
            if (stuff.containsKey(Type)){
                OrderedHashtable stuff1=(OrderedHashtable)stuff.get(Type);
                return stuff1;
            }
            else{
                System.out.println(CommNames[3]);
                return new OrderedHashtable();
            }
            }
            else{
                System.out.println(CommNames[3]);
                return new OrderedHashtable();
            }

            
        }

	public static void main(String[] argz)
	{
		StringOp.dayInfo=new OrderedHashtable();
		StringOp.dayInfo.put("LS","2");
		//Commemoration Paramony = new Commemoration("P_3174");    //Paramony of Christmas
		System.out.println("THIS IS RUNNING ON DEBUG MODE, USING THE FILE FOR the Paramony of Christmas");
                //OrderedHashtable stuff=Paramony.getRH("Idiomel","11");
                //System.out.println(Paramony.getService("/ROYALHOURS/IDIOMEL","13"));
                //System.out.println(Paramony .ServiceInfo());
                //System.out.println(Paramony.getRH("IDIOMEL","11"));
		//System.out.println(Paramony);
                Commemoration Paramony=new Commemoration("B_3174"); //Forefeast of Christmas
                System.out.println(Paramony.getService("/KONTAKION","1"));
	}
	
}