package kb360.service;

import java.io.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;

/**
 * 
 */
public class DeleteIndex
{
   private IndexWriter indexWriter;
   private File mIndexFolder;
   
   DeleteIndex(File indexFolder)	
   {
      mIndexFolder = indexFolder;
      try
      {
         Directory dir = FSDirectory.open(indexFolder);
         Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
         IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_41, analyzer);
         iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
         indexWriter = new IndexWriter(dir, iwc);
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
   }

   public boolean close()
   {
      try
      {
         indexWriter.close();
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
         return false;
      }
      return true;
   }

   public boolean delete(File[] files)
   {
      boolean deletedAll = true;
      for (File file : files)
      {
         boolean deleted = delete(file);
         if (deletedAll)
         {
            deletedAll = deleted;
         }
      }
      try
      {
         indexWriter.commit();
         indexWriter.deleteUnusedFiles();
      }
      catch (IOException ioe)
      {
         System.out.println("Error in commiting deleted items");
      }
      return deletedAll;
   }
   
    
   public boolean delete(File file)
   {
      try
      {
         //parent of index folder
         String folder = mIndexFolder.getParentFile().getCanonicalPath() +
            File.separator;

         //Change \\ to / if windows, because it causes replaceFirst to crash
         if (File.separator.equals("\\"))
         {
            folder = folder.replace("\\","/");
         }
         
         //path to file within parent of index
         String path = file.getCanonicalPath().replaceFirst(folder,"");
         
         indexWriter.deleteDocuments(new TermQuery(new Term("path",path)));
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
         return false;
      }
      return true;
   }
}
