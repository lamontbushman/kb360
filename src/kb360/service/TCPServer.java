package kb360.service;

import java.io.*;
import java.net.*;
import java.util.*;

/**
* A thread for handling a new client.
*/
class HandleClient extends Thread
{
	private static boolean tcpServerRunning = true;
	private ObjectInputStream mInputStream;
	private ObjectOutputStream mOutputStream;
	private Socket mSocket;
	private FileUpload mUploader;
	private SearchIndex mSearcher;
	
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
			while(tcpServerRunning)
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
		
	public static void setServerStopped() {
		tcpServerRunning = false;
	}
	
	public void finishProcces() {
		try {
			mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

/**
 * TCPServer listens for connections and then passes socket off to
 *  HandleClient.
 */
public class TCPServer extends Thread {  
   private Boolean running;
   private FileUpload uploader;
   private SearchIndex searcher;
   private ServerSocket serverSocket;
   private Vector<HandleClient> clients;
   
   /**
    *  Constructor. Initializes objects to upload and search the index.
	*  The references to these objects are then passed to each HandleClient thread.
    */
   public TCPServer(String pathToFileUpload,String pathToStudentFolder,
                 String pathToAdminFolder, String httpURL) {
	   running = null;
	   uploader = new FileUpload(pathToFileUpload);
	   searcher = new SearchIndex(pathToStudentFolder,pathToAdminFolder,httpURL);
	   searcher.openReaders();
	   clients = new Vector<HandleClient>();
   }

   public void finishProccess() {
	   running = false;
	   try {
		serverSocket.close();
	   } catch (IOException e) {
		   e.printStackTrace();
	   }
	   HandleClient.setServerStopped();
	   for(HandleClient client : clients) {   
		   client.finishProcces();
	   }
   }   
  
   public Boolean isRunning() {
	   return running;
   }

   public void run() {
	   try {
		   serverSocket = new ServerSocket(6000);			
		   Socket socket;
		   running = true;
		   System.out.println("Server is now running");
		   while(running) {
			   try {
				   socket = serverSocket.accept();
				   HandleClient client = new HandleClient(socket,uploader,searcher);
				   clients.add(client);
				   client.start();
			   } catch (SocketException se) {
					 //Server socket intentionally closed.
			   }
		   }

		   for (HandleClient client : clients) {
			   try {
				   client.join();
			   } catch (InterruptedException e) {
				   System.out.println("Interrupted while joining with a HandleClient thread.");
				   e.printStackTrace();
			   }
		   }
	   } catch (IOException ioe) {
		   ioe.printStackTrace();
		   running = false;
	   }
   }

   /**
    * Invokes the singleton TCPServer's run method.
    */
	public static void main(String args[]) {
		if ("true".equals(System.getProperty("desktop","false"))) {
			OptionPanePrintStream stream = new OptionPanePrintStream(
				new ByteArrayOutputStream());
			System.setOut(stream);
			System.setErr(stream);
		}
		
		Configuration config = new Configuration();
		if (!config.isSet()) {
			System.out.println("Configuration is not set up."); 
			System.exit(0);
		}
		
		TCPServer server = new TCPServer(
			config.getProperty("upload"),
			config.getProperty("student"),
			config.getProperty("admin"),
			config.getProperty("httpURL")
							);

		server.start();
	}  
}