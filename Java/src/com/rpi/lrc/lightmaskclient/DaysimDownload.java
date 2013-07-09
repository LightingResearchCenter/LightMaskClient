package com.rpi.lrc.lightmaskclient;

import java.awt.Toolkit;
import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import processing.core.PApplet;

public class DaysimDownload extends PApplet{
	// Variables for download ing from the daysimeter
	byte[] bytefile1, bytefile2; 		// Array of bytes of the two binary daysimeter file (1 = data, 2 = header)
	String[] headerstr;
	int[] EEPROM, header;
	DaysimDownloadThread dthread;		// Thread that gets the binary files
	FileDialog fileBrowser;
	boolean dataready;					// Flag indicating if the data is ready

	//for data organization
	float[] r, g, b, a, lux, CLA, CS;	// Arrays for the various data values (red, green, blue, activity, etc.)
	int ID;								// Daysimeter id number
	int endaddress;
	String[] timestamps;				// Array of the timestamps
	int mm, dd, yy, HH, MM, period;		// The date and times values gathered from the header file
	long[] matTime;

	float[] smac, vmac, mel, vp, vlam, CLAcal, RGBcal;  // The calibration values for calculating the Lux, CLA, and CS

	PApplet app; // Parent variable (lightmaskclient) for updating it's gui
  
	public DaysimDownload(PApplet papp) {
		//for downloading
		bytefile1 = new byte[130048];
		bytefile2 = new byte[1024];
		EEPROM = new int[130048];
		header = new int[1024];
		fileBrowser = new FileDialog();
		dataready = false;

		//for data organization
		r = new float[16256];
		g = new float[16256];
		b = new float[16256];
		a = new float[16256];
		lux = new float[16256];
		CLA = new float[16256];
		CS = new float[16256];
		endaddress = 16255;
		timestamps = new String[16256];
		//matTime = new long[16256];

		//for calibration
		smac = new float[3];
		vmac = new float[3];
		mel = new float[3];
		vp = new float[3];
		vlam = new float[3];
		RGBcal = new float[3];
		CLAcal = new float[4];
		
		app = papp;
	}

	//main downloading routine.  This gets the data from the Daysimeter, processes it, and saves it
	public void downloadData() {
		//Gets current time in millis
		int start = millis();
		//Creates a thread to grab the binary files from the daysimeter
		dthread = new DaysimDownloadThread();
		dthread.start();
		// Program will create a download percentage in the client that will increment until the download is complete
		dataready = false;
		while(!dataready) {
			if((millis() - start)/175 < 99) {
				LightMaskClient.setMainText("\n\n" + "Downloading: " + (millis() - start)/175 + "% completed");
			}
			else {
				LightMaskClient.setMainText("Downloading: " + 99 + "% completed");
			}
			delay(100);
		}
		dthread.quit();
		// Display that the download is done and play a beep
		LightMaskClient.setMainText("\n\n" + "Downloading: 100% completed");
		Toolkit.getDefaultToolkit().beep();
		delay(1000); 
		LightMaskClient.setMainText("Please disconnect the Daysimeter");
		LightMaskClient.dlComplete = true;
		
		// Add the binary data from the data file to the EEPROM byte array
		for(int i = 0; i < EEPROM.length; i++) {
			EEPROM[i] = bytefile1[i] & 0xFF;
		}
		// Add the binary data from the header file to the header byte array
		for(int i = 0; i < header.length; i++) {
			header[i] = bytefile2[i] & 0xFF;
		}
		organizeEEPROM();
		processData();
		savefile();
	}

