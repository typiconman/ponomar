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
	//private LanguagePack Text=new LanguagePack();
	//private String[] PrimesNames=Text.obtainValues((String)Text.Phrases.get("Primes"));
	
	
	public boolean closeFrame(String title)
	{
            LanguagePack Text=new LanguagePack();
            String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));

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
            LanguagePack Text=new LanguagePack();
            String[] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
	 String[] HelperNames=Text.obtainValues((String)Text.Phrases.get("Helpers"));
	 String[] AboutNames=Text.obtainValues((String)Text.Phrases.get("About"));
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
	    				out.write(strOut + "<body><BR><BR><i>"+getCopyright()+"</i></body></html>");
                                        if (strOut.substring(0, 4).equals("<html>"))
                                        {
                                            out.write("</html>");
                                        }
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
        public String langFileFind(String LanguagePath, String BasePath)
        {
            
            String addon="Ponomar/languages/";
            String currentPath=LanguagePath;
            File testFile=new File(addon+LanguagePath+BasePath);
            //System.out.println(currentPath);
            while (!testFile.exists())
            {
                if (currentPath.length()<=1)
                {
                    currentPath="";
                    break;
                }
                String shorter=currentPath.substring(0,currentPath.length()-2);
                int location=shorter.lastIndexOf("/");
                if (location==-1)
                {
                    //No localised files found use top level domain file.
                    currentPath="";
                    break;
                }
                currentPath=currentPath.substring(0,location)+"/";
                testFile=new File(addon+currentPath+BasePath);
                //System.out.println(currentPath);
            }


            return addon+currentPath+BasePath;
        }
        public String getCopyright(){

            LanguagePack Text=new LanguagePack();
		//String [] AboutNames=Text.obtainValues((String)Text.Phrases.get("About"));
		String[] Authors=Text.obtainValues((String) Text.Phrases.get("Authors"));
                String Year=Text.Phrases.get("Year").toString();
                String Comma=Text.Phrases.get("Comma").toString();
                String And=Text.Phrases.get("And").toString();
                String AuthorList=Authors[0];
                if (Authors.length>2)
                {
                for(int i=1;i < Authors.length-1;i++)
                {
                    AuthorList=AuthorList+Authors[i]+Comma;
                }
                }
                if (Authors.length>1)
                {
                    AuthorList=AuthorList+And+Authors[Authors.length-1];
                }

               String Copyright=Text.Phrases.get("Copyright").toString();
               Copyright=Copyright.replace("^YY",Year);
               Copyright=Copyright.replace("^AA",AuthorList);
               return Copyright;
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
