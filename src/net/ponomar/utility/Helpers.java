package net.ponomar.utility;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.filechooser.FileFilter;

import net.ponomar.internationalization.LanguagePack;

import javax.print.*;
import javax.print.attribute.*;
import java.awt.print.Printable;

public class Helpers
{
	//private LanguagePack Text=new LanguagePack();
	//private String[] PrimesNames=Text.obtainValues((String)Text.Phrases.get("Primes"));
	private StringOp analyse=new StringOp();

        public Helpers(OrderedHashtable dayInfo){
            analyse.setDayInfo(dayInfo);
        }
	
	public boolean closeFrame(String title)
	{
            LanguagePack text=new LanguagePack(analyse.getDayInfo());
            String[] languageNames=text.obtainValues((String)text.getPhrases().get("LanguageMenu"));

            Object[] options = {languageNames[3],languageNames[5]};
		//JOptionPane pane=new JOptionPane();
		Object selectedValue=JOptionPane.showOptionDialog(null, title ,(String)text.getPhrases().get("0"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
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
	public void saveHTMLFile(String defaultname, String strOut)
	{
            LanguagePack text=new LanguagePack(analyse.getDayInfo());
            String[] languageNames=text.obtainValues((String)text.getPhrases().get("LanguageMenu"));
	 String[] helperNames=text.obtainValues((String)text.getPhrases().get("Helpers"));
	 String[] aboutNames=text.obtainValues((String)text.getPhrases().get("About"));
            JFileChooser fileSelector=new JFileChooser();
        	File fileSelected = new File(defaultname);
        	fileSelector.setDialogTitle(helperNames[0]);
		fileSelector.setSelectedFile(fileSelected);
        	fileSelector.setFileFilter(new JavaFileFilter());
        	int result=fileSelector.showSaveDialog(null);
        	
        	if(result == JFileChooser.APPROVE_OPTION)
        	{
        		File fileName=fileSelector.getSelectedFile();
        		if(!fileName.getName().endsWith(".html"))
        		{
        			fileName=new File(fileName.getPath()+".html");
        		}
        		if(fileName.exists())
        		{
        			//CHECK WHETHER IT IS DESIRED TO OVERWRITE THE FILE
        			Object[] options = {languageNames[3],languageNames[5]};
				JOptionPane pane=new JOptionPane();
				pane.showOptionDialog(null, languageNames[6] + "\n "+fileName.getPath(),(String)text.getPhrases().get("0"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
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
        				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8));	
	    				out.write(strOut + "<body><BR><BR><i>"+getCopyright()+"</i></body></html>");
                                        if (strOut.substring(0, 4).equals("<html>"))
                                        {
                                            out.write("</html>");
                                        }
                                        out.close();
	   			 }
	   			 catch(Exception e1)
	   			 {
	   			 	System.out.println(fileName);
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
        public void applyOrientation(Component c, ComponentOrientation o)
        {
            //Based on the programme written in the book Java Internationalisation.
            c.setComponentOrientation(o);
            if (c instanceof JMenu)
            {
                JMenu menu = (JMenu)c;
                int ncomponents = menu.getMenuComponentCount();
                for (int i=0; i<ncomponents;i++)
                {
                    applyOrientation(menu.getMenuComponent(i),o);
                }
            }else if (c instanceof Container)
            {
                Container container=(Container)c;
                int ncomponents=container.getComponentCount();
                for (int i=0; i<ncomponents; i++)
                {
                    applyOrientation(container.getComponent(i),o);
                }
            }            
        }
        public String langFileFind(String languagePath, String basePath)
        {
            
            String addon="src/languages/";
            String currentPath=languagePath;
            /*if(LanguagePath==null){
                return "ERROR";
            }*/
            File testFile=new File(addon+languagePath+basePath);
            
            //System.out.println(currentPath);
            while (!testFile.exists())
            {
                if (currentPath.length()<=1)
                {
                    currentPath="";
                    break;
                }
                String shorter=currentPath.substring(0,currentPath.length()-2);
                int location=shorter.lastIndexOf('/');
                if (location==-1)
                {
                    //No localised files found use top level domain file.
                    currentPath="";
                    break;
                }
                currentPath=currentPath.substring(0,location)+"/";
                testFile=new File(addon+currentPath+basePath);
                //System.out.println(currentPath);
            }


            return addon+currentPath+basePath;
        }
        public String getCopyright(){

            LanguagePack text=new LanguagePack(analyse.getDayInfo());
		//String [] AboutNames=Text.obtainValues((String)Text.Phrases.get("About"));
		String[] authors=text.obtainValues((String) text.getPhrases().get("Authors"));
                String year=text.getPhrases().get("Year").toString();
                String comma=text.getPhrases().get("Comma").toString();
                String and=text.getPhrases().get("And").toString();
                String authorList=authors[0];
                if (authors.length>2)
                {
                for(int i=1;i < authors.length-1;i++)
                {
                    authorList=authorList+authors[i]+comma;
                }
                }
                if (authors.length>1)
                {
                    authorList=authorList+and+authors[authors.length-1];
                }

               String copyright=text.getPhrases().get("Copyright").toString();
               copyright=copyright.replace("^YY",year);
               copyright=copyright.replace("^AA",authorList);
               return copyright;
        }
        public Hashtable deepCopy(Hashtable original){
            //Currently does not work.
            Hashtable copy = new Hashtable();
            for (Enumeration e = original.keys(); e.hasMoreElements(); )
		{
			String type = e.nextElement().toString();
                        String vect = original.get(type).toString();

			copy.put(type,original.get(type).toString());
		}
            return copy;
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
