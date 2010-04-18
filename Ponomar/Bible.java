package Ponomar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import java.net.URL;

/****************************************************************************************
Bible.java : A SCRIPTURE READER INTERFACE FOR THE PONOMAR PROJECT.
THIS MODULE OUTPUTS, BROWSES, AND SEARCHES THE SCRIPTURAL TEXTS.
IT ALSO PROVIDES TOOLS FOR COPYING, SAVING, AND PRINTING SCRIPTURE TEXTS AND FOR
CROSS-REFERENCING TRANSLATIONS, VERSIONS, AND PATRISTIC COMMENTARY.

Bible.java is part of the Ponomar program.
Copyright 2007, 2008 Aleksandr Andreev.
aleksandr.andreev@gmail.com

Ponomar is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

While Ponomar is distributed in the hope that it will be useful,
it comes with ABSOLUTELY NO WARRANTY, without even the implied warranties of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for details.
 ****************************************************************************************/
class Bible extends JFrame implements DocHandler, ListSelectionListener, ActionListener {

    private final static String bmlfile = "Ponomar/xml/bible.xml"; // SOURCE FILE
    private final static String bibpath = "Ponomar/data/";
    // Parsing and navigation information
    private String curversion = "kjv"; // DEFAULT VERSION
    private String curbook;
    private String curpassage;
    private String lastversion = "";
    private String instructions = "";
    private OrderedHashtable versions = new OrderedHashtable();
    private OrderedHashtable books = new OrderedHashtable();
    private OrderedHashtable chapters = new OrderedHashtable();
    private OrderedHashtable abbrev = new OrderedHashtable();
    private boolean readFile = false;
    private OrderedHashtable findId = new OrderedHashtable();	//ADDED Y.S.
    private OrderedHashtable Intro = new OrderedHashtable(); //ADDED Y.S.
    private String DisplayFont = new String(); //ALLOWS A CUSTOM FONT AND SIZE TO BE SPECIFIED FOR A GIVEN BIBLE READING: REQUIRED FOR OLD CHURCH SLAVONIC AT PRESENT
    private String DisplaySize = "12";  //UNTIL A COMPLETE UNICODE FONT IS AVAILIBLE.
    private Font DefaultFont = new Font("", Font.BOLD, 12);		//CREATE THE DEFAULT FONT
    private Font CurrentFont = DefaultFont;
    // components
    private PrintableTextPane text;		// CONTAINS THE TEXT OUTPUT
    private JTextPane instructionsText;
    private JComboBox versionsBox;	// CONTAINS THE VERSIONS OF SCRIPTURE AVAILABLE
    private JList booksBox;	// CONTAINS THE BOOKS
    private JList chaptersBox;// CONTAINS THE CHAPTERS
    private boolean changeIt = false;
    private boolean changeItBooks = true;	//ADDED Y.S.
    //ADDED Y.S. 20081211 n.s. DEFAULT DISPLAY PARAMETERS
    private String ChapterName = "Chapter";
    private String[] VerseNumber = {"Prologue", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "100", "101", "102", "103", "104", "105", "106", "107", "108", "109", "110", "111", "112", "113", "114", "115", "116", "117", "118", "119", "120", "121", "122", "123", "124", "125", "126", "127", "128", "129", "130", "131", "132", "133", "134", "135", "136", "137", "138", "139", "140", "141", "142", "143", "144", "145", "146", "147", "148", "149", "150", "151", "152", "153", "154", "155", "156", "157", "158", "159", "160", "161", "162", "163", "164", "165", "166", "167", "168", "169", "170", "171", "172", "173", "174", "175", "176", "177", "178", "179", "180"};
    private String[] ChapterNumber = {"Prologue", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "100", "101", "102", "103", "104", "105", "106", "107", "108", "109", "110", "111", "112", "113", "114", "115", "116", "117", "118", "119", "120", "121", "122", "123", "124", "125", "126", "127", "128", "129", "130", "131", "132", "133", "134", "135", "136", "137", "138", "139", "140", "141", "142", "143", "144", "145", "146", "147", "148", "149", "150", "151", "152", "153", "154", "155", "156", "157", "158", "159", "160", "161", "162", "163", "164", "165", "166", "167", "168", "169", "170", "171", "172", "173", "174", "175", "176", "177", "178", "179", "180"};
    private String[] Comments;
    private String CVSep = ":";
    private String Duration = "-";
    private String SelectionSeparator = ", ";
    private String InstructFirst;
    private static final int NUM_BUTTONS = 7;
    private LanguagePack Text = new LanguagePack();
    private final String[] captions = Text.obtainValues((String) Text.Phrases.get("BibleW"));
    private String printText;
    private String currentBible="";
    private OrderedHashtable comboBoxList;
    private OrderedHashtable findIdL;
    //new String[]
    //{"Previous Chapter", "Entire Chapter", "Next Chapter", "Bookmark Passage", "Copy Passage", "Save Passage", "Print Passage"};

