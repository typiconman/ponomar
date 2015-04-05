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
public class Search extends JFrame implements ActionListener
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	//THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	//TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	//DURING THE COURSE OF A SINGLE WEEK.
	public String UsualBeginning1;
        private StringOp Analyse=new StringOp();
        private JTextField searchTerm = new JTextField("");
        private JButton okay;
        private JTextPane results;
		
	public Search(OrderedHashtable dayInfo)
	{
		//Assuming at present only English exists:
            Analyse.dayInfo = dayInfo;
/*        Text = new LanguagePack(dayInfo);
        captions = Text.obtainValues((String) Text.Phrases.get("BibleW"));*/
        setTitle("Search Commemorations");

//        LanguagePack getLang = new LanguagePack(Analyse.dayInfo);


        JPanel top = new JPanel();
        JPanel bottom = new JPanel();
        top.setLayout(new GridLayout(2,2));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.PAGE_AXIS));

        JLabel text =new JLabel("Search Term: ",SwingConstants.RIGHT);
        top.add(text);
        searchTerm.setEditable(true);
        searchTerm.setText("Please enter your search terms.");
        top.add(searchTerm);
        JTextPane text2 = new JTextPane();
        text2.setContentType("text/html; charset=UTF-8");
        text2.setText("This is a trial search of the commemorations in a given language with display across languages. Unfortunately, no stemming or collation is available so that the results are very, very, very dependent on what you enter. The fewer letters or words that are entered here, the more likely you are to find what you are looking for. Entering \"George\" is more likely to give results than \"George the New Martyr.\"");
        text2.setEditable(false);
        JScrollPane scroll = new JScrollPane(text2);
        top.add(scroll);

         okay=new JButton("Search");
         okay.addActionListener(this);
         //okay.setFont(CurrentFont); NEEDS TO BE IMPLEMENTED IN THE FINAL VERSION
         top.add(okay);

         results=new JTextPane();
         results.setEditable(false);
         results.setContentType("text/html; charset=UTF-8");
         results.setText("There are no results.");
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

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Remove in the final version

		 	
		 	
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
        if (name.equals("Search")) {
            System.out.println("Hello");
            String search = searchTerm.getText();
            //Will only search English
            File folder = new File("Ponomar/languages/en/xml/lives");
            File[] listOfFiles = folder.listFiles();
            System.out.println(listOfFiles.length);

            String found ="CId\tEnglish\tFrench\tRussian\tChurch Slavonic\tTraditional Chinese\tSimplified Chinese\tPolytonic Greek\tMonotonic Greek<BR>";
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
                                found+="Exists;\t";
                            }else{
                                found+="N/A;\t";
                            }
                            if (new File("Ponomar/languages/cu/xml/lives/"+file.getName()).exists()){
                                found+="Exists;\t";
                            }else{
                                found+="N/A;\t";
                            }
                            if (new File("Ponomar/languages/cu/ru/xml/lives/"+file.getName()).exists()){
                                found+="Exists;\t";
                            }else{
                                found+="N/A;\t";
                            }
                            if (new File("Ponomar/languages/zh/Hant/xml/lives/"+file.getName()).exists()){
                                found+="Exists;\t";
                            }else{
                                found+="N/A;\t";
                            }
                            if (new File("Ponomar/languages/zh/Hans/xml/lives/"+file.getName()).exists()){
                                found+="Exists;\t";
                            }else{
                                found+="N/A;\t";
                            }
                            if (new File("Ponomar/languages/el/xml/lives/"+file.getName()).exists()){
                                found+="Exists;\t";
                            }else{
                                found+="N/A;\t";
                            }
                            if (new File("Ponomar/languages/el/mono/xml/lives/"+file.getName()).exists()){
                                found+="Exists;\t";
                            }else{
                                found+="N/A;\t";
                            }
                            found+="<BR>";
                        }

                    }
                    
                }                
            }
            //System.out.println(found);
            results.setText(found+"<BR> Found a total of: "+count);
            results.setCaretPosition(0);

        }


    }

        public static void main(String[] argz)
        {
            OrderedHashtable dayinfo=new OrderedHashtable();
            dayinfo.put("LS","fr/");
            Search testing = new Search(dayinfo);
            

        }
}

