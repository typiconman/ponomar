package Ponomar;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.charset.StandardCharsets;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;


/***********************************************************************
THIS MODULE CREATES THE TEXT FOR THE SEARCHING FOR COMMEMORATIONS ACCROSS LANGUAGES/JURISDICTIONS

(C) 2022 YURI SHARDT. ALL RIGHTS RESERVED.


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
public class EditCommemoration extends JFrame implements ActionListener
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	
	public String UsualBeginning1;
            private StringOp Analyse=new StringOp();
            private JTextField searchTerm = new JTextField("");
            private JButton okay;
		private JButton save;
            private JTextPane results;
            private LanguagePack Text;
            private String[] captions;
            private boolean built=false;
            private OrderedHashtable[] database;
		private JRadioButton exact;
		private JCheckBox noCase;
		private JCheckBox noMarks;
		private JCheckBox normalise;
		private boolean exact_status;
		private JTextPane fullform;
		private JTextPane nominative;
		private JTextPane genitive;
		private JPanel troparionUKP;
		private JTextPane tropToneUKvalue;
		private JTextPane tropPropUKvalue;
		private JTextPane tropTextUKvalue;
		private JPanel troparionCUP;
		private JTextPane tropToneCUvalue;
		private JTextPane tropPropCUvalue;
		private JTextPane tropTextCUvalue;
		private JPanel contacionUKP;
		private JTextPane contToneUKvalue;
		private JTextPane contPropUKvalue;
		private JTextPane contTextUKvalue;
		private JPanel contacionCUP;
		private JTextPane contToneCUvalue;
		private JTextPane contPropCUvalue;
		private JTextPane contTextCUvalue;
		private JPanel life;
		private JTextPane lifeSource;
		private JTextPane lifeCopyright;
		private JTextPane lifeText;
		private HandlerXML saintCU;
		private HandlerXML saintUK;
		private HandlerXML saintUKUK;
		private Document saintCUdoc;
		private Document saintUKdoc;
		private Document saintUKUKdoc;
                private String filepathCUa="Ponomar\\languages\\cu\\xml\\lives\\";
		private String filepathUKa="Ponomar\\languages\\cu\\uk\\xml\\lives\\";
		private String filepathUKUKa="Ponomar\\languages\\cu\\uk\\uk\\xml\\lives\\";
            private OrderedHashtable corrections;
            private int countCorr;
		
		
	public EditCommemoration(OrderedHashtable dayInfo)
	{
		//We will only worry ourself about a special case here as a general version is not feasible, as there are just too many variables:
		//I will assume I want to edit the Ukrainian life, the troparia and contacia, as well as the last two in Church Slavonic.
            Analyse.dayInfo = dayInfo;
        Text = new LanguagePack(dayInfo);
        captions = new String[]{"Edit Commemoration","CCId", "Enter CCId Value.","LOAD","Full Form (Genitive):","Short Form (Nominative)", "Short From (Genitive)","Tone","Proper","Text","Source for Life","Copyright for Life","Text of Life","Troparion","Contacion","Ukrainian","Church Slavonic","SAVE"};//Text.obtainValues((String) Text.Phrases.get("Search"));
        setTitle(captions[0]);

//Create the Interface:


        JPanel top = new JPanel();
        top.setLayout(new GridLayout(4,3));
	  JLabel text =new JLabel(captions[1],SwingConstants.RIGHT);
	 top.add(text);
	searchTerm.setEditable(true);
        searchTerm.setText(captions[2]);
        top.add(searchTerm);
	okay=new JButton(captions[3]);
         okay.addActionListener(this);
         //okay.setFont(CurrentFont); NEEDS TO BE IMPLEMENTED IN THE FINAL VERSION
         top.add(okay);
	JLabel textab =new JLabel(captions[4],SwingConstants.RIGHT);
	top.add(textab);
	fullform=new JTextPane();
	fullform.setContentType("text/html; charset=UTF-8");
        fullform.setText(captions[4]);
	 fullform.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll = new JScrollPane(fullform);
	  scroll.setPreferredSize( new Dimension( 50, 50 ) );
        top.add(scroll);
	top.add(new JLabel("",SwingConstants.RIGHT));
	JLabel textac =new JLabel(captions[5],SwingConstants.RIGHT);
	top.add(textac);
	nominative=new JTextPane();
	nominative.setContentType("text/html; charset=UTF-8");
        nominative.setText(captions[5]);
	 nominative.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll1 = new JScrollPane(nominative);
	  scroll1.setPreferredSize( new Dimension( 50, 50 ) );
        top.add(scroll1);
	top.add(new JLabel("",SwingConstants.RIGHT));

	JLabel textae =new JLabel(captions[6],SwingConstants.RIGHT);
	top.add(textae);
	genitive=new JTextPane();
	genitive.setContentType("text/html; charset=UTF-8");
        genitive.setText(captions[6]);
	 genitive.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll2 = new JScrollPane(genitive);
	  scroll2.setPreferredSize( new Dimension( 50, 50 ) );
        top.add(scroll2);
	top.add(new JLabel("",SwingConstants.RIGHT));
	
	troparionUKP = new JPanel();
        troparionUKP.setLayout(new GridLayout(3,2));
	JLabel tropToneUK=new JLabel(captions[7]+" for the " +captions[13] + " in "+captions[15]);
	troparionUKP.add(tropToneUK);
	tropToneUKvalue = new JTextPane();
	 tropToneUKvalue.setText("Tone of the Troparion in Ukrainian Goes Here.");
	 tropToneUKvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll3 = new JScrollPane(tropToneUKvalue);
	  scroll3.setPreferredSize( new Dimension( 50, 50 ) );
        troparionUKP.add(scroll3);

	JLabel tropPropUK=new JLabel(captions[8]+" for the " +captions[13] + " in "+captions[15]);
	troparionUKP.add(tropPropUK);
	tropPropUKvalue = new JTextPane();
	 tropPropUKvalue.setText("Proper of the Troparion in Ukrainian Goes Here.");
	 tropPropUKvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll4 = new JScrollPane(tropPropUKvalue);
	  scroll4.setPreferredSize( new Dimension( 50, 50 ) );
        troparionUKP.add(scroll4);

	JLabel tropTextUK=new JLabel(captions[9]+" for the " +captions[13] + " in "+captions[15]);
	troparionUKP.add(tropTextUK);
	tropTextUKvalue = new JTextPane();
	 tropTextUKvalue.setText("Text of the Troparion in Ukrainian Goes Here.");
	 tropTextUKvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll5 = new JScrollPane(tropTextUKvalue);
	  scroll5.setPreferredSize( new Dimension( 50, 50 ) );
        troparionUKP.add(scroll5);
	
//Repeat for the Troparion in Church Slavonic
troparionCUP = new JPanel();
       troparionCUP.setLayout(new GridLayout(3,2));
	JLabel tropToneCU=new JLabel(captions[7]+" for the " +captions[13] + " in "+captions[16]);
	troparionCUP.add(tropToneCU);
	tropToneCUvalue = new JTextPane();
	 tropToneCUvalue.setText("Tone of the Troparion in Church Slavonic Goes Here.");
	 tropToneCUvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll6 = new JScrollPane(tropToneCUvalue);
	  scroll6.setPreferredSize( new Dimension( 50, 50 ) );
        troparionCUP.add(scroll6);

	JLabel tropPropCU=new JLabel(captions[8]+" for the " +captions[13] + " in "+captions[16]);
	troparionCUP.add(tropPropCU);
	tropPropCUvalue = new JTextPane();
	 tropPropCUvalue.setText("Proper of the Troparion in Church Slavonic Goes Here.");
	 tropPropCUvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll7 = new JScrollPane(tropPropCUvalue);
	  scroll7.setPreferredSize( new Dimension( 50, 50 ) );
        troparionCUP.add(scroll7);

	JLabel tropTextCU=new JLabel(captions[9]+" for the " +captions[13] + " in "+captions[16]);
	troparionCUP.add(tropTextCU);
	tropTextCUvalue = new JTextPane();
	 tropTextCUvalue.setText("Text of the Troparion in Church Slavonic Goes Here.");
	 tropTextCUvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll8 = new JScrollPane(tropTextCUvalue);
	  scroll8.setPreferredSize( new Dimension( 50, 50 ) );
        troparionCUP.add(scroll8);

//Contacion
contacionUKP = new JPanel();
        contacionUKP.setLayout(new GridLayout(3,2));
	JLabel contToneUK=new JLabel(captions[7]+" for the " +captions[14] + " in "+captions[15]);
	contacionUKP.add(contToneUK);
	contToneUKvalue = new JTextPane();
	 contToneUKvalue.setText("Tone of the Contacion in Ukrainian Goes Here.");
	 contToneUKvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll23 = new JScrollPane(contToneUKvalue);
	  scroll23.setPreferredSize( new Dimension( 50, 50 ) );
        contacionUKP.add(scroll23);

	JLabel contPropUK=new JLabel(captions[8]+" for the " +captions[14] + " in "+captions[15]);
	contacionUKP.add(contPropUK);
	contPropUKvalue = new JTextPane();
	 contPropUKvalue.setText("Proper of the Contacion in Ukrainian Goes Here.");
	 contPropUKvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll24 = new JScrollPane(contPropUKvalue);
	  scroll24.setPreferredSize( new Dimension( 50, 50 ) );
        contacionUKP.add(scroll24);

	JLabel contTextUK=new JLabel(captions[9]+" for the " +captions[14] + " in "+captions[15]);
	contacionUKP.add(contTextUK);
	contTextUKvalue = new JTextPane();
	 contTextUKvalue.setText("Text of the Contacion in Ukrainian Goes Here.");
	 contTextUKvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll25 = new JScrollPane(contTextUKvalue);
	  scroll25.setPreferredSize( new Dimension( 50, 50 ) );
        contacionUKP.add(scroll25);
	
//Repeat for the Contacion in Church Slavonic
contacionCUP = new JPanel();
       contacionCUP.setLayout(new GridLayout(3,2));
	JLabel contToneCU=new JLabel(captions[7]+" for the " +captions[14] + " in "+captions[16]);
	contacionCUP.add(contToneCU);
	contToneCUvalue = new JTextPane();
	 contToneCUvalue.setText("Tone of the Contacion in Church Slavonic Goes Here.");
	 contToneCUvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll26 = new JScrollPane(contToneCUvalue);
	  scroll26.setPreferredSize( new Dimension( 50, 50 ) );
        contacionCUP.add(scroll26);

	JLabel contPropCU=new JLabel(captions[8]+" for the " +captions[14] + " in "+captions[16]);
	contacionCUP.add(contPropCU);
	contPropCUvalue = new JTextPane();
	 contPropCUvalue.setText("Proper of the Contacion in Church Slavonic Goes Here.");
	 contPropCUvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll27 = new JScrollPane(contPropCUvalue);
	  scroll27.setPreferredSize( new Dimension( 50, 50 ) );
        contacionCUP.add(scroll27);

	JLabel contTextCU=new JLabel(captions[9]+" for the " +captions[14] + " in "+captions[16]);
	contacionCUP.add(contTextCU);
	contTextCUvalue = new JTextPane();
	 contTextCUvalue.setText("Text of the Contacion in Church Slavonic Goes Here.");
	 contTextCUvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll28 = new JScrollPane(contTextCUvalue);
	  scroll28.setPreferredSize( new Dimension( 50, 50 ) );
        contacionCUP.add(scroll28);
//The life itself
	life = new JPanel();
       life.setLayout(new GridLayout(3,2));
	JLabel lifeSource1=new JLabel(captions[10]);
	life.add(lifeSource1);
	lifeSource = new JTextPane();
	 lifeSource.setText("Source of Life goes here.");
	 lifeSource.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll36 = new JScrollPane(lifeSource);
	  scroll36.setPreferredSize( new Dimension( 50, 50 ) );
        life.add(scroll36);

	JLabel lifeCopyright2=new JLabel(captions[11]);
	life.add(lifeCopyright2);
	lifeCopyright = new JTextPane();
	 lifeCopyright.setText("Copyright Information goes here");
	 lifeCopyright.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll37 = new JScrollPane(lifeCopyright);
	  scroll37.setPreferredSize( new Dimension( 50, 50 ) );
        life.add(scroll37);

	JLabel lifeText1=new JLabel(captions[12]);
	life.add(lifeText1);
	lifeText = new JTextPane();
	 lifeText.setText("Text of the Life Goes Here.");
	 lifeText.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll38 = new JScrollPane(lifeText);
	  scroll38.setPreferredSize( new Dimension( 50, 50 ) );
        life.add(scroll38);

	save=new JButton(captions[17]);
      save.addActionListener(this);





         JPanel bottom = new JPanel();
		bottom.setLayout(new GridLayout(3,2));
		bottom.add(troparionUKP);
		bottom.add(troparionCUP);
		bottom.add(contacionUKP);
		bottom.add(contacionCUP);
		bottom.add(life);
		bottom.add(save);

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitter.setTopComponent(top);
        splitter.setBottomComponent(bottom);

        add(splitter);

        //Adding a Menu Bar
       /* MenuFiles demo = new MenuFiles(Analyse.dayInfo.clone());
        JMenuBar MenuBar = new JMenuBar();
        MenuBar.add(demo.createFileMenu(this));
        MenuBar.add(demo.createHelpMenu(this));
        MenuBar.setFont(CurrentFont);
        setJMenuBar(MenuBar);
*/

        pack();
        setSize(900, 800);
        setVisible(true);

        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Remove in the final version

		 	
		 	
	}
	public String getUsualBeginning()
	{
		return UsualBeginning1;
	}

         public void actionPerformed(ActionEvent e)
  {
        //JMenuItem source = (JMenuItem)(e.getSource());
        //String name = source.getText();

        //Helpers helper = new Helpers(Analyse.dayInfo);
        String name = e.getActionCommand();
        //ALLOWS A MULTILINGUAL PROPER VERSION
	//System.out.println(name);
	if (name.equals(captions[3])){
		//We will need three languages: cu, cu/uk, and cu/uk/uk.
		String CId=searchTerm.getText();
//C:\\Users\\yuris\\Documents\\Ponomar\\ponomar-combo\\
		String filepathCU=filepathCUa+CId+".xml";
		String filepathUK=filepathUKa+CId+".xml";
		String filepathUKUK=filepathUKUKa+CId+".xml";
            saintCU=new HandlerXML(filepathCU);
            saintCUdoc=saintCU.readXML();
		NodeList list=saintCUdoc.getElementsByTagName("NAME");
		
		saintUK=new HandlerXML(filepathUK);
            saintUKdoc=saintUK.readXML();	
		NodeList listUK=saintUKdoc.getElementsByTagName("NAME");

		saintUKUK=new HandlerXML(filepathUKUK);
            saintUKUKdoc=saintUKUK.readXML();
		NodeList listUKUK=saintUKUKdoc.getElementsByTagName("NAME");
		//Set default values in case nothing is to be found
		tropPropUKvalue.setText("");
		tropToneUKvalue.setText("");
		tropTextUKvalue.setText("");
		tropPropCUvalue.setText("");
		tropToneCUvalue.setText("");
		tropTextCUvalue.setText("");
		contPropUKvalue.setText("");
		contToneUKvalue.setText("");
		contTextUKvalue.setText("");
		contPropCUvalue.setText("");
		contToneCUvalue.setText("");
		contTextCUvalue.setText("");
		lifeSource.setText("Луцик, 2013");
		lifeCopyright.setText("І. Я. Луцик, «Житія святих, пам'ять яких Українська Греко-Католицька Церква кожного дня впродовж року поминає». Львів, Видавництво «Свічадо», 2013");
		lifeText.setText("");

		
		if (listUK.item(0).getAttributes().getNamedItem("Nominative")!=null)
		{
			fullform.setText(listUK.item(0).getAttributes().getNamedItem("Nominative").getTextContent()); //Change after testing.
		}else
		{
			fullform.setText(listUK.item(1).getAttributes().getNamedItem("Nominative").getTextContent());
		}
		nominative.setText(listUK.item(0).getAttributes().getNamedItem("Short").getTextContent());
		genitive.setText(listUK.item(0).getAttributes().getNamedItem("Genitive").getTextContent());

		//Ukrainian Troparion
		NodeList listT=saintUKUKdoc.getElementsByTagName("TROPARION");
		if (listT.getLength()>0)
		{
			tropToneUKvalue.setText(listT.item(0).getAttributes().getNamedItem("Tone").getTextContent());
			if (listT.item(0).getAttributes().getNamedItem("Podoben")!=null)
			{
				tropPropUKvalue.setText(listT.item(0).getAttributes().getNamedItem("Podoben").getTextContent());
			}else
			{
				tropPropUKvalue.setText("");
			}
			tropTextUKvalue.setText(listT.item(0).getTextContent());
		}

		//Church Slavonic Troparion
		listT=saintCUdoc.getElementsByTagName("TROPARION");
		if (listT.getLength()>0)
		{
			tropToneCUvalue.setText(listT.item(0).getAttributes().getNamedItem("Tone").getTextContent());
			if (listT.item(0).getAttributes().getNamedItem("Podoben")!=null)
			{
				tropPropCUvalue.setText(listT.item(0).getAttributes().getNamedItem("Podoben").getTextContent());
			}else
			{
				tropPropCUvalue.setText("");
			}
			tropTextCUvalue.setText(listT.item(0).getTextContent());
		}

		//Ukrainian Contacion
		listT=saintUKUKdoc.getElementsByTagName("KONTAKION");
		if (listT.getLength()>0)
		{
			contToneUKvalue.setText(listT.item(0).getAttributes().getNamedItem("Tone").getTextContent());
			if (listT.item(0).getAttributes().getNamedItem("Podoben")!=null)
			{
				contPropUKvalue.setText(listT.item(0).getAttributes().getNamedItem("Podoben").getTextContent());
			}else
			{
				contPropUKvalue.setText("");
			}
			contTextUKvalue.setText(listT.item(0).getTextContent());
		}

		//Church Slavonic Contacion
		listT=saintCUdoc.getElementsByTagName("KONTAKION");
		if (listT.getLength()>0)
		{
			contToneCUvalue.setText(listT.item(0).getAttributes().getNamedItem("Tone").getTextContent());
			if (listT.item(0).getAttributes().getNamedItem("Podoben")!=null)
			{
				contPropCUvalue.setText(listT.item(0).getAttributes().getNamedItem("Podoben").getTextContent());
			}else
			{
				contPropCUvalue.setText("");
			}
			contTextCUvalue.setText(listT.item(0).getTextContent());
		}

		//LIFE
		NodeList listL=saintUKdoc.getElementsByTagName("LIFE");
		if (listL.getLength()>0)
		{
			lifeSource.setText(listL.item(0).getAttributes().getNamedItem("Id").getTextContent());
			lifeCopyright.setText(listL.item(0).getAttributes().getNamedItem("Copyright").getTextContent());
			lifeText.setText(listL.item(0).getTextContent());
		}
		
		

//09497
		return;
	}
	if (name.equals(captions[17])){
        //We will now need to save the information as necessary.
        //Check if life data exists
        String CId=searchTerm.getText();
//C:\\Users\\yuris\\Documents\\Ponomar\\ponomar-combo\\
		String filepathCU=filepathCUa+CId+".xml";
		String filepathUK=filepathUKa+CId+".xml";
		String filepathUKUK=filepathUKUKa+CId+".xml";
                
               //Update Correction File
               try{
               readCorrections(new FileInputStream(new File("Ponomar\\languages\\xml\\Commands\\Changes.txt")));
               //System.out.println(corrections.get(5));
               //System.out.println(countCorr);
               }catch (IOException ed)
               {
                   ed.printStackTrace();
               }
               
                
        String lifetext=correctText(lifeText.getText());
        //We need to make further corrections in the text:
        if (lifetext.contains("\n\n")){
            lifetext="<p>"+lifetext.replaceAll("\n\n", "</p><p>")+"</p>";
        }
        //saintUKUKdoc
        //saintCUdoc
        if (!lifetext.isEmpty())
        {
            //Update Life
            NodeList listL=saintUKdoc.getElementsByTagName("LIFE");
            if (listL.getLength()>0)
		{
                listL.item(0).getAttributes().getNamedItem("Id").setTextContent(lifeSource.getText());
                listL.item(0).getAttributes().getNamedItem("Copyright").setTextContent(lifeCopyright.getText());
                listL.item(0).setTextContent(lifetext);
            
                }else
            {
                //We need to create the LIFE node
                Element newLife=saintUKdoc.createElement("LIFE");
                newLife.setAttribute("Id", lifeSource.getText());
                newLife.setAttribute("Copyright", lifeCopyright.getText());
                newLife.setTextContent(lifetext);
                saintUKdoc.getDocumentElement().appendChild(newLife);
            }
            //Save updated File
            try (FileOutputStream output= new FileOutputStream(filepathUK)){
                HandlerXML.writeXml(saintUKdoc,output);
            } catch (IOException | TransformerException e3){
                e3.printStackTrace();
            }
            
        }
        
            
            //System.out.println("I will save the files");
		//return;
	
	if (!tropTextUKvalue.getText().isEmpty() || !contTextUKvalue.getText().isEmpty()){
            //Update Troparion and Contacion for Ukrainian
            NodeList listT=saintUKUKdoc.getElementsByTagName("TROPARION");
		if (listT.getLength()>0)
		{
			listT.item(0).getAttributes().getNamedItem("Tone").setTextContent(tropToneUKvalue.getText());
			if (!tropPropUKvalue.getText().isEmpty())
			{
				//listT.item(0).getAttributes().getNamedItem("Podoben").setTextContent(tropPropUKvalue.getText());
                            Element el=(Element)listT.item(0);//.getAttributes().getNamedItem("Podoben").getNodeValue();
                               el.setAttribute("Podoben", tropPropUKvalue.getText());
			}else
			{
				
                                //listT.item(0).getAttributes().getNamedItem("Podoben").setTextContent(tropPropUKvalue.getText());
			}
			listT.item(0).setTextContent(correctText(tropTextUKvalue.getText()));
		}else
                {
                    //Create our new Elements
                    Element newTroparion = saintUKUKdoc.createElement("TROPARION");
                    newTroparion.setAttribute("Tone",tropToneUKvalue.getText());
                    if (!tropPropUKvalue.getText().isEmpty())
			{
				newTroparion.setAttribute("Podoben",tropPropUKvalue.getText());
			}
                    newTroparion.setAttribute("Type","1");
                    newTroparion.setTextContent(correctText(tropTextUKvalue.getText()));
                    
//                    Element newContacion = saintUKUKdoc.createElement("KONTAKION");
//                    newContacion.setAttribute("Tone",contToneUKvalue.getText());
//                    if (contPropUKvalue.getText()!="")
//			{
//				newContacion.setAttribute("Podoben",contPropUKvalue.getText());
//			}
//                    newContacion.setTextContent(contTextUKvalue.getText());
                    
                    NodeList listS=saintUKUKdoc.getElementsByTagName("LITURGY");
                    if (listS.getLength()>0){
                        //We just need to add the troparion/contacion
                        listS.item(0).appendChild(newTroparion);
                        //listS.item(0).appendChild(newContacion);
                    }else
                    {
                        Element newService=saintUKUKdoc.createElement("SERVICE");
                        Element newLiturgy=saintUKUKdoc.createElement("LITURGY");
                        newLiturgy.appendChild(newTroparion);
                        newService.appendChild(newLiturgy);
                       // newService.appendChild(newContacion);
                        saintUKUKdoc.getDocumentElement().appendChild(newService);
                    }
                }
                
                NodeList listC=saintUKUKdoc.getElementsByTagName("KONTAKION");
		if (listC.getLength()>0)
		{
			listC.item(0).getAttributes().getNamedItem("Tone").setTextContent(contToneUKvalue.getText());
			if (contPropUKvalue.getText().isEmpty())
                        {
                            //listT.item(0).getAttributes().getNamedItem("Podoben").setTextContent(contPropUKvalue.getText());
                        }else
                        {
                            //listC.item(0).getAttributes().getNamedItem("Podoben").setTextContent(contPropUKvalue.getText());
                            Element el=(Element)listC.item(0);//.getAttributes().getNamedItem("Podoben").getNodeValue();
                            el.setAttribute("Podoben", contPropUKvalue.getText());
                        }
			listC.item(0).setTextContent(correctText(contTextUKvalue.getText()));
		}else
                {
                    //Create our new Elements
                    Element newContacion = saintUKUKdoc.createElement("KONTAKION");
                    newContacion.setAttribute("Tone",contToneUKvalue.getText());
                    if (!contPropUKvalue.getText().isEmpty())
			{
				newContacion.setAttribute("Podoben",contPropUKvalue.getText());
                                
			}
                    newContacion.setAttribute("Type","1");
                    newContacion.setTextContent(correctText(contTextUKvalue.getText()));
                    
                    NodeList listS=saintUKUKdoc.getElementsByTagName("LITURGY");
                    if (listS.getLength()>0){
                        //We just need to add the troparion/contacion
                    //    listS.item(0).appendChild(newTroparion);
                        listS.item(0).appendChild(newContacion);
                    }else
                    {
                        Element newService=saintUKUKdoc.createElement("SERVICE");
                        Element newLiturgy=saintUKUKdoc.createElement("LITURGY");
                      //  newService.appendChild(newTroparion);
                        newLiturgy.appendChild(newContacion);
                        newService.appendChild(newLiturgy);
                        saintUKUKdoc.getDocumentElement().appendChild(newService);
                    }
                }
            
        }
        try (FileOutputStream output= new FileOutputStream(filepathUKUK)){
                HandlerXML.writeXml(saintUKUKdoc,output);
            } catch (IOException | TransformerException e3){
                e3.printStackTrace();
            }
        //And now for Church Slavonic
        if (!tropTextCUvalue.getText().isEmpty() || !contTextCUvalue.getText().isEmpty()){
            //Update Troparion and Contacion for Church Slavonic
            NodeList listT=saintCUdoc.getElementsByTagName("TROPARION");
		if (listT.getLength()>0)
		{
			listT.item(0).getAttributes().getNamedItem("Tone").setTextContent(tropToneCUvalue.getText());
			if (!tropPropCUvalue.getText().isEmpty())
			{
				//listT.item(0).getAttributes().getNamedItem("Podoben").setTextContent(tropPropCUvalue.getText());
                                Element el=(Element)listT.item(0);//.getAttributes().getNamedItem("Podoben").getNodeValue();
                               el.setAttribute("Podoben", tropPropCUvalue.getText());
			}else
			{
				//listT.item(0).getAttributes().getNamedItem("Podoben").setTextContent(tropPropCUvalue.getText());
			}
			listT.item(0).setTextContent(tropTextCUvalue.getText());
		}else
                {
                    //Create our new Elements
                    Element newTroparion = saintCUdoc.createElement("TROPARION");
                    newTroparion.setAttribute("Tone",tropToneCUvalue.getText());
                    if (!tropPropCUvalue.getText().isEmpty())
			{
				newTroparion.setAttribute("Podoben",tropPropCUvalue.getText());
			}
                    newTroparion.setAttribute("Type","1");
                    newTroparion.setTextContent(tropTextCUvalue.getText());
                    
//                    Element newContacion = saintCUdoc.createElement("KONTAKION");
//                    newContacion.setAttribute("Tone",contToneCUvalue.getText());
//                    if (contPropCUvalue.getText()!="")
//			{
//				newContacion.setAttribute("Podoben",contPropCUvalue.getText());
//			}
//                    newContacion.setTextContent(contTextCUvalue.getText());
                    
                    NodeList listS=saintCUdoc.getElementsByTagName("LITURGY");
                    if (listS.getLength()>0){
                        //We just need to add the troparion/contacion
                        listS.item(0).appendChild(newTroparion);
                        //listS.item(0).appendChild(newContacion);
                    }else
                    {
                        Element newService=saintCUdoc.createElement("SERVICE");
                        Element newLiturgy=saintCUdoc.createElement("LITURGY");
                        newLiturgy.appendChild(newTroparion);
                        newService.appendChild(newLiturgy);
                       // newService.appendChild(newContacion);
                        saintCUdoc.getDocumentElement().appendChild(newService);
                    }
                }
                
                NodeList listC=saintCUdoc.getElementsByTagName("KONTAKION");
		if (listC.getLength()>0)
		{
			listC.item(0).getAttributes().getNamedItem("Tone").setTextContent(contToneCUvalue.getText());
			if (!contPropCUvalue.getText().isEmpty())
			{
                           /* if (listC.item(0).getAttributes().getNamedItem("Podoben")!=null){
                            listC.item(0).getAttributes().getNamedItem("Podoben").setTextContent(contPropCUvalue.getText());
                            }else{
                              */  Element el=(Element)listC.item(0);//.getAttributes().getNamedItem("Podoben").getNodeValue();
                               el.setAttribute("Podoben", contPropCUvalue.getText());
                            //}
			}else
			{
				//listT.item(0).getAttributes().getNamedItem("Podoben").setTextContent(contPropCUvalue.getText());
			}
			listC.item(0).setTextContent(contTextCUvalue.getText());
		}else
                {
                    //Create our new Elements
                    Element newContacion = saintCUdoc.createElement("KONTAKION");
                    newContacion.setAttribute("Tone",contToneCUvalue.getText());
                    newContacion.setAttribute("Type","1");
                    if (!contPropCUvalue.getText().isEmpty())
			{
				newContacion.setAttribute("Podoben",contPropCUvalue.getText());
			}
                    newContacion.setTextContent(contTextCUvalue.getText());
                    
                    NodeList listS=saintCUdoc.getElementsByTagName("LITURGY");
                    if (listS.getLength()>0){
                        //We just need to add the troparion/contacion
                    //    listS.item(0).appendChild(newTroparion);
                        listS.item(0).appendChild(newContacion);
                    }else
                    {
                        Element newService=saintCUdoc.createElement("SERVICE");
                        Element newLiturgy=saintCUdoc.createElement("LITURGY");
                        newLiturgy.appendChild(newContacion);
                        newService.appendChild(newLiturgy);
                      //  newService.appendChild(newTroparion);
                       // newLiturgy.appendChild(newContacion);
                        saintCUdoc.getDocumentElement().appendChild(newService);
                    }
                }
            
        }
        try (FileOutputStream output= new FileOutputStream(filepathCU)){
                HandlerXML.writeXml(saintCUdoc,output);
            } catch (IOException | TransformerException e3){
                e3.printStackTrace();
            }
        System.out.println("Finished saving files associated with CId="+CId);
    }
        
        
        


    }
         private void readCorrections(InputStream inputStream)
  throws IOException {
    corrections=new OrderedHashtable();
    countCorr=0;
    try (BufferedReader br
      = new BufferedReader(new InputStreamReader(inputStream,StandardCharsets.UTF_8))) {
        String line;
        while ((line = br.readLine()) != null) {
            //String[] parts=line.split(";");
            corrections.put(countCorr,line);
            countCorr=countCorr+1;
            //resultStringBuilder.append(line).append("\n");
        }
    }
  countCorr=countCorr-1;
}
         private String correctText(String text){
             String correctedText=text;
                     for(int i=0;i<=countCorr;i++){
             String texts=(String)corrections.get(i);
             String[] replacement=texts.split(";");
             correctedText=correctedText.replaceAll(replacement[0], replacement[1]);
             //System.out.println(replacement[0]+" "+replacement[1]);
         }
             
             return correctedText;
         }
        
        public static void main(String[] argz)
        {
            OrderedHashtable dayinfo=new OrderedHashtable();
            dayinfo.put("LS","fr/");
            Search testing = new Search(dayinfo);
            

        }
}

