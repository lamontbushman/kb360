package kb360.service;

import java.io.*;
import java.net.*;
import java.util.*;

/**
* A thread for handling a new client.
*/
class HandleClient extends Thread
{
	Socket mSocket;
	ObjectInputStream mInputStream;
	ObjectOutputStream mOutputStream;
	FileUpload mUploader;
	SearchIndex mSearcher;
	
	HandleClient(Socket socket, FileUpload uploader, SearchIndex searcher)
	{
		mSocket = socket;
		mUploader = uploader;
		mSearcher = searcher;
		
		try
		{
			mInputStream = new ObjectInputStream( new
				BufferedInputStream(socket.getInputStream()));
			mOutputStream = new ObjectOutputStream( new
				BufferedOutputStream(socket.getOutputStream()));
			//Flushing required so that the client can initiate
			// its corresponding ObjectInputStream
			mOutputStream.flush();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

	}
	
	@Override
	public void run()
	{
		Object o;
		String query = null;
		Boolean student = null;
		int nthResult;
		
		try 
		{
			for(;;)
			{
				o = mInputStream.readObject();
			
				if (o instanceof String)
				{
					query = (String) o;

				}	
				else if (o instanceof Boolean)
				{
					student = (boolean) o;
				}
				else if (o instanceof Integer)
				{
					nthResult = (Integer) o;
					//Shouldn't ever be null with proper execution by the
					// client. Should receive in order query, student, and then nthResult.
					if (query == null || student == null)
					{
						mOutputStream.writeObject(null);
					}
					else
					{
						Results results = mSearcher.simpleSearch(query,student,nthResult);
						mOutputStream.writeObject(results);
					}
					mOutputStream.flush();
				}
				else if (o instanceof ByteArrayFile[])
				{
					boolean uploaded = mUploader.upload((ByteArrayFile[]) o);
					mOutputStream.writeObject(uploaded);
					mOutputStream.flush();
				}
				else
				{
					System.out.println("Incorrect Query:" + o);
				}
			}	
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		catch(ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
		}
	}
	
	public void close()
	{
		try
		{
			mInputStream.close();
			mOutputStream.close();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	
}

/**
 * TCPServer listens for connections and then passes socket off to
 *  HandleClient.
 */
public class TCPServer
{
   static TCPServer mInstance;
   
   private static boolean running = false;
   
   FileUpload uploader;
   SearchIndex searcher;
   ServerSocket serverSocket;
   Vector<HandleClient> clients;
   
   /**
    *  Constructor. Initializes objects to upload and search the index.
	*  The references to these objects are then passed to each HandleClient thread.
    */
   private TCPServer(String pathToFileUpload,String pathToStudentFolder,
                 String pathToAdminFolder, String httpURL)
   {
		uploader = new FileUpload(pathToFileUpload);
		searcher = new SearchIndex(pathToStudentFolder,pathToAdminFolder,httpURL);
		searcher.openReaders();
		clients = new Vector<HandleClient>();
   }

   /**
    *	Starts the server.
    */
   public boolean start()
   {
		try
		{
			serverSocket = new ServerSocket(6000);
			
			Socket socket;
			
			while(true)
			{
				socket = serverSocket.accept();
				HandleClient client = new HandleClient(socket,uploader,searcher);
				clients.add(client);
				client.start();
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		running = true;
		return true;
   }

   /**
   * Sets up and runs the singleton TCPServer.
   */
   public static boolean run()
   {
		if (running)
			return true;
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
			System.out.println(
				"Update Indexes First\n" + 
				"Configuration is not set up\n" +
				"Click UpdateIndexes to start creating the indexes.\n" +
				"Also, you will need to put at least one document in your admin and student folders"
                            ); 
			System.exit(0);
		}

		mInstance = new TCPServer(
			config.getProperty("upload"),
			config.getProperty("student"),
			config.getProperty("admin"),
			config.getProperty("httpURL")
							);

		if (mInstance.start())
		{
			System.out.println("Server up and running");
			return true;
		}
		else
			return false;
   }

   /**
    * Invokes the singleton TCPServer's run method.
    */
	public static void main(String args[])
	{
		TCPServer.run();
	}  
}