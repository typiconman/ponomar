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
	private LanguagePack Ponomar=new LanguagePack();
	private String value=(String)Ponomar.Phrases.get("0");
	protected About()
	{
		//ALLOWS A DIFFERENT TITLE TO BE SPECIFIED BY THE USER (CYRILLIC FOR THE CYRILLIC VERSIONS)
		setTitle(value);
		LanguagePack Text=new LanguagePack();
		String [] AboutNames=Text.obtainValues((String)Text.Phrases.get("About"));
		
		JPanel contentPane=new JPanel(new BorderLayout());
		contentPane.setOpaque(true);
		JTextPane output=new JTextPane();
		output.setEditable(false);
		output.setContentType("text/html");
		output.setText("<B><center><Font Size='14'>"+(String)Ponomar.Phrases.get("0")+"</Font></center></B><p><center>"+AboutNames[0]+" "+(String) ConfigurationFiles.Defaults.get("Version")+"</center></p><p>"+AboutNames[1]+"</p><p>"+AboutNames[2]+"</p><p>"+AboutNames[3]+" " +(String) ConfigurationFiles.Defaults.get("Year") +" " +AboutNames[4] + " " +(String) ConfigurationFiles.Defaults.get("Authors") +"<BR>" + AboutNames[5] + "<BR>");
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
