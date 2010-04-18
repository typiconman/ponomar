package Ponomar;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.filechooser.FileFilter;

/***********************************************************************
THIS MODULE CREATES THE TEXT FOR THE ORTHODOX SERVICE OF THE FIRST HOUR (PRIME)
THIS MODULE IS STILL IN THE DEVELOPMENT PHASE.

(C) 2007, 2008 YURI SHARDT. ALL RIGHTS RESERVED.
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
public class RoyalHours implements DocHandler, ActionListener, ItemListener, PropertyChangeListener
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	//THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	//TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	//DURING THE COURSE OF A SINGLE WEEK.
	private final static String configFileName = "ponomar.config";   //CONFIGURATIONS FILE
	//private final static String octoecheosFileName   = "Ponomar/xml/Services/Octoecheos/";   // THE LOCATION OF THE BASIC SERVICE RULES
	private final static String ServicesFileName = "Ponomar/xml/Services/"; // THE LOCATION FOR ANY EXTRA INFORMATION
	//private static OrderedHashtable PrimesTK;
	//private static String FileNameIn="Ponomar/xml/Services/PRIMES1/";
	//private static String FileNameOut=FileNameIn+"Primes.html";
	private static String text;
	private static boolean read=false;
	private static String Type;
	//private String Troparion1;
	//private String Kontakion1;
	//private String Kontakion2;
	//private String Troparion2;
	//private final static String triodionFileName   = "Ponomar/xml/triodion/";   // TRIODION FILE
	//private final static String pentecostarionFileName = "Ponomar/xml/pentecostarion/"; // PENTECOSTARION FILE
	private String filename;
	private int lineNumber;
	private LanguagePack Text=new LanguagePack();
	private String[] PrimesNames=Text.obtainValues((String)Text.Phrases.get("RoyalHours"));
	private String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	//private String LentenK;				//ANY REQUIRED KATHISMA REFERENCED USING "LENTENK = "17"" WOULD BE THE 17th KATHISMA.
	private JFrame frames;
	private String[] FileNames=Text.obtainValues((String)Text.Phrases.get("File"));
	private String[] HelpNames=Text.obtainValues((String)Text.Phrases.get("Help"));
	String newline = "\n";
	private String strOut;
	private JDate today;
	private Helpers helper;
	//private PrimeSelector SelectorP=new PrimeSelector();
	private PrintableTextPane output;
        private String DisplayFont =new String(); //ALLOWS A CUSTOM FONT AND SIZE TO BE SPECIFIED FOR A GIVEN BIBLE READING: REQUIRED FOR OLD CHURCH SLAVONIC AT PRESENT
	private String DisplaySize="12";  //UNTIL A COMPLETE UNICODE FONT IS AVAILIBLE.
	private Font DefaultFont=new Font("",Font.BOLD,12);		//CREATE THE DEFAULT FONT
	private Font CurrentFont=DefaultFont;


	public RoyalHours(JDate date)
	{
		today=date;
		helper=new Helpers();
                StringOp.dayInfo.put("PS",1);

		try
		{
			String strOut=createHours();
			if(strOut.equals("Royal Hours are not served today."))
			{
				Object[] options = {LanguageNames[3]};
				JOptionPane.showOptionDialog(null, PrimesNames[0],(String)Text.Phrases.get("0") + (String)Text.Phrases.get("Colon")+ PrimesNames[1], JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			}
			else
			{
				//strOut=strOut+"<p><Font Color='red'>Disclaimer: This is a preliminary attempt at creating the Primes service.</Font></p>";
				int LangCode=Integer.parseInt(StringOp.dayInfo.get("LS").toString());
                                if (LangCode==2 || LangCode==3 ){
                                    //strOut="<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><p><font face=\"Hirmos Ponomar\" size=\"5\">"+strOut+"</font></p>";
                                    //System.out.println("Added Font");
                                   }

                               RoyalHoursWindow(strOut);
			}
		}
		catch (IOException j)
		{
		}

	}

	private void RoyalHoursWindow(String textOut)
	{
		frames=new JFrame((String)Text.Phrases.get("0") + (String)Text.Phrases.get("Colon")+ PrimesNames[1]);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		textOut=textOut.replaceAll("</br>", "<BR>");
		textOut=textOut.replaceAll("<br>","<BR>");
		strOut=textOut;
		//System.out.println(textOut);
		JPanel contentPane=new JPanel(new BorderLayout());
		contentPane.setOpaque(true);
		output=new PrintableTextPane();
		output.setEditable(false);
		output.setSize(800,700);
		output.setContentType("text/html; charset=UTF-8");
		output.setText(textOut);
		output.setCaretPosition(0);
                JScrollPane scrollPane = new JScrollPane(output);
		JMenuBar MenuBar=new JMenuBar();
		MenuFiles demo=new MenuFiles();
		//PrimeSelector trial=new PrimeSelector();
		MenuBar.add(demo.createFileMenu(this));
		//MenuBar.add(trial.createPrimeMenu());
		MenuBar.add(demo.createHelpMenu(this));
		frames.setJMenuBar(MenuBar);
		//trial.addPropertyChangeListener(this);

		contentPane.add(scrollPane,BorderLayout.CENTER);
		frames.setContentPane(contentPane);
		frames.pack();
		frames.setSize(800,700);
		frames.setVisible(true);



		//scrollPane.top();
	}
	private String createHours() throws IOException
	{
		//OBTAIN THE DEFAULTS FOR THE SERVICE (WHAT WAS LAST USED!)
		//StringOp.dayInfo.put("PS",SelectorP.getWhoValue());
                //MUST ADD APPROPRIATE SELECTOR OF TYPE OF SERVICE
		//int TypeP=SelectorP.getTypeValue();
                
		Service ReadHours=new Service();

		int Eday=Integer.parseInt(StringOp.dayInfo.get("nday").toString());
                int day=Integer.parseInt(StringOp.dayInfo.get("doy").toString());
                int dow=Integer.parseInt(StringOp.dayInfo.get("dow").toString());
                

                if (!((Eday == -2) || (day == 4 && (dow != 6 && dow != 0)) || (day == 2 && dow == 5) || (day == 3 && dow == 5) || (day == 357 && ((dow != 6) && dow != 0)) || (day == 356 && dow == 5) || (day == 355 && dow == 5))){
                    return "Royal Hours are not served today.";
                }
		//BASED ON THE DATE DETERMINE THE CORRECT FLAGS
                StringOp.dayInfo.put("PFlag",0); //FOR EVE OF NATIVITY!
                if ((Eday == -2)){
                    StringOp.dayInfo.put("PFlag",2); //FOR GOOD FRIDAY

                }
                if ((day == 4 && (dow != 6 && dow != 0)) || (day == 2 && dow == 5) || (day == 3 && dow == 5)){
                    StringOp.dayInfo.put("PFlag",1);
                }


		String strOut= new String();
		//IT IS TO BE DECIDED WHETHER IT IS DESIRED TO SET THE TROPARIA PROPERLY!

		strOut=ReadHours.startService(ServicesFileName + "RoyalHours.xml")+"</p>";

                

	     return strOut;
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
		if (table.get("Cmd") != null)
		{
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE

			if (StringOp.evalbool(table.get("Cmd").toString()) == false)
			{

				return;
			}
		}
		if(elem.equals("LANGUAGE") || elem.equals("TONE"))
		{
			read=true;
		}
		if(elem.equals("TEXT") && read)
		{
			text+=(String)table.get("Value");

		}
		if (elem.equals("PRIMES") && read)
		{
			

		}
		//OTHER LITURGICAL SERVICES WOULD FOLLOW HERE

	}

	public void endElement(String elem)
	{
		if(elem.equals("LANGUAGE") || elem.equals("TONE"))
		{
			read=false;
		}
	}

	public void text(String text)
	{

	}

	private boolean eval(String expression) throws IllegalArgumentException
	{
		return false;
	}

	
	public String readText(String filename)
	{
		try
		{
       			 text= new String();
       			 BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
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

	 public void actionPerformed(ActionEvent e)
  {
        JMenuItem source = (JMenuItem)(e.getSource());
        String name = source.getText();
       if (name.equals(HelpNames[2]))
        {
        	 new About();
        }
         if (name.equals(HelpNames[0]))
        {
        	 //LAUNCH THE HELP FILE

        }
        if(name.equals(FileNames[1]))
        {
        	//SAVE THE CURRENT WINDOW
        	helper.SaveHTMLFile(PrimesNames[1]+" "+today, strOut);

       	}
        if(name.equals(FileNames[4]))
        {
        	//CLOSE THE HOURS FRAME
        	if(helper.closeFrame(LanguageNames[7]))
        	{
        		frames.dispose();
        	}
        }
        if(name.equals(FileNames[6]))
        {
        	//PRINT THE FILE
        	helper.sendHTMLToPrinter(output);
        }
        String s = "Action event detected."
                   + newline
                   + "    Event source: " + source.getText()
                   + " (an instance of " + getClassName(source) + ")";
        System.out.println(s);
        //output.append(s + newline);
        //output.setCaretPosition(output.getDocument().getLength());
    }

    protected String getClassName(Object o)
    {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");
        return classString.substring(dotIndex+1);
    }

    public void itemStateChanged(ItemEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Item event detected."
                   + newline
                   + "    Event source: " + source.getText()
                   + " (an instance of " + getClassName(source) + ")"
                   + newline
                   + "    New state: "
                   + ((e.getStateChange() == ItemEvent.SELECTED) ?
                     "selected":"unselected");
        System.out.println(s);
        //output.append(s + newline);
        //output.setCaretPosition(output.getDocument().getLength());
    }

	public void propertyChange(PropertyChangeEvent e)
	{
		//THERE IS NOTHING HERE TO DO??
		try
		{
			output.setText(createHours());
			output.setCaretPosition(0);
		}
		catch (Exception e1)
		{

		}

	}
        public static void main(String[] argz)
	{
		//DEBUG MODE
		System.out.println("RoyalHours.java running in Debug mode");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY!!");
                
                StringOp.dayInfo = new Hashtable();

                StringOp.dayInfo.put("dow",3);
                StringOp.dayInfo.put("doy",357);
                StringOp.dayInfo.put("nday",-256);
                StringOp.dayInfo.put("LS",0); //ENGLISH
                StringOp.dayInfo.put("PS",1);

                JDate todays=new JDate(12,24,2009);

		new RoyalHours(todays);
	}


}

