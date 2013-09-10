package kb360.service;

// Java libraries
import java.io.File;
import java.io.IOException;
import java.net.*;

// Lucene libraries
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;

/**
 * TODO add a series of search options
 */
public class SearchIndex implements Search
{
   Analyzer analyzer;   
   File mStudentIndex;
   File mAdminIndex;
   IndexSearcher studentSearcher;
   IndexSearcher adminSearcher;
   IndexReader studentReader;
   IndexReader adminReader;
   QueryParser parser;
   String field;
   String httpAccessibleURL;
      
   public SearchIndex(String studentFolder, String adminFolder, String folderURL)
   {
      mStudentIndex = new File(studentFolder + File.separator + "index");
      mAdminIndex = new File(adminFolder + File.separator + "index");
      if (!mStudentIndex.exists() || !mAdminIndex.exists())
      {
         String error = "";
         if (!mStudentIndex.exists())
            error += "Cannot find the student index at: " + mStudentIndex + "\n";
         if (!mAdminIndex.exists())
            error += "Cannot find the admin index at: " + mAdminIndex + "\n";
         System.out.print(error);
         System.exit(0);
      }
      field = "body";
      httpAccessibleURL = folderURL;
   }
   
   public void openReaders()
   {
      try
      {
         studentReader = DirectoryReader.open(FSDirectory.open(mStudentIndex));
         studentSearcher = new IndexSearcher(studentReader);
         
         adminReader = DirectoryReader.open(FSDirectory.open(mAdminIndex));
         adminSearcher = new IndexSearcher(adminReader);
         
         analyzer = new StandardAnalyzer(Version.LUCENE_41);
         parser = new QueryParser(Version.LUCENE_41, field, analyzer);
      }
      catch(IOException ioe)
      {
         System.out.println("Error: most likely no files exist in one of the folders.\n" +
                            "Make sure there is at least one document in each folder.\n" +
                            "Then update indexes");
         System.err.println(ioe);
         System.exit(0);
      }
   }

   private URL makeURL(String filePath, boolean student)
   {
      String folder = (student)?"student/":"admin/";
      String urlString = httpAccessibleURL + "/" + folder + filePath;
      urlString = urlString.replace("\\","/");
      URL url = null;
      try
      {
         url = new URL(urlString);
      }
      catch (MalformedURLException mue)
      {
         System.out.println(
            "Paths are set up incorrectly to be accessed from internet"
                            );
         mue.printStackTrace();
      }
      return url;
   }
   
   /**
    * Does a simple search. If there are multiple words, it treats the 
    *  expression as if there were AND's between each word.
    * @param query The query to be searched.
    * @return The searched results.
    */
   @Override
   public Results simpleSearch(String query, boolean student)
   {
      return simpleSearch(query,student,0);
   }

   /**
     * Same as the above simpleSearch(String, boolean), except you can specify
     *  what range of ten results you want returned.
     * (i.e) If start is 10, the results will return results 10 - 19
     * @param query The query to be searched.
     * @param student Whether the student or the admin folder should be searched.
     * @param start The beginning of the range. It is zero based.
     * @return The searched results. Will be null if there are none
     */
   public Results simpleSearch(String query, boolean student, int start)
   {
      Result[] results = null;
      int numTotalHits = 0;
      try
      {
         IndexSearcher searcher = (student)?studentSearcher:adminSearcher;

         Query luceneQuery = parser.parse(query);

         //long timeBefore = System.currentTimeMillis();
         
         TopDocs docResults = searcher.search(luceneQuery, 100);
         ScoreDoc[] hits = docResults.scoreDocs;
         
         //System.out.println("Search " + (System.currentTimeMillis() - timeBefore) + " " + System.currentTimeMillis());
         
         numTotalHits = docResults.totalHits;

         if ( (numTotalHits - start) >= 10)
            results = new Result[10];
         else if ( (numTotalHits - start) >= 1)
            results = new Result[numTotalHits - start];
         else
         {
        	 return new Results(null,query,0);
         }

         Highlighter highlighter =
            new Highlighter(
               // Custom Formatter. Default is placing bold tags around it.
               //It would be nice for the original text to be in bold.
               // But javafx doesn't support easy inline bolding.
               // Something to do later on.
               new Formatter()
               {
                  public String highlightTerm
                  (String originalText,TokenGroup tokenGroup)
                  {
                     return originalText;
                  }
               },
               new QueryScorer(luceneQuery));
         highlighter.setMaxDocCharsToAnalyze(514300 * 2);

         for (int i = start; i < hits.length && i < (start + 10); i++)
         {
            Document doc = searcher.doc(hits[i].doc);
            URL url  = makeURL(doc.get("path"),student);
            String text = doc.get("body");

            TokenStream tokenStream =
               TokenSources.getTokenStream(field, text, analyzer); 

            String dataSnippets = highlighter.getBestFragments(tokenStream, text, 2, "...");
            dataSnippets = dataSnippets.replaceAll("\\s+"," ").replace("... ","...").replaceFirst("^\\W+","");
            results[i - start] = new Result
               (url,dataSnippets,Long.parseLong( doc.get("date") ));
         }
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
      catch(ParseException pe)
      {
         pe.printStackTrace();
      }
      catch(InvalidTokenOffsetsException e)
      {
         e.printStackTrace();
      }
      Results resultObject = new Results(results,query,numTotalHits);
      return resultObject;
   }
   
   public void closeReaders()
   {
      try
      {
         studentReader.close();
         adminReader.close();
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
   }
}