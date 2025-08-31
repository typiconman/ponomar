package Ponomar;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.filechooser.FileFilter;

/***********************************************************************
THIS MODULE CREATES THE TEXT FOR THE ORTHODOX SERVICE OF THE FIRST HOUR (PRIME)
THIS MODULE IS STILL IN THE DEVELOPMENT PHASE.

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
public class GetID
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	//Allows me to create the correct HTML line for a specific piece of information required from a file.
        public String Type="M";
        public String Id="";
        public String What="";
        public String Who="";
        public String RedFirst="0";
        public String Times="1";
        public String NewLine="";
        public String Header="";
        private String HTML="";
        public String ToneA="";
	
	
		
	public GetID(String CId, String path)
	{
            Id=CId;
            What=path;
            //System.out.println("The ID is "+Id+" and the desired item is "+What);    
        }
	public String getHTML()
	{
            HTML="<GETID Type=\""+Type+"\" Id=\""+Id+"\" What=\""+What+"\" Who=\""+Who+"\" RedFirst=\""+RedFirst+"\" ";
            if (Integer.parseInt(Times)>1){
                HTML+="Times=\""+Times+"\" ";
            }
            HTML+="NewLine=\""+NewLine+"\" Header=\""+Header+"\" ToneA=\"" +ToneA+"\"/>";
            return HTML;
	}
        public String GetFullID()
	{
            //Id=CId;
            //What=path;
            String[] splitPath=What.split("/");
            
            //System.out.println("The ID is "+Id+" and the desired item is "+What);
            //System.out.println("Testing: "+Id+"_"+splitPath[splitPath.length-1]);
            return Id+"_"+splitPath[splitPath.length-1];
        }

	
    public static void main(String[] args) {
        GetID GetIDx=new GetID("9863","soemthing/somewhere/test");
        GetIDx.NewLine="1";
        GetIDx.Who="C";
        GetIDx.RedFirst="0";
        GetIDx.Times="1";
        GetIDx.NewLine="0";
        GetIDx.Header="Nothing to Show";
        System.out.println(GetIDx.getHTML());
        //output.append(s + newline);
        //output.setCaretPosition(output.getDocument().getLength());
    }
    
	

	
}

