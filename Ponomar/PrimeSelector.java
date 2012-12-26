package Ponomar;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;

/***************************************************************
GospelSelector.java :: MODULE THAT ALLOWS THE USER TO SELECT, USING RADIO BUTTONS,
WHICH TYPE OF REPEATS ARE BEING USED FOR THE GOSPEL READINGS
CURRENTLY BOTH THE LUCAN JUMP AND JORDANVILLE IMPLEMENTATIONS ARE AVAILABLE.

GospelSelector.java is part of the Ponomar project.
Copyright 2008 Yuri Shardt

version 1.0: August 2008
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

class PrimeSelector extends JPanel implements ActionListener, PropertyChangeListener
{
	private static String a = (String) ConfigurationFiles.Defaults.get("Primes");
	private static String[] Default=a.split(",");
	private static String ReadingLocation=Default[0];			//DETERMINES THE LOCATION OF THE READINGS
	private JPanel radioPanel;
	private JRadioButton LucanButton;
	private JRadioButton JordanvilleButton;
	private static String LastLocation=Default[0];			//AVOID REPEATING IF THERE IS NO CHANGE IN THE SELECTION
	private JMenu submenu;
	private JRadioButtonMenuItem rbMenu1Item, rbMenu2Item, rbMenu4Item, rbMenu5Item, rbMenu6Item;
	private LanguagePack Text;//=new LanguagePack();
	private String[] SelectorNames;//=Text.obtainValues((String)Text.Phrases.get("PrimeSelection"));
	private static String LastLocation2=Default[1];
	private static String ReadingLocation2=Default[1];
        private StringOp Analyse=new StringOp();
	
	public PrimeSelector(OrderedHashtable dayInfo)
	{
            Analyse.dayInfo=dayInfo;
            Text=new LanguagePack(dayInfo);
            SelectorNames=Text.obtainValues((String)Text.Phrases.get("PrimeSelection"));
	}
		
	public JMenu createPrimeMenu()
	{
		
		
		//GospelSelector sample=new GospelSelector();
		//CREATE THE DEFAULT MENU
		a = (String) ConfigurationFiles.Defaults.get("Primes");
		String[] Default=a.split(",");
	
		JMenu menu=new JMenu(SelectorNames[7]);
		menu.setMnemonic(KeyEvent.VK_T);
		menu.getAccessibleContext().setAccessibleDescription(SelectorNames[7]);
				
		//DETERMINE THE DEFAULTS
				
		ButtonGroup group = new ButtonGroup();
		rbMenu1Item=new JRadioButtonMenuItem(SelectorNames[0]);
		rbMenu1Item.addActionListener(this);
		rbMenu1Item.setMnemonic(KeyEvent.VK_R);
		rbMenu1Item.setActionCommand("Reader");
		rbMenu1Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		group.add(rbMenu1Item);

		rbMenu2Item = new JRadioButtonMenuItem(SelectorNames[1]);
		rbMenu2Item.setMnemonic(KeyEvent.VK_T);
		rbMenu2Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		rbMenu2Item.addActionListener(this);
		rbMenu2Item.setActionCommand("Priest");
		group.add(rbMenu2Item);
		
		if(Default[0].equals("Reader"))
        	{
        		rbMenu1Item.setSelected(true);
        		ReadingLocation="Reader";
        		LastLocation="Reader";
        	}
        	else
        	{
        		rbMenu2Item.setSelected(true);
        		ReadingLocation="Priest";
        		LastLocation="Priest";
        	}
        	
		menu.add(rbMenu1Item);
		menu.add(rbMenu2Item);
		menu.addSeparator();
		
		ButtonGroup group1=new ButtonGroup();
		JRadioButtonMenuItem rbMenu3Item=new JRadioButtonMenuItem(SelectorNames[3]);
		rbMenu3Item.addActionListener(this);
		rbMenu3Item.setMnemonic(KeyEvent.VK_I);
		rbMenu3Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		rbMenu3Item.setActionCommand("Independent");
		group1.add(rbMenu3Item);

		rbMenu4Item = new JRadioButtonMenuItem(SelectorNames[4]);
		rbMenu4Item.setMnemonic(KeyEvent.VK_B);
		rbMenu4Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		rbMenu4Item.addActionListener(this);
		rbMenu4Item.setActionCommand("W.Beginning");
		group1.add(rbMenu4Item);
		
		rbMenu5Item = new JRadioButtonMenuItem(SelectorNames[5]);
		rbMenu5Item.setMnemonic(KeyEvent.VK_E);
		rbMenu5Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		rbMenu5Item.addActionListener(this);
		rbMenu5Item.setActionCommand("W.Ending");
		group1.add(rbMenu5Item);
		
		rbMenu6Item = new JRadioButtonMenuItem(SelectorNames[6]);
		rbMenu6Item.setMnemonic(KeyEvent.VK_W);
		rbMenu6Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		rbMenu6Item.addActionListener(this);
		rbMenu6Item.setActionCommand("W.BeginningEnding");
		group1.add(rbMenu6Item);
		
		if(Default[1].equals("Independent"))
        	{
        		rbMenu3Item.setSelected(true);
        		ReadingLocation2="Independent";
        		LastLocation2="Independent";
        	}
        	else if(Default[1].equals("W.Beginning"))
        	{
        		rbMenu4Item.setSelected(true);
        		ReadingLocation2="W.Beginning";
        		LastLocation2="W.Beginning";
        	}
        	else if(Default[1].equals("W.Ending"))
        	{
        		rbMenu5Item.setSelected(true);
        		ReadingLocation2="W.Ending";
        		LastLocation2="W.Ending";
        	}
        	else
        	{
        		rbMenu6Item.setSelected(true);
        		ReadingLocation2="W.BeginningEnding";
        		LastLocation2="W.BeginningEnding";
        	}
        	
		menu.add(rbMenu3Item);
		menu.add(rbMenu4Item);
		menu.add(rbMenu5Item);
		menu.add(rbMenu6Item);

		return menu;
	}
	public void actionPerformed(ActionEvent e)
	{
		//THIS WILL DETERMINE THE PATH TO THE APPROPRIATE READING LOCATION
		ReadingLocation=e.getActionCommand();
		String Last1=LastLocation;
		String Last2=LastLocation2;
		if(!ReadingLocation.equals(LastLocation) && (ReadingLocation.equals("Priest") || ReadingLocation.equals("Reader")))
		{
			
			LastLocation=ReadingLocation;
			ConfigurationFiles.Defaults.put("Primes",LastLocation+","+LastLocation2);
			ConfigurationFiles.WriteFile();
			firePropertyChange("Who Change", (String) ReadingLocation, (String) Last1);										
		}
		else if(!ReadingLocation.equals(LastLocation2))
		{
			
			LastLocation2=ReadingLocation;
			ConfigurationFiles.Defaults.put("Primes",LastLocation+","+LastLocation2);
			ConfigurationFiles.WriteFile();
			firePropertyChange("Type Change", (String) ReadingLocation, (String) Last2);
												
		}
	}
	
	public void propertyChange(PropertyChangeEvent e)
	{
		//THERE IS NOTHING HERE TO DO??
		
	}

	protected static int getWhoValue()
	{
		if(LastLocation.equals("Reader"))
		{
			return 0;
		}
		return 1;
		
	}
	
	protected static int getTypeValue()
	{
		if(LastLocation2.equals("Independent"))
		{
			return 0;
		}
		else if(LastLocation2.equals("W.Beginning"))
		{
			return 1;
		}
		else if(LastLocation2.equals("W.Ending"))
		{
			return 2;
		}
		
		return 3;
		
	}

}