    // CONSTRUCTOR
    protected Bible(String passage) {
        throw new UnsupportedOperationException("Bible(String) has been deprecated. Please use Bible(String, String).");
    }

    protected Bible(String curbook, String curpassage) {
        //GET THE DEFAULT BIBLE FOR THE GIVEN LANGUAGE!
        //super("Ponomar Scripture Reader");
        setTitle(captions[7]);
        //ADDED Y.S. TO ALLOW FOR MULTILINGUAL AND ALL BIBLE READING
        String a = (String) ConfigurationFiles.Defaults.get("BibleDefaults");
        String[] Default = a.split(",");
        int LSa = Integer.parseInt(StringOp.dayInfo.get("LS").toString());
        curversion = Default[LSa];

        // FIRST, PARSE THE BIBLE.XML FILE TO OBTAIN ALL THE NECESSARY INFORMATION
        try {
            BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), "UTF8"));	//Unicodised it.
            QDParser.parse(this, frf);
        } catch (Exception e) {
            System.out.println(captions[8] + e.toString());
        }

        // NOW PUT THE XML INFORMATION INTO THE USER INTERFACE
        // Time to bring out the GridBag
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        versionsBox = new JComboBox(new Vector(versions.values()));
        //versionsBox = new JComboBox(new Vector(comboBoxList.values()));
       //versionsBox.setComponentOrientation(new ComponentOrientation);
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        add(versionsBox, c);
        //GET THE DEFAULT LANGUAGE BIBLE INDEX LOCATION IN THE GIVEN LIST
        int indexV = 0;
        Vector vers1 = new Vector(versions.values());
        for (indexV = 0; indexV < vers1.size(); indexV++) {
            System.out.println("Line 116 issues");
            System.out.println(vers1.get(indexV));
            System.out.println(findId.get(vers1.get(indexV)).toString().equals(curversion));
            if (findId.get(vers1.get(indexV)).toString().equals(curversion)) {
                break;
            }
        }
        versionsBox.setSelectedIndex(indexV);
        versionsBox.addActionListener(this);


        booksBox = new JList(new Vector(books.values()));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.ipady = 250;
        c.ipadx = 100;
        c.weightx = 0.5;
        booksBox.addListSelectionListener(this);
        JScrollPane scrollPane1 = new JScrollPane(booksBox);
        add(scrollPane1, c);

        chaptersBox = new JList(new String[]{"size dummy"});
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        c.ipadx = 0;
        c.weightx = 0.5;
        chaptersBox.addListSelectionListener(this);
        JScrollPane scrollPane2 = new JScrollPane(chaptersBox);
        add(scrollPane2, c);

        JToolBar toolbar = new JToolBar();
        toolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolbar.setFloatable(false);
        for (int bnum = 0; bnum < NUM_BUTTONS; bnum++) {
            String imgLocation = "images/" + bnum + ".gif";
            URL imgURL = Bible.class.getResource(imgLocation);

            JButton button = new JButton();
            button.setActionCommand(Integer.toString(bnum));
            button.setToolTipText(captions[bnum]);
            button.addActionListener(this);

            if (imgURL != null) {
                button.setIcon(new ImageIcon(imgURL, captions[bnum]));
            } else {
                button.setText(Integer.toString(bnum));
                System.err.println(captions[9] + imgLocation);
            }
            toolbar.add(button);
        }

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 2;
        c2.gridy = 0;
        c2.gridwidth = 2;
        c2.gridheight = 1;
        c2.fill = GridBagConstraints.BOTH;
        add(toolbar, c2);

        text = new PrintableTextPane();
        text.setEditable(false);
        text.setContentType("text/html; charset=UTF-8");

        text.setSize(400, 600);
        c.ipady = 0;
        c.ipadx = 350;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 2;
        c.gridheight = 2;
        c.anchor = GridBagConstraints.PAGE_START;
        JScrollPane scrollPane3 = new JScrollPane(text);
        add(scrollPane3, c);

        instructionsText = new JTextPane();
        instructionsText.setEditable(false);
        instructionsText.setContentType("text/html");
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weighty = 0.5;
        c.weightx = 0.5;
        c.ipady = 0;
        c.ipadx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        add(instructionsText, c);

        pack();
        setSize(700, 600);
        setVisible(true);
        update(curbook, curpassage);
    }

    protected Bible() {
        //new Bible("Gen", "1:1-13"); <-- removed by Y.S. (not sure why, A.A.)
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String elem, Hashtable table) {
        if (table.get("Cmd") != null) {
            // EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
            if (StringOp.evalbool(table.get("Cmd").toString()) == false) {
                return;
            }
        }
        if (elem.equals("LANGUAGE")) {
            readFile = true;
        }
        if (elem.equals("BIBLE")) {
            versions.put((String) table.get("Id"), (String) table.get("Name"));
            lastversion = (String) table.get("Id");
            currentBible=(String) table.get("Name");
             findId.put((String) table.get("Name"), (String) table.get("Id"));//ADDED Y.S.
        }
        if (elem.equals("INFO")) {
            //ADDED Y.S. 2001211 n.s.
            Font value1a = (Font) UIManager.get("Menu.font");
                //if(table.get("FontFace") != null)
                //{
                String DisplayFontA="";
                String DisplaySizeA="";

                if (table.get("FontFace") == null) {

                    DisplayFontA = value1a.getFontName();
                    if (DisplayFontA.equals("Hirmos Ponomar"));
                    {
                        DisplayFontA="Times New Roman";
                    }

                } else {
                    DisplayFontA = (String) table.get("FontFace");
                }
                if (table.get("FontSize") == null) {

                    DisplaySizeA = Integer.toString(value1a.getSize());
                } else {
                    DisplaySizeA = (String) table.get("FontSize");
                }
                String Name=currentBible;//(String)versions.get(lastversion);
                //comboBoxList.put(lastversion,"<body style=\"font-family:"+DisplayFontA+";font-size:"+DisplaySizeA+"\">"+Name+"</body>");
                //findIdL.put("<body style=\"font-family:"+DisplayFontA+";font-size:"+DisplaySizeA+"\">"+Name+"</body>", (String) table.get("Id"));
                //System.out.println(versions.get(lastversion));
               //versions.();

            if (curversion.equals(lastversion)) {
                ChapterName = (String) table.get("ChapterN");
                String a = (String) table.get("VerseNo");
                VerseNumber = a.split(",");
                a = (String) table.get("ChapterNo");
                ChapterNumber = a.split(",");
                //Comments=(String)table.get("Comments");
                CVSep = (String) table.get("CVSep");	//Chapter Verse Separator: Book Chapter:Verse or Book Chapter,Verse or something else
                Duration = (String) table.get("Duration"); //SEPARATOR BETWEEN THE ENDS OF A CONTINUOUS READING: 3:2-4:5, or 3:2-10
                SelectionSeparator = (String) table.get("SelectionSeparator"); //SEPARATOR BETWEEN SELECTIONS OF READINGS, Exodus 3:2, 4:5-10, 10:10-11:3
                //ALLOWINS DIFFERENT FONTS TO BE USED: 2009/02/16 n.s.
                Font value1 = (Font) UIManager.get("Menu.font");
                //if(table.get("FontFace") != null)
                //{
                

                if (table.get("FontFace") == null) {
                    
                    DisplayFont = value1.getFontName();
                    if (DisplayFont.equals("Hirmos Ponomar"));
                    {
                        DisplayFont="Times New Roman";
                    }

                } else {
                    DisplayFont = (String) table.get("FontFace");
                }
                if (table.get("FontSize") == null) {
                    
                    DisplaySize = Integer.toString(value1.getSize());
                } else {
                    DisplaySize = (String) table.get("FontSize");
                }
               
                CurrentFont = new Font(DisplayFont, Font.PLAIN, Integer.parseInt(DisplaySize));
                /*DisplayFont=(String)table.get("FontFace");
                DisplaySize=table.get("FontSize").toString();
                }*/
                /*else
                {
                if (DisplaySize == null || DisplaySize.equals(""))
                {
                DisplaySize=Integer.toString(value1.getSize());
                }
                if (DisplayFont == null || DisplayFont.equals(""))
                {
                DisplayFont=value1.getFontName();
                }
                CurrentFont=DefaultFont;
                DisplayFont=new String();
                DisplaySize="12";
                }*/

            }
        } else if (elem.equals("BOOK")) {
            if (curversion.equals(lastversion)) {
                books.put((String) table.get("Id"), (String) table.get("Name"));
                chapters.put((String) table.get("Id"), (String) table.get("Chapters"));
                Intro.put(table.get("Id").toString(), table.get("Intro").toString());  //ADDED Y.S.
            }
            if (readFile) {
                abbrev.put((String) table.get("Id"), (String) table.get("Short"));
            }
        }
    }

    public void endElement(String elem) {
        if (elem.equals("BIBLE")) {
            lastversion = "";
        }
        if (elem.equals("LANGUAGE")) {
            readFile = false;
        }
    }

    public void text(String text) {
    }

    public void valueChanged(ListSelectionEvent e) {
        // FIGURE OUT WHICH LIST WAS CHANGED
        if (e.getSource().equals(booksBox) && changeItBooks) {
            changeIt = false;
            // then, get the book
            int n = booksBox.getSelectedIndex();
            int m = Integer.parseInt(chapters.values().toArray()[n].toString());
            // clear chaptersBox
            chaptersBox.removeAll();
            Vector dummy = new Vector();
            // add the chapters of book n
            for (int i = 1; i <= m; i++) {
                dummy.addElement(ChapterNumber[i]);	//CHANGED Y.S. 2008/12/11 n.s. FOR DIFFERENT FORMAT VERSIONS
            }
            chaptersBox.setListData(dummy);
            curbook = books.keySet().toArray()[n].toString();
            int curchap = curpassage.indexOf(":") != -1 ? (int) Integer.parseInt(curpassage.substring(0, curpassage.indexOf(":"))) : (int) Integer.parseInt(curpassage);
            chaptersBox.setSelectedValue(curchap, true);
            changeIt = true;
        } else if ((e.getSource().equals(chaptersBox)) && !e.getValueIsAdjusting()) {
            if (changeIt) {
                // NAVIGATE TO a different chapter
                int n = chaptersBox.getSelectedIndex();
                update(curbook, (new Integer(n + 1)).toString());
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        Helpers helper = new Helpers();
        String name = e.getActionCommand();
        //ALLOWS A MULTILINGUAL PROPER VERSION
        if (name.equals("comboBoxChanged")) {
            curversion = (String) findId.get((String) versionsBox.getSelectedItem());
            //REREAD THE BIBLE.XML FILE FOR THE NEW READINGS
            try {
                
                BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), "UTF8"));	//Unicodised it.
                QDParser.parse(this, frf);
            } catch (Exception ew) {
                System.out.println("Error reading bmlfile: " + ew.toString());
            }
            changeItBooks = false;
            booksBox.removeAll();
            booksBox.setListData(new Vector(books.values()));
            changeItBooks = true;
            update(curbook, curpassage);
        } else {
            int butnum = (int) Integer.parseInt(name);
            if (butnum == 0) {
                // go back (prior chapter)
                int curchap;
                changeIt = false;
                if (curpassage.indexOf(":") != -1) {
                    // we have a composite passage
                    curchap = (int) Integer.parseInt(curpassage.substring(0, curpassage.indexOf(":")));
                } else {
                    curchap = (int) Integer.parseInt(curpassage);
                }
                curchap--;

                if (curchap == 0) {
                    // we have reached the beginning of this book
                    // get the previous book
                    if (curbook.indexOf("Gen") != -1) {
                        curbook = "Apoc";
                    } else {
                        Object[] booknames = books.keySet().toArray();
                        for (int i = 0; i < booknames.length; i++) {
                            if (booknames[i].toString() == curbook) {
                                curbook = booknames[i - 1].toString();
                                break;
                            }
                        }
                    }
                    curchap = (int) Integer.parseInt((String) chapters.get(curbook));
                }
                curpassage = Integer.toString(curchap);

                update(curbook, curpassage);
                chaptersBox.setSelectedValue(curchap, true);
                changeIt = true;
            } else if (butnum == 1) {
                // get whole chapter button
                int curchap = 0;
                if (curpassage.indexOf(":") == -1) {
                    return;
                } else {
                    curchap = (int) Integer.parseInt(curpassage.substring(0, curpassage.indexOf(":")));
                }

                curpassage = Integer.toString(curchap);
                update(curbook, curpassage);
            } else if (butnum == 2) {
                // go forward (next chapter)
                int curchap;
                changeIt = false;
                if (curpassage.indexOf(":") != -1) {
                    // we have a composite passage
                    curchap = (int) Integer.parseInt(curpassage.substring(0, curpassage.indexOf(":")));
                } else {
                    curchap = (int) Integer.parseInt(curpassage);
                }
                curchap++;

                if (curchap > (int) Integer.parseInt((String) chapters.get(curbook))) {
                    // we have reached the beginning of this book
                    // get the previous book
                    if (curbook.indexOf("Apoc") != -1) {
                        curbook = "Gen";
                    } else {
                        Object[] booknames = books.keySet().toArray();
                        for (int i = 0; i < booknames.length - 1; i++) {
                            if (booknames[i].toString() == curbook) {
                                curbook = booknames[i + 1].toString();
                                break;
                            }
                        }
                    }
                    curchap = 1;
                }
                curpassage = Integer.toString(curchap);

                update(curbook, curpassage);
                chaptersBox.setSelectedValue(curchap, true);
                changeIt = true;
            } else if (butnum == 3) {
                // bookmark button clicked ...
                System.out.println("Bookmark option not implemented ...");
            } else if (butnum == 4) {
                // requires a selection! ... according to Sun, getSelectionStart() should return
                // "the value of dot" if there is no seleciton. But it appears instead to return zero ...
                if (text.getSelectionStart() == 0) {
                    text.selectAll();
                }
                text.copy();
            } else if (butnum == 5) {
                // save button clicked ...
                //System.out.println(text.getText());
                helper.SaveHTMLFile((String) books.get(curbook) + "_" + formatPassage(curpassage).replace(":", "-") + ".html", "<html><TITLE>"+ (String) books.get(curbook) + " " + formatPassage(curpassage)+ "</TITLE>" + printText);
            } else if (butnum == 6) {
                // print button clicked ...
                //helper.sendHTMLToPrinter("<TITLE>" + curbook + " " + curpassage + "</TITLE>" + "<H3>" + curbook + " " + curpassage + "</H3>" + text.getText());
                helper.sendHTMLToPrinter(text);
            }
        }
    }

    // updates the Scripture reading, setting a new reading
    protected void update(String newBook, String newPassage) {
        // we will parse the passage
        // update the variables
        // fetch the new text from the file
        // and update the GUI
        // first, define some variables
        changeIt = false;
        instructions = "";

        String Stuff[] = parseReadings(newBook, newPassage, true);

        curbook = newBook;
        curpassage = newPassage;

        text.setCaretPosition(0);

        //if(!DisplayFont.equals(""))
        //{
        
        text.setContentType("text/html; charset=UTF-8");
        text.setFont(CurrentFont);
        printText="<body style=\"font-family:" + DisplayFont + ";font-size:" + DisplaySize + "pt\">" + "<B>" + (String) books.get(curbook) + " " + formatPassage(curpassage) + "</B><BR>" + Stuff[0] + "</body>";
        text.setText(printText);
        //System.out.println(Stuff[0]);
        //text.setText("<B>" + (String)books.get(curbook) + " " + formatPassage(curpassage) + "</B><BR>" +Stuff[0]);
        
		/*}
        else
        {
        text.setText("<B>" + (String)books.get(curbook) + " " + formatPassage(curpassage) + "</B><BR>" +Stuff[0]);
        }*/
        // update GUI

        //ALLOWING THE FONTS TO BE CHANGED: 2009/02/16 n.s.
        booksBox.setFont(CurrentFont);
        chaptersBox.setFont(CurrentFont);

        booksBox.setSelectedValue(books.get(curbook), true);
        text.setCaretPosition(0);
        instructionsText.setText("<body style=\"font-family:" + DisplayFont + ";font-size:" + DisplaySize + "pt\">" + instructions + "</body>");
                instructionsText.setFont(CurrentFont);
		changeIt = true;
    }

    public String[] getText(String book, String passage, boolean RedStuff) {
        //SELECT THE CURRENT GENERAL BIBLE
        String a = (String) ConfigurationFiles.Defaults.get("BibleDefaults");
        String[] Default = a.split(",");
        int LSa = Integer.parseInt(StringOp.dayInfo.get("LS").toString());
        curversion = Default[LSa];
        try {
            BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), "UTF8"));
            QDParser.parse(this, frf);
        } catch (Exception e) {
            System.out.println(captions[8] + e.toString());
        }

        //CALL THE PARSE READER AND OBTAIN THE RETURNED RESULTS: [0] contains the readings with or without extra markings, [1] contains any special instructions, and [2] contains the header, that is, a properly formated version of the reading.
        String[] parsedResults = parseReadings(book, passage, RedStuff);
        return parsedResults;
    }
    //THIS FUNCTION HAS BEEN REPLACED BY THE SAME NAMED FUNCTION BUT A DIFFERENT SET OF CALLING ARGUMENTS.

    private String process(String mText) {
        int k = mText.indexOf("**");
        if (k != -1) {
            // found scripture reading instructions ... parse
            String first = mText.substring(0, k);
            String third = mText.substring(mText.lastIndexOf("**") + 2);
            String second = mText.substring(k + 2, mText.lastIndexOf("**"));
            mText = first + "<FONT Color=\"red\">**</FONT>" + third;
            instructions += "**" + second + "<BR>";
        }
        mText = mText.replace("*(", "<FONT Color=\"red\">");
        mText = mText.replace(")*", "</FONT>");
        k = mText.indexOf("|");
        if (k != -1) {
            return " <SUP>" + VerseNumber[Integer.parseInt(mText.substring(0, k))] + "</SUP>" + mText.substring(k + 1); //ADDED A SPACE BETWEEN THE LAST SENTENCE AND THE VERSE NUMBER Y.S. AND CORRECTED MULTILINGUAL ISSUES
        }

        k = mText.indexOf("#");
        if (k != -1) {
            return "<BR><B>" + ChapterName + " " + ChapterNumber[Integer.parseInt(mText.substring(1))] + "</B>"; //ADDED MULTILINGUAL SUPPORT
        }
        return mText;
    }

    private String process(String mText, boolean RedStuff) {
        int k = mText.indexOf("**");
        if (k != -1) {
            // found scripture reading instructions ... parse
            String first = mText.substring(0, k);
            String third = mText.substring(mText.lastIndexOf("**") + 2);
            String second = mText.substring(k + 2, mText.lastIndexOf("**"));
            if (RedStuff) {
                mText = first + "<FONT Color=\"red\">**</FONT>" + third;
            } else {
                mText = first + "**" + third;
            }
            instructions += "**" + second + "<BR>";
        }
        if (RedStuff) {
            mText = mText.replace("*(", "<FONT Color=\"red\">");
            mText = mText.replace(")*", "</FONT>");
        } else {
            //THIS JUST GIVES THE REQUIRED TEXT WITHOUT ANY COMMENTS
            int k2 = mText.indexOf("*(");
            while (k2 != -1) {
                String first = mText.substring(0, k2);
                String third = mText.substring(mText.indexOf(")*") + 2);
                mText = first + " " + third;
                k2 = mText.indexOf("*(");
            }
        }
        k = mText.indexOf("|");
        if (k != -1 && RedStuff) {
            return " <SUP>" + VerseNumber[Integer.parseInt(mText.substring(0, k))] + "</SUP>" + mText.substring(k + 1); //ADDED A SPACE BETWEEN THE LAST SENTENCE AND THE VERSE NUMBER Y.S. AND CORRECTED MULTILINGUAL ISSUES
        } else if (k != -1 && !RedStuff) {
            return mText.substring(k + 1);
        }

        k = mText.indexOf("#");
        if (k != -1 && RedStuff) {
            return "<BR><B>" + ChapterName + " " + ChapterNumber[Integer.parseInt(mText.substring(1))] + "</B>"; //ADDED MULTILINGUAL SUPPORT
        } else if (k != -1 && !RedStuff) {
            return "";
        }
        return mText;
    }

    //ADDED BY YURI SHARDT 2008/09/20 TO MULTILINGUILISE THE READINGS!
    public String getAbbrev(String Id) {
        Id = Id.replace(' ', '_'); //THIS CAN BE AVOIDED IF THE DEFINITIONS ARE CHANGED

        //INITIALISE TO THE DEFAULT BIBLE FOR THE GIVEN LANGUAGE
        //ADDED Y.S. TO ALLOW FOR MULTILINGUAL AND ALL BIBLE READING
        String a = (String) ConfigurationFiles.Defaults.get("BibleDefaults");
        String[] Default = a.split(",");
        int LSa = Integer.parseInt(StringOp.dayInfo.get("LS").toString());
        curversion = Default[LSa];

        try {
            BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), "UTF8"));
            QDParser.parse(this, frf);
        } catch (Exception e) {
            System.out.println(captions[9] + e.toString());
        }

        return (String) abbrev.get(Id);
    }

    private String formatPassage(String newPassage) {
        if (newPassage.indexOf(":") == -1) {
            // just a chapter specification, e.g. Gen_1
            int d = (int) Integer.parseInt(newPassage);
            newPassage = ChapterNumber[d];
            return newPassage;
        } else {
            // e.g. 2:11-3:2, 5, 13-14, 17-4:1
            String[] parts = newPassage.split(",");

            for (int j = 0; j < parts.length; j++) {
                //e.g. 2:11-3:2 or 13-14 or 5 or 4:5
                if (parts[j].indexOf("-") == -1) {
                    // the example of 5 or 4:5; replicate
                    if (parts[j].indexOf(":") == -1) {
                        parts[j] = VerseNumber[Integer.parseInt(parts[j])];
                    } else {
                        int verse = (int) Integer.parseInt(parts[j].split(":")[1]);
                        int chapter = (int) Integer.parseInt(parts[j].split(":")[0]);
                        parts[j] = ChapterNumber[chapter] + CVSep + VerseNumber[verse];
                    }
                } else {
                    String[] sections = parts[j].split("-");

                    for (int k = 0; k < sections.length; k++) {
                        if (sections[k].indexOf(":") == -1) {
                            // E.g. 13 or 5
                            sections[k] = VerseNumber[Integer.parseInt(sections[k])];
                        } else {
                            int verse = (int) Integer.parseInt(sections[k].split(":")[1]);
                            int chapter = (int) Integer.parseInt(sections[k].split(":")[0]);
                            sections[k] = ChapterNumber[chapter] + CVSep + VerseNumber[verse];
                        }
                        if (k == 0) {
                            parts[j] = sections[k];
                        } else {
                            parts[j] = parts[j] + Duration + sections[k];
                        }
                    }
                }
                //RECONSTRUCT THE GIVEN READING PART
                if (j == 0) {
                    newPassage = parts[j];
                } else {
                    newPassage = newPassage + SelectionSeparator + parts[j];
                }
            }
        }
        return newPassage;
    }

    public String getHyperlink(String reading) {
        //THIS FUNCTION CREATES THE HYPERLINK FOR BIBLE READINGS
        //CREATED Y.S. 2008/12/11 n.s.
        String a = (String) ConfigurationFiles.Defaults.get("BibleDefaults");
        String[] Default = a.split(",");
        int LSa = Integer.parseInt(StringOp.dayInfo.get("LS").toString());
        curversion = Default[LSa];

        try {
            BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), "UTF8"));
            QDParser.parse(this, frf);
        } catch (Exception e) {
            System.out.println(captions[8] + e.toString());
        }

        String[] parts = reading.split("_");
        //TODO: get rid of these string operations. They are bad, and take up too much CPU time
        String passage = parts[1].replaceAll(" ", "");
        String output = "<A Href=reading#" + parts[0].replace(' ', '_') + "#" + passage + ">";
        output += getAbbrev(parts[0]) + " " + formatPassage(passage) + "</A>";
        return output;
    }

    public String[] parseReadings(String newBook, String newPassage, boolean RedStuff) {
        //RedStuff IS A BOOLEAN THAT DETERMINES WHETHER OR NOT ANY OF THE READ COMMENTS IN THE BIBLE READINGS ARE RETAINED!
        //THIS INCLUDES COMMENTS, (VERSE NUMBERS), CHAPTER NUMBERS,...
        //ALL EXTRA HEADERS WILL BE SENT TO A SEPARATE HOLDING VARIABLE AND ONLY THE FIRST ** IS KEPT!
        Vector pChapters = new Vector(); // stores the chapter #s
        Vector pVerses = new Vector(); // stores the verse #s
        int i = 0;			 // dummy
        newBook = newBook.replaceAll(" ", "_");
        String ret = "";
        instructions = "";

        if (newPassage.indexOf(":") == -1) {
            // just a chapter specification, e.g. Gen_1
            int d = (int) Integer.parseInt(newPassage);
            pChapters.add(d);
            pChapters.add(d + 1);
            pVerses.add(1);
            pVerses.add(0); // zero means -- stop before chapter starts
        } else {
            // e.g. 2:11-3:2, 5, 13-14, 17-4:1
            String[] parts = newPassage.split(",");

            for (int j = 0; j < parts.length; j++) {
                //e.g. 2:11-3:2 or 13-14 or 5
                if (parts[j].indexOf("-") == -1) {
                    // the example of 5; replicate
                    parts[j] = parts[j] + "-" + parts[j];
                }
                String[] sections = parts[j].split("-");

                for (int k = 0; k < sections.length; k++) {
                    int verse = 0;
                    int chapter = i;
                    if (sections[k].indexOf(":") == -1) {
                        // E.g. 13 or 5
                        verse = (int) Integer.parseInt(sections[k]);
                    } else {
                        verse = (int) Integer.parseInt(sections[k].split(":")[1]);
                        chapter = (int) Integer.parseInt(sections[k].split(":")[0]);
                        i = chapter;
                    }

                    pChapters.add(chapter);
                    pVerses.add(verse);
                }
            }
        }

        // that completes the parsing process
        // now update the system variables
        curbook = newBook;
        curpassage = newPassage;
        String[] Output = readBibleFiles(curbook, curpassage, RedStuff, pVerses, pChapters);
        return Output;
    }

    public String[] readBibleFiles(String curbook, String curpassage, boolean RedStuff, Vector pVerses, Vector pChapters) {
        String filename = bibpath + curversion + "/" + curbook + ".text";
        String ret = "";

        try {
            //BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), "UTF8"));
            FileInputStream fis = new FileInputStream(filename);
            //REMOVED TO ALLOW FOR NON-ASCII FILES Y.S. 2008/10/24 ns
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF8"));
            String mLine = "";
            int nCurChapter = 0;
            int nCurVerse = 0;
            boolean printMe = false;

            Enumeration e2 = pVerses.elements();

            for (Enumeration e = pChapters.elements(); e.hasMoreElements();) {
                Object c = e.nextElement();
                //Correcting issues with not being able to read all the desired readings Y.S. 2008/12/12 n.s.
                while (nCurChapter != Integer.parseInt(c.toString())) {
                    mLine = br.readLine();
                    if (mLine == null) {
                        break;
                    }

                    // format #number, where number is the chapter #
                    if (mLine.indexOf("#") != -1) {
                        //CHANGED DUE TO UNICODE ISSUES Y.S. 2008/10/24 ns
                        //System.out.println(mLine);
                        if (mLine.indexOf("#") == 1) {
                            nCurChapter = (int) Integer.parseInt(mLine.substring(2));
                        } else {
                            nCurChapter = (int) Integer.parseInt(mLine.substring(1));
                        }
                        nCurVerse = 0;
                    }

                    if (printMe) {
                        ret += process(mLine, RedStuff);
                    }
                }

                Object v = e2.nextElement();
                //Correcting issues with not being able to read all the desired readings Y.S. 2008/12/12 n.s.
                while (nCurVerse != Integer.parseInt(v.toString())) {
                    if (mLine == null) {
                        break;
                    }

                    mLine = br.readLine();
                    // format number|text, where number is verse number
                    int n = mLine.indexOf("|");
                    if (n != -1) {
                        nCurVerse = (int) Integer.parseInt(mLine.substring(0, n));
                    }
                    //Correcting issues with not being able to read all the desired readings Y.S. 2008/12/12 n.s.
                    if (printMe || nCurVerse == Integer.parseInt(v.toString())) {
                        ret += process(mLine, RedStuff);
                        if (nCurChapter == Integer.parseInt(pChapters.elementAt(0).toString()) && nCurVerse == Integer.parseInt(pVerses.elementAt(0).toString())) {
                            //THE FIRST VERSE HAS BEEN READ, THE INSTRUCTIONS ASSOCIATED WITH THIS VERSE NEED TO BE SAVED
                            InstructFirst = instructions;
                        }

                    }
                }
                printMe = !printMe;
            }
            //CHANGED TO ALLOW FOR NON-ASCII FILES Y.S. 2008/10/24 ns
            br.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        String[] output1 = new String[3];
        output1[0] = ret;
        output1[1] = InstructFirst;
        output1[2] = (String) books.get(curbook) + " " + formatPassage(curpassage);
        return output1;
    }

    public String getIntro(String Id) {
        //ONLY THE COMPLETE READING NEEDS TO BE SENT FOR THE COMPOSITE READING; OTHERWISE THE BOOK OF THE BIBLE WILL SUFFICE
        //INITIALISE TO THE DEFAULT BIBLE FOR THE GIVEN LANGUAGE
        //ADDED Y.S. TO ALLOW FOR MULTILINGUAL AND ALL BIBLE READING
        String a = (String) ConfigurationFiles.Defaults.get("BibleDefaults");
        String[] Default = a.split(",");
        int LSa = Integer.parseInt(StringOp.dayInfo.get("LS").toString());
        curversion = Default[LSa];

        try {
            BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), "UTF8"));
            QDParser.parse(this, frf);
        } catch (Exception e) {
            System.out.println(captions[8] + e.toString());
        }
        int k = Id.lastIndexOf("_");
        if (k != -1) {
            if (Id.substring(0, k).equals("Composite")) {
                String[] Result = parseReadings(Id.substring(0, k), Id.substring(k + 1), false);
                int star1 = Result[1].indexOf("\"");	//WE ARE INTERESTED IN THE QUOTATION INFORMATION
                int starL = Result[1].lastIndexOf("\"");
                System.out.println(Result[1].substring(star1 + 1, starL) + " value of star1 " + star1 + " value of starL " + starL);
                return Result[1].substring(star1 + 1, starL).replace("...", "");
            } else {
                return (String) Intro.get(Id.substring(0, k));
            }
        }
        return (String) Intro.get(Id);


    }

    public static void main(String[] argz) {
        //DEBUG MODE
        System.out.println("Bible.java running in Debug mode");
        System.out.println("This program comes with ABSOLUTELY NO WARRANTY!!");

        new Bible();
    }
}