	// Uses the calibration info to convert RGB into lux, CLA, and CS
	private void processData() {
		float S, M, VPR, VM;
		getCal();
		getTimestamps();
		
    
		for(int i = 0; i < r.length; i++) {
			// Calculate lux
			lux[i] = RGBcal[0]*vlam[0]*r[i] + RGBcal[1]*vlam[1]*g[i] + RGBcal[2]*vlam[2]*b[i];
  
			// Calculate CLA
			S = RGBcal[0]*smac[0]*r[i] + RGBcal[1]*smac[1]*g[i] + RGBcal[2]*smac[2]*b[i];
			M = RGBcal[0]*mel[0]*r[i] + RGBcal[1]*mel[1]*g[i] + RGBcal[2]*mel[2]*b[i];
			VPR = RGBcal[0]*vp[0]*r[i] + RGBcal[1]*vp[1]*g[i] + RGBcal[2]*vp[2]*b[i];
			VM = RGBcal[0]*vmac[0]*r[i] + RGBcal[1]*vmac[1]*g[i] + RGBcal[2]*vmac[2]*b[i];
  
			if(S > CLAcal[2]*VM) {
				CLA[i] = M + CLAcal[0]*(S - CLAcal[2]*VM) - CLAcal[1]*683*(1 - pow((float)2.71,  (float)(-VPR/4439.5)));
			}
			else {
				CLA[i] = M;
			}
			CLA[i] = CLA[i]*CLAcal[3];
			if(CLA[i] < 0) {
				CLA[i] = 0;
			}
  
			// Calculate CS
			CS[i] = (float) (.7*(1 - (1/(1 + pow((float)(CLA[i]/355.7), (float)1.1026)))));
  
			// Calculate activity
			a[i] = (float) (pow(a[i], (float).5) * .0039 * 4);
		}
	}
	
	// Saves the two binary files from the daysimeter and the processed data to the path in initi_run_flag.txt
	public void savefile() {
		// Loads the header from the daysimeter and gets the daysimeter ID
		String filename = LightMaskClient.autoget_startfile("log_info.txt");
		byte[] bytefile;
		bytefile = loadBytes(filename);
		String ID = str((1000*(bytefile[3] - 48) +  100*(bytefile[4] - 48) + 10*(bytefile[5] - 48) + (bytefile[6] - 48)));

		//Ensure date has YYMMDD_HHMM format
		Calendar date = new GregorianCalendar();
		String month, day, hour, minute;
		if (date.get(date.MONTH)+1 < 10)
			month = 0 + Integer.toString(date.get(date.MONTH)+1);
		else 
			month = Integer.toString(date.get(date.MONTH)+1);
		if (date.get(date.DATE) <10)
			day = 0 + Integer.toString(date.get(date.DATE));
		else
			day = Integer.toString(date.get(date.DATE));
		if (date.get(date.HOUR) < 10)
			hour = 0 + Integer.toString(date.get(date.HOUR));
		else 
			hour = Integer.toString(date.get(date.HOUR));
		if (date.get(date.MINUTE) < 10)
			minute = 0 + Integer.toString(date.get(date.MINUTE));
		else 
			minute = Integer.toString(date.get(date.MINUTE));
		String Date = Integer.toString(date.get(date.YEAR)).substring(2, 4) + month + day + "_" + hour + minute;

		// Loads the download path from the initial_run_flag.txt file and sets it as the savepath
		String settingsPath = new File("").getAbsolutePath() + "/src/data/initial_run_flag.txt";
		String[] settingsStrings = loadStrings(settingsPath);
		String savePath = settingsStrings[1] + "/Day" + ID + "_" + Date;		
		
		if (savePath != null) {  
			// Adds the processed data to an array of strings separated by tabs
			String[] processed = new String[endaddress + 1];
			processed[0] = "Time\tLux\tCLA\tCS\tActivity";
			for(int i = 0; i < endaddress; i++) {
				processed[i + 1] = timestamps[i] + "\t" +  str(lux[i]) + "\t" + str(CLA[i]) + "\t" + str(CS[i]) + "\t" + str(a[i]);
			}
			
			// Saves the file in savePath and saves the path in the processed path txt file
			String[] processedPath = new String[1];
			processedPath[0] = savePath + "_processed.txt";
			saveStrings("/src/data/daysimeter_processed_path.txt", processedPath);
			saveStrings(processedPath[0], processed);
			
			// Saves the 2 binary files from the daysimeter
			saveBytes(savePath + "_data.bin", bytefile1);
			saveBytes(savePath + "_header.bin", bytefile2);
			LightMaskClient.daysPathSet = true;
		}
	}
  
