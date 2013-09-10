package kb360.service;

import java.io.File;
import java.io.IOException;
import org.htmlparser.beans.HTMLTextBean;

/*****
 * HTMLPARSER Class: This class will retrieve the text from a .doc Document and
 *                   return it as a string
 *****/
public class HTMLParser extends Parser
{
   private File file;
   private HTMLTextBean bean;
   public HTMLParser(File pFile)
   {
      file = pFile;
      bodyText = "";
      bean = new HTMLTextBean();
   }

/*****
 * PARSE: Overrides the parser to get the text content from an HTML document
 *****/
   @Override
   public boolean parse()   
   {
      try
      {
         String path = file.toURI().toURL().toString(); // gets the path string
         bean.setURL(path);   // sets the extractor to the document
         bean.setLinks(false);
         bodyText = bean.getStrings(); // This does the extraction
      }
      catch (IOException ioe)
      {
         System.out.println("Your file was not found");
      }
      
      if (bodyText != "")
      {
         return true;
      }
      else
      {
         return false;
      }
   }  
}
