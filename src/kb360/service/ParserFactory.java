package kb360.service;

import java.net.*;
import java.io.IOException;
import java.io.File;
/**
 * PARSERFACTORY Class: This class will take in HTML, .doc, .docx, PDF, and text
 *                      documents. It will determine which type of parser is
 *                      needed, and returns the result as a Parser.
 */
public final class ParserFactory
{
   public static Parser createParser(String fileName)
   throws IOException
   {
      File file = new File(fileName);
      return test(file);      
   }

/**********************   
 * TEST: This will determine which type of document is in question, and create
 *       the appropriate type of Parser.
 *********************/
   private static Parser test(File file)
   throws IOException
   {      
      String path = file.toURI().toURL().toString();
      FileNameMap fnm = URLConnection.getFileNameMap();
      String type = fnm.getContentTypeFor(path);
      
      if (type == null)
      {
         if (file.getName().contains(".docx"))
         {
            return new DocXParser(file);   
         }
         else if (file.getName().contains(".doc"))
         {
            return new DocParser(file);
         }
      }
      else
      {
         switch (type)
         {
            case "application/pdf":        
               return new PDFParser(file.getPath());         
            case "text/plain":
               return new TextParser(file);
            case "text/html":
               return new HTMLParser(file);
            default:
               System.out.println("<ParserFactory> file is not supported:  " + file + " type: " + type);
               return null;// it should not get here
         }
      }
      return null;
   }
}
