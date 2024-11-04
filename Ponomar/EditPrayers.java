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

(C) 2024 YURI SHARDT. ALL RIGHTS RESERVED.


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
public class EditPrayers extends JFrame implements ActionListener
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
                private String filepathCUa="Ponomar\\languages\\cu\\xml\\Services\\Command";
                private String filepathCUb="Ponomar\\languages\\cu\\xml\\Services\\CommonPrayers";
                private String filepathCUc="Ponomar\\languages\\cu\\xml\\Services\\Text";
		private String filepathUKa="Ponomar\\languages\\cu\\uk\\uk\\xml\\Services\\Command";
		private String filepathUKb="Ponomar\\languages\\cu\\uk\\uk\\xml\\Services\\CommonPrayers";
		private String filepathUKc="Ponomar\\languages\\cu\\uk\\uk\\xml\\Services\\Text";
                
                private String filepathCUd="Ponomar\\languages\\cu\\xml\\Services\\CommonPrayers\\TROPARION";
                private String filepathUKd="Ponomar\\languages\\cu\\uk\\uk\\xml\\Services\\CommonPrayers\\TROPARION";
                private String filepathCUe="Ponomar\\languages\\cu\\xml\\Services\\CommonPrayers\\KONTAKION";
                private String filepathUKe="Ponomar\\languages\\cu\\uk\\uk\\xml\\Services\\CommonPrayers\\KONTAKION";
		//private String filepathUKUKa="Ponomar\\languages\\cu\\uk\\uk\\xml\\lives\\";
            private OrderedHashtable corrections;
            private OrderedHashtable fileName;
            private OrderedHashtable filePath;
            private OrderedHashtable filePathCU;
            private JComboBox combobox;
            private int countCorr;
            
            private String filepathUKUKa="";
		
