package com.rpi.lrc.lightmaskclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class DaysimDownloadThread extends Thread {
	DaysimDownloadThread() {}
	// Starts the thread
	public void start() {
		super.start();
	}

	// Runs until the files are loaded in and sets a flag to true to let the program know the data are available
	public void run() {
		// Grabs the data log and stores the file in the RAM
		RandomAccessFile data_log;
		try {
			data_log = new RandomAccessFile(LightMaskClient.autoget_startfile("data_log.txt"), "rw");
			data_log.read(LightMaskClient.download.bytefile1);
		} catch (FileNotFoundException e) {
			ErrorLog.write("Data log removed from RAM");
			e.printStackTrace();
		} catch (IOException e) {
			ErrorLog.write(e.getMessage());
			e.printStackTrace();
		}
		
		// Grabs the header and stores the file in the RAM
		RandomAccessFile log_info;
		try {
			log_info = new RandomAccessFile(LightMaskClient.autoget_startfile("log_info.txt"), "rw");
			log_info.read(LightMaskClient.download.bytefile2);
		} catch (FileNotFoundException e) {
			ErrorLog.write("Log info removed from RAM");
			e.printStackTrace();
		} catch (IOException e) {
			ErrorLog.write(e.getMessage());
			e.printStackTrace();
		}
		
		LightMaskClient.download.dataready = true;
	}
	  
	void quit() {
	   //interrupt();
	}
}
