
package kb360.desktop;

import javafx.geometry.*;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import javafx.scene.text.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.application.Platform;

public class Notification
{
   private Label mLabel;
   private Pane mPane;
   private Popup mPopup;
   private Stage mStage;
   private HBox mHBox;
   
   Notification(final Stage stage)
   {
      mStage = stage;
      
      mLabel = new Label();
      mLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
      mLabel.relocate(0,0);

      mHBox = new HBox();
      mHBox.setAlignment(Pos.CENTER);
      mHBox.getChildren().add(mLabel);
      HBox.setMargin(mLabel, new Insets(20,20,20,20));
      
      mPane = new Pane();
      mPane.setStyle("-fx-background-color: lightblue;-fx-border-radius: 20;-fx-border-width: 3;-fx-background-radius: 22;-fx-border-color: blue");
      mPane.getChildren().add(mHBox);

      mPopup = new Popup();
      mPopup.getContent().addAll(mPane);
   }
   
   public void notify(String text)
   {
      mLabel.setText(text);
       
      mPopup.show(mStage,
                  (mStage.getWidth() / 2) + mStage.getX() - ((text.length() * 11.5) / 2),
                  (mStage.getHeight() / 2) + mStage.getY());
      new Thread()
      {
         @Override
         public void run()
         {
            try
            {
               Thread.sleep(1500);
               Platform.runLater(
                  new Runnable()
                  {
                     @Override
                     public void run()
                     {
                        mPopup.hide();
                     }
                  });
            }
            catch (InterruptedException ie)
            {
               Platform.runLater(
                  new Runnable()
                  {
                     @Override
                     public void run()
                     {
                        mPopup.hide();
                     }
                  });
            }
         }
      }.start();
   }
}