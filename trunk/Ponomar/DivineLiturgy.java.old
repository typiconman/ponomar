package Ponomar;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;

/***************************************************************
DivineLiturgy.java :: MODULE THAT TAKES THE GIVEN DIVIN LITURGY (GOSPEL AND EPISTLE) READINGS FOR THE DAY,
THAT IS, PENTECOSTARION, MENELOGION, AND FLOATERS AND RETURNS
THE APPROPRIATE SET OF READINGS FOR THE DAY AND THEIR ORDER
ASSUMING THAT THE "LUCAN JUMP" IS BEING USED. THIS FUNCITON MUST BE
PREFORMED SEPARATELY FOR EACH TYPE OF READING: EPISTLE AND GOSPEL.
THIS PROGRAMME HAS BEEN GENERALISED TO ALLOW ANY SET OF RULES TO BE USED.

Further work will convert this into the programme that will allow the creation of the text for the Divine Liturgy.

DivineLiturgy.java is part of the Ponomar project.
Copyright 2008 Yuri Shardt
version 1.0: May 2008
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
public class DivineLiturgy implements DocHandler
{
	private final static String configFileName = "ponomar.config"; // CONFIGURATIONS FILE
	//private final static String generalFileName="Ponomar/xml/";
	private final static String triodionFileName   = "xml/triodion/";   // TRIODION FILE
	private final static String pentecostarionFileName = "xml/pentecostarion/"; // PENTECOSTARION FILE
	private static OrderedHashtable readings;	// CONTAINS TODAY'S SCRIPTURE READING
	private static OrderedHashtable PentecostarionS;		//CONTAINS THE PENTECOSTARION READINGS (SEQUENTIAL (rjadovoje) READINGS!)
	private static OrderedHashtable MenalogionS;		//CONTAINS THE MENALOGION READINGS, EXCLUDING ANY FLOATERS
	private static OrderedHashtable FloaterS;
	private static OrderedHashtable Information;		//CONTAINS COMMANDS ABOUT HOW TO CARRY OUT THE ORDERING OF THE READINGS
	private static String Glocation;
	private static LanguagePack Phrases;//=new LanguagePack();
	private static String[] TransferredDays;//=Phrases.obtainValues((String)Phrases.Phrases.get("DayReading"));
	private static String[] Error;//=Phrases.obtainValues((String)Phrases.Phrases.get("Errors"));
        private static Helpers findLanguage;//=new Helpers();
        private static StringOp Analyse=new StringOp();
	

	public DivineLiturgy(OrderedHashtable dayInfo) {
        Analyse.dayInfo=dayInfo;
        Phrases=new LanguagePack(dayInfo);
	TransferredDays=Phrases.obtainValues((String)Phrases.Phrases.get("DayReading"));
	Error=Phrases.obtainValues((String)Phrases.Phrases.get("Errors"));
        findLanguage=new Helpers(Analyse.dayInfo);
        }
	
//THESE ARE THE SAME FUNCTION AS IN MAIN, BUT TRIMMED FOR THE CURRENT NEEDS
	public void startDocument() { }

	public void endDocument() { }

	public void startElement(String elem, Hashtable table)
	{
		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo. 
		if (table.get("Cmd") != null)
		{
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
			
                        if (Analyse.evalbool(table.get("Cmd").toString()) == false)
			{
				return;
			}
		}
		if (elem.equals("SAINT"))
		{
			try
			{
				int floatnum = Integer.parseInt((String)table.get("Name"));
				// Floater: READ FROM AN XML FILE FOR THE PARTICULAR FLOATER
				try
				{
					
					FileReader frf = new FileReader(findLanguage.langFileFind(Analyse.dayInfo.get("LS").toString(),"xml/float/" + floatnum + ".xml"));
					QDParser.parse(this, frf);
				}
				catch(Exception e)
				{
					System.out.println(Error[1]+ " " + floatnum);
				}
				finally
				{
					//COPY THE READINGS FROM THE FLOATER TO THE UNIQUE IDENTIFIER (2008/05/19 n.s. Y.S.)
					for (Enumeration e = readings.enumerateKeys(); e.hasMoreElements(); )
					{
						String type = (String)e.nextElement();
						
						if(FloaterS.containsKey(type))
						{
							String vect = readings.get(type).toString();
							Vector vect2 = (Vector)FloaterS.get(type);
							vect2.add(vect.substring(1,vect.length()-1).trim());
							FloaterS.put(type, vect2);
						}
						else
						{
							Vector vect = (Vector)readings.get(type);
							FloaterS.put(type,vect);
						}
					}
					readings.clear();
					return;
				}
			}
			catch (NumberFormatException nfe)
			{
				// Not a floater, just continue (bad use of exceptions on my part)
			}
		}
		if (elem.equals("COMMAND"))
		{
		//THIS WILL STORE ALL THE POSSIBLE COMMANDS FOR A GIVEN SITUATION AND ALLOW THE RESULTS TO BE DETEMINED.
		String name = (String)table.get("Name");
		String value=(String)table.get("Value");
		//IF THE GIVEN name OCCURS IN THE information HASHTABLE THAN AUGMENT ITS VALUES.
		if (Information.containsKey(name))
		{
			Vector previous = (Vector)Information.get(name);
			previous.add(value);
			Information.put(name,previous);
		}
		else
		{
			Vector vect = new Vector();
			vect.add(value);
			Information.put(name,vect);
		}
		
		}
		//ALL WE CARE ABOUT ARE THE SCRIPTURE READINGS
		 if (elem.equals("SCRIPTURE"))
		{
			String type = (String)table.get("Type");
			String reading = (String)table.get("Reading");
			if (readings.containsKey(type))
			{
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
		} 		
	}
	public void endElement(String elem) { }

	public void text(String text) { }
	
public static String Readings(Vector q1, Vector q2, Vector q3, String ReadingType, JDate today,OrderedHashtable dayInfo)
{
    Analyse.dayInfo=dayInfo;
	/********************************************************
	SINCE I HAVE CORRECTED THE SCRIPTURE READINGS IN THE MAIN FILE, I CAN NOW PRECEDE WITH A BETTER VERSION OF THIS PROGRAMME!
	********************************************************/
	OrderedHashtable SortedReadings = new OrderedHashtable();
	Information = new OrderedHashtable();
	int doy = Integer.parseInt(Analyse.dayInfo.get("doy").toString());
	int dow = Integer.parseInt(Analyse.dayInfo.get("dow").toString());
	int nday = Integer.parseInt(Analyse.dayInfo.get("nday").toString());
	Vector empty = new Vector();
	
	//DETERMINE THE GOVERNING PARAMETERS FOR COMPILING THE READINGS
	try
	{
		FileReader frf = new FileReader(findLanguage.langFileFind(Analyse.dayInfo.get("LS").toString(),"xml/Commands/DivineLiturgy.xml"));
		DivineLiturgy a1 =new DivineLiturgy(Analyse.dayInfo.clone());
                QDParser.parse(a1, frf);
	}
	catch (Exception e)
	{
		e.printStackTrace();
	}
	
	if(q1 != null)
	{
		SortedReadings.put("Pentecostarion",q1);
		Vector id = new Vector();
		id.add(Analyse.dayInfo.get("dow").toString());
		SortedReadings.put("PentecostarionType",id);		
	}
	else
	{
		Vector q=new Vector();
		Vector r=new Vector();
		SortedReadings.put("Pentecostarion",q);
		SortedReadings.put("PentecostarionType",r);
	}
	
	if(q2 != null)
	{
		SortedReadings.put("Menalogion",q2);
		Vector a=new Vector();
		a.add("Today's Menalogion");
		SortedReadings.put("MenalogionType",a);
		
	}
	else
	{
		Vector q=new Vector();
		Vector r=new Vector();
		SortedReadings.put("Menalogion",q);
		SortedReadings.put("MenalogionType",r);
	}
	if(q3 != null)
	{
		SortedReadings.put("Floaters",q3);
		Vector a=new Vector();
		a.add("Today's Floater");
		SortedReadings.put("FloatersType",a);
	}
	else
	{
		Vector q=new Vector();
		Vector r=new Vector();
		SortedReadings.put("Floaters",q);
		SortedReadings.put("FloatersType",r);		
	}
	
	OrderedHashtable SuppressedReadings = new OrderedHashtable();			//THIS CONTAINS A LISTING OF THE READINGS THAT ARE SUPPRESSED FOR A GIVEN DAY! IN THE ORDER, PENTECOSTARION, MENALOGION, FLOATER
	//Vector OrderedReadings=OrderReadings(Pentecostarion, Menalogion, Floater, ReadingType,SuppressedReadings);
	
	/*NOTE: SINCE THE 33rd SUNDAY AFTER PENTECOST DOES NOT HAVE ANY ASSOCIATED READINGS IN THE PENTECOSTARION,
	THIS CAN LEAD TO DIFFICULTIES IN DOING CERTAIN THINGS! THUS, THE FOLLOWING CORRECTIONS.
	*/
	if ((doy>=4 && doy<=10) && (dow == 0) && ReadingType.equals("apostol"))
	{
		//IF THERE IS AN APOSTOL ON THIS DAY, THEN THERE MAY BE ISSUES WITH ITS PRESENCE. 
		//NOTE: NOTHING IS CURRENTLY DONE ABOUT THIS!
	}
	
	Suppress(SortedReadings, SuppressedReadings,Analyse.dayInfo);		//AT THIS POINT IT IS IRRELEVANT ABOUT WHAT READINGS ARE SKIPPED, BUT LATER IT WILL BE!
	//CHECK WHETHER OR NOT IT IS DESIRED TO TRANSFER THE SKIPPED SEQUENTIAL READINGS
	Vector transfer=(Vector)Information.get("Transfer");
	int transfer1 = Integer.parseInt((String)transfer.get(0));
	if (transfer1 == 1)
	{
	/*NOW CONSIDER ANY SUPPRESSED READINGS:
	//THE FOLLOWING SHOULD BE NOTED: 
	1. READINGS ARE NEVER TRANSFERRED TO A SUNDAY
	2. TUESDAY CAN HAVE 2 SETS OF READINGS TRANSFERRED TO IT: MONDAY'S AND WEDNESDAY'S
	*/
	//NOTE 2: NO READINGS ARE TRANSFERRED DURING LENT, THAT IS, -48 <= nday <=0.
	Vector transferRule=(Vector)Information.get("TransferRulesB");
	boolean transfer2 = Analyse.evalbool((String)transferRule.get(0));
	if(transfer2)		//St. NICHOLAS'S DAY HAS A SPECIAL SET OF RULES
	{
		//IT IS OBLIGATORY TO CHECK THE NEXT DAY IF ANY READINGS ARE TRANSFERRED!
		//THE SAME PROCEDURE AS IN Main.java WILL BE FOLLOWED!
		
		
		today.addDays(1);
		
		
		// PUT THE RELEVANT DATA IN THE HASH FOR TOMORROW
		Analyse.dayInfo.put("dow", today.getDayOfWeek());
		Analyse.dayInfo.put("doy", today.getDoy());
		nday = (int)JDate.difference(today, Paschalion.getPascha(today.getYear()));
		int ndayP = (int)JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
		//REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
		int ndayF = (int)JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));	
		Analyse.dayInfo.put("nday", nday);
		Analyse.dayInfo.put("ndayP", ndayP);
		Analyse.dayInfo.put("ndayF", ndayF);
		
		getReadings(SortedReadings,ReadingType, today);
				
		
		today.subtractDays(1);
		Analyse.dayInfo.put("dow", today.getDayOfWeek());
		Analyse.dayInfo.put("doy", today.getDoy());
		nday = (int)JDate.difference(today, Paschalion.getPascha(today.getYear()));
		ndayP = (int)JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
		//REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
		ndayF = (int)JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));		
		Analyse.dayInfo.put("nday",nday);
		Analyse.dayInfo.put("ndayP", ndayP);
		Analyse.dayInfo.put("ndayF", ndayF);
	}
	//NOW WE NEED TO CHECK YESTERDAY'S READINGS, BUT THIS WILL ONLY OCCUR ON A TUESDAY OR DEC. 6th
	transferRule=(Vector)Information.get("TransferRulesF");
	transfer2 = Analyse.evalbool((String)transferRule.get(0));
	if(transfer2)		//IF IT IS A SATURDAY, THEN THE READINGS WILL BE SKIPPED, ???
	{
		//IT IS OBLIGATORY TO CHECK THE NEXT DAY IF ANY READINGS ARE TRANSFERRED!
		//THE SAME PROCEDURE AS IN Main.java WILL BE FOLLOWED!
		
		
		today.subtractDays(1);
		
		
		// PUT THE RELEVANT DATA IN THE HASH FOR TOMORROW
		Analyse.dayInfo.put("dow", today.getDayOfWeek());
		Analyse.dayInfo.put("doy", today.getDoy());
		nday = (int)JDate.difference(today, Paschalion.getPascha(today.getYear()));
		int ndayP = (int)JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
		//REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
		int ndayF = (int)JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));	
		Analyse.dayInfo.put("nday", nday);
		Analyse.dayInfo.put("ndayP", ndayP);
		Analyse.dayInfo.put("ndayF", ndayF);
		
		getReadings(SortedReadings,ReadingType, today);
			
		
		today.addDays(1);
		Analyse.dayInfo.put("dow", today.getDayOfWeek());
		Analyse.dayInfo.put("doy", today.getDoy());
		nday = (int)JDate.difference(today, Paschalion.getPascha(today.getYear()));
		ndayP = (int)JDate.difference(today, Paschalion.getPascha(today.getYear() - 1));
		//REQUIRED FOR LUCAN JUMP CALCULATIONS! ADDED 2008/05/17 n.s.
		ndayF = (int)JDate.difference(today, Paschalion.getPascha(today.getYear() + 1));		
		Analyse.dayInfo.put("nday",nday);
		Analyse.dayInfo.put("ndayP", ndayP);
		Analyse.dayInfo.put("ndayF", ndayF);
	}}
		
	
	//NOW THAT IT HAS BEEN DETERMINED WHAT READINGS ARE TO BE TAKEN, WE CAN CREATE THE OUTPUT STRING
	//NOW EACH OF THE 3 TYPES OF READINGS WILL BE FORMATED TO THE APPROPRIATE DISPLAY FORMAT
	//System.out.println("The value in Pentecostarion is " + SortedReadings.get("Pentecostarion") + " The values in Menalogion is " + SortedReadings.get("Menalogion"));
	Vector vectP = (Vector) SortedReadings.get("Pentecostarion");
	Vector vectM=(Vector) SortedReadings.get("Menalogion");
	Vector vectF=(Vector) SortedReadings.get("Floaters");
	Vector vectTP=(Vector) SortedReadings.get("PentecostarionType");
	Vector vectTM=(Vector) SortedReadings.get("MenalogionType");
	Vector vectTF=(Vector) SortedReadings.get("FloatersType");
	
	String Pentecostarion=format(vectP,vectTP);
	String Menalogion=format(vectM,vectTM);
	String Floaters=format(vectF,vectTF);
	
	//THE GENERAL FORMAT IS: FLOATERS, PENTECOSTARION, MENALOGION, EXCEPT ON SATURDAYS WHERE IT IS FLOATERS, MENALOGION, PENTECOSTARION	
	
	if (dow == 6)
	{
		//ON SATURDAYS, THE READINGS FROM THE MENALOGION TAKE PRECEDENCE.
		return Display(Floaters,Menalogion,Pentecostarion);
	}
	if (dow == 0)
	{
		//ON SUNDAYS, THE READINGS FROM THE PENTECOSTARION SHOULD TAKE PRECEDENCE
            
		return Display(Pentecostarion,Floaters,Menalogion);
	}
	return Display(Floaters,Pentecostarion,Menalogion);
}

