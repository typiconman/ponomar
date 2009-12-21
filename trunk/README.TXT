RELEASE NOTES
Ponomar PRE-RELEASE VERSION 0.0.45 REVISION B
DATE: 03/25/2009 (03/12/2009 old-style)
PLACE: ST PETERSBURG, RUSSIA

This is a minor fix of the Ponomar program, taking care of some outstanding issues.

SUMMARY OF (some of) CHANGES SINCE 0.0.45

File				Line #		Description
------------------------------+---------------+-------------------------------------------------------------------------
Bible.java			616		Bible.main(String) deprecated
Bible.java			497		Fixed a problem whereby reading Psalm 128 made Bible crash
PrintableTextPane.java		35		Rewrote print method fixing a problem whereby if
						more than one page was printed, nothing was printed at all
xml/03/24.xml			4-8		Fixed < and > in xml code to &lt; and &gt;
xml/03/25.xml			4-8		Fixed < and > in xml code to &lt; and &gt;
xml/05/29.xml			15		Fixed error in xml file
xml/Commands/DivineLiturgy.xml	<all>		Fixed < and > in xml code to &lt; and &gt;
xml/Commands/LanguagePacks.xml	<all>		Fixed < and > in xml code to &lt; and &gt;
xml/pentecostarion/65.xml	<all>		Fixed < and > in xml code to &lt; and &gt;
.... to ...
xml/pentecostarion/99.xml	<all>		Fixed < and > in xml code to &lt; and &gt;
xml/pentecostarion/149.xml	<all>		Fixed < and > in xml code to &lt; and &gt;
.... to ...
xml/pentecostarion/315.xml	<all>		Fixed < and > in xml code to &lt; and &gt;
------------------------------------------------------------------------------------------------------------------------


RELEASE NOTES
Ponomar PRE-RELEASE VERSION 0.0.45
DATE: 10/05/2008 (09/22/2008 old-style)
PLACE: ST PETERSBURG, RUSSIA

Another major update of the Ponomar program.
New features (work by Yuri Shardt):
	* Main interface switches between J-ville and Lucan Jump lectionaries
	* Main interface toolbar included
	* Bugs with the lectionary resolved
	* International language support
New features (work by Aleks Andreev):
	* Scripture reader cleaned up, includes parsing of instructions
	* Toolbar included for Scripture reader with Save, Print, etc.

SUMMARY OF (some of) CHANGES SINCE 0.0.40b

File				Line #		Description
------------------------------+---------------+-------------------------------------------------------------------------
Bible.java			32		Toolbar support
				46-47		International language support
				57-59		Toolbar buttons
				74		International language support
				117-149		Toolbar support
				190		Bible() default behaviour changed
				207-226		International language support
				234-237		International language support
				276-412		Toolbar functionality
				563-566		getText(String, String) deprecated
				568-594		Parsing for reading instructions added
				596-611		International language support
				
Helpers.java			108-137		Printing support added

PrintableTextPane.java		<created>	Printing support

Main.java			475		Sunrise-sunset configurability support added
-----------------------------------------------------------------------------------------------------------------------


RELEASE NOTES
Ponomar PRE-RELEASE VERSION 0.0.40 Revision B
DATE: 09/15/2008 (09/02/2008 old-style)
PLACE: ST PETERSBURG, RUSSIA

This minor update fixes a few bugs in the link between the main program interface
and the scripture processing interface.

SUMMARY OF CHANGES SINCE 0.0.40a

File				Line #		Description
------------------------------+---------------+-------------------------------------------------------------------------
Bible.java			62		Bible(string) deprecated

Main.java			411-417		Updated link to Bible, to use Bible(String, String).
------------------------------------------------------------------------------------------------------------------------

RELEASE NOTES
Ponomar PRE-RELEASE VERSION 0.0.40 Revision A
DATE: 08/04/2008 (07/22/2008 old-style)
PLACE: DENVER, COLORADO

This minor update fixes a few bugs in the link between the main program interface
and the Scripture processing interface.
These issues consisted of handling Scripture readings with multiple parts
(e.g. Isa_4:5-10, 16-20) and Scripture readings with spaces in names
(e.g. I Cor_2:3-9).

SUMMARY OF CHANGES SINCE 0.0.40 (initial)

