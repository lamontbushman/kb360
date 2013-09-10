package kb360.service;

// Java core libraries
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

// Lucene libraries
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Bits;

/**
 * TODO change map to array
 */
public class UpdateIndex
{
//   private Directory directory;
//   private String indexName;
   private String results;
   private boolean exists;
   private boolean updated;
   private File mIndexFolder;
   private Set<File> filesToAdd;
    
   public UpdateIndex(File folder)
   {
      filesToAdd = new HashSet<File>();
      //indexName = "index";

      if (folder.exists() && !folder.isDirectory())
      {
         System.err.println(folder + " is not a folder, but a file.");
/*         JOptionPane.showMessageDialog(
            null,folder + " is not a folder, but a file.",
            "Error",JOptionPane.ERROR_MESSAGE);*/
         System.exit(0);
      }
      
      mIndexFolder = new File(
         folder.getAbsolutePath() + File.separator + "index");

      exists = true;
       
      if (!mIndexFolder.isDirectory())
      {
         exists = false;
      }

      results = "";
   }

   private void findFilesInFolder()
   {
      try
      {
         File parent = mIndexFolder.getParentFile();
         Path start = Paths.get(parent.toString());
         Files.walkFileTree
            (start,
             new SimpleFileVisitor<Path>()
             {
                @Override
                   public FileVisitResult preVisitDirectory(
                      Path dir,BasicFileAttributes attrs)
                {
                   try
                   {  //ignore the index folder
                      if (Files.isSameFile(
                             Paths.get(mIndexFolder.toString()), dir))
                      {
                         return FileVisitResult.SKIP_SUBTREE;
                      }
                   }
                   catch (IOException ioe)
                   {
                      ioe.printStackTrace();
                   }
                   return FileVisitResult.CONTINUE;
                }
             
                @Override
                   public FileVisitResult visitFile(
                      Path file, BasicFileAttributes attrs)
                {
                   try
                   {
                      filesToAdd.add(
                         new File(file.toFile().getCanonicalPath()));
                   }
                   catch (IOException ioe)
                   {
                      ioe.printStackTrace();
                   }
                   return FileVisitResult.CONTINUE;
                }
             });
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
   }

   
   private List<File> returnFilesToDeleteAndFindFilesToBeAdded()
   {
      List<File> filesToDelete = null;
      try
      {
         Directory dir = FSDirectory.open(mIndexFolder);
         IndexReader reader = DirectoryReader.open(dir);
         int maxDoc = reader.maxDoc();
         filesToDelete = new ArrayList<File>();

         //bits will be null if there are no deleted documents
         Bits bits = MultiFields.getLiveDocs(reader);
         
         for (int i = 0; i < maxDoc; i++)
         {
            String folder = mIndexFolder.getParentFile().getCanonicalPath() +
               File.separator;
            File file = new File(folder + reader.document(i).get("path"));
                    
            // if file has been "deleted" from index do not skip adding it
            // if it was re added to the directory.
            if (bits != null && !bits.get(i))
            {
               continue;
            }
            //If the document exists in the directory and in the index
            // do not add the document if the file was not modified
            if (filesToAdd.contains(file))
            {
               String modifiedStr = reader.document(i).get("date");
               Long modified =
                  (modifiedStr != null)? Long.parseLong(modifiedStr):0;
               if (modified == file.lastModified())
               {
                  filesToAdd.remove(file);
               }
               else
               {
                  //First have to delete the document then add it.
                  filesToDelete.add(file);
               }
            }
            else
            {
               filesToDelete.add(file);
            }
         }
         reader.close();
      }
      catch(IndexNotFoundException infe)
      {
         //Means that the folder 'index' exists but there is nothing in there.
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
         return null;
      }
      
      return filesToDelete;
   }
  
   public boolean update()
   {
      updated = true;
      results += "Updated\n";
      findFilesInFolder();
      if (exists)
      {
         List<File> filesToDelete = returnFilesToDeleteAndFindFilesToBeAdded();
         if (filesToDelete != null && filesToDelete.size() != 0)
         {
            results += "DELETED FILES\n";
            for (int i = 0; i < filesToDelete.size() ; i++)
            {
               results += filesToDelete.get(i).toString() + "\n";
            }
            
            if (filesToDelete.size() != 0)
            {
               delete(filesToDelete.toArray(new File[0]));
            }
         }
      }
      exists = true;
      
      File[] files = filesToAdd.toArray(new File[0]);
      File[] actualAddedFiles = null;
      if (files.length != 0)
      {
         actualAddedFiles = add(files);
         results += "ADDED FILES\n";
      }
      
      if(actualAddedFiles != null)
         for (File fileF : actualAddedFiles)
         {
            results += fileF.toString() + "\n";
         }
      return true;
   }

   public boolean delete(File[] files)
   {
      DeleteIndex index = new DeleteIndex(mIndexFolder);
      boolean deleted = index.delete(files);
      index.close();
      return deleted;
   }

   public boolean exists()
   {
      return exists;
   }

   private File[] add(File[] files)
   {
      AddIndex index = new AddIndex(mIndexFolder);
      //returns the actual files added
      File[] addedFiles = index.add(files);
      index.close();
      return addedFiles;
   }

   public void create()
   {
      results += "Index Created\n";
      mIndexFolder.mkdirs();
   }   
    
   public String getResults()
   {
      if (!exists)
      {
         return this.toString() +
            "\nIndex folder does not exist. Call create()\n";
      }
      if (!updated)
      {
         return this.toString() + results +
            "\nwas not updated. Call update()\n";
      }

      return this.toString() + "\n"  + results;
   }

   @Override
   public String toString()
   {
      return mIndexFolder.toString();
   }
}