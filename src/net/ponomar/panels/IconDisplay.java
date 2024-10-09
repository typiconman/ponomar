package net.ponomar.panels;

import javax.swing.*;

import net.ponomar.internationalization.LanguagePack;
import net.ponomar.utility.Constants;

import net.ponomar.utility.StringOp;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.net.URL;
import java.util.LinkedHashMap;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/**************************************************************
 * IconDisplay:
 * 
 * 
 * (C) 2010, 2012 Yuri Shardt PERMISSION IS HEREBY GRANTED TO REPRODUCE, MODIFY,
 * AND/OR DISTRIBUTE THIS SOURCE CODE PROVIDED THAT THIS NOTICE REMAINS IN ALL
 * VERSIONS OR DERIVATIVES THEREOF.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ***********************************************************/

public class IconDisplay extends JPanel implements ActionListener, FocusListener, PropertyChangeListener {
	// private JDaySelector daySelector;
	// private JDate date;
	private JPanel iconImage;
	private JPanel caption;

	private int number = 0;
	private String[] images;
	private String[] names;
	private JTextPane textPane;
	private static JFrame frame;
	private JButton previous;
	private JButton next;
	private String face;
	private String size;
	private StringOp analyse = new StringOp();
	// private Font CurrentFont = DefaultFont;

	private LanguagePack text;// =new LanguagePack();
	private String[] months;// = Text.obtainValues((String)Text.Phrases.get("1"));
	private String[] captions;// = Text.obtainValues((String) Text.Phrases.get("IconW"));
	// private static JFrame frame;

	protected IconDisplay() {
		// this(null);
	}

	public IconDisplay(String[] imagesF, String[] namesF, LinkedHashMap<String, Object> dayInfo) {
		// super();
		analyse.setDayInfo(dayInfo);
		text = new LanguagePack(dayInfo);
		months = text.obtainValues(text.getPhrases().get("1"));
		captions = text.obtainValues(text.getPhrases().get("IconW"));

		images = imagesF;
		names = namesF;
		// setName("Icon Viewer");
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		// System.out.println(Images.length);

		if (images.length <= 0) {
			// date = new JDate();
			System.out.println(images.length);
			images = new String[1];
			images[0] = Constants.DEFAULT_ICON;
			names = new String[1];
			names[0] = captions[2];
			// return;

		}

		iconImage = new JPanel();
		iconImage.setLayout(new BorderLayout());
		/*
		 * String IconLocation=pathImage+"icons/"; JLabel label= new JLabel(new
		 * ImageIcon(IconLocation+Images[Number]+".jpg"));
		 * label.setHorizontalAlignment(JLabel.CENTER); iconImage.add(label);
		 */

		caption = new JPanel();

		// if(Images.length>1){
		// BufferedImage image = ImageIO.read(new File(path));
		// LoadAndShow test = new LoadAndShow(image);

		String imgLocation = Constants.PREVIOUS_BUTTON;
		URL imgURL = IconDisplay.class.getResource(imgLocation);
		// System.out.println(imgURL);

		previous = new JButton();
		previous.setActionCommand("previous");
		previous.setToolTipText(captions[0]);
		previous.addActionListener(this);

		if (imgURL != null) {
			previous.setIcon(new ImageIcon(imgURL, captions[0]));// captions[bnum]));
		} else {
			// button.setText(Integer.toString(bnum));
			// System.err.println(captions[9] + imgLocation);
		}
		caption.setLayout(new BorderLayout());
		caption.add(previous, BorderLayout.WEST);

		// The textbox name
		face = analyse.getDayInfo().get(Constants.FONT_FACE_M).toString();
		size = analyse.getDayInfo().get(Constants.FONT_SIZE_M).toString();
		textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setContentType("text/html");
		JTextPane testing = new JTextPane();
		// text.setMaximumSize(new Dimension(1000,1500));
		textPane.setFont(new Font(face, Font.PLAIN, Integer.parseInt(size)));
		JScrollPane scroller = new JScrollPane(textPane);

		// JTextPane test=new JTextPane();

		// instructionsText.setComponentOrientation(OrientText);
		// text.setText(Names[Number]);
		// text.setFont();
		caption.add(textPane, BorderLayout.CENTER);
		caption.setMaximumSize(new Dimension(1000, 1000));
		// if(Images.length>1){
		next = new JButton();
		next.addActionListener(this);
		next.setActionCommand("next");
		next.setToolTipText(captions[1]);
		imgURL = IconDisplay.class.getResource(Constants.NEXT_BUTTON);
		if (imgURL != null) {
			next.setIcon(new ImageIcon(imgURL, captions[1]));// captions[bnum]));
		} else {
			// button.setText(Integer.toString(bnum));
			// System.err.println(captions[9] + imgLocation);
		}
		caption.add(next, BorderLayout.EAST);
		caption.setBorder(BorderFactory.createEmptyBorder());
		// }

		add(iconImage, BorderLayout.NORTH);
		add(caption, BorderLayout.CENTER);
		if (images.length < 2) {
			previous.setEnabled(false);
			next.setEnabled(false);
		}
		updateImages();
	}

