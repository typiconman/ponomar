package Ponomar;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.io.*;

/************************************************************
THIS CREATES THE MENUS REQUIRED FOR THE PROGRAMME/INTERFACE

COPYRIGHT 2008 Yuri Shardt
Version 1.0 August 2008

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

class MenuFiles extends JMenu implements ItemListener, PropertyChangeListener
{
	JTextArea output;
	String newline = "\n";
	private JMenuBar MenuBar;
	GospelSelector GospelSelection;			//ALLOWS THE USER TO NOTE WHEN THE GOSPEL LECTIONARY FORMAT IS CHANGED
	LanguageSelector LanguageSelection;		//DITTO FOR LANGUAGES
	private JMenu Selection;
	private JMenuBar menuBar;
	private JMenu menu, menu1, menu2, menu3, menu4, submenu, submenu2;
	private JMenuItem menuItem, menu3Item, menu4Item, menu5Item, menu6Item, menu7Item, menu8Item, menu9Item, menu10Item, menu11Item, menu12Item, menu13Item, menu14Item, menu15Item, menu16Item, menu17Item, menu18Item, menu19Item;
	private JRadioButtonMenuItem rbMenu1Item, rbMenu2Item;
	private JMenu OptionsMenu;
	private LanguagePack Text=new LanguagePack();
	private String[] SaintNames=Text.obtainValues((String)Text.Phrases.get("SMenu"));
	private String OptionsNames=(String)Text.Phrases.get("Options");
	private String[] FileNames=Text.obtainValues((String)Text.Phrases.get("File")); 
	private String[] ServiceNames=Text.obtainValues((String)Text.Phrases.get("Services"));
	private String[] BibleName=Text.obtainValues((String)Text.Phrases.get("Bible"));
	private String[] HelpNames=Text.obtainValues((String)Text.Phrases.get("Help"));
	

public MenuFiles()
{
	
}
public JMenu createOptionsMenu(PropertyChangeListener pl)
{
	OptionsMenu = new JMenu(OptionsNames);
	OptionsMenu.setMnemonic(KeyEvent.VK_O);
	//ADD THIS MENU TO THE MAIN MENU
	GospelSelection = new GospelSelector();
	Selection = GospelSelection.createGospelMenu();
	GospelSelection.addPropertyChangeListener(pl);
	OptionsMenu.add(Selection);
		
	LanguageSelection = new LanguageSelector();
	JMenu Selection2=LanguageSelection.createLanguageMenu();
	LanguageSelection.addPropertyChangeListener(pl);
	OptionsMenu.add(Selection2);
	
	return OptionsMenu;
}
public JMenu createFileMenu(ActionListener al)
{
	menu = new JMenu(FileNames[0]);
	menu.setMnemonic(KeyEvent.VK_F);
	menu.getAccessibleContext().setAccessibleDescription(FileNames[2]);
		
	menuItem=new JMenuItem(FileNames[1],KeyEvent.VK_S);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription(FileNames[3]);
	menuItem.addActionListener(al);
	menu.add(menuItem);

	menuItem = new JMenuItem(FileNames[6], KeyEvent.VK_P);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription(FileNames[7]);
	menuItem.addActionListener(al);
	menu.add(menuItem);
	
	menuItem = new JMenuItem(FileNames[4], KeyEvent.VK_E);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription(FileNames[5]);
	menuItem.addActionListener(al);
	menu.add(menuItem);
	
	
	return menu;
}
public JMenu createSaintsMenu(ActionListener al)
{
	menu = new JMenu(SaintNames[0]);
	menu.setMnemonic(KeyEvent.VK_N);
	menu.getAccessibleContext().setAccessibleDescription(SaintNames[1]);
		
	menuItem=new JMenuItem(SaintNames[2],KeyEvent.VK_S);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
	menuItem.getAccessibleContext().setAccessibleDescription(SaintNames[3]);
	menuItem.setEnabled(false);
	menuItem.addActionListener(al);
	menu.add(menuItem);

	return menu;
}
public JMenu createServicesMenu(ActionListener al)
{
	menu2= new JMenu(ServiceNames[0]);
	menu2.setMnemonic(KeyEvent.VK_S);
	menu2.getAccessibleContext().setAccessibleDescription(ServiceNames[13]);
		
	menu3Item = new JMenuItem(ServiceNames[1], KeyEvent.VK_D);
	menu3Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
	menu3Item.getAccessibleContext().setAccessibleDescription(ServiceNames[14]);
	menu3Item.addActionListener(al);
	menu3Item.setEnabled(false);		//Currently unavailable colour!
	menu2.add(menu3Item);
	
	menu4Item = new JMenuItem(ServiceNames[2], KeyEvent.VK_V);
	menu4Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
	menu4Item.getAccessibleContext().setAccessibleDescription(ServiceNames[15]);
	menu4Item.addActionListener(al);
	menu4Item.setEnabled(false);		//Currently unavailable colour!
	menu2.add(menu4Item);
	
	menu5Item = new JMenuItem(ServiceNames[3], KeyEvent.VK_C);
	menu5Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
	menu5Item.getAccessibleContext().setAccessibleDescription(ServiceNames[16]);
	menu5Item.addActionListener(al);
	menu5Item.setEnabled(false);		//Currently unavailable colour!
	menu2.add(menu5Item);
	
	menu6Item = new JMenuItem(ServiceNames[4], KeyEvent.VK_M);
	menu6Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
	menu6Item.getAccessibleContext().setAccessibleDescription(ServiceNames[17]);
	menu6Item.addActionListener(al);
	menu6Item.setEnabled(false);		//Currently unavailable colour!
	menu2.add(menu6Item);
	
	menu7Item = new JMenuItem(ServiceNames[5], KeyEvent.VK_P);
	menu7Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
	menu7Item.getAccessibleContext().setAccessibleDescription(ServiceNames[18]);
	menu7Item.addActionListener(al);
	menu2.add(menu7Item);
	
	menu8Item = new JMenuItem(ServiceNames[6], KeyEvent.VK_T);
	menu8Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
	menu8Item.getAccessibleContext().setAccessibleDescription(ServiceNames[19]);
	menu8Item.addActionListener(al);
	menu8Item.setEnabled(true);		//Currently available colour!
	menu2.add(menu8Item);
	
	menu9Item = new JMenuItem(ServiceNames[7], KeyEvent.VK_S);
	menu9Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
	menu9Item.getAccessibleContext().setAccessibleDescription(ServiceNames[20]);
	menu9Item.addActionListener(al);
	menu9Item.setEnabled(true);		//Currently available colour!
	menu2.add(menu9Item);
	
	menu10Item = new JMenuItem(ServiceNames[8], KeyEvent.VK_N);
	menu10Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
	menu10Item.getAccessibleContext().setAccessibleDescription(ServiceNames[21]);
	menu10Item.addActionListener(al);
	menu10Item.setEnabled(true);		//Currently available colour!
	menu2.add(menu10Item);
	
	menu11Item = new JMenuItem(ServiceNames[9], KeyEvent.VK_R);
	menu11Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
	menu11Item.getAccessibleContext().setAccessibleDescription(ServiceNames[22]);
	menu11Item.addActionListener(al);
	menu11Item.setEnabled(true);		//Currently available colour!
	menu2.add(menu11Item);
	
	menu12Item = new JMenuItem(ServiceNames[10], KeyEvent.VK_L);
	menu12Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
	menu12Item.getAccessibleContext().setAccessibleDescription(ServiceNames[23]);
	menu12Item.addActionListener(al);
	menu12Item.setEnabled(false);		//Currently unavailable colour!
	menu2.add(menu12Item);
	
	menu13Item = new JMenuItem(ServiceNames[11], KeyEvent.VK_I);
	menu13Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
	menu13Item.getAccessibleContext().setAccessibleDescription(ServiceNames[24]);
	menu13Item.addActionListener(al);
	menu13Item.setEnabled(false);		//Currently unavailable colour!
	menu2.add(menu13Item);
	
	menu14Item = new JMenuItem(ServiceNames[12], KeyEvent.VK_T);
	menu14Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
	menu14Item.getAccessibleContext().setAccessibleDescription(ServiceNames[25]);
	menu14Item.addActionListener(al);
	menu14Item.setEnabled(false);		//Currently unavailable colour!
	menu2.add(menu14Item);
	
	return menu2;
}
public JMenu createBibleMenu(ActionListener al)
{
	menu3= new JMenu(BibleName[0]);
	menu3.setMnemonic(KeyEvent.VK_B);
	menu3.getAccessibleContext().setAccessibleDescription(BibleName[1]);
		
	menu17Item = new JMenuItem(BibleName[0], KeyEvent.VK_B);
	menu17Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
	menu17Item.getAccessibleContext().setAccessibleDescription("Go to the Bible Reader");
	menu17Item.addActionListener(al);
	menu3.add(menu17Item);
	
	return menu3;
}

public JMenu createHelpMenu(ActionListener al)
{
	menu4 = new JMenu(HelpNames[0]);
	menu4.setMnemonic(KeyEvent.VK_H);
	menu4.getAccessibleContext().setAccessibleDescription(HelpNames[1]);
		
	menu15Item = new JMenuItem(HelpNames[2], KeyEvent.VK_A);
	menu15Item.getAccessibleContext().setAccessibleDescription(HelpNames[3]);
	menu15Item.addActionListener(al);
	menu4.add(menu15Item);
	
	menu16Item = new JMenuItem(HelpNames[0], KeyEvent.VK_H);
	menu16Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
	menu16Item.getAccessibleContext().setAccessibleDescription(HelpNames[4]);
	menu16Item.addActionListener(al);
	menu16Item.setEnabled(false);		//Currently unavailable colour!
	menu4.add(menu16Item);
	
	return menu4;
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
		
	}

 
   
}