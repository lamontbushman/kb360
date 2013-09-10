package kb360.desktop;

import kb360.service.*;

import java.io.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

/**
 * Handles non-graphical searching and uploading of documents to the KB360 server. 
 * 
 * @author Lamont Bushman
 */
public class Client
{
 /*
private ServerRemotes mRemotes;
 */
    /**
     * A {@link java.rmi.Remote} to the KB360 server to search files.
     */
	private Search mSearcher;

	/**
	 * A {@link java.rmi.Remote} to the KB360 server to upload files.
	 */
	private Upload mUploader;

	/**
	 * A list of files to upload.
	 */
	private List<File> mUploadFiles;
   
	/**
	 * Connects to the server with the given IP address.
	 * 
	 * @param serverIP the IP address to the KB360 server. If wanting to connect to localhost, "localhost" will due.
	 * @throws ServerException thrown when there is no searching connection to the server. Uploading isn't essential. 
	 */
	public Client(String serverIP) throws ServerException
	{
		ServerRemotes remotes;
		         
		try
		{
			String lookupName = "rmi://" + serverIP + ":1099/server";
			Registry registry = LocateRegistry.getRegistry(serverIP);

			remotes = (ServerRemotes) registry.lookup(lookupName);
			
			if (remotes == null)
			{
				throw new ServerException("Cannot search or upload. Remotes to the server are null.");
			}
			else
			{
				if ( (mSearcher = remotes.getSearchRemote()) == null)
				{
					throw new ServerException("Cannot search. The remote to search is null.");
				}
				if ( (mUploader = remotes.getUploadRemote()) == null)
				{
					System.err.println("The remote to upload is null");
				}
			}	
		}
		catch (NotBoundException nbe)
		{
			throw new ServerException(nbe);
		}
	    catch (RemoteException re)
	    {
	    	throw new ServerException(re);
	    }
		
        /*
         *  Now that the server is connected to the server ensure proxyHost is "localhost" so that
         *  the browser will not try to use the previous proxyHost to open a document.
         */
        System.setProperty("proxyHost","localhost");
	}
	
	/**
	 * Returns whether or not {@link #mUploader} is functioning to upload files to the server.
	 * @return whether or not the client can upload documents to the server.
	 */
	public boolean canUpload()
	{
		return mUploader != null;
	}
	   
	/*   ServerRemotes getRemotes()
	   {
	      return mRemotes;
	   }*/
	
	/**
	 * Setter for the files that will later be uploaded.
	 * Calling this function again, without uploading first, will reset the files.
	 * 
	 * @see #upload()
	 * @param files the list of files to upload
	 */
	public void setUploadFiles(List<File> files)
	{
		mUploadFiles = files;
	}	
	
	
	/**
	 * Uploads {@link #mUploadFiles} to the server.
	 * Also resets {@link #mUploadFiles}, whether they were uploaded or not.
	 * 
	 * @return the success/failure of uploading {@link #mUploadFiles}.
	 */
	public boolean upload()
	{
		/*
		 * Read file data in order to send to the KB360 server.
		 * For each file, get bytes and the file name and store it in a 
		 * kb360.service.ByteArrayFile.  
		 */
		int size = mUploadFiles.size();
		ByteArrayFile[] byteFiles = new ByteArrayFile[size];
		try
		{
			for (int i = 0; i < size; i++)
			{
				FileInputStream mStream = new FileInputStream(
						mUploadFiles.get(i));
	            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	            	
	            int nRead;
	            byte[] data = new byte[1024];
	            
	            while ((nRead = mStream.read(data, 0, data.length)) != -1)
	            {
	               buffer.write(data, 0, nRead);
	            }
	            byte[] byteArray = buffer.toByteArray(); 
	            byteFiles[i] = new ByteArrayFile(mUploadFiles.get(i),byteArray);
	         }
	    }
		catch (RemoteException re)
		{
			re.printStackTrace();
			return false;
	    }
		catch (FileNotFoundException fnfe)
		{
			fnfe.printStackTrace();
			return false;
		}
	    catch (IOException ioe)
	    {
	    	ioe.printStackTrace();
	    	return false;
	    }
		finally
		{
			//Reset files whether they were stored or not.
			mUploadFiles = null;
		}
	    
		//Upload and return success/failure.
		return upload(byteFiles);
	}	
	
	/**
	 * Helper method. Uploads the files read in by {@link #upload()}.
	 * 
	 * @param files the files that were read into serialized objects for sending to the server.
	 * @return the success/failure of uploading the given files.
	 */
	private boolean upload(ByteArrayFile[] files)
	{
		try
		{
			return mUploader.upload(files);
		}	
		catch (RemoteException re)
		{
			re.printStackTrace();
	    }
		return false;
	}
	
	/**
	 * Searches for documents matching the given query in the specified knowledge base.
	 * Returns at most the ten first results.
	 * The query can contain "NOTs" and "ANDs" (i.e. book AND president NOT Lincoln).
	 * 
	 * @param query the query to search the documents on the server
	 * @param student true - search the student index : false - search the admin index. 
	 * @return the results of the search. {@code Results} will never be null.
	 * @see #search(String,boolean)
	 * @see Result
	 * @see Results
	 */
	public Results search(String query,boolean student)
	{
		return search(query,student,0);
	}	
	  
	/**
	 * Searches for documents matching the given query in the specified knowledge base.
	 * Returns at most ten results starting at the specified nth zero-based result. 
	 * The query can contain "NOTs" and "ANDs" (i.e. book AND president NOT Lincoln).
	 * 
	 * @param query the query to search the documents on the server
	 * @param nthResult specifies the zero-based range of results to return [n, n + 10].
	 * @param student true - search the student index : false - search the admin index.
	 * @return the results of the search. {@code Results} will never be null.
	 *
	 * @see Result
	 * @see Results
	 */
	public Results search(String query,boolean student, int nthResult)
	{
		Results results = null;
		try
		{
			results = mSearcher.simpleSearch(query,student,nthResult);
		}
		catch (RemoteException re)
		{
			re.printStackTrace();
		}
		return results;
	}
}