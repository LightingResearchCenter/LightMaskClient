package com.rpi.lrc.lightmaskclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;


public class DaysimDownloadThread extends Thread {
	DaysimDownloadThread() {}
	// Starts the thread
	public void start() {
		super.start();
	}

	// Runs until the files are loaded in and sets a flag to true to let the program know the data are available
	public void run() {
		byte[] b, prev_b = null;
		try {
			// Get daysimeter path and datalog filepath 
			String filepath = LightMaskClient.autoget_startfile("data_log.txt");
			Path path = Paths.get(filepath);
			String parentPath = path.getParent().toString();

			// Read the datalog into the byte array
			System.out.println("Start reading");
			b = Files.readAllBytes(path);
			System.out.println("End reading");
			
			// Check if previous byte array is equal to the current one
			while (!Arrays.equals(b, prev_b)) {
				// Remount the daysimeter and wait 5 seconds
				LightMaskClient.remount(parentPath);
				// Wait for 1 second
				try {
				    Thread.sleep(1000);
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
				
				// Get the datalog filepath 
				do {
					filepath = LightMaskClient.autoget_startfile("data_log.txt");
					System.out.println("finding log");
				} while (filepath.equals("nothing"));
				path = Paths.get(filepath);
				
				// Set previous bytes to current and clear the datalog in RAM
				prev_b = b;
				System.out.println("Start reading");
				b = Files.readAllBytes(path);
				System.out.println("Done reading");
			}
			LightMaskClient.download.bytefile1 = Files.readAllBytes(path);
		} 
		catch (FileNotFoundException e) {
			ErrorLog.write("Data log removed from RAM");
			e.printStackTrace();
		} 
		catch (IOException e) {
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
