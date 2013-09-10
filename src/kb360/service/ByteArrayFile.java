package kb360.service;

import java.io.*;

/**
 * 
 */
public class ByteArrayFile implements Serializable
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte[] mByteArray;
	private String mFileName;
   
   public ByteArrayFile(File file, byte[] byteArray)
   {
	   	mByteArray = byteArray;
	   	mFileName = file.getName();
   }
   
   public byte[] getByteArray()
   {
      return mByteArray;
   }
   
   public String getName()
   {
      return mFileName;
   }
   
   public void setByteArray(byte[] byteArray)
   {
      mByteArray = byteArray;
   }
    
   public void setName(String fn)
   {
      mFileName = fn;
   }
}
