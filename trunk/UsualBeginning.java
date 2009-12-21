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
THIS MODULE CREATES THE TEXT FOR THE ORTHODOX USUAL BEGINNING OF A SERVICE
THIS MODULE IS STILL IN THE DEVELOPMENT PHASE.

(C) 2008 YURI SHARDT. ALL RIGHTS RESERVED.
Updated some parts to make it compatible with the changes in Ponomar, especially the language issues!

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
public class UsualBeginning
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	//THE DATE OR THE RELEVANT INFORMATION WILL HAVE TO BE GIVEN
	//TO THE PROGRAMME. AT PRESENT IT WILL BE ASSUMED THAT IT IS TONE 1
	//DURING THE COURSE OF A SINGLE WEEK.
	public static String UsualBeginning1;
	public UsualBeginning(int Weekday)
	{
		StringOp.dayInfo = new Hashtable();
		StringOp.dayInfo.put("dow", Weekday);		//DETERMINE THE DAY OF THE WEEK.
		StringOp.dayInfo.put("PS",1);
		StringOp.dayInfo.put("nday",250);
		StringOp.dayInfo.put("LS",0);
		final String UsualFileName = "Ponomar/xml/Services/UsualBeginning/"; // THE LOCATION FOR ANY EXTRA INFORMATION
		Service test2=new Service();
		 test2.readService(UsualFileName+"UsualBeginning.xml");
		UsualBeginning1=test2.Service1;
	}	
	public UsualBeginning()
	{
		final String UsualFileName = "Ponomar/xml/Services/UsualBeginning/";
		Service test2=new Service();
		UsualBeginning1=test2.readService(UsualFileName+"UsualBeginning.xml");
		 	
		 	
	}
	public String getUsualBeginning()
	{
		return UsualBeginning1;
	}
}

