package net.ponomar;

import net.ponomar.parsing.Commemoration;
import net.ponomar.utility.Constants;
 
 
 
import net.ponomar.utility.StringOp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedHashMap;


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
 * This module creates the text for the searching for commemorations across languages and jurisdictions.
 * 
 * @author Yuri Shardt
 * 
 */
public class Search extends JFrame implements ActionListener
{
	private static final String N_A = "N/A;\t";
	private static final String EXISTS = "Exists;\t";
	private static final String LIVES_PATH = "xml/lives/";
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	//THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	//TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	//DURING THE COURSE OF A SINGLE WEEK.
	public String usualBeginning1;
        private StringOp analyse=new StringOp();
        private JTextField searchTerm = new JTextField("");
        private JButton okay;
        private JTextPane results;
		
	public Search(LinkedHashMap<String, Object> dayInfo)
	{
		//Assuming at present only English exists:
            analyse.setDayInfo(dayInfo);
/*        Text = new LanguagePack(dayInfo);
        captions = Text.obtainValues((String) Text.Phrases.get("BibleW"));*/
        setTitle("Search Commemorations");

//        LanguagePack getLang = new LanguagePack(Analyse.getDayInfo());


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
        text2.setContentType(Constants.CONTENT_TYPE);
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
         results.setContentType(Constants.CONTENT_TYPE);
         results.setText("There are no results.");
         JScrollPane scrollPane3 = new JScrollPane(results);
         bottom.add(scrollPane3);

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitter.setTopComponent(top);
        splitter.setBottomComponent(bottom);

        add(splitter);

        //Adding a Menu Bar
       /* MenuFiles demo = new MenuFiles(Analyse.getDayInfo().clone());
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
		return usualBeginning1;
	}

         public void actionPerformed(ActionEvent e)
  {
        //JMenuItem source = (JMenuItem)(e.getSource());
        //String name = source.getText();

        //Helpers helper = new Helpers(Analyse.getDayInfo());
        String name = e.getActionCommand();
        //ALLOWS A MULTILINGUAL PROPER VERSION
        if (name.equals("Search")) {
            System.out.println("Hello");
            String search = searchTerm.getText();
            //Will only search English
            File folder = new File(Constants.LANGUAGES_PATH + "/en/" + LIVES_PATH);
            File[] listOfFiles = folder.listFiles();
            System.out.println(listOfFiles.length);

            StringBuilder found = new StringBuilder("CId\tEnglish\tFrench\tRussian\tChurch Slavonic\tTraditional Chinese\tSimplified Chinese\tPolytonic Greek\tMonotonic Greek<BR>");
            Commemoration test=new Commemoration();
            int count=0;
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    if (file.getName().endsWith("xml")){
                        System.out.println(file.getName());
                        test =new Commemoration(file.getName().substring(0, file.getName().length()-4),file.getName().substring(0, file.getName().length()-4),analyse.getDayInfo());
                        String nameF=test.getGrammar(Constants.NOMINATIVE);
                        if (nameF.contains(search)){
                            found.append(file.getName().subSequence(0, file.getName().length() - 4)).append("\t").append(nameF).append("\t");
                            count+=1;
                            if (new File(Constants.LANGUAGES_PATH + "/fr/" + LIVES_PATH + file.getName()).exists()){
                                found.append(EXISTS);
                            }else{
                                found.append(N_A);
                            }
                            if (new File(Constants.LANGUAGES_PATH + "/cu/" + LIVES_PATH + file.getName()).exists()){
                                found.append(EXISTS);
                            }else{
                                found.append(N_A);
                            }
                            if (new File(Constants.LANGUAGES_PATH + "/cu/ru/" + LIVES_PATH + file.getName()).exists()){
                                found.append(EXISTS);
                            }else{
                                found.append(N_A);
                            }
                            if (new File(Constants.LANGUAGES_PATH + "/zh/Hant/" + LIVES_PATH + file.getName()).exists()){
                                found.append(EXISTS);
                            }else{
                                found.append(N_A);
                            }
                            if (new File(Constants.LANGUAGES_PATH + "/zh/Hans/" + LIVES_PATH + file.getName()).exists()){
                                found.append(EXISTS);
                            }else{
                                found.append(N_A);
                            }
                            if (new File(Constants.LANGUAGES_PATH + "/el/" + LIVES_PATH + file.getName()).exists()){
                                found.append(EXISTS);
                            }else{
                                found.append(N_A);
                            }
                            if (new File(Constants.LANGUAGES_PATH + "/el/mono/" + LIVES_PATH + file.getName()).exists()){
                                found.append(EXISTS);
                            }else{
                                found.append(N_A);
                            }
                            found.append("<BR>");
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
        	LinkedHashMap<String, Object> dayinfo=new LinkedHashMap<>();
            dayinfo.put("LS","fr/");
            Search testing = new Search(dayinfo);
            

        }
}

