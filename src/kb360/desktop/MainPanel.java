package kb360.desktop;

//import kb360.service.*;

import java.io.*;
import java.util.*;
import javafx.application.Application;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import netscape.javascript.*;

/**
 * @author Lamont Bushman
 * @author John Morgan 
 */
public class MainPanel extends Application
{
   private String defaultTitle = "Knowledge Base";    
   private Pane mSearchPane;
   private StackPane mAdminPane;
   private BorderPane root;
   private Stage mStage;
   private RadioButton rb1; 
   private RadioButton rb2; 
   private TabPane mTabPane;
   private HBox mControlBox;

   private Notification mNotification;
   
   private Client client;
   
   public void initialize()
   {
      findProperties();

      try
      {
    	  if (System.getProperty("server") != null)
    		  client = new Client(System.getProperty("server"));
    	  else
    		  client = new Client("localhost");
      }
      catch(ServerException se)
      {
    	  se.printStackTrace();
    	  System.exit(0);
          //JOptionPane Causes errors on Windows machines in the browser.
          //JOptionPane.showMessageDialog(null,"Cannot connect to the server");
      }
      mNotification = new Notification(mStage);
 

      mTabPane = new TabPane();
      mTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);//SELECTED_TAB);

      mSearchPane = new Pane();
      mAdminPane = new StackPane();
      
      setUpSearchPane();
      setUpAdminPane();
//      setUpTabPane();
       
      mSearchPane.setStyle("-fx-border-width: 5px,5px, 5px, 5px;-fx-border-color: transparent transparent blue transparent;");
      mSearchPane.setMinWidth(mSearchPane.getPrefWidth());
      HBox.setHgrow(mSearchPane, Priority.ALWAYS);
      
      mAdminPane.setStyle("-fx-border-color: transparent transparent blue transparent;-fx-border-width: 5px, 5px, 5px, 5px;");
      mAdminPane.setMinWidth(mAdminPane.getPrefWidth());
      HBox.setHgrow(mAdminPane, Priority.ALWAYS);
      mAdminPane.setAlignment(Pos.CENTER_RIGHT);
      

      mControlBox = new HBox();
      mControlBox.setMinWidth(1110);

      mControlBox.getChildren().addAll(mSearchPane, mAdminPane);

