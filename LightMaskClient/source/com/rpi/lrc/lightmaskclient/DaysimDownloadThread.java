package com.rpi.lrc.lightmaskclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class DaysimDownloadThread extends Thread {
	DaysimDownloadThread() {}
	//starts the thread
	public void start() {
		super.start();
	}

	//runs until the files are loaded in and sets a flag to true to let the program know the data are available
	public void run() {
		//InputStream is1 = new ByteArrayInputStream(LightMaskClient.autoget_startfile("data_log.txt").getBytes());
		//LightMaskClient.download.bytefile1 = LightMaskClient.loadBytes(is1);
		RandomAccessFile data_log;
		try {
			data_log = new RandomAccessFile(LightMaskClient.autoget_startfile("data_log.txt"), "rw");
			//byte[] b = new byte[(int)data_log.length()];
			data_log.read(LightMaskClient.download.bytefile1);
		} catch (FileNotFoundException e) {
			ErrorLog.write("Data log removed from RAM");
			e.printStackTrace();
		} catch (IOException e) {
			ErrorLog.write(e.getMessage());
			e.printStackTrace();
		}
		
		RandomAccessFile log_info;
		try {
			log_info = new RandomAccessFile(LightMaskClient.autoget_startfile("log_info.txt"), "rw");
			//byte[] b = new byte[(int)log_info.length()];
			log_info.read(LightMaskClient.download.bytefile2);
		} catch (FileNotFoundException e) {
			ErrorLog.write("Log infro removed from RAM");
			e.printStackTrace();
		} catch (IOException e) {
			ErrorLog.write(e.getMessage());
			e.printStackTrace();
		}
		
		//InputStream is2 = new ByteArrayInputStream(LightMaskClient.autoget_startfile("log_info.txt").getBytes());
		//LightMaskClient.download.bytefile2 = LightMaskClient.loadBytes(is2);
		LightMaskClient.download.dataready = true;
	}
	  
	void quit() {
	   //interrupt();
	}
}
