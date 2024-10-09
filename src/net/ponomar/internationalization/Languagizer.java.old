package net.ponomar.internationalization;

import javax.swing.*;
import javax.swing.event.*;

import net.ponomar.calendar.JDate;
import net.ponomar.parsing.DocHandler;
import net.ponomar.parsing.QDParser;
import net.ponomar.utility.OrderedHashtable;
import net.ponomar.utility.StringOp;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

class Languagizer extends JFrame implements DocHandler, ListSelectionListener, ActionListener
{

	private String bmlfile;
	private JList russianBox;
	private JList englishBox;
	private JList frenchBox;
	private JLabel russianLabel = new JLabel();
	private JLabel englishLabel = new JLabel();
	private JLabel frenchLabel  = new JLabel();
	private JButton assignButton = new JButton("Assign...");
	private JButton priorButton  = new JButton("Previous Day ...");
	private JButton nextButton   = new JButton("Next Day ...");
	private JButton writeButton  = new JButton("Write this ...");
	private Vector russianNames = new Vector();
	private Hashtable russianIDs = new Hashtable();
	private Hashtable russianTypes = new Hashtable();
	private Vector englishNames = new Vector();
	private Hashtable englishIDs = new Hashtable();
	private Hashtable englishTypes = new Hashtable();
	private Vector frenchNames = new Vector();
	private Hashtable frenchIDs = new Hashtable();
	private Hashtable frenchTypes = new Hashtable();
	private boolean readFile = false;
	private int LS = 3;
        private StringOp Analyse=new StringOp();
	
	private JDate today = new JDate(1, 1, 2009);
	