File				Line #		Description
------------------------------+---------------+-------------------------------------------------------------------------
Main.java			433, 446, 459	New call: "<A Href=reading#" + parts[0].replace(' ', '_') + "#" + parts[1].replaceAll(" ", "") + ">";

DivineLiturgy.java		515		Ditto

Bible.java			349, 350	Removed debug code

xml/07/11.xml			9		Fixed typo

TODO: The code at Main.java, lines 433, etc, ought to be rewritten so as not to use
the replaceAll function.
-------------------------------------------------------------------------------------------------------------------------

RELEASE NOTES
Ponomar PRE-RELEASE VERSION 0.0.40 
DATE: 07/02/2008 (06/19/2008 old-style)
PLACE: DENVER, COLORADO

The present update releases an initial Scripture processing interface (Bible.java)
and attempts to implement the Lucan Jump methodology.

SUMMARY OF CHANGES SINCE 0.0.35d

File				Line #		Description
------------------------------+---------------+-------------------------------------------------------------------------
xml/bible.xml			created		This file contains all information
						about Scripture texts used by Ponomar

data/kjv/			created		Implementation of the Orthodox canon
						of Scripture
						(Notice: outside of the UK,
						 this text is in the public domain.
						Its distribution in the UK may be illegal).

**** NOTE: Major changes from the KJV:
	Some books have been renamed to correspond to the Septuagint
	KJV Name	-->	Orthodoxized KJV Name
	I Samuel		I Kingdoms
	II Samuel		II Kingdoms
	I Kings			III Kingdoms
	II Kings		IV Kingdoms
	I Chronicles		I Paralipomenon
	II Chronicles		II Paralipomenon

	Psalms have been renumbered
	KJV Psalm	-->	Orthodoxized KJV Psalm
	1-8			1-8
	9-10			9
	11-113			10-112 (subtract one from KJV number)
	114-115			113
	116:1-9			114
	116:10-16		115
	117-146			116-145 (subtract one from KJV number)
	147:1-11		146
	147:12-20		147
	148-150			148-150

	Added the so-called "apocryphal" books:
	II Esdra, Tobit, Judith, extra sections of Esther, Psalm 151
	Wisdom of Solomon, Sirach, Epistle of Jeremiah, Baruch,
	extra sections of Daniel, I Maccabees, II Maccabees,
	III Esdra, and the Prayer of Manasseh (inserted after II Paralipomenon)

NOTICE: The text of III Maccabees only exists in the Revised Standard Version.
The RSV is copyrighted. Thus the text of III Maccabees cannot be distributed 
under the terms of the GNU GPL, which is how Ponomar is licensed. THIS MEANS
THAT PONOMAR PRESENTLY DOES NOT INCLUDE A FULL ORTHODOX CANON OF SCRIPTURE IN ENGLISH.

The text of Psalm 151 is a completely new translation Copyright 2008 Aleksandr Andreev.
The copyright holder hereby releases it for licensing under the terms of the 
GNU General Public License.

Bible.java			created		Scripture reader implementation.

	TODO (tentative list of things to be added):
		* Copy to clipboard
		* Toggle off / on verse numbers
		* Switch langauges / versions
		* Print, bookmark, save, or export ot other software
		* Process inline instructions

xml/pentecostarion/06.xml	3-6		correctly classified Scripture readings
xml/pentecostarion/49.xml	3-6		correctly classified Scripture readings
xml/pentecostarion/58.xml \
 ...			   >	5-6		correctly implemented Apostles' fast
xml/pentecostarion/98.xml /
xml/08/03.xml			8		fixed error in fast specification

Main.java			30		implement HyperLinkListener
Main.java			49		declare Bible
Main.java			51-55		Lucan jump implementation by Yuri
Main.java			72		Add HyperLinkListener to text object
Main.java			145-179		Lucan jump implementation by Yuri
						(parsing floaters)
Main.java			219-234		Override command removed

Main.java			258-277		add support for HyperLinks to handle
						linking to Scripture text in Bible.java

Main.java			301-302		Get number of days to next year's Pascha (nDayF)
Main.java			309-310		Store it in the Hashtable
Main.java			314-318		Implementation of Lucan jump
Main.java			357-371		Implementation of Lucan jump
Main.java			395-483		Implementation of Lucan jump
Main.java			459		Modified by A.A. for hyperlinking
Main.java			473		Modified by A.A. for hyperlinking
Main.java			487		Modified by A.A. for hyperlinking

