package kb360.service;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

public class Configuration
{
   private boolean configSet;
   private Properties properties;
   private String configFile;
   private ArrayList<KeyTypePrompt> mKeysTypesAndPrompts;
   
   public Configuration()
   {
      configSet = true;
      properties = new Properties();
      configFile = "kb360.config";

      mKeysTypesAndPrompts = new ArrayList<KeyTypePrompt>();

      //TODO move this out to an Enum or a file.
      String[] keys = {"server", "httpURL", "upload"};
      String[] types = {"file","url","file"};
      String[] prompts = {"Server Folder", "File Server URL", "Upload Folder"};
      addKeysTypesAndPrompts(keys,types,prompts);

      try {
         properties.load( new FileInputStream(configFile));
      }
      catch (IOException ioe) {
         configSet = false;
      }
   }

   public void addKeysTypesAndPrompts(String[] keys, String[] types, String[] prompts)
   {
      for (int i = 0; i < keys.length; i++)
      {
         mKeysTypesAndPrompts.add(new KeyTypePrompt(keys[i],types[i],prompts[i]));
      }
   }

   public boolean isSet()
   {
      return configSet;
   }

   public String getProperty(String key)
   {
      return properties.getProperty(key);
   }

   private boolean prompt(KeyTypePrompt key)
   {
      boolean reprompt = false;
      switch(key.getType())
      {
         case "file":
        	File folder;
      
			JFileChooser chooser = new JFileChooser(new File("."));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setApproveButtonToolTipText("Choose the " + key.getPrompt());
			int returnVal = chooser.showDialog(null,key.getPrompt());
			  
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
			   folder = chooser.getSelectedFile();
			   if (!folder.exists() && folder.isDirectory())
			   {
			      JOptionPane.showMessageDialog
			         (null,folder.getName() + " is not an existing folder","Error",
			          JOptionPane.ERROR_MESSAGE);
			      prompt(key);
			   }
			   else
			   {
			      properties.put(key.getKey(),folder.getPath());
			   }
			}
			else
			{
			   reprompt = true;
			}
			  
			break;
         case "url":
            String value = JOptionPane.showInputDialog(
               null,key.getPrompt(),"Configuration",JOptionPane.INFORMATION_MESSAGE);

            if (value != null && value != "")
            {
               try
               {
                  new URL(value);
               }
               catch(MalformedURLException mfue)
               {
                  JOptionPane.showMessageDialog(null,value + " is not a valid url","URL",JOptionPane.ERROR_MESSAGE);
                  prompt(key);
               }
               properties.put(key.getKey(),value);
            }
            else
            {
               reprompt = true;
            }
            break;
      }
      
      if (reprompt)
      {
         reprompt = false;
         if (JOptionPane.YES_OPTION ==
             JOptionPane.showConfirmDialog(null,"Do you want to continue?",
                                           "Stop Configuration",JOptionPane.YES_NO_OPTION))
         {
            prompt(key);            
         }
         else
         {
            System.exit(0);
         }
      }
      return true;
   }

   private boolean prompt()
   {
      JOptionPane.showMessageDialog
         (null,
          "Thanks for using Knowledge Base 360(KB360)!\n" +
          "KB360 allows you to search and view documents* within an archive.\n" +
          
          "\nKB360 will start a server to search the documents.\n" +
          "In order to search the documents off the local network you'll need a public facing IP address.\n" +
          
          "\nYour KB360 documents also need to be able to be accesssed via the internet so they can be viewed in Google's Document Viewer.\n" +
          "This requires your own HTTP file server for the documents. I have used Dropbox as an alternative**.\n" +
        
          "\nTo configure KB360 the following screens will prompt for:\n" +
          "  Server Folder:\n" +
          "    The local folder where the KB360 folders will be placed. This will be the root of your HTTP file server.\n" +
          "    Two folders will be added to the Server Folder.\n" +
          "       1. Admin - Folder where admin type files will be placed.\n" +
          "       2. Student - Folder where student type files will be placed.\n" +
          "       However, both types of files are designed to be accessible to all types of users.\n" +
          "       Currently everytime new files are added to these folders, UpdateIndexes need to be rerun and the server restarted.\n" +
          "  URL:\n" +
          "     The URL to the Server Folder.\n" +
          "     i.e. https://kb360.com/Server/\n" +
          "  Upload Folder:\n" +
          "     A folder where users will be able to upload recommended files for the database.\n" +
          "     This folder is not required to be on the HTTP file server.\n" +
        
          "\n * Currently supports PDF, TXT, and HTML files. Microsoft type file capabilities have been removed due to Google's Terms of Use policies\n" +
          "** I have used Dropbox's Public folder as an HTTP file server.\n" +
          "    (Creating a Public folder - https://www.dropbox.com/en/help/16)\n" +
          "    However, to have a Public folder it is now required to have a Pro or Business account.\n" +
          "    After enabling your Public folder you can get a Public link to a folder within the Public folder.\n" +
          "    i.e. https://dl.dropboxusercontent.com/u/123456789/Server/\n",
          "Configuration", JOptionPane.INFORMATION_MESSAGE);

      for (KeyTypePrompt keyTypePrompt : mKeysTypesAndPrompts)
      {
         prompt(keyTypePrompt);
      }
      return true;
   }

   public void initialize()
   {
      prompt();

      String serverFolder = properties.getProperty("server");
	  File admin = new File(serverFolder + File.separator + "admin");
	  admin.mkdir();
	  File student = new File(serverFolder + File.separator + "student");
	  student.mkdir();
	  properties.setProperty("admin", admin.getPath());
	  properties.setProperty("student", student.getPath());
      
      String comments =
         "Do not delete, unless you want to change the paths to your files";
      try {
         properties.store(new FileOutputStream(configFile),comments);
         System.out.println("KB360 is now configured");
      }
      catch(IOException ioe) {
    	  System.out.println("KB360 was not able to be configured");
    	  System.exit(0);
      }
      
      //configSet = true;
   }
   
   public static void main(String args[]) {
      Configuration config = new Configuration();
      if (!config.isSet()) {
         config.initialize();
      }
   }
}

class KeyTypePrompt
{
   private String mKey;
   private String mType;
   private String mPrompt;
   
   public KeyTypePrompt(String key, String type, String prompt)
   {
      mKey = key;
      mType = type;
      mPrompt = prompt;
   }

   public String getKey()
   {
      return mKey;
   }

   public String getType()
   {
      return mType;
   }

   public String getPrompt()
   {
      return mPrompt;
   }

   public String toString()
   {
      return "KeyTypePrompt: " + mKey +  ":" + mType + ":" + mPrompt;
   }
}