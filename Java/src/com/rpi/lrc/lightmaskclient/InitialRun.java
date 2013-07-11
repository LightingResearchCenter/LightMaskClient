package com.rpi.lrc.lightmaskclient;

import java.awt.Frame;

import processing.core.PApplet;
import controlP5.*;

public class InitialRun extends PApplet {

	int w, h;								// Width and height of the window
	Frame fr;								//Frame containing the applet
	ControlP5 cp5;							//Cp5 object that contains the text fields and buttons
	LightMaskClient parent;					//The main application object
	Textfield txt [] = new Textfield [8];	//

	//String CBTmin, CBTminTarget, availStartTime, availEndTime, tau, lightLevel, maxDur, maskColor;

	public void setup() {
		println("Test");
		size(w, h);
		frameRate(25);
		cp5 = new ControlP5(this);
		String existingValues[] = loadStrings("/src/data/Lightmask_initial_values.txt");

		txt[0] = cp5.addTextfield("Subject Number");
		txt[1] = cp5.addTextfield("CBTmin Initial (0-24)");
		txt[2] = cp5.addTextfield("CBTmin Target (0-24)");
		txt[3] = cp5.addTextfield("Available Start Time (0-24)");
		txt[4] = cp5.addTextfield("Available End Time (0-24)");
		txt[5] = cp5.addTextfield("Tau (18-30)");
		txt[6] = cp5.addTextfield("Mask CS Level (0-0.7)");
		txt[7] = cp5.addTextfield("Max Duration (0-24)");
		
		for (int i = 0; i < 8; i++) {
			txt[i].setWidth(150)
				.setPosition(10, 40 * i + 10)
				.setAutoClear(false)
				.setText(existingValues[i]);
		}
		
		Boolean isBlue = true;
		if (existingValues[8].contentEquals("red")) {
			isBlue = false;
		}
		cp5.addToggle("Mask Color")
		.setValue(isBlue)
		.setColorActive(color(0, 164, 237))
		.setColorBackground(color(215, 0, 0))
		.setPosition(10, 330);
		
		cp5.addButton("Submit")
		.setPosition(10, 370);
		cp5.addButton("Cancel")
		.setPosition(90, 370);
		
	}

	public void draw() {
		background(100);
		CheckValidValues();
	}

	public InitialRun(LightMaskClient theParent, Frame f, int theWidth, int theHeight) {
		parent = theParent;
		w = theWidth;
		h = theHeight;
		fr = f;
	}

	public ControlP5 control() {
		return cp5;
	}

	void Submit(int theValue) {		
		String initValues [] = new String [10];
		
		for (int i = 0; i < 8; i++) {
			initValues[i] = txt[i].getText();
		}
		
		initValues[9] = "===============Only use numbers above this line\n" +
				"Format of this file is in order:\n" +
				"Subject ID\n" +
				"CBTmin\n" +
				"CBTmin target\n" +
				"availStartTime\n" +
				"availEndTime\n" +
				"Tau\n" +
				"maskLightLevel (CS)\n" +
				"maxDur\n" +
				"maskColor\n";

		if (!CheckValidValues()) {
			return;
		}

		if (cp5.get(Toggle.class, "Mask Color").getState()) {
			initValues[8] = "blue";
		}
		else {
			initValues[8] = "red";
		}
		String initFlag[] = loadStrings("/src/data/initial_run_flag.txt");
		initFlag[0] = "true";
		saveStrings("/src/data/initial_run_flag.txt", initFlag);
		saveStrings("/src/data/Lightmask_initial_values.txt", initValues);

		for (String s : initValues) {
			println(s + "\n");
		}
		
		fr.dispose();
	}

	void Cancel(int theValue) {
		fr.dispose();
	}

	public static boolean isFloat(String s) {
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
	
	boolean CheckValidValues () {
		boolean valid = true;
		for (int i = 0; i < 8; i++) {
			if ((i > 0 && i < 5) || i == 7) {
				valid &= isValidValue(txt[i], 0, 24);
			}
			else if (i == 5) {
				valid &= isValidValue(txt[i], 18, 30);
			}
			else if (i == 6) {
				valid &= isValidValue(txt[i], 0, (float) 0.7);
			}
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