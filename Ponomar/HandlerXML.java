package Ponomar;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/***********************************************************************
THIS MODULE CREATES THE TEXT FOR THE SEARCHING FOR COMMEMORATIONS ACCROSS LANGUAGES/JURISDICTIONS

(C) 2022 YURI SHARDT. ALL RIGHTS RESERVED.


 PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE CODE
 PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES THEREOF.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 * 
 * Based on https://mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
***********************************************************************/
public class HandlerXML 
{
	//SOME QUICK NOTES FOR FURTHER IMPLEMENTATION:
	private String filelocation;
	private Document readfile;
       
		
		
	public HandlerXML(String filepath)
	{
            //This simply creates the handler
            filelocation=filepath;
        }
        
        public Document readXML()
        {
            //Reads the file and returns the processed file model
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

          // optional, but recommended
          // process XML securely, avoid attacks like XML External Entities (XXE)
          dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

          // parse XML file
          DocumentBuilder db = dbf.newDocumentBuilder();

          Document doc = db.parse(new File(filelocation));

          // optional, but recommended
          // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
          doc.getDocumentElement().normalize();
            return doc;
        } catch (ParserConfigurationException | SAXException | IOException e) 
        {
          e.printStackTrace();
          return null;
        }
       }
        public void writeXML(Document doc, OutputStream output)
        {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
      try{
            Transformer transformer = transformerFactory.newTransformer();

      // pretty print
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");

      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(output);
     
        transformer.transform(source, result);
      } catch(TransformerException e){
          e.printStackTrace();
      }

        }
        public static void writeXml(Document doc,  OutputStream output) throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);

    }
        

//Create the Interface:


     
	public String getUsualBeginning()
	{
		return "";
	}

         
        public static void main(String[] argz)
        {
           
            String filepath="C:\\Users\\yuris\\Documents\\Ponomar\\ponomar-combo\\Ponomar\\languages\\cu\\uk\\xml\\lives\\09497.xml";
            OutputStream out;
            HandlerXML saintUK=new HandlerXML(filepath);
            Document saintUKdoc=saintUK.readXML();
            System.out.println("Root Element :" + saintUKdoc.getDocumentElement().getNodeName());
          System.out.println("------");
         /* if (saintUKdoc.hasChildNodes()) {
              printNote(saintUKdoc.getChildNodes());
          }*/
         NodeList list=saintUKdoc.getElementsByTagName("NAME");
         System.out.println(list.item(0).getAttributes().getNamedItem("Short").getTextContent());
         System.out.println(list.item(1).getAttributes().getNamedItem("Nominative").getTextContent());
         System.out.println(list.item(0).getAttributes().getNamedItem("Genitive").getTextContent());
         
         NodeList listL=saintUKdoc.getElementsByTagName("LIFE");
         System.out.println(listL.item(0).getAttributes().getNamedItem("Id").getTextContent());
         System.out.println(listL.item(0).getAttributes().getNamedItem("Copyright").getTextContent());
         System.out.println(listL.item(0).getTextContent());
         
         NodeList listS=saintUKdoc.getElementsByTagName("SERVICE");
         NodeList listT=saintUKdoc.getElementsByTagName("TROPARION");
         System.out.println(listT.item(0).getAttributes().getNamedItem("Tone").getTextContent());
         //System.out.println(listT.item(0).getAttributes().getNamedItem("Podoben").getTextContent());
         System.out.println(listT.item(0).getTextContent());
         
         
         NodeList listK=saintUKdoc.getElementsByTagName("KONTAKION");
         System.out.println(listK.item(0).getAttributes().getNamedItem("Tone").getTextContent());
         System.out.println(listK.item(0).getAttributes().getNamedItem("Podoben").getTextContent());
         System.out.println(listK.item(0).getTextContent());
         
         //Read of the desired elements works as well as can be desired.
         //Now see if we can write the file correctly.
         listK.item(0).setTextContent("Hippocampus testing the files and trying to see what is going on and making sure that I can in fact write everything as desired!!!");
         try {
         out =new FileOutputStream("C:\\Users\\yuris\\Documents\\Ponomar\\ponomar-combo\\Ponomar\\languages\\cu\\uk\\xml\\lives\\testing.xml");
         saintUK.writeXML(saintUKdoc, out);
         } catch (FileNotFoundException e){
             System.out.println(e);
         }
         
                 
        }
        
         private static void printNote(NodeList nodeList) {

      for (int count = 0; count < nodeList.getLength(); count++) {

          Node tempNode = nodeList.item(count);

          // make sure it's element node.
          if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

              // get node name and value
              System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
              System.out.println("Node Value =" + tempNode.getTextContent());

              if (tempNode.hasAttributes()) {

                  // get attributes names and values
                  NamedNodeMap nodeMap = tempNode.getAttributes();
                  for (int i = 0; i < nodeMap.getLength(); i++) {
                      Node node = nodeMap.item(i);
                      System.out.println("attr name : " + node.getNodeName());
                      System.out.println("attr value : " + node.getNodeValue());
                  }

              }

              if (tempNode.hasChildNodes()) {
                  // loop again if has child nodes
                  printNote(tempNode.getChildNodes());
              }

              System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");

          }

      }

  }
}

