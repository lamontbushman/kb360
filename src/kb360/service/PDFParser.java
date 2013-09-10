package kb360.service;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;


import com.itextpdf.text.pdf.parser.*; 
import com.itextpdf.text.pdf.PdfReader;


/**
 * PDFPARSER Class: This class will retrieve the text from the PDF Document and
 *                  return it as a string
 */
public class PDFParser extends Parser
{
   
   private PdfReader mReader; // The Reader represents the document
   private int numPages;      // The number of pages in the document
      
   public PDFParser(String pdf)
   throws IOException
   {
      final FileChannel channel = new FileInputStream(pdf).getChannel();
      MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
      byte[] byteArray = new byte[buffer.remaining()];
      buffer.get(byteArray);
      channel.close();

      mReader = new PdfReader(byteArray);
      
      numPages = mReader.getNumberOfPages();
      
      bodyText = "";
   }

/*************************
 * PARSE: This function overrides parse from Parser to go page by page through
 *        the PDF document, retrieving the text.
 * @return Whether the parse was successful.
 **********************/
   @Override
   public boolean parse()
   {
      StringBuilder sb = new StringBuilder();
      
      try
      {
         for (int i = 1; i <= numPages; i++)
         {
            sb.append( PdfTextExtractor.getTextFromPage(mReader, i));
         }
         
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      
   
      mReader.close();

      bodyText = sb.toString();
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