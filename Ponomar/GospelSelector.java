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

class GospelSelector extends JPanel implements ActionListener, PropertyChangeListener
{
	private static String ReadingLocation;			//DETERMINES THE LOCATION OF THE READINGS
	private JPanel radioPanel;
	private JRadioButton LucanButton;
	private JRadioButton JordanvilleButton;
	private static String LastLocation;			//AVOID REPEATING IF THERE IS NO CHANGE IN THE SELECTION
	private JMenu submenu;
	private JRadioButtonMenuItem rbMenu1Item, rbMenu2Item;
	private LanguagePack Text;//=new LanguagePack();
	private String[] SelectorNames;//=Text.obtainValues((String)Text.Phrases.get("GospelSelection"));
	private StringOp Analyse=new StringOp();
	public GospelSelector(OrderedHashtable dayInfo)
	{
	Analyse.dayInfo=dayInfo;
        Text=new LanguagePack(dayInfo);
         SelectorNames=Text.obtainValues((String)Text.Phrases.get("GospelSelection"));
                Font CurrentFont=new Font((String)Analyse.dayInfo.get("FontFaceM"),Font.PLAIN,Integer.parseInt((String)Analyse.dayInfo.get("FontSizeM")));
           
	}
	public JPanel createGospelSelector()
	{
            
           
            SelectorNames=Text.obtainValues((String)Text.Phrases.get("GospelSelection"));
                Font CurrentFont=new Font((String)Analyse.dayInfo.get("FontFaceM"),Font.PLAIN,Integer.parseInt((String)Analyse.dayInfo.get("FontSizeM")));
            //DETERMINE THE DEFAULTS
		String Default = (String) ConfigurationFiles.Defaults.get("GospelSelector");
		//Create the radio buttons.
		JordanvilleButton = new JRadioButton(SelectorNames[0]);
	        JordanvilleButton.setMnemonic(KeyEvent.VK_T);
        	JordanvilleButton.setActionCommand("TheophanyJump");
        	JordanvilleButton.setFont(CurrentFont);
        	LucanButton = new JRadioButton(SelectorNames[1]);
		 LucanButton.setMnemonic(KeyEvent.VK_L);
                 LucanButton.setActionCommand("LucanJump");
        	 LucanButton.setFont(CurrentFont);
                 
        	if(Default.equals("TheophanyJump"))
        	{
        		JordanvilleButton.setSelected(true);
        		ReadingLocation="TheophanyJump";
        		LastLocation="TheophanyJump";
        	}
        	else
        	{
        		LucanButton.setSelected(true);
        		ReadingLocation="LucanJump";
        		LastLocation="LucanJump";
        	}
        	      	
        	
        	 
        	 //GROUP THE RADIO BUTTONS
        	 ButtonGroup group = new ButtonGroup();
        	group.add(JordanvilleButton);
        	group.add(LucanButton);
        	
        	//Register a listener for the radio buttons.
       		JordanvilleButton.addActionListener(this);
       		LucanButton.addActionListener(this);
       		
       		
       		
       		
       		//CREATE THE RADIO BUTTONS AND ADD THEM		
		radioPanel = new JPanel(new GridLayout(0, 2));
		radioPanel.add(JordanvilleButton);
        	radioPanel.add(LucanButton);
        	JTextPane text = new JTextPane();
		text.setEditable(false);
		text.setContentType("text/html");
        	text.setText(SelectorNames[2]);
        	text.setOpaque(false);
        	add(text);
        	add(radioPanel, BorderLayout.LINE_START);
        	//radioPanel.addPropertyChangeListener(this);
        	
        	return radioPanel;
        	       	
        }
	
	public JMenu createGospelMenu()
	{
		
		
		//GospelSelector sample=new GospelSelector();
		//CREATE THE DEFAULT MENU
             //Font CurrentFont=new Font((String)StringOp.dayInfo.get("FontFaceM"),Font.PLAIN,Integer.parseInt((String)StringOp.dayInfo.get("FontSizeM")));
		submenu=new JMenu(SelectorNames[2]);
		submenu.setToolTipText(SelectorNames[3]);
		submenu.setMnemonic(KeyEvent.VK_G);
		submenu.getAccessibleContext().setAccessibleDescription(SelectorNames[3]);
		//submenu.setFont(CurrentFont);
		//DETERMINE THE DEFAULTS
		String Default = (String) ConfigurationFiles.Defaults.get("GospelSelector");
		
		ButtonGroup group = new ButtonGroup();
		rbMenu1Item=new JRadioButtonMenuItem(SelectorNames[0]);
		rbMenu1Item.addActionListener(this);
                rbMenu1Item.setActionCommand("TheophanyJump");
        	rbMenu1Item.setMnemonic(KeyEvent.VK_T);
		rbMenu1Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, ActionEvent.CTRL_MASK));
		//rbMenu1Item.setFont(CurrentFont);

		rbMenu2Item = new JRadioButtonMenuItem(SelectorNames[1]);
		rbMenu2Item.setMnemonic(KeyEvent.VK_L);
                rbMenu2Item.setActionCommand("LucanJump");
		rbMenu2Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		//rbMenu2Item.setFont(CurrentFont);
		
		if(Default.equals("TheophanyJump"))
        	{
        		rbMenu1Item.setSelected(true);
        		ReadingLocation="TheophanyJump";
        		LastLocation="TheophanyJump";
        	}
        	else
        	{
        		rbMenu2Item.setSelected(true);
        		ReadingLocation="LucanJump";
        		LastLocation="LucanJump";
        	}
        	
		group.add(rbMenu1Item);
		submenu.add(rbMenu1Item);
		
		group.add(rbMenu2Item);
		rbMenu2Item.addActionListener(this);
		submenu.add(rbMenu2Item);
		
		return submenu;
	}
	public void actionPerformed(ActionEvent e)
	{
		//THIS WILL DETERMINE THE PATH TO THE APPROPRIATE READING LOCATION
		ReadingLocation=e.getActionCommand();
                
		if(!ReadingLocation.equals(LastLocation))
		{
			firePropertyChange("Gospel Lectionary", (String) ReadingLocation, (String) LastLocation);
			LastLocation=ReadingLocation;
			ConfigurationFiles.Defaults.put("GospelSelector",LastLocation);
			ConfigurationFiles.WriteFile();
													
		}
		
	}
	
	public void propertyChange(PropertyChangeEvent e)
	{
		//THERE IS NOTHING HERE TO DO??
		
	}

	protected static int getGValue()
	{
            
		if(ReadingLocation.equals("TheophanyJump"))
		{
			return 0;
		}
		return 1;
                
		
	}

}