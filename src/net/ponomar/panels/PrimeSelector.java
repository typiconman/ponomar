package net.ponomar.panels;

import net.ponomar.ConfigurationFiles;
import net.ponomar.internationalization.LanguagePack;

import net.ponomar.utility.StringOp;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;

/***************************************************************
 * GospelSelector.java :: MODULE THAT ALLOWS THE USER TO SELECT, USING RADIO
 * BUTTONS, WHICH TYPE OF REPEATS ARE BEING USED FOR THE GOSPEL READINGS
 * CURRENTLY BOTH THE LUCAN JUMP AND JORDANVILLE IMPLEMENTATIONS ARE AVAILABLE.
 * 
 * GospelSelector.java is part of the Ponomar project. 
 * Copyright 2008 Yuri Shardt
 * 
 * version 1.0: August 2008 yuri.shardt (at) gmail.com
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
 **************************************************************/

public class PrimeSelector extends JPanel implements ActionListener, PropertyChangeListener {
	private static final String PRIEST = "Priest";
	private static final String READER = "Reader";
	private static final String INDEPENDENT = "Independent";
	private static final String W_BEGINNING_ENDING = "W.BeginningEnding";
	private static final String W_BEGINNING = "W.Beginning";
	private static final String W_ENDING = "W.Ending";
	private static final String PRIMES = "Primes";
	private static String a = ConfigurationFiles.getDefaults().get(PRIMES);
	private static String[] primesDefault = a.split(",");
	private static String readingLocation = primesDefault[0]; // DETERMINES THE LOCATION OF THE READINGS
	// private JPanel radioPanel;
	// private JRadioButton LucanButton;
	// private JRadioButton JordanvilleButton;
	private static String lastLocation = primesDefault[0]; // AVOID REPEATING IF THERE IS NO CHANGE IN THE SELECTION
	// private JMenu submenu;
	private JRadioButtonMenuItem rbMenu1Item, rbMenu2Item, rbMenu4Item, rbMenu5Item, rbMenu6Item;
	private LanguagePack text;// =new LanguagePack();
	private String[] selectorNames;// =Text.obtainValues((String)Text.Phrases.get("PrimeSelection"));
	private static String lastLocation2 = primesDefault[1];
	private static String readingLocation2 = primesDefault[1];
	private StringOp analyse = new StringOp();

	public PrimeSelector(LinkedHashMap<String, Object> dayInfo) {
		analyse.setDayInfo(dayInfo);
		text = new LanguagePack(dayInfo);
		selectorNames = text.obtainValues(text.getPhrases().get("PrimeSelection"));
	}

