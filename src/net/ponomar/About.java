package net.ponomar;

import net.ponomar.internationalization.LanguagePack;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 
import net.ponomar.utility.StringOp;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;

/********************************************************************
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

/**
 * 
 * This class creates the about box that appears in the window.
 * 
 * @author Yuri Shardt
 * 
 */
public class About extends JFrame
{
	private LanguagePack ponomar;//=new LanguagePack();
	private String value;//=(String)Ponomar.Phrases.get("0");
        private StringOp analyse=new StringOp();
	public About(LinkedHashMap<String, Object> dayInfo)
	{
            analyse.setDayInfo(dayInfo);
            ponomar=new LanguagePack(dayInfo);
            
	 value= ponomar.getPhrases().get("0");
		//ALLOWS A DIFFERENT TITLE TO BE SPECIFIED BY THE USER (CYRILLIC FOR THE CYRILLIC VERSIONS)
		setTitle(value);
		LanguagePack text=new LanguagePack(dayInfo);
		String [] aboutNames=text.obtainValues(text.getPhrases().get("About"));
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
                Helpers about1=new Helpers(analyse.getDayInfo());
		JPanel contentPane=new JPanel(new BorderLayout());
		contentPane.setOpaque(true);
		JTextPane output=new JTextPane();
		output.setEditable(false);
		output.setContentType("text/html");
                String displayFont= analyse.getDayInfo().get(Constants.FONT_FACE_M).toString();
                String displaySize= analyse.getDayInfo().get(Constants.FONT_SIZE_M).toString();
		output.setText("<body style=\"font-family:" + displayFont + ";font-size:" + displaySize
				+ "\"><B><h1 style=\"text-align: center;\">" + ponomar.getPhrases().get("0")
				+ "</h1></B><p style=\"text-align: center;\">" + aboutNames[0] + " "
				+ ConfigurationFiles.getDefaults().get("Version") + "</p><p>" + aboutNames[1] + "</p><p>"
				+ aboutNames[2] + "</p><p>" + about1.getCopyright() + "<BR>" + aboutNames[5] + "<BR>");
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
