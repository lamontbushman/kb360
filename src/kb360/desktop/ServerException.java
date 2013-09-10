package kb360.desktop;

/**
 * @author Lamont Bushman
 *
 *	ServerException
 */
class ServerException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * @param message
	 */
	ServerException(String message)
	{
		super(message);
	}
	
	/**
	 * @param cause
	 */
	ServerException(Throwable cause)
	{
		super(cause);
	}
}