private static void getReadings(OrderedHashtable SortedReadings, String ReadingType, JDate today)
{
	String filename = "";
	int lineNumber = 0;
	readings = new OrderedHashtable();
	PentecostarionS= new OrderedHashtable();
	MenalogionS= new OrderedHashtable();
	FloaterS= new OrderedHashtable();
	int nday = (int)JDate.difference(today, Paschalion.getPascha(today.getYear()));
	
	//I COPIED THIS FROM THE Main.java FILE BY ALEKS WITH MY MODIFICATIONS (Y.S.)
	//FROM HERE UNTIL
	if (nday >= -70 && nday < 0)
	{
		filename = triodionFileName;
		lineNumber = Math.abs(nday);
	}
	else if (nday < -70)
	{
		// WE HAVE NOT YET REACHED THE LENTEN TRIODION
		filename = pentecostarionFileName;
		JDate lastPascha = Paschalion.getPascha(today.getYear() - 1);
		lineNumber = (int)JDate.difference(today, lastPascha) + 1;
	}
	else
	{
		// WE ARE AFTER PASCHA AND BEFORE THE END OF THE YEAR
		filename =pentecostarionFileName;
		lineNumber = nday + 1;
	}
	filename += lineNumber >= 10 ? lineNumber + ".xml" : "0" + lineNumber + ".xml"; // CLEANED UP
	// READ THE PENTECOSTARION / TRIODION INFORMATION
	try
	{
		FileReader frf = new FileReader(findLanguage.langFileFind(Analyse.dayInfo.get("LS").toString(),filename));
		DivineLiturgy a =new DivineLiturgy(Analyse.dayInfo.clone());
                
		QDParser.parse(a, frf);
	}
	catch (Exception e)
	{
		e.printStackTrace();
	}
	//ADDED 2008/05/19 n.s. Y.S.
	//COPYING SOME READINGS FILES
		
		
	for (Enumeration e = readings.enumerateKeys(); e.hasMoreElements(); )
	{
		String type = (String)e.nextElement();
		Vector vect = (Vector)readings.get(type);
		PentecostarionS.put(type, vect);
	}
		
		
	readings.clear();
	FloaterS.put("","");			//ALLOWS CERTAIN THINGS TO BE DONE LATER!!	
	// GET THE MENAION DATA
	int m = today.getMonth();
	int d = today.getDay();
	
	filename = "";
	filename += m < 10 ? "xml/0" + m : "xml/" + m;  // CLEANED UP
	filename += d < 10 ? "/0" + d : "/" + d; // CLEANED UP
	filename += ".xml";
	// PARSE THE MENAION XML FILE
	try
	{
		FileReader fr = new FileReader(findLanguage.langFileFind(Analyse.dayInfo.get("LS").toString(),filename));
		DivineLiturgy a =new DivineLiturgy(Analyse.dayInfo.clone());
		QDParser.parse(a, fr);
	}
	catch (Exception e)
	{
		System.out.println(Error[2] + "  " + today.toString() + Analyse.dayInfo.get("Colon")+" " + e.toString());
	}
	//ADDED 2008/05/19 n.s. Y.S.
	
	for (Enumeration e = readings.enumerateKeys(); e.hasMoreElements(); )
	{
		String type = (String)e.nextElement();
		Vector vect = (Vector)readings.get(type);
		MenalogionS.put(type, vect);
	}
		
	//IT IS NOW NECESSARY TO PARSE THE RESULTS!
	
		
	if(MenalogionS.containsKey(ReadingType) || PentecostarionS.containsKey(ReadingType) || FloaterS.containsKey(ReadingType))
	{
			
		OrderedHashtable SortedReadingsII= new OrderedHashtable();			//THIS IS THE INTERNAL SET OF READINGS
		try
		{
			SortedReadingsII.put("Pentecostarion",PentecostarionS.get(ReadingType));
			Vector id = new Vector();
			id.add(Analyse.dayInfo.get("dow").toString());
			SortedReadingsII.put("PentecostarionType",id);		
		}
		catch (Exception e)
		{
			Vector q=new Vector();
			Vector r=new Vector();
			SortedReadingsII.put("Pentecostarion",q);
			SortedReadingsII.put("PentecostarionType",r);
		}
		try
		{
			SortedReadingsII.put("Menalogion",MenalogionS.get(ReadingType));
			Vector a=new Vector();
			a.add("Tomorrow's Menalogion");
			SortedReadingsII.put("MenalogionType",a);
		}
		catch   (Exception e)
		{
			Vector q=new Vector();
			Vector r=new Vector();
			SortedReadingsII.put("Menalogion",q);
			SortedReadingsII.put("MenalogionType",r);
		}
		try
		{
			SortedReadingsII.put("Floaters",FloaterS.get(ReadingType));
			Vector b=new Vector();
			b.add("Tomorrow's Floater");
			SortedReadingsII.put("FloatersType",b);
		}
		catch  (Exception e)
		{
			Vector q=new Vector();
			Vector r=new Vector();
			SortedReadingsII.put("Floaters",q);
			SortedReadingsII.put("FloatersType",r);		
		}
			
		//UNTIL HERE
		//NOW CALL THE FUNTION
		OrderedHashtable SuppressedReadings = new OrderedHashtable();
		Suppress(SortedReadingsII, SuppressedReadings,Analyse.dayInfo);			//WE ARE INTERESTED IN THE SUPPRESSED READINGS
		//NOW COMBINE THE READINGS
		
		for(Enumeration e=SortedReadings.enumerateKeys();e.hasMoreElements();)
		{
			String type = (String)e.nextElement();
			Vector vect1 = (Vector)SortedReadings.get(type);
			String reading=(String)SuppressedReadings.get(type);
				
			if ( reading != null)
			{
				if(!reading.equals("[]")) 
				{
					int n = reading.length();
					vect1.add( reading.substring(1,n-1).trim());
					SortedReadings.put(type,vect1);
				}
			
			}				
		}
	}
}
protected static String Display(String a,String b,String c)
{
	//THIS FUNCTION TAKES THE POSSIBLE 3 READINGS AND COMBINES THEM AS APPROPRIATE, SO THAT NO SPACES OR OTHER UNDESIRED STUFF IS DISPLAYED!
	String output="";
	if (a.length()>0)
	{
		output +=a; 
	}
	if (b.length() >0)
	{
		if (output.length()>0)
		{
			output +=Analyse.dayInfo.get("ReadSep")+" ";
		}
		output +=b;
	}
	if (c.length() > 0)
	{
		if (output.length()>0)
		{
			output +=Analyse.dayInfo.get("ReadSep")+" ";
		}
		output +=c;
		
	}
	
	//TECHNICALLY, IF THERE ARE 3 OR MORE READINGS, THEN SOME SHOULD BE TAKEN "FROM THE BEGINNING" (nod zachalo).
	return output;
}
public static String format(Vector vect,Vector Type)
{
	String output = "";
	//AT THIS POINT, THE PENTECOSTARION READINGS WILL BE FORMATED SO THAT THEY ARE SEQUENTIAL BY THE WEEK,
	//ESPECIALLY IF THERE ARE ANY RETRACTIONS OR THE LIKE.
	try
	{
		//The Readings should be sorted based on the order of values in Type, but only if it is numeric, that is, it is the Pentecostarion data
		if(Type.size()>1)
		{
			//THIS IS NOT THE MOST EFFECTIVE TECHNIQUE, BUT THEN THERE WILL ONLY EVER TRULY BE 2 READINGS TO MOVE!
				int secondDay=Integer.parseInt((String)Type.get(1));
				int firstDay=Integer.parseInt((String)Type.get(0));
				//SOLVES AN ORDERING ISSUE WITH SUNDAY BEING ORIGINALLY PLACED BEFORE SATURDAY,
				//WHEN IT SHOULD HAVE BEEN AFTER
				//ADDED 2008/08/04 n.s. by Y.S.
				if(secondDay == 0)
				{
					secondDay=7;
				}
				if(firstDay == 0)
				{
					firstDay = 7;
				}
				
				if( Integer.parseInt((String)Type.get(0)) > secondDay)
				{
					
					Object a = vect.set(0,vect.get(1));
					vect.set(1,a);
					a=Type.set(0,Type.get(1));
					Type.set(1,a);					
				}
			
		}
	}
	catch (Exception e)
	{
	}
	Bible ShortForm=new Bible(Analyse.dayInfo);
	try
	{
		Enumeration e3=Type.elements();
		for (Enumeration e2=vect.elements();e2.hasMoreElements();)
		{
			String reading = (String) e2.nextElement();
			output+=ShortForm.getHyperlink(reading);
			
			if(Type.size()>1)
			{
				output += " (" + Week((String) e3.nextElement()) + ")";
			}
			
			if(e2.hasMoreElements())
			{
				output +=Analyse.dayInfo.get("ReadSep")+" ";		//IF THERE ARE MORE READINGS OF THE SAME TYPE APPEND A SEMICOLON!
			}
		}
	}
	catch (Exception a)
	{
	}
	return output;
}
private static String Week(String dow)
{
	//CONVERTS THE DOW STRING INTO A NAME. THIS SHOULD BE IN THE ACCUSATIVE CASE
	try
	{
		return TransferredDays[Integer.parseInt(dow)];
	}
	catch (Exception a)
	{
		return dow;		//A DAY OF THE WEEK WAS NOT SENT
	}
}
protected static void LeapReadings(OrderedHashtable CurrentReadings)
{
	//SKIPS THE READINGS IF THERE ARE ANY BREAKS!
	int doy = Integer.parseInt(Analyse.dayInfo.get("doy").toString());
	int dow = Integer.parseInt(Analyse.dayInfo.get("dow").toString());
	int nday = Integer.parseInt(Analyse.dayInfo.get("nday").toString());
	int ndayF=Integer.parseInt(Analyse.dayInfo.get("ndayF").toString());
	int ndayP=Integer.parseInt(Analyse.dayInfo.get("ndayP").toString());
	
	//IN ALL CASES ONLY THE PENTECOSTARION READINGS ARE EFFECTED!
	Vector empty = new Vector();
	//USING THE NEWER VERSION OF STORED VALUES
	//EACH OF THE STORED COMMANDS ARE EVALUATED IF ANY ARE TRUE THEN THE READINGS ARE SKIPPED IF THERE ARE ANY FURTHER READINGS ON THAT DAY.
	Vector vect1=(Vector) CurrentReadings.get("Menalogion");
	Vector vect2=(Vector) CurrentReadings.get("Floaters");
	if((vect1.size()+vect2.size())>0)
	{
		Vector vect = (Vector) Information.get("Suppress");
		if(vect != null)
		{
			for(Enumeration e2=vect.elements();e2.hasMoreElements();)
			{	
				String Command = (String)e2.nextElement();
				if (Analyse.evalbool(Command))
				{
					//THE CURRENT COMMAND WAS TRUE AND THE SEQUENTITIAL READING IS TO BE SUPPRESSED
					CurrentReadings.put("Pentecostarion",empty);
					CurrentReadings.put("PentecostarionType",empty);
					return;
				}
			
			}
		}
	}
	
	return;
}
private static void Suppress(OrderedHashtable CurrentReadings, OrderedHashtable Suppressed, OrderedHashtable dayInfo)
{
    Analyse.dayInfo=dayInfo;
    //THIS FUNCTION CONSIDERS WHAT HOLIDAYS ARE CURRENTLY OCCURING AND RETURNS THE READINGS FOR THE DAY, WHERE SUPPRESSED CONTAINS THE READINGS THAT WERE SUPPRESSED.
	int doy = Integer.parseInt(Analyse.dayInfo.get("doy").toString());
	int dow = Integer.parseInt(Analyse.dayInfo.get("dow").toString());
	int nday = Integer.parseInt(Analyse.dayInfo.get("nday").toString());
	int ndayF=Integer.parseInt(Analyse.dayInfo.get("ndayF").toString());
	int ndayP=Integer.parseInt(Analyse.dayInfo.get("ndayP").toString());
	LeapReadings(CurrentReadings);		//THIS ALLOWS APPROPRIATE SKIPPING OF READINGS OVER THE NATIVITY SEASON!
	
	/******************************************************
	FOR ALL HOLIDAYS OF THE FIRST CLASS, THAT IS, OF THE LORD, THEN ONLY THE MENALOGION
	READINGS ARE TAKEN. THE PENTECOSTARION READINGS CAN BE TRANSFERRED.
	THE FOLLOWING FESTIVALS ARE CONSIDERED:
	1. EXALTATION: SEPTEMBER 14th: DOY == 256
	2. CHRISTMAS: DECEMBER 25th: DOY == 358
	3. THEOPHANY: JANUARY 6th: DOY == 5
	4. TRANSFIGURATION: AUGUST 6th: DOY == 217
	******************************************************/
	Vector empty = new Vector();
	
	if (doy == 256 || doy == 358 || doy == 5 || doy == 217)
	{
		
		Suppressed.put("Pentecostarion",CurrentReadings.get("Pentecostarion").toString());
		Suppressed.put("PentecostarionType",CurrentReadings.get("PentecostarionType").toString());
		Suppressed.put("Floaters",CurrentReadings.get("Floaters").toString());
		Suppressed.put("FloatersType",CurrentReadings.get("FloatersType").toString());
		
		CurrentReadings.put("Pentecostarion",empty);
		CurrentReadings.put("Floaters",empty);
		CurrentReadings.put("PentecostarionType",empty);
		CurrentReadings.put("FloatersType",empty);
		
		return;				//There is no need for any other readings to be considered!
	}
	
	/********************************
	FOR ALL HOLIDAY OF THE SECOND CLASS, THAT IS, OF THE MOTHER OF GOD, THEN ONLY THE MENALOGION
	READINGS ARE TAKEN, IF IT FALLS DURING MONDAY TO SATURDAY, OTHERWISE THE READINGS
	ARE COMBINED WITH THE SEQUENTIAL READINGS.
	THE FOLLOWING FESTIVALS ARE CONSIDERED:
	1. ANNUNCIATION: MARCH 25th: DOY == 83 (ALTHOUGH THE RULES ARE ACTUALLY MORE INVOLVED, HENCE SKIPPED)
	2. PRESENTATION OF THE LORD: FEBRUARY 2nd: DOY == 32, BUT NOT IF NDAY == -48 (FIRST DAY OF LENT).
	3. NATIVITY OF THE MOTHER OF GOD: SEPTEMBER 8th: DOY == 250
	4. DORMITION OF THE MOTHER OF GOD: AUGUST 15th: DOY == 226
	5. ENTRY OF THE MOTHER OF GOD INTO THE TEMPLE: NOVEMBER 21st: 324
	**************************************************************************************/
	if ((doy == 32 && nday != -48) || doy == 250 || doy == 226 || doy == 324)
	{
		if (dow != 0)
		{
			//MoveReadings();
			Suppressed.put("Pentecostarion",CurrentReadings.get("Pentecostarion").toString());
			Suppressed.put("PentecostarionType",CurrentReadings.get("PentecostarionType").toString());
			Suppressed.put("Floaters",CurrentReadings.get("Floaters").toString());
			Suppressed.put("FloatersType",CurrentReadings.get("FloatersType").toString());
			CurrentReadings.put("Pentecostarion",empty);
			CurrentReadings.put("Floaters",empty);
			CurrentReadings.put("PentecostarionType",empty);
			CurrentReadings.put("FloatersType",empty);
			return;					//There is no need for any other readings to be considered!
		}
		else
		{
			//ALL THE READINGS ARE COMBINED IN SOME FASHION, HOWEVER SOME COULD POTENTIAL BE REDUCED DUE TO REPEATS
			 
			return;					//CHECK WHETHER IS TRUE
		}
	}
	
	/*******************************************************************
	 FOR SAINTS WHOSE HOLIDAYS HAVE A VIGIL, SIMILAR RULES TO THAT FOR CLASS 2 FEASTS APPLIES.
	ACCORDING TO THE RUSSIAN ORTHODOX CALENDAR AT (http:--days.pravoslavie.ru-Days-20081125.htm), THE FOLLOWING
	SAINTS COUNT FOR THIS RULE:
	1. SAINT BASIL THE GREAT: JANUARY 1st: DOY == 0
	2. (SYNAXIS OF THE 70 APOSTLES: JANUARY 4th: DOY == 3 (?)) NOT IMPLEMENTED
	3. SYNAXIS OF THE 3 HIERARCHS: JANUARY 30th: DOY == 29
	4. PETER AND PAUL: JUNE 29th: DOY == 179
	5. APPEARANCE OF THE ICON OF THE MOTHER OF GOD IN KAZAN: JULY 8th: DOY == 188
	6. ST. VLADIMIR: JULY 15th: DOY == 195
	7. (UNCOVERING THE RELICS OF VENERABLE SERAPHIM OF SAROV: JULY 19th: DOY == 222): NOT CONSIDERED
	8. ST. PANTELEIMON: JULY 27th: DOY == 207
	9. PROCESSION OF THE LIFE-GIVING CROSS: AUGUST 1st: DOY == 212
	10. BEHEADING OF ST. JOHN THE BAPTIST: AUGUST 29th: DOY == 240
	11. REPOSE OF APOSTLE JOHN THE THEOLOGIAN: SEPTEMBER 26th: DOY == 268 (TO THE CLOSEST DAY AFTER!?!)
	12. PROTECTION OF THE MOTHER OF GOD: OCTOBER 1st: DOY == 273
	13. FEAST OF THE KAZAN ICON OF THE MOTHER OF GOD: OCTOBER 22nd: DOY == 294 (ONLY THE SLAVIC ORTHODOX)
	14. ST. DEMETRIUS: OCTOBER 26th: DOY == 298
	15. SYNAXIS OF ST. MICHAEL AND ALL BODILESS BEINGS: NOVEMBER 8th: DOY == 311
	16. ST. JOHN CHRYSTOSTOM: NOVEMBER 13th: DOY == 316
	17. VENERABLE SABBAS THE SANCTIFIED: DECEMBER 5th: DOY == 338
	18. ST. NICHOLAS: DECEMBER 6th: DOY == 339 (FORWARDS DUE TO ABOVE)
	19. NATIVITY OF ST. JOHN THE BAPTIST JUNE 24th: DOY == 174
	20. THE START OF THE CHURCH YEAR SEPTEMBER 1st: DOY == 243
	IN THEORY, THIS SHOULD BE ABLE TO BE DETERMINED BY READING THE APPROPRIATE FILES IN THE MENALOGION, BUT
	THEN IT WOULD BE QUITE DIFFICULT TO PROPERLY IMPLEMENT.
	********************************************************************************************************************/
	/*******************************************************************************************************************
	AS AN IMPROVEMENT THESE DAYS ARE TRANSFERRED TO THE COMMANDS FILE AND THERE CAN BE MODIFIED AS NEED BE.
	ADDED THE PARAMONY OF CHRISTMAS AND THEOPHANY AS SKIPPING DAYS WITH TRANSFERRING OF READINGS.
	*********************************************************************************************************************/	
	if (dow != 0)
	{
		Vector vect = (Vector) Information.get("Class3Transfers");
		if(vect != null)
		{
			for(Enumeration e2=vect.elements();e2.hasMoreElements();)
			{	
				String Command = (String)e2.nextElement();
				if (Analyse.evalbool(Command))
				{
					//THE CURRENT COMMAND WAS TRUE AND THE SEQUENTITIAL READING IS TO BE SUPPRESSED/TRANSFERRED
					Suppressed.put("Pentecostarion",CurrentReadings.get("Pentecostarion").toString());
					Suppressed.put("PentecostarionType",CurrentReadings.get("PentecostarionType").toString());
					Suppressed.put("Floaters",CurrentReadings.get("Floaters").toString());
					Suppressed.put("FloatersType",CurrentReadings.get("FloatersType").toString());
					CurrentReadings.put("Pentecostarion",empty);
					CurrentReadings.put("Floaters",empty);
					CurrentReadings.put("PentecostarionType",empty);
					CurrentReadings.put("FloatersType",empty);
					return;
				}
			}
		}
		return;					//There is no need for any other readings to be considered!
	}
		
	//AT THIS POINT, THE PENTECOSTARION READINGS MAY BE REDUCED DUE TO REPEATS
	
	return;
}

	public static void main(String[] argz)
	{
		
	}
}
