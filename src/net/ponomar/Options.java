package net.ponomar;

import javax.swing.*;

import net.ponomar.internationalization.LanguagePack;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;

import net.ponomar.utility.StringOp;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.beans.*;

/***********************************************************************
(C) 2013 YURI SHARDT. ALL RIGHTS RESERVED.


 PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE CODE
 PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES THEREOF.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
***********************************************************************/

/**
 * 
 * This module allows the user to change the defaults associated with the java
 * programme. This module is still in the development phase.
 * 
 * @author Yuri Shardt
 * 
 */
public class Options extends JFrame implements ActionListener, ItemListener, PropertyChangeListener {
	private static final String GREGORIAN = "Gregorian";
	private static final String CALENDAR = "Calendar";
	private LanguagePack text;// =new LanguagePack();
	private StringOp analyse = new StringOp();
	private String[] optionsStrings;
	// private JFrame frames;
	private String newline = "\n";
	private JTextField latitude;
	private JTextField longitude;
	private JComboBox<String> timeZoneBox;
	private ButtonGroup calendarButtons;
	private JButton okay;
	private JButton cancel;
	private String[] timeZone = { "-12", "-11", "-10", "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1", "0", "+1",
			"+2", "+3", "+4", "+5", "+6", "+7", "+8", "+9", "+10", "+11", "+12", };
	private JRadioButton jRadioButton2;
	private JRadioButton jRadioButton1;
	private Font currentFont;
	private boolean ignore = true;

	public Options(LinkedHashMap<String, Object> dayInfo) {
		analyse.setDayInfo(dayInfo);
		text = new LanguagePack(dayInfo);
		ConfigurationFiles.setDefaults(new LinkedHashMap<>());
		ConfigurationFiles.readFile();

		optionsStrings = text.obtainValues(text.getPhrases().get("Options2"));
		currentFont = new Font((String) analyse.getDayInfo().get(Constants.FONT_FACE_M), Font.PLAIN,
				Integer.parseInt((String) analyse.getDayInfo().get(Constants.FONT_SIZE_M)));

		// createDefaultWindow();

	}

