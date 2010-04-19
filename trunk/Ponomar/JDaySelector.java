package Ponomar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import java.io.UnsupportedEncodingException;

/**************************************************************
JDaySelector: A class for the object that does the heavy duty work in JCalendar
The purpuse of this class is to select a day.

Instructions: declare a JCalendar object and work from there.

Copyright information:
Adapted from JDayChooser: - A bean for choosing a day
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
class JDaySelector extends JPanel implements ActionListener, KeyListener, FocusListener
{
	private JButton[] days;
	private JButton[] tones;
	private JButton selectedDay;
	private int day;
	private JDate today;
	private JPanel dayPanel;
	private JPanel tonePanel;
	private Color sundayForeground;
	private Color weekdayForeground;
	private Color decorBackground;
	private Color oldColor;
	private Color[] commonBackgrounds = new Color[3];
	private Color dukeBlue;
	private Color selectedColor;
	private String[] dayNames;
	private boolean initialized;
	private boolean decorationBackgroundVisible;
	private boolean decorationBordersVisible;
	private Hashtable feasts;
	private int[] fasts;
	private LanguagePack Text=new LanguagePack();
        private Font CurrentFont=new Font((String)StringOp.dayInfo.get("FontFaceM"),Font.BOLD,Integer.parseInt((String)StringOp.dayInfo.get("FontSizeM")));
        private NumberFormat numFormat = NumberFormat.getInstance(new Locale(Text.Phrases.get("Language").toString(),Text.Phrases.get("Country").toString()));
        //(String)Text.Phrases.get("LanguageMenu")
        private DecimalFormat df=(DecimalFormat)numFormat;

	protected JDaySelector()
	{
            //Initialise the required locales
            DecimalFormatSymbols dfs=df.getDecimalFormatSymbols();
            //dfs.setZeroDigit('\u0660');
            
            dfs.setZeroDigit(Text.Phrases.get("ZeroPoint").toString().charAt(0));
            //System.out.println(Text.Phrases.get("ZeroPoint").toString().charAt(0));
            df.setDecimalFormatSymbols(dfs);

            setName("JDaySelector");
		setBackground(Color.blue);
		//locale = Locale.getDefault();

		days = new JButton[49];
		selectedDay = null;

		today = new JDate();
		setLayout(new BorderLayout());

		dayPanel = new JPanel();
		dayPanel.setLayout(new GridLayout(7, 7));

		sundayForeground  = new Color(255, 0, 0);
		weekdayForeground = new Color(0, 0, 0);
		decorBackground = new Color(210, 228, 238);
		dukeBlue = new Color(0, 51, 102);

		for (int i = 0; i < 7; i++)
		{
			for (int j = 0; j < 7; j++)
			{
				int index = j + (7 * i);

				if (i == 0)
				{
					//Create the top row (weedkay short names)
					days[index] = new PlaceholderButton();
				}
				else
				{
					// Create an actual day of the month button
					days[index] = new JButton("x") {
						public void paint(Graphics g)
						{
							if ("Windows".equals(UIManager.getLookAndFeel().getID()))
							{
								// this is a hack to get the background painted
								// when using Windows Look & Feel
								if (selectedDay == this) {
									g.setColor(selectedColor);
									g.fillRect(0, 0, getWidth(), getHeight());
								}
							}
							super.paint(g);
						}
					};
				
					days[index].addActionListener(this);
					days[index].addKeyListener(this);
					days[index].addFocusListener(this);
				}
			
				days[index].setMargin(new Insets(0, 0, 0, 0));
				days[index].setFocusPainted(false);
				dayPanel.add(days[index]);
			}
		}

		tonePanel = new JPanel();
		tonePanel.setLayout(new GridLayout(7, 1));
		tones = new JButton[7];

		for (int i = 0; i < 7; i++)
		{
			tones[i] = new PlaceholderButton();
			tones[i].setMargin(new Insets(0, 0, 0, 0));
			tones[i].setFocusPainted(false);
			tones[i].setForeground(new Color(100, 100, 100));

			if (i != 0)
			{
				tones[i].setText("0" + (i + 1));
			}

			tonePanel.add(tones[i]);
		}

		//defaultMinSelectableDate = new JDate(9, 1, 33);
		//defaultMaxSelectableDate = new JDate(8, 31, 9999);

		init();
		setDay(today.getDay());

		add(dayPanel, BorderLayout.CENTER);
		add(tonePanel, BorderLayout.WEST);

		initialized = true;
		updateUI();
	}

	private void init()
	{
//		JButton dummy = new JButton();
//		commonBackground = dummy.getBackground();
		commonBackgrounds[0] = new Color(255, 255, 255);
		commonBackgrounds[1] = new Color(170, 170, 170);
		commonBackgrounds[2] = new Color(221, 221, 221);
		selectedColor = new Color(255, 255, 0);
		feasts = Paschalion.getFeasts(today.getYear());
		fasts = Paschalion.getFasts(today.getYear());

		drawDayNames();
		drawDays();
	}

	private void drawDayNames()
	{
		//DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
		dayNames = Text.obtainValues((String)Text.Phrases.get("Week1"));

		int day = 0;

		for (int i = 0; i < 7; i++)
		{
			days[i].setText(dayNames[day]);
                        days[i].setFont(CurrentFont);
			if (day == 0) {
				days[i].setForeground(sundayForeground);
			} else {
				days[i].setForeground(weekdayForeground);
			}

			if (day < 7) {
				day++;
			} else {
				day -= 6;
			}
		}
	}

	private void drawDays()
	{
		JDate tmpCalendar = (JDate)today.clone();
		int firstDayOfWeek = 0;
		// SET CALENDAR TO START OF THE MONTH
		tmpCalendar.subtractDays(tmpCalendar.getDay() - 1);
		int firstDay = tmpCalendar.getDayOfWeek() - firstDayOfWeek;
		JDate startOfYear = new JDate(1, 1, tmpCalendar.getYear());

		if (firstDay < 0)
		{
			firstDay += 7;
		}

		int i;

		for (i = 0; i < firstDay; i++)
		{
			days[i + 7].setVisible(false);
			days[i + 7].setText("");
		}

		JDate firstDayOfNextMonth = (JDate)tmpCalendar.clone();
		firstDayOfNextMonth.addMonths();

		int n = 0; // KEEPS TRACK OF DAYS
		int diff = (int)JDate.difference(tmpCalendar, startOfYear);

		while (JDate.difference(firstDayOfNextMonth, tmpCalendar) > 0)
		{
                    
                    days[i + n + 7].setText(numFormat.format(n + 1)); //Integer.toString(n + 1))
                     //days[i + n + 7].setText(Integer.toString(n + 1));
                    
                        //Potentially there might be a need to change the font of the numbers here;
                        //for those cases where non-numbers are used to mark a calendar!
			days[i + n + 7].setVisible(true);

			// SEE IF THIS DAY IS TODAY
			if (tmpCalendar.equals(today))
			{
				days[i + n + 7].setForeground(dukeBlue);
				
			}

			// SEE IF THIS IS A SUNDAY
			if (tmpCalendar.getDayOfWeek() == 0)
			{
				days[i + n + 7].setForeground(sundayForeground);
			}
			else
			{
				days[i + n + 7].setForeground(weekdayForeground);
			}	
			
//			System.out.println(tmpCalendar.toString());
			// CHECK IF THIS DAY IS A FEAST DAY
			if (feasts.containsKey(tmpCalendar.getJulianDay()))
			{
				// THIS IS A FEAST DAY
				days[i + n + 7].setForeground(weekdayForeground);
				days[i + n + 7].setBackground(sundayForeground);
				days[i + n + 7].setToolTipText((String)feasts.get(tmpCalendar.getJulianDay()));
			} 
			else
			{
			//	days[i + n + 7].setBackground(commonBackground);
				days[i + n + 7].setBackground(commonBackgrounds[fasts[diff]]);
				days[i + n + 7].setToolTipText(null);
			}

			// CHECK IF THIS DAY IS SELECTED
			if (this.day == (n + 1))
			{
				oldColor = days[i + n + 7].getBackground();
				days[i + n + 7].setBackground(selectedColor);
				selectedDay = days[i + n + 7];
			}
/**			else
			{
				days[i + n + 7].setBackground(commonBackground);
			}**/

			// WE ARE NOT GOING TO WORRY ABOUT THE CALENDAR BEING OUT OF BOUNDS
			// THOUGH WE PROBABLY SHOULD

			diff++;
			n++;
			tmpCalendar.addDays(1);
		}

		for (int k = n + i + 7; k < 49; k++)
		{
			// HIDE THE BUTTONS THAT COME AFTER THE END OF THE MONTH
			days[k].setVisible(false);
			days[k].setText("");
		}

		// DRAW THE TONES
		drawTones();
	}

	private void drawTones()
	{
		String numerals[]  =Text.obtainValues((String)Text.Phrases.get("Tones"));
		

		// THIS WILL WORK FOR ALL TIMES EXCEPT DURING LENT 
		// WE'LL LEAVE IT AT THAT, FOR NOW
		//System.out.println(today.getYear());
		JDate pentecost = Paschalion.getPentecost(today.getYear());
		JDate tmpDate   = new JDate(today.getMonth(), 1, today.getYear());
		JDate pascha    = Paschalion.getPascha(today.getYear());

		// SET TO A SUNDAY, SINCE SUNDAYS DETERMINE TONES
		int dow = tmpDate.getDayOfWeek();
		tmpDate.subtractDays(dow);

		for (int i = 1; i < 7; i++)
		{
			int dif = (int)Math.floor(JDate.difference(tmpDate, pentecost) / 7);
			int df2 = (int)Math.floor(JDate.difference(tmpDate, pascha) / 7);
			int tone = 0; 
			//System.out.println(df2);

			if (dif > 0) 
			{
				// WE ARE AFTER PENTECOST
				// COMPUTE THE TONE BASED ON THE WEEKS AFTER PENTECOST
				tone = (dif % 8) + 7;
			}
			else if (df2 > 0 && dif != 0)
			{
				// WE ARE BETWEEN PASCHA AND PENTECOST
				// COMPUTE THE TONE BASED ON THE WEEKS AFTER PASCHA
				// THE SUNDAY OF MYRRH-BEARERS IS TONE II
				tone = (df2 % 8);
			}
			else if (df2 < -1)
			{
				// WE ARE BEFORE PASCHA
				// COMPUTE THE TONE BASED ON LAST YEAR'S PENTECOST
				JDate pentecost2 = Paschalion.getPentecost(today.getYear() - 1);
				int dif3 = (int)Math.floor(JDate.difference(tmpDate, pentecost2) / 7);
				tone = (Math.abs(dif3) % 8) + 7;
			}
			else
			{
				// THERE IS NO "TONE" FOR PALM SUNDAY, PASCHA, AND PENTECOST
				tone = 0;
			}

			if (tone > 8)
			{
				tone -= 8;
			}

			tones[i].setText(numerals[tone]);
                        tones[i].setFont(CurrentFont);

			if ((i == 5) || (i == 6)) {
				tones[i].setVisible(days[i * 7].isVisible());
			}
			tmpDate.addDays(7);
		}
	}

	protected void setDay(int d)
	{
		// CHECK FOR THE VALIDITY OF THIS d
		if (d < 1)
		{
			d = 1;
		}

		/**JDate tmpCalendar = new JDate(today.getMonth(), 1, today.getYear());
		tmpCalendar.addMonths(1);
		tmpCalendar.subtractDays(1); **/
		int max = JDate.getMaxDaysInMonth(today.getMonth(), today.getYear());

		if (d > max)
		{
			d = max;
		}

		// CHANGE THE SELECTED BUTTON TO d
		int oldDay = this.day;
		this.day = d;

		if (selectedDay != null)
		{
			//selectedDay.setBackground(commonBackgrounds[0]);
			selectedDay.setBackground(oldColor);
			selectedDay.repaint();
		}

		for (int i = 7; i < 49; i++)
		{
			if (days[i].getText().equals(numFormat.format(d)))
			{
				selectedDay = days[i];
				oldColor = selectedDay.getBackground();
				selectedDay.setBackground(selectedColor);
				break;
			}
		}

		firePropertyChange("day", oldDay, day);
	}

	protected void setMonth(int month)
	{
		int y   = today.getYear();
		int max = JDate.getMaxDaysInMonth(month, y);

		if (day > max) 
		{
			day = max;
		}

		today = new JDate(month, day, y);
		setDay(day);
		drawDays();
	}

	protected int getMonth()
	{
		return today.getMonth();
	}

	protected void setYear(int year)
	{
		if (year < 33)
		{
			year = 33;
		}

		int m = today.getMonth();
		int max = JDate.getMaxDaysInMonth(m, year);

		if (day > max)
		{
			day = max;	// TAKES CARE OF LEAP DAYS
		}

		today = new JDate(m, day, year);
		//setDay(day);
		init();
	}

	protected int getYear()
	{
		return today.getYear();
	}

	public void updateUI()
	{
		super.updateUI();
		//setFont(Font.decode("Times 11"));

		if (tonePanel != null)
		{
			tonePanel.updateUI();
		}

		if (initialized) {
			if ("Windows".equals(UIManager.getLookAndFeel().getID())) {
				setDayBordersVisible(false);
				setDecorationBackgroundVisible(true);
				setDecorationBordersVisible(false);
			} else {
				setDayBordersVisible(true);
				setDecorationBackgroundVisible(decorationBackgroundVisible);
				setDecorationBordersVisible(decorationBordersVisible);
			}
		}
	}

	private void setDayBordersVisible(boolean dayBordersVisible)
	{
		//this.dayBordersVisible = dayBordersVisible;
		if (initialized) {
			for (int x = 7; x < 49; x++) {
				if ("Windows".equals(UIManager.getLookAndFeel().getID())) {
					days[x].setContentAreaFilled(dayBordersVisible);
				} else {
					days[x].setContentAreaFilled(true);
				}
				days[x].setBorderPainted(dayBordersVisible);
			}
		}
	}

	private void setDecorationBackgroundVisible(boolean decorationBackgroundVisible) 
	{
		this.decorationBackgroundVisible = decorationBackgroundVisible;
		initDecorations();
	}

	private void setDecorationBordersVisible(boolean decorationBordersVisible)
	{
		this.decorationBordersVisible = decorationBordersVisible;
		initDecorations();
	}

	/**
	 * Initializes both day names and weeks of the year.
	 */
	private void initDecorations() {
		for (int x = 0; x < 7; x++) {
			days[x].setContentAreaFilled(decorationBackgroundVisible);
			days[x].setBorderPainted(decorationBordersVisible);
			days[x].invalidate();
			days[x].repaint();
			tones[x].setContentAreaFilled(decorationBackgroundVisible);
			tones[x].setBorderPainted(decorationBordersVisible);
			tones[x].invalidate();
			tones[x].repaint();
		}
	}

	// IMPLEMENTED METHODS
	//
	public void actionPerformed(ActionEvent e)
	{
		JButton button = (JButton)e.getSource();
		String buttonText = button.getText();
		int day = new Integer(buttonText).intValue();
		setDay(day);
	}

	public void focusGained(FocusEvent e)
	{
		// ...
	}

	public void focusLost(FocusEvent e)
	{
		// ...
	}

	public void keyPressed(KeyEvent e)
	{
		int offset = (e.getKeyCode() == KeyEvent.VK_UP) ? (-7)
				: ((e.getKeyCode() == KeyEvent.VK_DOWN) ? (+7)
						: ((e.getKeyCode() == KeyEvent.VK_LEFT) ? (-1)
								: ((e.getKeyCode() == KeyEvent.VK_RIGHT) ? (+1) : 0)));

		int newday = getDay() + offset;
		setDay(newday);
	}

	public void keyTyped(KeyEvent e)
	{

	}

	public void keyReleased(KeyEvent e)
	{

	}

	protected int getDay()
	{
		return day;
	}

	class PlaceholderButton extends JButton
	{
		protected PlaceholderButton()
		{
			setBackground(decorBackground);
			setContentAreaFilled(decorationBackgroundVisible);
			setBorderPainted(decorationBordersVisible);
		}

		public void addMouseListener(MouseListener l) {
		}

		public boolean isFocusable() {
			return false;
		}

		public void paint(Graphics g) 
		{
			if ("Windows".equals(UIManager.getLookAndFeel().getID())) {
				// this is a hack to get the background painted
				// when using Windows Look & Feel
				if (decorationBackgroundVisible) {
					g.setColor(decorBackground);
				} else {
					g.setColor(days[7].getBackground());
				}
				g.fillRect(0, 0, getWidth(), getHeight());
				if (isBorderPainted()) {
					setContentAreaFilled(true);
				} else {
					setContentAreaFilled(false);
				}
			}
			super.paint(g);
		}
	};

	public static void main(String[] argz)
	{
		// FOR TESTING PURPOSES ONLY!!!
		JFrame frame = new JFrame("TESTING THE CALENDAR CONTROL");
		frame.getContentPane().add(new JDaySelector());
		frame.pack();
		frame.setVisible(true);
	}
}