DivineLiturgy.java		created		Implementation of Lucan jump
DivineLiturgy.java		517		Modified by A.A. for hyperlinking

xml/pentecostarion/149.xml \
 ...			    >			Corrected for Lucan jump implementation
xml/pentecostarion/315.xml /

OrderedHashtable.java		129-132		Created keySet() function
OrderedHashtable.java		153-179		Created OrderedSet class

TODO:	
	* Create a toggle for Lucan Jump / non-Lucan Jump
	* Outsource DivineLiturgy.java Supress and Leap functions to XML
-------------------------------------------------------------------------------------------

RELEASE NOTES
Ponomar PRE-RELEASE VERSION 0.0.35 Revision D
DATE: 01/28/2008 (01/15/2008 old-style)
PLACE: DURHAM, NORTH CAROLINA

The present minor update corrects an error in the Theophany jump implementation.

SUMMARY OF CHANGES SINCE 0.0.35c

File				Line #		Description
------------------------------+---------------+-------------------------------------------------------------------------
xml/pentecostarion/302.xml	8, 9		Readings for the 17th week used
						instead of readings for the 31st
						Source: Jordanville calendar for 2008.
xml/pentecostarion/295.xml	6, 7		Readings for the 31st week used.
				8, 9		Supressed. Can be deleted safely.

An error in the initial implementation of the Theophany jump improperly provided
Scripture reading information for the 35th and 36th Sundays after Pentecost.
------------------------------------------------------------------------------------------------------------------------

RELEASE NOTES
Ponomar PRE-RELEASE VERSION 0.0.35 Revision C
DATE: 12/29/2007 (12/16/2007 old-style)
PLACE: DENVER, COLORADO

The present minor update implements Theophany jump methodology for all scripture readings.

SUMMARY OF CHANGES SINCE 0.0.35b

File				Line #		Description
------------------------------+---------------+-------------------------------------------------------------------------
xml/pentecostarion/282.xml			Created these files
and so on...					Scriputre readings implemented using
						Theophany jump methodology.

OrderedHashtable.java		124		Created a method values(), returning
						the keys of the Hashtable as a Collection,
						preserving the ordering.

OrderedHashtable.java		148		Created the inner class OrderedCollection
						for the values() implementation.

OrderedHashtable.java		163		Created the inner class ValuesIterator
						for the OrderedCollection.iterator() 
						implementation.


Known issues: the Lucan jump (Vozdvizheskaya otstupka) has not been implemented as of yet.
Therefore, Scripture readings info provided by Ponomar will agree with Jordanville calendar,
but will diverge from Moscow Patriarchate rubrics by one-two weeks.
------------------------------------------------------------------------------------------------------------------------

RELEASE NOTES
Ponomar PRE-RELEASE VERSION 0.0.35 Rev. B
DATE: 12/05/2007 (11/22/2007 old-style)
PLACE: DURHAM, NORTH CAROLINA

The present minor update fixes important problems in the calculation of Sunday gospel and epistle readings.
The readings for other days following the 32nd week are not yet displayed.
This issue will be addressed in the next release.

SUMMARY OF CHANGES SINCE 0.0.35a

File				Line #		Description
------------------------------+---------------+-------------------------------------------------------------------------
JDate.java			373		addMonths(int) has been changed to addMonths()
JDate.java			381		subtractMonths(int) has been changed to subtractMonths()

Main.java			188		An Override parameter for Scripture readings 
						has been implemented.
						The Override parameter is applied in certain readings
						such as SATHEO/SBTHEO, SBN, SSBN, SAN to override appointed Sunday readings
						with those specific to the floater.
