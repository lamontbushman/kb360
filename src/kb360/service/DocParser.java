package kb360.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

/*****************************
 * DOCPARSER Class: This class will retrieve the text from the .doc Document and
 *                  return it as a string
 *****************************/
public class DocParser extends Parser
{
   private File file;                // The .docx file;
   private FileInputStream finStream;// The connection to the doc as an input stream
   private HWPFDocument dotDoc;      // Reads input stream into an extractable format 
   private WordExtractor extractionator; // Extracts the text
   
   public DocParser(File pFile)
   throws IOException, FileNotFoundException
   {
      try
      {
         file = pFile;
         fileName = file.getName();
         finStream = new FileInputStream(file.getAbsolutePath());
         dotDoc = new HWPFDocument(finStream);
         bodyText = "";
         extractionator = new WordExtractor(dotDoc);
      }
      catch (IOException ioe)
      {
         System.out.println("No document of that name was found");
      }
   }
    
   /**
    * Parses the document into a string
    * @return Whether the parse was successful.
    */
   @Override
   public boolean parse()
   {
            
      bodyText = extractionator.getText();
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