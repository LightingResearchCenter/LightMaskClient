package com.rpi.lrc.lightmaskclient;

//import lightmaskclient.Button;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Panel;
import java.awt.TextArea;
import java.io.File;
import java.util.Date;

import processing.core.PApplet;
import processing.core.PFont;

//TODO
	//Better exception handling


public class LightMaskClient extends PApplet {
	
	PFont f20, f28;
	static TextArea taMain;
	Panel mainPanel;
	static Date date = new Date("05/15/2012 13:12");

	//Colored Buttons
	Button downloadButton;
	Button checkButton;
	Button loadButton;
	Button programButton;

	//StatusBars
	StatusBar DaysimStatus;
	StatusBar LightMaskStatus;
	boolean dayConnected = false;
	static boolean maskConnected = false;
	static boolean daysPathSet = false;
	static boolean calcComplete = true;

	//Device Managers
	static DaysimDownload download;
	static LightMaskManager maskManager;
	FileDialog fileSelector;
	static MatlabODESolver odesolver;
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { com.rpi.lrc.lightmaskclient.LightMaskClient.class.getName() });
	}	

	//Configure window and text
	public void setup() {
		size(400, 600);
		background(0);
		f28 = loadFont("Calibri-28.vlw"); 
		f20 = loadFont("Calibri-20.vlw");
		frame.setTitle("Daysimeter and Lightmask Client");
		strokeWeight(5);
		DaysimStatus = new StatusBar(this, "Daysimeter", 0, 187);
		LightMaskStatus = new StatusBar(this, "LightMask", 215, 184);
		download = new DaysimDownload(this);
		maskManager = new LightMaskManager();
		fileSelector = new FileDialog();
		odesolver = new MatlabODESolver();
		initTextArea();
		initButtons();
	} 
	
	//Main Program Loop
	public void draw() {
		frameRate(20);
		textFont(f28, 28);
		downloadButton.display();
		checkButton.display();
		loadButton.display();
		programButton.display();
		statusbar_display();
		dayConnected = find_daysimeter();
		if (!maskConnected){
			maskConnected = maskManager.find_mask();
		}
		
	}
	
	//Action if clicked on a button
	public void mousePressed() {
		if(calcComplete){
			//Download the data from the Daysimeter and store the location of the saved file in /data/daysimeter_processed_path.txt
			if (downloadButton.over() == true){
				if (dayConnected){
					download.downloadData();
			    }
				else{
					taMain.setText("Please Connect Daysimeter");
				}
			}
			//Display the Mask Schedule
			else if (checkButton.over() == true){
				if( maskConnected){
					taMain.setText("                  LightMask Schedule\n");
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
					for (int i = 0; i < 7; i++){
						if (!maskManager.getMaskSchedule(i).isEmpty()){
							taMain.append(i + maskManager.getMaskSchedule(i));
						}
					}
				}
				else {
					taMain.setText("LightMask not available. Please make sure that it is plugged in and this is the only client running.");
				}
			}
			//Save location of processed data file in /data/daysimeter_processed_path.txt
			else if (loadButton.over() == true){
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
			//Set the Mask times based on the Daysimeter file and Matlab calculations
			else if (programButton.over() == true){
				if (daysPathSet == false){
					taMain.setText("Please select the processed data file to use by downloading it from the Daysimeter or loading it from your computer using the buttons on the left.");
				}
				else if (maskConnected){
					taMain.setText("Calculating on/off times, please wait...");
					String[] firstRun = loadStrings("/src/data/initial_run_flag.txt");
					//If this is the initial calculation us CBTmin file
					if (firstRun[0].toLowerCase().contains("true")){
						String[] values = loadStrings("/src/data/Lightmask_initial_values.txt");
						appendMainText("\nInitial Run");
						odesolver.calculateInitial(values[1], values[2], values[3], values[4], values[5], values[6]);
						firstRun[0] = "false";
						saveStrings("/src/data/initial_run_flag.txt", firstRun);
					}
					//else use x0xc0 file
					else{
						odesolver.calculate();
					}
					daysPathSet = false;
				}
				else {
					taMain.setText("LightMask not available. Please make sure that it is plugged in and this is the only client running.");
				}
			}  
		}
	}
	
	

	//initialize the GUI buttons
	private void initButtons(){		
		strokeWeight(5);
		rectMode(CENTER);
		downloadButton = new Button(this, 105, 100, "DOWNLOAD\nDATA");
		downloadButton.setRecColor(255, 0, 0);
		downloadButton.setStrokeColor(150, 0, 0);
		checkButton = new Button(this, 295, 100, "CHECK MASK\nSCHEDULE");
		checkButton.setRecColor(0,0,255);
		checkButton.setStrokeColor(0,0,155);
		loadButton = new Button(this, 105, 225, "LOAD DATA\nFROM FILE");
		loadButton.setRecColor(235, 235, 0);
		loadButton.setStrokeColor(135,135,0);
		programButton = new Button (this, 295, 225, "PROGRAM\nMASK");
		programButton.setRecColor(0, 125, 0);
		programButton.setStrokeColor(0, 75, 0);
	}
	
	//Set up main text for displaying inforation
	private void initTextArea(){
		taMain = new TextArea("Welcome to the Daysimeter and Lightmask Programing Station.", 5, 5, 3);
		taMain.setFont(new Font("Calibri", Font.PLAIN, 18));
		taMain.setBackground(Color.darkGray);
		taMain.setForeground(Color.white);
		mainPanel = new Panel();  
		mainPanel.setLayout(new BorderLayout());
		add(mainPanel);
		mainPanel.setBounds(25, 300, 350, 280);
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

	//TODO
		//Find a better way of doing this 
	
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
	
	//getters and setters
	
	public static LightMaskManager getMaskMan(){
		return maskManager;
	}
	
	public static void calcStatus(boolean status){
		calcComplete = status;
	}
	
	
	public static void setMainText(String mtext){
		taMain.setText(mtext);
	}
	
	public static void appendMainText(String atext){
		taMain.append(atext);
	}
}