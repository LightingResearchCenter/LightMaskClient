package com.rpi.lrc.lightmaskclient;

//import lightmaskclient.Button;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import controlP5.ControlEvent;
import processing.core.PApplet;
import processing.core.PFont;

//TODO
	//Better exception handling

public class LightMaskClient extends PApplet {
	
	PFont f20, f28;				//Variables for processing font
	static TextArea taMain;		//Initializes the main text area for output 
	Panel mainPanel;			//The main window of the program
	static Date date = new Date("05/15/2012 13:12");

	//Colored Buttons
	Button downloadButton;
	Button programButton;

	//Buttons coordinates
	final int leftButtonX = 25;
	final int rightButtonX = 215;
	final int buttonY = 70;
	
	//Dropdown Research Menu
	DropdownMenu ddm;

	//StatusBars
	StatusBar DaysimStatus;
	StatusBar LightMaskStatus;
	boolean dayConnected = false;
	static boolean maskConnected = false;
	static boolean daysPathSet = false;
	
	static boolean calcComplete = true;
	static boolean dlComplete = false;
	static boolean progMaskStart = false;
	static boolean progMaskComplete = false;
	static boolean availWinOpen = false;
	boolean firstRun = true;
	static String initRunFlagPath = new File("").getAbsolutePath() + "/src/data/initial_run_flag.txt";

	//Device Managers
	static DaysimDownload download;
	static LightMaskManager maskManager;
	//FileDialog fileSelector;
	static MatlabODESolver odesolver;
	
	//Starts the processing main applet
	public static void main(String _args[]) {
		PApplet.main(new String[] { com.rpi.lrc.lightmaskclient.LightMaskClient.class.getName() });
	}	

	//Configure window and text
	public void setup() {
		checkIfRunning();					//Checks if there is another instance of the program running
		
		size(400, 500);						//Sets the size of the main window
		f28 = loadFont("Calibri-28.vlw"); 	//Loads the fonts 
		f20 = loadFont("Calibri-20.vlw");
		strokeWeight(5);					//Sets the stroke weight of the font
		
		DaysimStatus = new StatusBar(this, "Daysimeter", 0, 220);		//Creates a status bar for the Daysimeter
		LightMaskStatus = new StatusBar(this, "LightMask", 215, 184);	//Creates a status bar for the LightMask
		download = new DaysimDownload(this);							//Sets up the download for the daysimeter data
		maskManager = new LightMaskManager();							//Creates a LightMask Manager
		odesolver = new MatlabODESolver(this);								//Initializes the MatlabODE solver
		ddm = new DropdownMenu(this);									//Creates the dropdown menu
		System.out.println(new File("").getAbsolutePath());
		
		initTextArea();		//Creates the text area
		initButtons();		//Creates the buttons
		setPaths();			//Sets the paths for the log file and the raw/processed data files
	} 
	
