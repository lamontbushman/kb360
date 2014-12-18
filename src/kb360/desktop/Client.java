package kb360.desktop;

import kb360.service.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Handles non-graphical searching and uploading of documents to the KB360 server. 
 * 
 * @author Lamont Bushman
 */
public class Client
{
	/**
	 * A list of files to upload.
	 */
	private List<File> mUploadFiles;
	
	private ObjectInputStream mInputStream;
	private ObjectOutputStream mOutputStream;
	private Socket socket;
	private boolean connected;
	
	/**
	 * Connects to the server with the given IP address.
	 * 
	 * @param serverIP the IP address to the KB360 server.
	 * @throws ServerException thrown when there is no connection to the server. 
	 */
	public Client(String serverIP) throws ServerException
	{	         
		connected = false;
		try
		{
			socket = new Socket(serverIP,6000);
			mOutputStream = new ObjectOutputStream( new
				BufferedOutputStream(socket.getOutputStream()));
			mOutputStream.flush();
			mInputStream = new ObjectInputStream( new 
				BufferedInputStream(socket.getInputStream()));
			connected = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns whether or not {@link #mUploader} is functioning to upload files to the server.
	 * @return whether or not the client can upload documents to the server.
	 */
	public boolean canUpload()
	{
		return connected;
	}
	
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
	            mStream.close();
	        }
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
			mOutputStream.writeObject(files);
			mOutputStream.flush();
			return (boolean) mInputStream.readObject();
		}	
		catch (Exception e)
		{
			e.printStackTrace();
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
			mOutputStream.writeObject(query);
			mOutputStream.writeObject(student);
			mOutputStream.writeObject(nthResult);
			mOutputStream.flush();
			results = (Results) mInputStream.readObject();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		catch(ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
		}

		return results;
	}
	

}
