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
import javax.print.*;
import javax.print.attribute.*;
import java.awt.print.Printable;

class Helpers
{
	private LanguagePack Text=new LanguagePack();
	private String[] PrimesNames=Text.obtainValues((String)Text.Phrases.get("Primes"));
	private String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	private String[] HelperNames=Text.obtainValues((String)Text.Phrases.get("Helpers"));
	private String[] AboutNames=Text.obtainValues((String)Text.Phrases.get("About"));
	
	public boolean closeFrame(String title)
	{
		Object[] options = {LanguageNames[3],LanguageNames[5]};
		//JOptionPane pane=new JOptionPane();
		Object selectedValue=JOptionPane.showOptionDialog(null, title ,(String)Text.Phrases.get("0"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		//Object selectedValue = pane.getValue();
     		//System.out.println(selectedValue);
     		if(selectedValue == null)
      		{	 //I WILL TREAT THIS AS NO
      			 return false;
    			 //If there is an array of option buttons:
    		}
    		 if(options[1].equals(selectedValue))
    		{
    			 	return false;
    		}
    		if(selectedValue instanceof Integer)
    		{
    			if(((Integer)selectedValue).intValue()!=0)
    			{
    				return false;
    			}
    		}
    		return true;
	}
	public void SaveHTMLFile(String defaultname, String strOut)
	{
		JFileChooser fileSelector=new JFileChooser();
        	File fileSelected = new File(defaultname);
        	fileSelector.setDialogTitle(HelperNames[0]);
		fileSelector.setSelectedFile(fileSelected);
        	fileSelector.setFileFilter(new JavaFileFilter());
        	int result=fileSelector.showSaveDialog(null);
        	
        	if(result == JFileChooser.APPROVE_OPTION)
        	{
        		File FileName=fileSelector.getSelectedFile();
        		if(!FileName.getName().endsWith(".html"))
        		{
        			FileName=new File(FileName.getPath()+".html");
        		}
        		if(FileName.exists())
        		{
        			//CHECK WHETHER IT IS DESIRED TO OVERWRITE THE FILE
        			Object[] options = {LanguageNames[3],LanguageNames[5]};
				JOptionPane pane=new JOptionPane();
				pane.showOptionDialog(null, LanguageNames[6] + "\n "+FileName.getPath(),(String)Text.Phrases.get("0"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				Object selectedValue = pane.getValue();
     				if(selectedValue == null)
      					 //I WILL TREAT THIS AS NO
      					 return;
    				 //If there is an array of option buttons:
    				 if(options[1].equals(selectedValue))
    				 	return;
    				 if(selectedValue instanceof Integer)
    				{
    					if(((Integer)selectedValue).intValue()!=0)
    					{
    						return;
    					}
    				}
    			}
           		
        		
        			//CREATE THE LOCATION AND WRITE THE FILE
        			try
        			{
        				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FileName),"UTF8"));	
	    				out.write(strOut + "<BR><BR><i>"+AboutNames[3]+" " +(String) ConfigurationFiles.Defaults.get("Year") +" " +AboutNames[4] + " " +(String) ConfigurationFiles.Defaults.get("Authors")+"</i>");
	   			 	out.close();
	   			 }
	   			 catch(Exception e1)
	   			 {
	   			 	System.out.println(FileName);
	   			 	e1.printStackTrace();
	   			 }
   			
        		
        		
        	}
        	return;
	
	}
	
	public void sendHTMLToPrinter(Printable obj)
	{
		//html += "<BR><BR><i>" + AboutNames[3]+ " "+ (String) ConfigurationFiles.Defaults.get("Year") + " " +AboutNames[4] + " " +(String) ConfigurationFiles.Defaults.get("Authors")+"</i>";

		DocFlavor flavor = new DocFlavor("application/x-java-jvm-local-objectref", "java.awt.print.Printable");
		PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, null);
		PrintService svc 	= PrintServiceLookup.lookupDefaultPrintService();
		PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
		if (services.length == 0)
		{
			System.out.println("Sorry, but you don't have any printers installed.");
			return;
		}
		PrintService service    = ServiceUI.printDialog(null, 50, 50, services, svc, null, attributes);
			
		if (service != null)
		{
			DocPrintJob job = service.createPrintJob();
			SimpleDoc   doc = new SimpleDoc(obj, flavor, null);
			try
			{
				job.print(doc, attributes);
			}
			catch (PrintException e)
			{
				System.out.println("Failed to print!");
				e.printStackTrace();
			}
		}
	}
	
}

class JavaFileFilter extends FileFilter
{
	public boolean accept(File file)
	{
		if(file.getName().endsWith(".html"))  return true;
		if(file.isDirectory()) return true;
		return false;
	}
	
	public String getDescription()
	{
		return "HTML Files (.html)";
	}
}
