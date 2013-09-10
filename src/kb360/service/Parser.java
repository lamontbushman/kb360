package kb360.service;

/**
 * PARSER: an abstract class that will be defined to parse different document
 *         types. It holds the file name and text of the document as a String
 */
public abstract class Parser
{
   protected String fileName;
   protected String bodyText;

   
   public abstract boolean parse();
  
    
   public String getFileName()
   {
      return fileName;
   }

   public String getBody()
   {
      return bodyText;
   }
}
