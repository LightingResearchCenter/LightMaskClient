package com.rpi.lrc.lightmaskclient;

import java.io.File;

public class FileDialog extends LightMaskClient {
	
	String result;
	boolean done;
	
	public void select(File file) {
		result = file!=null?file.toString():result;
		done = true;
	}
	
	public String selectFolder() {
		return selectFolder("");
	}
	
	public String selectFolder(String prompt) {
		FileDialog select = new FileDialog();
		selectFolder(prompt, "select", null, select);
		while(!select.done) {}
			return select.result;
	}
	
	public String selectInput() {
		return selectInput("");
	}
	
	public String selectInput(String prompt) {
		FileDialog select = new FileDialog();
		selectInput(prompt, "select", null, select);
		while(!select.done) { print("");}
			return select.result;
	}

	public String selectOutput() {
		return selectOutput("");
	}
	public String selectOutput(String name) {
		FileDialog select = new FileDialog();
		File defaultSave = new File(name);
		selectOutput("Select location to save log data", "select", defaultSave, select);
		while(!select.done) { print("");}
			return select.result;
	}

	public void fileSelectedOpen(File selection) {
		if (selection == null){
			println("Window was closed or canceled.");
		}
		else {
			println("User selected " + selection.getAbsolutePath());
		}
	}

	public void fileSelectedInput(File selection) {
		if (selection == null){
			println("Window was closed or canceled.");
		}
		else {
			println("User selected " + selection.getAbsolutePath());
		}
	}
}
