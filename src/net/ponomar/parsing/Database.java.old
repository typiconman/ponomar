package Ponomar.parsing;

import java.io.*;
import java.sql.*;

import Ponomar.utility.OrderedHashtable;
import Ponomar.utility.StringOp;
/***********************************************************************
THIS MODULE READS XML FILES THAT CONTAIN THE <DAY> TYPE 
AND STORES THE INFORMATION IN A MANNER USUABLE BY OTHER COMPONENTS
OF PONOMAR.

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

public class Database
{
	private final static String Location  = "Ponomar/xml/";   // THE LOCATION OF THE DAY FILES
	static String dbDriver="org.hsqldb.jdbcDriver";
	static String dbURL="jdbc:hsqldb:hsql://localhost";
	static Connection con=null;
	static Statement stat=null;
	//private static boolean read=false;
	//private String filename;
	//private int lineNumber;
	//private LanguagePack Text=new LanguagePack();
	//private String[] ServiceNames=Text.obtainValues((String)Text.Phrases.get("ServiceRead"));
	//private String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	//private OrderedHashtable Commemorations;
	//private Vector CommemorationList;
	//private String[] Rank;
	//private String[] Icons;
	//private OrderedHashtable readings;
	//private OrderedHashtable grammar;
	//private OrderedHashtable variable;
	//private String textR;
	
	private StringOp Analyse=new StringOp();
	protected Database(OrderedHashtable dayInfo)
	{
		//THIS OPENS A NEW INSTANCE OF THE CLASS AND RESETS EVERYTHING TO DEFAULT VALUES
		//LOADS THE DATABASE, CREATES ONE IF THERE IS NONE CREATED FOR THE GIVEN LANGUAGE
		Analyse.setDayInfo(dayInfo);
    		try
    		{
    			Class.forName(dbDriver);
    			con=DriverManager.getConnection(dbURL,"sa","");
    			stat=con.createStatement();
    		}
    		catch(SQLException e)
    		{
    			System.out.println("SQL Exception");
    			e.printStackTrace();
    		}
    		catch(ClassNotFoundException cEx)
    		{
    			System.out.println("Class not found exception");
    			cEx.printStackTrace();
    		}
				
	}
	public void create()
	{
		//THIS FUNCTION CREATES THE DATABASE FILE IF REQUIRED AND LOADS THE FILE INTO THE PROGRAMME!
		String FileName="Database"+Analyse.getDayInfo().get("LS")+".txt";
		System.out.println(FileName);
		File f = new File(Location+FileName);
    		if(!f.exists())
    		{
    			//THE DATABASE FILE IS MISSING OR NOT YET CREATED FOR THE GIVEN LANGUAGE.
    			String[] Months={"01","02","03","04","05","06","07","08","09","10","11","12"};
    			String[] Days={"31","29","31","30","31","30","31","31","30","31","30","31"};
    			try
    			{
    				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Location+FileName),"UTF8"));
	    			int Count=-1;
				for(int i=0;i<12;i++)
    				{
    					for(int j=0;j<=Integer.parseInt(Days[i]);j++)
    					{
    						String FileName2;
	    					Count++;
    						
    						//THE CONFOUNDED LEAP YEARS!!!!
    						if(i==1 && j==29)
    						{
    							Count=366;
    						}
	    					if(Count==367)
    						{
    							Count=31+28+1;
    						}
    						if(j<10)
    						{
    							FileName2=Months[i]+"/"+Months[j]+".xml";
	    					}
    						else
    						{
    							FileName2=Months[i]+"/"+j+".xml";
    						}
    						Days Events= new Days(FileName2,Analyse.getDayInfo());
    						for(int k=0;k<Events.getNumber();k++)
    						{
    					  			out.write(Events.getID(k)+"%"+Events.getGrammar(k,"")+"%"+"F"+Count+"%"+Events.getCycle(k));
    					  			out.newLine();
    						}
    					}
	    			}
	    			out.close();
    			}
    			catch(Exception e)
    			{
    				System.out.println("Error creating the file and hence there is no database.");
    				e.printStackTrace();
    				return;
    			}			
    		}
    		try
    		{
    			stat.executeUpdate("CREATE TEXT TABLE COMMEMORATIONS"+"(ID VARCHAR(10), NAME VARCHAR(256), DATE VARCHAR(10), CYCLE VARCHAR(100))");
    			stat.executeUpdate("SET TABLE COMMEMORATIONS SOURCE \""+FileName+"\";fs=%");
    		}
    		catch(SQLException e)
    		{
    			System.out.println("SQL Exception");
    			e.printStackTrace();
    		}    		
	}
	public void select(String SQLCommand)
	{
		//SEARCH THE DATABASE BASED ON THE GIVEN VALUE
		try
		{
			ResultSet rs=stat.executeQuery(SQLCommand);
			System.out.println("Query results");
			System.out.println("-------------------------------------------------------");
			//while (rs.next())
			for(int i=0;i<10;i++)
			{
				System.out.println("ID:\t"+rs.getString("ID"));
				System.out.println("Name:\t"+rs.getString("NAME"));
				System.out.println("Date:\t"+rs.getString("DATE"));
				System.out.println("Cycle:\t"+rs.getString("CYCLE"));
				System.out.println("---------------------------------------------------");
				rs.next();
			}
		}
		catch(SQLException e)
    		{
    			System.out.println("SQL Exception");
    			e.printStackTrace();
    		}			
		
	}
	public void close()
	{
		//THIS CLOSES THE DATABASE
		try
		{
			con.close();
		}
		catch(Exception e)
		{
		
		}
		try
		{
			stat.close();
		}
		catch(Exception e)
		{
		
		}
	}
		
	public static void main(String[] argz)
	{
		//THIS STILL DOES NOT WORK!!!
		OrderedHashtable dayInfo=new OrderedHashtable();
		dayInfo.put("LS","0");
		dayInfo.put("nday",1);
		dayInfo.put("dow",1);
		dayInfo.put("doy",1);
		Database trial=new Database(dayInfo);
		System.out.println("THIS IS RUNNING ON DEBUG MODE");
		trial.create();
		trial.select("SELECT * FROM COMMEMORATIONS");
		trial.close();
				
	}
	
}