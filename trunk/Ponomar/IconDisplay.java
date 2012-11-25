package Ponomar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.net.URL;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/**************************************************************
IconDisplay:

 
 (C) 2010, 2012 Yuri Shardt
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

class IconDisplay extends JPanel implements ActionListener, FocusListener, PropertyChangeListener
{
	//private JDaySelector daySelector;
	//private JDate date;
	private JPanel iconImage;
        private JPanel caption;
        private String pathImage="Ponomar/images/";
	private JComboBox monthChooser;
	private PrintableTextPane yearChooser;
        private int Number=0;
        private String[] Images;
        private String[] Names;
        private JTextPane text;
        private static JFrame frame;
        private JButton previous;
        private JButton next;
        private String Face;
        private String Size;
        //private Font CurrentFont = DefaultFont;
        
	private LanguagePack Text=new LanguagePack();
	private final String[] months = Text.obtainValues((String)Text.Phrases.get("1"));
        private final String[] captions = Text.obtainValues((String) Text.Phrases.get("IconW"));
        //private static JFrame frame;


	protected IconDisplay()
	{
		//this(null);
	}
        
	protected IconDisplay(String[] ImagesF, String[] NamesF)
	{
		//super();
                
                Images=ImagesF;
                Names=NamesF;
		//setName("Icon Viewer");
		setLayout(new BorderLayout());
                //System.out.println(Images.length);

		if (Images.length<=0)
		{
			//date = new JDate();
                    System.out.println(Images.length);
                    Images=new String[1];
                    Images[0]="Ponomar/languages/icons/Default1.jpg";
                    Names=new String[1];
                    Names[0]=captions[2];
                    //return;

		}

                iconImage = new JPanel();
		iconImage.setLayout(new BorderLayout());
                /*String IconLocation=pathImage+"icons/";
                JLabel label= new JLabel(new ImageIcon(IconLocation+Images[Number]+".jpg"));
                label.setHorizontalAlignment(JLabel.CENTER);
                iconImage.add(label);*/

                caption=new JPanel();

                //if(Images.length>1){
                // BufferedImage image = ImageIO.read(new File(path));
        //LoadAndShow test = new LoadAndShow(image);

                String imgLocation = "images/0.gif";
                URL imgURL = IconDisplay.class.getResource(imgLocation);
                //System.out.println(imgURL);

                previous = new JButton();
                previous.setActionCommand("previous");
                previous.setToolTipText(captions[0]);
                previous.addActionListener(this);

                if (imgURL != null) {
                    previous.setIcon(new ImageIcon(imgURL, captions[0]));//captions[bnum]));
                } else {
                    //button.setText(Integer.toString(bnum));
                    //System.err.println(captions[9] + imgLocation);
                }
                caption.add(previous,BorderLayout.WEST);


                //The textbox name
                Face=StringOp.dayInfo.get("FontFaceM").toString();
                Size=StringOp.dayInfo.get("FontSizeM").toString();
                text = new PrintableTextPane();
                text.setEditable(false);
                text.setContentType("text/html");

                //JTextPane test=new JTextPane();
                text.setFont(new Font(Face,Font.PLAIN,Integer.parseInt(Size)));

                // instructionsText.setComponentOrientation(OrientText);
                //text.setText(Names[Number]);
                //text.setFont();
                caption.add(text,BorderLayout.EAST);
                //if(Images.length>1){
                    next = new JButton();
                next = new JButton();
                next.addActionListener(this);
                next.setActionCommand("next");
                next.setToolTipText(captions[1]);
               imgURL = IconDisplay.class.getResource("images/2.gif");
                if (imgURL != null) {
                    next.setIcon(new ImageIcon(imgURL, captions[1]));//captions[bnum]));
                } else {
                    //button.setText(Integer.toString(bnum));
                    //System.err.println(captions[9] + imgLocation);
                }
               caption.add(next,BorderLayout.EAST);
               caption.setBorder(BorderFactory.createEmptyBorder());
                //}
               
               add(iconImage,BorderLayout.NORTH);
               add(caption,BorderLayout.CENTER);
               if (Images.length<2){
            previous.setEnabled(false);
            next.setEnabled(false);
             }
               updateImages();		
	}

	public void actionPerformed(ActionEvent e)
	{
            String name=e.getActionCommand();
            //System.out.println(name);
            if(name.equals("next")){
                Number+=1;
                if (Number>=Images.length){
                    Number=0;
                }

            }else if(name.equals("previous")){
                Number-=1;
                if (Number<0){
                    Number=Images.length-1;
                }
            }
            updateImages();
	}
        public void updateImages(String[] ImagesF, String[] NamesF){
            //This changes the images available in the system
            //System.out.println(Names[0]);
            Number=0;
            Images=ImagesF;
            Names=NamesF;
            //text=new JTextPane();
            //System.out.println(Names[0]);
            //System.out.println(NamesF[0]);
            if (Images.length<=0)
		{
			//date = new JDate();
                    //System.out.println(Images.length);
                    Images=new String[1];
                    Images[0]="Default1";
                    Names=new String[1];
                    Names[0]=captions[2];
                    //return;

		}
            if (Images.length<2){
            previous.setEnabled(false);
            next.setEnabled(false);
             }else
             {

            previous.setEnabled(true);
            next.setEnabled(true);

             }

            updateImages();

        }
        private void updateImages() {
            //iconImage=new JPanel();
            iconImage.removeAll();
            String IconLocation=pathImage+"icons/";
            //System.out.println(IconLocation+Images[Number]+".jpg");
             JLabel label =new JLabel();
            if (Images[Number].contains(".jpg")){
               label= new JLabel(new ImageIcon(Images[Number]));
            }
            else
            {
                label= new JLabel(new ImageIcon(IconLocation+Images[Number]+".jpg"));
            }
             BufferedImage image=null;
             try
             {
             image = ImageIO.read(new File(Images[Number]));
             }
             catch(IOException valueIO)
             {                

             }
             System.out.println(image.getWidth() +" " + image.getHeight());

             //Testing something

             float iw = image.getWidth();
            float ih = image.getHeight();
            float pw = this.getWidth()*(float)0.95;   //panel width
            float ph = this.getHeight()*(float)0.8;  //panel height
            System.out.println("pw=" +pw+ " ph="+ph);
            Image scaledImage=image;
            //TESTING HERE
            if ( pw < iw || ph < ih ) {

                /* compare some ratios and then decide which side of image to anchor to panel
                   and scale the other side
                   (this is all based on empirical observations and not at all grounded in theory)*/

                //System.out.println("pw/ph=" + pw/ph + ", iw/ih=" + iw/ih);

                if ( (pw / ph) > (iw / ih) ) {
                    iw = -1;
                    ih = ph;
                } else {
                    iw = pw;
                    ih = -1;
                }

                //prevent errors if panel is 0 wide or high
                if (iw == 0) {
                    iw = -1;
                }
                if (ih == 0) {
                    ih = -1;
                }

                scaledImage = image.getScaledInstance(new Float(iw).intValue(), new Float(ih).intValue(), Image.SCALE_DEFAULT);

            } else {
                //scaledImage = image;
            }
                //scaledImage = image.getScaledInstance(new Float(pw).intValue(), new Float(ph).intValue(), Image.SCALE_DEFAULT);

             //Image scaledImage=image.getScaledInstance(new Float(40).intValue(), new Float(40).intValue(), Image.SCALE_DEFAULT);
             label.setIcon(new ImageIcon(scaledImage));
            label.setHorizontalAlignment(JLabel.CENTER);
            iconImage.add(label);
            text.setText("<body style=\"font-family:"+Face+";font-size:"+Size+"pt\">"+ Names[Number]+"</body>");           
           //frame.pack();
           //System.out.Println(getAncestorOfClass(new JFrame(),iconImage));
           //repaint();
           

               
        }

	public void focusGained(FocusEvent e)
	{
		// DO NOTHING
	}

	public void focusLost(FocusEvent e)
	{
		// CHECK IF THE TEXT OF THE OBJECT CHANGED
		JTextField jtf = (JTextField)e.getSource();
		String text = jtf.getText();

		/*int oldyear = date.getYear();
		if (text.equals(String.valueOf(oldyear)) == false)
		{
			// THE OBJECT HAS CHANGED
			// MAKE SURE THAT THE TEXT IS VALID
			int newyear = Integer.parseInt(text);
			if (newyear > 33) 
			{
				// CHANGE THE DATE
				int m = date.getMonth();
				int d = date.getDay();
				int max = JDate.getMaxDaysInMonth(m, newyear);

				// MAKE SURE WE'RE VALID
				if (d < max) 
				{
					d = max;
				}

				date = new JDate(m, d, newyear);
				daySelector.setYear(newyear);
				firePropertyChange("year", oldyear, newyear);
			}
			else
			{
				// NOT VALID
				jtf.setText(new Integer(oldyear).toString());
			}
		}*/
	}

	public void propertyChange(PropertyChangeEvent e)
	{
		/*int d = daySelector.getDay();
		int old = date.getDay();
		int m = date.getMonth();
		int y = date.getYear();
		date = new JDate(m, d, y);
		firePropertyChange("day", old, d);*/
            //System.out.println(e.getNewValue()+" : "+e.getOldValue());
            //frame.pack();
	}

	/*protected int getMonth()
	{
		//return date.getMonth();
	}

	protected int getDay()
	{
		//return date.getDay();
	}

	protected int getYear()
	{
		//return date.getYear();
	}
*/
        public void updateImagesFiled(String[] ImagesF, String[] NamesF){
            //This changes the images available in the system
            //System.out.println(Names[0]);
            Number=0;
            Images=ImagesF;
            Names=NamesF;
            System.out.println(Images.length);
            //text=new JTextPane();
            //System.out.println(Names[0]);
            //System.out.println(NamesF[0]);
            if (Images.length<=0)
		{
			//date = new JDate();
                    //System.out.println(Images.length);
                    Images=new String[1];
                    Images[0]="Ponomar/languages/icons/Default1.jpg";
                    Names=new String[1];
                    Names[0]=captions[2];
                    //return;

		}
            if (Images.length<2){
            previous.setEnabled(false);
            next.setEnabled(false);
             }else
             {

            previous.setEnabled(true);
            next.setEnabled(true);

             }

            updateImages();

        }
        private void updateImages2(){
            //iconImage=new JPanel();
            iconImage.removeAll();
            String IconLocation=pathImage+"icons/";
            //System.out.println(IconLocation+Images[Number]+".jpg");
            JLabel label= new JLabel(new ImageIcon(Images[Number]));
            label.setHorizontalAlignment(JLabel.CENTER);
            iconImage.add(label);
           text.setText("<body style=\"font-family:"+Face+";font-size:"+Size+"pt\">"+ Names[Number]+"</body>");
           //frame.pack();
           //System.out.Println(getAncestorOfClass(new JFrame(),iconImage));
           //repaint();
           


        }
	public static void main(String[] argz)
	{
		// for testing purposes only
		// do not run as standalone -- may explode, leak, and cause serious injury
		// (to your computer) [just kidding]
		StringOp.dayInfo=new OrderedHashtable();
                StringOp.dayInfo.put("LS","0");
                frame = new JFrame("IconDisplay");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setTitle("Icon Viewer");
                String[] a={"141","137","131"};
                String[] b={"Saint First and Second","Saint Second","Saint Third and Fourth and Fifteenth"};
		//String[] a=new String[0];
                //String[] b=new String[0];
                IconDisplay jcalendar = new IconDisplay(a,b);
		jcalendar.setOpaque(true);
		frame.setContentPane(jcalendar);
		frame.pack();
		frame.setVisible(true);
	}
}