xml/triodion/70.xml				Matins gospel functionality added
xml/triodion/63.xml				Matins gospel functionality added
xml/triodion/56.xml				Matins gospel functionality added
xml/triodion/49.xml				Matins gospel functionality added
xml/triodion/42.xml				Matins gospel functionality added
xml/triodion/35.xml				Matins gospel functionality added
xml/triodion/28.xml				Matins gospel functionality added
xml/triodion/21.xml				Matins gospel functionality added
xml/triodion/14.xml				Matins gospel functionality added
xml/float/3.xml					Override functionality added
xml/float/2.xml					Override functionality added
xml/float/5.xml					Override functionality added
xml/float/16.xml				Override functionality added
xml/12/24.xml					Override functionality added
xml/12/25.xml					Override functionality added
xml/12/26.xml					Override functionality added
xml/1/1.xml					Override functionality added
xml/1/5.xml					Override functionality added
xml/1/6.xml					Override functionality added
********************* SEE NOTE (A) BELOW *********************************************
xml/pentecostarion/246.xml			Correct handling of SSBN added
xml/pentecostarion/253.xml			Correct handling of SSBN added
xml/pentecostarion/267.xml			Theophany jump methodology implemented
xml/pentecostarion/274.xml			Theophany jump methodology implemented
xml/pentecostarion/281.xml			Theophany jump methodology implemented
xml/pentecostarion/288.xml			Theophany jump methodology implemented
xml/pentecostarion/295.xml			Theophany jump methodology implemented
xml/pentecostarion/302.xml			Theophany jump methodology implemented
xml/pentecostarion/309.xml			Theophany jump methodology implemented

(A) In figuring out the Theophany jump, a spreadsheet of the Indiction has been used.
The spreadsheet, lectionary.ods, has been attached. Consult the notes therein for 
additional information on the implementation.
------------------------------------------------------------------------------------------------------------------------


RELEASE NOTES
Ponomar PRE-RELEASE VERSION 0.0.35 Rev. A
DATE: 09/18/2007 (09/05/2007 old-style)
PLACE: DURHAM, NORTH CAROLINA

This is a minor update of the Ponomar program. No new features have been added.

SUMMARY OF CHANGES SINCE 0.0.35

File				Line #		Description
------------------------------+---------------+-------------------------------------------------------------------------
JDate.java			292		Method getDoy() has been created.
						getDoy() returns the doy (Day-of-Year) integer for any day.
						The doy is defined as follows:
							doy = 366 if the day is February 29
							    =  # of days since January 1 of this year, otherwise

Main.java			310		To obtain the doy, JDate.getDoy() is now called.

xml/02/09.xml			2		Fixed calculation of the Apodosis of the Meeting of the Lord.
				4		Fixed a typo.

xml/02/03.xml - 08.xml				All calculation of the Apodosis of the Meeting of the Lord
						has been moved to the Menaion xml files (instead of the Triodion),
						using Cmds involving nday. Example (from 04.xml):

						<SAINT Name="19" Cmd="nday == -49" />
						Implies that Apodosis falls on Feb 04 IFF it's Cheesefare Sunday.

xml/triodion/49.xml	\			
xml/triodion/50.xml	 |
xml/triodion/52.xml	  >			All calculation of the Apodosis of the Meeting of the Lord 
xml/triodion/54.xml	 |			has been deleted from these files.
xml/triodion/58.xml	/
------------------------------------------------------------------------------------------------------------------------

RELEASE NOTES
Ponomar PRE-RELEASE VERSION 0.0.35
DATE: 08/08/2007 (07/26/07 old-style)
PLACE: DENVER, COLORADO

This is a major revision of the Ponomar program(me).
Although it will not be noticed by the end user, many changes have taken place in the code in order
for the sake of efficiency as we move forward to service assembly functionality.

CHANGES SINCE 0.0.34d

File				Line #		Description
------------------------------+---------------+-------------------------------------------------------------------------
StringOp.java			all		The class StringOp has been created to handle all additional
						String operations required by the program. These include the join() functions,
						the capitalize() function and eval(). These functions have
						been deleted from Main.java. Now, call StringOp.eval() instead.

StringOp.java			74		eval() has been completely rewritten by Yuri Shardt, supporting
						correct OOO and mathematical operators.

						I have not changed anything in Yuri's version except:
						 - lines 377 and 382, fixing a minor issue in handling booleans
						 - My comment concerning line 452, which I'll reproduce below:
<quote>
		80 < Easter < 100 is "true" since the function first evaluates 80 < Easter to be 1, which is < 100.
		Analogously, 60 < Easter < 80 is evaluated true, when in fact it should be false.

		In reality x < y < z is nice functionality, but it does not appear to be supported natively
		by e.g. Perl's eval() function.  That is too bad, but there isn't much we can do about it.
		I suggest reverting back to 80 < Easter && Easter < 100 for the sake of cross-language compatibility.

		A. ANDREEV, 8/1/2007 N.S.
