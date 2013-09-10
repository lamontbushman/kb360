package kb360.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * ServerObject is aninterface that defines the remote object that a client
 *  will be able to lookup to get other remote objects.
 */
public interface ServerRemotes extends Remote
{
   /** 
    * Returns the Upload remote object to the client.
    * @return The remote object
    * @exception RemoteException if the remote invocation fails .
    */
   public Upload getUploadRemote() throws RemoteException;

   /**
    * Returns the Search remote object to the client.
    * @return The remote object
    * @exception RemoteException if the remote invocation fails .
    */
   public Search getSearchRemote() throws RemoteException;
   
}
