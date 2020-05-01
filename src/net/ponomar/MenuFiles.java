package net.ponomar;

import javax.swing.*;

import net.ponomar.internationalization.LanguagePack;
import net.ponomar.internationalization.LanguageSelector;
import net.ponomar.panels.GospelSelector;
 
 
import net.ponomar.utility.StringOp;

import java.awt.event.*;
import java.beans.*;
import java.util.LinkedHashMap;

/************************************************************
 * 
 * COPYRIGHT 2008, 2012 Yuri Shardt Version 1.0 August 2008 Version 2.0 December
 * 2012
 * 
 * PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE
 * CODE PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES
 * THEREOF.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 **************************************************************/

/**
 * 
 * This creates the menus required for the programme/interface
 * 
 * @author Yuri Shardt
 * 
 */
public class MenuFiles extends JMenu implements ItemListener, PropertyChangeListener {
	JTextArea output;
	static final String NEWLINE = "\n";
	GospelSelector gospelSelection; // ALLOWS THE USER TO NOTE WHEN THE GOSPEL LECTIONARY FORMAT IS CHANGED
	LanguageSelector languageSelection; // DITTO FOR LANGUAGES
	private final LanguagePack text;// =new LanguagePack();
	private final String[] saintNames;// =Text.obtainValues((String)Text.Phrases.get("SMenu"));
	private final String optionsNames;// =(String)Text.Phrases.get("Options");
	private final String[] fileNames;// =Text.obtainValues((String)Text.Phrases.get("File"));
	private final String[] serviceNames;// =Text.obtainValues((String)Text.Phrases.get("Services"));
	private final String[] bibleName;// =Text.obtainValues((String)Text.Phrases.get("Bible"));
	private final String[] helpNames;// =Text.obtainValues((String)Text.Phrases.get("Help"));
	private final StringOp analyse = new StringOp();

	public MenuFiles(LinkedHashMap<String, Object> dayInfo) {
		analyse.setDayInfo(dayInfo);
		text = new LanguagePack(dayInfo);
		saintNames = text.obtainValues(text.getPhrases().get("SMenu"));
		optionsNames = text.getPhrases().get("Options");
		fileNames = text.obtainValues(text.getPhrases().get("File"));
		serviceNames = text.obtainValues(text.getPhrases().get("Services"));
		bibleName = text.obtainValues(text.getPhrases().get("Bible"));
		helpNames = text.obtainValues(text.getPhrases().get("Help"));
	}

	public JMenu createOptionsMenu(PropertyChangeListener pl, ActionListener al) {
		JMenu optionsMenu = new JMenu(optionsNames);
		optionsMenu.setMnemonic(KeyEvent.VK_O);
		// ADD THIS MENU TO THE MAIN MENU
		gospelSelection = new GospelSelector(analyse.getDayInfo());
		JMenu gospelSelectionMenu = gospelSelection.createGospelMenu();
		gospelSelection.addPropertyChangeListener(pl);
		optionsMenu.add(gospelSelectionMenu);

		languageSelection = new LanguageSelector((LinkedHashMap) analyse.getDayInfo().clone());
		JMenu languageSelectionMenu = languageSelection.createLanguageMenu((LinkedHashMap) analyse.getDayInfo().clone());
		languageSelection.addPropertyChangeListener(pl);
		optionsMenu.add(languageSelectionMenu);

		optionsMenu.add(generateMenuItem(text.getPhrases().get("OptionMenu"),
				text.getPhrases().get("OptionMenu"), KeyEvent.VK_D, KeyEvent.VK_D, al, true));

		return optionsMenu;
	}

	public JMenu createFileMenu(ActionListener al) {
		JMenu menu = new JMenu(fileNames[0]);
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription(fileNames[2]);
		menu.add(generateMenuItem(fileNames[1], fileNames[3], KeyEvent.VK_S, KeyEvent.VK_S, al, true));
		menu.add(generateMenuItem(fileNames[6], fileNames[7], KeyEvent.VK_P, KeyEvent.VK_P, al, true));
		menu.add(generateMenuItem(fileNames[4], fileNames[5], KeyEvent.VK_E, KeyEvent.VK_Q, al, true));

		return menu;
	}