	//Main Program Loop
	public void draw() {
		frameRate(20);
		background(0);
		textFont(f28, 28);
		
		downloadButton.display();
		programButton.display();
		statusbar_display();
		
		dayConnected = find_daysimeter();
		if (!maskConnected){
			//Tries to find the mask if it isn't connected
			maskConnected = maskManager.find_mask();
		}
		
		// Series of prompts the the user indicated by flags
		if (!dayConnected && dlComplete && !progMaskStart && !maskConnected) {
			setMainText("Daysimeter disconnected. Please connect the Light Mask to program it.");
		}
		
		else if (dayConnected && !dlComplete && !progMaskStart) {
			setMainText("Daysimeter connected. Press the red DOWNLOAD DATA button to download new data.");
		}
		
		else if (maskConnected && dlComplete && !progMaskStart && !availWinOpen) {
			setMainText("Light Mask connected. Please press the green PROGRAM MASK button to program the Light Mask.");
		}
		
		else if (!maskConnected && progMaskComplete && firstRun) {
			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					resetFlags();
				}
			};
			Timer timer = new Timer(30000, listener);
			timer.setRepeats(false);
			timer.start();
			firstRun = false;
		}
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////
////Event Handling
////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//Action if clicked on a button
	public void mousePressed() {
		if(calcComplete){
			//Download the data from the Daysimeter and store the location of the saved file in /data/daysimeter_processed_path.txt
			if (downloadButton.over() == true) {
				if (dayConnected){
					progMaskStart = false;
					dlComplete = false;
					download.downloadData();
			    }
				else{
					taMain.setText("Please Connect Daysimeter");
				}
			}
			
			//Set the Mask times based on the Daysimeter file and Matlab calculations
			else if (programButton.over() == true){
				if (daysPathSet == false){
					taMain.setText("Please select the processed data file to use by downloading it from the Daysimeter.");
				}
				else if (maskConnected){
					String dialogFlag[] = loadStrings(initRunFlagPath);
					if (dialogFlag[3].contentEquals("true")) {
						showAvailWinDialog();
					}
					else {
						calcMaskTimes();
					}
				}
				else {
					taMain.setText("LightMask not available. Please make sure that it is plugged in.");
				}
			}  
		}
	}
	
	//Checks for presses in the drop down menu
	public void controlEvent(ControlEvent event) {
		if (event.isGroup()) {
			//Load a daysimeter file for testing
			if (event.getValue() == 1) {
				loadFile();
			}

			//Check mask schedule
			else if (event.getValue() == 2) {
				checkSchedule();
			}
			
			//Check the current time of the mask
			else if (event.getValue() == 3) {
				maskManager.sendCommand("getClock:!");
			}
			
			//Set the mask time to the current time of the computer's clock and show it
			else if (event.getValue() == 4) {
				String date = year() + "," + month() + "," + day() + "," + hour() + "," + minute();
				maskManager.sendCommand("setClock:" + date + " !");
				maskManager.sendCommand("getClock:!");
			}
			
			// Check the current pulse duration, intensity, and repetition period
			else if (event.getValue() == 5) {
				checkPulseSettings();
			}
			
			//Create a dialog to change the pulse settings and then show the settings
			else if (event.getValue() == 6) {
				showPulseSettingsDialog();
			}
			
			// Creates a dialog to set the initial run values
			else if (event.getValue() == 7) {
				showInitRunValuesDialog();
			}
			
			// Sets the mask time and creates dialogs to set the mask pulse settings & initial run values
			else if (event.getValue() == 8) {
				String date = year() + "," + month() + "," + day() + "," + hour() + "," + minute();
				maskManager.sendCommand("setClock:" + date + " !");
				showPulseSettingsDialog();
				showInitRunValuesDialog();
			}
			
			// Toggles whether there is a popup for the user to set bed time
			else if (event.getValue() == 9) {
				String initRunFlag[] = loadStrings(initRunFlagPath);
				if (initRunFlag[3].contentEquals("true")) {
					initRunFlag[3] = "false";
				}
				else {
					initRunFlag[3] = "true";
				}
				saveStrings(initRunFlagPath, initRunFlag);
			}
			ddm.researchTools.setCaptionLabel("Research Menu");
		}
	}
	
	//Shows the research menu if SHIFT + R is pressed, 
	//and hides it if SHIFT + S is pressed
	public void keyPressed() {
		if (key == 'R') {
			ddm.researchTools.show();
		}
		else if (key == 'S') {
			ddm.researchTools.hide();
		}
		else if (key == 'I') {
			String workingDirectory = new String(System.getProperty("user.dir")+ "\\src\\");
			String s[] = loadStrings(workingDirectory + "data\\initial_run_flag.txt");
			s[0] = "true";
			saveStrings(workingDirectory + "data\\initial_run_flag.txt", s);
		}
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////
////Initialization
////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//initialize the GUI buttons
	private void initButtons(){		
		strokeWeight(5);
		rectMode(CENTER);
		
		//Creates download data button
		downloadButton = new Button(this, leftButtonX, buttonY, "DOWNLOAD\nDATA", ddm.cp5);
		downloadButton.setRecColor(255, 0, 0);
		downloadButton.setStrokeColor(150, 0, 0);
		
		//Creates program mask button
		programButton = new Button (this, rightButtonX, buttonY, "PROGRAM\nMASK", ddm.cp5);
		programButton.setRecColor(0, 125, 0);
		programButton.setStrokeColor(0, 75, 0);
	}
	
	//Set up main text for displaying information
	private void initTextArea(){
		//Creates and sets up the text area
		taMain = new TextArea("Welcome to the Daysimeter and Lightmask Programing Station. Please connect your Daysimeter.", 5, 5, 3);
		taMain.setFont(new Font("Calibri", Font.PLAIN, 18));
		taMain.setBackground(Color.darkGray);
		taMain.setForeground(Color.white);
		taMain.setEditable(false);
		
		//Creates the main panel and add the text area to it
		mainPanel = new Panel();  
		mainPanel.setLayout(new BorderLayout());
		add(mainPanel);
		mainPanel.setBounds(25, buttonY + 130, 350, 280);
		mainPanel.add(taMain, BorderLayout.CENTER);
		setLayout(new BorderLayout());
		mainPanel.setVisible(true);
	}
	
	//Status bars to display if each device is connected
	private void statusbar_display(){
		DaysimStatus.setStatus(find_daysimeter());
		LightMaskStatus.setStatus(maskConnected);
		DaysimStatus.display();
		LightMaskStatus.display();
	}
	
	//Creates the pulse settings dialog in a new window (frame)
	void showPulseSettingsDialog() {
		  Frame f = new Frame("Light Pulse Settings");
		  PulseSettings p = new PulseSettings(this, f, 175, 220);
		  f.add(p);
		  p.init();
		  
		  f.setTitle("Light Pulse Settings");
		  f.setSize(p.w, p.h);
		  f.setLocation(100, 100);
		  f.setResizable(false);
		  f.setVisible(true);
		}
	
	//Creates the initial run values dialog in a new window (frame)
	void showInitRunValuesDialog() {
		  Frame f = new Frame("Initial Run Values");
		  InitialRun w = new InitialRun(this, f, 175, 430);
		  f.add(w);
		  w.init();
		  
		  f.setTitle("Initial Run Values");
		  f.setSize(w.w, w.h);
		  f.setLocation(100, 100);
		  f.setResizable(false);
		  f.setVisible(true);
		}
	
	//Creates the initial run values dialog in a new window (frame)
	void showAvailWinDialog() {
		  Frame f = new Frame("Initial Run Values");
		  AvailWin w = new AvailWin(this, f, 175, 150, odesolver);
		  f.add(w);
		  w.init();
		  setMainText("Please make changes, if necessary, to your sleep and wake times " +
		  		"and press SUBMIT to continue.");
		  
		  f.setTitle("Initial Run Values");
		  f.setSize(w.w, w.h);
		  f.setLocation(100, 100);
		  f.setResizable(false);
		  f.setVisible(true);
		}

////////////////////////////////////////////////////////////////////////////////////////////////////////////
////File Handling
////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** This scans all drives to find the Daysimeter.
	  *  Daysimeter is identified as a drive with only two files that
	  *  were created on May 15, 2012 at 13:12.
	  */
	public boolean find_daysimeter() {
		String filename = autoget_startfile("data_log.txt");
		if(filename == "nothing") {
			return false;
		}
		else {
			return true;
		}
	}
	
	public static String autoget_startfile(String name) {
		File[] roots = File.listRoots(); 
	  
	    //scan all directories for data_log.txt
	    for(int i=0;i<roots.length;i++) {
	    	File f = new File((roots[i] + name));
	      
	    	if(f.exists()) {
	    		//if the file is found, make sure it's the only file in the directory
	    		if(roots[i].listFiles().length == 2) {
	          
	    			//if it is, make sure it was created at the right time (within 27.7 hours)
	    			if(abs(f.lastModified() - date.getTime()) < 100000000) {
	    				return f.getPath();
	    			}
	    		}
	    	}
	    }
	    return "nothing";
	}
	
	public static String runCommand(String cmd) {
		 try {
			 String command = "cmd /c " + cmd;

             Process p = Runtime.getRuntime().exec(command);
             BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
             BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
             String Error;
             while ((Error = stdError.readLine()) != null) {
                 System.err.println(Error);
             }
             
             String result = stdInput.readLine();
             if (result != null) {
            	 result = result.trim();
            	 System.out.println(result);
             }
             
             p.waitFor();
             return result;
		} 
	    catch (Exception e) {
	             e.printStackTrace();
	             return null;
	    }  
	}
	public static void remount(String path) {
		//String cmd = System.getProperty("user.dir") + "\\remount.bat " + path;
		replaceSelected(path.substring(0, 1));
		String scriptPath = new File("").getAbsolutePath() + "/diskpartScript.txt";
		String cmd = "diskpart /s " + scriptPath;
		System.out.println(cmd);
	    runCommand(cmd);
	}
	
	//Loads a processed file into the program
	public void loadFile () {
		FileDialog fileSelector = new FileDialog();
		taMain.setText("Select File to Load");
		String[] loadPath = new String[1];
		loadPath[0] = fileSelector.selectInput("Select Processed File");
		if (loadPath[0] == null){
			daysPathSet = false;
		}
		else {
			saveStrings("/src/data/daysimeter_processed_path.txt", loadPath);
			taMain.append("\nFile  at location:\n" + loadPath[0] + "\nhas been loaded");
			daysPathSet = true;
		}
	}
	
	public static void replaceSelected(String driveLetter) {
	    try {
	        // input the file content to the String "input"
            String scriptPath = new File("").getAbsolutePath() + "/diskpartScript.txt";
	        BufferedReader in_file = new BufferedReader(new FileReader(scriptPath));
	        String line;
	        String input = "";

	        line = in_file.readLine();
	        line = line.substring(0, line.length() - 1) + driveLetter;
	        input += line + '\n';
	        while ((line = in_file.readLine()) != null)
	        	input += line + '\n';
	        in_file.close();

	        // check if the new input is right
	        System.out.println("----------------------------------"  + '\n' + input);

	        // write the new String with the replaced line OVER the same file
	        FileOutputStream out_file = new FileOutputStream(scriptPath);
	        out_file.write(input.getBytes());
	        out_file.close();

	    } catch (Exception e) {
	        System.out.println("Problem reading file.");
	    }
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////
////Getters and setters
////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//Checks the mask schedule and outputs it to the text area
	public void checkSchedule () {
		if( maskConnected){
			taMain.setText("                  Light Mask Schedule\n");
			taMain.append("             ON                               OFF\n");
			maskManager.sendCommand("getOn:!");
			
			//allow MSP430 time to respond
			try {
			    Thread.sleep(500);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			maskManager.sendCommand("getOff:!");
			
			//allow MSP430 time to respond
			try {
			    Thread.sleep(500);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
			//Adds values to text area if there are values
			for (int i = 0; i < 7; i++){
				if (!maskManager.getMaskSchedule(i).isEmpty()){
					taMain.append(i + maskManager.getMaskSchedule(i));
				}
			}
			appendMainText("\nPlease disconnect the Light Mask.");
			progMaskComplete = true;
		}
		else {
			taMain.setText("LightMask not available. Please make sure that it is plugged in.");
		}
	}
	
	//Checks the current pulse settings of the mask
	public void checkPulseSettings () {
		taMain.setText("");
		
		//Get the pulse duration and wait
		maskManager.sendCommand("getDur:!");
		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//Get the pulse intensity and wait
		maskManager.sendCommand("getInt:!");
		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//Get the pulse repetition period
		maskManager.sendCommand("getRep:!");
	}
	
	//Set the pulse settings
	public void setPulseSettings (String pulseDur, String pulseInt, String pulseRep) {
		taMain.setText("");
		
		//Set the pulse duration and wait
		maskManager.sendCommand("pulseDur:" + pulseDur + "!");
		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//Set the pulse intensity and wait
		maskManager.sendCommand("pulseInt:" + pulseInt + "!");
		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//Set the pulse repetition period
		maskManager.sendCommand("pulseRep:" + pulseRep + "!");
	}
	
	public void testPulseSettings(String pulseInt, String pulseDur) {
		maskManager.sendCommand("calFlash:" + pulseInt + "," + pulseDur + "!");
		try {
			wait(Long.parseLong(pulseDur));
		}
		catch (Exception e) {}
	}
	
	public static LightMaskManager getMaskMan(){
		return maskManager;
	}
	
	//Check if the calculation has finished
	public static void calcStatus(boolean status){
		calcComplete = status;
	}
	
	//Sets the text in the text area
	public static void setMainText(String mtext){
		taMain.setText(mtext);
	}
	
	//Appends text in the text area
	public static void appendMainText(String atext){
		taMain.append(atext);
	}	
	
	public static TextArea getFrame() {
		return taMain;
	}
	
	private static final int PORT = 9999;
	private static ServerSocket socket;    

	//Only runs the program if it is not already running
	private static void checkIfRunning() {  
	  try {
	    //Bind to localhost adapter with a zero connection queue 
	    socket = new ServerSocket(PORT,0,InetAddress.getByAddress(new byte[] {127,0,0,1}));
	  }
	  catch (BindException e) {
	    System.err.println("Already running.");
	    JOptionPane.showMessageDialog(taMain, "LightMask Client already running.");
	    System.exit(1);
	  }
	  catch (IOException e) {
	    System.err.println("Unexpected error.");
	    e.printStackTrace();
	    System.exit(2);
	  }
	}
	
	//Sets the paths for the daysimeter files and the log files
	void setPaths () {
		String settingsPath = "/src/data/initial_run_flag.txt";
		String[] settingsStrings = loadStrings(settingsPath);
		FileDialog fileBrowser = new FileDialog();
		String daySavePath = null, logSavePath = null;
		
		// Create a dialog for user to choose a folder for Daysim files if it doesn't exist
		if (settingsStrings.length <= 1) {
			while (daySavePath == null) {
				daySavePath = fileBrowser.selectFolder("Choose Daysim Folder");
			}
			settingsStrings = append(settingsStrings, daySavePath);
		}
		
		// Create a dialog for user to choose a folder for log files if it doesn't exist
		if (settingsStrings.length <= 2) {
			while (logSavePath == null) {
				logSavePath = fileBrowser.selectFolder("Choose Log Folder");
			}
			settingsStrings = append(settingsStrings, logSavePath);
		}
		
		// Flag for toggling whether the user inputs their bed time or not
		if (settingsStrings.length <= 3) {
			settingsStrings = append(settingsStrings, "true");
		}
		saveStrings(settingsPath, settingsStrings);
		// Creates the error log instance and sets its path
		ErrorLog l = new ErrorLog();
		l.setLogPath();
		
	}
	
	void calcMaskTimes() {
		LightMaskClient.setMainText("Calculating on/off times, please wait...");
		//Loads settings file
		String[] firstRun = loadStrings("/src/data/initial_run_flag.txt");
		LightMaskClient.progMaskStart = true;
		
		// If this is the initial calculation use CBTmin file
		if (firstRun[0].toLowerCase().contains("true")){
			LightMaskClient.appendMainText("\nInitial Run");
			odesolver.calculateInitial();
			
			//Sets the initial run flag to false 
			firstRun[0] = "false";										
			saveStrings("/src/data/initial_run_flag.txt", firstRun);
		}
		// Else use x0xc0 file
		else{
			odesolver.calculate();
		}
		LightMaskClient.daysPathSet = false;
	}
	
	// Resets all of the flags for the client and resets the main text message
	void resetFlags() {
		calcComplete = true;
		dlComplete = false;
		progMaskStart = false;
		progMaskComplete = false;
		availWinOpen = false;
		firstRun = true;
		setMainText("Welcome to the Daysimeter and Light Mask Programing Station. Please connect your Daysimeter.");
	}
}

