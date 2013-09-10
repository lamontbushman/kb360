package kb360.desktop;

import kb360.service.*;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;

/* 
 * @author Lamont Bushman
 */
public class ResultsPane extends Tab
{
   public BorderPane mPane;
   private Results mResults;
   private Client mClient;
   static MainPanel mainPanel;
   private String mSearchTerm;
   private boolean mStudent;
   private VBox mCenter;
   private HBox mTopBox;
   private Hyperlink mPreviousLink;
   private Hyperlink mNextLink;
   
   private int mCurrentFirst;
   private int mCurrentLast;

   private boolean noResults;
   //private int mTotalResults;
   
   ResultsPane(Client client, String searchTerm, boolean student,MainPanel gui)
   {
      //Set class variables
      mClient = client;
      mSearchTerm = searchTerm;
      mStudent = student;
      mainPanel = gui;
      noResults = false;
      
      //The pane for this class
      mPane = new BorderPane();

      //The Results Bar
      mTopBox= new HBox();
      mTopBox.setSpacing(10);
      mTopBox.setAlignment(Pos.CENTER);

      //  Text of Results Bar
      Text totalText = new Text();
      HBox.setHgrow(totalText, Priority.ALWAYS);

      //  Previous Button
      mPreviousLink = new Hyperlink("Previous");
      mPreviousLink.setStyle("-fx-text-fill: blue");
      HBox.setHgrow(mPreviousLink, Priority.ALWAYS);
      mPreviousLink.setDisable(true);
      mPreviousLink.setOnAction(
         new EventHandler<ActionEvent>()
         {
            @Override
               public void handle(ActionEvent e)
            {
               previous();
            }
         });
            
      //  Next Button
      mNextLink = new Hyperlink("Next ");
      mNextLink.setStyle("-fx-text-fill: blue");
      HBox.setHgrow(mNextLink, Priority.ALWAYS);
      mNextLink.setOnAction(
         new EventHandler<ActionEvent>()
         {
            @Override
               public void handle(ActionEvent e)
            {
               next();
            }
         });

      mTopBox.getChildren().addAll(totalText,mPreviousLink,mNextLink);
      mPane.setTop(mTopBox);
      
      //The container for the results
      ScrollPane sp = new ScrollPane();
      mCenter = new VBox();
//      mCenter.setSpacing(20);
      sp.setContent(mCenter);
      mPane.setCenter(sp);

      search(1);
      
      setContent(mPane);
      setText(mResults.searchTerm());
   }

   public void search(int start)
   {
      mResults = mClient.search(mSearchTerm,mStudent,start - 1);
      if (mResults.totalResults() == 0)
      {
         noResults = true;
      }
      else
      {
         updateTopBox(start);
         fillCenter();
      }
   }

   public boolean noResults()
   {
      return noResults;
   }
   
   public void updateTopBox(int start)
   {
      mCurrentFirst = start;
      mCurrentLast = (mResults.totalResults() < (start + 9))?
         mResults.totalResults():(start + 9);

      if (start == 1)
         mPreviousLink.setDisable(true);

      if (mCurrentLast == mResults.totalResults())
          mNextLink.setDisable(true);

      ((Text)mTopBox.getChildren().get(0)).setText(
         mCurrentFirst + " - " + mCurrentLast + " of " +
         mResults.totalResults() + " Results");
   }
   
   public void fillCenter()
   {
      if(!mCenter.getChildren().isEmpty())
      {
         mCenter.getChildren().clear();
      }
      
      Result[] results = mResults.getResults();
      
      for (int i = 0; i < results.length; i++)
      {
         VBox resultPane = new ResultPane(results[i],i + mCurrentFirst);
         mCenter.getChildren().add(resultPane);
         VBox.setMargin(resultPane, new Insets(0,0,20,10));
      }  
   }

   private void previous()
   {
      search(mCurrentFirst - 10);
      mNextLink.setDisable(false);
   }

   private void next()
   {
      search(mCurrentLast + 1);
      mPreviousLink.setDisable(false);
   }
   
   public Pane getView()
   {
      return mPane;
   }
}

class ResultPane extends VBox
{
   private Hyperlink link;
   String snippet;
   String modified;
      
   ResultPane(Result result, int number)
   {
      try
      {
//         String googleDocs = "http://docs.google.com/viewer?url=";
         String googleDocs = "http://docs.google.com/viewer?embedded=true&url=";
         
         String fileUrl = result.getURL().toString();
         
         final String url = googleDocs +
            URLEncoder.encode(fileUrl,"UTF-8");
         final String title =
            fileUrl.substring(fileUrl.lastIndexOf('/') + 1,
               fileUrl.lastIndexOf('.'));

         Date date = new Date(result.getModified());
         DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
         modified = dateFormat.format(date);
         
         link = new Hyperlink();
         link.setText(number + ". " + title);
         link.setStyle("-fx-text-fill: blue;");//-fx-underline: true");
         link.setUnderline(true);
         link.setOnAction(
            new EventHandler<ActionEvent>()
            {
               @Override
                  public void handle(ActionEvent e)
               {
                  link.setStyle("-fx-text-fill: darkviolet");
                  Browser browser = new Browser(url,title);
                  ResultsPane.mainPanel.addTab(browser);
               }
            });
         VBox.setVgrow(link, Priority.ALWAYS);
         getChildren().add(link);

         Text dateText = new Text(modified);
//         dateText.setStyle("-fx-text-fill : orange");
         dateText.setFill(Color.GREEN);
         VBox.setVgrow(dateText, Priority.ALWAYS);
         getChildren().add(dateText);

         
         snippet = result.getDataSnippet();
         Text text = new Text(snippet);
         text.setWrappingWidth(500);
         VBox.setVgrow(text, Priority.ALWAYS);
         getChildren().add(text);
         

      }
      catch (UnsupportedEncodingException uee)
      {
         uee.printStackTrace();
      }
   }
}