  // Organizes the EEPROM into RGBA and gets the device ID
  private void organizeEEPROM() {
	  
  
	  // Put red, green, blue, and activity values into r, g, b, and a
	  for(int i = 0; i < EEPROM.length; i += 8) {
		  r[(i)/8] = 256*EEPROM[i] + EEPROM[i + 1];
		  g[(i)/8] = 256*EEPROM[i + 2] + EEPROM[i + 3];
		  b[(i)/8] = 256*EEPROM[i + 4] + EEPROM[i + 5];
		  a[(i)/8] = 256*EEPROM[i + 6] + EEPROM[i + 7];
  
		  // Set looks for specific rgb values that indicate a reset and sets the values to 0
		  if((r[(i)/8] == 65278) && (b[(i)/8] == 65278) && (g[i/8] == 0)) {
			  r[(i)/8] = 0;
			  g[(i)/8] = 0;
			  b[(i)/8] = 0;
			  a[(i)/8] = 0;
		  }
	  }
  
	  // Get end of address
	  for(int i = 0; i < EEPROM.length; i += 8) {
		  if((EEPROM[i] == 255) && (EEPROM[i + 1] == 255)) {
			  endaddress = i/8;
			  break;
		  }
	  }
  
	  // Gets the ID, the intital start time, and period from the header
	  ID = (header[3] - 48)*1000 + (header[4] - 48)*100 + (header[5] - 48)*10 + (header[6] - 48);
	  mm = (header[9] - 48)*10 + (header[10] - 48);
	  dd = (header[12] - 48)*10 + (header[13] - 48);
	  yy = (header[15] - 48)*10 + (header[16] - 48);
	  HH = (header[18] - 48)*10 + (header[19] - 48);
	  MM = (header[21] - 48)*10 + (header[22] - 48);
	  period = (header[25] - 48)*100 + (header[26] - 48)*10 + (header[27] - 48);
  	}
  
  
  	// Pulls the calibration info from the cal files
  	private void getCal() {
  		int temp = 0;
  		int j = 0;
  		
  		String[] s = loadStrings("/src/data/Day12_Cal_Values.txt");
  		
  
  		// Scone/macula
  		for(int i = 0; i < s[1].length(); i++) {
  			if(s[1].charAt(i) == 9) {
  				if(temp != 0) {
  					smac[j] = Float.parseFloat(s[1].substring(temp, i));
  					j++;
  				}
  				temp = i;
  			}
  		}
  		smac[j] = Float.parseFloat(s[1].substring(temp, s[1].length()));
  		temp = 0;
  		j = 0;
  
  		// Vlamda/macula
  		for(int i = 0; i < s[2].length(); i++) {
  			if(s[2].charAt(i) == 9) {
  				if(temp != 0) {
  					vmac[j] = Float.parseFloat(s[2].substring(temp, i));
  					j++;
  				}
  				temp = i;
  			}
  		}
  		vmac[j] = Float.parseFloat(s[2].substring(temp, s[2].length()));
  		temp = 0;
  		j = 0;
  
  		// Melanopsin
  		for(int i = 0; i < s[3].length(); i++) {
  			if(s[3].charAt(i) == 9) {
  				if(temp != 0) {
  					mel[j] = Float.parseFloat(s[3].substring(temp, i));
  					j++;
  				}
  				temp = i;
  			}
    	}
  		mel[j] = Float.parseFloat(s[3].substring(temp, s[3].length()));
  		temp = 0;
  		j = 0;
  
  		// Vprime
  		for(int i = 0; i < s[4].length(); i++) {
  			if(s[4].charAt(i) == 9) {
  				if(temp != 0) {
  					vp[j] = Float.parseFloat(s[4].substring(temp, i));
  					j++;
  				}
  				temp = i;
  			}
  		}
  		vp[j] = Float.parseFloat(s[4].substring(temp, s[4].length()));
  		temp = 0;
  		j = 0;
  
  		// Vlamda
  		for(int i = 0; i < s[5].length(); i++) {
  			if(s[5].charAt(i) == 9) {
  				if(temp != 0) {
  					vlam[j] = Float.parseFloat(s[5].substring(temp, i));
  					j++;
  				}
  				temp = i;
  			}
  		}
  		vlam[j] = Float.parseFloat(s[5].substring(temp, s[5].length()));
  		temp = 0;
  		j = 0;
  
  		// CLA
  		for(int i = 0; i < s[8].length(); i++) {
  			if(s[8].charAt(i) == 9) {
  				if(temp != 0) {
  					CLAcal[j] = Float.parseFloat(s[8].substring(temp, i));
  					j++;
  				}
  				temp = i;
  			}
  		}
  		CLAcal[j] = Float.parseFloat(s[8].substring(temp, s[8].length()));
  		temp = 0;
  		j = 0;
  
  		// RGBcal
  		s = loadStrings("/src/data/Day12 RGB Values.txt");
  		for(int i = 0; i < s[ID].length(); i++) {
  			if(s[ID].charAt(i) == 9) {
  				if(temp != 0) {
  					RGBcal[j] = Float.parseFloat(s[ID].substring(temp, i));
  					j++;
        	}
  				temp = i;
  			}
    	}
  		RGBcal[j] = Float.parseFloat(s[ID].substring(temp, s[ID].length()));
  		temp = 0;
  		j = 0;
  	}
  
