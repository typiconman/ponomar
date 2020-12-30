package net.ponomar.calendar;

import javax.swing.*;

import net.ponomar.internationalization.LanguagePack;
 
 
import net.ponomar.utility.StringOp;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.LinkedHashMap;

/**************************************************************
JCalendar: AN IMPLEMENTATION OF A JULIAN CALENDAR CONTROL FOR THE GUI
THE PURPOSE IS TO DISPLAY A FULLY FUNCTIONAL CALENDAR OF JDATES WITH FASTS AND FEASTS

INSTRUCTIONS: CREATE A JCalendar(JDate date) TO GET A CALENDAR WITH date SELECTED, SEE DOCUMENTATION BELOW

Copyright information:
Adapted from JCalendar: - A Calendar control using swing
 *  Copyright (C) 2004 Kai Toedter
 *  kai@toedter.com
 *  www.toedter.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 
 (C) 2006 ALEKSANDR ANDREEV.
 PERMISSION IS HEREBY GRANTED TO REPRODUCE, MODIFY, AND/OR DISTRIBUTE THIS SOURCE CODE
 PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSIONS OR DERIVATIVES THEREOF.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE. 
***********************************************************/

public class JCalendar extends JPanel implements ActionListener, FocusListener, PropertyChangeListener {
	private JDaySelector daySelector;
	private JDate date;
	private JPanel monthYearPanel;
	private JComboBox<String> monthChooser;
	private JTextField yearChooser;
	private LanguagePack text;// =new LanguagePack();
	private String[] months; // = Text.obtainValues((String)Text.Phrases.get("1"));
	private String orderBox;// =(String)Text.Phrases.get("OrderBox");
	private StringOp analyse = new StringOp();

	public JCalendar(LinkedHashMap<String, Object> dayInfo) {
		this(null, dayInfo);

		analyse.setDayInfo(dayInfo);
		text = new LanguagePack(dayInfo);
		months = text.obtainValues(text.getPhrases().get("1"));
		orderBox = text.getPhrases().get("OrderBox");
		date = new JDate();

	}

	protected JCalendar(JDate date, LinkedHashMap<String, Object> dayInfo) {
		super();
		analyse.setDayInfo(dayInfo);
		text = new LanguagePack(dayInfo);
		months = text.obtainValues(text.getPhrases().get("1"));
		orderBox = text.getPhrases().get("OrderBox");
		setName("JCalendar");
		setLayout(new BorderLayout());

		if (date == null) {
			date = new JDate();
		}
		this.date = date;

		monthYearPanel = new JPanel();
		monthYearPanel.setLayout(new BorderLayout());

		monthChooser = new JComboBox<>(months);
		monthChooser.setSelectedIndex(date.getMonth() - 1);
		monthChooser.addActionListener(this);

		yearChooser = new JTextField();
		yearChooser.setText(Integer.toString(date.getYear()));
		yearChooser.addFocusListener(this);

		if (orderBox.equals("YY")) {
			monthYearPanel.add(yearChooser, BorderLayout.WEST);
			monthYearPanel.add(monthChooser, BorderLayout.CENTER);
		} else {
			monthYearPanel.add(monthChooser, BorderLayout.WEST);
			monthYearPanel.add(yearChooser, BorderLayout.CENTER);
		}

		monthYearPanel.setBorder(BorderFactory.createEmptyBorder());

		daySelector = new JDaySelector((LinkedHashMap<String, Object>) analyse.getDayInfo().clone());
		daySelector.setYear(date.getYear());
		daySelector.setMonth(date.getMonth());
		daySelector.setDay(date.getDay());
		daySelector.addPropertyChangeListener(this);

		add(monthYearPanel, BorderLayout.NORTH);
		add(daySelector, BorderLayout.CENTER);
		System.out.println("Calendar created");
	}

	public void actionPerformed(ActionEvent e) {
		JComboBox<?> jcb = (JComboBox<?>) e.getSource();
		int m = jcb.getSelectedIndex() + 1;
		int d = date.getDay();
		int old = date.getMonth();
		int y = date.getYear();
		int max = JDate.getMaxDaysInMonth(m, y);

		if (d > max) {
			d = max;
		}

		date = new JDate(m, d, y);
		daySelector.setMonth(jcb.getSelectedIndex() + 1);
		firePropertyChange("month", old, m);
	}

	public void focusGained(FocusEvent e) {
		// DO NOTHING
	}

	public void focusLost(FocusEvent e) {
		// CHECK IF THE TEXT OF THE OBJECT CHANGED
		JTextField jtf = (JTextField) e.getSource();
		String textContent = jtf.getText();
		int oldyear = date.getYear();
		if (!textContent.equals(String.valueOf(oldyear))) {
			// THE OBJECT HAS CHANGED
			// MAKE SURE THAT THE TEXT IS VALID
			int newyear = Integer.parseInt(textContent);
			if (newyear > 33) {
				// CHANGE THE DATE
				int m = date.getMonth();
				int d = date.getDay();
				int max = JDate.getMaxDaysInMonth(m, newyear);

				// MAKE SURE WE'RE VALID
				if (d < max) {
					d = max;
				}

				date = new JDate(m, d, newyear);
				daySelector.setYear(newyear);
				firePropertyChange("year", oldyear, newyear);
			} else {
				// NOT VALID
				jtf.setText(Integer.toString(oldyear));
			}
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		int d = daySelector.getDay();
		int old = date.getDay();
		int m = date.getMonth();
		int y = date.getYear();
		date = new JDate(m, d, y);
		firePropertyChange("day", old, d);
	}

	public int getMonth() {
		return date.getMonth();
	}

	public int getDay() {
		return date.getDay();
	}

	public int getYear() {
		return date.getYear();
	}

	public static void main(String[] argz) {
		// for testing purposes only
		// do not run as standalone -- may explode, leak, and cause serious injury
		// (to your computer) [just kidding]
		/*
		 * JFrame frame = new JFrame("JCalendar");
		 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); JCalendar jcalendar =
		 * new JCalendar(); jcalendar.setOpaque(true); frame.setContentPane(jcalendar);
		 * frame.pack(); frame.setVisible(true);
		 */
	}
}
