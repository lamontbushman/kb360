package kb360.service;

import java.io.Serializable;

/**
 * 
 */
public class Results implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Result[] mResultArray;
	private int mNumberOfResults;
	private String mSearchTerm;

   public Results(Result[] resultArray, String searchTerm,int numResults)
   {
      mResultArray = resultArray;
      mSearchTerm = searchTerm;
      mNumberOfResults = numResults;
   }

   public Result[] getResults()
   {
      return mResultArray;
   }

   public String searchTerm()
   {
      return mSearchTerm;
   }

   public int totalResults()
   {
      return mNumberOfResults;
   }

   public void setResults(Result[] resultArray)
   {
      mResultArray = resultArray;
   }

   public void setTotalResults(int numResults)
   {
      mNumberOfResults = numResults;
   }
}