  	// Get standard formatted timestamps for the file
  	private void getTimestamps() {
  		// Creates a new calendar starting at the specified date
  		Calendar calendar = new GregorianCalendar(2007,Calendar.JANUARY,1);
  		calendar.set(2000 + yy, mm - 1, dd, HH, MM);
  		String[] tempstr = new String[6];
  
  		// Converts the dates to strings
  		for(int i = 0; i < r.length; i++) {
  			if(calendar.get(Calendar.YEAR) < 10) {
  				tempstr[0] = "0" + str(calendar.get(Calendar.YEAR));
  			}
  			else {
  				tempstr[0] = str(calendar.get(Calendar.YEAR));
  			}
  			if(calendar.get(Calendar.MONTH) < 9) {
  				tempstr[1] = "0" + str(calendar.get(Calendar.MONTH) + 1);
  			}
  			else {
  				tempstr[1] = str(calendar.get(Calendar.MONTH) + 1);
  			}
  			if(calendar.get(Calendar.DATE) < 10) {
  				tempstr[2] = "0" + str(calendar.get(Calendar.DATE));
  			}
  			else {
  				tempstr[2] = str(calendar.get(Calendar.DATE));
  			}
  			if(calendar.get(Calendar.HOUR) < 12) {
  				if(calendar.get(Calendar.AM_PM) == 1) {
  					tempstr[3] = str(calendar.get(Calendar.HOUR) + 12);
  				}
  				else if(calendar.get(Calendar.HOUR) < 10) {
  					tempstr[3] = "0" + str(calendar.get(Calendar.HOUR));
  				}
  				else {
  					tempstr[3] = str(calendar.get(Calendar.HOUR));
  				}
  			}
  			else {
  				tempstr[3] = str(calendar.get(Calendar.HOUR));
  			}
  			if(calendar.get(Calendar.MINUTE) < 10) {
  				tempstr[4] = "0" + str(calendar.get(Calendar.MINUTE));
  			}
  			else {
  				tempstr[4] = str(calendar.get(Calendar.MINUTE));
  			}
  			if(calendar.get(Calendar.SECOND) < 10) {
  				tempstr[5] = "0" + str(calendar.get(Calendar.SECOND));
  			}
  			else {
  				tempstr[5] = str(calendar.get(Calendar.SECOND));
  			}

  			// Adds the dates to an array of timestamps
  			timestamps[i] = tempstr[1] + "/" + tempstr[2] + "/" + tempstr[0] + " " + tempstr[3] + ":" + tempstr[4] + ":" + tempstr[5];
  			calendar.add(Calendar.SECOND, period);
  		}
  	}
}
