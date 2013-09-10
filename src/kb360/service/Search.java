package kb360.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 */
public interface Search extends Remote
{
    /**
     * Does a simple search. If there are multiple words, it treats the expression
     *  as if there were AND's between each word.
     * Results will contain a Restult[] array of at most the first ten results.
     * As well Results will tell you how many total hits there were.
     * @param query The query to be searched.
     * @param student Whether the student or the admin folder should be searched.
     * @return The searched results.
     * @exception RemoteException if the remote invocation fails .
     */
   public Results simpleSearch(String query, boolean student)
   throws RemoteException;

   /**
     * Same as the above simpleSearch(String, boolean), except you can specify
     *  what range of ten results you want returned.
     * (i.e) If start is 10, the results will return results 10 - 19
     * @param query The query to be searched.
     * @param student Whether the student or the admin folder should be searched.
     * @param start The beginning of the range. It is zero based.
     * @return The searched results.
     * @exception RemoteException if the remote invocation fails .
     */
   public Results simpleSearch(String query, boolean student, int start)
   throws RemoteException;
}
