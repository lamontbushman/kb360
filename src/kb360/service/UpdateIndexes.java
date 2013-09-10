package kb360.service;

import java.io.*;

/**
 * 
 */
public class UpdateIndexes
{
   UpdateIndex[] updateIndexes;
   String results;

   /**
    *
    */
   public UpdateIndexes(File[] folders)
   {
      results = "";
      updateIndexes = new UpdateIndex[folders.length];
      
      for (int i = 0; i < folders.length; i++)
      {
         updateIndexes[i] = new UpdateIndex(folders[i]);
         if (!updateIndexes[i].exists())
         {
               updateIndexes[i].create();
         }
      }
   }
   
   /**
    *
    */
   private boolean update(UpdateIndex index)
   {
      return index.update();
   }
   
   /**
    *
    */
   boolean update()
   {
      boolean allUpdated = true;
      for (UpdateIndex index : updateIndexes)
      {
         boolean updated = update(index);
         results += index.getResults() + "\n";
         
         if (allUpdated)
         {
            allUpdated = updated;
         }
      }
      return allUpdated;
   }

   /**
    *
    */
   void showResults()
   {
      System.out.println("Results:\n" + results);
   }

   /**
    *
    */
   public static void main(String args[])
   {
      if ("true".equals(System.getProperty("desktop","false")))
      {
         OptionPanePrintStream stream = new OptionPanePrintStream(
            new ByteArrayOutputStream());
         System.setOut(stream);
         System.setErr(stream);
      }

      Configuration config = new Configuration();
      if (!config.isSet())
      {
         config.initialize();  
      }

      File[] files = new File[2];

      files[0] = new File(config.getProperty("student"));
      files[1] = new File(config.getProperty("admin"));

      UpdateIndexes updater = new UpdateIndexes(files);
      updater.update();
      updater.showResults();
   }
}
