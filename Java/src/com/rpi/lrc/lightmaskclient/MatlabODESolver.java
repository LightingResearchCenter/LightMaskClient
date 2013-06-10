package com.rpi.lrc.lightmaskclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JOptionPane;


import processing.core.PApplet;

public class MatlabODESolver extends PApplet{
	
	Process process;
	OutputStream stdin;
	InputStream stdout;
	boolean calc_complete;
	String[] ODEresponse1;
	String[] ODEresponse2;
	Calendar[] onTimes;
	Calendar[] offTimes;
	Vector log;
	String[] logArray;
	String X, XC, endtime;
	String matlabconsole;
	LightMaskManager maskManager;
	String workingDirectory;
	String[] processed_file;
	File tempDir;
	boolean isParseComplete = false;
	String[] labels = {"Subject ID", "CBTmin", "CBTminTarget", "availStartTime", "availEndTime", "Tau", "maskLightLevel (CS)", "maxDur", "Mask Color", "X", "XC0", "time0"};
	
	public MatlabODESolver(){
		calc_complete = false;
		maskManager = new LightMaskManager();  
	    log = new Vector(6, 6);
	    workingDirectory = new String(System.getProperty("user.dir")+ "\\src\\");  
	    tempDir = new File(workingDirectory);  
	}
	
	//Uses CBTmin Matlab console file for the initial calculation
	public void calculateInitial(String CBTminInitial, String CBTminTarget, String starttime, String endtime, String tau, String lightlevel, String maxDur, String maskColor){
		//LightMaskClient.setMainText(workingDirectory);
		LightMaskClient.calcStatus(false);
		processed_file = loadStrings(workingDirectory + "data\\daysimeter_processed_path.txt");
		logArray = loadStrings(workingDirectory + "data\\Lightmask_initial_values.txt");
		onTimes = new Calendar[8];
		offTimes = new Calendar[8];
		
		File CBTminPath = new File(workingDirectory + "CBTmin.exe");
		
		//Grab file location for matlab program
		if (CBTminPath.isFile()) {  
		    matlabconsole = new String(workingDirectory + "CBTmin.exe");
		}
		else {
			LightMaskClient.setMainText("CMTmin file missing");
			ErrorLog.write("CMTmin file missing");
		}
		
		//Run matlab program from the command line in a new process
		try {   
			LightMaskClient.setMainText("Start calc");
			process = new ProcessBuilder("cmd", "/c", matlabconsole, 
					processed_file[0], CBTminInitial, CBTminTarget, starttime, endtime, tau, lightlevel, maxDur, maskColor).start();
		} 
		catch (IOException e) {
			JOptionPane.showMessageDialog(LightMaskClient.getFrame(), "IO.");
			ErrorLog.write(e.getMessage());
			e.printStackTrace();
		}
	    parseResponse(); 
	}
	
	//Uses x0xc0 Matlab console file for the calculation
	public void calculate(){
		//LightMaskClient.setMainText(workingDirectory);
		LightMaskClient.calcStatus(false);
		logArray = loadStrings(workingDirectory + "data\\Lightmask_last_values.txt");
		processed_file = loadStrings(workingDirectory + "data\\daysimeter_processed_path.txt");
		onTimes = new Calendar[8];
		offTimes = new Calendar[8];
		if (tempDir.exists()) {  
		    matlabconsole = new String(workingDirectory + "x0xc0.exe");
		}
		else {
			LightMaskClient.setMainText("File not found");
		}
		try {
			System.out.println(processed_file[0]+" "+logArray[2]+" "+logArray[9]+" "+logArray[10]+" "+logArray[11]+" "+
				logArray[3]+" "+logArray[4]+" "+logArray[5]+" "+logArray[6]+ " "+ logArray[12]+ " "+ logArray[13]+ " "+ 
				logArray[14]+ " "+ logArray[15]+ " "+ logArray[16]+ " "+ logArray[17]+ " "+ logArray[7]+ " "+ logArray[8]);
			/*for (String s : logArray) {
				LightMaskClient.appendMainText("\n" + s);
			}*/
			
			//LightMaskClient.setMainText(logArray[2]);
			
			process = new ProcessBuilder("cmd", "/c", matlabconsole, 
					processed_file[0], logArray[2], logArray[9], logArray[10], "\"" + logArray[11]+ "\"", logArray[3], 
					logArray[4], logArray[5], logArray[6], "\"" + logArray[12] + "\"", "\"" + logArray[13] + "\"",
					"\"" + logArray[14] + "\"", "\"" + logArray[15] + "\"", "\"" + logArray[16] + "\"", 
					"\"" + logArray[17] + "\"", logArray[7], logArray[8]).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ErrorLog.write(e.getMessage());
			e.printStackTrace();
		}
		parseResponse();
	}
	
