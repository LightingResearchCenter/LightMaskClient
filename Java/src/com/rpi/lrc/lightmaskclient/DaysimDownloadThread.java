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
		// Grabs the data log and stores the file in the RAM
		//RandomAccessFile data_log;
		//File data_log;
		byte[] b, prev_b = null;
		try {
			// Get daysimeter path and datalog filepath 
			String filepath = LightMaskClient.autoget_startfile("data_log.txt");
			Path path = Paths.get(filepath);
			String parentPath = path.getParent().toString();
			// Read the datalog into ram
			System.out.println("Start reading");
			//data_log = new RandomAccessFile(filepath, "r");
			//b = new char[(int) data_log.length()];
			//data_log.read(b);
			b = Files.readAllBytes(path);

			System.out.println("End reading");
			System.out.println(b);
			
			// Check if previous byte array is equal to the current one
			while (!Arrays.equals(b, prev_b)) {
				// Remount the daysimeter and wait 5 seconds
				//LightMaskClient.remount(parentPath);
				System.out.println("Sleeping");
				try {
				    Thread.sleep(5000);
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
				//data_log.close();
				System.out.println("Start reading");
				b = Files.readAllBytes(path);
				//data_log = new RandomAccessFile(filepath, "r");
				System.out.println("Done reading");
				
				// Read the datalog into the byte array
				//b = new byte[(int) data_log.length()];
				//data_log.read(b);
			}

			System.out.print(b);
			System.out.println(prev_b);
			LightMaskClient.download.bytefile1 = Files.readAllBytes(path);
			// Get get the datalog with read-write access
			//data_log = new RandomAccessFile(filepath, "rw");
			//data_log.read(LightMaskClient.download.bytefile1);
			//data_log.close();
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
