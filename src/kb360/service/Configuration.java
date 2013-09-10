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

   private HashMap<String,String> propertiesMap;

   private ArrayList<KeyTypePrompt> mKeysTypesAndPrompts;
   
   public Configuration()
   {
      configSet = true;
      properties = new Properties();
      configFile = "kb360.config";

      propertiesMap = new HashMap<String,String>();

      mKeysTypesAndPrompts = new ArrayList<KeyTypePrompt>();

      String[] keys = {"upload","student","admin","httpURL",
                       "registryPort","objectsPort","uploadPort","searchPort"};
      String[] types = {"file","file","file","url","port","port","port","port"};
      String[] prompts = {"Upload Folder","Student Folder","Admin Folder","URL path",
               "Registry Port","Objects Port","Upload Port","Search Port"};
      addKeysTypesAndPrompts(keys,types,prompts);

      try
      {
         properties.load( new FileInputStream(configFile));

         for (KeyTypePrompt keyType : mKeysTypesAndPrompts)
         {
            propertiesMap.put(keyType.getKey(),
                              properties.getProperty(keyType.getKey()));
         }
      }
      catch (IOException ioe)
      {
//         System.out.println("Configuration file was not found");
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
      return propertiesMap.get(key);
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
                        propertiesMap.put(key.getKey(),folder.getPath());
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
               propertiesMap.put(key.getKey(),value);
            }
            else
            {
               reprompt = true;
            }
            break;
         case "port":
            value = JOptionPane.showInputDialog(
               null,key.getPrompt(),"Configuration",JOptionPane.INFORMATION_MESSAGE);
            if (value != null && value != "")
            {
               int port = 0;
               try
               {
                  port = Integer.parseInt(value);
               }
               catch(NumberFormatException nfe)
               {
                  JOptionPane.showMessageDialog(null,value + " is not a number","Port",JOptionPane.ERROR_MESSAGE);
                  prompt(key);                  
               }
               propertiesMap.put(key.getKey(),port + "");
            }
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
          "\nConfigure your knowledge base.\n" +
          "\nUpload folder:  Where will you have your upload folder? It can be anywhere you want.\n" +
          "http accessible URL: The URL to where your student and admin folders will exist on the web.\n" +
          "Student folder: Where is the student folder on your computer?\n" +
          "Admin folder: Where is the admin folder on your computer?\n" +
          "Ports: The ports you enter have to be open. They can be the same port.\n" +
          "          However, unique ports are preferred for speed purposes.\n" +
          "Registry Port: For the initial connection to the server RMI registry. Port 1099 is usually the default.\n" +
          "Objects Port: For the initial access to the Upload and Search remotes.\n" +
          "Upload Port: For constant access to the Upload remote.\n" +
          "Search Port: For constant access to the Search remote.\n" +
          
          "\nExample:\n" +
          "URL: \"http://kb360.com/kb360\"\n" +
          "Upload: \"C://desktop/upload\"\n" +
          "Student: \"kb360/student\"\n" +
          "Admin: \"kb360/admin\"\n",
          "Configuration", JOptionPane.INFORMATION_MESSAGE);

      for (KeyTypePrompt keyTypePrompt : mKeysTypesAndPrompts)
      {
         System.out.println(keyTypePrompt);
         prompt(keyTypePrompt);
      }
      return true;
   }

   public boolean initialize()
   {
      prompt();

      for (KeyTypePrompt keyTypePrompt : mKeysTypesAndPrompts)
      {
         String key = keyTypePrompt.getKey();
         properties.setProperty(key,propertiesMap.get(key));
      }
       
      String comments =
         "Do not delete, unless you want to change the paths to your files";
      try
      {
         properties.store(new FileOutputStream(configFile),comments);
      }
      catch(IOException ioe)
      {
         configSet = false;
      }
      
      configSet = true;

      return configSet;
   }
   
   public static void main(String args[])
   {
      Configuration config = new Configuration();
      if (!config.isSet())
      {
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
      return mKey +  " " + " " + mType + " " + mPrompt;
   }
}