//Allows the prayers to be quickly edited and made correct. As with the saints, it is limited to only Ukrainian and Church Slavonic, as the Church Slavonic needs correction from place to place. It is to be made equivalent to the Rusyn version.
		
	public EditPrayers(OrderedHashtable dayInfo)
	{
		//We will only worry ourself about a special case here as a general version is not feasible, as there are just too many variables:
		//I will assume I want to edit the Ukrainian life, the troparia and contacia, as well as the last two in Church Slavonic.
            Analyse.dayInfo = dayInfo;
        Text = new LanguagePack(dayInfo);
//        captions = new String[]{"Edit Commemoration","CCId", "Enter CCId Value.","LOAD","Full Form (Genitive):","Short Form (Nominative)", "Short From (Genitive)","Tone","Proper","Text","Source for Life","Copyright for Life","Text of Life","Troparion","Contacion","Ukrainian","Church Slavonic","SAVE"};//Text.obtainValues((String) Text.Phrases.get("Search"));
	captions=new String[]{"Edit Prayers","Load","Header for the Prayer","Text of the Prayer","Ukrainian","Church Slavonic","Save"};
        setTitle(captions[0]);

//Create the Interface:

//Read all files in Ukrainian that exist (these are to be updated, as they are currently in Church Slavonic). There are three paths to consider
    File folder= new File(filepathUKa);
    //System.out.println(filepathUKa);
    fileName=new OrderedHashtable();
    filePath=new OrderedHashtable();
    filePathCU=new OrderedHashtable();
    int count = 1;
    fileName.put(0,"");
    filePath.put(0,"");
    filePathCU.put(0,"");

    for (File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
            //do nothing
        } else {
            //System.out.println(fileEntry.getName());
            fileName.put(count,fileEntry.getName());
            filePath.put(count,fileEntry.getPath());
            filePathCU.put(count,filepathCUa+"\\"+fileEntry.getName());
            count=count+1;
        }
    }
    
    folder= new File(filepathUKb);
    for (File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
            //do nothing
        } else {
            //System.out.println(fileEntry.getName());
            fileName.put(count,fileEntry.getName());
            filePath.put(count,fileEntry.getPath());
            filePathCU.put(count,filepathCUb+"\\"+fileEntry.getName());
            count=count+1;
        }
    }
    folder= new File(filepathUKc);
    for (File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
            //do nothing
        } else {
            //System.out.println(fileEntry.getName());
            fileName.put(count,fileEntry.getName());
            filePath.put(count,fileEntry.getPath());
            filePathCU.put(count,filepathCUc+"\\"+fileEntry.getName());
            count=count+1;
        }
    }
    
    /*folder= new File(filepathUKd);
    for (File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
            //do nothing
        } else {
            //System.out.println(fileEntry.getName());
            fileName.put(count,fileEntry.getName());
            filePath.put(count,fileEntry.getPath());
            filePathCU.put(count,filepathCUd+"\\"+fileEntry.getName());
            count=count+1;
        }
    }*/
    folder= new File(filepathUKe);
    for (File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
            //do nothing
        } else {
            //System.out.println(fileEntry.getName());
            fileName.put(count,fileEntry.getName());
            filePath.put(count,fileEntry.getPath());
            filePathCU.put(count,filepathCUe+"\\"+fileEntry.getName());
            count=count+1;
        }
    }
    count=count-1;

    //All required files have been read!
    //Create interface
    
        JPanel top = new JPanel();
        top.setLayout(new GridLayout(1,2));
	/*  JLabel text =new JLabel(captions[1],SwingConstants.RIGHT);
	 top.add(text);
	searchTerm.setEditable(true);
        
        searchTerm.setText(captions[2]);
        top.add(searchTerm);
	*/
        //Add the required combobox to select which file I wish to modify.
        //System.out.println(count);
        //System.out.println(fileName.get(0));
        String[] choices= new String[count+1]; //{fileName.get(0).toString()};
        choices[0]="";
        for (int i=1;i<=count;i++){
            //System.out.println("Testing: " + i +" out of "+ count);
            //System.out.println(fileName.size());
            choices[i]=fileName.get(i).toString();
        }
        combobox=new JComboBox(choices);
        top.add(combobox);
        
        okay=new JButton(captions[1]);
         okay.addActionListener(this);
         //okay.setFont(CurrentFont); NEEDS TO BE IMPLEMENTED IN THE FINAL VERSION
         top.add(okay);
	/*JLabel textab =new JLabel(captions[4],SwingConstants.RIGHT);
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
	*/
        //The Ukrainian prayer and header
	troparionUKP = new JPanel();
        troparionUKP.setLayout(new GridLayout(2,2));
	JLabel tropToneUK=new JLabel(captions[2]+" in "+captions[4]);
	troparionUKP.add(tropToneUK);
	tropToneUKvalue = new JTextPane();
	 tropToneUKvalue.setText(captions[2]+" in "+captions[4]+" goes here.");
	 tropToneUKvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll3 = new JScrollPane(tropToneUKvalue);
	  scroll3.setPreferredSize( new Dimension( 50, 50 ) );
        troparionUKP.add(scroll3);

/*	JLabel tropPropUK=new JLabel(captions[8]+" for the " +captions[13] + " in "+captions[15]);
	troparionUKP.add(tropPropUK);
	tropPropUKvalue = new JTextPane();
	 tropPropUKvalue.setText("Proper of the Troparion in Ukrainian Goes Here.");
	 tropPropUKvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll4 = new JScrollPane(tropPropUKvalue);
	  scroll4.setPreferredSize( new Dimension( 50, 50 ) );
        troparionUKP.add(scroll4);
*/
	JLabel tropTextUK=new JLabel(captions[3]+" in "+captions[4]);
	troparionUKP.add(tropTextUK);
	tropTextUKvalue = new JTextPane();
	 tropTextUKvalue.setText(captions[3]+" in "+captions[4]+" goes here.");
	 tropTextUKvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll5 = new JScrollPane(tropTextUKvalue);
	  scroll5.setPreferredSize( new Dimension( 50, 50 ) );
        troparionUKP.add(scroll5);
	
