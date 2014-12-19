package kb360.service;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.swing.JOptionPane;

public class KB360 {
	private Configuration mConfig;
	private TCPServer mServer;
	private MenuItem mStartItem;
	private MenuItem mStopItem;
	private MenuItem mUpdateItem;
	
	KB360() {
		//Change System.out to a JOptionPane. I used System.out originally for headless servers that I was working with.
		System.setProperty("desktop","true");
		OptionPanePrintStream stream = new OptionPanePrintStream(
			new ByteArrayOutputStream());
		System.setOut(stream);
		
		mConfig = new Configuration();
		if (!mConfig.isSet()) {
			mConfig.initialize();
		}
				
		TrayIcon trayIcon = null;
		if (SystemTray.isSupported()) {
		    Image image = Toolkit.getDefaultToolkit().getImage(
		    		getClass().getResource("/resources/tray-icon.png"));
		    
		    ActionListener listener = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	switch(e.getActionCommand()) {
		        		case "start":
		        			start();
		        			break;
		        		case "stop":
		        			stop();
		        			break;
		        		case "quit":
		        			quit();
		        			break;
		        		case "update":
		        			updateIndexes();
		        			break;
		        	}
		        }
		    };
		    
		    PopupMenu popup = new PopupMenu();
		    
		    mStartItem = new MenuItem("Start Server");
		    mStartItem.setActionCommand("start");
		    mStartItem.addActionListener(listener);
		    popup.add(mStartItem);
		    
		    mStopItem = new MenuItem("Stop Server");
		    mStopItem.setActionCommand("stop");
		    mStopItem.setEnabled(false);
		    mStopItem.addActionListener(listener);
		    popup.add(mStopItem);
		    
		    MenuItem quitItem = new MenuItem("Quit");
		    quitItem.setActionCommand("quit");
		    quitItem.setEnabled(true);
		    quitItem.addActionListener(listener);
		    popup.add(quitItem);
		    
		    mUpdateItem = new MenuItem("Update Database");
		    mUpdateItem.setActionCommand("update");
		    mUpdateItem.setEnabled(true);
		    mUpdateItem.addActionListener(listener);
		    popup.add(mUpdateItem);
		    
		    trayIcon = new TrayIcon(image, "KB360", popup);
		    trayIcon.addActionListener(listener);

		    SystemTray tray = SystemTray.getSystemTray();		    
		    try {
		        tray.add(trayIcon);
			    trayIcon.displayMessage("KB360","Server options",MessageType.INFO);
		    } catch (AWTException e) {
		        System.err.println(e);
		    }
		} else {
			//TODO add window based menu when system tray is not supported.
			JOptionPane.showMessageDialog(null,"KB360 is not yet supported for your machine.");
		}
	}
	
	public void start() {
		mServer = new TCPServer(
				mConfig.getProperty("upload"),
				mConfig.getProperty("student"),
				mConfig.getProperty("admin"),
				mConfig.getProperty("httpURL")
								);
		
		mStartItem.setEnabled(false);
		mUpdateItem.setEnabled(false);
		mServer.start();
		mStopItem.setEnabled(true);
	}
	
	public void stop() {
		mStopItem.setEnabled(false);
		mServer.finishProccess();
		try {
			mServer.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mStartItem.setEnabled(true);
		mUpdateItem.setEnabled(true);
	}
	
	public void quit() {
		stop();
		System.exit(0);
	}
	
	//TODO
	public void showConfiguration() {
		
	}
	
	//TODO fill out the rest of the function.
	public void resetConfiguration() {
		String message = "Are you sure?";
		if(mServer.isRunning()) {
			message += " The server will be stoppped.";
		}
		
		int answer = JOptionPane.showConfirmDialog(null, message, "Reset configuration", 
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if(answer == JOptionPane.YES_OPTION) {
			if(mServer.isRunning()) {
				mServer.finishProccess();
			}
			//TODO reset configuration.
		} 	
	}
	
	public void updateIndexes() {
		File[] files = new File[2];
		files[0] = new File(mConfig.getProperty("student"));
		files[1] = new File(mConfig.getProperty("admin"));

		UpdateIndexes updater = new UpdateIndexes(files);
		updater.update();
		updater.showResults();
	}
	
	public static void main(String args[]) {
		new KB360();
	}
}
