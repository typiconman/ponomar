package net.ponomar.parsing;

import java.util.LinkedHashMap;

import net.ponomar.utility.Constants;
 
 
import net.ponomar.utility.StringOp;


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
	public static String usualBeginning1;
        private static StringOp analyse=new StringOp();
	public UsualBeginning(int weekday)
	{
		//Analyse.getDayInfo() = new OrderedHashtable();
		analyse.getDayInfo().put("dow", weekday);		//DETERMINE THE DAY OF THE WEEK.
		analyse.getDayInfo().put("PS",1);
		analyse.getDayInfo().put("nday",250);
		analyse.getDayInfo().put("LS",0);
		final String UsualFileName = "src/"+ Constants.SERVICES_PATH + "UsualBeginning/"; // THE LOCATION FOR ANY EXTRA INFORMATION
		Service test2=new Service((LinkedHashMap<String, Object>) analyse.getDayInfo().clone());
		 test2.readService(UsualFileName+"UsualBeginning.xml");
		usualBeginning1=test2.service1;
	}	
	public UsualBeginning(LinkedHashMap<String, Object> dayInfo)
	{
		final String UsualFileName = "src/" + Constants.SERVICES_PATH + "UsualBeginning/";
		Service test2=new Service(dayInfo);
		usualBeginning1=test2.readService(UsualFileName+"UsualBeginning.xml");
		 	
		 	
	}
	public String getUsualBeginning()
	{
		return usualBeginning1;
	}
}