//Repeat for the Troparion in Church Slavonic
troparionCUP = new JPanel();
       troparionCUP.setLayout(new GridLayout(3,2));
	JLabel tropToneCU=new JLabel(captions[2]+" in "+captions[5]);
	troparionCUP.add(tropToneCU);
	tropToneCUvalue = new JTextPane();
	 tropToneCUvalue.setText(captions[2]+" in "+captions[5]+" goes here.");
	 tropToneCUvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll6 = new JScrollPane(tropToneCUvalue);
	  scroll6.setPreferredSize( new Dimension( 50, 50 ) );
        troparionCUP.add(scroll6);
/*
	JLabel tropPropCU=new JLabel(captions[8]+" for the " +captions[13] + " in "+captions[16]);
	troparionCUP.add(tropPropCU);
	tropPropCUvalue = new JTextPane();
	 tropPropCUvalue.setText("Proper of the Troparion in Church Slavonic Goes Here.");
	 tropPropCUvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll7 = new JScrollPane(tropPropCUvalue);
	  scroll7.setPreferredSize( new Dimension( 50, 50 ) );
        troparionCUP.add(scroll7);
*/
	JLabel tropTextCU=new JLabel(captions[3]+" in "+captions[5]);
	troparionCUP.add(tropTextCU);
	tropTextCUvalue = new JTextPane();
	 tropTextCUvalue.setText(captions[3]+" in "+captions[5]+" goes here.");
	 tropTextCUvalue.setBounds( 0, 0, 50, 50 );
        JScrollPane scroll8 = new JScrollPane(tropTextCUvalue);
	  scroll8.setPreferredSize( new Dimension( 50, 50 ) );
        troparionCUP.add(scroll8);