	public void actionPerformed(ActionEvent e) {
		String name = e.getActionCommand();
		// System.out.println(name);
		if (name.equals("next")) {
			number += 1;
			if (number >= images.length) {
				number = 0;
			}

		} else if (name.equals("previous")) {
			number -= 1;
			if (number < 0) {
				number = images.length - 1;
			}
		}
		updateImages();
	}

	public void updateImages(String[] imagesF, String[] namesF) {
		// This changes the images available in the system
		// System.out.println(Names[0]);
		number = 0;
		images = imagesF;
		names = namesF;
		// text=new JTextPane();
		// System.out.println(Names[0]);
		// System.out.println(NamesF[0]);
		if (images.length <= 0) {
			// date = new JDate();
			// System.out.println(Images.length);
			images = new String[1];
			images[0] = Constants.DEFAULT_ICON;
			names = new String[1];
			names[0] = captions[2];
			// return;

		}
		if (images.length < 2) {
			previous.setEnabled(false);
			next.setEnabled(false);
		} else {

			previous.setEnabled(true);
			next.setEnabled(true);

		}

		updateImages();

	}

	private void updateImages() {
		// iconImage=new JPanel();
		iconImage.removeAll();
		String iconLocation = Constants.ICON_PATH;
		// System.out.println(IconLocation+Images[Number]+".jpg");
		JLabel label;
		if (images[number].contains(".jpg")) {
			label = new JLabel(new ImageIcon(images[number]));
		} else {
			label = new JLabel(new ImageIcon(iconLocation + images[number] + ".jpg"));
		}
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(images[number]));
		} catch (IOException valueIO) {
			valueIO.printStackTrace();
		}
		// System.out.println(image.getWidth() +" " + image.getHeight());

		// Testing something

		float iw = 0;
		float ih = 0;

		try {
			iw = image.getWidth();
			ih = image.getHeight();
		} catch (NullPointerException e) {
			System.out.println("Error finding file: " + images[number]);
			// Create fallback image
			image = new BufferedImage(1, 1, 1);
			e.printStackTrace();
		}

		float pw = this.getWidth() * (float) 0.95; // panel width
		float ph = this.getHeight() * (float) 0.8; // panel height
		// System.out.println("pw=" +pw+ " ph="+ph);
		Image scaledImage = image;
		// TESTING HERE
		if (pw < iw || ph < ih) {

			/*
			 * compare some ratios and then decide which side of image to anchor to panel
			 * and scale the other side (this is all based on empirical observations and not
			 * at all grounded in theory)
			 */

			if ((pw / ph) > (iw / ih)) {
				iw = -1;
				ih = ph;
			} else {
				iw = pw;
				ih = -1;
			}

			// prevent errors if panel is 0 wide or high
			if (iw == 0) {
				iw = -1;
			}
			if (ih == 0) {
				ih = -1;
			}

			scaledImage = image.getScaledInstance((int) iw, (int) ih, Image.SCALE_DEFAULT);

		} else {
			// scaledImage = image;
		}
		// scaledImage = image.getScaledInstance(new Float(pw).intValue(), new
		// Float(ph).intValue(), Image.SCALE_DEFAULT);

		// Image scaledImage=image.getScaledInstance(new Float(40).intValue(), new
		// Float(40).intValue(), Image.SCALE_DEFAULT);
		label.setIcon(new ImageIcon(scaledImage));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		iconImage.add(label);
		textPane.setText("<html><body style=\"font-family:" + face + ";font-size:" + size + "pt;text-align: center;\">"
				+ names[number] + "</body></html>");
		// frame.pack();
		// System.out.Println(getAncestorOfClass(new JFrame(),iconImage));
		// repaint();

	}

	public void focusGained(FocusEvent e) {
		// DO NOTHING
	}

	public void focusLost(FocusEvent e) {
		// CHECK IF THE TEXT OF THE OBJECT CHANGED
		JTextField jtf = (JTextField) e.getSource();
		String jtfText = jtf.getText();
	}

	public void propertyChange(PropertyChangeEvent e) {
		/*
		 * int d = daySelector.getDay(); int old = date.getDay(); int m =
		 * date.getMonth(); int y = date.getYear(); date = new JDate(m, d, y);
		 * firePropertyChange("day", old, d);
		 */
		// System.out.println(e.getNewValue()+" : "+e.getOldValue());
		// frame.pack();
	}

	public void updateImagesFiled(String[] imagesF, String[] namesF) {
		updateImages(imagesF, namesF);
	}


	public static void main(String[] argz) {
		// for testing purposes only
		// do not run as standalone -- may explode, leak, and cause serious injury
		// (to your computer) [just kidding]
		LinkedHashMap<String, Object> dayInfo = new LinkedHashMap<>();
		dayInfo.put("LS", "0");
		frame = new JFrame("IconDisplay");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Icon Viewer");
		String[] a = { "141", "137", "131" };
		String[] b = { "Saint First and Second", "Saint Second", "Saint Third and Fourth and Fifteenth" };
		// String[] a=new String[0];
		// String[] b=new String[0];
		IconDisplay jcalendar = new IconDisplay(a, b, dayInfo);
		jcalendar.setOpaque(true);
		frame.setContentPane(jcalendar);
		frame.pack();
		frame.setVisible(true);
	}
}
