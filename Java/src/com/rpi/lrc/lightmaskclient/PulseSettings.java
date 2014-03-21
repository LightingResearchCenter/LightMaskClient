package com.rpi.lrc.lightmaskclient;

import java.awt.Frame;

import processing.core.PApplet;
import controlP5.*;

public class PulseSettings extends PApplet {

	  int w, h;
	  Frame fr;
	  Textfield txt [] = new Textfield [3];
	  
	  public String pulseDur, pulseInt, pulseRep;
	  
	  
	  
	  public void setup() {
	    size(w, h);
	    frameRate(25);
	    cp5 = new ControlP5(this);
	    
	    // Creates the text input boxes
	    txt[0] = cp5.addTextfield("Pulse Duration");
	    txt[1] = cp5.addTextfield("Pulse Intensity");
	    txt[2] = cp5.addTextfield("Pulse Repetition Period");
	    for (int i = 0; i < 3; i++) {
	    	txt[i].setWidth(150)
	    		.setPosition(10, 40 * i + 10);
	    }
	    
	    // Create buttons to check the leds and to submit and cancel
	    cp5.addButton("ledCheck")
	    	.setPosition(10, 130);
	    cp5.addButton("Submit")
	    	.setPosition(10, 160);
	    cp5.addButton("Cancel")
    		.setPosition(90, 160);
	  }

	  public void draw() {
	      background(100);
	      checkValidValues();
	  }
	  
	  // Sets the size of the window
	  public PulseSettings(LightMaskClient theParent, Frame f, int theWidth, int theHeight) {
	    parent = theParent;
	    w = theWidth;
	    h = theHeight;
	    fr = f;
	    pulseRep = pulseInt = pulseDur = null;
	  }


	  public ControlP5 control() {
	    return cp5;
	  }
	  
	  void ledCheck (int theValue) {
		  if (checkValidValues()) {
			  String tempInt = txt[1].getText();
			  String tempDur = txt[0].getText();
			  parent.testPulseSettings(tempInt, tempDur);
		  }
	  }
	  
	  ControlP5 cp5;

	  LightMaskClient parent;
	  
	  // Handles what happens when a button is pressed
	  public void controlEvent(ControlEvent theEvent) {
		  if(theEvent.getName() == "Submit") {
			  // Gets the text from the text boxes
			  pulseDur = txt[0].getText();
			  pulseInt = txt[1].getText();
			  pulseRep = txt[2].getText();
			  
			  // Can only submit if they are valid values
			  if (!checkValidValues()) {
				  return;
			  }
			  
			  println(pulseDur + "\n" + pulseInt + "\n" + pulseRep);
			  parent.setPulseSettings(pulseDur, pulseInt, pulseRep);
			  fr.dispose();		  
		  }
		  
		  else if(theEvent.getName() == "Cancel") {
			  fr.dispose();
		  }
		}
	  
	  // Returns true if the value is between one of the values
	  boolean isInRange (int n, int min, int max) {
			return ((n > min) && (n < max));
		}
	  
	  // Returns true if string can be parsed to an int
	  public static boolean isInteger(String s) {
		    try { 
		        Integer.parseInt(s); 
		    }
		    catch(NumberFormatException e) { 
		        return false; 
		    }
		    // only got here if we didn't return false
		    return true;
		}
	  
	  // Checks whether all the values are within the specified range
	  boolean checkValidValues () {
			boolean valid = true;
			for (int i  = 0; i < 3; i++) {
				valid &= isValidValue(txt[i], 0, 9999);
			}
			return valid;
		}

	  	// Checks whether a textfield value is value and sets the outline of the box accordingly
		boolean isValidValue(Textfield t, int min, int max) {
			if(isInteger(t.getText()) && isInRange(Integer.parseInt(t.getText()), min, max)) {
				t.setColorForeground(color(45, 185, 0));
				return true;
			}
			else {
				t.setColorForeground(color(183, 0, 0));
				return false;
			}
		}
	}