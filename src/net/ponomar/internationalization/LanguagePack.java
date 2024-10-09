package net.ponomar.internationalization;

import net.ponomar.parsing.utility.DocHandler;
import net.ponomar.parsing.utility.QDParser;
import net.ponomar.utility.Constants;
import net.ponomar.utility.Helpers;
 
 
 
import net.ponomar.utility.StringOp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import java.util.LinkedHashMap;

/***************************************************************
LanguagePack.java :: MODULE THAT DETERMINES THE LANGUAGE SPECIFIC OUTPUTS

LanguagePack.java is part of the Ponomar project.
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
**************************************************************/

public class LanguagePack implements DocHandler {
	private LinkedHashMap<String, String> requiredPhrases; // STORES ALL THE REQUIRED PHRASES FOR THE INTERFACE IN THE CURRENT
												// INTERFACE LANGUAGE.
	private boolean readPhrases = false; // DETERMINE WHETHER TO READ OR NOT TO READ THE GIVEN PHRASES (THIS MUST BE
											// ADDED TO ALL THE READERS).
	private final StringOp analyse = new StringOp();

	public LanguagePack(LinkedHashMap<String, Object> dayInfo) {
		setPhrases(new LinkedHashMap<>());
		analyse.setDayInfo(dayInfo);
		readPhrases();

	}

	public LanguagePack(String path, LinkedHashMap<String, Object> dayInfo) {
		analyse.setDayInfo(dayInfo);
		setPhrases(new LinkedHashMap<>());
		readPhrases(path);

	}

	private void readPhrases() {
		Helpers getFile = new Helpers(analyse.getDayInfo());
		readPhrases(getFile.langFileFind(analyse.getDayInfo().get("LS").toString(), Constants.LANGUAGE_PACKS));
	}

	private void readPhrases(String langPath) {
		String filename = langPath;
		try {
			// ALLOWS MULTILINGUAL SUPPORT, WHICH IS A MUST IN OUR CASE.
			BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
			// FileReader fr = new FileReader(filename);
			QDParser.parse(this, fr);
		} catch (Exception e) {
			// THIS STATEMENT CANNOT BE MULTILINGUAL!
			System.out.println("Unable to find " + filename);
			System.out.println(e.toString());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				System.out.println(e.getStackTrace()[i].toString());
			}
			System.out.println("------------------");
		}

	}

	public String[] obtainValues(String in) {
		// THIS FUNCTION TAKES A STTRING SEPARATED BY '\,' AND RETURNS A STRING ARRAY.
		return in.split("/,");
	}

	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startElement(String elem, HashMap<String, String> table) {
		// THE TAG COULD CONTAIN A COMMAND Cmd
		// THE COMMAND TELLS US WHETHER OR NOT TO PROCESS THIS TAG GIVEN
		// TODAY'S INFORMATION IN dayInfo.
		//String language = "";

		if (table.get("Cmd") != null) {
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
			if (!analyse.evalbool(table.get("Cmd"))) {
				return;
			}
		}
		// if(elem.equals("LANGUAGE"))
		// {
		readPhrases = true;
		// }
		if (elem.equals("PHRASE") && readPhrases) {

			String key = table.get("Key");
			String value = table.get(Constants.VALUE);
			getPhrases().put(key, value);
			// System.out.println("The current language is " + Language + ". The phrases are
			// " +Phrases);
		}
	}

	public void endElement(String elem) {
		if (elem.equals(Constants.LANGUAGE)) {
			readPhrases = false;
		}
	}

	public LinkedHashMap<String, String> getPhrases() {
		return requiredPhrases;
	}

	public void setPhrases(LinkedHashMap<String, String> phrases) {
		requiredPhrases = phrases;
	}

	@Override
	public void text(String str) throws Exception {
		
	}
}