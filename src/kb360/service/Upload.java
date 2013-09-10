package kb360.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 */
public interface Upload extends Remote
{
    /**
     * @param files The files to be saved to the system.
     * @return Whether the upload was successful.
     * @exception RemoteException if the remote invocation fails.
     */
    boolean upload(ByteArrayFile[] files) throws RemoteException;

       /**
     * @param file The file to be saved to the system.
     * @return Whether the upload was successful.
     * @exception RemoteException if the remote invocation fails.
     */
    boolean upload(ByteArrayFile file) throws RemoteException;
}