      root = new BorderPane();
   }
    
   @Override
   public void start(Stage primaryStage)
   {
      mStage = primaryStage;
      
      //set Stage boundaries to visible bounds of the main screen
      Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
      mStage.setX(primaryScreenBounds.getMinX());
      mStage.setY(primaryScreenBounds.getMinY());
      mStage.setWidth(primaryScreenBounds.getWidth());
      mStage.setHeight(primaryScreenBounds.getHeight());        

      
      initialize();

      root.setTop(mControlBox);
      
      root.setCenter(mTabPane);
      
      Scene scene = new Scene(root, 1200,800);
//      scene.getStylesheets().add("stylesheet.css");
      String title = "";
      if ((title = System.getProperty("title")) == null)
         title = defaultTitle;
     
      primaryStage.setTitle(title);
      primaryStage.setScene(scene);
      primaryStage.show();
   }

   /**
    * The main() method is ignored in correctly deployed JavaFX application.
    * main() serves only as fallback in case the application can not be
    * launched through deployment artifacts, e.g., in IDEs with limited FX
    * support. NetBeans ignores main().
    *
    * @param args the command line arguments
    */
   public static void main(String[] args) {
      launch(args);
   }

   private void findProperties()
   {
      if (System.getProperty("server","null") != "null")
         return;
      
      JSObject jsWin = getHostServices().getWebContext();
      if (jsWin != null)
      {
         String server = (String) jsWin.eval("getServer()");
         if (server != null && !server.equals(""))
         {
            System.setProperty("server",server);
         }

         String proxyHost = (String) jsWin.eval("getProxyHost()");
         if (proxyHost != null && !proxyHost.equals(""))
         {
            System.setProperty("proxyHost",proxyHost);
         }
      }
   }

   private void setUpAdminPane() 
   {   
      Label missingLabel = new Label("Is there missing information?");
      missingLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 13));

      final TextField fileTextField = new TextField();
      fileTextField.setPrefColumnCount(12);
      fileTextField.setEditable(false);
      final String textFieldText = "pdf, doc, docx, txt, html"; 
      fileTextField.setText(textFieldText);
      
      Button browseButton = new Button("Browse");
      final Button uploadButton = new Button("Upload");
      uploadButton.setDisable(true);
        
      browseButton.setOnAction(
         new EventHandler<ActionEvent>()
         {
            @Override
               public void handle(ActionEvent Event)
            {
               FileChooserBuilder fcb;
               List<File> selectedFiles;

               String currentDir =
                  System.getProperty("user.home") + File.separator;
               StringBuilder sb = new StringBuilder();
               fcb = FileChooserBuilder.create();
               FileChooser fc =
                  fcb.title("Open Dialog").initialDirectory(new File(currentDir))
                  .build();

               fc.getExtensionFilters().addAll(
                  new FileChooser.ExtensionFilter(".pdf, .docx, .doc, .txt, .html",
                                                  "*.pdf", "*.docx", "*.doc", "*.txt", "*.html")
                                                        );
               
               selectedFiles = fc.showOpenMultipleDialog(mStage);
			
               for (File file : selectedFiles)
               {
                  sb.append(file.getName() + " ");
               }
               fileTextField.setText(sb.toString());
               client.setUploadFiles(selectedFiles);
               uploadButton.setDisable(false);
            }
         });
        
      uploadButton.setOnAction(
         new EventHandler<ActionEvent>()
         {
            @Override
               public void handle(ActionEvent Event)
            {
               if (client.upload())
               {
                  mNotification.notify("File(s) Uploaded");
               }
               else
               {
                  mNotification.notify("File(s) were not upload");
               }
               uploadButton.setDisable(true);
               fileTextField.setText(textFieldText);
            }
         });

      HBox hBox = new HBox();
      hBox.setAlignment(Pos.CENTER_RIGHT);
      hBox.getChildren().addAll(missingLabel,fileTextField,browseButton,uploadButton);
      hBox.setSpacing(10);
      hBox.relocate(10,10);
      
      mAdminPane.getChildren().add(hBox);
   }

   public void addTab(Tab tab)
   {
      tab.setClosable(true);

      /*
        The following sets the length of the tab's text.
        mTabPane.setTabMaxWidth(maxWidth) could do this.
        However, tabs that were smaller weren't displaying the close operation.
        mTabPane.setTabMinWidth(minWidth) fails fixing this problem.
      */
      String title = tab.getText();
      if (title.length() > 15)
         tab.setText(title.substring(0,15) + "...");

      mTabPane.getTabs().add(tab);
      mTabPane.getSelectionModel().select(tab);
     
      root.setCenter(mTabPane);
   }

   private void setUpSearchPane() 
   {
      final TextField searchTextField = new TextField();      
      searchTextField.setPromptText("search knowledge base");
      searchTextField.setPrefColumnCount(12);

      rb1 = new RadioButton("Student");
      rb2 = new RadioButton("Employee");
      rb1.setSelected(true);
        
      final ToggleGroup toggleGroup = new ToggleGroup();
      rb1.setToggleGroup(toggleGroup);
      rb2.setToggleGroup(toggleGroup);
      
      searchTextField.setOnAction(
         new EventHandler<ActionEvent>()
         {
            @Override
               public void handle(ActionEvent event)
            {
               search(searchTextField.getText(),toggleGroup.getSelectedToggle() == rb1);
               searchTextField.setText("");
            }
         });

      Button searchButton = new Button("Search");
      searchButton.setOnAction(
         new EventHandler<ActionEvent>()
         {
            @Override
               public void handle(ActionEvent event)
            {
               search(searchTextField.getText(),toggleGroup.getSelectedToggle() == rb1);
               searchTextField.setText("");
            }
         });

      HBox hSearchBox = new HBox();
      
      hSearchBox.setAlignment(Pos.CENTER);
      hSearchBox.getChildren().addAll(searchTextField,rb1,rb2,searchButton);
      hSearchBox.setSpacing(10);
      hSearchBox.relocate(10,10);
      mSearchPane.getChildren().add(hSearchBox);
   }

   private void search(String search,boolean searchStudent)
   {
      if(search != null && !search.equals(""))
      {
         ResultsPane results = new ResultsPane(
            client,search,searchStudent,MainPanel.this);
         if (results.noResults())
            mNotification.notify("No results found");
         else
            addTab(results);
      }
   }

   /*private void setUpTabPane()
   {
//      Tab tab= new Tab();
//      mTabPane.getTabs().add(tab);
/*       
      vsep = new Separator();
      vsep.setOrientation(Orientation.VERTICAL);
       
      HBox tabHBox = new HBox();
       
      tabHBox.getChildren().addAll(vsep);
      tabHBox.setMargin(vsep,new Insets(10,10,10,10));
   }*/
}
