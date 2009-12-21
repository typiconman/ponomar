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
	
	
	public LanguageSelector()
	{
		String Default = (String) ConfigurationFiles.Defaults.get("Language");
		String rough = (String) ConfigurationFiles.Defaults.get("AvailableLanguages");
		AvailableLanguages=rough.split(",");
		LanguageLocation=Default;
		LastLanguage=Default;
		
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
		
		ButtonGroup group = new ButtonGroup();
		
		for(int i=0;i<AvailableLanguages.length;i++)
		{
			LanguageBox=new JRadioButtonMenuItem((String) AvailableLanguages[i]);
			LanguageBox.addActionListener(this);	
			if (i == DefaultLocation)
			{
				LanguageBox.setSelected(true);
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
		if(!LanguageLocation.equals(LastLanguage))
		{
			
			DefaultLocation=findString(AvailableLanguages,LanguageLocation);
			firePropertyChange("Language", (String) LanguageLocation, (String) LastLanguage); //THIS WILL ONLY CAUSE A CHANGE IN THE DISPLAY OF DATA LANGUAGE, BUT NOT THE INTERFACE LANGUAGE. THE PROGRAMME NEEDS TO BE RESTARTED FOR THIS TO OCCUR.
			//A MESSAGE BOX SHOULD ALSO BE DISPLAYED!
			Object[] options = {LanguageNames[3]};
			JOptionPane.showOptionDialog(null, LanguageNames[4],(String)Text.Phrases.get("0"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			//JOptionPane.showMessageDialog(null, "In order for the interface language to change, please restart the programme.","Ponomar");
			LastLanguage=LanguageLocation;
			ConfigurationFiles.Defaults.put("Language",LastLanguage);
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
	protected int getLValue()
	{
		return DefaultLocation;
	}

}