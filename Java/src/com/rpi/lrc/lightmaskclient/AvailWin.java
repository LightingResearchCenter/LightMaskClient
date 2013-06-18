package com.rpi.lrc.lightmaskclient;

import java.awt.Frame;

import processing.core.PApplet;

import controlP5.ControlP5;
import controlP5.Textfield;

public class AvailWin extends PApplet {
	int w, h;								// Width and height of the window
	Frame fr;								//Frame containing the applet
	ControlP5 cp5;							//Cp5 object that contains the text fields and buttons
	LightMaskClient parent;					//The main application object
	Textfield txt [] = new Textfield [2];	//
	MatlabODESolver odeSolver;

	public void setup() {
		size(w, h);
		frameRate(25);
		cp5 = new ControlP5(this);
		String existingValues[] = loadStrings("/src/data/Lightmask_last_values.txt");
		if (existingValues.length < 9) {
			existingValues = loadStrings("/src/data/Lightmask_initial_values.txt");
		}
		
		txt[0] = cp5.addTextfield("Available Start Time (0-24)");
		txt[1] = cp5.addTextfield("Available End Time (0-24)");
		
		for (int i = 0; i < 2; i++) {
			txt[i].setWidth(150)
				.setPosition(10, 40 * i + 10)
				.setAutoClear(false)
				.setText(existingValues[3 + i]);
		}
		
		cp5.addButton("Submit")
			.setPosition(10, 90);
		cp5.addButton("Cancel")
			.setPosition(90, 90);
	}
	
	public void draw() {
		background(100);
		areValuesValid();
	}
	
	public AvailWin(LightMaskClient theParent, Frame f, int theWidth, int theHeight, MatlabODESolver ode) {
		parent = theParent;
		w = theWidth;
		h = theHeight;
		fr = f;
		odeSolver = ode;
	}
	
	void Submit(int theValue) {		
		
		if (!areValuesValid()) {
			return;
		}
		
		odeSolver.availStartTime = txt[0].getText();
		odeSolver.availEndTime = txt[1].getText();
		
		fr.dispose();
		
		LightMaskClient.setMainText("Calculating on/off times, please wait...");
		String[] firstRun = loadStrings("/src/data/initial_run_flag.txt");	//Loads settings file
		LightMaskClient.progMaskStart = true;
		
		//If this is the initial calculation use CBTmin file
		if (firstRun[0].toLowerCase().contains("true")){
			LightMaskClient.appendMainText("\nInitial Run");
			odeSolver.calculateInitial();
			
			firstRun[0] = "false";										
			saveStrings("/src/data/initial_run_flag.txt", firstRun);	//Sets the initial run flag to false 
		}
		//else use x0xc0 file
		else{
			odeSolver.calculate();
		}
		LightMaskClient.daysPathSet = false;
	}

	void Cancel(int theValue) {
		LightMaskClient.setMainText("");
		fr.dispose();
	}
	
	public boolean isFloat(String s) {
		try { 
			Float.parseFloat(s); 
		} catch(NumberFormatException e) { 
			return false; 
		}
		// only got here if we didn't return false
		return true;
	}

	boolean isInRange (float n, float min, float max) {
		return ((n > min) && (n < max));
	}
	
	boolean areValuesValid () {
		boolean valid = true;
		for (int i = 0; i < 2; i++) {
				valid &= isValidValue(txt[i], 0, 24);
		}
		return valid;
	}

	boolean isValidValue(Textfield t, float min, float max) {
		if(isFloat(t.getText()) && isInRange(Float.parseFloat(t.getText()), min, max)) {
			t.setColorForeground(color(45, 185, 0));
			return true;
		}
		else {
			t.setColorForeground(color(183, 0, 0));
			return false;
		}
	}
}