	public JMenu createSaintsMenu(ActionListener al) {
		JMenu menu = new JMenu(saintNames[0]);
		menu.setMnemonic(KeyEvent.VK_N);
		menu.getAccessibleContext().setAccessibleDescription(saintNames[1]);
		menu.add(generateMenuItem(saintNames[2], saintNames[3], KeyEvent.VK_S, KeyEvent.VK_W, al, false));

		return menu;
	}

	public JMenu createServicesMenu(ActionListener al) {
		JMenu menu = new JMenu(serviceNames[0]);
		menu.setMnemonic(KeyEvent.VK_S);
		menu.getAccessibleContext().setAccessibleDescription(serviceNames[13]);
		menu.add(generateMenuItem(serviceNames[1], serviceNames[14], KeyEvent.VK_D, KeyEvent.VK_D, al, false));
		menu.add(generateMenuItem(serviceNames[2], serviceNames[15], KeyEvent.VK_V, KeyEvent.VK_E, al, false));
		menu.add(generateMenuItem(serviceNames[3], serviceNames[16], KeyEvent.VK_C, KeyEvent.VK_C, al, false));
		menu.add(generateMenuItem(serviceNames[4], serviceNames[17], KeyEvent.VK_M, KeyEvent.VK_M, al, false));
		menu.add(generateMenuItem(serviceNames[5], serviceNames[18], KeyEvent.VK_P, KeyEvent.VK_R, al, true));
		menu.add(generateMenuItem(serviceNames[6], serviceNames[19], KeyEvent.VK_T, KeyEvent.VK_T, al, true));
		menu.add(generateMenuItem(serviceNames[7], serviceNames[20], KeyEvent.VK_S, KeyEvent.VK_S, al, true));
		menu.add(generateMenuItem(serviceNames[8], serviceNames[21], KeyEvent.VK_N, KeyEvent.VK_N, al, true));
		menu.add(generateMenuItem(serviceNames[9], serviceNames[22], KeyEvent.VK_R, KeyEvent.VK_R, al, true));
		menu.add(generateMenuItem(serviceNames[10], serviceNames[23], KeyEvent.VK_L, KeyEvent.VK_G, al, false));
		menu.add(generateMenuItem(serviceNames[11], serviceNames[24], KeyEvent.VK_I, KeyEvent.VK_I, al, false));
		menu.add(generateMenuItem(serviceNames[12], serviceNames[25], KeyEvent.VK_T, KeyEvent.VK_Y, al, false));

		return menu;
	}

	public JMenu createBibleMenu(ActionListener al) {
		JMenu menu = new JMenu(bibleName[0]);
		menu.setMnemonic(KeyEvent.VK_B);
		menu.getAccessibleContext().setAccessibleDescription(bibleName[1]);
		// menu3.setFont(CurrentFont);

		menu.add(generateMenuItem(bibleName[0], serviceNames[25], KeyEvent.VK_B, KeyEvent.VK_B, al, true));

		return menu;
	}

	public JMenu createHelpMenu(ActionListener al) {
		JMenu menu = new JMenu(helpNames[0]);
		menu.setMnemonic(KeyEvent.VK_H);
		menu.getAccessibleContext().setAccessibleDescription(helpNames[1]);
		menu.add(generateMenuItem(helpNames[2], helpNames[3], KeyEvent.VK_A, KeyEvent.VK_A, al, true));
		menu.add(generateMenuItem(helpNames[0], helpNames[4], KeyEvent.VK_H, KeyEvent.VK_H, al, false));

		return menu;
	}

	private JMenuItem generateMenuItem(String text, String description, int keyEvent, int acceleratorKeyEvent,
			ActionListener al, boolean enabled) {
		JMenuItem menuItem = new JMenuItem(text, keyEvent);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(acceleratorKeyEvent, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(description);
		menuItem.addActionListener(al);
		menuItem.setEnabled(enabled);
		return menuItem;
	}

	protected String getClassName(Object o) {
		String classString = o.getClass().getName();
		int dotIndex = classString.lastIndexOf('.');
		return classString.substring(dotIndex + 1);
	}

	public void itemStateChanged(ItemEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String s = "Item event detected." + NEWLINE + "    Event source: " + source.getText() + " (an instance of "
				+ getClassName(source) + ")" + NEWLINE + "    New state: "
				+ ((e.getStateChange() == ItemEvent.SELECTED) ? "selected" : "unselected");
		System.out.println(s);
	}

	public void propertyChange(PropertyChangeEvent e) {
		// THERE IS NOTHING HERE TO DO??

	}

}