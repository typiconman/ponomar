package net.ponomar.utility;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.swing.filechooser.FileFilter;

import net.ponomar.internationalization.LanguagePack;

import javax.print.*;
import javax.print.attribute.*;
import java.awt.print.Printable;

public class Helpers {
	// private LanguagePack Text=new LanguagePack();
	// private String[]
	// PrimesNames=Text.obtainValues((String)Text.Phrases.get("Primes"));
	private StringOp analyse = new StringOp();

	public Helpers(LinkedHashMap<String, Object> dayInfo) {
		analyse.setDayInfo(dayInfo);
	}

	public boolean closeFrame(String title) {
		LanguagePack text = new LanguagePack(analyse.getDayInfo());
		String[] languageNames = text.obtainValues(text.getPhrases().get(Constants.LANGUAGE_MENU));

		Object[] options = { languageNames[3], languageNames[5] };
		// JOptionPane pane=new JOptionPane();
		Integer selectedValue = JOptionPane.showOptionDialog(null, title, text.getPhrases().get("0"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		// Object selectedValue = pane.getValue();
		// System.out.println(selectedValue);
		if (selectedValue == null) { // I WILL TREAT THIS AS NO
			return false;
			// If there is an array of option buttons:
		}
		if (options[1].equals(selectedValue)) {
			return false;
		}
		if (selectedValue instanceof Integer) {
			return selectedValue == 0;
		}
		return true;
	}

	public void saveHTMLFile(String defaultname, String strOut) {
		LanguagePack text = new LanguagePack(analyse.getDayInfo());
		String[] languageNames = text.obtainValues(text.getPhrases().get(Constants.LANGUAGE_MENU));
		String[] helperNames = text.obtainValues(text.getPhrases().get("Helpers"));
		String[] aboutNames = text.obtainValues(text.getPhrases().get("About"));
		JFileChooser fileSelector = new JFileChooser();
		File fileSelected = new File(defaultname);
		fileSelector.setDialogTitle(helperNames[0]);
		fileSelector.setSelectedFile(fileSelected);
		fileSelector.setFileFilter(new JavaFileFilter());
		int result = fileSelector.showSaveDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			File fileName = fileSelector.getSelectedFile();
			if (!fileName.getName().endsWith(".html")) {
				fileName = new File(fileName.getPath() + ".html");
			}
			if (fileName.exists()) {
				// CHECK WHETHER IT IS DESIRED TO OVERWRITE THE FILE
				Object[] options = { languageNames[3], languageNames[5] };
				JOptionPane pane = new JOptionPane();
				pane.showOptionDialog(null, languageNames[6] + "\n " + fileName.getPath(), text.getPhrases().get("0"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				Object selectedValue = pane.getValue();
				if (selectedValue == null)
					// I WILL TREAT THIS AS NO
					return;
				// If there is an array of option buttons:
				if (options[1].equals(selectedValue))
					return;
				if (selectedValue instanceof Integer) {
					if ((Integer) selectedValue != 0) {
						return;
					}
				}
			}

			// CREATE THE LOCATION AND WRITE THE FILE
			try {
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8));

				// This removes the body end tag, and strips out hyperlinks
				String strippedHtml = strOut.replace("</body>", "").replace("</A>", "")
						.replaceAll("<A Href=[a-zA-z0-9#=:?,\'\"-]+>", "");

				out.write(strippedHtml + "<BR><BR><i>" + getCopyright() + "</i></body></html>");
				if (strippedHtml.substring(0, 4).equals("<html>")) {
					out.write("</html>");
				}
				out.close();
			} catch (Exception e1) {
				System.out.println(fileName);
				e1.printStackTrace();
			}

		}
		return;

	}

	public void sendHTMLToPrinter(Printable obj) {
		// html += "<BR><BR><i>" + AboutNames[3]+ " "+ (String)
		// ConfigurationFiles.Defaults.get("Year") + " " +AboutNames[4] + " " +(String)
		// ConfigurationFiles.Defaults.get("Authors")+"</i>";

		DocFlavor flavor = new DocFlavor("application/x-java-jvm-local-objectref", "java.awt.print.Printable");
		PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, null);
		PrintService svc = PrintServiceLookup.lookupDefaultPrintService();
		PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
		if (services.length == 0) {
			System.out.println("Sorry, but you don't have any printers installed.");
			return;
		}
		PrintService service = ServiceUI.printDialog(null, 50, 50, services, svc, null, attributes);

		if (service != null) {
			DocPrintJob job = service.createPrintJob();
			SimpleDoc doc = new SimpleDoc(obj, flavor, null);
			try {
				job.print(doc, attributes);
			} catch (PrintException e) {
				System.out.println("Failed to print!");
				e.printStackTrace();
			}
		}
	}

	public void applyOrientation(Component c, ComponentOrientation o) {
		// Based on the programme written in the book Java Internationalisation.
		c.setComponentOrientation(o);
		if (c instanceof JMenu) {
			JMenu menu = (JMenu) c;
			int ncomponents = menu.getMenuComponentCount();
			for (int i = 0; i < ncomponents; i++) {
				applyOrientation(menu.getMenuComponent(i), o);
			}
		} else if (c instanceof Container) {
			Container container = (Container) c;
			int ncomponents = container.getComponentCount();
			for (int i = 0; i < ncomponents; i++) {
				applyOrientation(container.getComponent(i), o);
			}
		}
	}

	public String langFileFind(String languagePath, String basePath) {

		final String addon = Constants.LANGUAGES_PATH + "/";
		String currentPath = languagePath;
		/*
		 * if(LanguagePath==null){ return "ERROR"; }
		 */
		File testFile = new File(addon + languagePath + basePath);

		// System.out.println(currentPath);
		while (!testFile.exists()) {
			if (currentPath.length() <= 1) {
				currentPath = "";
				break;
			}
			String shorter = currentPath.substring(0, currentPath.length() - 2);
			int location = shorter.lastIndexOf('/');
			if (location == -1) {
				// No localised files found use top level domain file.
				currentPath = "";
				break;
			}
			currentPath = currentPath.substring(0, location) + "/";
			testFile = new File(addon + currentPath + basePath);
			// System.out.println(currentPath);
		}

		return addon + currentPath + basePath;
	}

	public String getCopyright() {

		LanguagePack text = new LanguagePack(analyse.getDayInfo());
		// String [] AboutNames=Text.obtainValues((String)Text.Phrases.get("About"));
		String[] authors = text.obtainValues(text.getPhrases().get("Authors"));
		String year = text.getPhrases().get("Year");
		String comma = text.getPhrases().get("Comma");
		String and = text.getPhrases().get("And");
		StringBuilder authorList = new StringBuilder(authors[0]);
		if (authors.length > 2) {
			for (int i = 1; i < authors.length - 1; i++) {
				authorList.append(authors[i]).append(comma);
			}
		}
		if (authors.length > 1) {
			authorList.append(and).append(authors[authors.length - 1]);
		}

		String copyright = text.getPhrases().get(Constants.COPYRIGHT);
		copyright = copyright.replace("^YY", year);
		copyright = copyright.replace("^AA", authorList.toString());
		return copyright;
	}
}

class JavaFileFilter extends FileFilter {
	public boolean accept(File file) {
		if (file.getName().endsWith(".html"))
			return true;
		return file.isDirectory();
	}

	public String getDescription() {
		return "HTML Files (.html)";
	}
}
