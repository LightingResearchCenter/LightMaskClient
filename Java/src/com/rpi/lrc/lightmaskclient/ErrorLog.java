package com.rpi.lrc.lightmaskclient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import processing.core.PApplet;

public class ErrorLog extends PApplet{
	static String logPath;
	
	void setLogPath() {		
		String filePath = new File("").getAbsolutePath() + "/src/data/initial_run_flag.txt";
		String settingsFile[] = loadStrings(filePath);
		logPath = settingsFile[2];
	}
	
	public static void write(String s) {
		BufferedWriter writer;
		try {
			String fileName = logPath + "\\" + "log_" + month() + "-" + day() + "-" + year() + "_" +  hour() + "-" + minute() + ".txt";
			File file = new File(fileName);
		    file.createNewFile();
			println(fileName);
			writer = new BufferedWriter(new FileWriter(fileName, true));
			writer.write(s);
			writer.close();
		}
		catch (IOException e) {}
	}
	
	boolean isLogFile (String s) {
		String pattern = "[\\S]+(\\\\log\\.txt)$";
		return s.matches(pattern);
	}
}
