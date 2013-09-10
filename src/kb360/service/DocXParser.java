package kb360.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

/*****************************
 * DOCPARSER Class: This class will retrieve the text from a .docx Document and
 *                  return it as a string
 *****************************/
public class DocXParser extends Parser
{
   private File file;                // The Doc file
   private FileInputStream finStream;// The connection to the doc as an input stream
   private XWPFDocument dotDocx;      // Reads input stream into an extractable format 
   private XWPFWordExtractor extractionator; // Extracts the text
   
   public DocXParser(File pFile)
   throws IOException, FileNotFoundException
   {
    
      file = pFile;
      fileName = file.getName();
      finStream = new FileInputStream(file.getAbsolutePath());
      dotDocx = new XWPFDocument(finStream);
      bodyText = "";
      extractionator = new XWPFWordExtractor(dotDocx);
   }
    
   /**
    * Parses the whole document into a string
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