package net.ponomar.services;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import java.util.LinkedHashMap;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.ponomar.About;
import net.ponomar.MenuFiles;
import net.ponomar.calendar.JDate;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.panels.PrimeSelector;
import net.ponomar.panels.PrintableTextPane;
import net.ponomar.parsing.utility.DocHandler;
import net.ponomar.parsing.utility.QDParser;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 
import net.ponomar.utility.StringOp;

/***********************************************************************
 * THIS MODULE COLLECT THE COMMON CODE OF THE SERVICE CLASSES.
 ***********************************************************************/
public abstract class LitService implements DocHandler, ActionListener, ItemListener, PropertyChangeListener {

	protected static final String OCTOECHEOS_FILENAME = Constants.SERVICES_PATH + "Octoecheos/";
	protected static final String TROPARION_OUTPUT_START = "<SERVICES>\r\n<LANGUAGE>\r\n<CREATE Who=\"\" What=\"TROPARION/";
	protected static final String TROPARION_OUTPUT_END = "\" Header=\"1\" RedFirst=\"1\" NewLine=\"1\"/>\r\n</LANGUAGE>\r\n</SERVICES>";
	protected static final String LENTENK = "LENTENK";
	protected static final String NEWLINE = "\n";
	protected static LinkedHashMap primesTK;
	protected static String text;
	protected static boolean read=false;
	protected static String type;
	protected String filename;
	protected int lineNumber;
	protected LanguagePack langText;//=new LanguagePack();
	protected String[] primesNames;
	protected String[] languageNames;//=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	protected JFrame frames;
	protected String[] fileNames;//=Text.obtainValues((String)Text.Phrases.get("File"));
	protected String[] helpNames;//=Text.obtainValues((String)Text.Phrases.get("Help"));
	protected String strOut;
	protected JDate today;
	protected Helpers helper;
	protected PrintableTextPane output;
	protected String displayFont = ""; //ALLOWS A CUSTOM FONT AND SIZE TO BE SPECIFIED FOR A GIVEN BIBLE READING: REQUIRED FOR OLD CHURCH SLAVONIC AT PRESENT
	protected String displaySize="12";  //UNTIL A COMPLETE UNICODE FONT IS AVAILIBLE.
	protected Font defaultFont=new Font("",Font.BOLD,12);		//CREATE THE DEFAULT FONT
	protected Font currentFont=defaultFont;
	protected StringOp analyse=new StringOp();
	protected String troparion1;
	protected String troparion2;
	protected String kontakion1;
	protected String kontakion2;
	protected String lentenKat;	//ANY REQUIRED KATHISMA REFERENCED USING "LENTENK = "17"" WOULD BE THE 17th KATHISMA.

	public LitService() {
		super();
	}
	
    public void propertyChange(PropertyChangeEvent e) {
        //THERE IS NOTHING HERE TO DO??
        try {
            output.setText(createHours());
            output.setCaretPosition(0);
        } catch (Exception e1) {
        }

    }
    
    protected String getClassName(Object o)
    {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf('.');
        return classString.substring(dotIndex+1);
    }
    
    public void actionPerformed(ActionEvent e)
    {
          JMenuItem source = (JMenuItem)(e.getSource());
          String name = source.getText();
         if (name.equals(helpNames[2]))
          {
          	 Helpers orient=new Helpers(analyse.getDayInfo());
                  orient.applyOrientation(new About(analyse.getDayInfo()),(ComponentOrientation)analyse.getDayInfo().get(Constants.ORIENT));
          }
           if (name.equals(helpNames[0]))
          {
          	 //LAUNCH THE HELP FILE
          	 
          }
          if(name.equals(fileNames[1]))
          {
          	//SAVE THE CURRENT WINDOW
          	helper.saveHTMLFile(primesNames[1]+" "+today, strOut);
          	       	
         	}
          if(name.equals(fileNames[4]))
          {
          	//CLOSE THE HOURS FRAME
          	if(helper.closeFrame(languageNames[7]))
          	{
          		frames.dispose();
          	}
          }
          if(name.equals(fileNames[6]))
          {
          	//PRINT THE FILE
          	helper.sendHTMLToPrinter(output);
          }
          String s = "Action event detected."
                     + NEWLINE
                     + "    Event source: " + source.getText()
                     + " (an instance of " + getClassName(source) + ")";
          System.out.println(s);
          //output.append(s + newline);
          //output.setCaretPosition(output.getDocument().getLength());
      }
    
	protected void serviceWindow(String textOut)
	{
		frames=new JFrame(langText.getPhrases().get("0") + langText.getPhrases().get(Constants.COLON) + primesNames[1]);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		textOut=textOut.replace("</br>", "<BR>");
		textOut=textOut.replace("<br>","<BR>");
		strOut=textOut;
		//System.out.println(textOut);
		JPanel contentPane=new JPanel(new BorderLayout());
		contentPane.setOpaque(true);
		output=new PrintableTextPane();
		output.setEditable(false);
		output.setSize(800,700);
		output.setContentType(Constants.CONTENT_TYPE);
		output.setText(textOut);
		output.setCaretPosition(0);
                JScrollPane scrollPane = new JScrollPane(output);
		constructMenu(frames);
		
		contentPane.add(scrollPane,BorderLayout.CENTER);
		frames.setContentPane(contentPane);
		frames.pack();
		frames.setSize(800,700);
		frames.setVisible(true);
                
                Helpers orient=new Helpers(analyse.getDayInfo());
                orient.applyOrientation(frames,(ComponentOrientation)analyse.getDayInfo().get(Constants.ORIENT));

		//scrollPane.top();
	}

	protected void constructMenu(JFrame frame) {
		JMenuBar menuBar=new JMenuBar();
		MenuFiles demo=new MenuFiles(analyse.getDayInfo());
		PrimeSelector trial=new PrimeSelector(analyse.getDayInfo());
		menuBar.add(demo.createFileMenu(this));
		menuBar.add(trial.createPrimeMenu());
		menuBar.add(demo.createHelpMenu(this));
		frame.setJMenuBar(menuBar);
		trial.addPropertyChangeListener(this);
	}

    protected abstract String createHours() throws IOException;
    
    public abstract void startElement(String elem, HashMap<String, String> table);
    
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

	protected boolean eval(String expression) throws Throwable
	{
		return false;
	}
	
	public String readText(String filename)
	{
		try
		{
       			 text= "";
       			 BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(helper.langFileFind(analyse.getDayInfo().get("LS").toString(),filename)), StandardCharsets.UTF_8));
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
    
	public void startDocument()
	{

	}

	public void endDocument()
	{

	}
	
    public void itemStateChanged(ItemEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Item event detected."
                   + NEWLINE
                   + "    Event source: " + source.getText()
                   + " (an instance of " + getClassName(source) + ")"
                   + NEWLINE
                   + "    New state: "
                   + ((e.getStateChange() == ItemEvent.SELECTED) ?
                     "selected":"unselected");
        System.out.println(s);
        //output.append(s + newline);
        //output.setCaretPosition(output.getDocument().getLength());
    }

}