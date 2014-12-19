package kb360.service;

// Java Libraries
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class FileUpload
{
   OutputStream stream;
   String mUploadDir;

   public FileUpload()
   {
      this("upload");
   }
   public FileUpload(String uploadDir)
   {
      mUploadDir = uploadDir + "/";

      File file = new File(mUploadDir);
      if (!file.exists())
      {
         file.mkdirs();
      }
   }
   
/**
   code from
   http://www.journaldev.com/855/how-to-set-file-permissions-in-java-easily-using-java-7-posixfilepermission
   creates the file if it doesn't exist, but doesn't overwrite the data just the
   permissions
*/
   public void setPermissions(File file)
   {
      try
      {
         //using PosixFilePermission to set file permissions
         Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
         //add owners permission
         perms.add(PosixFilePermission.OWNER_READ);
         perms.add(PosixFilePermission.OWNER_WRITE);
         perms.add(PosixFilePermission.OWNER_EXECUTE);
         //add group permissions
         perms.add(PosixFilePermission.GROUP_READ);
         perms.add(PosixFilePermission.GROUP_WRITE);
         perms.add(PosixFilePermission.GROUP_EXECUTE);
         //add others permissions  
         perms.add(PosixFilePermission.OTHERS_READ);
         //perms.add(PosixFilePermission.OTHERS_WRITE);
         //perms.add(PosixFilePermission.OTHERS_EXECUTE);
         
         Files.setPosixFilePermissions(Paths.get(file.toString()), perms);
      }
      catch(IOException ioe)
      {
         System.out.println(ioe.getMessage());
      }
   }

   private File makeUniqueFile(String name, int i)
   {
      //Create a file
      File file;
      if (i == 0)
         file = new File(mUploadDir + name);
      else
         file = new File(mUploadDir + i + name);

      //base case
      if (!file.exists())
      {
         return file;
      }

      //recursive call
      return makeUniqueFile(name, i + 1);
   }

   //helper function for recursive call
   private File makeUniqueFile(String name)
   {
      return makeUniqueFile(name,0);
   }

   public synchronized boolean upload(ByteArrayFile file)
   {
      byte[] bytes = file.getByteArray();

      try
      {              
         File newFile = makeUniqueFile(file.getName());
         OutputStream outStream = new FileOutputStream(newFile);
         outStream.write(bytes);
         outStream.flush();
         outStream.close();
         //setPermissions(newFile);
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
         return false;
      }
      return true;
   }
   
   /**
    * @param files The files to be saved to the system.
    * @return Whether the upload was successful.
    */
   public boolean upload(ByteArrayFile[] files)
   {
      boolean allUploaded = true;
      for (ByteArrayFile byteFile : files)
      {
         boolean uploaded = upload(byteFile);
         if (allUploaded)
         {
            allUploaded = uploaded;
         }
      }
      return allUploaded;
   }
}