	public JMenu createPrimeMenu() {

		// GospelSelector sample=new GospelSelector();
		// CREATE THE DEFAULT MENU
		a = ConfigurationFiles.getDefaults().get(PRIMES);
		String[] primesDefaultStrings = a.split(",");

		JMenu menu = new JMenu(selectorNames[7]);
		menu.setMnemonic(KeyEvent.VK_T);
		menu.getAccessibleContext().setAccessibleDescription(selectorNames[7]);

		// DETERMINE THE DEFAULTS

		ButtonGroup group = new ButtonGroup();
		rbMenu1Item = new JRadioButtonMenuItem(selectorNames[0]);
		rbMenu1Item.addActionListener(this);
		rbMenu1Item.setMnemonic(KeyEvent.VK_R);
		rbMenu1Item.setActionCommand(READER);
		rbMenu1Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		group.add(rbMenu1Item);

		rbMenu2Item = new JRadioButtonMenuItem(selectorNames[1]);
		rbMenu2Item.setMnemonic(KeyEvent.VK_T);
		rbMenu2Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		rbMenu2Item.addActionListener(this);
		rbMenu2Item.setActionCommand(PRIEST);
		group.add(rbMenu2Item);

		if (primesDefaultStrings[0].equals(READER)) {
			rbMenu1Item.setSelected(true);
			readingLocation = READER;
			lastLocation = READER;
		} else {
			rbMenu2Item.setSelected(true);
			readingLocation = PRIEST;
			lastLocation = PRIEST;
		}

		menu.add(rbMenu1Item);
		menu.add(rbMenu2Item);
		menu.addSeparator();

		ButtonGroup group1 = new ButtonGroup();
		JRadioButtonMenuItem rbMenu3Item = new JRadioButtonMenuItem(selectorNames[3]);
		rbMenu3Item.addActionListener(this);
		rbMenu3Item.setMnemonic(KeyEvent.VK_I);
		rbMenu3Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		rbMenu3Item.setActionCommand(INDEPENDENT);
		group1.add(rbMenu3Item);

		rbMenu4Item = new JRadioButtonMenuItem(selectorNames[4]);
		rbMenu4Item.setMnemonic(KeyEvent.VK_B);
		rbMenu4Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		rbMenu4Item.addActionListener(this);
		rbMenu4Item.setActionCommand(W_BEGINNING);
		group1.add(rbMenu4Item);

		rbMenu5Item = new JRadioButtonMenuItem(selectorNames[5]);
		rbMenu5Item.setMnemonic(KeyEvent.VK_E);
		rbMenu5Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		rbMenu5Item.addActionListener(this);
		rbMenu5Item.setActionCommand(W_ENDING);
		group1.add(rbMenu5Item);

		rbMenu6Item = new JRadioButtonMenuItem(selectorNames[6]);
		rbMenu6Item.setMnemonic(KeyEvent.VK_W);
		rbMenu6Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		rbMenu6Item.addActionListener(this);
		rbMenu6Item.setActionCommand(W_BEGINNING_ENDING);
		group1.add(rbMenu6Item);

		switch (primesDefaultStrings[1]) {
		case INDEPENDENT:
			rbMenu3Item.setSelected(true);
			readingLocation2 = INDEPENDENT;
			lastLocation2 = INDEPENDENT;
			break;
		case W_BEGINNING:
			rbMenu4Item.setSelected(true);
			readingLocation2 = W_BEGINNING;
			lastLocation2 = W_BEGINNING;
			break;
		case W_ENDING:
			rbMenu5Item.setSelected(true);
			readingLocation2 = W_ENDING;
			lastLocation2 = W_ENDING;
			break;
		default:
			rbMenu6Item.setSelected(true);
			readingLocation2 = W_BEGINNING_ENDING;
			lastLocation2 = W_BEGINNING_ENDING;
			break;
		}

		menu.add(rbMenu3Item);
		menu.add(rbMenu4Item);
		menu.add(rbMenu5Item);
		menu.add(rbMenu6Item);

		return menu;
	}

	public void actionPerformed(ActionEvent e) {
		// THIS WILL DETERMINE THE PATH TO THE APPROPRIATE READING LOCATION
		readingLocation = e.getActionCommand();
		String last1 = lastLocation;
		String last2 = lastLocation2;
		if (!readingLocation.equals(lastLocation)
				&& (readingLocation.equals(PRIEST) || readingLocation.equals(READER))) {

			lastLocation = readingLocation;
			ConfigurationFiles.getDefaults().put(PRIMES, lastLocation + "," + lastLocation2);
			ConfigurationFiles.writeFile();
			firePropertyChange("Who Change", readingLocation, last1);
		} else if (!readingLocation.equals(lastLocation2)) {

			lastLocation2 = readingLocation;
			ConfigurationFiles.getDefaults().put(PRIMES, lastLocation + "," + lastLocation2);
			ConfigurationFiles.writeFile();
			firePropertyChange("Type Change", readingLocation, last2);

		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		// THERE IS NOTHING HERE TO DO??

	}

	public static int getWhoValue() {
		if (lastLocation.equals(READER)) {
			return 0;
		}
		return 1;

	}

	public static int getTypeValue() {
		switch (lastLocation2) {
		case INDEPENDENT:
			return 0;
		case W_BEGINNING:
			return 1;
		case W_ENDING:
			return 2;
		default:
			return 3;
		}
	}

}