	public Languagizer ()
	{

            super ("Languagizer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		//WE NEED THIS HANDY STORER OF VALUES NOW.
		Analyse.setDayInfo(new OrderedHashtable());
		
		Analyse.getDayInfo().put("LS", LS);
		Analyse.getDayInfo().put("nday", 1);
		Analyse.getDayInfo().put("dow", 0);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout());
		
		int month = today.getMonth();
		int day   = today.getDay();
		bmlfile = "Ponomar/xml/" + (month < 10 ? "0" + Integer.toString(month) : Integer.toString(month)) + "/" + 
			(day < 10 ? "0" + Integer.toString(day) : Integer.toString(day)) + ".xml";
		
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), "UTF8"));	//Unicodised it.
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			System.out.println("Error reading bmlfile: ");
			e.printStackTrace();
		}		

		JPanel russianPanel = new JPanel();
		russianPanel.setLayout(new BorderLayout());
		
		russianBox = new JList(russianNames);
		russianBox.addListSelectionListener(this);
		JScrollPane russianScrollPane = new JScrollPane(russianBox);
		russianPanel.add(russianScrollPane, BorderLayout.NORTH);
		russianPanel.add(russianLabel, BorderLayout.SOUTH);
		topPanel.add(russianPanel);

		LS = 0;
		Analyse.getDayInfo().put("LS", LS);
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), "UTF8"));	//Unicodised it.
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			System.out.println("Error reading bmlfile: ");
			e.printStackTrace();
		}		

		JPanel englishPanel = new JPanel();
		englishPanel.setLayout(new BorderLayout());
		
		englishBox = new JList(englishNames);
		englishBox.addListSelectionListener(this);
		JScrollPane englishScrollPane = new JScrollPane(englishBox);
		englishPanel.add(englishScrollPane, BorderLayout.NORTH);
		englishPanel.add(englishLabel, BorderLayout.SOUTH);
		topPanel.add(englishPanel);
		
		LS = 1;
		Analyse.getDayInfo().put("LS", LS);
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), "UTF8"));	//Unicodised it.
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			System.out.println("Error reading bmlfile: ");
			e.printStackTrace();
		}		

		JPanel frenchPanel = new JPanel();
		frenchPanel.setLayout(new BorderLayout());
		
		frenchBox = new JList(frenchNames);
		frenchBox.addListSelectionListener(this);
		JScrollPane frenchScrollPane = new JScrollPane(frenchBox);
		frenchPanel.add(frenchScrollPane, BorderLayout.NORTH);
		frenchPanel.add(frenchLabel, BorderLayout.SOUTH);
		topPanel.add(frenchPanel);
	
		add(topPanel, BorderLayout.NORTH);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(2, 2));
		
		assignButton.addActionListener(this);
		writeButton.addActionListener(this);
		priorButton.addActionListener(this);
		nextButton.addActionListener(this);
		bottomPanel.add(assignButton);
		bottomPanel.add(writeButton);
		bottomPanel.add(priorButton);
		bottomPanel.add(nextButton);
		
		add(bottomPanel, BorderLayout.SOUTH);
		pack();
		setSize(700, 600);
		setVisible(true);

	}

	public void startDocument() { }

	public void endDocument() { }

	public void startElement(String elem, Hashtable table)
	{
		if (table.get("Cmd") != null)
		{
			// EXECUTE THE COMMAND, AND STOP IF IT IS FALSE
			if (Analyse.evalbool(table.get("Cmd").toString()) == false)
			{
				return;
			}
		}
		if (elem.equals("LANGUAGE"))
		{
			readFile=true;
		}
		if (elem.equals("SAINT") && readFile) 
		{
			String id      = (String)table.get("Id");
			String type    = (String)table.get("Type");
			String mName   = (String)table.get("Name");
			if (LS == 3) {
				try
				{
					russianNames.add(mName);
					russianIDs.put(mName, id);
					russianTypes.put(mName, type);
				} catch (Exception e) {
				}
			}
			if (LS == 0) {
				try
				{
					englishNames.add(mName);
					englishIDs.put(mName, id);
					englishTypes.put(mName, type);
				}
				catch (Exception e) {
				}
			}
			if (LS == 1) {
				try
				{
					frenchNames.add(mName);
					frenchIDs.put(mName, id);
					frenchTypes.put(mName, type);
				}
				catch (Exception e) {
				}
			}
		}
	}
	
	public void endElement(String elem) 
	{ 
		if (elem.equals("LANGUAGE"))
		{
			readFile=false;			
		}
	}

	public void text(String text) { }
	
	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getSource().equals(russianBox))
		{
			// update russian label
			int n = russianBox.getSelectedIndex();
			String name = russianNames.elementAt(n).toString();
			String id = "null";
			String type = "null";
			if (russianIDs.containsKey(name)) {
				id = russianIDs.get(name).toString();
			} 
			if (russianTypes.containsKey(name)) {
				type = russianTypes.get(name).toString();
			} 
			russianLabel.setText("ID: " + id + "; Type: " + type);
		}
		else if (e.getSource().equals(englishBox))
		{
			// update russian label
			int n = englishBox.getSelectedIndex();
			String name = englishNames.elementAt(n).toString();
			String id = "null";
			String type = "null";
			if (englishIDs.containsKey(name)) {
				id = englishIDs.get(name).toString();
			} 
			if (englishTypes.containsKey(name)) {
				type = englishTypes.get(name).toString();
			} 
			englishLabel.setText("ID: " + id + "; Type: " + type);
		}
		else if (e.getSource().equals(frenchBox))
		{
			// update russian label
			int n = frenchBox.getSelectedIndex();
			String name = frenchNames.elementAt(n).toString();
			String id = "null";
			String type = "null";
			if (frenchIDs.containsKey(name)) {
				id = frenchIDs.get(name).toString();
			} 
			if (frenchTypes.containsKey(name)) {
				type = frenchTypes.get(name).toString();
			} 
			frenchLabel.setText("ID: " + id + "; Type: " + type);
		}

	}

	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource().equals(assignButton))
		{
			int nRu = russianBox.getSelectedIndex();
			String nameRu = russianNames.elementAt(nRu).toString();
			String idRu = russianIDs.get(nameRu).toString();
			String typeRu = russianTypes.get(nameRu).toString();
			
			int nEn = englishBox.getSelectedIndex();
			String nameEn = englishNames.elementAt(nEn).toString();
			
			int nFr = frenchBox.getSelectedIndex();
			String nameFr = frenchNames.elementAt(nFr).toString();
			
			// set
			englishIDs.put(nameEn, idRu);
			englishTypes.put(nameEn, typeRu);
			
			frenchIDs.put(nameFr, idRu);
			frenchTypes.put(nameFr, typeRu);
		}
		else if (e.getSource().equals(priorButton))
		{
			today.subtractDays(1);
			loadDay();
		}
		else if (e.getSource().equals(nextButton))
		{
			today.addDays(1);
			loadDay();
		}
		else if (e.getSource().equals(writeButton))
		{
		
		}
	}
	
	private void loadDay()
	{
		System.out.println(today.toString());
		russianNames.clear();
		englishNames.clear();
		frenchNames.clear();
		russianIDs.clear();
		englishIDs.clear();
		frenchIDs.clear();
		russianTypes.clear();
		englishTypes.clear();
		frenchTypes.clear();
		
		LS = 3;
		Analyse.getDayInfo().put("LS", LS);
		int month = today.getMonth();
		int day   = today.getDay();
		bmlfile = "Ponomar/xml/" + (month < 10 ? "0" + Integer.toString(month) : Integer.toString(month)) + "/" + 
			(day < 10 ? "0" + Integer.toString(day) : Integer.toString(day)) + ".xml";

		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), "UTF8"));	//Unicodised it.
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			System.out.println("Error reading bmlfile: ");
			e.printStackTrace();
		}
		russianBox.setListData(russianNames);
				
		LS = 0;
		Analyse.getDayInfo().put("LS", LS);
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), "UTF8"));	//Unicodised it.
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			System.out.println("Error reading bmlfile: ");
			e.printStackTrace();
		}
		englishBox.setListData(englishNames);
		
		LS = 1;
		Analyse.getDayInfo().put("LS", LS);
		try
		{
			BufferedReader frf = new BufferedReader(new InputStreamReader(new FileInputStream(bmlfile), "UTF8"));	//Unicodised it.
			QDParser.parse(this, frf);
		}
		catch (Exception e)
		{
			System.out.println("Error reading bmlfile: ");
			e.printStackTrace();
		}
		frenchBox.setListData(frenchNames);
		
	}
	
	public static void main(String[] argz)
	{
		new Languagizer();
	}
}
