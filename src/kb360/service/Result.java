package kb360.service;


import java.io.Serializable;
import java.net.URL;

/**
 * 
 */
public class Result implements Serializable
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private URL mUrl;
   private String mDataSnippet;
   private long mLastModified;

   public Result(URL url, String dataSnippet, long modified)
   {
      mUrl = url;
      mDataSnippet = dataSnippet;
      mLastModified = modified;
   }

   public void setURL(URL url)
   {
      mUrl = url;
   }

   public void setDataSnippet(String dataSnippet)
   {
      mDataSnippet = dataSnippet;
   }

   public void setModified(long modified)
   {
      mLastModified = modified;
   }
   
   public URL getURL()
   {
      return mUrl;
   }

   public String getDataSnippet()
   {
      return mDataSnippet;
   }

   public long getModified()
   {
      return mLastModified;
   }
}
