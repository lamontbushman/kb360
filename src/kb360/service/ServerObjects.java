package kb360.service;

import java.rmi.*;
import java.rmi.server.*;

public class ServerObjects implements ServerRemotes
{
   FileUpload uploader;
   SearchIndex searcher;
   Upload uploadRemote;
   Search searchRemote;
   int mUploadPort;
   int mSearchPort;

   /**
    * 
    */
   public ServerObjects(String pathToFileUpload, String pathToStudentFolder,
                        String pathToAdminFolder, String httpURL, int uploadPort,
                        int searchPort)
   {
      uploader = new FileUpload(pathToFileUpload);
      searcher = new SearchIndex(pathToStudentFolder,pathToAdminFolder,httpURL);
      searcher.openReaders();
      mUploadPort = uploadPort;
      mSearchPort = searchPort;
   }
  
   public void export() throws RemoteException
   {
      uploadRemote = (Upload) UnicastRemoteObject.exportObject(uploader, mUploadPort);
      searchRemote = (Search) UnicastRemoteObject.exportObject(searcher, mSearchPort);
   }

   public boolean unexport()// throws NoSuchObjectException
   {
      boolean uploadExported, searchExported;
      uploadExported = searchExported = false;
      try
      {
      uploadExported = UnicastRemoteObject.unexportObject(uploader,true);
      searchExported = UnicastRemoteObject.unexportObject(searcher,true);
      }
      catch(NoSuchObjectException nsoe)
      {
         System.out.println("<ServerObjects> upload " + uploadExported + " search " + searchExported );
         System.out.println("<ServerObjects> " + nsoe);
      }
      return uploadExported && searchExported;
   }
   
   /**
    * Returns the Upload remote object to the client.
    * @return The remote object
    * @exception RemoteException if the remote invocation fails .
    */
   @Override
   public Upload getUploadRemote()
   {
      return uploadRemote;
   }

   /**
    * Returns the Search remote object to the client.
    * @return The remote object
    * @exception RemoteException if the remote invocation fails .
    */
   @Override
   public Search getSearchRemote()
   {
      return searchRemote;
   }
}