	public void createDefaultWindow() {
		// frames=new JFrame(Options[0]);
		//(String)Text.Phrases.get("0") + (String)Text.Phrases.get("Colon")+ PrimesNames[1]);
		setTitle(optionsStrings[0]);
		// frames.setSize(200,200);
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel top = new JPanel();
		top.setLayout(new GridLayout(4, 3));
		JLabel blank = new JLabel();
		blank.setFont(currentFont);
		top.add(blank);
		JLabel blank2 = new JLabel(optionsStrings[1], SwingConstants.CENTER);
		blank2.setFont(currentFont);
		top.add(blank2);
		JLabel blank3 = new JLabel();
		blank3.setFont(currentFont);
		top.add(blank3);

		JLabel latitude2 = new JLabel(optionsStrings[2], SwingConstants.RIGHT);
		latitude2.setFont(currentFont);
		top.add(latitude2);
		latitude = new JTextField();
		latitude.setEditable(true);
		latitude.setText(ConfigurationFiles.getDefaults().get("Latitude"));
		latitude.setHorizontalAlignment(SwingConstants.RIGHT);
		latitude.setFont(currentFont);
		top.add(latitude);
		JLabel units = new JLabel(optionsStrings[3], SwingConstants.LEFT);
		units.setFont(currentFont);
		top.add(units);

		JLabel longitude2 = new JLabel(optionsStrings[4], SwingConstants.RIGHT);
		longitude2.setFont(currentFont);
		top.add(longitude2);
		longitude = new JTextField();
		longitude.setEditable(true);
		longitude.setText(ConfigurationFiles.getDefaults().get("Longitude"));
		longitude.setHorizontalAlignment(SwingConstants.RIGHT);
		longitude.setFont(currentFont);
		top.add(longitude);
		JLabel units2 = new JLabel(optionsStrings[5], SwingConstants.LEFT);
		units2.setFont(currentFont);
		top.add(units2);

		JLabel timezone = new JLabel(optionsStrings[6], SwingConstants.RIGHT);
		timezone.setFont(currentFont);
		top.add(timezone);

		timeZoneBox = new JComboBox<>();
		timeZoneBox.setFont(currentFont);

		/*
		 * TimeZonesAll timesGet=new TimeZonesAll(); 
		 * java.util.List<TimeZone> timeZones = timesGet.getTimeZones(); 
		 * for (TimeZone timeZone : timeZones) {
		 * System.out.println(timesGet.getName(timeZone));
		 * }
		 */
		for (String s : timeZone) {
			timeZoneBox.addItem(s);
		}
		timeZoneBox.setSelectedItem(ConfigurationFiles.getDefaults().get("TimeZone"));
		timeZoneBox.setEditable(false);
		top.add(timeZoneBox, BorderLayout.CENTER);

		JLabel units3 = new JLabel(optionsStrings[7], SwingConstants.LEFT);
		units3.setFont(currentFont);
		top.add(units3);

		top.setBorder(BorderFactory.createBevelBorder(0));

		JPanel centre = new JPanel();
		centre.setLayout(new GridLayout(2, 3));

		JLabel blanka = new JLabel();
		blanka.setFont(currentFont);
		centre.add(blanka);
		JLabel blank5 = new JLabel(optionsStrings[8], SwingConstants.CENTER);
		blank5.setFont(currentFont);
		centre.add(blank5);
		JLabel blank4 = new JLabel();
		blank4.setFont(currentFont);
		centre.add(blank4);
		centre.setPreferredSize(new Dimension(100, 150));

		JLabel name = new JLabel(optionsStrings[9], SwingConstants.LEFT);
		name.setFont(currentFont);
		name.setPreferredSize(new Dimension(50, 50));
		centre.add(name);

		String defaultCalendar = ConfigurationFiles.getDefaults().get(CALENDAR);

		jRadioButton1 = new JRadioButton();
		jRadioButton1.setText(optionsStrings[10]);
		jRadioButton1.setFont(currentFont);

		if (!(defaultCalendar.equals(GREGORIAN))) {
			jRadioButton1.setSelected(true);
		}

		jRadioButton2 = new JRadioButton();

		jRadioButton2.setText(optionsStrings[11]);
		jRadioButton2.setFont(currentFont);
		if ((defaultCalendar.equals(GREGORIAN))) {
			jRadioButton2.setSelected(true);
		}

		calendarButtons = new ButtonGroup();
		calendarButtons.add(jRadioButton1);
		calendarButtons.add(jRadioButton2);

		centre.add(jRadioButton1);
		centre.add(jRadioButton2);

		centre.setBorder(BorderFactory.createBevelBorder(0));

		JPanel bottom = new JPanel();
		bottom.setLayout(new GridLayout(2, 2));

		JLabel blank6 = new JLabel("Religious Information", SwingConstants.CENTER);
		bottom.add(blank6);
		JLabel blank7 = new JLabel();
		bottom.add(blank7);

		bottom.setBorder(BorderFactory.createBevelBorder(0));

		JPanel footer = new JPanel();
		footer.setLayout(new GridLayout(1, 4));
		okay = new JButton(optionsStrings[12]);
		cancel = new JButton(optionsStrings[13]);
		JLabel blank8 = new JLabel();
		JLabel blank9 = new JLabel();
		okay.addActionListener(this);
		okay.setFont(currentFont);
		cancel.addActionListener(this);
		cancel.setFont(currentFont);

		footer.add(blank8);
		footer.add(blank9);
		footer.add(okay);
		footer.add(cancel);

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.setOpaque(true);

		contentPane.add(top, BorderLayout.NORTH);
		contentPane.add(centre, BorderLayout.CENTER);

		contentPane.add(footer, BorderLayout.SOUTH);

		setContentPane(contentPane);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Helpers orient = new Helpers(analyse.getDayInfo());
		orient.applyOrientation(this, (ComponentOrientation) analyse.getDayInfo().get(Constants.ORIENT));

		pack();
		int width = 550;
		if (text.getPhrases().get("OptionsW") != null) {
			width = Integer.parseInt(text.getPhrases().get("OptionsW"));
		}
		int height = 220;
		if (text.getPhrases().get("OptionsH") != null) {
			height = Integer.parseInt(text.getPhrases().get("OptionsH"));
		}
		setSize(width, height);
		setVisible(true);
		ignore = false;

	}