</quote>
						This became an issue in the implementation of floaters (see below).

Paschalion.java			403		getFloaters(int) has been deprecated.
						All floaters are now handled directly by xml cmds in relevant menaion
						and triodion files.
						For this purpose, the variable doy (day-of-year) was added to dayInfo,
						containing the number of days since Jan 1 of that year.

						The implementation of floaters has been as follows:
In a menaion file, e.g. /xml/01/05.xml, lines have been added:
<SAINT Name="0" Cmd="dow == 0" />
<SAINT Name="6" Cmd="dow == 6" />

In parsing the SAINT tag, the program only processes the contents if dow (day-of-week) is 0 (Sunday) or 6 (Saturday) respectively.
If processing continues (i.e., cmd is true), the program verifies if Name is numeric (in Main.java, line 154);
if Name is numeric, the program loads /xml/floaters/##.xml for processing. Otherwise, it continues as usual.

Analagous functionality has been implemented for the Apodosis of the Meeting of the Lord (floater #19). E.g.
/xml/triodion/58.xml, line 1 reads:
<SAINT Name="19" Cmd="36 &lt;= doy &amp;&amp; doy &lt;= 38" />
Hence, #19 occurs iff tridion #58 (Meatfare Friday) coincides with doy #36, #37, #38 (February 5, 6, or 7).

A full table of the way the Apodosis is rendered is enclosed below, as an appendix. Please also read my note in the Appendix.

As such, Main.java lines 95-100 dealing with the Apodosis have been deprecated.

/xml/floaters/18.xml		deleted 	No longer necessary ("Saturday before Next Year's Theophany")

Main.java 			267		getTone() has been deprecated.
						Tones are now handled directly in xml.

						The implementation is as follows:
Each tone has a numeric value. 0 is tone 8. 1 is tone 1 and so on. -1 represents no tone.
As such, Main.java line 48 has been modified.
For each file in /xml/pentecostarion/*.xml, Tone has been assigned to the SAINT tag reflecting the (fixed) tone.
For each file in /xml/triodion/*.xml, Tone has been set to ((ndayP - 49 - dow) / 7 + 7) % 8,
where ndayP is the number of days since last year's Pascha (in dayInfo) and dow is the day of week (in dayInfo).
Processing tones takes place in Main.java, line 205. If Tone is not null, StringOp.eval() is used to get its numeric value.
The value is then printed as previously by reading the Roman Numerals from an array (unless Tone == -1).

It is important to note that tone has been removed from dayInfo in Main.java, line 316;
it is feasible that tone will be required in dayInfo for the assembly of services. Should that be the case,
a way of adding it to dayInfo *before* calling the relevant assembly code but after processing the xml needs to be designed.

Additionally, the calendar control in the upper-left-hand corner still uses a getTone() routine for its tones. 
This control will probably need to be redesigned.

Other portions of Main.java dealing with tones and floaters (lines 366-391) have been deprecated.
Lines 397-400 have been cleaned up.

I've left most of the deprecated code as comments so that, should the need arise, the new implementation could be
checked against the old, which was known to be correct.

-------------------------------------------------------------------------------------------------------------------
APPENDIX:
LOGICAL TABLE FOR THE CELEBRATION OF THE MEETING OF THE LORD, ITS AFTERFEAST AND APODOSIS (LEAVE-TAKING)

BY ALEKSANDR ANDREEV

FEBRUARY - APODOSIS OF THE MEETING OF THE LORD - SUMMARY OF TYPICON RULES

Day of February	2	3	4	5	6	7	8	9

apodosis		-49	-49	-49
(in days				-50	-50
 before					-52	-52
 Pascha)				-54	-54	-54	-54
					-58	-58	-58	-58 (?) **
									DEFAULT

POSSIBILITIES FOR THE APODOSIS OF THE MEETING OF THE LORD - LOGICAL TABLE
DAY OF FEB	2	3	4	5	6	7	8	9
		-51	-50	|-49|<-- APODOSIS LABELED WITH | |	
D		-52	-51	-50	|-49|
A		-53	-52	-51	|-50|	
Y		-54	-53	-52	-51	|-50|	
S		-55	-54	-53	|-52|
		-56	-55	-54	-53	|-52|
B		-57	-56	-55	|-54|
E		-58	-57	-56	-55	|-54|
F		-59	-58	-57	-56	-55	|-54|
O		-60	-59	-58	-57	-56	-55	|-54|
R		-61	-60	-59	|-58|
E		-62	-61	-60	-59	|-58|
 		-63	-62	-61	-60	-59	|-58|
P		-64	-63	-62	-61	-60	-59	|-58| (?) <-----CHECK THIS **
A		-65	-64	-63	-62	-61	-60	-59	|-58|
S		-66	-65	-64	-63	-62	-61	-60	|-59|
C		-67	-66	-65	-64	-63	-62	-61	|-60|
H		.	.	.	.	.	.	.	|.|
A		.	.	.	.	.	.	.	|.|
		.	.	.	.	.	.	.	|.|

** Nota Bene: The Typicon (at least as laid out at http://www.orthlib.info/Typikon/T48-f.pdf) does not appear to 
have a rubric for what happens when the Meeting of the Lord falls on the Saturday before the Sunday of the Prodigal Son
(in our notation, nday# -64). If the default is used, then the Apodosis falls on Meatfare Saturday (# -57);
but there are no rubrics in the Typicon for this scenario. Hence, **I have assumed** (perhaps incorrectly)
that the Apodosis falls on Meatfare Friday (# -58), for which there are rubrics. But I am not at all sure.
However, according to http://www.klikovo.ru/db/book/msg/4897, "as a rule, the Apodosis does not fall on Meatfare
Saturday or Sunday ...", which makes me think my assumption is not completely erroneous. Further research of this topic 
is necessary.
-----------------------------------------------------------------------------------------------------------------------

RELEASE NOTES
Ponomar PRE-RELEASE VERSION 0.0.34 REVISION D
DATE: 07/19/2007 (07/06/2007 old-style)
PLACE: DENVER, COLORADO

This is a minor revision of the Ponomar program(me). Limited handling of fast regulations was added. Minor bugs fixed.

CHANGES SINCE 0.0.34c

File				Line #		Description
------------------------------+---------------+-------------------------------------------------------------------------
Main.java			671		The variable nday is now loaded into the Hashtable instead of lineNumber
						lineNumber = |nDay|. This is known to affect xml/02/01.xml and xml/02/02.xml
						*** Need to check if this affects any other xml files ***
Main.java			220-230		Method convolve(int, int) was created to handle fast regulations information
Main.java			55		Stack fastInfo was created to store fast information
Main.java			199-202		Processing of the <FAST> tag was added to startElement(String, Hashtable)
Main.java			765-774		Output of fast regulations added to write()
xml/02/01.xml			2-3, 12-17	The day number was changed to -49 because nDay is now used (see above)
xml/02/02.xml			2, 6-11		The day number was changed to -48 because nDay is now used (see above)
xml/03/24.xml			10-14		Correct handling of Scripture readings for Annunciation added
xml/03/25.xml			5-12		Correct handling of Scripture readings for Annunciation added

KNOWN ISSUES

* Fasting information is wrong for Apostles' Fast; the correct cmds must be inserted into the xml files
* Fasting information is wrong for certain days of Lent for the same reason

------------------------------------------------------------------------------------------------------------------------

RELEASE NOTES
Ponomar PRE-RELEASE VERSION 0.0.34 REVISION C
DATE: 06/18/2007 (06/5/2007 old-style)
PLACE: ST PETERSBURG, RUSSIA

This is a minor revision of the Ponomar program(me). No major features were added.

CHANGES SINCE 0.0.34b

File				Line #		Description
------------------------------+---------------+-------------------------------------------------------------------------
Main.java			649		The variable nday is now loaded into the Hashtable before reading the xml files.
						This fixes an earlier bug whereby xml commands were not read properly.
Main.java			257		Fixed a bug in substr(). This allowes parentheses to be placed at front of expression.
Main.java			247		Added capability for the ! (not) operator
Main.java			279		We now check to see if the parentheses close properly.
						If they do not, an IllegalArgumentException is thrown.
Main.java			357 etc.	We now check to see if the variable provided exists in the Hashtable.
						If it does not, an IllegalArgumentException is thrown.
xml/pentecostarion/118.xml	4		An underscore was added after Mt
