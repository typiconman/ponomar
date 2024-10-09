package net.ponomar.panels;

import net.ponomar.ConfigurationFiles;
import net.ponomar.internationalization.LanguagePack;
import net.ponomar.utility.Constants;
 
 
import net.ponomar.utility.StringOp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;

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

public class GospelSelector extends JPanel implements ActionListener, PropertyChangeListener
{
	private static final String GOSPEL_SELECTOR = "GospelSelector";
	private static final String THEOPHANY_JUMP = "TheophanyJump";
	private static final String LUCAN_JUMP = "LucanJump";
	private static String readingLocation;			//DETERMINES THE LOCATION OF THE READINGS
	private JPanel radioPanel;
	private JRadioButton lucanButton;
	private JRadioButton jordanvilleButton;
	private static String lastLocation;			//AVOID REPEATING IF THERE IS NO CHANGE IN THE SELECTION
	private JMenu submenu;
	private JRadioButtonMenuItem rbMenu1Item, rbMenu2Item;
	private LanguagePack text;//=new LanguagePack();
	private String[] selectorNames;//=Text.obtainValues((String)Text.Phrases.get("GospelSelection"));
	private StringOp analyse=new StringOp();
	public GospelSelector(LinkedHashMap<String, Object> dayInfo)
	{
	analyse.setDayInfo(dayInfo);
        text=new LanguagePack(dayInfo);
         selectorNames=text.obtainValues(text.getPhrases().get("GospelSelection"));
                Font currentFont=new Font((String)analyse.getDayInfo().get(Constants.FONT_FACE_M),Font.PLAIN,Integer.parseInt((String)analyse.getDayInfo().get(Constants.FONT_SIZE_M)));
           
	}
	public JPanel createGospelSelector()
	{
            
           
            selectorNames=text.obtainValues(text.getPhrases().get("GospelSelection"));
                Font currentFont=new Font((String)analyse.getDayInfo().get(Constants.FONT_FACE_M),Font.PLAIN,Integer.parseInt((String)analyse.getDayInfo().get(Constants.FONT_SIZE_M)));
            //DETERMINE THE DEFAULTS
		String gospelDefault = ConfigurationFiles.getDefaults().get(GOSPEL_SELECTOR);
		//Create the radio buttons.
		jordanvilleButton = new JRadioButton(selectorNames[0]);
	        jordanvilleButton.setMnemonic(KeyEvent.VK_T);
        	jordanvilleButton.setActionCommand(THEOPHANY_JUMP);
        	jordanvilleButton.setFont(currentFont);
        	lucanButton = new JRadioButton(selectorNames[1]);
		 lucanButton.setMnemonic(KeyEvent.VK_L);
                 lucanButton.setActionCommand(LUCAN_JUMP);
        	 lucanButton.setFont(currentFont);
                 
        	if(gospelDefault.equals(THEOPHANY_JUMP))
        	{
        		jordanvilleButton.setSelected(true);
        		readingLocation=THEOPHANY_JUMP;
        		lastLocation=THEOPHANY_JUMP;
        	}
        	else
        	{
        		lucanButton.setSelected(true);
        		readingLocation=LUCAN_JUMP;
        		lastLocation=LUCAN_JUMP;
        	}
        	      	
        	
        	 
        	 //GROUP THE RADIO BUTTONS
        	 ButtonGroup group = new ButtonGroup();
        	group.add(jordanvilleButton);
        	group.add(lucanButton);
        	
        	//Register a listener for the radio buttons.
       		jordanvilleButton.addActionListener(this);
       		lucanButton.addActionListener(this);
       		
       		
       		
       		
       		//CREATE THE RADIO BUTTONS AND ADD THEM		
		radioPanel = new JPanel(new GridLayout(0, 2));
		radioPanel.add(jordanvilleButton);
        	radioPanel.add(lucanButton);
        	JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setContentType("text/html");
        	textPane.setText(selectorNames[2]);
        	textPane.setOpaque(false);
        	add(textPane);
        	add(radioPanel, BorderLayout.LINE_START);
        	//radioPanel.addPropertyChangeListener(this);
        	
        	return radioPanel;
        	       	
        }
	
	public JMenu createGospelMenu()
	{
		
		
		//GospelSelector sample=new GospelSelector();
		//CREATE THE DEFAULT MENU
             //Font CurrentFont=new Font((String)StringOp.dayInfo.get("FontFaceM"),Font.PLAIN,Integer.parseInt((String)StringOp.dayInfo.get("FontSizeM")));
		submenu=new JMenu(selectorNames[2]);
		submenu.setToolTipText(selectorNames[3]);
		submenu.setMnemonic(KeyEvent.VK_G);
		submenu.getAccessibleContext().setAccessibleDescription(selectorNames[3]);
		//submenu.setFont(CurrentFont);
		//DETERMINE THE DEFAULTS
		String gospelDefault = ConfigurationFiles.getDefaults().get(GOSPEL_SELECTOR);
		
		ButtonGroup group = new ButtonGroup();
		rbMenu1Item=new JRadioButtonMenuItem(selectorNames[0]);
		rbMenu1Item.addActionListener(this);
                rbMenu1Item.setActionCommand(THEOPHANY_JUMP);
        	rbMenu1Item.setMnemonic(KeyEvent.VK_T);
		rbMenu1Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, ActionEvent.CTRL_MASK));
		//rbMenu1Item.setFont(CurrentFont);

		rbMenu2Item = new JRadioButtonMenuItem(selectorNames[1]);
		rbMenu2Item.setMnemonic(KeyEvent.VK_L);
                rbMenu2Item.setActionCommand(LUCAN_JUMP);
		rbMenu2Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		//rbMenu2Item.setFont(CurrentFont);
		
		if(gospelDefault.equals(THEOPHANY_JUMP))
        	{
        		rbMenu1Item.setSelected(true);
        		readingLocation=THEOPHANY_JUMP;
        		lastLocation=THEOPHANY_JUMP;
        	}
        	else
        	{
        		rbMenu2Item.setSelected(true);
        		readingLocation=LUCAN_JUMP;
        		lastLocation=LUCAN_JUMP;
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
		readingLocation=e.getActionCommand();
                
		if(!readingLocation.equals(lastLocation))
		{
			firePropertyChange("Gospel Lectionary", readingLocation, lastLocation);
			lastLocation=readingLocation;
			ConfigurationFiles.getDefaults().put(GOSPEL_SELECTOR,lastLocation);
			ConfigurationFiles.writeFile();
													
		}
		
	}
	
	public void propertyChange(PropertyChangeEvent e)
	{
		//THERE IS NOTHING HERE TO DO??
		
	}

	public static int getGValue()
	{
            
		if(readingLocation.equals(THEOPHANY_JUMP))
		{
			return 0;
		}
		return 1;
                
		
	}

}