	public void actionPerformed(ActionEvent e) {
		// JMenuItem source = (JMenuItem)(e.getSource());
		// String name = source.getText();

		// Helpers helper = new Helpers(Analyse.getDayInfo());
		String name = e.getActionCommand();
		// ALLOWS A MULTILINGUAL PROPER VERSION
		if (name.equals(optionsStrings[12])) {
			// System.out.println("Pressed Okay");
			String latitude2 = latitude.getText();
			double latitude1 = Double.parseDouble(latitude2);
			String longitude2 = longitude.getText();
			double longitude1 = Double.parseDouble(longitude2);
			if (!(latitude1 >= -90 && latitude1 <= 90)) {
				System.out.println("Error in Entering Latitude; value not used");
			} else {
				ConfigurationFiles.getDefaults().put("Latitude", latitude.getText());
			}

			if (!(longitude1 >= -180 && longitude1 <= 180)) {
				System.out.println("Error in Entering Longitude; value not used");
			} else {
				ConfigurationFiles.getDefaults().put("Longitude", longitude.getText());
			}
			ConfigurationFiles.getDefaults().put("TimeZone",
					Objects.requireNonNull(timeZoneBox.getSelectedItem()).toString());

			String previous = ConfigurationFiles.getDefaults().get(CALENDAR);
			if (jRadioButton1.getSelectedObjects() != null) {
				ConfigurationFiles.getDefaults().put(CALENDAR, "Julian");
				if (!(previous.equals("Julian")) && !ignore) {
					firePropertyChange("CalendarChange", 1, 0);
				}
			} else {
				ConfigurationFiles.getDefaults().put(CALENDAR, GREGORIAN);
				if (!(previous.equals(GREGORIAN)) && !ignore) {
					firePropertyChange("CalendarChange", 0, 1);
				}
			}
			ConfigurationFiles.writeFile();
			dispose();

		}
		if (name.equals(optionsStrings[13])) {
			// System.out.println("Pressed Cancel");
			dispose();
		}

	}

	protected String getClassName(Object o) {
		String classString = o.getClass().getName();
		int dotIndex = classString.lastIndexOf('.');
		return classString.substring(dotIndex + 1);
	}

	public void itemStateChanged(ItemEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());
		String s = "Item event detected." + newline + "    Event source: " + source.getText() + " (an instance of "
				+ getClassName(source) + ")" + newline + "    New state: "
				+ ((e.getStateChange() == ItemEvent.SELECTED) ? "selected" : "unselected");
		System.out.println(s);
		// output.append(s + newline);
		// output.setCaretPosition(output.getDocument().getLength());
	}

	public void propertyChange(PropertyChangeEvent e) {
		// THERE IS NOTHING HERE TO DO??
		try {
			// output.setText(createHours());
			// output.setCaretPosition(0);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}

	public static void main(String[] argz) {
		// DEBUG MODE
		System.out.println("Options.java running in Debug mode");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY!!");

		LinkedHashMap<String, Object> dayInfo = new LinkedHashMap<>();

		dayInfo.put("dow", 3);
		dayInfo.put("doy", 357);
		dayInfo.put("nday", -256);
		dayInfo.put("LS", "cu/"); // ENGLISH
		dayInfo.put("PS", 1);
		dayInfo.put(Constants.FONT_FACE_M, Constants.PONOMAR_UNICODE_TT);
		dayInfo.put(Constants.FONT_SIZE_M, "18");
		dayInfo.put(Constants.ORIENT, ComponentOrientation.getOrientation(new Locale("ru")));

		new Options(dayInfo);
	}

}