	//deals with the response from the matlab calculation
	public void parseResponse(){
		
		stdin = process.getOutputStream();
	    stdout = process.getInputStream();
	    
	    //LightMaskClient.setMainText("PreParse");
		//Create new thread to redirect stdout
	    new Thread(new Runnable() {
	        public void run() {
	        	ODEresponse1 = new String[7];
	    	    ODEresponse2 = new String[7];
	        	String line;
	            int i = -1;
	            int j = 0;
	            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
	            
	            /*Format of the matlab output:
	             * ans = 
	             *
	             * value1
	             * value2
	             * etc
	             * ans = 
	             * 
	             * diffvalue1
	             */
	            LightMaskClient.setMainText("Start parse");
	            try {
	            	//Read the lines from the command window until they're null
	            	//LightMaskClient.appendMainText("Begin");
	                while ((line = br.readLine()) != null) {
	                	//System.out.println(line);
	                	LightMaskClient.setMainText("Parsing");
	                	//Only read if the line if it's not empty
	                	if (line.length() != 0 ){
	                		//If line conatains ans = move onto the next line
	                		if(line.contains("ans =")){
	                			i++;
	                			j=0;
	                		}
	                		else{
	                			//i represents the sets of data delimited by ans =
		                		switch (i) {
		                        case 0: ODEresponse1[j] = line;
		                        		j++;
		                        		break;
		                        case 1: ODEresponse2[j] = line;
		                        		j++;
		                        		break;
		                        case 2: X = line.substring(3);
		                        		logArray[9] = X;
		                                break;
		                        case 3: XC = line.substring(3);
		                        		logArray[10] = XC;
		                                break;
		                        case 4: endtime = line;
		                        		logArray[11] = endtime;
		                        		break;
		                        default:
		                        		break;
		                		}
	                		}
	                	}
	                }
	                
	                LightMaskClient.appendMainText("\nCheck");
	                for (String s : ODEresponse1) {
	                	LightMaskClient.appendMainText("\n" + s);
	                }
	                for (String s : ODEresponse2) {
	                	LightMaskClient.appendMainText("\n" + s);
	                }

	                //LightMaskClient.setMainText("PostRead");
	                LightMaskClient.setMainText("Start format");
	                //Writes the values if the dates can be parsed as date objects
	                if (formatResponse()){
	                	//LightMaskClient.setMainText("PostFormat");
		                saveStrings(workingDirectory + "\\data\\Lightmask_last_values.txt", logArray);
		                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		                Calendar cal = Calendar.getInstance();
		                
		                //append the datalog.txt with new data and update Lightmask_last_values.txt
		                try {
		                    PrintWriter log = new PrintWriter(new FileWriter(workingDirectory + "\\data\\datalog.txt", true));
		                    log.println("Log from: " + dateFormat.format(cal.getTime()));
		                    final int nArgs = labels.length;
		                    for (int k = 0; k < logArray.length && k < nArgs; k++){
		                    	log.println(labels[k] + ": " + logArray[k]);
		                    }
		                    
		                    log.println("On Times:");
		                    for (int q = 0; q < 3; q++){
		                    	log.println(ODEresponse1[q]);
		                    	logArray[nArgs + q] = ODEresponse1[q];
		                    }
		                    
		                    log.println("Off Times:");
		                    for (int t = 0; t<3; t++){
		                    	log.println(ODEresponse2[t]);
		                    	logArray[nArgs + 3 + t] = ODEresponse2[t];
		                    }
		                    log.println("");
		                    
		                    log.close();
		                    saveStrings(workingDirectory + "\\data\\Lightmask_last_values.txt", logArray);
		                    
		                } catch (IOException e) {
		                	ErrorLog.write(e.getMessage());
		                	e.printStackTrace();
		                }
		                LightMaskClient.appendMainText("\nCalculation Complete");
		                LightMaskManager maskMan = LightMaskClient.getMaskMan(); 
		        		maskMan.sendTimes(onTimes, offTimes);
	                }
	                
	                //Otherwise the calculation ran into an error
	                else{
	                	LightMaskClient.appendMainText("\nCalculation error");
	                	ErrorLog.write("Calculation Error in Parse Response");
	                }
	                LightMaskClient.calcStatus(true);
	                process.destroy();
	            } 
	            catch (IOException e) {
	            	ErrorLog.write(e.getMessage());
	                e.printStackTrace();
	            }
	            isParseComplete = true;
	        }
	    }).start();
	    
	    //Creates a 3 dot loop for feedback on calculation time
	    /*int n = 4;
	    while (!isParseComplete) {
	    	if (n >= 3) {
	    		LightMaskClient.setMainText("Calculating on/off times, please wait");
	    		n = 0;
	    	}
	    	else {
	    		LightMaskClient.appendMainText(".");
	    		n++;
	    	}
	    	delay(500);
	    }*/
	    //process.destroy();
	}
	
	//Convert times to Calendar object
	public boolean formatResponse(){
		boolean response = false;
		for (int i =0; i < 6; i++){
			if (ODEresponse1[i] != null){
				onTimes[i] = convertResponse(ODEresponse1[i]);
				response = true;
			}
			if (ODEresponse2[i] != null){
				offTimes[i] = convertResponse(ODEresponse2[i]);
				response = true;
			}
		}
		return response;
	}
	
	//Parse the date and time strings a calendar
	private Calendar convertResponse(String formattedDate){
		int year = Integer.parseInt(formattedDate.substring(7, 11));	
		int month = convertMonth(formattedDate.substring(3, 7));
		int day = Integer.parseInt(formattedDate.substring(0, 2));
		int hour = Integer.parseInt(formattedDate.substring(12, 14));
		int minute = Integer.parseInt(formattedDate.substring(15, 17));
		int second = Integer.parseInt(formattedDate.substring(18));
		Calendar cal = new GregorianCalendar(year, month, day, hour, minute, second);
		return cal;
	}
	
	//Parse the 3 letter month string as a calendar object
	private int convertMonth(String month) {
		Date date;
		try {
			date = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(month);
			Calendar cal = Calendar.getInstance();
		    cal.setTime(date);
		    return cal.get(Calendar.MONTH)+1;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			ErrorLog.write(e.getMessage());
			e.printStackTrace();
		}
	    return 0;
	}
}
