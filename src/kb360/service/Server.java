package kb360.service;

import java.io.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

/**
 *
 */
public class Server
{
   ServerObjects mObjects;
   String lookupName;
   int mRegistryPort;
   int mObjectsPort;
   ServerRemotes mRemotes;
   Registry mRegistry;
   static Server mInstance;
   
   private static boolean running = false;

   /**
    *      
    */
   public Server(String pathToFileUpload,String pathToStudentFolder,
                 String pathToAdminFolder, String httpURL, int registryPort,
                 int objectsPort, int uploadPort, int searchPort)
   {
      mObjects = new ServerObjects(pathToFileUpload,pathToStudentFolder,
                                  pathToAdminFolder,httpURL,uploadPort,searchPort);
      String server = System.getProperty("java.rmi.server.hostname");
      if (server == null || server.equals(""))
         lookupName = "rmi://localhost:1099/server";
      else
         lookupName = "rmi://" + server + ":1099/server";
      System.out.println("lookup: " + lookupName);
      mRegistryPort = registryPort;
      mObjectsPort = objectsPort;
   }

   /**
    *      
    */
   public boolean start()
   {
      try
      {
         mRegistry = LocateRegistry.createRegistry(mRegistryPort); 
         
         mObjects.export();
         mRemotes =
            (ServerRemotes) UnicastRemoteObject.exportObject(mObjects, mObjectsPort);
         
         Registry registry = LocateRegistry.getRegistry(mRegistryPort);
         registry.rebind(lookupName, mRemotes);
      }
      catch (RemoteException re)
      {
         System.err.println("Cannot Create Registry:");
         re.printStackTrace();
         return false;
      }
      catch (Exception e)
      {
         System.err.println("ServerRemotes Exception:");
         e.printStackTrace();
         return false;
      }
      running = true;
      return true;
   }

   public static boolean stop()
   {
      if (!running)
         return true;
      
      boolean objectsExported, remotesExported, registryExported;
      objectsExported = remotesExported = registryExported = false;
      try
      {
         objectsExported = mInstance.mObjects.unexport();
         if (!objectsExported)
            System.out.println("Objects not exported");
         remotesExported = UnicastRemoteObject.unexportObject(mInstance.mObjects,true);
         registryExported = UnicastRemoteObject.unexportObject(mInstance.mRegistry,true);
      }
      catch (NoSuchObjectException nsoe)
      {
         System.out.println("remotes " + remotesExported + " registry " + registryExported );
         System.out.println("<Server> " + nsoe);
         return false;
      }
      running = false;
      return remotesExported && registryExported && objectsExported;
   }

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
      
      //Server server = null;
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

      mInstance = new Server(
         config.getProperty("upload"),
         config.getProperty("student"),
         config.getProperty("admin"),
         config.getProperty("httpURL"),
         Integer.parseInt(config.getProperty("registryPort")),
         Integer.parseInt(config.getProperty("objectsPort")),
         Integer.parseInt(config.getProperty("uploadPort")),
         Integer.parseInt(config.getProperty("searchPort"))
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
    *      
    */
   public static void main(String args[])
   {
      Server.run();
   }  
}