//Contacion
/*contacionUKP = new JPanel();
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
*/
	save=new JButton(captions[6]);
      save.addActionListener(this);





         JPanel bottom = new JPanel();
		bottom.setLayout(new GridLayout(2,2));
		bottom.add(troparionUKP);
		bottom.add(troparionCUP);
		//bottom.add(contacionUKP);
		//bottom.add(contacionCUP);
		//bottom.add(life);
                //bottom.add("");
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
	if (name.equals(captions[1])){
		//We will need two languages: cu and cu/uk/uk. We will worry about any changes at the cu/uk level later (there will be a few).
		int CId=combobox.getSelectedIndex();
                String path=filePath.get(CId).toString();
//C:\\Users\\yuris\\Documents\\Ponomar\\ponomar-combo\\
		String pathCU=filePathCU.get(CId).toString();
            saintCU=new HandlerXML(pathCU);
            saintCUdoc=saintCU.readXML();
		//NodeList list=saintCUdoc.getElementsByTagName("NAME");
		
		/*saintUK=new HandlerXML(filepathUK);
            saintUKdoc=saintUK.readXML();	
		//NodeList listUK=saintUKdoc.getElementsByTagName("NAME");
*/
		saintUKUK=new HandlerXML(path);
            saintUKUKdoc=saintUKUK.readXML();
	//	NodeList listUKUK=saintUKUKdoc.getElementsByTagName("NAME");
		//Set default values in case nothing is to be found
	//	tropPropUKvalue.setText("");
		tropToneUKvalue.setText("");
		tropTextUKvalue.setText("");
	//	tropPropCUvalue.setText("");
		tropToneCUvalue.setText("");
		tropTextCUvalue.setText("");
	/*	contPropUKvalue.setText("");
		contToneUKvalue.setText("");
		contTextUKvalue.setText("");
		contPropCUvalue.setText("");
		contToneCUvalue.setText("");
		contTextCUvalue.setText("");
		lifeSource.setText("Луцик, 2013");
		lifeCopyright.setText("І. Я. Луцик, «Житія святих, пам'ять яких Українська Греко-Католицька Церква кожного дня впродовж року поминає». Львів, Видавництво «Свічадо», 2013");
		lifeText.setText("");
*/
		
		//Ukrainian Troparion
		NodeList listT=saintUKUKdoc.getElementsByTagName("HEADER");
		if (listT.getLength()>0)
		{
			tropToneUKvalue.setText(listT.item(0).getAttributes().getNamedItem("Value").getTextContent());
			
		}
                listT=saintUKUKdoc.getElementsByTagName("TEXT");
                tropTextUKvalue.setText(listT.item(0).getAttributes().getNamedItem("Value").getTextContent());

		//Church Slavonic Troparion
		listT=saintCUdoc.getElementsByTagName("HEADER");
		if (listT.getLength()>0)
		{
			tropToneCUvalue.setText(listT.item(0).getAttributes().getNamedItem("Value").getTextContent());
			
		}
                listT=saintCUdoc.getElementsByTagName("TEXT");
                tropTextCUvalue.setText(listT.item(0).getAttributes().getNamedItem("Value").getTextContent());

		return;
	}
	if (name.equals(captions[6])){
        //We will now need to save the information as necessary.
        //Check if life data exists
        
        int CId=combobox.getSelectedIndex();
        if (CId==0){
            return;
        }
                String path=filePath.get(CId).toString();
		String pathCU=filePathCU.get(CId).toString();
                
               //Update Correction File
               try{
               readCorrections(new FileInputStream(new File("Ponomar\\languages\\xml\\Commands\\Changes.txt")));
               //System.out.println(corrections.get(5));
               //System.out.println(countCorr);
               }catch (IOException ed)
               {
                   ed.printStackTrace();
               }
               
                
        
        
            
            
	
	if (!tropTextUKvalue.getText().isEmpty()){
            //Update Prayer for Ukrainian
            NodeList listT=saintUKUKdoc.getElementsByTagName("HEADER");
		if (listT.getLength()>0)
		{
			listT.item(0).getAttributes().getNamedItem("Value").setTextContent(tropToneUKvalue.getText());
                }else
                {
                    Element newTroparion = saintUKUKdoc.createElement("HEADER");
                    newTroparion.setAttribute("Value",tropToneUKvalue.getText());
                    saintUKUKdoc.getDocumentElement().appendChild(newTroparion);
                }
		listT=saintUKUKdoc.getElementsByTagName("TEXT");
                listT.item(0).getAttributes().getNamedItem("Value").setTextContent(correctText(tropTextUKvalue.getText()));
		              
        }
        
        try (FileOutputStream output= new FileOutputStream(path)){
                HandlerXML.writeXml(saintUKUKdoc,output);
            } catch (IOException | TransformerException e3){
                e3.printStackTrace();
            }
        //And now for Church Slavonic
        if (!tropTextUKvalue.getText().isEmpty()){
            //Update Prayer for Church Slavonic
            NodeList listT=saintCUdoc.getElementsByTagName("HEADER");
		if (listT.getLength()>0)
		{
			listT.item(0).getAttributes().getNamedItem("Value").setTextContent(tropToneCUvalue.getText());
                }else
                {
                    Element newTroparion = saintCUdoc.createElement("HEADER");
                    newTroparion.setAttribute("Value",tropToneCUvalue.getText());
                    saintCUdoc.getDocumentElement().appendChild(newTroparion);
                }
		listT=saintCUdoc.getElementsByTagName("TEXT");
                listT.item(0).getAttributes().getNamedItem("Value").setTextContent(correctText(tropTextCUvalue.getText()));
		              
        }
       
        try (FileOutputStream output= new FileOutputStream(pathCU)){
                HandlerXML.writeXml(saintCUdoc,output);
            } catch (IOException | TransformerException e3){
                e3.printStackTrace();
            }
        System.out.println("Finished saving files associated with name of "+fileName.get(CId).toString());
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

