package Ponomar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/***************************************************************
LanguageSelector.java :: MODULE THAT ALLOWS THE USER TO SELECT, USING A COMBOBOX,
WHICH LANGUAGE THE PROGRAMME WILL RUN IN.

LanguageSelector.java is part of the Ponomar project.
Copyright 2008 Yuri Shardt
version 1.0: August 2008
yuri (dot) shardt (at) gmail.com

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

class LanguageSelector extends JMenu implements ActionListener, PropertyChangeListener
{
	private String LanguageLocation;			//DETERMINES THE LOCATION OF THE READINGS
	private JMenu menuPanel;
	private JRadioButtonMenuItem LanguageBox;
	private String LastLanguage;			//AVOID REPEATING IF THERE IS NO CHANGE IN THE SELECTION
	private String[] AvailableLanguages;
	private int DefaultLocation;
        private String Default;
        private String[] NameLanguages;
	

	
	public LanguageSelector()
	{
	
                Default = (String) ConfigurationFiles.Defaults.get("Language");
		String rough = (String) ConfigurationFiles.Defaults.get("AvailableLanguages");
		AvailableLanguages=rough.split(",");
		LanguageLocation=Default;
		LastLanguage=Default;
		NameLanguages=AvailableLanguages;

		DefaultLocation = 0;
		
		for(int i=0; i<AvailableLanguages.length;i++)
		{
			if(AvailableLanguages[i].equals(Default))
			{
				DefaultLocation=i;
				break;
			}
		}
	
	}
	public JMenu createLanguageMenu()
	{
            Font CurrentFont=new Font((String)StringOp.dayInfo.get("FontFaceM"),Font.PLAIN,Integer.parseInt((String)StringOp.dayInfo.get("FontSizeM")));
            LanguagePack Text=new LanguagePack();
		String [] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
		//DETERMINE THE DEFAULTS
		String Default = (String) ConfigurationFiles.Defaults.get("Language");
		String rough = (String) ConfigurationFiles.Defaults.get("AvailableLanguages");
		AvailableLanguages=rough.split(",");
		LanguageLocation=Default;
		LastLanguage=Default;
		
		//CREATE THE COMBOBOX
		menuPanel=new JMenu(LanguageNames[0]);
		menuPanel.setToolTipText(LanguageNames[1]);
		menuPanel.setMnemonic(KeyEvent.VK_L);
		menuPanel.getAccessibleContext().setAccessibleDescription(LanguageNames[2]);
		//menuPanel.setFont(CurrentFont);
		ButtonGroup group = new ButtonGroup();
		
		for(int i=0;i<AvailableLanguages.length;i++)
		{
                    //System.out.println(AvailableLanguages[i]);
                    Helpers getFile=new Helpers();
                    LanguagePack lang=new LanguagePack(getFile.langFileFind(AvailableLanguages[i],"xml/Commands/LanguagePacks.xml"));
                    NameLanguages[i]= lang.Phrases.get("NameLocal").toString();
                    LanguageBox=new JRadioButtonMenuItem((String) lang.Phrases.get("NameLocal").toString());
			LanguageBox.addActionListener(this);	
			if (i == DefaultLocation)
			{
				LanguageBox.setSelected(true);
                                LastLanguage=NameLanguages[i];
			}

                        if (AvailableLanguages[i].substring(0,2).equals("zh"))
                        {
                            //We are going to treat Chinese specially
                            String FontName="SimSun";

                            if (AvailableLanguages[i].substring(3,7).equals("Hant"))
                            {
                                FontName="MingLiU";
                            }
                            Font ChineseFont=new Font(FontName,Font.PLAIN,20);
                            LanguageBox.setFont(ChineseFont);

                        }
                        else if (AvailableLanguages[i].equals("cu/"))
                        {
                        
                            //We are going to treat Church Slavonic specially
                            Font ChineseFont=new Font("Hirmos Ponomar",Font.PLAIN,18);
                            LanguageBox.setFont(ChineseFont);
                            //LanguageBox.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                            //LanguageBox.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                        
                        }
                        else
                        {
                            //We do not want the default font necessarily being used here.
                            //if (CurrentFont.getFontName().toString().equals("Hirmos Ponomar"))
                                //This is only an issue for fonts that lack a complete character set.
                            {
                            Font DefaultFont=new Font("Times New Roman",Font.BOLD,14);
                            LanguageBox.setFont(DefaultFont);
                            }
                        }
                        group.add(LanguageBox);
			menuPanel.add(LanguageBox);
		}
		
		return menuPanel;	   	      	
        	       	       	
        }
	
	public void actionPerformed(ActionEvent e)
	{
		LanguagePack Text=new LanguagePack();
		String [] LanguageNames=Text.obtainValues((String)Text.Phrases.get("LanguageMenu"));
		//THIS WILL DETERMINE THE APPROPRIATE LANGUAGE LOCATION
		LanguageLocation=e.getActionCommand().toString();
                //System.out.println(LanguageLocation);
                int NewLocation=-1;
                for(int i =0;i < NameLanguages.length;i++)
                {
                    if (NameLanguages[i].equals(LanguageLocation))
                    {
                        NewLocation = i;
                        break;
                    }
                }
                //System.out.println("LL: "+LanguageLocation);
                //System.out.println("Location: "+DefaultLocation);
                //String rough = (String) ConfigurationFiles.Defaults.get("AvailableLanguages");
		//AvailableLanguages=rough.split(",");

		if(!LanguageLocation.equals(LastLanguage)) //I might need to do something here!!!
		{
			
			DefaultLocation=findString(AvailableLanguages,LanguageLocation);
			firePropertyChange("Language", (String) LanguageLocation, (String) LastLanguage); //THIS WILL ONLY CAUSE A CHANGE IN THE DISPLAY OF DATA LANGUAGE, BUT NOT THE INTERFACE LANGUAGE. THE PROGRAMME NEEDS TO BE RESTARTED FOR THIS TO OCCUR.
			//A MESSAGE BOX SHOULD ALSO BE DISPLAYED!
			Object[] options = {LanguageNames[3]};
			JOptionPane.showOptionDialog(null, LanguageNames[4],(String)Text.Phrases.get("0"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			//JOptionPane.showMessageDialog(null, "In order for the interface language to change, please restart the programme.","Ponomar");
			LastLanguage=NameLanguages[NewLocation];
			ConfigurationFiles.Defaults.put("Language",AvailableLanguages[NewLocation]);
			ConfigurationFiles.WriteFile();
		}
		
	}
	
	public int findString(String[] w,String compare)
	{
		//FINDS compare IN THE STRING ARRAY w.
		int location=-1;
		for(int i=0;i<w.length;i++)
		{
			if(w[i].equals(compare))
			{
				location=i;
				break;
			}
		}
		return location;
		
	}
	public void propertyChange(PropertyChangeEvent e)
	{
		//THERE IS NOTHING HERE TO DO??
	}
	protected String getLValue()
	{
		return Default; //DefaultLocation;
	}        

}