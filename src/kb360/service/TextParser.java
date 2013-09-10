package kb360.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Parses a text file.  We are going to let the user be smart enough to send
 *  it a text file.  We won't care about the file extension.
 *  However, ParserFactory will return an appropriate parser based on the actual
 *  file and extension.
 */
public class TextParser extends Parser
{
   private File mFile;
   
   public TextParser(File file)
   {
      fileName = file.getName();
      if (file.exists())
      {
         mFile = file;
      }
      mFile = file;
      bodyText = "";
   }
   
   @Override
   public boolean parse()
   {
      try
      {
         if(!mFile.exists())
         {
            System.out.println(mFile + " doesn't exist");
         }
         Scanner scanner = new Scanner(mFile.getAbsoluteFile());
         while(scanner.hasNext())
         {
            bodyText += scanner.nextLine() + "\n";
         }
      }
      catch(FileNotFoundException fnfe)
      {
         fnfe.printStackTrace();
      }
      return false;
   }
}
