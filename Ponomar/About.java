package Ponomar;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;

/********************************************************************
THIS CLASS CREATES THE ABOUT BOX THAT APPEARS IN THE WINDOW.

Copyright 2008 Yuri Shardt
version 1.0: August 2008
yuri (dot) shardt (at) gmail.com

 PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE CODE
 PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES THEREOF.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

**********************************************************************/
class About extends JFrame
{
	private LanguagePack Ponomar;//=new LanguagePack();
	private String value;//=(String)Ponomar.Phrases.get("0");
        private StringOp Analyse=new StringOp();
	protected About(OrderedHashtable dayInfo)
	{
            Analyse.dayInfo=dayInfo;
            Ponomar=new LanguagePack(dayInfo);
            
	 value=(String)Ponomar.Phrases.get("0");
		//ALLOWS A DIFFERENT TITLE TO BE SPECIFIED BY THE USER (CYRILLIC FOR THE CYRILLIC VERSIONS)
		setTitle(value);
		LanguagePack Text=new LanguagePack(dayInfo);
		String [] AboutNames=Text.obtainValues((String)Text.Phrases.get("About"));
		/*String[] Authors=Text.obtainValues((String) Text.Phrases.get("Authors"));
                String Year=Text.Phrases.get("Year").toString();
                String Comma=Text.Phrases.get("Comma").toString();
                String And=Text.Phrases.get("And").toString();
                String AuthorList=Authors[0];
                if (Authors.length>2)
                {
                for(int i=1;i < Authors.length-1;i++)
                {
                    AuthorList=AuthorList+Authors[i]+Comma;
                }
                }
                if (Authors.length>1)
                {
                    AuthorList=AuthorList+And+Authors[Authors.length-1];
                }*/
                Helpers About1=new Helpers(Analyse.dayInfo);
		JPanel contentPane=new JPanel(new BorderLayout());
		contentPane.setOpaque(true);
		JTextPane output=new JTextPane();
		output.setEditable(false);
		output.setContentType("text/html");
                String DisplayFont= Analyse.dayInfo.get("FontFaceM").toString();
                String DisplaySize= Analyse.dayInfo.get("FontSizeM").toString();
		output.setText("<body style=\"font-family:" + DisplayFont + ";font-size:" + DisplaySize + "\"><B><h1 style=\"text-align: center;\">"+(String)Ponomar.Phrases.get("0")+"</h1></B><p style=\"text-align: center;\">"+AboutNames[0]+" "+(String) ConfigurationFiles.Defaults.get("Version")+"</p><p>"+AboutNames[1]+"</p><p>"+AboutNames[2]+"</p><p>"+About1.getCopyright()+"<BR>" + AboutNames[5] + "<BR>");
		output.setCaretPosition(0);
		JScrollPane scrollPane = new JScrollPane(output);
		contentPane.add(scrollPane,BorderLayout.CENTER);
		setContentPane(contentPane);
		pack();
		setSize(500,400);
		//CENTRES THE FRAME
		setLocationRelativeTo(null);
		//ALLOWS US TO ADD OUR OWN IMAGES IN THE TOP OF THE WINDOW
		//setIconImage(new ImageIcon(imgURL).getImage());
		setVisible(true);                
	}
}
