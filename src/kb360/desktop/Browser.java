package kb360.desktop;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.web.*;

/**
 * Simple web browser that can be placed in a {@link TabPane}.
 * 
 * @author Lamont Bushman
 * @author Adam Harper
 * @author Jordan Jensen
 */
public class Browser extends Tab
{
   /**
    * Creates a new {@code Browser} tab pointing to the specified URL and with the specified title.
    * 
    * @param url the URL to load
    * @param title the title of the tab
    */
   public Browser(String url, String title)
   { 
      WebView webView = new WebView();
      final WebEngine webEngine = webView.getEngine();
      webEngine.load(url);
    
      BorderPane mPane = new BorderPane();
      mPane.setCenter(webView);
      setContent(mPane);
      
      setText(title);
   }
}