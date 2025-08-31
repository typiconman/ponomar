package Ponomar;

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

(C) 2013, 2022 YURI SHARDT. ALL RIGHTS RESERVED.


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
public class Search extends JFrame implements ActionListener
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	
	public String UsualBeginning1;
            private StringOp Analyse=new StringOp();
            private JTextField searchTerm = new JTextField("");
            private JButton okay;
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
		
		
	public Search(OrderedHashtable dayInfo)
	{
		//Assuming at present only English exists:
            Analyse.dayInfo = dayInfo;
        Text = new LanguagePack(dayInfo);
        captions = Text.obtainValues((String) Text.Phrases.get("Search"));
        setTitle(captions[0]);

//        LanguagePack getLang = new LanguagePack(Analyse.dayInfo);


        JPanel top = new JPanel();
        JPanel bottom = new JPanel();
        top.setLayout(new GridLayout(3,2));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.PAGE_AXIS));

        JLabel text =new JLabel(captions[1],SwingConstants.RIGHT);
        top.add(text);
        searchTerm.setEditable(true);
        searchTerm.setText(captions[2]);
        top.add(searchTerm);
        
		//Type of search: x exact | x ignore case x ignore diacriticals x normalise
	JPanel searchtype= new JPanel();
	searchtype.setLayout(new GridLayout(2,2));
	JLabel text3 =new JLabel(captions[14],SwingConstants.RIGHT);        
	top.add(text3);
	
	//Search Components
	exact= new JRadioButton(captions[10],true);
	exact.addActionListener(this);
	exact_status=true;
	searchtype.add(exact);
	
	noCase= new JCheckBox(captions[11],false);
	noCase.addActionListener(this);
	searchtype.add(noCase);

	noMarks= new JCheckBox(captions[12],false);
	noMarks.addActionListener(this);
	searchtype.add(noMarks);

	normalise= new JCheckBox(captions[13],false);
	normalise.addActionListener(this);
	searchtype.add(normalise);
	top.add(searchtype);

	JTextPane text2 = new JTextPane();
        text2.setContentType("text/html; charset=UTF-8");
        text2.setText(captions[3]);
        text2.setEditable(false);
	  text2.setBounds( 0, 0, 100, 100 );
        JScrollPane scroll = new JScrollPane(text2);
	  scroll.setPreferredSize( new Dimension( 100, 100 ) );
        top.add(scroll);

         okay=new JButton(captions[4]);
         okay.addActionListener(this);
         //okay.setFont(CurrentFont); NEEDS TO BE IMPLEMENTED IN THE FINAL VERSION
         top.add(okay);

         results=new JTextPane();
         results.setEditable(false);
         results.setContentType("text/html; charset=UTF-8");
         results.setText(captions[5]);
         JScrollPane scrollPane3 = new JScrollPane(results);
         bottom.add(scrollPane3);

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
        setSize(700, 600);
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
	if (name.equals(captions[10])){
		if (exact.isSelected()){
			//exact.setSelected(!exact_status);
			noCase.setSelected(false);
			noMarks.setSelected(false);
			normalise.setSelected(false);
		exact_status=!exact_status;
		}else{
			exact.setSelected(false);
			noCase.setSelected(true);
		}
		return;
	}
	if (name.equals(captions[11])){
		if (noCase.isSelected()){
			exact.setSelected(false);
			exact_status=false;
		}else if (!noCase.isSelected() && !noMarks.isSelected() && !normalise.isSelected()){
			exact.setSelected(true);
			exact_status=true;
		}
		return;
	}
	if (name.equals(captions[12])){
		if (noMarks.isSelected()){
			exact.setSelected(false);
			exact_status=false;
		}else if (!noCase.isSelected() && !noMarks.isSelected() && !normalise.isSelected()){
			exact.setSelected(true);
			exact_status=true;
		}
		return;
	}
	if (name.equals(captions[13])){
		if (normalise.isSelected()){
			exact.setSelected(false);
			exact_status=false;
		}else if (!noCase.isSelected() && !noMarks.isSelected() && !normalise.isSelected()){
			exact.setSelected(true);
			exact_status=true;
		}

		return;
	}
	if (name.equals(captions[4])){
        if (!built){
            //We have not created the database already should do so now.
            built=true;
            File folder = new File("Ponomar/languages/en/xml/lives");
            File[] listOfFiles = folder.listFiles();
            Commemoration1 test=new Commemoration1();
            OrderedHashtable current=new OrderedHashtable();
            int count=0;
            database=new OrderedHashtable[listOfFiles.length];
            for (File file : listOfFiles) {
                database[count]=new OrderedHashtable();
                if (file.isFile()) {
                    if (file.getName().endsWith("xml")){
                        System.out.println(file.getName());
                        test =new Commemoration1(file.getName().substring(0, file.getName().length()-4),file.getName().substring(0, file.getName().length()-4),Analyse.dayInfo);
                        String nameF=test.getGrammar("Nominative").toString();
                        database[count].put("Nominative",nameF);
                        database[count].put("Lowercased",nameF.toLowerCase());
                        database[count].put("Normalised",normalise(nameF));
                        database[count].put("Dediacritised",dediacriticalise(nameF));
                        database[count].put("CId",file.getName().subSequence(0, file.getName().length()-4));
                        String CId=database[count].get("CId").toString();
                            if (new File("Ponomar/languages/fr/xml/lives/"+file.getName()).exists()){
                                database[count].put("fr",true);
                            }else{
                                database[count].put("fr",false);
                            }
                            if (new File("Ponomar/languages/cu/xml/lives/"+file.getName()).exists()){
                                database[count].put("cu",true);
                            }else{
                                database[count].put("cu",false);
                            }
                            if (new File("Ponomar/languages/cu/ru/xml/lives/"+file.getName()).exists()){
                                database[count].put("ru",true);
                            }else{
                                database[count].put("ru",false);
                            }
                            if (new File("Ponomar/languages/zh/Hant/xml/lives/"+file.getName()).exists()){
                                database[count].put("zht",true);
                            }else{
                                database[count].put("zht",false);
                            }
                            if (new File("Ponomar/languages/zh/Hans/xml/lives/"+file.getName()).exists()){
                                database[count].put("zhs",true);
                            }else{
                                database[count].put("zhs",false);
                            }
                            if (new File("Ponomar/languages/el/xml/lives/"+file.getName()).exists()){
                                database[count].put("el",true);
                            }else{
                                database[count].put("el",false);
                            }
                            if (new File("Ponomar/languages/el/mono/xml/lives/"+file.getName()).exists()){
                                database[count].put("eln",true);
                            }else{
                                database[count].put("eln",false);
                            }
                            //database[count]=current;
                            count+=1;
                            
                        //}

                    }
                    
                }                
            }
            for (int i=0;i<listOfFiles.length-count;i++){
                database[count+i]=new OrderedHashtable();
            }
            
        }
        String found =captions[6];
        String search = searchTerm.getText();
        int count=0;
            //We have already built the database and now we need to search it.
            for (int i=0;i<database.length;i++){
               OrderedHashtable current=(OrderedHashtable)database[i];
               
               //System.out.println(database);
               //System.out.println(current);
               if (!current.isEmpty()){
                String nameF=current.get("Nominative").toString();
                        if (nameF.contains(search)){
                            found+="<B>"+current.get("CId").toString()+"</B>\t"+nameF+";\t";
                            count+=1;
                            if ((boolean) current.get("fr")){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            if ((boolean) current.get("cu")){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            if ((boolean) current.get("ru")){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            if ((boolean) current.get("zht")){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            if ((boolean) current.get("zhs")){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            if ((boolean) current.get("el")){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            if ((boolean) current.get("eln")){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            found+="<BR>";
                        }
            }
            }
            results.setText(found+"<BR> " +captions[9]+count);
            results.setCaretPosition(0);
        }
        /*if (name.equals(captions[4])) {
            //System.out.println("Hello");
            String search = searchTerm.getText();
            //Will only search English
            File folder = new File("Ponomar/languages/en/xml/lives");
            File[] listOfFiles = folder.listFiles();
            //System.out.println(listOfFiles.length);

            String found =captions[6];
            Commemoration1 test=new Commemoration1();
            int count=0;
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    if (file.getName().endsWith("xml")){
                        System.out.println(file.getName());
                        test =new Commemoration1(file.getName().substring(0, file.getName().length()-4),file.getName().substring(0, file.getName().length()-4),Analyse.dayInfo);
                        String nameF=test.getGrammar("Nominative").toString();
                        if (nameF.contains(search)){
                            found+=file.getName().subSequence(0, file.getName().length()-4)+"\t"+nameF+"\t";
                            count+=1;
                            if (new File("Ponomar/languages/fr/xml/lives/"+file.getName()).exists()){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            if (new File("Ponomar/languages/cu/xml/lives/"+file.getName()).exists()){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            if (new File("Ponomar/languages/cu/ru/xml/lives/"+file.getName()).exists()){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            if (new File("Ponomar/languages/zh/Hant/xml/lives/"+file.getName()).exists()){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            if (new File("Ponomar/languages/zh/Hans/xml/lives/"+file.getName()).exists()){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            if (new File("Ponomar/languages/el/xml/lives/"+file.getName()).exists()){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            if (new File("Ponomar/languages/el/mono/xml/lives/"+file.getName()).exists()){
                                found+=captions[7]+"\t";
                            }else{
                                found+=captions[8]+"\t";
                            }
                            found+="<BR>";
                        }

                    }
                    
                }                
            }
            //System.out.println(found);
            results.setText(found+"<BR> " +captions[9]+count);
            results.setCaretPosition(0);

        }*/


    }
         private String normalise(String text){
             return text;
         }
         private String dediacriticalise(String text){
             return text;
         }
        public static void main(String[] argz)
        {
            OrderedHashtable dayinfo=new OrderedHashtable();
            dayinfo.put("LS","fr/");
            Search testing = new Search(dayinfo);
            

        }
}

