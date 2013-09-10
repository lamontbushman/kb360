package kb360.service;

//Java Libraries
import java.io.*;
import java.util.*;

//Lucene Libraries
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;

/**
 * 
 */
public class AddIndex
{
   private Parser parser;
   private File mIndexFolder;
   private IndexWriter indexWriter;
    
   public AddIndex(File indexFolder)
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
    
   public File[] add(File[] files)
   {
      ArrayList<File> filesList = new ArrayList<File>();
      String removeFiles = " ";
      
      for (File file : files)
      {
         if (add(file))
            filesList.add(file);
         else
            removeFiles += file + "\n";
      }

      if (!" ".equals(removeFiles))
         System.out.println("The following files are not supported. Please Remove.\n" + removeFiles);
      
      try
      {
         indexWriter.commit();
         indexWriter.deleteUnusedFiles();
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
      return filesList.toArray(new File[filesList.size()]);
   }

   private boolean add(File file)
   {
      try
      {
         parser = ParserFactory.createParser(file.getCanonicalPath());

         if (parser != null)
         {
            parser.parse();
         }
         else
         {
            System.out.println(
               file.getPath() + " is not a supported file! " +
                "Please remove from folder."
                );
            return false;
         }

         
         Document doc = new Document();

         //parent of index folder
         String folder = mIndexFolder.getParentFile().getCanonicalPath() +
            File.separator;

         //path to file within parent of the index folder
         String path = "";
         
         // '\\' causes regex problems with replaceAll
         if (File.separator.equals("\\"))
         {
            folder = folder.replace("\\","/");
            path = file.getCanonicalPath().replace("\\","/").replaceFirst(folder,"");
         }
         else
         {
            path = file.getCanonicalPath().replaceFirst(folder,"");
         }

         
         Field relativeURLField = new StringField(
            "path",path, Field.Store.YES);
         doc.add(relativeURLField);
         
         Field lastModified = new LongField(
            "date", file.lastModified(), Field.Store.YES);
         doc.add(lastModified);

         Field bodyField = new TextField(
            "body",parser.getBody(), Field.Store.YES);
         doc.add(bodyField);

         // Take out when all testing is done.
         try
         {
            indexWriter.addDocument(doc);
         }
         catch(Exception e)
         {
            System.out.println("IndexWriter is null!!!" + e);
         }
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
      return true;
   }

   public void close()
   {
      try
      {
         indexWriter.close();
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
   }
}
